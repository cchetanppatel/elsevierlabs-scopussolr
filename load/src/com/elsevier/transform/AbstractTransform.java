package com.elsevier.transform;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
	
	private static Calendar now = Calendar.getInstance();
	private static SimpleDateFormat fastloaddatefmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private static int DEFAULT_JSON_FIELD_STRINGBUILDER_SIZE = 1024;
	
	// Basic field mappings	
	private static String[] absArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/abstracts/abstract"
	};
	
	private static String[] absMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/abstracts/abstract//text()"
	};
	
	private static String[] abslangArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-info/abstract-language"
	};
	
	private static String[] affilArrayMappings =  new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation"
	};
	
	private static String[] affilcityArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/city-group",
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/city"
	};
	
	private static String[] affilcityMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/city-group//text()",
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/city//text()"
	};
	
	private static String[] affilctryArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation"
	};
	
	private static String[] affilctryMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/@country"
	};
	
	private static String[] affilorgArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/organization",
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/ce:text"
	};
	
	private static String[] affilorgMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/organization//text()",
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/ce:text//text()"
	};
	
	private static String[] afhistidArrayMappings = new String[] {
		"/xocs:doc/xocs:author-profile/author-profile/affiliation-history/affiliation/ip-doc[@type='parent']"
	};

	private static String[] afidArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation"
	};	

	private static String[] artnumMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/article-number//text()"
	};	
	
	private static String[] authArrayMappings =  new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author"
	};
	
	private static String[] auciteArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:surname",
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:initials"
	};	
	
	private static String[] authemailArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:e-address"
	};	
	
	private static String[] authemailMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:e-address//text()"
	};
	
	private static String[] authfirstiniArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:initials"
	};	
	
	private static String[] authfirstiniMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:initials//text()"
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
	
	private static String[] authkeywordsMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-info/author-keywords/author-keyword//text()"
	};
	
	private static String[] authlastArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:surname"
	};	
	
	private static String[] authlastMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:surname//text()"
	};
	
	private static String[] authsuffArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:suffix"
	};	
	
	private static String[] authsuffMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:suffix//text()"
	};
	
	private static String[] casregistrynumArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/chemicalgroup/chemicals/chemical/cas-registry-number"
	};	
	
	private static String[] chemArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/chemicalgroup/chemicals/chemical"
	};
	
	private static String[] chemMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/chemicalgroup/chemicals/chemical//text()"
	};
	
	private static String[] chemnameArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/chemicalgroup/chemicals/chemical/chemical-name"
	};
	
	private static String[] chemnameMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/chemicalgroup/chemicals/chemical/chemical-name//text()"
	};
	
	private static String[] codenMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/codencode//text()"
	};	
	
	private static String[] collabArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/collaboration"
	};
	
	private static String[] collabMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/collaboration//text()"
	};
	
	private static String[] collecidMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/itemidlist/itemid[@idtype='SCP']//text()"
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
	
	private static String[] confsponsorMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/additional-srcinfo/conferenceinfo/confevent/confsponsors/confsponsor//text()"
	};
	
	private static String[] copyrightArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/copyright"
	};
	
	private static String[] corresArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/correspondence"
	};
	
	private static String[] corresMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/correspondence//text()"
	};
	
	private static String datecompletedtxtMappings = "/xocs:doc/xocs:item/item/ait:process-info/ait:date-delivered/@timestamp";
	
	private static String[] dateloadedArrayMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:timestamp"
	};
	
	private static String[] daterevisedArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/history/date-revised"
	};
	
	//private static String datesortMappings = "/xocs:doc/xocs:item/item/ait:process-info/ait:date-sort//text()";
	
	private static String[] datesortYearMappings = new String[] {
		"/xocs:doc/xocs:item/item/ait:process-info/ait:date-sort/@year"
	};
	private static String[] datesortMonthMappings = new String[] {
		"/xocs:doc/xocs:item/item/ait:process-info/ait:date-sort/@month"
	};
	private static String[] datesortDayMappings = new String[] {
		"/xocs:doc/xocs:item/item/ait:process-info/ait:date-sort/@day"
	};

	private static String[] dbArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/dbcollection"
	};
	
	private static String[] dbdocidArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/itemidlist/itemid"	
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
	
	private static String[] edfirstiniMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/editors/editor/ce:initials//text()",
		"/xocs:doc/xocs:item/item/bibrecord/head/source/additional-srcinfo/conferenceinfo/confpublication/confeditors/editors/editor/ce:initials//text()",
		"/xocs:doc/xocs:item/item/bibrecord/head/source/contributor-group/contributor[@role='edit']/ce:initials//text()"
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
	
	private static String[] edlastMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/editors/editor/ce:surname//text()",
		"/xocs:doc/xocs:item/item/bibrecord/head/source/additional-srcinfo/conferenceinfo/confpublication/confeditors/editors/editor/ce:surname//text()",
		"/xocs:doc/xocs:item/item/bibrecord/head/source/contributor-group/contributor[@role='edit']/ce:surname//text()"
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
	
	private static String[] fundallArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/grantlist/grant"
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
	
	private static String[] idxtermsMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/descriptorgroup/descriptors/descriptor/mainterm//text()"
	};
	
	private static String[] idxtypeArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/descriptorgroup/descriptors"
	};
	
	private static String[] isbnArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/isbn"
	};
	
	private static String[] isbnMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/isbn//text()"
	};
	
	private static String[] issnArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/issn"
	};
	
	private static String[] issnMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/issn//text()"
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
	
	private static String[] itemtitleMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-title/titletext//text()"
	};

	
	private static String[] langArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-info/citation-language"
	};
	
	private static String[] langMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-info/citation-language/@xml:lang"
	};
	
	private static String[] langitemtitleArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-title/titletext"
	};
	
	private static String[] langreftitleArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/citation-title/titletext"
	};
	
	private static String[] langreftitleMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/citation-title/titletext//text()"
	};
	
	private static String[] loadnumArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/itemidlist/itemid[@idtype='PUI']"
	};
	
	
	private static String[] manufArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/manufacturergroup/manufacturers/manufacturer"
	};
	
	private static String[] manufMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/manufacturergroup/manufacturers/manufacturer//text()"
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
	
	private static String[] prefnameauidArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author", 
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author",
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author"
	};
	
	private static String[] pubArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/publisher"	
	};
	
	private static String[] pubMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/publisher//text()"	
	};
	
	private static String pubdatetxtMappings = "/xocs:doc/xocs:item/item/bibrecord/head/source/publicationdate/date-text//text()";
	
	private static String[] pubyrMappings = new String[] {
		"/xocs:doc/xocs:item/item/ait:process-info/ait:date-sort/@year"
	};
	
	private static String[] refArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference"	
	};
	
	private static String[] refartnumArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/article-number"	
	};
	
	private static String[] refartnumMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/article-number//text()"	
	};
	
	private static String[] refauidArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/author-group/author"	
	};
	
	private static String[] refauidMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/author-group/author/@auid"	
	};

	
	private static String[] refcountMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/@refcount"	
	};
	
	// Original CIP mappings not present in docs, pulling different values that are in the doc
	private static String[] refeidArrayMappings = new String[] {
		//"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/eid",
		//"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/oeid"
		"/xocs:doc/xocs:meta/cto:ref-id"
	};
	
	// Original CIP mappings not present in docs, pulling different values that are in the doc
	private static String[] refeidMappings = new String[] {
		//"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/eid//text()",
		//"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/oeid//text()"
		"/xocs:doc/xocs:meta/cto:ref-id//text()"
	};
	
	private static String[] refpgArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/volisspag/pages",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-volisspag/pages"
	};
	
	private static String[] refpgMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/volisspag/pages//text()",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-volisspag/pages//text()"
	};
	
	private static String[] refpgfirstArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/volisspag/pagerange",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-volisspag/pagerange"
	};
	
	private static String[] refpgfirstMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/volisspag/pagerange/@first",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-volisspag/pagerange/@first"
	};
	
	private static String[] refpubyrArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/publicationyear",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-publicationyear"
	};
	
	private static String[] refpubyrMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/publicationyear//@first",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-publicationyear//@first"
	};
	
	private static String[] refscpArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/refd-itemidlist/itemid[@idtype='SGR']"
	};
	
	private static String[] refscpMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/refd-itemidlist/itemid[@idtype='SGR']//text()"
	};
	
	private static String[] refsrctitleArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/sourcetitle",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-sourcetitle"
	};
	
	private static String[] refsrctitleMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/sourcetitle//text()",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-sourcetitle//text()"
	};
	
	private static String[] reftitleArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/citation-title/titletext",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-title/ref-titletext"
	};
	
	private static String[] reftitleMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/citation-title/titletext//text()",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-title/ref-titletext//text()"
	};
	
	private static String[] relauthidArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/author-group/author"
	};

	private static String[] restrictedaccessMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/citation-info/dummy-link/@restricted-access"	
	};
	
	private static String[] scpidMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/item-info/itemidlist/itemid[@idtype='SCP']//text()"	
	};
	
	private static String[] sdeidMappings = new String[] {
		"/xocs:doc/xocs:meta/xocs:fulltext-eid//text()"
	};	
	
	private static String[] seqbankArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/sequencebanks/sequencebank"
	};
	
	private static String[] seqbankMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/sequencebanks/sequencebank/@name"
	};
	
	private static String[] seqnumberArrayMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/sequencebanks/sequencebank/sequence-number"
	};
	
	private static String[] seqnumberMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/sequencebanks/sequencebank/sequence-number//text()"
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
	
	private static String[] statustypeMappings = new String[] {
		"/xocs:doc/xocs:item/item/ait:process-info/ait:status/@type"
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
	
	private static String[] tradenamesMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/enhancement/ tradenamegroup/tradenames/trademanuitem/tradename//text()"
	};
	
	private static String[] volMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/head/source/volisspag/voliss/@volume"
	};
	
	private static String[] websiteMappings = new String[] {
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/refd-itemcitation/website//text()",
		"/xocs:doc/xocs:item/item/bibrecord/tail/bibliography/reference/ref-info/ref-website//text()"
	};
	
	
	//
	// Compound Field mappings
	// Note: These fields must be generated after the single fields are generated to take advantage of the cached values to speed processing
	//
	
	private static String[] affilMappings =  ArrayConcat.concatAll(affilcityMappings, affilctryMappings, affilorgMappings);
	private static String[] authMappings =  ArrayConcat.concatAll(authlastMappings, authfirstiniMappings, authemailMappings, authsuffMappings);
	private static String[] confallMappings =  ArrayConcat.concatAll(confnameMappings, confsponsorMappings, conflocMappings, confcodeMappings);
	private static String[] edMappings =  ArrayConcat.concatAll(edlastMappings, edfirstiniMappings);
	private static String[] keywordsMappings =  ArrayConcat.concatAll(authkeywordsMappings, idxtermsMappings, tradenamesMappings, chemnameMappings);
	private static String[] keywordsArrayMappings =  ArrayConcat.concatAll(authkeywordsArrayMappings, idxtermsArrayMappings, tradenamesArrayMappings, chemnameArrayMappings);
	private static String[] pgMappings =  ArrayConcat.concatAll(pgfirstMappings, pglastMappings, pginfoMappings);
	private static String[] refMappings =  ArrayConcat.concatAll(refeidMappings, reftitleMappings, langreftitleMappings,refsrctitleMappings, refpubyrMappings, refpgMappings, refpgfirstMappings, refartnumMappings,refscpMappings, refauidMappings, websiteMappings  );
	
	private static String[] allsmallMappings =  ArrayConcat.concatAll(itemtitleMappings, absMappings, keywordsMappings);
	private static String[] allmedMappings =  ArrayConcat.concatAll(itemtitleMappings, authMappings, absMappings, keywordsMappings);
	private static String[] allMappings =  ArrayConcat.concatAll(absMappings, affilMappings, artnumMappings, authMappings, chemMappings, codenMappings, collabMappings, confallMappings, corresMappings, doiMappings, edMappings, isbnMappings, issnMappings, issueMappings, itemtitleMappings, keywordsMappings, langMappings, manufMappings, pubMappings, pubyrMappings, refMappings, seqbankMappings, seqnumberMappings, srctitleMappings, srctitleabbrMappings, volMappings, websiteMappings);

	
	
	
	
	/**
	 * Transform the record.  The inputstream contains the ScienceDirect record that will be transformed.
	 * The returned HashMap object will contain fields (and their values) that will be used to
	 * create a JSON document that will be inserted into ElasticSearch.
	 * 
	 * @param is
	 * @return fieldValues;
	 * @throws XPathExpressionException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	
	public HashMap<String,Object> transform(InputStream is) throws Exception {
		
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
			
			// Somehow FAST decides absavail... We will fake out the value by seeing if abs was populated in the previous line.
			if (fieldValues.get("abs") != null) {
				fieldValues.put("absavail", "1");
			} else {
				fieldValues.put("absavail", "0");
			}
						
			createArray("abslang",abslangArrayMappings, "(xml:lang)");
						
			createArray("affilcity",affilcityArrayMappings, "(.//text())");
			
			// Note we need to merge in country names instead of codes
			createArray("affilctry", affilctryArrayMappings, "(@country)");
			
			convertCountryCodesToNames("affilctry");
			
			createSortFieldFromArrayField("affilctry-s", "affilctry");
					
			createArray("affilorg",affilorgArrayMappings, "(.//text())");
			
			createArray("afid",afidArrayMappings, "(@afid)");
			
			createSingleField("artnum",artnumMappings);
			
			createArray("aucite", auciteArrayMappings, "(.//text())");
			createSortFieldFromArrayField("aucite-s", "aucite");

			
			createArray("authemail",authemailArrayMappings, "(.//text())");
			
			createArray("authfirstini",authfirstiniArrayMappings, "(.//text())");
			
			//createArray("authgrpid", authgrpidArrayMappings, "(./author/@auid | ./affiliation/@afid)");
			createArray("authgrpid", authgrpidArrayMappings, "((./author/@auid) | (./affiliation/@afid))");			 
			
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
			
			// We don't have the fast generated value - putting dummy value.
			fieldValues.put("collec", "SCOPUS");
						
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

			// Note: We need to put the datesort field together since it exists as discrete tag attributes.
			createSingleField("datesortyear", datesortYearMappings);
			createSingleField("datesortmonth", datesortMonthMappings);
			createSingleField("datesortday", datesortDayMappings);
			
			String datesortyear = (String) fieldValues.get("datesortyear");
			String datesortmonth = (String) fieldValues.get("datesortmonth");
			String datesortday = (String) fieldValues.get("datesortday");
			if (datesortyear != null && datesortmonth != null && datesortday != null) {
				fieldValues.put("datesort", datesortyear + datesortmonth + datesortday);
			}
			fieldValues.remove("datesortyear");
			fieldValues.remove("datesortmonth");
			fieldValues.remove("datesortday");
			
			createArray("db", dbArrayMappings, "(.//text())");
			
			createArray("dbdocid", dbdocidArrayMappings, "(@idtype)");
			
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
			
			// Note!!! Per Darin's email, fastloaddate is really yhe epoch.  We don't have that value available to us here,
			// but can add it into the values much like we do the epoch in the SolrCore controller for the Transform process.
			
			// We don't have the fast generated value - putting dummy value. Based on our actual load processing timestamp
			//String fastdatestr =  fastloaddatefmt.format(now.getTime());
			//fieldValues.put("fastloaddate", fastdatestr);
						
			createArray("firstauth", firstauthArrayMappings, "(.//text())");
			
			createArray("fundacr", fundacrArrayMappings, "(.//text())");
			
			createArray("fundall", fundallArrayMappings, "./grantid//text() | ./grant-acronym//text() | ./grant-agency//text()");
			
			createArray("fundno", fundnoArrayMappings, "(.//text())");
			
			createArray("fundsponsor", fundsponsorArrayMappings, "(.//text())");
			
			createSingleField("groupid", groupidMappings);
			
			createArray("idxterms", idxtermsArrayMappings, "(.//text())");
			
			createArray("idxtype", idxtypeArrayMappings, "(@type)");
			
			// Create an inteid field from the eid field suffix
			Object val = fieldValues.get("eid");
			if (val instanceof String) {
				String intid = (String)val;
				intid = intid.substring(intid.lastIndexOf("-") + 1);
				fieldValues.put("intid", intid);
			}
			
			createArray("isbn", isbnArrayMappings, "(.//text())");
			
			createArray("issn", issnArrayMappings, "(.//text())");
			
			createArray("issnp", issnpArrayMappings, "(.//text())");
			
			createSingleField("issue", issueMappings);
			
			createArray("itemtitle", itemtitleArrayMappings, "(.//text())");
			createSortFieldFromArrayField("itemtitle-s", "itemtitle");
			
			createArray("lang", langArrayMappings, "(@xml:lang)");
			
			createArray("langitemtitle", langitemtitleArrayMappings, "(@xml:lang)");
			
			createArray("langreftitle", langreftitleArrayMappings, "(@xml:lang)");
			
			createArray("loadnum", loadnumArrayMappings, "(.//text())");
			
			// We don't have the fast generated value - putting dummy value.
			fieldValues.put("loadunit", "ABCDEFGHIJ0123456789");
						
			createArray("manuf", manufArrayMappings, "(.//text())");
			
			// Hard coding these values for now.
			// Commented out because this in now populated by Redshift job and we don't want to overwrite good values on record updates.
			//fieldValues.put("numcitedby", "10");
			fieldValues.put("numpatcites", "5");
			fieldValues.put("numwebcites", "5");
			
			createSingleField("oeid", oeidMappings);
			
			createSingleField("pagecount", pagecountMappings);
			
			createSingleField("part", partMappings);
			
			createArray("patinfo", patinfoArrayMappings, "(.//text())");
			
			createSingleField("pgfirst", pgfirstMappings);
			
			createSingleField("pginfo", pginfoMappings);
			
			createSingleField("pglast", pglastMappings);
			
			createSingleField("pii", piiMappings);
			
			createSingleField("pmid", pmidMappings);
			
			createArray("prefnameauid", prefnameauidArrayMappings, "(./preferred-name/ce:surname//text() | ./preferred-name/ce:initials//text() | @auid)");
			
			createArray("pub", pubArrayMappings, "(.//text())");
			
			createSingleField("pubdatetxt", pubdatetxtMappings);
			
			createSingleField("pubyr", pubyrMappings);
						
			createArray("refartnum", refartnumArrayMappings, "(.//text())");
			
			createArray("refauid", refauidArrayMappings, "(@auid)");
			
			createSingleField("refcount", refcountMappings);
			
			createArray("refeid", refeidArrayMappings, "(.//text())");
			// Need to transform the values we are using in the ref-eid from short eids to full eids so our sample queries work
			convertRefEids("refeid");
			
			createArray("refpg", refpgArrayMappings, "(.//text())");
			
			createArray("refpgfirst", refpgfirstArrayMappings, "(@first)");
			
			createArray("refpubyr", refpubyrArrayMappings, "(@first)");

			createArray("refscp", refscpArrayMappings, "(.//text())");
			
			createArray("refsrctitle", refsrctitleArrayMappings, "(.//text())");
			
			createArray("reftitle", reftitleArrayMappings, "(.//text())");
			
			createArray("relauthid", relauthidArrayMappings, "(@auid)");
			
			createSingleField("restrictedaccess", restrictedaccessMappings);
			
			createSingleField("scpid", scpidMappings);
			
			createSingleField("sdeid", sdeidMappings);
			
			// Somehow FAST decides sdfullavail... We will fake out the value by seeing if sdeid was populated in the previous line.
			if (fieldValues.get("sdeid") != null) {
				fieldValues.put("sdfullavail", "1");
			} else {
				fieldValues.put("sdfullavail", "0");
			}
			
			createArray("seqbank", seqbankArrayMappings, "(@name)");
			
			createArray("seqnumber", seqnumberArrayMappings, "(.//text())");
			
			createSingleField("srcid", srcidMappings);
			
			createSingleField("srctitle", srctitleMappings);
			
			createSingleField("srctitleabbr", srctitleabbrMappings);
			
			createSingleField("srctype", srctypeMappings);
			
			createSingleField("statustype", statustypeMappings);
			
			//createArray("subjabbr", subjabbrArrayMappings, "(.//text())");
			
			createArray("subjmain", subjmainArrayMappings, "(.//text())");
			
			// Note: our data doesn't have merged in subject abbreviations so we'll manually match them up and 
			// put them in the index.
			convertSubjectCodesToAbbrs("subjmain", "subjabbr");
			
			createArray("subjterms", subjtermsArrayMappings, "(.//text())");
			
			createArray("subjtype", subjtypeArrayMappings, "(@type)");
			
			createSingleField("subtype", subtypeMappings);
			
			createSingleField("supplement", supplementMappings);
			
			createArray("tradenames", tradenamesArrayMappings, "(.//text())");
			
			// We don't have the fast generated value - putting dummy value.
			fieldValues.put("transid", "ABCDEFGHIJ0123456789");
						
			createSingleField("vol", volMappings);
			
			createSingleField("website", websiteMappings);
			
			
			// Create composite, single value fields
			// Note: need to build those composite fields referenced in other composite fields first
			
			createArray("affil", affilArrayMappings, "./city-group//text() | ./city//text() | ./country//text() | ./organization//text() | ./ce:text//text()");
			createArray("auth", authArrayMappings, "./ce:e-address//text() | ./ce:initials//text() | ./ce:surname//text() | ./ce:suffix//text()");
			createSortFieldFromArrayField("auth-s", "auth");
			createCompositeSingleField("confall", confallMappings);
			createCompositeSingleField("ed", edMappings);
			createArray("keywords", keywordsArrayMappings, "(.//text())");
			createCompositeSingleField("pg", pgMappings);
			createArray("ref", refArrayMappings, "  ./refd-itemcitation/article-number//text() | ./refd-itemcitation/author-group/author/@auid | ./refd-itemcitation/eid//text() " +
					                             "| ./refd-itemcitation/oeid//text() | ./refd-itemcitation/volisspag/pages//text() | ./ref-info/ref-volisspag/pages//text() " +
					                             "| ./refd-itemcitation/volisspag/pagerange/@first | ./ref-info/ref-volisspag/pagerange/@first | ./refd-itemcitation/publicationyear/@first " +
					                             "| ./ref-info/ref-publicationyear/@first | ./ref-info/refd-itemidlist/itemid[@idtype='SGR']//text() | ./refd-itemcitation/sourcetitle//text() " + 
					                             "| ./ref-info/ref-sourcetitle//text() | ./refd-itemcitation/citation-title/titletxt//text() | ./ref-info/ref-title/ref-titletext//text() " +
					                             "| ./refd-itemcitation/author-group/author/ce:initials//text() | ./ref-info/ref-authors/author/ce:initials//text() " +
					                             "| ./refd-itemcitation/author-group/author/ce:surname//text() | ./ref-info/ref-authors/author/ce:surname//text() "
					    );
			createCompositeSingleField("allsmall", allsmallMappings);
			createCompositeSingleField("allmed", allmedMappings);
			createCompositeSingleField("all", allMappings);	
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw e;
		} catch (SAXException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			throw e;
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
	
	private void convertCountryCodesToNames(String fieldValueKey) {
		
		// Change countrycodes into country names. Note, the value returned can be either a single string
		// value or an arrayList of Strings.  Have to take this into account during processing.
		Object val = fieldValues.get(fieldValueKey);
		if (val instanceof String) {
			String ctryName = CountryCodeMap.codes.get(val);
			if (ctryName != null)
				fieldValues.put(fieldValueKey, ctryName);
		} else if (val instanceof ArrayList<?>){
			ArrayList<String> oldvals = (ArrayList<String>)val;
			ArrayList<String> newvals = new ArrayList<String>();
			Iterator<String> it = oldvals.iterator();
			while (it.hasNext()) {
				String ctryCode = it.next();
				String ctryName = CountryCodeMap.codes.get(ctryCode);
				if (ctryName != null)
					newvals.add(ctryName);
				else
					newvals.add(ctryCode);
			}
			fieldValues.put(fieldValueKey, newvals);
		} else {
			// just leave things alone..  Shouldn't happen unless there aren't any ctrycodes
		}
	}
	
	private void convertSubjectCodesToAbbrs(String fieldValueKey, String newFieldValueKey) {
		
		// Change subject codes into subject abreviations. Note, the value returned can be either a single string
		// value or an arrayList of Strings.  Have to take this into account during processing.
		Object val = fieldValues.get(fieldValueKey);
		if (val instanceof String) {
			String subjAbbr = SubjectAbbrMap.codes.get(val);
			if (subjAbbr != null)
				fieldValues.put(newFieldValueKey, subjAbbr);
		} else if (val instanceof ArrayList<?>){
			ArrayList<String> oldvals = (ArrayList<String>)val;
			ArrayList<String> newvals = new ArrayList<String>();
			Iterator<String> it = oldvals.iterator();
			while (it.hasNext()) {
				String subjCode = it.next();
				String subjAbbr = SubjectAbbrMap.codes.get(subjCode);
				if (subjAbbr != null)
					newvals.add(subjAbbr);
				else
					newvals.add(subjCode);
			}
			fieldValues.put(newFieldValueKey, newvals);
		} else {
			// just leave things alone..  Shouldn't happen unless there aren't any subject codes
		}
	}
	
	private void convertRefEids(String fieldValueKey) {
		
		// Change short refeids into full length eids.
		Object val = fieldValues.get(fieldValueKey);
		String prefix = "2-s2.0-";
		if (val instanceof String) {
			String newEid = prefix + (String)val;
			fieldValues.put(fieldValueKey, newEid);
		} else if (val instanceof ArrayList<?>){
			ArrayList<String> oldvals = (ArrayList<String>)val;
			ArrayList<String> newvals = new ArrayList<String>();
			Iterator<String> it = oldvals.iterator();
			while (it.hasNext()) {
				String oldeid = it.next();
				String neweid = prefix + oldeid;
				newvals.add(neweid);
			}
			fieldValues.put(fieldValueKey, newvals);
		} else {
			// just leave things alone..  Shouldn't happen unless there aren't any refeids
		}
	}
}
