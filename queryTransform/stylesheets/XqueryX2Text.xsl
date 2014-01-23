<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:ft="http://www.elsevier.com/2003/01/xqueryxFT-schema" 
  xmlns:qw="http://webservices.elsevier.com/schemas/search/fast/types/v4"
  version="2.0">
    
    <!-- Turn off auto-insertion of <?xml> tag and set indenting on -->
    <xsl:output method="text" encoding="utf-8" indent="yes"/>
    
    <!-- strip whitespace from whitespace-only nodes -->
    <xsl:strip-space elements="*"/>

  	<!-- Ingore what we didn't explicitly ask for -->
  	<xsl:template match="text()|@*"/>
  
    <!-- Get the return fields -->
    <xsl:variable name="fields" select="/qw:search/qw:searchReqPayload/qw:reqFields"/>
  
    <!-- Get the start -->
    <xsl:variable name="start" select="/qw:search/qw:searchReqPayload/qw:returnAttributes/@start"/>
 
    <!-- Get the maxResults -->
    <xsl:variable name="maxResults" select="/qw:search/qw:searchReqPayload/qw:returnAttributes/@maxResults"/>
  
    <!-- Get the orderBy fields -->
    <xsl:variable name="orderBys" select="/qw:search/qw:searchReqPayload/qw:orderByList/qw:orderByAttributes"/>
  
    <!-- Get the facet fields -->
    <xsl:variable name="facets" select="/qw:search/qw:searchReqPayload/qw:reqDimension[qw:navigator]"/>

    <!-- Get the filter query -->
    <xsl:variable name="filter" select="/qw:search/qw:searchReqPayload/qw:elsfilter/ft:fullTextQuery"/>
        	
    <!-- Build the query -->
    <xsl:template match="/">
      
      <!-- go build the query -->
      <xsl:text>Query = </xsl:text>
	  <xsl:if test="/qw:search/qw:searchReqPayload/qw:xQueryX/ft:fullTextQuery[ft:query]">
	  	<xsl:apply-templates select="/qw:search/qw:searchReqPayload/qw:xQueryX/ft:fullTextQuery/ft:query"/>
	  </xsl:if>
	  <xsl:if test="/qw:search/qw:searchReqPayload/qw:xQueryX/ft:fullTextQuery[ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery | ft:scopeQuery]">
	  	<xsl:apply-templates select="/qw:search/qw:searchReqPayload/qw:xQueryX/ft:fullTextQuery/(ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery | ft:scopeQuery)"/>
	  </xsl:if>

      <!--  return fields clause -->
      <xsl:if test="exists($fields)">
      	<xsl:text>&#10;</xsl:text>
        <xsl:text>Return Fields = </xsl:text>
        <xsl:for-each select="$fields">
          <xsl:value-of select="./text()"/>
          <xsl:if test="position() != last()">
            <xsl:text>,</xsl:text>
          </xsl:if>
        </xsl:for-each>
      </xsl:if>
 
      <!-- start clause -->
      <xsl:if test="exists($start)">
      	<xsl:text>&#10;</xsl:text>
        <xsl:text>Results start = </xsl:text>
        <xsl:value-of select="$start"/>
      </xsl:if>

      
      <!-- maxResults clause -->
      <xsl:if test="exists($maxResults)">
      	<xsl:text>&#10;</xsl:text>
        <xsl:text>Results count = </xsl:text>
        <xsl:value-of select="$maxResults"/>
      </xsl:if>
      
      <!-- orderBy clause -->
      <xsl:if test="exists($orderBys)">
      	<xsl:text>&#10;</xsl:text>
        <xsl:text>Results order = </xsl:text>
        <xsl:for-each select="$orderBys">
        	<xsl:value-of select="string(./@path)"/>
          <xsl:text> </xsl:text>
          <xsl:choose>
            <xsl:when test="./@sortOrder = 'descending'">
              <xsl:text>desc</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>asc</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="position() != last()">
            <xsl:text>, </xsl:text>
          </xsl:if>
        </xsl:for-each>
      </xsl:if>

      <!-- facet clause -->
      <xsl:if test="exists($facets)">
      	<xsl:text>&#10;</xsl:text>
        <xsl:text>Facets = </xsl:text>
        <xsl:for-each select="$facets">
       		<xsl:value-of select="string(./qw:navigator)"/>
       		<xsl:if test="position() != last()">
            	<xsl:text>, </xsl:text>
          	</xsl:if>
        </xsl:for-each>
      </xsl:if>
      
      <!-- filter query -->
      <xsl:if test="exists($filter)">
      	<xsl:text>&#10;</xsl:text>
        <xsl:text>Filter query = </xsl:text>
        <xsl:apply-templates select="$filter/ft:query"/>
      </xsl:if>
      <xsl:text>&#10;</xsl:text>
            
  </xsl:template>
  
  
  <!-- Query clause -->
  <xsl:template match="ft:query">
    <xsl:apply-templates select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery | ft:scopeQuery"/>
  </xsl:template>
  
  
  <!-- AND query clause -->
  <xsl:template match="ft:andQuery">
    <xsl:text>(</xsl:text>  
    <xsl:for-each select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery | ft:scopeQuery">
      <xsl:if test="position() != 1">
        <xsl:text> AND </xsl:text>
      </xsl:if>
        <xsl:apply-templates select="."/>
    </xsl:for-each>
    <xsl:text>)</xsl:text>
  </xsl:template>
  
  
  <!-- OR query clause -->
  <xsl:template match="ft:orQuery">
    <xsl:text>(</xsl:text>  
    <xsl:for-each select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery | ft:scopeQuery">
      <xsl:if test="position() != 1">
        <xsl:text> OR </xsl:text>
      </xsl:if>
      <xsl:apply-templates select="."/>
    </xsl:for-each>
    <xsl:text>)</xsl:text>
  </xsl:template>
  
  
  <!-- NOT query clause -->
  <xsl:template match="ft:notQuery">
    <xsl:text>NOT(</xsl:text>
    <xsl:apply-templates select="ft:word | ft:andQuery | ft:orQuery | ft:numericCompare | ft:proximityQuery | ft:scopeQuery" />
    <xsl:text>)</xsl:text>
  </xsl:template>
  
  
  <!-- WORD query clause -->
  <xsl:template match="ft:word">
    <xsl:choose>
      <xsl:when test="(./@startsWith='true' or ./@equals='true')">
        <xsl:call-template name="starts-equals-lookup-field">
          <xsl:with-param name="f" select="string(./@path)"/>
        </xsl:call-template>    
      </xsl:when>
      <xsl:otherwise>
      	<xsl:if test="exists(./@path)">  
        	<xsl:value-of select="./@path"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="exists(./@path)">
    	<xsl:text>:</xsl:text>
    </xsl:if>
    <!-- Go decide whether to cleanup the word (add quotes, escape characters, add trailing wildcard) -->
    <xsl:call-template name="word-cleanup">
      <xsl:with-param name="w" select="."/>
    </xsl:call-template> 
  </xsl:template>
  
  
  <!-- NUMERIC queryclause -->
  <!-- TODO may need to adjust the values for the field -->
  <!-- TODO need to check if a date value may need to be adjusted (but date is never used) -->
  <xsl:template match="ft:numericCompare">  
    <xsl:value-of select="ft:clause/@path"/>
    <xsl:text>:</xsl:text>
    <xsl:choose>
      <xsl:when test="./@comparator='lessThan'">
        <xsl:text>[* TO </xsl:text>
        <xsl:value-of select="ft:decimal | ft:date"/>
        <xsl:text>}</xsl:text>
      </xsl:when>   
      <xsl:when test="./@comparator='lessThanOrEqual'">
        <xsl:text>[* TO </xsl:text>
        <xsl:value-of select="ft:decimal | ft:date"/>
        <xsl:text>]</xsl:text>
      </xsl:when> 
      <xsl:when test="./@comparator='greaterThan'">
        <xsl:text>{</xsl:text>
        <xsl:value-of select="ft:decimal | ft:date"/>
        <xsl:text> TO *]</xsl:text>
      </xsl:when>
      <xsl:when test="./@comparator='greaterThanOrEqual'">
        <xsl:text>[</xsl:text>
        <xsl:value-of select="ft:decimal | ft:date"/>
        <xsl:text> TO *]</xsl:text>        
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="ft:decimal | ft:date"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
 
 
  <xsl:template match="ft:proximityQuery">
  	<xsl:text> PROXIMITY</xsl:text>
		<xsl:text>[</xsl:text>
		<xsl:text>distance=</xsl:text>
		<xsl:value-of select="./@slack"/>
		<xsl:text> ordered=</xsl:text>
		<xsl:value-of select="./@ordered"/>			
		<xsl:if test="exists(./@path)">
			<xsl:text> path=</xsl:text>
			<xsl:value-of select="./@path"/>
		</xsl:if>	
		<xsl:text>](</xsl:text>	
		<xsl:for-each  select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery">
			<xsl:if test="position() != 1">
        		<xsl:text>, </xsl:text>
      		</xsl:if>
      		<xsl:apply-templates select="."/>
		</xsl:for-each>
		<xsl:text>)</xsl:text>
 </xsl:template>
	
	
 <xsl:template match="ft:scopeQuery">
  	<xsl:text> SCOPE</xsl:text>
		<xsl:text>[</xsl:text>		
		<xsl:if test="exists(./@path)">
			<xsl:text>path=</xsl:text>
			<xsl:value-of select="./@path"/>
		</xsl:if>	
		<xsl:text>](</xsl:text>	
		<xsl:for-each  select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | ft:proximityQuery">
			<xsl:if test="position() != 1">
        		<xsl:text>, </xsl:text>
      		</xsl:if>
      		<xsl:apply-templates select="."/>
		</xsl:for-each>
		<xsl:text>)</xsl:text>
 </xsl:template>
 	
 	
  <!-- Starts Equals lookup (replace field names) -->
  <xsl:template name="starts-equals-lookup-field">
    <xsl:param name="f"/> 
    <xsl:choose>
      
      <!-- Author first name -->
      <xsl:when test="matches($f,'^authfirst$')">
        <xsl:text>authfirst-p</xsl:text>
      </xsl:when>
      
      <!-- Author last name -->
      <xsl:when test="matches($f,'^authlast$')">
        <xsl:text>authlast-p</xsl:text>
      </xsl:when>
     
      <!-- Assume we use the specified name for now as the field (which really won't work as expected) -->                                                                                                                                                                                                                                                                                                                                                                                 
      <xsl:otherwise>
        <xsl:value-of select="$f"/>
      </xsl:otherwise>
      
    </xsl:choose> 
  </xsl:template>
  
 
  
  <!-- Word cleanup  -->
  <!-- For now, be safe and lower-case everything (may not really be needed) -->
  <xsl:template name="word-cleanup">
    <xsl:param name="w"/> 
    <xsl:choose>
      
      <!-- Marked as a phrase so make it a phrase -->
      <xsl:when test="$w/@phrase='true'">
        <xsl:text>"</xsl:text>
        <xsl:value-of select="$w/text()"/>
        <xsl:text>"</xsl:text>
      </xsl:when>
      
      <xsl:otherwise>
      	<xsl:value-of select="$w/text()"/>
        <!-- Marked as a 'starts with' so add a wildcard -->
        <xsl:if test="$w/@startsWith='true'">
          <xsl:text>*</xsl:text>
        </xsl:if>
      </xsl:otherwise>
      
    </xsl:choose> 
  </xsl:template>
  
    
</xsl:stylesheet>

