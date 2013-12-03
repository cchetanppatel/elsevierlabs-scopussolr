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
 * Transform a Scopus Author record into a HashMap containing a field name and the associated
 * value.  Xpath expressions will be used to map the associated elements (and the descendant text)
 * to a specified field name.  Currently, the code assumes a field will only have one value.
 * 
 * @author Curt Kohler
 *
 */
public class AbstractTransform {

	Document doc = null;
	XPath xpath = null;
	HashMap<String,Object> fieldValues = new HashMap<String,Object>();
	HashMap<String, String> cachedFieldValues = new HashMap<String, String>();
	
	private static int DEFAULT_JSON_FIELD_STRINGBUILDER_SIZE = 1024;
	
	// Basic field mappings	
	private static String[] absArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/abstracts/abstract"
	};
	
	private static String[] abslangArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-info/abstract-language"
	};
			
	private static String[] affilcityArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/city-group",
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/city"
	};
	
	private static String[] affilctryArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/country"
	};
	
	private static String[] affilorgArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/organization",
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/ce:text"
	};
	
	private static String[] afhistidArrayMappings = new String[] {
		"/xocs:doc/xocs:author-profile/author-profile/affiliation-history/affiliation/ip-doc[@type='parent']"
	};

	private static String[] afidArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation"
	};	

	private static String[] artnumArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/article-number//text()"
	};	
	
	private static String[] auciteArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:surname",
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:initials"
	};	
	
	private static String[] authemailArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:e-address"
	};	
	
	private static String[] authfirstiniArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:initials"
	};	
	
	private static String[] authgrpidArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group"
	};	
	
	private static String[] authidArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author"
	};	
	
	private static String[] authidxnameArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:indexed-name"
	};	
	
	private static String[] authkeywordsArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-info/author-keywords/author-keyword"
	};	
	
	private static String[] authlastArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:surname"
	};	
	
	private static String[] authsuffArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:suffix"
	};	
	
	private static String[] casregistrynumArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/chemicalgroup/chemicals/chemical/cas-registry-number"
	};	
	
	private static String[] chemArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/chemicalgroup/chemicals/chemical"
	};
	
	private static String[] chemnameArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/chemicalgroup/chemicals/chemical/chemical-name"
	};
	
	private static String[] codenMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/codencode//text()"
	};	
	
	private static String[] collabArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/collaboration"
	};
	
	private static String[] collecidMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/itemidlist/itemid[@idtype='SCP']/@idtype"
	};
	
	private static String[] confcodeMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/additional-srcinfo/conferenceinfo/confevent/confcode//text()"
	};
	
	private static String[] conflocMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/additional-srcinfo/conferenceinfo/confevent/conflocation//text()"
	};
	
	private static String[] confnameMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/additional-srcinfo/conferenceinfo/confevent/confname//text()"
	};
	
	private static String[] confsponsorArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/additional-srcinfo/conferenceinfo/confevent/confsponsors/confsponsor"
	};
	
	private static String[] copyrightArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/copyright"
	};
	
	private static String[] corresArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/correspondence"
	};
	
	private static String datecompletedtxtMappings = "/xocs:doc/xocs:item/item/ait:process-info/ait:date-delivered/@timestamp";
	
	private static String[] dateloadedArrayMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:timestamp"
	};
	
	private static String[] daterevisedArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/history/date-revised"
	};
	
	private static String datesortMappings = "/xocs:doc/xocs:item/item/ait:process-info/ait:date-sort//text()";
	
	private static String[] dbArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/dbcollection"
	};
	
	private static String[] doiMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/itemidlist/ce:doi//text()"
	};
	
	private static String[] dptidArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation"
	};
	
	private static String[] dummycodeMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-info/dummy-link/gen-citationtype/@code"
	};
	
	private static String[] dummylinkMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-info/dummy-link/itemlink//text()"
	};
	
	private static String[] edaddressMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/additional-srcinfo/conferenceinfo/confpublication/confeditors/editoraddress//text()"
	};
	
	private static String[] edfirstiniArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/editors/editor/ce:initials",
		"/xocs:doc/xocs:item/item/bibrecord/head/source/additional-srcinfo/conferenceinfo/confpublication/confeditors/editors/editor/ce:initials",
		"/xocs:doc/xocs:item/item/bibrecord/head/source/contributor-group/contributor[@role='edit']/ce:initials"
	};
	
	private static String[] edidxnameArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/editors/editor/ce:indexed-name",
		"/xocs:doc/xocs:item/item/bibrecord/head/source/additional-srcinfo/conferenceinfo/confpublication/confeditors/editors/editor/ce:indexed-name",
		"/xocs:doc/xocs:item/item/bibrecord/head/source/contributor-group/contributor[@role='edit']/ce:indexed-name"
	};
	
	private static String[] editionMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/edition//text()"	
	};
	
	private static String[] edlastArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/editors/editor/ce:surname",
		"/xocs:doc/xocs:item/item/bibrecord/head/source/additional-srcinfo/conferenceinfo/confpublication/confeditors/editors/editor/ce:surname",
		"/xocs:doc/xocs:item/item/bibrecord/head/source/contributor-group/contributor[@role='edit']/ce:surname"
	};
	
	private static String[] eidMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:eid//text()"
	};
	
	private static String[] eissnMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/issn[@type='electronic']//text()"
	};
	
	private static String[] firstauthArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author[@seq='1']/ce:indexed-name"
	};
	
	private static String[] fundacrArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/grantlist/grant/grant-acronym"
	};
	
	private static String[] fundnoArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/grantlist/grant/grant-id"
	};
	
	private static String[] fundsponsorArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/grantlist/grant/grant-agency"
	};
	
	private static String[] groupidMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/itemidlist/itemid[@idtype='SGR']//text()"	
	};
	
	private static String[] idxtermsArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/descriptorgroup/descriptors/descriptor/mainterm"
	};
	
	private static String[] idxtypeArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/descriptorgroup/descriptors"
	};
	
	private static String[] isbnArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/isbn"
	};
	
	private static String[] issnArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/issn"
	};
	
	private static String[] issnpArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/issn[@type = 'print']"
	};
	
	private static String[] issueMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/volisspag/voliss/@issue"	
	};
	
	private static String[] itemtitleArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-title/titletext"
	};
	
	private static String[] langArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-info/citation-language"
	};
	
	private static String[] langitemtitleArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-title/titletext"
	};
	
	private static String[] langreftitleArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/citation-title/titletext"
	};
	
	private static String[] loadnumArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/itemidlist/itemid[@idtype='PUI']"
	};
	
	
	private static String[] manufArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/manufacturergroup/manufacturers/manufacturer"
	};
	
	private static String[] oeidMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:oeid//text()"	
	};
	
	private static String[] pagecountMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/volisspag/pagecount//text()"	
	};
	
	private static String[] partMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/part//text()"	
	};
	
	private static String[] patinfoArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/patent"
	};
	
	private static String[] pgfirstMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/volisspag/pagerange/@first"	
	};
	
	private static String[] pginfoMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/volisspag/pages//text()"	
	};
	
	private static String[] pglastMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/volisspag/pagerange/@last"	
	};
	
	private static String[] piiMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/itemidlist/ce:pii//text()"	
	};
	
	private static String[] pmidMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/itemidlist/itemid[@idtype='MEDL']//text()"	
	};
	
	/*private static String[] prefnameauid = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/itemidlist/itemid[@idtype='MEDL']//text()"	
	};*/
	
	private static String[] pubArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/publisher"	
	};
	
	private static String pubdatetxtMappings = "/xocs:doc/xocs:item/item/bibrecord/head/source/publicationdate/date-text//text()";
	
	private static String pubyrMappings = "/xocs:doc/xocs:item/item/ait:process-info/ait:date-sort/@year";

	private static String[] refartnumArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/article-number"	
	};
	
	private static String[] refauidArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/author-group/author"	
	};
	
	private static String[] refcountMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/@refcount"	
	};
	
	private static String[] refeidArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/eid",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/oeid"
	};
	
	private static String[] refpgArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/volisspag/pages",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-volisspag/pages"
	};
	
	private static String[] refpgfirstArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/volisspag/pagerange",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-volisspag/pagerange"
	};
	
	private static String[] refpubyrArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/publicationyear",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-publicationyear"
	};
	
	private static String[] refscpArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/refd-itemidlist/itemid[@idtype='SGR']"
	};
	
	private static String[] refsrctitleArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/sourcetitle",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-sourcetitle"
	};
	
	private static String[] reftitleArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/citation-title/titletext",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-title/ref-titletext"
	};
	
	private static String[] relauthidArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author"
	};

	private static String[] restrictedaccessMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-info/dummy-link/@restricted-access"	
	};
	
	private static String[] sdeidMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:fulltext-eid//text()"
	};	
	
	private static String[] seqbankArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/sequencebanks/sequencebank"
	};
	
	private static String[] seqnumberArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/sequencebanks/sequencebank/sequence-number"
	};
	
	private static String[] srcidMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/@srcid"	
	};
	
	private static String[] srctitleMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/sourcetitle//text()"
	};
	
	private static String[] srctitleabbrMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/sourcetitle-abbrev//text()"
	};
	
	private static String[] srctypeMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/@type"
	};
	
	private static String[] subjabbrArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/classifictiongroup/classifications[@type='SUBJABBR']/classification"
	};
	
	private static String[] subjmainArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/classificationgroup/classifications[@type='ASJC']/classification"
	};
	
	private static String[] subjtermsArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/classificationgroup/classifications"
	};
	
	private static String[] subjtypeArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/classificationgroup/classifications"
	};
	
	private static String[] subtypeMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-info/citation-type/@code"
	};	
	
	private static String[] supplementMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/volisspag/supplement//text()"
	};
	
	private static String[] tradenamesArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/ tradenamegroup/tradenames/trademanuitem/tradename"
	};
	
	private static String[] volMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/volisspag/voliss/@volume"
	};
	
	private static String[] websiteMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/website//text()",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-website//text()"
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
			
			createArray("abs",absArrayMappings, "(.//text())");
			
			createSortFieldFromArrayField("afdispcity-s", "afdispcity");
			
			createArray("abslang",abslangArrayMappings, "(xml:lang)");
						
			createArray("affilcity",affilcityArrayMappings, "(.//text())");
						
			createArray("affilctry",affilctryArrayMappings, "(.//text())");
			
			createSortFieldFromArrayField("affilctry-s", "affilctry");
			
			//createArray("affilcurr",affilcurrArrayMappings, "(./address/city//text() | ./address/city-group//text() | ./address/country//text() | ./afdispname//text())");
			
			createArray("affilorg",affilorgArrayMappings, "(.//text())");
			
			createArray("afid",afidArrayMappings, "(@afid)");
			
			createSingleField("artnum",artnumArrayMappings);
			
			createArray("aucite", auciteArrayMappings, "(.//text())");
			
			createArray("authemail",authemailArrayMappings, "(.//text())");
			
			createArray("authfirstini",authfirstiniArrayMappings, "(.//text())");
			
			//createArray("authgrpid", authgrpidArrayMappings, "(./author/@auid | ./affiliation/@afid");
						
			createArray("authid",authidArrayMappings, "(@auid)");
			
			createArray("authidxname", authidxnameArrayMappings, "(.//text())");
			
			createArray("authkeywords", authkeywordsArrayMappings, "(.//text())");
			
			createArray("authlast", authlastArrayMappings, "(.//text())");
			
			createArray("authsuff", authsuffArrayMappings, "(.//text())");
			
			createArray("casregistrynum", casregistrynumArrayMappings, "(.//text())");
			
			createArray("chem", chemArrayMappings, "(.//text())");
			
			createArray("chemname", chemnameArrayMappings, "(.//text())");
			
			createSingleField("coden", codenMappings);
			
			createArray("collab", collabArrayMappings, "(.//text())");
			
			createSingleField("collecid", collecidMappings);
			
			createSingleField("confcode", confcodeMappings);
			
			createSingleField("confloc", conflocMappings);
			
			createSingleField("confname", confnameMappings);
			
			createArray("confsponsor", confsponsorArrayMappings, "(.//text())");

			createArray("copyright", copyrightArrayMappings, "(.//text())");
			
			createArray("corres", corresArrayMappings, "(.//text())");
					
			createSingleDateField("datecompletedtxt", datecompletedtxtMappings);
			
			createArrayDateField("dateloaded", dateloadedArrayMappings, "(.//text())");
			
			createArrayDateField("daterevised", daterevisedArrayMappings, "(.//text())");

			createSingleDateField("date-sort", datesortMappings);
			
			createArray("db", dbArrayMappings, "(.//text())");
			
			// dbdocid?????
			
			createSingleField("doi", doiMappings);
			
			createArray("dptid",dptidArrayMappings, "(@dptid)");
			
			createSingleField("dummycode", dummycodeMappings);
			
			createSingleField("dummylink", dummylinkMappings);
			
			createSingleField("edaddress", edaddressMappings);
			
			createArray("edfirstini", edfirstiniArrayMappings, "(.//text())");
			
			createArray("edidxname", edidxnameArrayMappings, "(.//text())");
			
			createSingleField("edition", editionMappings);
			
			createArray("edlast", edlastArrayMappings, "(.//text())");
			
			createSingleField("eid", eidMappings);
			
			createSingleField("eissn", eissnMappings);
			
			// exactkeyword - handle in schema.xml
			
			// exactsrctitle - handle in schema.xml
			
			createArray("firstauth", firstauthArrayMappings, "(.//text())");
			
			createArray("fundacr", fundacrArrayMappings, "(.//text())");
			
			createArray("fundno", fundnoArrayMappings, "(.//text())");
			
			createArray("fundsponsor", fundsponsorArrayMappings, "(.//text())");
			
			createSingleField("groupid", groupidMappings);
			
			createArray("idxterms", idxtermsArrayMappings, "(.//text())");
			
			createArray("idxtype", idxtypeArrayMappings, "(@type)");
			
			createArray("isbn", isbnArrayMappings, "(.//text())");
			
			createArray("issn", issnArrayMappings, "(.//text())");
			
			createArray("issnp", issnpArrayMappings, "(.//text())");
			
			createSingleField("issue", issueMappings);
			
			createArray("itemtitle", itemtitleArrayMappings, "(.//text())");
			
			createArray("lang", langArrayMappings, "(@xml:lang)");
			
			createArray("langitemtitle", langitemtitleArrayMappings, "(@xml:lang)");
			
			createArray("langreftitle", langreftitleArrayMappings, "(@xml:lang)");
			
			createArray("loadnum", loadnumArrayMappings, "(.//text())");
						
			createArray("manuf", manufArrayMappings, "(.//text())");
			
			// numcitedby
			fieldValues.put("numcitedby", "10");
			
			createSingleField("oeid", oeidMappings);
			
			createSingleField("pagecount", pagecountMappings);
			
			createSingleField("part", partMappings);
			
			createArray("patinfo", patinfoArrayMappings, "(.//text())");
			
			createSingleField("pgfirst", pgfirstMappings);
			
			createSingleField("pginfo", pginfoMappings);
			
			createSingleField("pglast", pglastMappings);
			
			createSingleField("pii", piiMappings);
			
			createSingleField("pmid", pmidMappings);
			
			//createSingleField("prefnameauid", prefnameauidMappings);
			
			createArray("pub", pubArrayMappings, "(.//text())");
			
			createSingleField("pubdatetxt", pubdatetxtMappings);
			
			createSingleField("pubyr", pubyrMappings);
			
			//ref
			
			createArray("refartnum", refartnumArrayMappings, "(.//text())");
			
			createArray("refauid", refauidArrayMappings, "(@auid)");
			
			createSingleField("refcount", refcountMappings);
			
			createArray("refeid", refeidArrayMappings, "(.//text())");
			
			createArray("refpg", refpgArrayMappings, "(.//text())");
			
			createArray("refpgfirst", refpgfirstArrayMappings, "(@first)");
			
			createArray("refpubyr", refpubyrArrayMappings, "(@first)");

			createArray("refscp", refscpArrayMappings, "(.//text())");
			
			createArray("refsrctitle", refsrctitleArrayMappings, "(.//text())");
			
			createArray("reftitle", reftitleArrayMappings, "(.//text())");
			
			createArray("relauthid", relauthidArrayMappings, "(@auid)");
			
			createSingleField("restrictedaccess", restrictedaccessMappings);
			
			createSingleField("sdeid", sdeidMappings);
			
			createArray("seqbank", seqbankArrayMappings, "(@name)");
			
			createArray("seqnumber", seqnumberArrayMappings, "(.//text())");
			
			createSingleField("srcid", srcidMappings);
			
			createSingleField("srctitle", srctitleMappings);
			
			createSingleField("srctitleabbr", srctitleabbrMappings);
			
			createSingleField("srctype", srctypeMappings);
			
			createArray("subjabbr", subjabbrArrayMappings, "(.//text())");
			
			createArray("subjmain", subjmainArrayMappings, "(.//text())");
			
			createArray("subjterms", subjtermsArrayMappings, "(.//text())");
			
			createArray("subjtype", subjtypeArrayMappings, "(@type)");
			
			createSingleField("subtype", subtypeMappings);
			
			createSingleField("supplement", supplementMappings);
			
			createArray("tradenames", tradenamesArrayMappings, "(.//text())");
			
			createSingleField("vol", volMappings);
			
			createSingleField("website", websiteMappings);
			
			
			
			
			
				
			
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
	 * It is not possible for duplicate values (nodes) to occur in a single field.
     * 
	 * @param fieldName  field name
	 * @param xpathExpression XPath to apply
	 * @throws XPathExpressionException
	 */
	public void createSingleDateField(String fieldName, String xpathExpression) throws XPathExpressionException {
		
		// If we have already resolved this XPath expression, use the cached value to create the field and return
		if (cachedFieldValues.containsKey(xpathExpression)) {
			fieldValues.put(fieldName, cachedFieldValues.get(xpathExpression));
			return;
		}
		
		NodeList nodes = (NodeList)xpath.evaluate(xpathExpression, doc, XPathConstants.NODESET);
		
		// If it doesn't exist or more than one node exists, cache an empty results and return
		if (nodes.getLength() == 0 || nodes.getLength() > 1) {
			cachedFieldValues.put(xpathExpression, "");
			return;
		}
		
		String wrkStr = nodes.item(0).getNodeValue().trim();
			
		// Create the field and cache the results
		if (wrkStr.length()  > 0) {
			wrkStr = wrkStr.substring(0, wrkStr.lastIndexOf('.')) + "Z";
			fieldValues.put(fieldName, wrkStr);
			cachedFieldValues.put(xpathExpression, wrkStr);
		} else {
			cachedFieldValues.put(xpathExpression, "");
		}

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
	public void createArrayDateField(String fieldName, String[] xpathExpressions, String xpathExpression) throws XPathExpressionException {
		
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
					String wrkStr = sb.toString();
					wrkStr = wrkStr.substring(0, wrkStr.lastIndexOf('.')) + "Z";
					values.add(wrkStr);
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
	public void createArrayFromAttributes(String fieldName, String[] xpathExpressions) throws XPathExpressionException {
		
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
				sb.append(nodes.item(j).getNodeValue().trim());				
				if (j < nodes.getLength() - 1) sb.append(" ");
			    
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
	
	// In Solr, you can't sort on a multivalued field. In many cases for Scopus, fields that are
	// defined as multivalued are also listed as sortable. In order to get around this issues for 
	// these cases, we want to create a new single value field from the various multi-values present
	// in the field mapping. To do this, we make sure the target multivalue field exists, and if it 
	// does we take the first entry from that field to use as the value for the single value sort field.
	public void createSortFieldFromArrayField(String fieldName, String sourceArrayField) { 
		
		// Is there data in sourceArrayField to create the sort field from
		if (fieldValues.containsKey(sourceArrayField) == false) {
			// Nothing to do, just return without creating an empty field.
			return;
		}
		Object values = fieldValues.get(sourceArrayField);
		if (values instanceof ArrayList<?>) {
			fieldValues.put(fieldName, ((ArrayList<String>)values).get(0));
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
