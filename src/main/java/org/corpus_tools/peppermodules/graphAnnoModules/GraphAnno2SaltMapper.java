package org.corpus_tools.peppermodules.graphAnnoModules;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

  private final Map<Long, SToken> tokenById = new HashMap<>();
  private final Map<Long, SStructure> structById = new HashMap<>();

  @Override
  public DOCUMENT_STATUS mapSDocument() {

    // Load JSON file
    try (FileReader reader =
        new FileReader(new File(getResourceURI().toFileString()), StandardCharsets.UTF_8)) {
      Gson gson = new Gson();
      PartFile partFile = gson.fromJson(reader, PartFile.class);

      mapToken(partFile);
      mapSentences(partFile);
      mapAnnotationNodes(partFile);
      mapAnnotationEdges(partFile);

      return DOCUMENT_STATUS.COMPLETED;
    } catch (IOException ex) {
      throw new PepperModuleException("Could not read part file " + getResourceURI(), ex);
    }
  }

  private void mapToken(PartFile f) {

    Map<Long, Node> tokenById = f.getNodes().parallelStream().filter(n -> n.getType() == NodeType.t)
        .collect(Collectors.toMap(Node::getId, n -> n));
    Map<Long, Long> connectedToken =
        f.getEdges().parallelStream().filter(e -> e.getType() == EdgeType.o)
            .collect(Collectors.toMap(Edge::getStart, Edge::getEnd));

    // Find the token node with no incoming ordering edge
    Set<Long> tokenHasIncomingOrdering = new HashSet<>(connectedToken.values());
    Optional<Node> rootNode = tokenById.values().parallelStream()
        .filter(n -> !tokenHasIncomingOrdering.contains(n.getId())).findAny();

    List<String> tokenTexts = new LinkedList<>();
    List<Node> tokenNodes = new ArrayList<>();
    if (rootNode.isPresent()) {
      Node currentNode = rootNode.get();
      // Traverse the ordering edges from the root node
      while (currentNode != null) {
        tokenTexts.add(currentNode.getAttr().get("token").toString());
        tokenNodes.add(currentNode);

        Long nextTokenId = connectedToken.get(currentNode.getId());
        if (nextTokenId == null) {
          currentNode = null;
        } else {
          currentNode = tokenById.get(nextTokenId);
        }
      }
    }

    SDocumentGraph g = getDocument().getDocumentGraph();
    STextualDS ds = g.createTextualDS("");
    List<SToken> token = g.insertTokensAt(ds, 0, tokenTexts, true);
    for (int i = 0; i < token.size() && i < tokenNodes.size(); i++) {
      SToken saltToken = token.get(i);
      Node graphAnnoNode = tokenNodes.get(i);
      this.tokenById.put(graphAnnoNode.getId(), saltToken);
      saltToken.setName("" + graphAnnoNode.getId());
      mapAttributes(graphAnnoNode.getAttr(), saltToken);
    }
  }


  private void mapSentences(PartFile f) {
    SDocumentGraph g = getDocument().getDocumentGraph();

    // Get all sentence nodes
    Map<Long, Node> sentenceNodesById =
        f.getNodes().parallelStream().filter(n -> n.getType() == NodeType.s)
            .collect(Collectors.toMap(Node::getId, n -> n));
    for (Map.Entry<Long, Node> entry : sentenceNodesById.entrySet()) {
      // Get all token belonging to this node
      Set<SToken> coveredToken = f.getEdges().parallelStream()
          .filter(e -> e.getType() == EdgeType.s && e.getStart() == entry.getKey())
          .map(e -> this.tokenById.get(e.getEnd())).filter(Objects::nonNull)
          .collect(Collectors.toSet());
      // Create span node for this sentence
      SSpan sentenceSpan = g.createSpan(new LinkedList<>(coveredToken));
      SSentenceAnnotation sentenceAnno = SaltFactory.createSSentenceAnnotation();
      sentenceSpan.addAnnotation(sentenceAnno);
    }
  }
  
  private void mapAnnotationNodes(PartFile f) {
	 SDocumentGraph g = getDocument().getDocumentGraph();
	 f.getNodes().stream().filter(n -> n.getType() == NodeType.a).forEach(n -> {
		SStructure struct =  SaltFactory.createSStructure();
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
		  if(target == null) {
			  target = this.tokenById.get(e.getEnd());
		  }
		  if(source != null && target != null) {
			  SDominanceRelation rel = SaltFactory.createSDominanceRelation();
			  mapAttributes(e.getAttr(), rel);
			  rel.setSource(source);
			  rel.setTarget(target);
			  g.addRelation(rel);
		  }
	  });
  }

  private void mapAttributes(Map<String, Object> attributes, SAnnotationContainer saltObject) {
    for (Map.Entry<String, Object> a : attributes.entrySet()) {
      // TODO: handle namespaces
      saltObject.createAnnotation(null, a.getKey(), a.getValue());
    }
  }

}
