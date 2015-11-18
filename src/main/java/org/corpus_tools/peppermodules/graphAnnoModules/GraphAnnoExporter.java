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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperExporterImpl;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperExporter.EXPORT_MODE;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

@Component(name = "GraphAnnoExporterComponent", factory = "PepperExporterComponentFactory")
public class GraphAnnoExporter extends PepperExporterImpl {
	public GraphAnnoExporter() {
		super();
		setName("GraphAnnoExporter");
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-GraphAnnoModules"));
		setDesc("This exporter transforms a Salt model into a format for the GraphAnno tool (https://github.com/LBierkandt/graph-anno). ");
		// set list of formats supported by this module
		addSupportedFormat("Jason", "1.0", null);
		setDocumentEnding("json");
		setExportMode(EXPORT_MODE.DOCUMENTS_IN_FILES);
	}

	@Override
	public PepperMapper createPepperMapper(Identifier sElementId) {
		PepperMapper mapper = new GraphAnnoMapper();
		mapper.setResourceURI(getIdentifier2ResourceTable().get(sElementId));
		return (mapper);
	}

	public static class GraphAnnoMapper extends PepperMapperImpl {
		@Override
		public DOCUMENT_STATUS mapSDocument() {

			if ((getDocument() != null) && (getDocument().getDocumentGraph() != null)) {
				StringBuilder json = new StringBuilder();
				json.append("{");
				json.append("\"nodes\":[");
				json.append("{");
				json.append("\"attr\": {" + "\"cat\": \"meta\"," + "\"sentence\": \"1\"" + "}," + "\"ID\": \"0\"");
				json.append("},");
				int sentenceNo = 1;
				StringBuilder relationOut = new StringBuilder();
				SToken lastToken = null;
				boolean first = true;
				boolean firstRelation = true;
				for (SSpan span : getDocument().getDocumentGraph().getSpans()) {
					if (span.containsLabel("sentence")) {
						lastToken = null;
						List<SToken> tokens = getDocument().getDocumentGraph().getSortedTokenByText(getDocument().getDocumentGraph().getOverlappedTokens(span, SALT_TYPE.SSPANNING_RELATION));
						for (SToken token : tokens) {
							if (!first) {
								json.append(",\n");
							} else {
								first = false;
							}
							json.append("{\n");
							json.append("\"attr\":{\n");
							json.append("\"token\":");
							json.append("\"");
							json.append(getDocument().getDocumentGraph().getText(token).replace("\"", "\\\""));
							json.append("\"");
							json.append(",\n");
							json.append("\"sentence\":");
							json.append("\"");
							json.append(sentenceNo);
							json.append("\"");
							json.append("},\n");
							json.append("\"ID\":");
							json.append("\"");
							json.append(token.getPath().fragment());
							json.append("\"");
							json.append("\n");
							json.append("}");

							if (lastToken != null) {
								if (!firstRelation) {
									relationOut.append(",\n");
								} else {
									firstRelation = false;
								}
								relationOut.append("{\n");
								relationOut.append("\"start\":\"");
								relationOut.append(lastToken.getPath().fragment());
								relationOut.append("\",\n");
								relationOut.append("\"end\":\"");
								relationOut.append(token.getPath().fragment());
								relationOut.append("\",\n");
								relationOut.append("\"attr\":{\n");
								relationOut.append("\"sentence\":\"");
								relationOut.append(sentenceNo);
								relationOut.append("\"\n");
								relationOut.append("},\n");
								relationOut.append("\"ID\":");
								relationOut.append("\"");
								relationOut.append(span.getPath().fragment());
								relationOut.append("\",");
								relationOut.append("\"type\":\"t\"");
								relationOut.append("}\n");
							}
							lastToken = token;
						}
					}
					sentenceNo++;
				}
				json.append("],");
				json.append("\"relations\":[");
				json.append(relationOut.toString());
				json.append("],");
				json.append("\"version\": \"5\"");
				json.append("}");

				File outputFile = new File(getResourceURI().toFileString());
				if ((!outputFile.isDirectory()) && (!outputFile.getParentFile().exists())) {
					outputFile.getParentFile().mkdirs();
				}
				FileWriter flwTemp = null;
				try {
					flwTemp = new FileWriter(outputFile);
					flwTemp.write(json.toString());
					flwTemp.flush();
				} catch (IOException e) {
					throw new PepperModuleException(this, "Unable to write output file for PTB export '" + getResourceURI() + "'.", e);
				} finally {
					try {
						if (flwTemp != null) {
							flwTemp.close();
						}
					} catch (IOException e) {
						throw new PepperModuleException(this, "Unable to close output file writer for PTB export '" + getResourceURI() + "'.", e);
					}
				}
			}
			return (DOCUMENT_STATUS.COMPLETED);
		}
	}
}
