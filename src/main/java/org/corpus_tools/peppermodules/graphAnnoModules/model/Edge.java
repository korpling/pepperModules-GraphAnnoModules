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
