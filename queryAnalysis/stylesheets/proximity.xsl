<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:ft="http://www.elsevier.com/2003/01/xqueryxFT-schema">
      
    <!-- Turn off auto-insertion of <?xml> tag and set indenting on -->
    <xsl:output method="text" encoding="utf-8" indent="no" omit-xml-declaration="yes"/>
    
    <!-- strip whitespace from whitespace-only nodes -->
    <xsl:strip-space elements="*"/>
       
    <xsl:template match="text()|@*"/>
	
	<xsl:template match="ft:fullTextQuery/ft:query">
		<xsl:apply-templates select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery">
			<xsl:with-param name="p">0</xsl:with-param> 
		</xsl:apply-templates>	
	</xsl:template>

	<!--  Needed for some different structured queries in Scopus (missing ft:query element) -->
	<xsl:template match="ft:fullTextQuery[ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery]">
		<xsl:apply-templates select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery">
			<xsl:with-param name="p">0</xsl:with-param> 
		</xsl:apply-templates>	
	</xsl:template>
		
	<xsl:template match="ft:word">
	<!--  TODO ... look into getting word attributes -->
		<xsl:param name="p"/>
		<xsl:if test="$p != 0">
          	<xsl:call-template name="indent">
            	<xsl:with-param name="cnt" select="$p"/>
          	</xsl:call-template>
			<xsl:text>WORD [</xsl:text>
			<xsl:if test="exists(./@path)">
				<xsl:text> path=</xsl:text>
				<xsl:value-of select="./@path"/>
			</xsl:if>
			<xsl:if test="./@phrase='true'">
				<xsl:text> phrase=true</xsl:text>
			</xsl:if>
			<xsl:if test="./@punct='sensitive'">
				<xsl:text> punct=sensitive</xsl:text>
			</xsl:if>
			<xsl:if test="./@equals='true'">
				<xsl:text> equals=true</xsl:text>
			</xsl:if>
			<xsl:if test="./@startsWith='true'">
				<xsl:text> startsWith=true</xsl:text>
			</xsl:if>
			<xsl:if test="./@endsWith='true'">
				<xsl:text> endsWith=true</xsl:text>
			</xsl:if>			
			<xsl:text> ] </xsl:text>
			<xsl:value-of select="./text()"/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="ft:andQuery">
		<xsl:param name="p"/>
		<xsl:if test="$p != 0">
          	<xsl:call-template name="indent">
            	<xsl:with-param name="cnt" select="$p"/>
          	</xsl:call-template>
			<xsl:text>BEGIN AND</xsl:text>
		</xsl:if>
		<xsl:apply-templates select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery">
			<xsl:with-param name="p">
				<xsl:choose>
					<xsl:when test="$p != 0">
						<xsl:value-of select="$p+1"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$p"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
		</xsl:apply-templates>
		<xsl:if test="$p != 0">
          	<xsl:call-template name="indent">
            	<xsl:with-param name="cnt" select="$p"/>
          	</xsl:call-template>
			<xsl:text>END AND</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="ft:orQuery">
		<xsl:param name="p"/>
		<xsl:if test="$p != 0">
          	<xsl:call-template name="indent">
            	<xsl:with-param name="cnt" select="$p"/>
          	</xsl:call-template>
			<xsl:text>BEGIN OR</xsl:text>
		</xsl:if>
		<xsl:apply-templates select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery">
			<xsl:with-param name="p">
				<xsl:choose>
					<xsl:when test="$p != 0">
						<xsl:value-of select="$p+1"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$p"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
		</xsl:apply-templates>
		<xsl:if test="$p != 0">
          	<xsl:call-template name="indent">
            	<xsl:with-param name="cnt" select="$p"/>
          	</xsl:call-template>
			<xsl:text>END OR</xsl:text>
		</xsl:if>		
	</xsl:template>
	
	<xsl:template match="ft:notQuery">
		<xsl:param name="p"/>
		<xsl:if test="$p != 0">
          	<xsl:call-template name="indent">
            	<xsl:with-param name="cnt" select="$p"/>
          	</xsl:call-template>
			<xsl:text>BEGIN NOT</xsl:text>
		</xsl:if>		
		<xsl:apply-templates select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery">
			<xsl:with-param name="p">
				<xsl:choose>
					<xsl:when test="$p != 0">
						<xsl:value-of select="$p+1"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$p"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>
		</xsl:apply-templates>
		<xsl:if test="$p != 0">
          	<xsl:call-template name="indent">
            	<xsl:with-param name="cnt" select="$p"/>
          	</xsl:call-template>
			<xsl:text>END NOT</xsl:text>
		</xsl:if>		
	</xsl:template>

	<xsl:template match="ft:numericCompare"/>

	<xsl:template match="ft:proximityQuery">
		<xsl:param name="p"/>
          	<xsl:call-template name="indent">
            	<xsl:with-param name="cnt" select="$p"/>
          	</xsl:call-template>
		<xsl:text>BEGIN PROXIMITY </xsl:text>
		<xsl:text>[ </xsl:text>
		<xsl:text>distance=</xsl:text>
		<xsl:value-of select="./@slack"/>
		<xsl:text> ordered=</xsl:text>
		<xsl:value-of select="./@ordered"/>			
		<xsl:if test="exists(./@path)">
			<xsl:text> path=</xsl:text>
			<xsl:value-of select="./@path"/>
		</xsl:if>	
		<xsl:text> ] </xsl:text>	
		<xsl:apply-templates select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery">
			<xsl:with-param name="p"><xsl:value-of select="$p+1"/></xsl:with-param>
		</xsl:apply-templates>
          	<xsl:call-template name="indent">
            	<xsl:with-param name="cnt" select="$p"/>
          	</xsl:call-template>
		<xsl:text>END PROXIMITY</xsl:text>
	</xsl:template>
	
	<xsl:template name="indent">
    	<xsl:param name="cnt"/>
    	<xsl:text>&#10;</xsl:text>
    	<xsl:for-each select="(1 to $cnt)">
    		<xsl:text>    </xsl:text>
		</xsl:for-each>
    </xsl:template>
				
</xsl:stylesheet>

