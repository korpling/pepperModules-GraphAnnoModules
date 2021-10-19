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

public class MasterFile {

  private long version;
  private Map<String, Object> info;
  private Map<String, Object> conf;
  private List<Object> tagset;
  private Map<String, Object> anno_makros;
  private List<Object> search_makros;
  private List<Object> annotators;
  private Map<String, Object> file_settings;

  private List<String> files;
  private long max_node_id;
  private long max_edge_id;


  private List<Object> nodes;
  private List<Object> edges;

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public Map<String, Object> getInfo() {
    return info;
  }

  public void setInfo(Map<String, Object> info) {
    this.info = info;
  }

  public Map<String, Object> getConf() {
    return conf;
  }

  public void setConf(Map<String, Object> conf) {
    this.conf = conf;
  }

  public List<Object> getTagset() {
    return tagset;
  }

  public void setTagset(List<Object> tagset) {
    this.tagset = tagset;
  }

  public Map<String, Object> getAnno_makros() {
    return anno_makros;
  }

  public void setAnno_makros(Map<String, Object> anno_makros) {
    this.anno_makros = anno_makros;
  }

  public List<Object> getSearch_makros() {
    return search_makros;
  }

  public void setSearch_makros(List<Object> search_makros) {
    this.search_makros = search_makros;
  }

  public List<Object> getAnnotators() {
    return annotators;
  }

  public void setAnnotators(List<Object> annotators) {
    this.annotators = annotators;
  }

  public Map<String, Object> getFile_settings() {
    return file_settings;
  }

  public void setFile_settings(Map<String, Object> file_settings) {
    this.file_settings = file_settings;
  }

  public List<String> getFiles() {
    return files;
  }

  public void setFiles(List<String> files) {
    this.files = files;
  }

  public long getMax_node_id() {
    return max_node_id;
  }

  public void setMax_node_id(long max_node_id) {
    this.max_node_id = max_node_id;
  }

  public long getMax_edge_id() {
    return max_edge_id;
  }

  public void setMax_edge_id(long max_edge_id) {
    this.max_edge_id = max_edge_id;
  }

  public List<Object> getNodes() {
    return nodes;
  }

  public void setNodes(List<Object> nodes) {
    this.nodes = nodes;
  }

  public List<Object> getEdges() {
    return edges;
  }

  public void setEdges(List<Object> edges) {
    this.edges = edges;
  }



}
