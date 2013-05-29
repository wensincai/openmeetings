/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openmeetings.servlet.outputhandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.openmeetings.OpenmeetingsVariables;
import org.apache.openmeetings.data.basic.FieldLanguageDao;
import org.apache.openmeetings.data.basic.FieldManager;
import org.apache.openmeetings.data.basic.SessiondataDao;
import org.apache.openmeetings.data.user.UserManager;
import org.apache.openmeetings.persistence.beans.lang.FieldLanguage;
import org.apache.openmeetings.persistence.beans.lang.Fieldlanguagesvalues;
import org.apache.openmeetings.servlet.BaseHttpServlet;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

/**
 * 
 * @author sebastianwagner
 * 
 */
public class LangExport extends BaseHttpServlet {
	
	private static final long serialVersionUID = 243294279856160463L;
	
	private static final Logger log = Red5LoggerFactory.getLogger(
			LangExport.class, OpenmeetingsVariables.webAppRootKey);
	
	public static final String FILE_COMMENT = ""
			+ "\n"
			+ "  Licensed to the Apache Software Foundation (ASF) under one\n"
			+ "  or more contributor license agreements.  See the NOTICE file\n"
			+ "  distributed with this work for additional information\n"
			+ "  regarding copyright ownership.  The ASF licenses this file\n"
			+ "  to you under the Apache License, Version 2.0 (the\n"
			+ "  \"License\"); you may not use this file except in compliance\n"
			+ "  with the License.  You may obtain a copy of the License at\n"
			+ "  \n"
			+ "      http://www.apache.org/licenses/LICENSE-2.0\n"
			+ "    	  \n"
			+ "  Unless required by applicable law or agreed to in writing,\n"
			+ "  software distributed under the License is distributed on an\n"
			+ "  \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n"
			+ "  KIND, either express or implied.  See the License for the\n"
			+ "  specific language governing permissions and limitations\n"
			+ "  under the License.\n"
			+ "  \n"
			+ "\n"
			+ "\n"
			+ "###############################################\n"
			+ "This File is auto-generated by the LanguageEditor \n"
			+ "to add new Languages or modify/customize it use the LanguageEditor \n"
			+ "see http://openmeetings.apache.org/LanguageEditor.html for Details \n"
			+ "###############################################";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void service(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {

		try {

			if (getBean(UserManager.class) == null
					|| getBean(FieldLanguageDao.class) == null
					|| getBean(FieldManager.class) == null
					|| getBean(SessiondataDao.class) == null) {
				return;
			}

			String sid = httpServletRequest.getParameter("sid");
			if (sid == null) {
				sid = "default";
			}
			log.debug("sid: " + sid);

			String language = httpServletRequest.getParameter("language");
			if (language == null) {
				language = "0";
			}
			Long language_id = Long.valueOf(language).longValue();
			log.debug("language_id: " + language_id);

			Long users_id = getBean(SessiondataDao.class).checkSession(sid);
			Long user_level = getBean(UserManager.class).getUserLevelByID(users_id);

			log.debug("users_id: " + users_id);
			log.debug("user_level: " + user_level);

			if (user_level != null && user_level > 0) {
				FieldLanguage fl = getBean(FieldLanguageDao.class)
						.getFieldLanguageById(language_id);

				List<Fieldlanguagesvalues> flvList = getBean(FieldManager.class).getMixedFieldValuesList(language_id);

				if (fl != null && flvList != null) {
					Document doc = createDocument(flvList, getBean(FieldManager.class).getUntranslatedFieldValuesList(language_id));

					String requestedFile = fl.getName() + ".xml";

					httpServletResponse.reset();
					httpServletResponse.resetBuffer();
					OutputStream out = httpServletResponse.getOutputStream();
					httpServletResponse
							.setContentType("APPLICATION/OCTET-STREAM");
					httpServletResponse.setHeader("Content-Disposition",
							"attachment; filename=\"" + requestedFile + "\"");
					// httpServletResponse.setHeader("Content-Length", ""+
					// rf.length());

					serializetoXML(out, "UTF-8", doc);

					out.flush();
					out.close();
				}
			} else {
				log.debug("ERROR LangExport: not authorized FileDownload "
						+ (new Date()));
			}

		} catch (Exception er) {
			log.error("ERROR ", er);
			System.out.println("Error exporting: " + er);
			er.printStackTrace();
		}
	}

	public static Document createDocument() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding("UTF-8");
		document.addComment(LangExport.FILE_COMMENT);
		return document;
	}
	
	public static Element createRoot(Document document) {
		Element root = document.addElement("language");
		root.add(new Namespace("xsi",
				"http://www.w3.org/2001/XMLSchema-instance"));
		root.add(new Namespace("noNamespaceSchemaLocation", "language.xsd"));
		return root;
	}
	
	public static Document createDocument(List<Fieldlanguagesvalues> flvList, List<Fieldlanguagesvalues> untranslatedList) throws Exception {
		Document document = createDocument();
		Element root = createRoot(document);

		for (Fieldlanguagesvalues flv : flvList) {
			Element eTemp = root.addElement("string")
					.addAttribute("id", flv.getFieldvalues().getFieldvalues_id().toString())
					.addAttribute("name", flv.getFieldvalues().getName());
			Element value = eTemp.addElement("value");
			value.addText(flv.getValue());
		}

		//untranslated
		if (untranslatedList.size() > 0) {
			root.addComment("Untranslated strings");
			for (Fieldlanguagesvalues flv : untranslatedList) {
				Element eTemp = root.addElement("string")
						.addAttribute("id", flv.getFieldvalues().getFieldvalues_id().toString())
						.addAttribute("name", flv.getFieldvalues().getName());
				Element value = eTemp.addElement("value");
				value.addText(flv.getValue());
			}
		}

		return document;
	}

	public static void serializetoXML(OutputStream out, String aEncodingScheme,
			Document doc) throws Exception {
		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setEncoding(aEncodingScheme);
		XMLWriter writer = new XMLWriter(out, outformat);
		writer.write(doc);
		writer.flush();
	}

}
