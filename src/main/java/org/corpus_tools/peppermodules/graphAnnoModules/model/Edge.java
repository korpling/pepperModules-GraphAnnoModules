package org.corpus_tools.peppermodules.graphAnnoModules.model;

import java.util.List;
import java.util.Map;

public class Edge {
  private long id;
  private long start;
  private long end;
  private EdgeType type;
  private Map<String, Object> attr;
  private List<String> layer;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getStart() {
    return start;
  }

  public void setStart(long start) {
    this.start = start;
  }

  public long getEnd() {
    return end;
  }

  public void setEnd(long end) {
    this.end = end;
  }

  public EdgeType getType() {
    return type;
  }

  public void setType(EdgeType type) {
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


}
