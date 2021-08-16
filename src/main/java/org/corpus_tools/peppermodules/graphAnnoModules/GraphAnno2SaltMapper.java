package org.corpus_tools.peppermodules.graphAnnoModules;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

public class GraphAnno2SaltMapper extends PepperMapperImpl {
  @Override
  public DOCUMENT_STATUS mapSDocument() {
    
    // Load JSON file
    try (FileReader reader =
        new FileReader(new File(getResourceURI().toFileString()),
            StandardCharsets.UTF_8)) {
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
        .collect(Collectors.toMap(n -> n.getId(), n -> n));
    // Sort all ordering edges by their start node index
    Comparator<Edge> startCmp = Comparator.comparingLong(Edge::getStart);
    List<Edge> orderingEdges =
        f.getEdges().parallelStream().filter(e -> e.getType() == EdgeType.o)
            .filter(e -> tokenById.containsKey(e.getStart()) && tokenById.containsKey(e.getEnd()))
            .sorted(startCmp).collect(Collectors.toList());
    
    // Create token strings for each ordering edges
    List<String> tokenTexts = orderingEdges.stream().map(e -> e.getStart())
        .map(id -> (String) tokenById.get(id).getAttr().get("token")).collect(Collectors.toList());
    // Add last token
    tokenTexts.add((String) tokenById.get(orderingEdges.get(orderingEdges.size() - 1).getEnd())
        .getAttr().get("token"));
    
    //
    SDocumentGraph g = getDocument().getDocumentGraph();
    STextualDS ds = g.createTextualDS("");
    g.insertTokensAt(ds, 0, tokenTexts, true);
  }

}
