/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 *
 */
package org.corpus_tools.peppermodules.graphAnnoModules;

import com.google.common.io.Files;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.peppermodules.graphAnnoModules.model.MasterFile;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

@Component(name = "GraphAnnoImporterComponent", factory = "PepperImporterComponentFactory")
public class GraphAnnoImporter extends PepperImporterImpl {
  public GraphAnnoImporter() {
    super();
    setName("GraphAnnoImporter");
    setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
    setSupplierHomepage(
        URI.createURI("https://github.com/korpling/pepperModules-GraphAnnoModules"));
    setDesc(
        "This import imports the format from  the GraphAnno tool (https://github.com/LBierkandt/graph-anno). ");
    // set list of formats supported by this module
    addSupportedFormat("GraphAnno", "11", null);
  }

  @Override
  public Double isImportable(URI corpusPath) {
    // Check for the "master.json" file
    File masterFile = new File(corpusPath.appendSegment("master.json").toFileString());
    return masterFile.isFile() ? 1.0 : 0.0;
  }

  @Override
  public PepperMapper createPepperMapper(Identifier sElementId) {
    GraphAnno2SaltMapper mapper = new GraphAnno2SaltMapper();
    mapper.setResourceURI(getIdentifier2ResourceTable().get(sElementId));
    return (mapper);
  }

  @Override
  public void importCorpusStructure(SCorpusGraph corpusGraph) throws PepperModuleException {
    // Create a top-level corpus from the folder name
    this.setCorpusGraph(corpusGraph);
    File corpusPath = new File(this.getCorpusDesc().getCorpusPath().toFileString());
    if (corpusPath.isDirectory()) {
      SCorpus rootCorpus = getCorpusGraph().createCorpus(null, corpusPath.getName());
      // Parse the file and get the linked JSON document files
      Gson gson = new Gson();
      try (FileReader reader =
          new FileReader(new File(corpusPath, "master.json"), StandardCharsets.UTF_8)) {

        MasterFile masterFile = gson.fromJson(reader, MasterFile.class);

        // Create a document for each linked file
        for (String linkedFileName : masterFile.getFiles()) {
          File linkedFile = new File(corpusPath, linkedFileName);
          if(!linkedFile.exists()) {
            throw new PepperModuleException(
                "Linked file " + linkedFile.getAbsolutePath() + " does not exist.");
          }

          String docName = Files.getNameWithoutExtension(linkedFile.getName());
          SDocument doc = getCorpusGraph().createDocument(rootCorpus, docName);
          this.getIdentifier2ResourceTable().put(doc.getIdentifier(),
              URI.createFileURI(linkedFile.getAbsolutePath()));
          
        }

      } catch (IOException ex) {
        throw new PepperModuleException("Could not read master.json file for corpus", ex);
      }
    }

  }
}
