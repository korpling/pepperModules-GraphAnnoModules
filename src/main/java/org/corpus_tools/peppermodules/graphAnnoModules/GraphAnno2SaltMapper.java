/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.corpus_tools.peppermodules.graphAnnoModules;

import com.google.common.collect.ComparisonChain;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.peppermodules.graphAnnoModules.model.Edge;
import org.corpus_tools.peppermodules.graphAnnoModules.model.EdgeType;
import org.corpus_tools.peppermodules.graphAnnoModules.model.Node;
import org.corpus_tools.peppermodules.graphAnnoModules.model.NodeType;
import org.corpus_tools.peppermodules.graphAnnoModules.model.PartFile;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotationContainer;
import org.corpus_tools.salt.semantics.SSentenceAnnotation;

public class GraphAnno2SaltMapper extends PepperMapperImpl {

  private static final String TOKEN = "token";
  private final Map<Long, Node> nodeById = new HashMap<>();
  private final Map<Long, SToken> tokenById = new HashMap<>();
  private final Map<Long, SStructure> structById = new HashMap<>();

  @Override
  public DOCUMENT_STATUS mapSDocument() {

    nodeById.clear();
    tokenById.clear();
    structById.clear();

    // Load JSON file
    try (FileReader reader =
        new FileReader(new File(getResourceURI().toFileString()), StandardCharsets.UTF_8)) {
      Gson gson = new Gson();
      PartFile partFile = gson.fromJson(reader, PartFile.class);

      nodeById.putAll(
          partFile.getNodes().parallelStream().collect(Collectors.toMap(Node::getId, n -> n)));

      // Create an empty data source for the graph
      SDocumentGraph g = getDocument().getDocumentGraph();
      STextualDS ds = g.createTextualDS("");

      for (Node sentence : getSortedSentenceNodes(partFile)) {
        mapSentence(partFile, sentence, ds);
      }

      mapAnnotationNodes(partFile);
      mapAnnotationEdges(partFile);

      return DOCUMENT_STATUS.COMPLETED;
    } catch (IOException ex) {
      throw new PepperModuleException("Could not read part file " + getResourceURI(), ex);
    }
  }

  private List<Node> getSortedSentenceNodes(PartFile f) {
    // Find the sentence node with no incoming ordering edge
    Map<Long, Long> connectedNodes =
        f.getEdges().parallelStream().filter(e -> e.getType() == EdgeType.o)
            .collect(Collectors.toMap(Edge::getStart, Edge::getEnd));
    Set<Long> hasIncomingOrdering = new HashSet<>(connectedNodes.values());

    Map<Long, Node> sentenceById = f.getNodes().parallelStream()
        .filter(n -> n.getType() == NodeType.s).collect(Collectors.toMap(Node::getId, n -> n));
    Optional<Node> rootNode = sentenceById.values().parallelStream()
        .filter(n -> !hasIncomingOrdering.contains(n.getId())).findAny();

    List<Node> orderedSentenceNodes = new ArrayList<>();
    if (rootNode.isPresent()) {
      Node currentNode = rootNode.get();
      // Traverse the ordering edges from the root node
      while (currentNode != null) {
        orderedSentenceNodes.add(currentNode);

        Long nextSentenceId = connectedNodes.get(currentNode.getId());
        if (nextSentenceId == null) {
          currentNode = null;
        } else {
          currentNode = sentenceById.get(nextSentenceId);
        }
      }
    }
    return orderedSentenceNodes;
  }

