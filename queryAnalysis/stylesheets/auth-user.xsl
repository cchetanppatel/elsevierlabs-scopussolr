<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:ft="http://www.elsevier.com/2003/01/xqueryxFT-schema"
                xmlns:ns2="http://webservices.elsevier.com/schemas/search/fast/types/v4">
      
    <!-- Turn off auto-insertion of <?xml> tag and set indenting on -->
    <xsl:output method="text" encoding="utf-8" indent="no" omit-xml-declaration="yes"/>
    
    <!-- strip whitespace from whitespace-only nodes -->
    <xsl:strip-space elements="*"/>
       
    <xsl:template match="text()|@*"/>
	
	<xsl:template match="/ns2:search/ns2:searchReqPayload[ns2:xQueryX/ft:fullTextQuery[not((.//ft:word[@path='authid'] and not(.//ft:word[@path!='authid']))
	                                       or (.//ft:word[@path=('authid')] and .//ft:word[@path=('afid')] and (count(distinct-values(.//ft:word/@path)) = 2)))
	                                       and exists(..//ft:word)]]">
	                                       
		<xsl:text>FOUND ONE</xsl:text>

	</xsl:template>
	
</xsl:stylesheet>

