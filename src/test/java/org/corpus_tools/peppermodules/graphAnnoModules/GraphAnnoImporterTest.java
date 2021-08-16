package org.corpus_tools.peppermodules.graphAnnoModules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;
import org.corpus_tools.pepper.common.CorpusDesc;
import org.corpus_tools.pepper.testFramework.PepperImporterTest;
import org.corpus_tools.pepper.testFramework.PepperTestUtil;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SToken;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

public class GraphAnnoImporterTest extends PepperImporterTest {

  @Before
  public void setUp() throws Exception {
    setFixture(new GraphAnnoImporter());
    File testFolder = new File(PepperTestUtil.getTestResources() + "sampleCorpus/");
    getFixture().setCorpusDesc(
        new CorpusDesc().setCorpusPath(URI.createFileURI(testFolder.getAbsolutePath())));
  }

  @Test
  public void testImportCorpusStructureSCorpusGraph() {

    start();

    // This should have created a root corpus and two documents
    assertEquals(1, getFixture().getSaltProject().getCorpusGraphs().size());
    SCorpusGraph cg = getFixture().getSaltProject().getCorpusGraphs().get(0);
    assertEquals(1, cg.getRoots().size());
    assertEquals(1, cg.getCorpora().size());
    SCorpus rootCorpus = (SCorpus) cg.getRoots().get(0);
    assertEquals("sampleCorpus", rootCorpus.getName());

    assertEquals(2, cg.getDocuments().size());
    SDocument doc1 = cg.getDocuments().get(0);
    assertEquals("0001", doc1.getName());
    
    assertEquals(2, cg.getDocuments().size());
    SDocument doc2 = cg.getDocuments().get(1);
    assertEquals("0002", doc2.getName());
  }

  @Test
  public void testTokenization() {
    start();

    assertEquals(2, getFixture().getSaltProject().getCorpusGraphs().get(0).getDocuments().size());
    SDocument doc = getFixture().getSaltProject().getCorpusGraphs().get(0).getDocuments().get(0);
    SDocumentGraph g = doc.getDocumentGraph();
    
    assertNotNull(g);;
    
    
    assertEquals(1, g.getTextualDSs().size());
    assertEquals("Is this example more complicated than it appears to be ? ",
        g.getTextualDSs().get(0).getText());

    List<SToken> token = doc.getDocumentGraph().getSortedTokenByText();
    assertEquals(11, token.size());
    assertEquals("Is", g.getText(token.get(0)));
    assertEquals("this", g.getText(token.get(1)));
    assertEquals("example", g.getText(token.get(2)));
    assertEquals("more", g.getText(token.get(3)));
    assertEquals("complicated", g.getText(token.get(4)));
    assertEquals("than", g.getText(token.get(5)));
    assertEquals("it", g.getText(token.get(6)));
    assertEquals("appears", g.getText(token.get(7)));
    assertEquals("to", g.getText(token.get(8)));
    assertEquals("be", g.getText(token.get(9)));
    assertEquals("?", g.getText(token.get(10)));
  }

}
