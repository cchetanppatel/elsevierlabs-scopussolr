package com.elsevier.transform;

import java.util.Iterator;
import javax.xml.*;
import javax.xml.namespace.NamespaceContext;

/**
 * Namespace prefix to Namespace uri mappings.  Required when using Java XPath and the
 * XML to be processed contains namespaces.
 * 
 * @author Darin McBeath
 *
 */
public class ScienceDirectNamespaceContext implements NamespaceContext {

	/**
	 * Provide namespace prefix to namespace uri resolution capability.  All of the namespaces
	 * declared in the ScienceDirect records must be defined.
	 * 
	 * @param prefix Namespace prefix
	 * @return Namespace uri
	 */
    public String getNamespaceURI(String prefix) {
        if (prefix == null) throw new NullPointerException("Null prefix");
        else if ("xocs".equals(prefix)) return "http://www.elsevier.com/xml/xocs/dtd";
        else if ("ja".equals(prefix)) return "http://www.elsevier.com/xml/ja/dtd";
        else if ("si".equals(prefix)) return "http://www.elsevier.com/xml/si/dtd";
        else if ("ehs".equals(prefix)) return "http://www.elsevier.com/xml/ehs-book/dtd";
        else if ("bk".equals(prefix)) return "http://www.elsevier.com/xml/bk/dtd";
        else if ("ce".equals(prefix)) return "http://www.elsevier.com/xml/common/dtd";
        else if ("sb".equals(prefix)) return "http://www.elsevier.com/xml/common/struct-bib/dtd";
        else if ("tb".equals(prefix)) return "http://www.elsevier.com/xml/common/table/dtd";
        else if ("xlink".equals(prefix)) return "http://www.w3.org/1999/xlink";
        else if ("mml".equals(prefix)) return "http://www.w3.org/1998/Math/MathML";
        else if ("cals".equals(prefix)) return "http://www.elsevier.com/xml/common/cals/dtd";
        else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
        else if ("fn".equals(prefix)) return "http://www.w3.org/2005/xpath-functions";
        return XMLConstants.NULL_NS_URI;
    }

    // This method isn't necessary for XPath processing.
    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    // This method isn't necessary for XPath processing either.
    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }

}
