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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graphAnnoModules;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.common.DOCUMENT_STATUS;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperExporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

@Component(name = "GraphAnnoExporterComponent", factory = "PepperExporterComponentFactory")
public class GraphAnnoExporter extends PepperExporterImpl {
	public GraphAnnoExporter() {
		super();
		this.setName("GraphAnnoExporter");
		// set list of formats supported by this module
		this.addSupportedFormat("Jason", "1.0", null);
		setSDocumentEnding("json");
		setExportMode(EXPORT_MODE.DOCUMENTS_IN_FILES);
	}

	@Override
	public PepperMapper createPepperMapper(SElementId sElementId) {
		PepperMapper mapper = new GraphAnnoMapper();
		mapper.setResourceURI(getSElementId2ResourceTable().get(sElementId));
		return (mapper);
	}

	public static class GraphAnnoMapper extends PepperMapperImpl {
		@Override
		public DOCUMENT_STATUS mapSDocument() {

			if ((getSDocument() != null) && (getSDocument().getSDocumentGraph() != null)) {
				StringBuilder json = new StringBuilder();
				json.append("{");
				json.append("\"nodes\":[");
				json.append("{");
				json.append("\"attr\": {" + "\"cat\": \"meta\"," + "\"sentence\": \"1\"" + "}," + "\"ID\": \"0\"");
				json.append("},");
				int sentenceNo = 1;
				StringBuilder edgeOut = new StringBuilder();
				SToken lastToken = null;
				boolean first = true;
				boolean firstEdge = true;
				for (SSpan span : getSDocument().getSDocumentGraph().getSSpans()) {
					if (span.hasLabel("sentence")) {
						lastToken = null;
						EList<STYPE_NAME> relTypes = new BasicEList<STYPE_NAME>();
						relTypes.add(STYPE_NAME.SSPANNING_RELATION);
						List<SToken> tokens = getSDocument().getSDocumentGraph().getSortedSTokenByText(getSDocument().getSDocumentGraph().getOverlappedSTokens(span, relTypes));
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
							json.append(getSDocument().getSDocumentGraph().getSText(token).replace("\"", "\\\""));
							json.append("\"");
							json.append(",\n");
							json.append("\"sentence\":");
							json.append("\"");
							json.append(sentenceNo);
							json.append("\"");
							json.append("},\n");
							json.append("\"ID\":");
							json.append("\"");
							json.append(token.getSElementPath().fragment());
							json.append("\"");
							json.append("\n");
							json.append("}");

							if (lastToken != null) {
								if (!firstEdge) {
									edgeOut.append(",\n");
								} else {
									firstEdge = false;
								}
								edgeOut.append("{\n");
								edgeOut.append("\"start\":\"");
								edgeOut.append(lastToken.getSElementPath().fragment());
								edgeOut.append("\",\n");
								edgeOut.append("\"end\":\"");
								edgeOut.append(token.getSElementPath().fragment());
								edgeOut.append("\",\n");
								edgeOut.append("\"attr\":{\n");
								edgeOut.append("\"sentence\":\"");
								edgeOut.append(sentenceNo);
								edgeOut.append("\"\n");
								edgeOut.append("},\n");
								edgeOut.append("\"ID\":");
								edgeOut.append("\"");
								edgeOut.append(span.getSElementPath().fragment());
								edgeOut.append("\",");
								edgeOut.append("\"type\":\"t\"");
								edgeOut.append("}\n");
							}
							lastToken = token;
						}
					}
					sentenceNo++;
				}
				json.append("],");
				json.append("\"edges\":[");
				json.append(edgeOut.toString());
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
