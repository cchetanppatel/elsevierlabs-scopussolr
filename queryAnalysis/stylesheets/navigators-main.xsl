<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:ft="http://www.elsevier.com/2003/01/xqueryxFT-schema"
                xmlns:ns2="http://webservices.elsevier.com/schemas/search/fast/types/v4">
      
    <!-- Turn off auto-insertion of <?xml> tag and set indenting on -->
    <xsl:output method="text" encoding="utf-8" indent="no" omit-xml-declaration="yes"/>
    
    <!-- strip whitespace from whitespace-only nodes -->
    <xsl:strip-space elements="*"/>
       
    <xsl:template match="text()|@*"/>

	<xsl:template match="ns2:viaParamsList/ns2:paramValue[starts-with(.,'sco') and not(. ='scomode')]">
		<xsl:for-each select=".">
			<xsl:value-of select="./text()"/>
			<xsl:text></xsl:text>
		</xsl:for-each>
	</xsl:template>
	
</xsl:stylesheet>

