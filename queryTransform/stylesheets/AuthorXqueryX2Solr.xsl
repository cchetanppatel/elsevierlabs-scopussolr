<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:ft="http://www.elsevier.com/2003/01/xqueryxFT-schema" 
  xmlns:qw="http://webservices.elsevier.com/schemas/search/fast/types/v4"
  version="2.0">
    
     <!-- 
     
    	Stylesheet for transforming author queries into Solr syntax.
    
    	Limitations:
      		Navigator selection (AdjustmentGroup elements) are currently ignored.
      		
        TODO:
          May want to add -p field for punct="sensitive" queries although none exist
      
     -->
     
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
      <!--  All fields present in query set are correctly defined and to not need to be adjusted.
      		Some of the fields may not actually have content (or contain dummy values).  This would 
      		include many of the affiliation related fields not merged into the author record.
      -->
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
 
  
  <!-- Sort lookup (replace field names) -->
  <xsl:template name="sort-lookup-field">
    <xsl:param name="f"/> 
    <xsl:choose>
      
      <!-- Affiliation display city (21) -->
      <!--  Queries with this field removed as part of 'slim' script.  -->
      
      <!-- Affiliation display country (68) -->
      <!--  Queries with this field removed as part of 'slim' script.  -->

      <!-- Affiliation city (0) -->
      <!--  Queries with this field removed as part of 'slim' script.  -->
      <!--  Never referenced in any queries anyway.                    -->

      <!-- Affiliation country (0) -->
      <!--  Queries with this field removed as part of 'slim' script.  -->
      <!--  Never referenced in any queries anyway.                    -->

      <!-- Affiliation name (0) -->
      <!--  Queries with this field removed as part of 'slim' script.  -->
      <!--  Never referenced in any queries anyway.                    -->

      <!-- Affiliation sort name (37) -->
      <!--  Queries with this field removed as part of 'slim' script.  -->
      
      <!-- Affiliation history city (0) -->
      <!--  Queries with this field removed as part of 'slim' script.  -->
      <!--  Never referenced in any queries anyway.                    -->

      <!-- Affiliation history country (0) -->
      <!--  Queries with this field removed as part of 'slim' script.  -->
      <!--  Never referenced in any queries anyway.                    -->

      <!-- Affiliation history display name  (0) -->
      <!--  Queries with this field removed as part of 'slim' script.  -->
      <!--  Never referenced in any queries anyway.                    -->
      
      <!-- Relevancy (938) -->
      <xsl:when test="matches($f,'^relevancy$')">
        <xsl:text>score</xsl:text>
      </xsl:when>
      
      <!-- Assume we use the specified name as the field  -->   
      <!--  This would include 
      			active (175259)
      			authid (223826)
      			count (223824)
      			eid (0)
      			preffirst (766)
      			preffirstsort (223826)
      			prefini (766)
      			preflast (224869)
      			prefname  (0)
      -->                                                                                                                                                                                                                                                                                                                                                                               
      <xsl:otherwise>
        <xsl:value-of select="$f"/>
      </xsl:otherwise>
      
    </xsl:choose> 
  </xsl:template>
  
  
  <!-- Facet lookup (replace field names) -->
  <xsl:template name="facet-lookup-field">
    <xsl:param name="f"/> 
    <xsl:choose>
      
      <!-- Active (38451) -->
      <xsl:when test="matches($f,'^autactivenav$')">
        <xsl:text>active-f</xsl:text>
      </xsl:when>
      
      <!-- Affiliation city (49754) -->
      <!-- Don't have a value for affilcity-f.  -->
      <xsl:when test="matches($f,'^autaffilcitynav$')">
        <xsl:text>affilcity-f</xsl:text>
      </xsl:when>
 
      <!-- Affiliation country (49734) -->
      <!-- Don't have a value for affilctry-f.  -->
      <xsl:when test="matches($f,'^autaffilctrynav$')">
        <xsl:text>affilctry-f</xsl:text>
      </xsl:when>

      <!-- Affiliation id (0) -->
      <xsl:when test="matches($f,'^autafidnav$')">
        <xsl:text>afid-f</xsl:text>
      </xsl:when>

      <!-- Affiliation name id (49789) -->
      <!-- Don't have a value for afnameid-f.  Used author last name and afid. -->
      <xsl:when test="matches($f,'^autafnameidnav$')">
        <xsl:text>afnameid-f</xsl:text>
      </xsl:when>

      <!-- Source title (49227) -->
      <xsl:when test="matches($f,'^autsrctitlenav$')">
        <xsl:text>srctitle-f</xsl:text>
      </xsl:when>

      <!-- Subject cluster (49745) -->
      <!-- Don't have a value for subjclus-f.  -->
      <xsl:when test="matches($f,'^autsubjclusnav$')">
        <xsl:text>subjclus-f</xsl:text>
      </xsl:when>
      
      <!-- Assume we use the specified name as the field  -->
      <!--  This should not happen. -->                                                                                                                                                                                                                                                                                                                                                                                 
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
        <xsl:text>authfirst-m</xsl:text>
      </xsl:when>
      
      <!-- Author last name -->
      <xsl:when test="matches($f,'^authlast$')">
        <xsl:text>authlast-m</xsl:text>
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
        <xsl:value-of select="lower-case($w)"/>
        <xsl:text>"</xsl:text>
      </xsl:when>
      
      <!-- Just use the text (no need for a phrase)  -->
      <!-- Escape these characters for 'keyword' fields   + - & | ! ( ) { } [ ] ^ ~ : \ / " -->
      <xsl:otherwise>
        <xsl:variable name="var1">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="lower-case($w)" />
            <xsl:with-param name="replace" select="'\'" />
            <xsl:with-param name="by" select="'\\'" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="var2">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var1" />
            <xsl:with-param name="replace" select="'-'" />
            <xsl:with-param name="by" select="'\-'" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="var3">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var2" />
            <xsl:with-param name="replace" select="'&amp;'" />
            <xsl:with-param name="by" select="'\&amp;'" />
          </xsl:call-template>
        </xsl:variable>       
        <xsl:variable name="var4">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var3" />
            <xsl:with-param name="replace" select="'|'" />
            <xsl:with-param name="by" select="'\|'" />
          </xsl:call-template>
        </xsl:variable>          
        <xsl:variable name="var5">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var4" />
            <xsl:with-param name="replace" select="'!'" />
            <xsl:with-param name="by" select="'\!'" />
          </xsl:call-template>
        </xsl:variable>          
        <xsl:variable name="var6">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var5" />
            <xsl:with-param name="replace" select="'('" />
            <xsl:with-param name="by" select="'\('" />
          </xsl:call-template>
        </xsl:variable>         
        <xsl:variable name="var7">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var6" />
            <xsl:with-param name="replace" select="')'" />
            <xsl:with-param name="by" select="'\)'" />
          </xsl:call-template>
        </xsl:variable>        
        <xsl:variable name="var8">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var7" />
            <xsl:with-param name="replace" select="'{'" />
            <xsl:with-param name="by" select="'\{'" />
          </xsl:call-template>
        </xsl:variable>         
        <xsl:variable name="var9">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var8" />
            <xsl:with-param name="replace" select="'}'" />
            <xsl:with-param name="by" select="'\}'" />
          </xsl:call-template>
        </xsl:variable>        
        <xsl:variable name="var10">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var9" />
            <xsl:with-param name="replace" select="'['" />
            <xsl:with-param name="by" select="'\['" />
          </xsl:call-template>
        </xsl:variable>  
        <xsl:variable name="var11">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var10" />
            <xsl:with-param name="replace" select="']'" />
            <xsl:with-param name="by" select="'\]'" />
          </xsl:call-template>
        </xsl:variable>          
        <xsl:variable name="var12">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var11" />
            <xsl:with-param name="replace" select="'^'" />
            <xsl:with-param name="by" select="'\^'" />
          </xsl:call-template>
        </xsl:variable>         
        <xsl:variable name="var13">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var12" />
            <xsl:with-param name="replace" select="'~'" />
            <xsl:with-param name="by" select="'\~'" />
          </xsl:call-template>
        </xsl:variable> 
        <xsl:variable name="var14">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var13" />
            <xsl:with-param name="replace" select="':'" />
            <xsl:with-param name="by" select="'\:'" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="var15">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var14" />
            <xsl:with-param name="replace" select="'+'" />
            <xsl:with-param name="by" select="'\+'" />
          </xsl:call-template>
        </xsl:variable>  
        <xsl:variable name="var16">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var15" />
            <xsl:with-param name="replace" select="'/'" />
            <xsl:with-param name="by" select="'\/'" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="var17">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$var16" />
            <xsl:with-param name="replace" select="'&quot;'" />
            <xsl:with-param name="by" select="'\&quot;'" />
          </xsl:call-template>
        </xsl:variable>        
        <xsl:value-of select="$var17"/>
        <!-- Marked as a 'starts with' so add a wildcard -->
        <xsl:if test="$w/@startsWith='true'">
          <xsl:text>*</xsl:text>
        </xsl:if>
      </xsl:otherwise>
      
    </xsl:choose> 
  </xsl:template>


  <xsl:template name="string-replace-all">
    <xsl:param name="text" />
    <xsl:param name="replace" />
    <xsl:param name="by" />
    <xsl:choose>
      <xsl:when test="contains($text, $replace)">
        <xsl:value-of select="substring-before($text,$replace)" />
        <xsl:value-of select="$by" />
        <xsl:call-template name="string-replace-all">
          <xsl:with-param name="text" select="substring-after($text,$replace)" />
          <xsl:with-param name="replace" select="$replace" />
          <xsl:with-param name="by" select="$by" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Ingore what we didn't explicitly ask for -->
  <xsl:template match="text()"/>
  
    
</xsl:stylesheet>

