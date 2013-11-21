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
    <xsl:template match="/qw:search/qw:searchReqPayload/qw:xQueryX">
      
      <!-- go build the query -->
      <xsl:text>q=</xsl:text>
      <xsl:apply-templates select="./ft:fullTextQuery/ft:query"/>

      <!--  return fields clause -->
      <xsl:if test="exists($fields)">
        <xsl:text>&amp;fl=</xsl:text>
        <xsl:for-each select="$fields">
          <!-- TODO may need to adjust values for return field -->
          <xsl:value-of select="./text()"/>
          <xsl:if test="position() != last()">
            <xsl:text>,</xsl:text>
          </xsl:if>
        </xsl:for-each>
      </xsl:if>
 
      <!-- start clause -->
      <xsl:if test="exists($start)">
        <xsl:text>&amp;start=</xsl:text>
        <xsl:value-of select="$start"/>
      </xsl:if>
      
      <!-- maxResults clause -->
      <xsl:if test="exists($maxResults)">
        <xsl:text>&amp;rows=</xsl:text>
        <xsl:value-of select="$maxResults"/>
      </xsl:if>
      
      <!-- orderBy clause -->
      <xsl:if test="exists($orderBys)">
        <xsl:text>&amp;sort=</xsl:text>
        <xsl:for-each select="$orderBys">
          <xsl:call-template name="sort-lookup-field">
            <xsl:with-param name="f" select="string(./@path)"/>
          </xsl:call-template>
          <xsl:text>+</xsl:text>
          <xsl:choose>
            <xsl:when test="./@sortOrder = 'descending'">
              <xsl:text>desc</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>asc</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="position() != last()">
            <xsl:text>,</xsl:text>
          </xsl:if>
        </xsl:for-each>
      </xsl:if>

      <!-- facet clause -->
      <xsl:if test="exists($facets)">
        <xsl:text>&amp;facet=true</xsl:text>
        <xsl:text>&amp;facet.mincount=1</xsl:text>
        <xsl:for-each select="$facets">
          <xsl:text>&amp;facet.field=</xsl:text>
          <xsl:call-template name="facet-lookup-field">
            <xsl:with-param name="f" select="string(./qw:navigator)"/>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="$facets">
          <xsl:text>&amp;f.</xsl:text>
          <xsl:call-template name="facet-lookup-field">
            <xsl:with-param name="f" select="string(./qw:navigator)"/>
          </xsl:call-template>
          <xsl:text>.facet.limit=</xsl:text>
          <xsl:value-of select="./qw:count/text()"/>
        </xsl:for-each>          
      </xsl:if>
      
      <!-- filter query -->
      <xsl:if test="exists($filter)">
        <xsl:text>&amp;fq=</xsl:text>
        <xsl:apply-templates select="ft:query"/>
      </xsl:if>
      
  </xsl:template>
  
  
  <!-- Query clause -->
  <xsl:template match="ft:query">
    <xsl:apply-templates select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare"/>
  </xsl:template>
  
  
  <!-- AND query clause -->
  <xsl:template match="ft:andQuery">
    <xsl:text>(</xsl:text>  
    <xsl:for-each select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare">
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
    <xsl:for-each select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare">
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
    <xsl:apply-templates select="ft:word | ft:andQuery | ft:orQuery | ft:numericCompare"/>
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
        <xsl:value-of select="./@path"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>:</xsl:text>
    <!-- Go decide whether to quote the text -->
    <xsl:call-template name="quote-lookup-field">
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
 
  
  <!-- Sort lookup (replace field names) -->
  <xsl:template name="sort-lookup-field">
    <xsl:param name="f"/> 
    <xsl:choose>
      
      <!-- Affiliation display city -->
      <!-- Don't have a value for afdispcity-s.  Use authfirst. -->
      <xsl:when test="matches($f,'^afdispcity$')">
        <xsl:text>authfirst</xsl:text>
      </xsl:when>
      
      <!-- Affiliation display country -->
      <!-- Don't have a value for afdispctry-s.  Use issn. -->
      <xsl:when test="matches($f,'^afdispctry$')">
        <xsl:text>issn</xsl:text>
      </xsl:when>

      <!-- Affiliation city -->
      <!-- Don't have a value for affilcity-s.  Use authfirst. -->
      <xsl:when test="matches($f,'^affilcity$')">
        <xsl:text>authfirst</xsl:text>
      </xsl:when>

      <!-- Affiliation country -->
      <!-- Don't have a value for affilctry-s.  Use issn. -->
      <xsl:when test="matches($f,'^affilctry$')">
        <xsl:text>issn</xsl:text>
      </xsl:when>

      <!-- Affiliation name -->
      <!-- Don't have a value for affilname-s.  Use namevarini. -->
      <xsl:when test="matches($f,'^affilname$')">
        <xsl:text>namevarini</xsl:text>
      </xsl:when>

      <!-- Affiliation sort name -->
      <!-- Don't have a value for affilsortname-s.  Use namevarini. -->
      <xsl:when test="matches($f,'^affilsortname$')">
        <xsl:text>namevarini</xsl:text>
      </xsl:when>
      
      <!-- Affiliation history city -->
      <!-- Don't have a value for afdispcity-s.  Use authfirst. -->
      <xsl:when test="matches($f,'^afhistcity$')">
        <xsl:text>authfirst</xsl:text>
      </xsl:when>

      <!-- Affiliation history country -->
      <!-- Don't have a value for afhistctry-s.  Use issn. -->
      <xsl:when test="matches($f,'^afhistctry$')">
        <xsl:text>issn</xsl:text>
      </xsl:when>

      <!-- Affiliation history display name  -->
      <!-- Don't have a value for afhistdispname-s.  Use namevarini. -->
      <xsl:when test="matches($f,'^afhistdispname$')">
        <xsl:text>namevarini</xsl:text>
      </xsl:when>
      
      <!-- Assume we use the specified name as the field  -->                                                                                                                                                                                                                                                                                                                                                                                 
      <xsl:otherwise>
        <xsl:value-of select="$f"/>
      </xsl:otherwise>
      
    </xsl:choose> 
  </xsl:template>
  
  
  <!-- Facet lookup (replace field names) -->
  <xsl:template name="facet-lookup-field">
    <xsl:param name="f"/> 
    <xsl:choose>
      
      <!-- Active -->
      <xsl:when test="matches($f,'^autactivenav$')">
        <xsl:text>active</xsl:text>
      </xsl:when>
      
      <!-- Affiliation city -->
      <!-- Don't have a value for affilcity-f.  Use authfirst. -->
      <xsl:when test="matches($f,'^autaffilcitynav$')">
        <xsl:text>authfirst</xsl:text>
      </xsl:when>
 
      <!-- Affiliation country -->
      <!-- Don't have a value for affilctry-f.  Use issn. -->
      <xsl:when test="matches($f,'^autaffilctrynav$')">
        <xsl:text>issn</xsl:text>
      </xsl:when>

      <!-- Affiliation id -->
      <xsl:when test="matches($f,'^autafidnav$')">
        <xsl:text>afid-f</xsl:text>
      </xsl:when>

      <!-- Affiliation name id -->
      <!-- Don't have a value for afnameid-f.  Use namevarini. -->
      <xsl:when test="matches($f,'^autafnameidnav$')">
        <xsl:text>namevarini</xsl:text>
      </xsl:when>

      <!-- Source title  -->
      <xsl:when test="matches($f,'^autsrctitlenav$')">
        <xsl:text>srctitle-f</xsl:text>
      </xsl:when>

      <!-- Subject cluster  -->
      <!-- Don't have a value for subjclus-f.  Use subjmain.  -->
      <xsl:when test="matches($f,'^autsubjclusnav$')">
        <xsl:text>subjmain</xsl:text>
      </xsl:when>
      
      <!-- Assume we use the specified name as the field  -->                                                                                                                                                                                                                                                                                                                                                                                 
      <xsl:otherwise>
        <xsl:value-of select="$f"/>
      </xsl:otherwise>
      
    </xsl:choose> 
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
  

  <!-- Quote lookup  -->
  <xsl:template name="quote-lookup-field">
    <xsl:param name="w"/> 
    <xsl:choose>
      
      <!-- Marked as a phrase so make it a phrase -->
      <xsl:when test="$w/@phrase='true'">
        <xsl:text>"</xsl:text>
        <xsl:value-of select="$w/text()"/>
        <xsl:text>"</xsl:text>
      </xsl:when>
      
      <!-- Marked as a 'starts with' so add a wildcard -->
      <xsl:when test="$w/@startsWith='true'">
        <xsl:value-of select="./text()"/>
        <xsl:text>*</xsl:text>
      </xsl:when>
      
      <!-- Just use the text (no need for a phrase)  -->                                                                                                                                                                                                                                                                                                                                                                                 
      <xsl:otherwise>
        <xsl:value-of select="$w/text()"/>
      </xsl:otherwise>
      
    </xsl:choose> 
  </xsl:template>    
  
  
  <!-- Ingore what we didn't explicitly ask for -->
  <xsl:template match="text()"/>
  
    
</xsl:stylesheet>

