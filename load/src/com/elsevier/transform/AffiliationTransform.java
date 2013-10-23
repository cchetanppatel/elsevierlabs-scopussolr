package com.elsevier.transform;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



/**
 * Transform a Scopus Affiliation record into a HashMap containing a field name and the associated
 * value.  Xpath expressions will be used to map the associated elements (and the descendant text)
 * to a specified field name.  Currently, the code assumes a field will only have one value.
 * 
 * @author Curt Kohler
 *
 */
public class AffiliationTransform {

	Document doc = null;
	XPath xpath = null;
	HashMap<String,Object> fieldValues = new HashMap<String,Object>();
	HashMap<String, String> cachedFieldValues = new HashMap<String, String>();
	
	private static int DEFAULT_JSON_FIELD_STRINGBUILDER_SIZE = 1024;
	
	// Basic field mappings	
	private static String[] affilArrayMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile[not(@parent)]"
	};
	
	private static String[] affilcityArrayMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile[not(@parent)]/address",
	};
		
	private static String[] affilctrytArrayMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile[not(@parent)]/address/country"
	};
	
	private static String[] affilnameArrayMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile[not(@parent)]/preferred-name",
		"/xocs:doc/xocs:institution-profile/institution-profile[not(@parent)]/name-variant"
	};
	
	private static String[] afidMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile[not(@parent)]/@affiliation-id"
	};
	
	private static String[] certscoreArrayMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile[not(@parent)]/certainty-scores/certainty-score/orig-id",
		"/xocs:doc/xocs:institution-profile/institution-profile[not(@parent)]/certainty-scores/certainty-score/score"
	};
	
	private static String[] datecompletedtxtArrayMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile/date-revised/@timestamp"
	};

	private static String[] eidMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:eid//text()"
	};
	
	private static String[] loaddateMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:timestamp//text()"
	};
	
	private static String[] namevarArrayMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile[not(@parent)]/name-variant"
	};
	
	private static String[] parafidMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile/@parent"
	};
	
	private static String[] prefnameMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile[not(@parent)]/preferred-name//text()"
	};
	
	private static String[] prefparnameMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile/parent-preferred-name//text()"
	};
	
	private static String[] qualityMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile/quality//text()"
	};
	
	private static String[] sortnameMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile[not(@parent)]/sort-name//text()"
	};
	
	private static String[] statusMappings = new String[] {
		"/xocs:doc/xocs:institution-profile/institution-profile/status//text()"
	};

	

	
	/**
	 * Transform the record.  The inputstream contains the ScienceDirect record that will be transformed.
	 * The returned HashMap object will contain fields (and their values) that will be used to
	 * create a JSON document that will be inserted into ElasticSearch.
	 * 
	 * @param is
	 * @return fieldValues;
	 */
	
	public HashMap<String,Object> transform(InputStream is) {
		
		//fieldValues = new HashMap();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder;
		
		try {
			builder = dbFactory.newDocumentBuilder();
			doc = builder.parse(is);
			
			XPathFactory xpFactory = XPathFactory.newInstance();
			xpath = xpFactory.newXPath();
			xpath.setNamespaceContext(new ScopusNamespaceContext());
			
			// Begin applying rules ...

			//
			// First generate the atomic fields
			//
			
			createArray("affil", affilArrayMappings, "(./preferred-name//text() | ./name-variant//text() | ./address/city-group//text() | ./address/city//text() | ./address/country//text())");
			
			createArray("affilcity", affilcityArrayMappings, "(./city-group//text() | ./city//text())");
			
			createArray("affilctry", affilctrytArrayMappings, "(.//text())");
			
			createArray("affilname", affilnameArrayMappings, "(.//text())");
			
			createSingleField("afid", afidMappings);
			
			createArray("certScore", certscoreArrayMappings, "(.//text())");
		
			createArray("datecompleted", datecompletedtxtArrayMappings, "(.//text())");
	
			createSingleField("eid", eidMappings);
			
			createSingleField("loaddate", loaddateMappings);
			
			createArray("namevar", namevarArrayMappings, "(.//text())");
			
			createSingleField("parafid", parafidMappings);

			createSingleField("prefname", prefnameMappings);
			
			createSingleField("prefparname", prefparnameMappings);

			createSingleField("quality", qualityMappings);

			createSingleField("sortname", sortnameMappings);
			
			createSingleField("status", statusMappings);
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} finally {
		}
		return fieldValues;
	}
	
	
	/**
	 * Create a single field.  In other words, there will be only one field with this name in the docuemnt.
	 * It's possible for duplicate values (nodes) to occur in a single field.
     * 
	 * @param fieldName  field name
	 * @param xpathExpression XPath to apply
	 * @throws XPathExpressionException
	 */
	public void createSingleField(String fieldName, String xpathExpression) throws XPathExpressionException {
		
		// If we have already resolved this XPath expression, use the cached value to create the field and return
		if (cachedFieldValues.containsKey(xpathExpression)) {
			fieldValues.put(fieldName, cachedFieldValues.get(xpathExpression));
			return;
		}
		
		NodeList nodes = (NodeList)xpath.evaluate(xpathExpression, doc, XPathConstants.NODESET);
		
		// If it doesn't exist, cache the results and return
		if (nodes.getLength() == 0) {
			cachedFieldValues.put(xpathExpression, "");
			return;
		}
		
		StringBuilder sb = new StringBuilder(DEFAULT_JSON_FIELD_STRINGBUILDER_SIZE);
		for (int i = 0; i < nodes.getLength(); i++) {
		    sb.append(nodes.item(i).getNodeValue().trim());
		    if (i < nodes.getLength() - 1) sb.append(" ");
		}
		
		String wrkStr = sb.toString();
		
		// Create the field and cache the results
		if (wrkStr.length()  > 0) {
			fieldValues.put(fieldName, wrkStr);
			cachedFieldValues.put(xpathExpression, wrkStr);
		} else {
			cachedFieldValues.put(xpathExpression, "");
		}

	}

	
	/**
	 * Create a single field.  In other words, there will be only one field with this name in the document.
	 * It's possible for duplicate values (nodes) to occur in a single field.
     * 
	 * @param fieldName  field name
	 * @param xpathExpression Array of XPath expressions to apply
	 * @throws XPathExpressionException
	 */
	public void createSingleField(String fieldName, String[] xpathExpressions) throws XPathExpressionException {
		
		StringBuilder partSb = new StringBuilder(DEFAULT_JSON_FIELD_STRINGBUILDER_SIZE);
		StringBuilder sb = new StringBuilder(DEFAULT_JSON_FIELD_STRINGBUILDER_SIZE);
		
		for (int i=0; i < xpathExpressions.length; i++) {
			
			// If we have already resolved this XPath expression, use the cached value to build up the field and continue
			if (cachedFieldValues.containsKey(xpathExpressions[i])) {
				sb.append(cachedFieldValues.get(xpathExpressions[i]));
				if (sb.length() > 0) sb.append(" "); // new
				continue;
			}
			
			NodeList nodes = (NodeList)xpath.evaluate(xpathExpressions[i], doc, XPathConstants.NODESET);

			// If the XPath expression doesn't match anything, cache the result and continue to next expression
			if (nodes.getLength() == 0) {
				cachedFieldValues.put(xpathExpressions[i], "");
				continue;
			}
			
			partSb.setLength(0);
			
			for (int j = 0; j < nodes.getLength(); j++) {
				partSb.append(nodes.item(j).getNodeValue().trim());
				if (j < nodes.getLength() - 1) partSb.append(" ");
			}
			
			String wrkStr = partSb.toString();
			
			if (wrkStr.length()  > 0) {
				cachedFieldValues.put(xpathExpressions[i], wrkStr);
				sb.append(wrkStr);
				if (sb.length() > 0) sb.append(" "); // new
			} else {
				cachedFieldValues.put(xpathExpressions[i], "");
			}
			
		}
		if (sb.length()  > 0) {
			fieldValues.put(fieldName, sb.toString().trim());
		}
	}
		
	/**
	 * Create a single composite field from previously found fields.  In other words, there will be only one field with this name in the document.
	 * It's possible for duplicate values (nodes) to occur in a single field.
     * 
	 * @param fieldName  field name
	 * @param xpathExpression Array of XPath expressions to apply
	 * @throws XPathExpressionException
	 */
	public void createCompositeSingleField(String fieldName, String[] xpathExpressions) throws XPathExpressionException {
		
		StringBuilder sb = new StringBuilder(DEFAULT_JSON_FIELD_STRINGBUILDER_SIZE);
		StringBuilder partSb = new StringBuilder(DEFAULT_JSON_FIELD_STRINGBUILDER_SIZE);
		
		for (int i=0; i < xpathExpressions.length; i++) {
			if (cachedFieldValues.containsKey(xpathExpressions[i])) {
				String cachedValue = cachedFieldValues.get(xpathExpressions[i]);
				sb.append(cachedFieldValues.get(xpathExpressions[i]));
				if (sb.length() > 0) {
					sb.append(" "); 
				}
			} else {
				// If we haven't looked this expression up yet, look it up, process it, and cache the results 
				NodeList nodes = (NodeList)xpath.evaluate(xpathExpressions[i], doc, XPathConstants.NODESET);

				if (nodes.getLength() == 0) {
					cachedFieldValues.put(xpathExpressions[i], "");
					continue;
				}
				
				partSb.setLength(0);
				for (int j = 0; j < nodes.getLength(); j++) {
					partSb.append(nodes.item(j).getNodeValue().trim());
					if (j < nodes.getLength() - 1) partSb.append(" ");
				}
				
				String wrkStr = partSb.toString();
				
				if (wrkStr.length()  > 0) {
					cachedFieldValues.put(xpathExpressions[i], wrkStr);
					sb.append(wrkStr);
					if (sb.length() > 0) sb.append(" "); // new
				} else {
					cachedFieldValues.put(xpathExpressions[i], "");
				}
			}	
		}
		
		if (sb.length()  > 0) {
			fieldValues.put(fieldName, sb.toString().trim());
		}
	}
	
	/**
	 * Create an array of values for the specified field.  If there is only one value, an array will not be constructed.  Instead
	 * a simple field/value will be constructed.  The xathExpresions[] should be considered the grouping.  Each matching node in 
	 * the xml tree for the particular xpath expression will become a value in the array.  The second xpathExpression identifies
	 * the children (of this match in the tree) that should be used for the values.
	 * 
	 * @param fieldName field name
	 * @param xpathExpressions Array of XPath expressions to apply
	 * @param xpathExpression Secondary XPath expression 
	 * @throws XPathExpressionException
	 */
	public void createArray(String fieldName, String[] xpathExpressions, String xpathExpression) throws XPathExpressionException {
		
		// Array to hold all the possible values
		ArrayList<String> values = new ArrayList();
		
		// Loop through the outer xpath expressions and evaluate them separately 
		for (int i=0; i < xpathExpressions.length; i++) {
			
			// Evaluate the xpath expression
			NodeList nodes = (NodeList)xpath.evaluate(xpathExpressions[i], doc, XPathConstants.NODESET);
			
			// If no results for this xpath, continue with the next one
			if (nodes.getLength() == 0) continue;
			
			// Create a buffer to hold the values collected for this xpath expression
			StringBuilder sb = new StringBuilder(DEFAULT_JSON_FIELD_STRINGBUILDER_SIZE);
			
			for (int j = 0; j < nodes.getLength(); j++) {
				
				// Evaluate the secondary xpath expression
				Object result = xpath.evaluate(xpathExpression, nodes.item(j), XPathConstants.NODESET);
				NodeList nodes2 = (NodeList) result;
				
				// Collect the values for the children 
				for (int k = 0; k < nodes2.getLength(); k++) {
					sb.append(nodes2.item(k).getNodeValue().trim());
					if (k < nodes2.getLength() - 1) sb.append(" ");
				}
			    
				// Add the value to the array
				if (sb.length() > 0) {
					values.add(sb.toString());
					sb.setLength(0);
				}			
				
			}			

		}
		
		if (values.size() == 0) {
			return;
		} else if (values.size() == 1) {
			fieldValues.put(fieldName, values.get(0));
		} else {
			fieldValues.put(fieldName, values);
		}
		
	}
	
	
	public void createNestedField(String fieldName, String[] xpathExpressions, Map<String, String[]> subFieldExpressions) throws XPathExpressionException {
		
		// Array to hold all the possible values
		NestedObjectHelper values = new NestedObjectHelper();
		
		int objCnt = 0;
		
		// Loop through the outer xpath expressions and evaluate them separately 
		for (int i=0; i < xpathExpressions.length; i++) {
					
			// Evaluate the xpath expression to the enclosing element
			NodeList nodes = (NodeList)xpath.evaluate(xpathExpressions[i], doc, XPathConstants.NODESET);
					
			// If no results for this xpath, continue with the next one
			if (nodes.getLength() == 0) continue;
			
			// Create a buffer to hold the values collected for the various fields xpath expression
			StringBuilder sb = new StringBuilder(DEFAULT_JSON_FIELD_STRINGBUILDER_SIZE);
			
			String subFieldName = null;
			Iterator<String> subFieldIter = null;
			HashMap<String, String> subFields = null;
			for (int j = 0; j < nodes.getLength(); j++) {
				
				subFields = new HashMap<String, String>();
				// Evaluate the secondary subfield xpath expression(s)
				subFieldIter = subFieldExpressions.keySet().iterator();
				
				while (subFieldIter.hasNext()) {
					sb.setLength(0);
					subFieldName = subFieldIter.next();
					String[] subFieldExpr = subFieldExpressions.get(subFieldName);
					
					for (int k = 0; k < subFieldExpr.length; k++) {
						Object result = xpath.evaluate(subFieldExpr[k], nodes.item(j), XPathConstants.NODESET);
						NodeList nodes2 = (NodeList) result;
						
						for (int l = 0; l < nodes2.getLength(); l++) {
							sb.append(nodes2.item(l).getNodeValue().trim());
							if (l < nodes2.getLength() - 1) sb.append(" ");
						}
						
						if (sb.length() > 0) {
							subFields.put(subFieldName, sb.toString());
							sb.setLength(0);
						}
					}
				}
				values.addNestedObject(new Integer(objCnt += 1), subFields);
						
			}			

		}
				
		if (values.isEmpty()) {
			return;
		} else {
			fieldValues.put(fieldName, values);
		}
	}

}
