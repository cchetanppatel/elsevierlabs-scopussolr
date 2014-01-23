<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:ft="http://www.elsevier.com/2003/01/xqueryxFT-schema"
                xmlns:ns2="http://webservices.elsevier.com/schemas/search/fast/types/v4">
      
    <!-- Turn off auto-insertion of <?xml> tag and set indenting on -->
    <xsl:output method="text" encoding="utf-8" indent="no" omit-xml-declaration="yes"/>
    
    <!-- strip whitespace from whitespace-only nodes -->
    <xsl:strip-space elements="*"/>
     
    <xsl:variable name="paramNames" select="//ns2:viaParamsList/ns2:paramName"/> 
    <xsl:variable name="lengthParamNames" select="count($paramNames)"/>
    <xsl:variable name="paramValues" select="//ns2:viaParamsList/ns2:paramValue"/> 
    <xsl:variable name="lengthParamValues" select="count($paramValues)"/>    
    <xsl:template match="text()|@*"/>

	<!--  resolvetombstone -->
	
	
	<xsl:template match="ns2:viaParamsList">
		<xsl:text>paramNames length is </xsl:text><xsl:value-of select="$lengthParamNames"/><xsl:text>. </xsl:text>
		<xsl:text>paramValues length is </xsl:text><xsl:value-of select="$lengthParamValues"/><xsl:text>. </xsl:text>
		 <xsl:for-each select="$paramNames">
    		<xsl:if test="./text() = 'resolvetombstone'">
    			<xsl:variable name="tspos" select="position()"/>
      			<xsl:text>Position is </xsl:text><xsl:value-of select="$tspos"/><xsl:text>. </xsl:text>
      			<xsl:text>Tombstone is </xsl:text><xsl:value-of select="$paramValues[$tspos]"/><xsl:text>. </xsl:text>
    		</xsl:if>
  		</xsl:for-each>
	</xsl:template>
	
</xsl:stylesheet>

