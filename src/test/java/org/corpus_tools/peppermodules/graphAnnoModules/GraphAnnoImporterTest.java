package org.corpus_tools.peppermodules.graphAnnoModules;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
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
    List<String> tokenTexts = token.stream().map(t -> g.getText(t)).collect(Collectors.toList());
    assertArrayEquals(new String[] {"Is", "this", "example", "more", "complicated", "than", "it",
        "appears", "to", "be", "?"}, tokenTexts.toArray());

    // check lemma and pos tags
    List<String> pos = token.stream().map(t -> t.getAnnotation("pos").getValue_STEXT())
        .collect(Collectors.toList());
    List<String> lemma =
        token.stream().map(t -> t.getAnnotation("lemma").getValue_STEXT())
            .collect(Collectors.toList());

    assertArrayEquals(
        new String[] {"VBZ", "DT", "NN", "RBR", "NN", "IN", "PRP", "VBZ", "TO", "VB", "."},
        pos.toArray());
    assertArrayEquals(new String[] {"be", "this", "example", "more", "complication", "than", "it",
        "appear", "to", "be", "?"}, lemma.toArray());
  }

}
