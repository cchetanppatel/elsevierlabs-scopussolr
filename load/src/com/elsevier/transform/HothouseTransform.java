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

import org.apache.commons.codec.digest.DigestUtils;
//import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.elsevier.es.NestedObjectHelper;


/**
 * Transform a ScienceDirect record into a HashMap containing a field name and the associated
 * value.  Xpath expressions will be used to map the associated elements (and the descendant text)
 * to a specified field name.  Currently, the code assumes a field will only have one value.
 * 
 * @author Curt Kohler
 *
 */
public class HothouseTransform {

	Document doc = null;
	XPath xpath = null;
	HashMap<String,Object> fieldValues = new HashMap<String,Object>();
	HashMap<String, String> cachedFieldValues = new HashMap<String, String>();
	
	private static int DEFAULT_JSON_FIELD_STRINGBUILDER_SIZE = 1024;
	
	// Basic field mappings
	private static String[] absattachmentMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:attachment-metadata-doc/xocs:attachments/xocs:attachment[xocs:abstract-attachment=\"true\"]//text()"
	};
	
	private static String[] acknowledgeMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:body/ce:acknowledgment//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:body/ce:acknowledgment//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:body/ce:acknowledgment//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:body/ce:acknowledgment//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:acknowledgment//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:acknowledgment//text()"
	};
	
	private static String[] affilMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:affiliation/ce:textfn//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:affiliation/ce:textfn//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:affiliation/ce:textfn//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:affiliation/ce:textfn//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:affiliation/ce:textfn//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:affiliation//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:affiliation//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:affiliation//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:affiliation//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:affiliation//text()"
	};
		
	private static String[] aiptxtMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:aip-text//text()"
	};
	
	private static String[] altitemtitleMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:alt-title//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:alt-subtitle//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:alt-title//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:alt-subtitle//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:alt-title//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:alt-subtitle//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:alt-title//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:alt-title//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:alt-subtitle//text()"
	};
	
	private static String[] appendicesMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:body/ce:appendices//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:body/ce:appendices//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:body/ce:appendices//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:body/ce:appendices//text()"
	};
	
	private static String[] articletitleMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:title//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:subtitle//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:label//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:title//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:subtitle//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:label//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:title//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:subtitle//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:label//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:title//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/sb:reference//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:other-ref//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:label//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:title//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:subtitle//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:title//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:label//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:title//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:subtitle//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:label//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:title//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:subtitle//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:label//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:title//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:label//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:title//text()",
		"/xocs:doc/xocs:nonserial-item/bk:glossary/ce:label//text()",
		"/xocs:doc/xocs:nonserial-item/bk:glossary/ce:title//text()",
		"/xocs:doc/xocs:nonserial-item/bk:glossary/ce:glossary/ce:section-title//text()",
		"/xocs:doc/xocs:nonserial-item/bk:index/ce:label//text()",
		"/xocs:doc/xocs:nonserial-item/bk:index/ce:title//text()",
		"/xocs:doc/xocs:nonserial-item/bk:index/ce:index/ce:section-title//text()",
		"/xocs:doc/xocs:nonserial-item/bk:bibliography/ce:label//text()",
		"/xocs:doc/xocs:nonserial-item/bk:bibliography/ce:title//text()",
		"/xocs:doc/xocs:nonserial-item/bk:bibliography/ce:further-reading/ce:section-title//text()"
	};
	
	private static String[] articletitlenormMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:normalized-article-title//text()"
	};
	
	private static String[] authMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration/ce:text//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:alt-name//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author/ce:alt-name//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration/ce:text//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:alt-name//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author/ce:alt-name//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration/ce:text//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:alt-name//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author/ce:alt-name//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration/ce:text//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:alt-name//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author/ce:alt-name//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration/ce:text//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration/ce:author-group/ce:author/ce:alt-name//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author/ce:alt-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:collaboration/ce:indexed-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:collaboration/ce:text//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:collaboration/ce:indexed-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:collaboration/ce:text//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:collaboration/ce:indexed-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:collaboration/ce:text//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:collaboration/ce:indexed-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:collaboration/ce:text//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:collaboration/ce:indexed-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:collaboration/ce:text//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author/ce:initials//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author/ce:indexed-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author/ce:degrees//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author/ce:suffix//text()"
	};
	
	private static String[] authDisplayArrayMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author"
	};
	
	private static String[] authfirstiniMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author/ce:given-name//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author/ce:given-name//text()"
	};
	
	private static String[] authfirstinitialnormMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:normalized-first-auth-initial//text()"
	};
	
	private static String[] authfirstsurnamenormMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:normalized-first-auth-surname//text()"
	};
	
	private static String[] authfullMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:collaboration//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:collaboration//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:collaboration//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:collaboration//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:collaboration//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author//text()"
	};
	
	private static String[] authkeywordsMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:keywords[@class = \"keyword\"]/ce:keyword//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:keywords[@class = \"keyword\"]/ce:keyword//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:keywords[@class = \"keyword\"]/ce:keyword//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:keywords[@class = \"keyword\"]/ce:keyword//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:keywords[@class = \"keyword\"]/ce:keyword//text()"
	};
	
	private static String[] authkeywordsArrayMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:keywords[@class = \"keyword\"]/ce:keyword[.//text()]",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:keywords[@class = \"keyword\"]/ce:keyword[.//text()]",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:keywords[@class = \"keyword\"]/ce:keyword[.//text()]",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:keywords[@class = \"keyword\"]/ce:keyword[.//text()]",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:keywords[@class = \"keyword\"]/ce:keyword[.//text()]"
	};
	
	private static String[] authlastMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author/ce:surname//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author/ce:surname//text()"
	};
	
	private static String[] authlastArrayMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author/ce:surname[.//text()]",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author/ce:surname[.//text()]",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author/ce:surname[.//text()]",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author/ce:surname[.//text()]",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author/ce:surname[.//text()]",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author/ce:surname[.//text()]",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author/ce:surname[.//text()]",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author/ce:surname[.//text()]",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author/ce:surname[.//text()]",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author/ce:surname[.//text()]"
	};
	
	private static String[] authsuffMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author/ce:suffix//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author/ce:suffixx//text()"
	};
	
	private static String[] bodyMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ce:floats//text()",		
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:body/ce:salutation//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:body/ce:sections//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ce:floats//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:body/ce:salutation//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:body/ce:sections//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:tail/ce:exam-answers//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:tail/ce:exam-questions//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:tail/ce:exam-reference//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ce:floats//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:body/ce:salutation//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:body/ce:sections//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ce:floats//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:body/ce:salutation//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:body/ce:sections//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ce:exam-answers//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ce:exam-questions//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ce:floats//text()",
		"/xocs:doc/xocs:rawtext//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:floats//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:sections//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:displayed-quote//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:floats//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:section//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:sections//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/bk:exam//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/bk:objectives//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/bk:poem//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/bk:subchapter//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:displayed-quote//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:floats//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:section//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:sections//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/bk:exam//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/bk:objectives//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/bk:poem//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/bk:subchapter//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:floats//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:para//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:section//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:floats//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/bk:exam//text()",
		"/xocs:doc/xocs:nonserial-item/bk:index/ce:index//text()"
	};
	
	private static String[] cidMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:cid//text()"
	};
	
	// Note: used in cids and cids-f
	private static String[] cidsMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:cid//text()",
		"/xocs:doc/xocs:meta/xocs:sat-metas/xocs:sat-meta/xocs:sat-cid//text()"
	};
	
	private static String[] confabbrMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/xocs:conference-info/xocs:abbr-name//text()"
	};
	
	private static String[] confdateMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/xocs:conference-info/xocs:conference-date-text//text()"
	};
	
	private static String[] confeditorMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/xocs:editors//text()",		
		"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/ce:editors/ce:author-group/ce:collaboration//text()",
		"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/ce:editors/ce:author-group/ce:author//text()",
		"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/ce:author-group/ce:collaboration//text()",
		"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/ce:author-group/ce:author//text()"
	};
	
	private static String[] conflocMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/xocs:conference-info/xocs:venue//text()"
	};
	
	private static String[] confsponsorMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/xocs:sponsors//text()"
	};
	
	private static String[] contentsubtypeMappings =  new String[] {
		"/xocs:doc/xocs:meta/xocs:content-subtype//text()"
	};
	
	private static String[] contenttypetxtMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:content-type//text()"
	};
	
	private static String[] copyrightMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:copyright-line//text()"
	};
	
	private static String[] dateloadedtxtMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:orig-load-date//text()"
	};
	
	private static String[] dedicationMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:dedication//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:dedication//text()"
	};
	
	private static String[] docsubtypeMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:document-subtype//text()"
	};
	
	private static String[] doctopicMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:item-info/ce:doctopics//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:item-info/ce:doctopics//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:item-info/ce:doctopics//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:item-info/ce:doctopics//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:item-info/ce:doctopics//text()",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/bk:info/ce:doctopics//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/bk:info/ce:doctopics//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/bk:info/ce:doctopics//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/bk:info/ce:doctopics//text()",
		"/xocs:doc/xocs:nonserial-item/bk:examination/bk:info/ce:doctopics//text()",
		"/xocs:doc/xocs:nonserial-item/bk:glossary/bk:info/ce:doctopics//text()",
		"/xocs:doc/xocs:nonserial-item/bk:index/bk:info/ce:doctopics//text()",
		"/xocs:doc/xocs:nonserial-item/bk:bibliography/bk:info/ce:doctopics//text()"
	};
	
	private static String[] doctypeMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:document-type//text()"
	};
	
	private static String[] doiMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:doi//text()"
	};
	
	private static String[] editionMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:edition//text()"
	};
	
	private static String[] eidMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:eid//text()"
	};
	
	private static String[] ewtransactionidMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:ew-transaction-id//text()"
	};
	
	private static String[] footnotesMappings = new String[] {
		"//ce:footnote//text()"
	};
	
	private static String[] fundingbodyidMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:funding-body-id//text()"
	};
	
	private static String[] glossaryMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:tail/ce:glossary//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:tail/ce:glossary//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-tail/ce:glossary//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:simple-tail/ce:glossary//text()",
		"/xocs:doc/xocs:nonserial-item/bk:glossary/ce:glossary//text()"
	};
	
	private static String[] grantnumberMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:body/ce:acknowledgment/ce:grant-number//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:body/ce:acknowledgment/ce:grant-number//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:body/ce:acknowledgment/ce:grant-number//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:body/ce:acknowledgment/ce:grant-number//text()"
	};

	private static String[] grantsponsorMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:body/ce:acknowledgment/ce:grant-sponsor//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:body/ce:acknowledgment/ce:grant-sponsor//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:body/ce:acknowledgment/ce:grant-sponsor//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:body/ce:acknowledgment/ce:grant-sponsor//text()"
	};
	
	private static String[] grantsponsoridMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:body/ce:acknowledgment/ce:grant-sponsor[@sponsor-id]//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:body/ce:acknowledgment/ce:grant-sponsor[@sponsor-id]//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:body/ce:acknowledgment/ce:grant-sponsor[@sponsor-id]//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:body/ce:acknowledgment/ce:grant-sponsor[@sponsor-id]//text()"
	};
	
	private static String[] highlightsabstMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:abstract[@class = \"author-highlights\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:abstract[@class = \"editor-highlights\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:abstract[@class = \"author-highlights\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:abstract[@class = \"editor-highlights\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:abstract[@class = \"author-highlights\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:abstract[@class = \"editor-highlights\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:abstract[@class = \"author-highlights\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:abstract[@class = \"editor-highlights\"]//text()"
	};

	private static String[] hubeidMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:hub-eid//text()"
	};
	
	private static String[] indextypeMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:index-type//text()"
	};
	
	private static String[] isbnMappings = new String[] { 
		"/xocs:doc/xocs:meta/xocs:isbns/xocs:isbn-primary-formatted//text()"
	};
	
	private static String[] isbnsMappings = new String[] { 
		"/xocs:doc/xocs:meta/xocs:isbns/xocs:isbn-primary-formatted//text()",
		"/xocs:doc/xocs:meta/xocs:sat-metas/xocs:sat-meta/xocs:sat-isbns/xocs:sat-isbn-formatted//text()"
	};
	
	private static String[]  isbnnormMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:isbns/xocs:isbn-primary-unformatted//text()"
	};
	
	private static String[]  isbnsnormMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:isbns/xocs:isbn-primary-unformatted//text()",
		"/xocs:doc/xocs:meta/xocs:sat-metas/xocs:sat-meta/xocs:sat-isbns/xocs:sat-isbn-unformatted//text()"
	};
	
	private static String[] issuedisplayMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:iss-first//text()",
		"/xocs:doc/xocs:meta/xocs:suppl//text()"
	};
	
	private static String[] issnMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:issns/xocs:issn-primary-formatted//text()"
	};
	
	private static String[] issnnormMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:issns/xocs:issn-primary-unformatted//text()"
	};
	
	private static String[] itemstageMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:item-stage//text()"
	};
	
	private static String[] itemtransactionidMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:timestamp//text()"
	};
	
	private static String[] itemweightMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:item-weight//text()"
	};
	
	private static String[] markersMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:markers//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:markers//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:markers//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:markers//text()"
	};
	
	private static String[] misctextMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:miscellaneous//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:miscellaneous//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:miscellaneous//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:miscellaneous//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:miscellaneous//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:miscellaneous//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:miscellaneous//text()"
	};
	
	private static String[] nomenclatureMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:body/ce:nomenclature//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:body/ce:nomenclature//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:body/ce:nomenclature//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:body/ce:nomenclature//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:nomenclature//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:nomenclature//text()",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:nomenclature//text()"
	};
	
	private static String[] nonengabstMappings =  new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:abstract[@xml:lang != \"en\"][@class = \"author\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:abstract[@xml:lang != \"en\"][@class = \"editor\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:abstract[@xml:lang != \"en\"][@class = \"author\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:abstract[@xml:lang != \"en\"][@class = \"editor\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:abstract[@xml:lang != \"en\"][@class = \"author\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:abstract[@xml:lang != \"en\"][@class = \"editor\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:abstract[@xml:lang != \"en\"][@class = \"author\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:abstract[@xml:lang != \"en\"][@class = \"editor\"]//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:abstract[@xml:lang != \"en\"][@class = \"author\"]//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:abstract[@xml:lang != \"en\"][@class = \"editor\"]//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:abstract[@xml:lang != \"en\"][@class = \"author\"]//text()",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:abstract[@xml:lang != \"en\"][@class = \"editor\"]//text()"
	};
	
	private static String[] orcidMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author[@orcid]//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author[@orcid]//text()"
	};
	
	private static String[] otherkwdsMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:keywords[@class != \"keyword\"]/ce:keyword//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:keywords[@class != \"keyword\"]/ce:keyword//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:keywords[@class != \"keyword\"]/ce:keyword//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:keywords[@class != \"keyword\"]/ce:keyword//text()",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:keywords[@class != \"keyword\"]/ce:keyword//text()"
	};
	
	private static String[] otherkwdsArrayMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:keywords[@class != \"keyword\"]/ce:keyword[.//text()]",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:keywords[@class != \"keyword\"]/ce:keyword[.//text()]",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:keywords[@class != \"keyword\"]/ce:keyword[.//text()]",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:keywords[@class != \"keyword\"]/ce:keyword[.//text()]",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:keywords[@class != \"keyword\"]/ce:keyword[.//text()]"
	};
	
	private static String[] pgMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:pages//text()"
	};
	
	private static String[] pgfirstMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:first-fp//text()"
	};
	
	private static String[] pglastMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:last-lp//text()"
	};
	
	private static String[] piiMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:pii-formatted//text()"
	};
	
	private static String[] piinormMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:pii-unformatted//text()"
	};
	
	private static String[] preprintMappings = new String[] {
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:item-info/ce:preprint//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:item-info/ce:preprint//text()",
			"/xocs:doc/xocs:serial-item/ja:simple-article/ja:item-info/ce:preprint//text()",
			"/xocs:doc/xocs:serial-item/ja:book-review/ja:item-info/ce:preprint//text()",
			"/xocs:doc/xocs:serial-item/ja:exam/ja:item-info/ce:preprint//text()"
		};
	
	private static String[] presentedbyMappings = new String[] {
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:presented//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:presented//text()"
		};
	
	private static String[] primabstMappings = new String[] {
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:abstract[@xml:lang = (\"en\")][@class = \"author\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:abstract[not(@xml:lang)][@class = \"author\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:abstract[@xml:lang = (\"en\")][@class = \"editor\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:abstract[not(@xml:lang)][@class = \"editor\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:abstract[@xml:lang = (\"en\")][@class = \"author\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:abstract[not(@xml:lang)][@class = \"author\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:abstract[@xml:lang = (\"en\")][@class = \"editor\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:abstract[not(@xml:lang)][@class = \"editor\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:abstract[@xml:lang = (\"en\")][@class = \"author\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:abstract[not(@xml:lang)][@class = \"author\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:abstract[@xml:lang = (\"en\")][@class = \"editor\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:abstract[not(@xml:lang)][@class = \"editor\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:abstract[@xml:lang = (\"en\")][@class = \"author\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:abstract[not(@xml:lang)][@class = \"author\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:abstract[@xml:lang = (\"en\")][@class = \"editor\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:abstract[not(@xml:lang)][@class = \"editor\"]//text()",
			"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:intro//text()",
			"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:intro//text()",
			"/xocs:doc/xocs:nonserial-item/bk:examination/ce:intro//text()",
			"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:abstract[@xml:lang = (\"en\")][@class = \"author\"]//text()",
			"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:abstract[not(@xml:lang)][@class = \"author\"]//text()",
			"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:abstract[@xml:lang = (\"en\")][@class = \"editor\"]//text()",
			"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:abstract[not(@xml:lang)][@class = \"editor\"]//text()",
			"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:abstract[@xml:lang = (\"en\")][@class = \"author\"]//text()",
			"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:abstract[not(@xml:lang)][@class = \"author\"]//text()",
			"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:abstract[@xml:lang = (\"en\")][@class = \"editor\"]//text()",
			"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:abstract[not(@xml:lang)][@class = \"editor\"]//text()"
		};
	
	private static String[] pubdateendMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:cover-date-end//text()"
	};
	
	private static String[] pubdatestartMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:cover-date-start//text()"
	};
	
	private static String[] pubdatetxtMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:cover-date-text//text()"
	};
	
	private static String[] pubtypeMappings = new String[] {
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:dochead//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:dochead//text()",
			"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:dochead//text()",
			"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:dochead//text()",
			"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:dochead//text()"
		};
	
	private static String[] pubyrMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:cover-date-year//text()"
	};
	
	private static String[] pubyrArrayMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:cover-date-year"
	};
	
	private static String[] refMappings =  new String[] {
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:tail/ce:bibliography//text()",
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:tail/ce:further-reading//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:tail/ce:bibliography//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:tail/ce:further-reading//text()",
			"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-tail/ce:bibliography//text()",
			"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-tail/ce:further-reading//text()",
			"/xocs:doc/xocs:serial-item/ja:book-review/ja:simple-tail/ce:bibliography//text()",
			"/xocs:doc/xocs:serial-item/ja:book-review/ja:simple-tail/ce:further-reading//text()",
			"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:bibliography//text()",
			"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:further-reading//text()",
			"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:bibliography//text()",
			"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:further-reading//text()",
			"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:bibliography//text()",
			"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:further-reading//text()",
			"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:bibliography//text()",
			"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:further-reading//text()",
			"/xocs:doc/xocs:nonserial-item/bk:bibliography/ce:further-reading//text()"
		};
	
	private static String[] restrictedaccessdateMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:restricted-access-date//text()"
	};
	
	private static String[] restrictedaccessdelayMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:restricted-access-day-delay//text()"
	};
	
	private static String[] restrictedaccesstypeMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:restricted-access-type//text()"
	};
	
	
	private static String[] saffilMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:affiliation",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:affiliation",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:affiliation",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:affiliation",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:affiliation",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:affiliation",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:affiliation",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:affiliation",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:affiliation",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:affiliation"
	};
	
	private static String[] satsrcinfoMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:sat-metas/xocs:sat-meta/xocs:sat-cid//text()",
		"/xocs:doc/xocs:meta/xocs:sat-metas/xocs:sat-meta/xocs:sat-srctitle//text()"
	};
	
	private static String[] satsrctitlesMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:sat-metas/xocs:sat-meta/xocs:sat-srctitle//text()"
	};
	
	private static String[] sauthMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:author-group/ce:author",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:author-group/ce:author",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:author-group/ce:author",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:serial-item/ja:book-review/ja:book-review-head/ce:author-group/ce:author",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:author-group/ce:author",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:nonserial-item/bk:introduction/ce:author-group/ce:author",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:author-group/ce:author",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:author-group/ce:author",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:nonserial-item/bk:fb-non-chapter/ce:author-group/ce:author",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:collaboration",
		"/xocs:doc/xocs:nonserial-item/bk:examination/ce:author-group/ce:author"
	};
	
	private static String[] sauthfirstiniMappings = new String[] {
		".//ce:given-name//text()"
	};
	
	private static String[] sauthlastMappings = new String[] {
		".//ce:surname//text()"
	};
	
	private static String[] sauthsuffMappings = new String[] {
		".//ce:suffix//text()"
	};
	
	
	private static String[] sdabstMappings = new String[] {
		"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:abstract[@class = \"sda\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:abstract[@class = \"sda\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:abstract[@class = \"sda\"]//text()",
		"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:abstract[@class = \"sda\"]//text()"
	};
	
	private static String[] sectiontitleMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:hub-sec//text()"
	};
	
	private static String[] smartlinksMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:sat-metas/xocs:sat-meta/xocs:sat-ann/xocs:sat-ann-concept-target//text()"
	};
	
	private static String[] sortorderMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:sort-order//text()"
	};
	
	private static String[] specialabstMappings = new String[] {
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:abstract[@class = \"graphical\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:abstract[@class = \"graphical\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:abstract[@class = \"graphical\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:abstract[@class = \"graphical\"]//text()",
			"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:abstract[@class = \"graphical\"]//text()",
			"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:abstract[@class = \"graphical\"]//text()"
		};
	
	private static String[] sponsoredaccessdateMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:sponsored-access-date//text()"
	};
	
	private static String[] sponsoredaccessdelayMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:sponsored-access-day-delay//text()"
	};
	
	private static String[] sponsoredaccesstypeMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:sponsored-access-type//text()"
	};
	
	// Note these are used for both srctitle and srctitle-f fields
	private static String[] srctitleMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:srctitle//text()",
		"/xocs:doc/xocs:meta/xocs:src-subtitle//text()"
	};
	
	private static String[] srctitlenormMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:normalized-srctitle//text()"
	};
	
	private static String[] srctypeMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:content-family//text()"
	};
	
	private static String[] stereochemabstMappings =  new String[] {
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:stereochem//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:stereochem//text()"
		};
	
	private static String[] subheadingsMappings =  new String[] {
		"/xocs:doc/xocs:meta/xocs:item-toc//text()"
	};
	
	private static String[] subheadingsArrayMappings =  new String[] {
		"/xocs:doc/xocs:meta/xocs:item-toc//xocs:item-toc-entry[xocs:item-toc-section-title]"
	};
	
	private static String[] supplMappings =  new String[] {
		"/xocs:doc/xocs:meta/xocs:suppl//text()"
	};
	
	private static String[] teaserabstMappings = new String[] {
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:head/ce:abstract[@class = \"teaser\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:head/ce:abstract[@class = \"teaser\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-head/ce:abstract[@class = \"teaser\"]//text()",
			"/xocs:doc/xocs:serial-item/ja:exam/ja:simple-head/ce:abstract[@class = \"teaser\"]//text()",
			"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:abstract[@class = \"teaser\"]//text()",
			"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:abstract[@class = \"teaser\"]//text()"
		};
	
	private static String[] vitaeMappings = new String[] {
			"/xocs:doc/xocs:serial-item/ja:converted-article/ja:tail/ce:biography//text()",
			"/xocs:doc/xocs:serial-item/ja:article/ja:tail/ce:biography//text()",
			"/xocs:doc/xocs:serial-item/ja:simple-article/ja:simple-tail/ce:biography//text()",
			"/xocs:doc/xocs:serial-item/ja:book-review/ja:simple-tail/ce:biography//text()",
			"/xocs:doc/xocs:nonserial-item/bk:chapter/ce:biography//text()",
			"/xocs:doc/xocs:nonserial-item/bk:simple-chapter/ce:biography//text()"
		};
	
	private static String[] volMappings = new String[] {
			"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/ce:title//text()",
			"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/ce:subtitle//text()",
			"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/ce:alt-title//text()",
			"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/ce:alt-subtitle//text()",
			"/xocs:doc/xocs:meta/xocs:title-editors-groups/xocs:title-editors-group/xocs:conference-info/xocs:full-name//text()"
		};
	
	private static String[] volfirstMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:vol-first//text()"
	};
	
	private static String[] volissueMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:vol-iss-suppl-text//text()"
	};
	
	
	private static String[] webpdfMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:attachment-metadata-doc/xocs:attachments/xocs:web-pdf[xocs:web-pdf-purpose=\"MAIN\"]//text()"
	};
	
	private static String[] webpdfEidDisplayMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:attachment-metadata-doc/xocs:attachments/xocs:web-pdf[xocs:web-pdf-purpose=\"MAIN\"]/xocs:attachment-eid//text()"
	};
	
	private static String[] webpdfSizeDisplayMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:attachment-metadata-doc/xocs:attachments/xocs:web-pdf[xocs:web-pdf-purpose=\"MAIN\"]/xocs:filesize//text()"
	};
	
	//
	// Compound Field mappings
	// Note: These fields must be generated after the single fields are generated to take advantage of the cached values to speed processing
	//
	
	private static String[] absMappings =  ArrayConcat.concatAll(primabstMappings, highlightsabstMappings, nonengabstMappings, sdabstMappings, specialabstMappings, stereochemabstMappings, teaserabstMappings);
	
	private static String[] itemtitleMappings =  ArrayConcat.concatAll(articletitleMappings, altitemtitleMappings);

	private static String[] keywordsMappings =  ArrayConcat.concatAll(authkeywordsMappings, otherkwdsMappings);
	private static String[] keywordsArrayMappings =  ArrayConcat.concatAll(authkeywordsArrayMappings, otherkwdsArrayMappings);
	
	private static String[] srctitleplusMappings =  ArrayConcat.concatAll(srctitleMappings, volMappings);
	
	private static String[] allMappings =  ArrayConcat.concatAll(absMappings, acknowledgeMappings, affilMappings, aiptxtMappings, appendicesMappings, authMappings, bodyMappings, 
												confabbrMappings, confdateMappings, confeditorMappings, conflocMappings, confsponsorMappings, dedicationMappings,
												doctopicMappings, doiMappings, footnotesMappings, glossaryMappings, isbnMappings, issnMappings, itemtitleMappings,
												keywordsMappings, nomenclatureMappings, pgMappings, piiMappings, preprintMappings, presentedbyMappings, pubdatetxtMappings,
												pubtypeMappings, refMappings, sectiontitleMappings, srctitleMappings, subheadingsMappings, vitaeMappings, volissueMappings,
												volMappings);
	
	private static String[] allmedMappings =  ArrayConcat.concatAll(absMappings, acknowledgeMappings, affilMappings, aiptxtMappings, appendicesMappings, authMappings, bodyMappings, 
												confabbrMappings, confdateMappings, confeditorMappings, conflocMappings, confsponsorMappings, dedicationMappings,
												doiMappings, footnotesMappings, glossaryMappings, issnMappings, itemtitleMappings,
												keywordsMappings, nomenclatureMappings, pgMappings, piiMappings, preprintMappings, presentedbyMappings, pubdatetxtMappings,
												sectiontitleMappings, srctitleMappings, subheadingsMappings, vitaeMappings, volissueMappings, volMappings);

	private static String[] allsmallMappings =  ArrayConcat.concatAll(itemtitleMappings, absMappings, keywordsMappings);
	

	
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
			xpath.setNamespaceContext(new ScienceDirectNamespaceContext());
			
			// Begin applying rules ...

			//
			// First generate the atomic fields
			//
			
			createSingleField("absattachment", absattachmentMappings);
			
			createSingleField("acknowledge", acknowledgeMappings);
			
			
			createSingleField("affil", affilMappings);
			
			
			createSingleField("aiptxt", aiptxtMappings);
			

			createSingleField("altitemtitle", altitemtitleMappings);
			
			createSingleField("appendices", appendicesMappings);
		
			createSingleField("articletitle", articletitleMappings);
	
			
			createSingleField("articletitlenorm", articletitlenormMappings);
			
			createSingleField("auth", authMappings);
			
			createArray("authdisplay", authDisplayArrayMappings, "(./ce:text | .//ce:given-name | .//ce:surname)//text()");
			
			createSingleField("authfirstini", authfirstiniMappings);

			createSingleField("authfirstinitialnorm", authfirstinitialnormMappings);
			
			createSingleField("authfirstsurnamenorm", authfirstsurnamenormMappings);

			createSingleField("authfull", authfullMappings);

			createArray("authkeywords", authkeywordsArrayMappings, ".//text()");
			
			
			createSingleField("authlast", authlastMappings);
			createArray("authlast-f", authlastArrayMappings, ".//text()");
			
			createSingleField("authsuff", authsuffMappings);
				
			createSingleField("body", bodyMappings);
						
			createSingleField("cid", cidMappings);
						
			createSingleField("cids", cidsMappings);
			
			createSingleField("cids-f", cidsMappings);
			
			createSingleField("confabbr", confabbrMappings);
			
			createSingleField("confdate", confdateMappings);
			
			createSingleField("confeditor", confeditorMappings);
			
			createSingleField("confloc", conflocMappings);
		
			createSingleField("confsponsor", confsponsorMappings);
			
			createSingleField("contentsubtype", contentsubtypeMappings);
			
			createSingleField("contenttypetxt", contenttypetxtMappings);
			
			createSingleField("copyright", copyrightMappings);					
			
			createSingleField("dateloadedtxt", dateloadedtxtMappings);			
			
			createSingleField("dedication", dedicationMappings);
		
			createSingleField("docsubtype", docsubtypeMappings);					
			
			createSingleField("doctopic", doctopicMappings);
			
			createSingleField("doctype", doctypeMappings);			
			
			createSingleField("doi", doiMappings);			
			
			createSingleField("edition", editionMappings);
						
			createSingleField("eid", eidMappings);	
			
			createSingleField("ewtransactionid", ewtransactionidMappings);				
			
			createSingleField("footnotes", footnotesMappings);
			
			createSingleField("fundingbodyid", fundingbodyidMappings);	
			
			createSingleField("glossary", glossaryMappings);
				
			createSingleField("grantnumber", grantnumberMappings);				

			createSingleField("grantsponsor", grantsponsorMappings);
	
			createSingleField("grantsponsorid", grantsponsoridMappings);	
			
			createSingleField("highlightsabst", highlightsabstMappings);
			
			createSingleField("hubeid", hubeidMappings);				
			
			createSingleField("indextype", indextypeMappings);
			
			createSingleField("isbn", isbnMappings);	
			
			createSingleField("isbns", isbnsMappings);			
			
			createSingleField("isbnnorm", isbnnormMappings);
			
			createSingleField("isbnsnorm", isbnsnormMappings);
			
			createSingleField("issuedisplay", issuedisplayMappings);
			
			createSingleField("issn", issnMappings);
			
			createSingleField("issnnorm", issnnormMappings);			
			
			createSingleField("itemstage", itemstageMappings);			

			createSingleField("itemtransactionid", itemtransactionidMappings);			
			
			createSingleField("itemweight", itemweightMappings);				
			
			createSingleField("markers", markersMappings);
			
			createSingleField("misctext", misctextMappings);
			
			createSingleField("nomenclature", nomenclatureMappings);
	
			createSingleField("nonengabst", nonengabstMappings);
		
			createSingleField("orcid", orcidMappings);
			
			createArray("otherkwds", otherkwdsArrayMappings, ".//text()");
			
			createSingleField("pg", pgMappings);
			
			createSingleField("pgfirst", pgfirstMappings);
			
			createSingleField("pglast", pglastMappings);
			
			createSingleField("pii", piiMappings);
			
			createSingleField("piinorm", piinormMappings);
			
			createSingleField("preprint", preprintMappings);
			
			createSingleField("presentedby",presentedbyMappings);
			
			createSingleField("primabst", primabstMappings);
			
			createSingleField("pubdateend", pubdateendMappings);
			
			createSingleField("pubdatestart", pubdatestartMappings);
			
			createSingleField("pubdatetxt", pubdatetxtMappings);
			
			createSingleField("pubtype", pubtypeMappings);
			
			
			createArray("pubyr", pubyrArrayMappings, ".//text()");
			
			createSingleField("ref", refMappings);
			createSingleField("restrictedaccessdate", restrictedaccessdateMappings);
			createSingleField("restrictedaccessdelay", restrictedaccessdelayMappings);
			createSingleField("restrictedaccesstype", restrictedaccesstypeMappings);
			createSingleField("satsrcinfo", satsrcinfoMappings);
			createSingleField("satsrctitles", satsrctitlesMappings);
			createSingleField("sdabst", sdabstMappings);
			createSingleField("sectiontitle", sectiontitleMappings);
			createSingleField("smartlinks",smartlinksMappings);
			createSingleField("sortorder", sortorderMappings);
			createSingleField("specialabst", specialabstMappings);
			
			createSingleField("sponsoredaccessdate", sponsoredaccessdateMappings);
			createSingleField("sponsoredaccessdelay", sponsoredaccessdelayMappings);
			createSingleField("sponsoredaccesstype", sponsoredaccesstypeMappings);
			createSingleField("srctitle", srctitleMappings);
			createSingleField("srctitlenorm", srctitlenormMappings);
			createSingleField("srctitleplus", srctitleplusMappings);
			createSingleField("srctype", srctypeMappings);
			createSingleField("stereochemabst", stereochemabstMappings);
			
			createArray("subheadings", subheadingsArrayMappings, ".//text()");
			createSingleField("suppl", supplMappings);
			createSingleField("teaserabst", teaserabstMappings);
			createSingleField("vitae", vitaeMappings);
			createSingleField("vol", volMappings);
			
			
			createSingleField("volfirst", volfirstMappings);
			createSingleField("volissue", volissueMappings);
			
			createSingleField("webpdf", webpdfMappings);
			createSingleField("webpdfeiddisplay", webpdfEidDisplayMappings);
			createSingleField("webpdfsizedisplay", webpdfSizeDisplayMappings);
		
			//
			// Now generate the composite fields
			//
			
			createCompositeSingleField("abs", absMappings);
			createCompositeSingleField("itemtitle", itemtitleMappings);
		
			createArray("keywords", keywordsArrayMappings, ".//text()");
			createCompositeSingleField("srctitle", srctitleMappings);
			createCompositeSingleField("all", allMappings);
			createCompositeSingleField("allmed", allmedMappings);
			createCompositeSingleField("allsmall", allsmallMappings);
			
			
			HashMap<String, String[]> saffilSubFields = new HashMap<String, String[]>();
			saffilSubFields.put("affil", new String[]{".//text()"});
			createNestedField("saffil", saffilMappings, saffilSubFields);
			createNestedField("saffil-p", saffilMappings, saffilSubFields);  // Is this right?
			
			HashMap<String, String[]> sauthSubFields = new HashMap<String, String[]>();
			sauthSubFields.put("authfirstini", sauthfirstiniMappings);
			sauthSubFields.put("authlast", sauthlastMappings);
			sauthSubFields.put("authsuff", sauthsuffMappings);
			createNestedField("sauth", sauthMappings, sauthSubFields);
			
			String[] skeywordMappings = new String[] {
				".//text()"
			};
			HashMap<String, String[]> skeywordsSubFields = new HashMap<String, String[]>();
			skeywordsSubFields.put("skeyword", skeywordMappings);
			createNestedField("skeywords", keywordsArrayMappings, skeywordsSubFields);
			createNestedField("skeywords-p", keywordsArrayMappings, skeywordsSubFields);  // Is this right??
			
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
