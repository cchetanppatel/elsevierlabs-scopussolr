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
public class ScopusNamespaceContext implements NamespaceContext {

	/**
	 * Provide namespace prefix to namespace uri resolution capability.  All of the namespaces
	 * declared in the Scopus records must be defined.
	 * 
	 * @param prefix Namespace prefix
	 * @return Namespace uri
	 */
    public String getNamespaceURI(String prefix) {
        if (prefix == null) throw new NullPointerException("Null prefix");
        else if ("xocs".equals(prefix)) return "http://www.elsevier.com/xml/xocs/dtd";
        else if ("ce".equals(prefix)) return "http://www.elsevier.com/xml/ani/common";
        else if ("ait".equals(prefix)) return "http://www.elsevier.com/xml/ani/ait";
        else if ("cto".equals(prefix)) return "http://www.elsevier.com/xml/cto/dtd";
        else if ("xsi".equals(prefix)) return "http://www.w3.org/2001/XMLSchema-instance";  
        else if ("fn".equals(prefix)) return "http://www.w3.org/2001/XMLSchema-instance"; 
        else if ("xml".equals(prefix)) return "http://www.w3.org/2005/xpath-functions";
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
