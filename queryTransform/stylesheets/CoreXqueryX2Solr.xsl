<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:ft="http://www.elsevier.com/2003/01/xqueryxFT-schema"
  xmlns:qw="http://webservices.elsevier.com/schemas/search/fast/types/v4" version="2.0">

  <!-- 
     
    	Stylesheet for transforming core queries into Solr syntax.
    
    	Limitations:
      		Navigator selection (AdjustmentGroup elements) are currently ignored.
      		Scope query not implemented
      		Proximity query not implemented
      		
        TODO:
        
          Implement basic proximity (no children AND nodes)
          	Surround Query Parser Limitations:
          		Multi-character wildcard (*) requires 3 leading characeters.  A* or AB* will not work (by default).
          		There is no analysis of the tokens.  In other words, no lower-casing, stemming, punctuation, etc.
          		Not sure edixmax will be suported (need to verify).
			
          May want to add -m for 'boundary' queries although none exist
      
     -->

  <!-- Turn off auto-insertion of <?xml> tag and set indenting on -->
  <xsl:output method="text" encoding="utf-8" indent="yes"/>

  <!-- strip whitespace from whitespace-only nodes -->
  <xsl:strip-space elements="*"/>

  <!-- Get the return fields -->
  <xsl:variable name="fields" select="/qw:search/qw:searchReqPayload/qw:reqFields"/>

  <!-- Get the start -->
  <xsl:variable name="start">
    <xsl:variable name="val" select="/qw:search/qw:searchReqPayload/qw:returnAttributes/@start"/>
    <xsl:choose>
      <xsl:when test="number($val) ge 0">
        <xsl:value-of select="$val"/>
      </xsl:when>
      <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- Get the maxResults -->
  <xsl:variable name="maxResults"
    select="/qw:search/qw:searchReqPayload/qw:returnAttributes/@maxResults"/>

  <!-- Get the orderBy fields -->
  <xsl:variable name="orderBys"
    select="/qw:search/qw:searchReqPayload/qw:orderByList/qw:orderByAttributes[@path=('affilctry','artnum','aucite','auth','datesort','fastloaddate','issue','itemtitle','numcitedby','pagecount','pg','pgfirst','pubyr','relevancy','srctitle','statustype','vol')]"/>

  <!-- Get the facet fields (what to do about scoundefinednav?) -->
  <xsl:variable name="facets"
    select="/qw:search/qw:searchReqPayload/qw:reqDimension[qw:navigator=('scoaffilctrynav','scoafidnav','scoaucitenav','scoauthgrpidnav','scoauthidnav','scoexactkeywordsnav','scoexactsrctitlenav','scolangnav','scoprefnameauidnav','scopubyrnav','scosrctypenav','scostatustypenav','scosubjabbrnav','scosubtypenav')]"/>

  <!-- Get the filter query -->
  <xsl:variable name="filter" select="/qw:search/qw:searchReqPayload/qw:elsfilter/ft:fullTextQuery"/>

  <!-- Get the param names -->
  <xsl:variable name="paramNames" select="//qw:viaParamsList/qw:paramName"/>

  <!-- Get the param values -->
  <xsl:variable name="paramValues" select="//qw:viaParamsList/qw:paramValue"/>

  <!-- Get the highlight indicator (we only ever highlight the abstract) -->
  <xsl:variable name="highlight">
    <xsl:for-each select="$paramNames">
      <xsl:if test="./text() = 'rt_highlight'">
        <xsl:variable name="tspos" select="position()"/>
        <xsl:value-of select="$paramValues[$tspos]"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>

  <!-- Build the query -->
  <xsl:template match="/qw:search/qw:searchReqPayload/qw:xQueryX">

    <!-- go build the query -->
    <xsl:text>q=</xsl:text>
    <xsl:apply-templates select="./ft:fullTextQuery/ft:query" mode="query"/>

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
      <xsl:apply-templates select="$filter/ft:query" mode="query"/>
    </xsl:if>

    <!-- highlight clause -->
    <xsl:if test="$highlight='on'">
      <xsl:text>&amp;hl=true&amp;hl.fl=abs</xsl:text>
    </xsl:if>

  </xsl:template>


  <!-- Query clause -->
  <xsl:template match="ft:query" mode="query">
    <xsl:apply-templates
      select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | proximityQuery"
    />
  </xsl:template>


  <!--  Prevent filter query from being processed twice -->
  <xsl:template match="ft:query"/>


  <!-- AND query clause -->
  <xsl:template match="ft:andQuery">
    <xsl:text>(</xsl:text>
    <xsl:for-each
      select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | proximityQuery">
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
    <xsl:for-each
      select="ft:word | ft:andQuery | ft:orQuery | ft:notQuery | ft:numericCompare | proximityQuery">
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
    <xsl:apply-templates
      select="ft:word | ft:andQuery | ft:orQuery | ft:numericCompare | proximityQuery"/>
    <xsl:text>)</xsl:text>
  </xsl:template>


  <!-- WORD query clause -->
  <xsl:template match="ft:word">
    <xsl:choose>
      <xsl:when
        test="string(./@path) = ('all','allmed','allsmall','itemtitle','keywords','abs','auth','srctitle','issn','coden','doi','isbn','lang','pub','chemname','affil','ed','corres','collab','confall')">
        <!--  Adjust query for field weighting -->
        <xsl:call-template name="field-weight-adjustment">
          <xsl:with-param name="f" select="string(./@path)"/>
          <xsl:with-param name="w" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <!--Adjust punct sensitive fields (if needed) -->
        <xsl:call-template name="word-lookup-field">
          <xsl:with-param name="f" select="string(./@path)"/>
          <xsl:with-param name="p" select="string(./@punct)"/>
        </xsl:call-template>
        <xsl:text>:</xsl:text>
        <!-- Go decide whether to cleanup the word (add quotes, escape characters, add trailing wildcard) -->
        <xsl:call-template name="word-cleanup">
          <xsl:with-param name="w" select="."/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- NUMERIC queryclause -->
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


  <!-- Field Weight Lookup (adjust field names)      			
    itemtitle (12x) .. also in all,allmed,allsmall
    keywords  (8x)  .. also in all,allmed,allsmall
    abs       (8x)  .. also in all,allmed,allsmall
    auth      (8x)  .. also in all,allmed
    srctitle  (2x)  .. also in all
    issn      (2x)  .. also in all
    coden     (2x)  .. also in all
    doi       (2x)  .. also in all
    isbn      (2x)  .. also in all
    lang      (2x)  .. also in all
    pub       (2x)  .. also in all
    chemname  (2x)  .. also in all
    affil     (2x)  .. also in all
    ed        (2x)  .. also in all
    corres    (2x)  .. also in all
    collab    (2x)  .. also in all
    confall   (2x)  .. also in all
  -->
  <xsl:template name="field-weight-adjustment">
    <xsl:param name="f"/>
    <xsl:param name="w"/>
    <xsl:choose>
      <xsl:when test="$f='itemtitle'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='itemtitle-p^12'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='itemtitle^12'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='keywords'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='keywords-p^8'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='keywords^8'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='abs'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='abs-p^8'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='abs^8'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='auth'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='auth-p^8'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='auth^8'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='srctitle'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='srctitle-p^2'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='srctitle^2'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='issn'">
        <xsl:text>_query_:"{!edismax qf='issn^2'}</xsl:text>
      </xsl:when>
      <xsl:when test="$f='coden'">
        <xsl:text>_query_:"{!edismax qf='coden^2'}</xsl:text>
      </xsl:when>
      <xsl:when test="$f='doi'">
        <xsl:text>_query_:"{!edismax qf='doi^2'}</xsl:text>
      </xsl:when>
      <xsl:when test="$f='isbn'">
        <xsl:text>_query_:"{!edismax qf='isbn^2'}</xsl:text>
      </xsl:when>
      <xsl:when test="$f='lang'">
        <xsl:text>_query_:"{!edismax qf='lang^2'}</xsl:text>
      </xsl:when>
      <xsl:when test="$f='pub'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='pub-p^2'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='pub^2'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='chemname'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='chemname-p^2'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='chemname^2'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='affil'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='affil-p^2'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='affil^2'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='ed'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='ed-p^2'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='ed^2'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='corres'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='corres-p^2'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='corres^2'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='collab'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='collab-p^2'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='collab^2'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='confall'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='confall-p^2'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='confall^2'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='all'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='all-p itemtitle-p^12 keywords-p^8 abs-p^8 auth-p^8 srctitle-p^2 issn^2 coden^2 doi^2 isbn^2 lang^2 pub-p^2 chemname-p^2 affil-p^2 ed-p^2 corres-p^2 collab-p^2 confall-p^2'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='all itemtitle^12 keywords^8 abs^8 auth^8 srctitle^2 issn^2 coden^2 doi^2 isbn^2 lang^2 pub^2 chemname^2 affil^2 ed^2 corres^2 collab^2 confall^2'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='allmed'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='allmed-p itemtitle-p^12 keywords-p^8 abs-p^8 auth-p^8'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='allmed itemtitle^12 keywords^8 abs^8 auth^8'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$f='allsmall'">
        <xsl:choose>
          <xsl:when test="$w[@punct='sensitive']">
            <xsl:text>_query_:"{!edismax qf='allsmall-p itemtitle-p^12 keywords-p^8 abs-p^8'}</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>_query_:"{!edismax qf='allsmall itemtitle^12 keywords^8 abs^8'}</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <!-- Should never happen. -->
      <xsl:otherwise/>
    </xsl:choose>
    <!-- Check if a phrase or punc sensitive -->
    <xsl:choose>
      <xsl:when test="$w[@phrase='true' or @punct='sensitive']">
        <xsl:text>\"</xsl:text>
        <xsl:call-template name="string-escape">
          <xsl:with-param name="str" select="$w/text()"/>
        </xsl:call-template>
        <xsl:text>\"</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="string-escape">
          <xsl:with-param name="str" select="$w/text()"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>"</xsl:text>
  </xsl:template>


  <!-- Word lookup (replace field names) -->
  <xsl:template name="word-lookup-field">
    <xsl:param name="f"/>
    <xsl:param name="p"/>
    <xsl:choose>

      <xsl:when test="$p='sensitive'">
        <xsl:choose>

          <!-- Abstract (36) -->
          <xsl:when test="matches($f,'^abs$')">
            <xsl:text>abs-p</xsl:text>
          </xsl:when>

          <!-- Affiliation (112) -->
          <xsl:when test="matches($f,'^affil$')">
            <xsl:text>affil-p</xsl:text>
          </xsl:when>

          <!-- Affiliation City (0) -->
          <xsl:when test="matches($f,'^affilcity$')">
            <xsl:text>affilcity-p</xsl:text>
          </xsl:when>

          <!-- Affiliation Country (0) -->
          <xsl:when test="matches($f,'^affilctry$')">
            <xsl:text>affilctry-p</xsl:text>
          </xsl:when>

          <!-- Affiliation Organization (4) -->
          <xsl:when test="matches($f,'^affilorg$')">
            <xsl:text>affilorg-p</xsl:text>
          </xsl:when>

          <!-- All (6317) -->
          <xsl:when test="matches($f,'^all$')">
            <xsl:text>all-p</xsl:text>
          </xsl:when>

          <!-- All Medium (190) -->
          <xsl:when test="matches($f,'^allmed$')">
            <xsl:text>allmed-p</xsl:text>
          </xsl:when>

          <!-- All Small (435) -->
          <xsl:when test="matches($f,'^allsmall$')">
            <xsl:text>allsmall-p</xsl:text>
          </xsl:when>

          <!-- Article Number (0) -->
          <xsl:when test="matches($f,'^artnum$')">
            <xsl:text>artnum-p</xsl:text>
          </xsl:when>

          <!-- Author Cite (0) -->
          <xsl:when test="matches($f,'^aucite$')">
            <xsl:text>aucite-p</xsl:text>
          </xsl:when>

          <!-- Author (185) -->
          <xsl:when test="matches($f,'^auth$')">
            <xsl:text>auth-p</xsl:text>
          </xsl:when>

          <!-- Author Email (0) -->
          <xsl:when test="matches($f,'^authemail$')">
            <xsl:text>authemail-p</xsl:text>
          </xsl:when>

          <!-- Author First Initial (0) -->
          <xsl:when test="matches($f,'^authfirstini$')">
            <xsl:text>authfirstini-p</xsl:text>
          </xsl:when>

          <!-- Author Index Name (0) -->
          <xsl:when test="matches($f,'^authidxname$')">
            <xsl:text>authidxname-p</xsl:text>
          </xsl:when>

          <!-- Author Keywords (0) -->
          <xsl:when test="matches($f,'^authkeywords$')">
            <xsl:text>authkeywords-p</xsl:text>
          </xsl:when>

          <!-- Author Last Name (0) -->
          <xsl:when test="matches($f,'^authlast$')">
            <xsl:text>authlast-p</xsl:text>
          </xsl:when>

          <!-- Author Suffix (0) -->
          <xsl:when test="matches($f,'^authsuff$')">
            <xsl:text>authsuff-p</xsl:text>
          </xsl:when>

          <!-- CAS Registry Number (0) -->
          <xsl:when test="matches($f,'^casregistrynum$')">
            <xsl:text>casregistrynum-p</xsl:text>
          </xsl:when>

          <!-- Chemical (0) -->
          <xsl:when test="matches($f,'^chem$')">
            <xsl:text>chem-p</xsl:text>
          </xsl:when>

          <!-- Chemical Information (0) -->
          <xsl:when test="matches($f,'^cheminfo$')">
            <xsl:text>cheminfo-p</xsl:text>
          </xsl:when>

          <!-- Chemical Name (0) -->
          <xsl:when test="matches($f,'^chemname$')">
            <xsl:text>chemname-p</xsl:text>
          </xsl:when>

          <!-- Collaboration (0) -->
          <xsl:when test="matches($f,'^collab$')">
            <xsl:text>collab-p</xsl:text>
          </xsl:when>

          <!-- Conference All (0) -->
          <xsl:when test="matches($f,'^confall$')">
            <xsl:text>confall-p</xsl:text>
          </xsl:when>

          <!-- Conference Code (0) -->
          <xsl:when test="matches($f,'^confcode$')">
            <xsl:text>confcode-p</xsl:text>
          </xsl:when>

          <!-- Conference Location (0) -->
          <xsl:when test="matches($f,'^confloc$')">
            <xsl:text>confloc-p</xsl:text>
          </xsl:when>

          <!-- Conference Name (0) -->
          <xsl:when test="matches($f,'^confname$')">
            <xsl:text>confname-p</xsl:text>
          </xsl:when>

          <!-- Conference Sponsor (0) -->
          <xsl:when test="matches($f,'^confsponsor$')">
            <xsl:text>confsponsor-p</xsl:text>
          </xsl:when>

          <!-- Copyright (0) -->
          <xsl:when test="matches($f,'^copyright$')">
            <xsl:text>copyright-p</xsl:text>
          </xsl:when>

          <!-- Correspondence (0) -->
          <xsl:when test="matches($f,'^corres$')">
            <xsl:text>corres-p</xsl:text>
          </xsl:when>

          <!-- Date Completed Text (0) -->
          <xsl:when test="matches($f,'^datecompletedtxt$')">
            <xsl:text>datecompletedtxt-p</xsl:text>
          </xsl:when>

          <!-- DOI (170)            -->
          <!-- -p field not needed  -->

          <!-- Editor (190) -->
          <xsl:when test="matches($f,'^ed$')">
            <xsl:text>ed-p</xsl:text>
          </xsl:when>

          <!-- Editor Address (0) -->
          <xsl:when test="matches($f,'^edaddress$')">
            <xsl:text>edaddress-p</xsl:text>
          </xsl:when>

          <!-- Editor First Initial (0) -->
          <xsl:when test="matches($f,'^edfirstini$')">
            <xsl:text>edfirstini-p</xsl:text>
          </xsl:when>

          <!-- Editor Index Name (0) -->
          <xsl:when test="matches($f,'^edidxname$')">
            <xsl:text>edidxname-p</xsl:text>
          </xsl:when>

          <!-- Edition (0) -->
          <xsl:when test="matches($f,'^edition$')">
            <xsl:text>edition-p</xsl:text>
          </xsl:when>

          <!-- Editor Last Name (0) -->
          <xsl:when test="matches($f,'^edlast$')">
            <xsl:text>edlast-p</xsl:text>
          </xsl:when>

          <!-- EID (15)             -->
          <!-- -p field not needed  -->

          <!-- First Author (0) -->
          <xsl:when test="matches($f,'^firstauth$')">
            <xsl:text>firstauth-p</xsl:text>
          </xsl:when>

          <!-- Funding Acronym (0) -->
          <xsl:when test="matches($f,'^fundacr$')">
            <xsl:text>fundacr-p</xsl:text>
          </xsl:when>

          <!-- Funding All (0) -->
          <xsl:when test="matches($f,'^fundall$')">
            <xsl:text>fundall-p</xsl:text>
          </xsl:when>

          <!-- Funding Number (0) -->
          <xsl:when test="matches($f,'^fundno$')">
            <xsl:text>fundno-p</xsl:text>
          </xsl:when>

          <!-- Funding Sponsor (0) -->
          <xsl:when test="matches($f,'^fundsponsor$')">
            <xsl:text>fundsponsor-p</xsl:text>
          </xsl:when>

          <!-- Index Terms (0) -->
          <xsl:when test="matches($f,'^idxterms$')">
            <xsl:text>idxterms-p</xsl:text>
          </xsl:when>

          <!-- Issue (0) -->
          <xsl:when test="matches($f,'^issue$')">
            <xsl:text>issue-p</xsl:text>
          </xsl:when>

          <!-- Item Title (371) -->
          <xsl:when test="matches($f,'^itemtitle$')">
            <xsl:text>itemtitle-p</xsl:text>
          </xsl:when>

          <!-- Keywords (49) -->
          <xsl:when test="matches($f,'^keywords$')">
            <xsl:text>keywords-p</xsl:text>
          </xsl:when>

          <!-- Manufacturer (0) -->
          <xsl:when test="matches($f,'^manuf$')">
            <xsl:text>manuf-p</xsl:text>
          </xsl:when>

          <!-- Part (0) -->
          <xsl:when test="matches($f,'^part$')">
            <xsl:text>part-p</xsl:text>
          </xsl:when>

          <!-- Patent Information (0) -->
          <xsl:when test="matches($f,'^patinfo$')">
            <xsl:text>patinfo-p</xsl:text>
          </xsl:when>

          <!-- PMID (1)             -->
          <!-- -p field not needed  -->

          <!-- Preferred Name Author Id (0) -->
          <xsl:when test="matches($f,'^prefnameauid$')">
            <xsl:text>prefnameauid-p</xsl:text>
          </xsl:when>

          <!-- Pub (0) -->
          <xsl:when test="matches($f,'^pub$')">
            <xsl:text>pub-p</xsl:text>
          </xsl:when>

          <!-- Pub Date Text (0) -->
          <xsl:when test="matches($f,'^pubdatetxt$')">
            <xsl:text>pubdatetxt-p</xsl:text>
          </xsl:when>

          <!-- Reference (0) -->
          <xsl:when test="matches($f,'^ref$')">
            <xsl:text>ref-p</xsl:text>
          </xsl:when>

          <!-- Reference Article Number (0) -->
          <xsl:when test="matches($f,'^refartnum$')">
            <xsl:text>refartnum-p</xsl:text>
          </xsl:when>

          <!-- Reference Source Title (1) -->
          <xsl:when test="matches($f,'^refsrctitle$')">
            <xsl:text>refsrctitle-p</xsl:text>
          </xsl:when>

          <!-- Reference Title (0) -->
          <xsl:when test="matches($f,'^reftitle$')">
            <xsl:text>reftitle-p</xsl:text>
          </xsl:when>

          <!-- SAUTH:AUTH (11)      -->
          <!-- Needs more thinking  -->

          <!-- Sequence Bank (0) -->
          <xsl:when test="matches($f,'^seqbank$')">
            <xsl:text>seqbank-p</xsl:text>
          </xsl:when>

          <!-- Sequence Number (0) -->
          <xsl:when test="matches($f,'^seqnumber$')">
            <xsl:text>seqnumber-p</xsl:text>
          </xsl:when>

          <!-- Source Title (18) -->
          <xsl:when test="matches($f,'^srctitle$')">
            <xsl:text>srctitle-p</xsl:text>
          </xsl:when>

          <!-- Source Title Abbreviation (0) -->
          <xsl:when test="matches($f,'^srctitleabbr$')">
            <xsl:text>srctitleabbr-p</xsl:text>
          </xsl:when>

          <!-- Subject Terms (0) -->
          <xsl:when test="matches($f,'^subjterms$')">
            <xsl:text>subjterms-p</xsl:text>
          </xsl:when>

          <!-- Supplement (0) -->
          <xsl:when test="matches($f,'^supplement$')">
            <xsl:text>supplement-p</xsl:text>
          </xsl:when>

          <!-- Trade Names (0) -->
          <xsl:when test="matches($f,'^tradenames$')">
            <xsl:text>tradenames-p</xsl:text>
          </xsl:when>

          <!-- Volume (0) -->
          <xsl:when test="matches($f,'^vol$')">
            <xsl:text>vol-p</xsl:text>
          </xsl:when>

          <!-- Website (0) -->
          <xsl:when test="matches($f,'^website$')">
            <xsl:text>website-p</xsl:text>
          </xsl:when>

          <!-- Assume we use the specified name as the field. -->
          <!-- This will happen for DOI, EID, and PMID.       -->
          <xsl:otherwise>
            <xsl:value-of select="$f"/>
          </xsl:otherwise>

        </xsl:choose>
      </xsl:when>

      <!-- Assume we use the specified name as the field  -->
      <xsl:otherwise>
        <xsl:value-of select="$f"/>
      </xsl:otherwise>

    </xsl:choose>
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
      <xsl:when test="matches($f,'^aucite$')">
        <xsl:text>aucite-s</xsl:text>
      </xsl:when>

      <!-- Author (2) -->
      <xsl:when test="matches($f,'^auth$')">
        <xsl:text>auth-s</xsl:text>
      </xsl:when>

      <!-- Datesort (1475970) -->
      <xsl:when test="matches($f,'^datesort$')">
        <xsl:text>datesort-s</xsl:text>
      </xsl:when>

      <!-- FAST Load Date (2456) -->
      <xsl:when test="matches($f,'^fastloaddate$')">
        <xsl:text>fastloaddate-s</xsl:text>
      </xsl:when>

      <!-- Issue (0) -->
      <xsl:when test="matches($f,'^issue$')">
        <xsl:text>issue-s</xsl:text>
      </xsl:when>

      <!-- Item Title (1102) -->
      <xsl:when test="matches($f,'^itemtitle$')">
        <xsl:text>itemtitle-s</xsl:text>
      </xsl:when>

      <!-- Number Cited By (141814) -->
      <xsl:when test="matches($f,'^numcitedby$')">
        <xsl:text>numcitedby-s</xsl:text>
      </xsl:when>

      <!-- Page Count (0) -->
      <xsl:when test="matches($f,'^pagecount$')">
        <xsl:text>pagecount-s</xsl:text>
      </xsl:when>

      <!-- Pages (0) -->
      <xsl:when test="matches($f,'^pg$')">
        <xsl:text>pg-s</xsl:text>
      </xsl:when>

      <!-- Page (First) (0) -->
      <xsl:when test="matches($f,'^pgfirst$')">
        <xsl:text>pgfirst-s</xsl:text>
      </xsl:when>

      <!-- Publication Year (20374) -->
      <xsl:when test="matches($f,'^pubyr$')">
        <xsl:text>pubyr-s</xsl:text>
      </xsl:when>

      <!-- Relevancy (1326725) -->
      <xsl:when test="matches($f,'^relevancy$')">
        <xsl:text>score</xsl:text>
      </xsl:when>

      <!-- Source Title (0) -->
      <xsl:when test="matches($f,'^srctitle$')">
        <xsl:text>srctitle-s</xsl:text>
      </xsl:when>

      <!-- Status Type (0) -->
      <xsl:when test="matches($f,'^statustype$')">
        <xsl:text>statustype-s</xsl:text>
      </xsl:when>

      <!-- Volume (0) -->
      <xsl:when test="matches($f,'^vol$')">
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
      <xsl:when test="matches($f,'^scoaffilctrynav$')">
        <xsl:text>affilctry-f</xsl:text>
      </xsl:when>

      <!-- Affiliation Id (195,488) -->
      <xsl:when test="matches($f,'^scoafidnav$')">
        <xsl:text>afid-f</xsl:text>
      </xsl:when>

      <!-- Author Cite (1,125) -->
      <xsl:when test="matches($f,'^scoaucitenav$')">
        <xsl:text>aucite-f</xsl:text>
      </xsl:when>

      <!-- Author Group Id (0) -->
      <xsl:when test="matches($f,'^scoauthgrpidnav$')">
        <xsl:text>authgrpid-f</xsl:text>
      </xsl:when>

      <!-- Author Id (64,711) -->
      <xsl:when test="matches($f,'^scoauthidnav$')">
        <xsl:text>authid-f</xsl:text>
      </xsl:when>

      <!-- Exact Keyword (186,621) -->
      <xsl:when test="matches($f,'^scoexactkeywordsnav$')">
        <xsl:text>exactkeyword-f</xsl:text>
      </xsl:when>

      <!-- Exact Source Title (189,573) -->
      <xsl:when test="matches($f,'^scoexactsrctitlenav$')">
        <xsl:text>exactsrctitle-f</xsl:text>
      </xsl:when>

      <!-- Language (186,357) -->
      <xsl:when test="matches($f,'scolangnav$')">
        <xsl:text>lang-f</xsl:text>
      </xsl:when>

      <!-- Preferred Name Author Id (188,368) -->
      <xsl:when test="matches($f,'^scoprefnameauidnav$')">
        <xsl:text>prefnameauid-f</xsl:text>
      </xsl:when>

      <!-- Publication Year (200,427) -->
      <xsl:when test="matches($f,'^scopubyrnav$')">
        <xsl:text>pubyr-f</xsl:text>
      </xsl:when>

      <!-- Source Type (186,343) -->
      <xsl:when test="matches($f,'^scosrctypenav$')">
        <xsl:text>srctype-f</xsl:text>
      </xsl:when>

      <!-- Status Type (463) -->
      <xsl:when test="matches($f,'^scostatustypenav$')">
        <xsl:text>statustype-f</xsl:text>
      </xsl:when>

      <!-- Subject Abbreviation (188,862) -->
      <xsl:when test="matches($f,'^scosubjabbrnav$')">
        <xsl:text>subjabbr-f</xsl:text>
      </xsl:when>

      <!-- Sub Type (187,916) -->
      <xsl:when test="matches($f,'^scosubtypenav$')">
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
  <!-- TODO add another param to indicate edismax -->
  <xsl:template name="word-cleanup">
    <xsl:param name="w"/>
    <xsl:choose>

      <!-- Marked as a phrase (or punct=sensitive) so make it a phrase -->
      <!-- TODO Must minimally escape the " -->
      <xsl:when test="$w[@phrase='true' or @punct='sensitive']">
        <xsl:text>"</xsl:text>
        <xsl:value-of select="$w/text()"/>
        <xsl:text>"</xsl:text>
      </xsl:when>

      <!-- Just use the text (no need for a phrase)  -->
      <!-- Escape these characters for 'keyword' fields   + - & | ! ( ) { } [ ] ^ ~ : \ / " -->
      <xsl:otherwise>
        <xsl:call-template name="string-escape">
          <xsl:with-param name="str" select="$w/text()"/>
        </xsl:call-template>
      </xsl:otherwise>

    </xsl:choose>
  </xsl:template>

  <xsl:template name="string-escape">
    <xsl:param name="str"/>
    <xsl:variable name="var1">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$str"/>
        <xsl:with-param name="replace" select="'\'"/>
        <xsl:with-param name="by" select="'\\'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var2">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var1"/>
        <xsl:with-param name="replace" select="'-'"/>
        <xsl:with-param name="by" select="'\-'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var3">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var2"/>
        <xsl:with-param name="replace" select="'&amp;'"/>
        <xsl:with-param name="by" select="'\&amp;'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var4">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var3"/>
        <xsl:with-param name="replace" select="'|'"/>
        <xsl:with-param name="by" select="'\|'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var5">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var4"/>
        <xsl:with-param name="replace" select="'!'"/>
        <xsl:with-param name="by" select="'\!'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var6">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var5"/>
        <xsl:with-param name="replace" select="'('"/>
        <xsl:with-param name="by" select="'\('"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var7">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var6"/>
        <xsl:with-param name="replace" select="')'"/>
        <xsl:with-param name="by" select="'\)'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var8">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var7"/>
        <xsl:with-param name="replace" select="'{'"/>
        <xsl:with-param name="by" select="'\{'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var9">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var8"/>
        <xsl:with-param name="replace" select="'}'"/>
        <xsl:with-param name="by" select="'\}'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var10">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var9"/>
        <xsl:with-param name="replace" select="'['"/>
        <xsl:with-param name="by" select="'\['"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var11">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var10"/>
        <xsl:with-param name="replace" select="']'"/>
        <xsl:with-param name="by" select="'\]'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var12">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var11"/>
        <xsl:with-param name="replace" select="'^'"/>
        <xsl:with-param name="by" select="'\^'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var13">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var12"/>
        <xsl:with-param name="replace" select="'~'"/>
        <xsl:with-param name="by" select="'\~'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var14">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var13"/>
        <xsl:with-param name="replace" select="':'"/>
        <xsl:with-param name="by" select="'\:'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var15">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var14"/>
        <xsl:with-param name="replace" select="'+'"/>
        <xsl:with-param name="by" select="'\+'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var16">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var15"/>
        <xsl:with-param name="replace" select="'/'"/>
        <xsl:with-param name="by" select="'\/'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="var17">
      <xsl:call-template name="string-replace-all">
        <xsl:with-param name="text" select="$var16"/>
        <xsl:with-param name="replace" select="'&quot;'"/>
        <xsl:with-param name="by" select="'\&quot;'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:value-of select="$var17"/>
  </xsl:template>

  <xsl:template name="string-replace-all">
    <xsl:param name="text"/>
    <xsl:param name="replace"/>
    <xsl:param name="by"/>
    <xsl:choose>
      <xsl:when test="contains($text, $replace)">
        <xsl:value-of select="substring-before($text,$replace)"/>
        <xsl:value-of select="$by"/>
        <xsl:call-template name="string-replace-all">
          <xsl:with-param name="text" select="substring-after($text,$replace)"/>
          <xsl:with-param name="replace" select="$replace"/>
          <xsl:with-param name="by" select="$by"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Ingore what we didn't explicitly ask for -->
  <xsl:template match="text()"/>


</xsl:stylesheet>
