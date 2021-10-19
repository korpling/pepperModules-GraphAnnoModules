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
import org.corpus_tools.salt.common.SSpan;
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

    assertEquals(3, cg.getDocuments().size());
    SDocument doc1 = cg.getDocuments().get(0);
    assertEquals("0001", doc1.getName());
    SDocument doc2 = cg.getDocuments().get(1);
    assertEquals("0002", doc2.getName());
    SDocument doc3 = cg.getDocuments().get(2);
    assertEquals("0003", doc3.getName());
  }

  @Test
  public void testDocumentMapping() {
    start();

    SDocument doc = getFixture().getSaltProject().getCorpusGraphs().get(0).getDocuments().get(0);
    SDocumentGraph g = doc.getDocumentGraph();
    
    assertNotNull(g);;
    
    
    assertEquals(1, g.getTextualDSs().size());
    assertEquals("Is this example more complicated than it appears to be ? ",
        g.getTextualDSs().get(0).getText());

    // Test token and token annotations
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

    // Check that sentence spans have been created
    List<SSpan> spans = doc.getDocumentGraph().getSpans();
    assertNotNull(spans);
    List<SSpan> sentences =
        spans.stream().filter(s -> s.getAnnotation("salt::unit") != null)
            .collect(Collectors.toList());
    assertEquals(1, sentences.size());
  }

  @Test
  public void testTokenMappingByTime() {
    start();

    SDocument doc = getFixture().getSaltProject().getCorpusGraphs().get(0).getDocuments().get(1);
    SDocumentGraph g = doc.getDocumentGraph();

    assertNotNull(g);;


    assertEquals(1, g.getTextualDSs().size());
    assertEquals("Is this example more complicated than it appears to be ? ",
        g.getTextualDSs().get(0).getText());

    // Test token
    List<SToken> token = doc.getDocumentGraph().getSortedTokenByText();
    assertEquals(11, token.size());
    List<String> tokenTexts = token.stream().map(t -> g.getText(t)).collect(Collectors.toList());
    assertArrayEquals(new String[] {"Is", "this", "example", "more", "complicated", "than", "it",
        "appears", "to", "be", "?"}, tokenTexts.toArray());

    // Check that sentence spans have been created
    List<SSpan> spans = doc.getDocumentGraph().getSpans();
    assertNotNull(spans);
    List<SSpan> sentences = spans.stream().filter(s -> s.getAnnotation("salt::unit") != null)
        .collect(Collectors.toList());
    assertEquals(1, sentences.size());
  }

  @Test
  public void testSentenceOrder() {
    start();

    SDocument doc = getFixture().getSaltProject().getCorpusGraphs().get(0).getDocuments().get(2);
    SDocumentGraph g = doc.getDocumentGraph();

    assertNotNull(g);;


    assertEquals(1, g.getTextualDSs().size());
    assertEquals("A1 A2 B1 B2 ",
        g.getTextualDSs().get(0).getText());

    // Test token
    List<SToken> token = doc.getDocumentGraph().getSortedTokenByText();
    assertEquals(4, token.size());
    List<String> tokenTexts = token.stream().map(t -> g.getText(t)).collect(Collectors.toList());
    assertArrayEquals(new String[] {"A1", "A2", "B1", "B2"}, tokenTexts.toArray());

    // Check that sentence spans have been created
    List<SSpan> spans = doc.getDocumentGraph().getSpans();
    assertNotNull(spans);
    List<SSpan> sentences = spans.stream().filter(s -> s.getAnnotation("salt::unit") != null)
        .collect(Collectors.toList());
    assertEquals(2, sentences.size());
  }

}
