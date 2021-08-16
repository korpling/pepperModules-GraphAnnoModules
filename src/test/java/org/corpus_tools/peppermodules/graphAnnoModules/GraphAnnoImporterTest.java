package org.corpus_tools.peppermodules.graphAnnoModules;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.corpus_tools.pepper.common.CorpusDesc;
import org.corpus_tools.pepper.testFramework.PepperImporterTest;
import org.corpus_tools.pepper.testFramework.PepperTestUtil;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

public class GraphAnnoImporterTest extends PepperImporterTest {

  @Before
  public void setUp() throws Exception {
    setFixture(new GraphAnnoImporter());
  }

  @Test
  public void testImportCorpusStructureSCorpusGraph() {
    File testFolder = new File(PepperTestUtil.getTestResources() + "sampleCorpus/");
    getFixture().setCorpusDesc(
        new CorpusDesc().setCorpusPath(URI.createFileURI(testFolder.getAbsolutePath())));
    start();


    // This should have created a root corpus and a single document
    assertEquals(1, getFixture().getSaltProject().getCorpusGraphs().size());
    SCorpusGraph cg = getFixture().getSaltProject().getCorpusGraphs().get(0);
    assertEquals(1, cg.getRoots().size());
    assertEquals(1, cg.getCorpora().size());
    SCorpus rootCorpus = (SCorpus) cg.getRoots().get(0);
    assertEquals("sampleCorpus", rootCorpus.getName());

    assertEquals(1, cg.getDocuments().size());
    SDocument doc0001 = cg.getDocuments().get(0);
    assertEquals("0001", doc0001.getName());
    
  }

}
