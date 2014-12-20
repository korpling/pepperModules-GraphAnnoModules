/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graphAnnoModules.tests;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.german.korpling.saltnpepper.pepper.common.CorpusDesc;
import de.hu_berlin.german.korpling.saltnpepper.pepper.common.FormatDesc;
import de.hu_berlin.german.korpling.saltnpepper.pepper.testFramework.PepperExporterTest;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.graphAnnoModules.GraphAnnoExporter;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;

public class GraphAnnoExporterTest extends PepperExporterTest {
	@Before
	public void setUp(){		
		setFixture(new GraphAnnoExporter());
		getFixture().setSaltProject(SaltFactory.eINSTANCE.createSaltProject());
		//set formats to support
		FormatDesc formatDef= new FormatDesc();
		formatDef.setFormatName("Jason");
		formatDef.setFormatVersion("1.0");
		this.supportedFormatsCheck.add(formatDef);
	}
	
	@Test
	public void testExportJSON() {
		CorpusDesc corpDef= new CorpusDesc();
		FormatDesc formatDef= new FormatDesc();
		formatDef.setFormatName("Jason");
		formatDef.setFormatVersion("1.0");
		corpDef.setFormatDesc(formatDef);
		
		corpDef.setCorpusPath(URI.createFileURI(getTempPath("graphAnnoExporter").getAbsolutePath()));
		getFixture().setCorpusDesc(corpDef);
		
		SCorpusGraph corpusGraph= SaltFactory.eINSTANCE.createSCorpusGraph();
		this.getFixture().getSaltProject().getSCorpusGraphs().add(corpusGraph);
		SDocument sDoc1= corpusGraph.createSDocument(URI.createURI("/c1/d1"));
		sDoc1.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		String text= "Sentence number one. Sentence number two! Sentence etc. number three?";
		STextualDS textualDS= sDoc1.getSDocumentGraph().createSTextualDS(text);
		SToken t1= sDoc1.getSDocumentGraph().createSToken(textualDS, 0, 8);
		SToken t2= sDoc1.getSDocumentGraph().createSToken(textualDS, 9, 15);
		SToken t3= sDoc1.getSDocumentGraph().createSToken(textualDS, 16, 19);
		SToken t4= sDoc1.getSDocumentGraph().createSToken(textualDS, 19, 20);
		SToken t5= sDoc1.getSDocumentGraph().createSToken(textualDS, 21, 29);
		SToken t6= sDoc1.getSDocumentGraph().createSToken(textualDS, 30, 36);
		SToken t7= sDoc1.getSDocumentGraph().createSToken(textualDS, 37, 40);
		SToken t8= sDoc1.getSDocumentGraph().createSToken(textualDS, 40, 41);
		SToken t9= sDoc1.getSDocumentGraph().createSToken(textualDS, 42, 50);
		SToken t10= sDoc1.getSDocumentGraph().createSToken(textualDS, 51, 55);
		SToken t11= sDoc1.getSDocumentGraph().createSToken(textualDS, 56, 62);
		SToken t12= sDoc1.getSDocumentGraph().createSToken(textualDS, 63, 68);
		SToken t13= sDoc1.getSDocumentGraph().createSToken(textualDS, 68, 69);
		EList<SToken> tokens= new BasicEList<SToken>();
		tokens.add(t1);
		tokens.add(t2);
		tokens.add(t3);
		tokens.add(t4);
		SSpan sentence1= sDoc1.getSDocumentGraph().createSSpan(tokens);
		sentence1.createSAnnotation(null, "sentence", "sentence");
		
		tokens= new BasicEList<SToken>();
		tokens.add(t5);
		tokens.add(t6);
		tokens.add(t7);
		tokens.add(t8);
		SSpan sentence2= sDoc1.getSDocumentGraph().createSSpan(tokens);
		sentence2.createSAnnotation(null, "sentence", "sentence");
		
		tokens= new BasicEList<SToken>();
		tokens.add(t9);
		tokens.add(t10);
		tokens.add(t11);
		tokens.add(t12);
		tokens.add(t13);
		SSpan sentence3= sDoc1.getSDocumentGraph().createSSpan(tokens);
		sentence3.createSAnnotation(null, "sentence", "sentence");
		
		this.start();
	}
}