  private List<Node> getSortedTokenNodes(PartFile f, Collection<Node> tokenNodes) {
    // Find the token node with no incoming ordering edge
    Map<Long, Long> connectedNodes =
        f.getEdges().parallelStream().filter(e -> e.getType() == EdgeType.o)
            .collect(Collectors.toMap(Edge::getStart, Edge::getEnd));
    Set<Long> hasIncomingOrdering = new HashSet<>(connectedNodes.values());

    Map<Long, Node> graphAnnoTokenById =
        tokenNodes.parallelStream().collect(Collectors.toMap(Node::getId, n -> n));
    Set<Node> rootNodes = graphAnnoTokenById.values().parallelStream()
        .filter(n -> !hasIncomingOrdering.contains(n.getId())).collect(Collectors.toSet());

    List<Node> orderedTokenNodes = new ArrayList<>();
    if (rootNodes.size() == 1) {
      Node currentNode = rootNodes.iterator().next();
      // Traverse the ordering edges from the root node
      while (currentNode != null) {
        orderedTokenNodes.add(currentNode);

        Long nextTokenId = connectedNodes.get(currentNode.getId());
        if (nextTokenId == null) {
          currentNode = null;
        } else {
          currentNode = graphAnnoTokenById.get(nextTokenId);
        }
      }
    } else {
      // There are no ordering edges for all token, try to order token by their time code
      orderedTokenNodes.addAll(tokenNodes);
      orderedTokenNodes.sort((n1, n2) -> ComparisonChain.start()
          .compare(n1.getStart(), n2.getStart()).compare(n1.getEnd(), n2.getEnd()).result());
    }
    return orderedTokenNodes;
  }


  private void mapSentence(PartFile f, Node sentence, STextualDS ds) {
    SDocumentGraph g = getDocument().getDocumentGraph();

    // Get all token belonging to this node
    Set<Node> coveredTokenIds = f.getEdges().parallelStream()
        .filter(e -> e.getType() == EdgeType.s && e.getStart() == sentence.getId())
        .map(e -> this.nodeById.get(e.getEnd())).filter(Objects::nonNull)
        .filter(n -> n.getType() == NodeType.t).collect(Collectors.toSet());

    // Sort the tokens by the ordering edges
    List<Node> sortedToken = getSortedTokenNodes(f, coveredTokenIds);
    List<String> tokenTexts = sortedToken.stream().map(n -> n.getAttr().get(TOKEN).toString())
        .collect(Collectors.toList());

    // Add the tokens for this sentence to the graph
    List<SToken> createdToken = g.insertTokensAt(ds, ds.getText().length(), tokenTexts, true);
    // Create helper maps for the created tokens and map their annotations
    for (int i = 0; i < sortedToken.size() && i < sortedToken.size(); i++) {
      SToken saltToken = createdToken.get(i);
      Node graphAnnoNode = sortedToken.get(i);
      this.tokenById.put(graphAnnoNode.getId(), saltToken);
      saltToken.setName("" + graphAnnoNode.getId());
      mapAttributes(graphAnnoNode.getAttr(), saltToken);
    }

    // Create span node for this sentence
    SSpan sentenceSpan = g.createSpan(new LinkedList<>(createdToken));
    SSentenceAnnotation sentenceAnno = SaltFactory.createSSentenceAnnotation();
    sentenceSpan.addAnnotation(sentenceAnno);

  }

  private void mapAnnotationNodes(PartFile f) {
    SDocumentGraph g = getDocument().getDocumentGraph();
    f.getNodes().stream().filter(n -> n.getType() == NodeType.a).forEach(n -> {
      SStructure struct = SaltFactory.createSStructure();
      this.structById.put(n.getId(), struct);
      mapAttributes(n.getAttr(), struct);
      g.addNode(struct);
    });
  }

  private void mapAnnotationEdges(PartFile f) {
    SDocumentGraph g = getDocument().getDocumentGraph();
    f.getEdges().stream().filter(e -> e.getType() == EdgeType.a).forEach(e -> {
      // Try to get source and target nodes
      SStructure source = this.structById.get(e.getStart());
      SStructuredNode target = this.structById.get(e.getEnd());
      if (target == null) {
        target = this.tokenById.get(e.getEnd());
      }
      if (source != null && target != null) {
        SDominanceRelation rel = SaltFactory.createSDominanceRelation();
        mapAttributes(e.getAttr(), rel);
        rel.setSource(source);
        rel.setTarget(target);
        g.addRelation(rel);
      }
    });
  }

  private void mapAttributes(Map<String, Object> attributes, SAnnotationContainer saltObject) {
    if (attributes != null) {
      for (Map.Entry<String, Object> a : attributes.entrySet()) {
        // TODO: handle namespaces
        if (!(saltObject instanceof SToken) || !TOKEN.equals(a.getValue())) {
          saltObject.createAnnotation(null, a.getKey(), a.getValue());
        }
      }
    }
  }

}
