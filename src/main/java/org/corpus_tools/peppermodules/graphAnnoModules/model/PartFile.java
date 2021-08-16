package org.corpus_tools.peppermodules.graphAnnoModules.model;

import java.util.List;

public class PartFile {

  private List<Node> nodes;
  private List<Edge> edges;

  public List<Edge> getEdges() {
    return edges;
  }

  public void setEdges(List<Edge> edges) {
    this.edges = edges;
  }

  public List<Node> getNodes() {
    return nodes;
  }

  public void setNodes(List<Node> nodes) {
    this.nodes = nodes;
  }

}
