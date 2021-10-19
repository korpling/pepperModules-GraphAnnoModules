package org.corpus_tools.peppermodules.graphAnnoModules.model;

import java.util.List;
import java.util.Map;

public class Node {

  private long id;
  private Double start;
  private Double end;
  private NodeType type;
  private Map<String, Object> attr;
  private List<String> layer;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public NodeType getType() {
    return type;
  }

  public void setType(NodeType type) {
    this.type = type;
  }


  public Map<String, Object> getAttr() {
    return attr;
  }

  public void setAttr(Map<String, Object> attr) {
    this.attr = attr;
  }

  public List<String> getLayer() {
    return layer;
  }

  public void setLayer(List<String> layer) {
    this.layer = layer;
  }

  public Double getStart() {
    return start;
  }

  public void setStart(Double start) {
    this.start = start;
  }

  public Double getEnd() {
    return end;
  }

  public void setEnd(Double end) {
    this.end = end;
  }



}
