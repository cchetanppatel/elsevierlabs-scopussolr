<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:ft="http://www.elsevier.com/2003/01/xqueryxFT-schema" 
  xmlns:qw="http://webservices.elsevier.com/schemas/search/fast/types/v4"
  version="2.0">
    
    <!-- Check for highlighting flag (only highlights abs) -->    
    <!--  Switch fields for 'punct sensitive'               -->
    
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
    <xsl:apply-templates select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | proximityQuery"/>
  </xsl:template>
  
  
  <!-- AND query clause -->
  <xsl:template match="ft:andQuery">
    <xsl:text>(</xsl:text>  
    <xsl:for-each select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | proximityQuery">
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
    <xsl:for-each select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | proximityQuery">
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
    <xsl:apply-templates select="ft:word | ft:andQuery | ft:orQuery | ft:numericCompare | proximityQuery"/>
    <xsl:text>)</xsl:text>
  </xsl:template>
  
  
  <!-- WORD query clause -->
  <xsl:template match="ft:word">
    <!--TODO may need to adjust values for the field -->
    <!--
      Need to figure out those fields for weighting 
      _query_:"{!edismax qf='itemtitle^10 abs^5'}earth"
      auth:mcbeath
     -->
    <xsl:value-of select="./@path"/>
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
 
 
  <!-- PROXIMITY query clause -->
  <!-- _query_:"{!surround}(abs:(heat 15w drive))"  -->
  <!-- May want to ignore complex proximity queries -->
  <!-- Can handle descendant OR but not AND         -->
  <xsl:template match="ft:proximityQuery">
    <xsl:value-of select="./@path"/>
    <xsl:text>:</xsl:text>
    <xsl:text>"</xsl:text>
    <xsl:for-each select="./ft:word">
      <xsl:if test="position() != 1 and position() != last()">
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:call-template name="word-cleanup">
        <xsl:with-param name="w" select="."/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:text>"~</xsl:text>
    <xsl:value-of select="./@slack"/>
  </xsl:template>  
  
  
  <!-- Sort lookup (replace field names) -->
  <xsl:template name="sort-lookup-field">
    <xsl:param name="f"/> 
    <xsl:choose>
          
      <!-- Affiliation Country (0) -->
      <xsl:when test="matches($f,'^affilctry$')">
        <xsl:text>affilctry-s</xsl:text>
      </xsl:when>

	  <!-- Article Number (0) -->
      <xsl:when test="matches($f,'^artnum$')">
        <xsl:text>artnum-s</xsl:text>
      </xsl:when>

	  <!-- Author Cite (0) -->
      <xsl:when test="matches($f,'aucite')">
        <xsl:text>aucite-s</xsl:text>
      </xsl:when>

	  <!-- Author (2) -->
      <xsl:when test="matches($f,'auth')">
        <xsl:text>auth-s</xsl:text>
      </xsl:when>

	  <!-- Datesort (1475970) -->
      <xsl:when test="matches($f,'datesort')">
        <xsl:text>datesort-s</xsl:text>
      </xsl:when>

	  <!-- FAST Load Date (2456) -->
      <xsl:when test="matches($f,'fastloaddate')">
        <xsl:text>fastloaddate-s</xsl:text>
      </xsl:when>

	  <!-- Issue (0) -->
      <xsl:when test="matches($f,'issue')">
        <xsl:text>issue-s</xsl:text>
      </xsl:when>

	  <!-- Item Title (1102) -->
      <xsl:when test="matches($f,'itemtitle')">
        <xsl:text>itemtitle-s</xsl:text>
      </xsl:when>

	  <!-- Number Cited By (141814) -->
      <xsl:when test="matches($f,'numcitedby')">
        <xsl:text>numcitedby-s</xsl:text>
      </xsl:when>

	  <!-- Page Count (0) -->
      <xsl:when test="matches($f,'pagecount')">
        <xsl:text>pagecount-s</xsl:text>
      </xsl:when>

	  <!-- Pages (0) -->
      <xsl:when test="matches($f,'pg')">
        <xsl:text>pg-s</xsl:text>
      </xsl:when>

	  <!-- Page (First) (0) -->
      <xsl:when test="matches($f,'pgfirst')">
        <xsl:text>pgfirst-s</xsl:text>
      </xsl:when>
 
 	  <!-- Publication Year (20374) -->
      <xsl:when test="matches($f,'pubyr')">
        <xsl:text>pubyr-s</xsl:text>
      </xsl:when>

      <!-- Relevancy (1326725) -->
      <xsl:when test="matches($f,'^relevancy$')">
        <xsl:text>score</xsl:text>
      </xsl:when>
      
 	  <!-- Source Title (0) -->
      <xsl:when test="matches($f,'srctitle')">
        <xsl:text>srctitle-s</xsl:text>
      </xsl:when>

 	  <!-- Status Type (0) -->
      <xsl:when test="matches($f,'statustype')">
        <xsl:text>statustype-s</xsl:text>
      </xsl:when>

 	  <!-- Volume (0) -->
      <xsl:when test="matches($f,'vol')">
        <xsl:text>vol-s</xsl:text>
      </xsl:when>
                                                                                               
      <!-- Assume we use the specified name as the field  -->
      <!--  This should not happen. -->                                                                                                                                                                                                                                                                                                                                                                                 
      <xsl:otherwise>
        <xsl:value-of select="$f"/>
      </xsl:otherwise>
      
    </xsl:choose> 
  </xsl:template>
  
  
  <!-- Facet lookup (replace field names) -->
  <xsl:template name="facet-lookup-field">
    <xsl:param name="f"/> 
    <xsl:choose>
         
      <!-- Affiliation Country (187,636) -->
      <xsl:when test="matches($f,'affilctry')">
        <xsl:text>affilctry-f</xsl:text>
      </xsl:when>

      <!-- Affiliation Id (195,488) -->
      <xsl:when test="matches($f,'afid')">
        <xsl:text>afid-f</xsl:text>
      </xsl:when>
      
      <!-- Author Cite (1,125) -->
      <xsl:when test="matches($f,'aucite')">
        <xsl:text>aucite-f</xsl:text>
      </xsl:when>     

      <!-- Author Group Id (0) -->
      <xsl:when test="matches($f,'authgrpid')">
        <xsl:text>authgrpid-f</xsl:text>
      </xsl:when> 

      <!-- Author Id (64,711) -->
      <xsl:when test="matches($f,'authid')">
        <xsl:text>authid-f</xsl:text>
      </xsl:when> 
      
      <!-- Exact Keyword (186,621) -->
      <xsl:when test="matches($f,'exactkeyword')">
        <xsl:text>exactkeyword-f</xsl:text>
      </xsl:when> 

      <!-- Exact Source Title (189,573) -->
      <xsl:when test="matches($f,'exactsrctitle')">
        <xsl:text>exactsrctitle-f</xsl:text>
      </xsl:when> 

      <!-- Language (186,357) -->
      <xsl:when test="matches($f,'lang')">
        <xsl:text>lang-f</xsl:text>
      </xsl:when> 

      <!-- Preferred Name Author Id (188,368) -->
      <xsl:when test="matches($f,'prefnameauid')">
        <xsl:text>prefnameauid-f</xsl:text>
      </xsl:when> 

      <!-- Publication Year (200,427) -->
      <xsl:when test="matches($f,'pubyr')">
        <xsl:text>pubyr-f</xsl:text>
      </xsl:when>

      <!-- Source Type (186,343) -->
      <xsl:when test="matches($f,'srctype')">
        <xsl:text>srctype-f</xsl:text>
      </xsl:when>

      <!-- Status Type (463) -->
      <xsl:when test="matches($f,'statustype')">
        <xsl:text>statustype-f</xsl:text>
      </xsl:when>

      <!-- Subject Abbreviation (188,862) -->
      <xsl:when test="matches($f,'subjabbr')">
        <xsl:text>subjabbr-f</xsl:text>
      </xsl:when>

      <!-- Sub Type (187,916) -->
      <xsl:when test="matches($f,'subtype')">
        <xsl:text>subtype-f</xsl:text>
      </xsl:when>
                                                                         
      <!-- Assume we use the specified name as the field  -->  
      <!--  This should not happen. -->                                                                                                                                                                                                                                                                                                                                                                                
      <xsl:otherwise>
        <xsl:value-of select="$f"/>
      </xsl:otherwise>
      
    </xsl:choose> 
  </xsl:template>
  
  <!-- Word cleanup  -->
  <xsl:template name="word-cleanup">
    <xsl:param name="w"/> 
    <xsl:choose>
      
      <!-- Marked as a phrase so make it a phrase -->
      <xsl:when test="$w/@phrase='true'">
        <xsl:text>"</xsl:text>
        <xsl:value-of select="$w/text()"/>
        <xsl:text>"</xsl:text>
      </xsl:when>
      
      <!-- Just use the text (no need for a phrase)  -->
      <!-- Escape these characters for 'keyword' fields   + - & | ! ( ) { } [ ] ^ ~ : \ / " -->
      <xsl:otherwise>
        <xsl:variable name="var1">
          <xsl:call-template name="string-replace-all">
            <xsl:with-param name="text" select="$w/text()" />
            <xsl:with-param name="replace" select="'+'" />
            <xsl:with-param name="by" select="'\+'" />
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
            <xsl:with-param name="replace" select="'\'" />
            <xsl:with-param name="by" select="'\\'" />
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

