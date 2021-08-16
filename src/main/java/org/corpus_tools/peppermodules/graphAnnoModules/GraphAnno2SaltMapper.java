package org.corpus_tools.peppermodules.graphAnnoModules;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotationContainer;

public class GraphAnno2SaltMapper extends PepperMapperImpl {
  @Override
  public DOCUMENT_STATUS mapSDocument() {

    // Load JSON file
    try (FileReader reader =
        new FileReader(new File(getResourceURI().toFileString()), StandardCharsets.UTF_8)) {
      Gson gson = new Gson();
      PartFile partFile = gson.fromJson(reader, PartFile.class);

      mapToken(partFile);

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
      saltToken.setName("" + graphAnnoNode.getId());
      mapAttributes(graphAnnoNode.getAttr(), saltToken);
    }
    
  }

  private void mapAttributes(Map<String, Object> attributes, SAnnotationContainer saltObject) {
    for (Map.Entry<String, Object> a : attributes.entrySet()) {
      // TODO: handle namespaces
      saltObject.createAnnotation(null, a.getKey(), a.getValue());
    }
  }

}
