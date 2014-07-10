package com.elsevier.solr.queryparser;


import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.Version;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

import com.searchtechnologies.solr.queryoperators.SpanAndQuery;
import com.searchtechnologies.solr.queryoperators.SpanBetweenQuery;


/**
 * Create a scope query.
 * 
 * Assume the following 'author' documents have been loaded:
 *  <ce:authors><ce:author><ce:surname>jones</ce:surname><ce:given-name>jeff</ce:given-name></ce:author>jeff</ce:authors>
 *  <ce:authors><ce:author><ce:surname>jeff</ce:surname><ce:given-name>jones</ce:given-name></ce:author>jones</ce:authors>
 *  <ce:authors><ce:author><ce:surname>McBeath</ce:surname><ce:given-name>Darin William</ce:given-name></ce:author><ce:author><ce:surname>Fulford</ce:surname><ce:given-name>Darby</ce:given-name></ce:author><ce:author><ce:surname>McBeath</ce:surname><ce:given-name>Darby</ce:given-name></ce:author></ce:authors>
 *  <ce:authors><ce:author><ce:surname>McBeath</ce:surname><ce:given-name>Darin</ce:given-name></ce:author><ce:author><ce:surname>Fulford</ce:surname><ce:given-name>Darin</ce:given-name></ce:author></ce:authors>
 *  <ce:authors><ce:author><ce:surname>Johnson-Smith</ce:surname><ce:given-name>Joe</ce:given-name>Ohio</ce:author></ce:authors>
 *
 * And these 'author' documents were transformed with:     
 *  <fieldType name="authorScope" class="solr.TextField">
 *     <analyzer>
 *       <charFilter class="solr.MappingCharFilterFactory" mapping="mapping-uppertolowercase.txt"/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt;/? *[a-zA-Z]*:?(bold|sup|inf|hsp|vsp) *>" replacement=""/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt; *[a-zA-Z]*:?authors *>" replacement=" BAUTHORS "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt;/ *[a-zA-Z]*:?authors *>" replacement=" EAUTHORS "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt; *[a-zA-Z]*:?author *>" replacement=" BAUTHOR "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt;/ *[a-zA-Z]*:?author *>" replacement=" EAUTHOR "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt; *[a-zA-Z]*:?surname *>" replacement=" BLNAME "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt;/ *[a-zA-Z]*:?surname *>" replacement=" ELNAME "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt; *[a-zA-Z]*:?given-name *>" replacement=" BFNAME "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt;/ *[a-zA-Z]*:?given-name *>" replacement=" EFNAME "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt; *[a-zA-Z]*:?email *>" replacement=" BEMAIL "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt;/ *[a-zA-Z]*:?email *>" replacement=" EEMAIL "/>
 *       <charFilter class="solr.HTMLStripCharFilterFactory"/>
 *       <tokenizer class="solr.ClassicTokenizerFactory"/>
 *       <filter class="solr.ClassicFilterFactory"/>
 *       <filter class="solr.ASCIIFoldingFilterFactory"/>
 *     </analyzer>
 *   </fieldType>
 *   
 * Assume the following 'affiliation' documents were loaded:
 *  <ce:affiliiations><ce:affiliation><ce:city>columbus, ohio</ce:city><ce:country>united states</ce:country><ce:organization>ohio state university</ce:organization></ce:affiliation></ce:affiliiations>
 *  <ce:affiliiations><ce:affiliation><ce:city>columbus</ce:city><ce:country>mexico</ce:country><ce:organization>ohio state university</ce:organization></ce:affiliation></ce:affiliiations>
 * 
 * And these 'affiliation' documents were transformed with:
 *  <fieldType name="affiliationScope" class="solr.TextField">
 *     <analyzer>
 *       <charFilter class="solr.MappingCharFilterFactory" mapping="mapping-uppertolowercase.txt"/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt;/? *[a-zA-Z]*:?(bold|sup|inf|hsp|vsp) *>" replacement=""/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt; *[a-zA-Z]*:?affiliations *>" replacement=" BAFFILIATIONS "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt;/ *[a-zA-Z]*:?affiliations *>" replacement=" EAFFILIATIONS "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt; *[a-zA-Z]*:?affiliation *>" replacement=" BAFFILIATION "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt;/ *[a-zA-Z]*:?affiliation *>" replacement=" EAFFILIATION "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt; *[a-zA-Z]*:?city *>" replacement=" BCITY "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt;/ *[a-zA-Z]*:?city *>" replacement=" ECITY "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt; *[a-zA-Z]*:?country *>" replacement=" BCOUNTRY "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt;/ *[a-zA-Z]*:?country *>" replacement=" ECOUNTRY "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt; *[a-zA-Z]*:?organization *>" replacement=" BORGANIZATION "/>
 *       <charFilter class="solr.PatternReplaceCharFilterFactory" pattern="&lt;/ *[a-zA-Z]*:?organization *>" replacement=" EORGANIZATION "/>
 *       <charFilter class="solr.HTMLStripCharFilterFactory"/>
 *       <tokenizer class="solr.ClassicTokenizerFactory"/>
 *       <filter class="solr.ClassicFilterFactory"/>
 *       <filter class="solr.ASCIIFoldingFilterFactory"/>
 *     </analyzer>
 *   </fieldType>
 * 
 * Examples:
 * 
 * Not matching a specific author: 
 * 	 Works as expected {!scope field='authscope' query='fname:Darin AND lname:McBeath'}
 * 	 Fails with 500RC  {!scope field='authscope' query='fname:Darin AND fname:Darby AND fname:jeff'}
 * 	 Works as expected {!scope field='authscope' query='(fname:Darin AND fname:Darby) AND fname:jeff'}
 *	 Works as expected {!scope field='authscope' query='fname:Darin AND fname:Darby AND fname:junk'}
 *   Works as expected {!scope field='authscope' query='fname:(Darin AND Darby)'}
 *   Works as expected {!scope field='authscope' query='fname:(Darin Darby)'}
 * 
 * Matching a specific author:
 *   Works as expected {!scope field='authscope' query='scope:author(fname:(Darin AND Darby) AND lname:mcbeath)'} 
 *   Works as expected {!scope field='authscope' query='scope:author(fname:(Darin OR Darby) AND lname:mcbeath)'}
 *   Works as expected {!scope field='authscope' query='scope:author(fname:"darin william" AND lname:mcbeath)'}
 *   Works as expected {!scope field='authscope' query='scope:author(fname:da?in AND lname:fulford)'}
 *   Works as expected {!scope field='authscope' query='scope:author(fname:dar* AND lname:mcbeath)'}
 *   
 * More complex author queries:  
 *   Works as expected {!scope field='authscope' query='scope:author(fname:dar* AND lname:mcbeath) OR jones'}
 *   Fails with 500RC  {!scope field='authscope' query='scope:author(fname:dar* AND lname:mcbeath) AND jones'}
 *   
 * Not matching a specific affiliation:   
 *   Works as expected {!scope field='affscope' query='city:ohio'}
 *   Works as expected {!scope field='affscope' query='ohio'}
 *   Works as expected {!scope field='affscope' query='org:state OR ctry:mexico'}
 *   Works as expected {!scope field='affscope' query='org:state AND ctry:mexico'}
 *   
 * Matching a specific affiliation:
 *   
 * Default operator is AND
 * Leading, Embedded, and Trailing wildcards are supported
 * TODO Wildcards in phrases are not supported
 * TODO NOT operator is implemented but does not appear to work correctly
 * 
 * 
 */
public class ScopeQParserPlugin extends QParserPlugin{

	  // Name for the Plugin
	  public static final String NAME = "scope";
	  
	  // Identifies the field for scoping and the query
	  private static final String SCOPE_FIELD = "field";
	  private static final String QUERY_FIELD = "query";
	  	  
	  // Possible values for the field to scope
	  private static final String AUTHOR_SCOPE_FIELD = "authscope";
	  private static final String AFFILIATION_SCOPE_FIELD = "affscope";
	  private static final String REFERENCE_SCOPE_FIELD = "refscope";
	  
	  // Subscope field name
	  private static final String SUBSCOPE_FIELD = "scope";

	  // Default fields
	  private static final HashMap<String, String> DEFAULT_FIELD = new HashMap<String, String>(){
	        {
	            put(AUTHOR_SCOPE_FIELD, "author");
	            put(AFFILIATION_SCOPE_FIELD, "affiliation");
	            put(REFERENCE_SCOPE_FIELD, "reference");
	        }
	  };
	  
	  // Author Scope Fields (specified in the query)
	  private static final String AUTHOR_FIELD = "author";
	  private static final String AUTHOR_FIRST_NAME_FIELD = "fname";
	  private static final String AUTHOR_LAST_NAME_FIELD = "lname";
	  private static final String AUTHOR_EMAIL_FIELD = "email";
	  
	  // Author tokens inserted into content when indexing (begin tag and end tag).
	  private static final HashMap<String, String> AUTHOR_BEGIN_TAG = new HashMap<String, String>(){
	        {
	            put(AUTHOR_FIELD, "BAUTHOR");
	            put(AUTHOR_FIRST_NAME_FIELD, "BFNAME");
	            put(AUTHOR_LAST_NAME_FIELD, "BLNAME");
	            put(AUTHOR_EMAIL_FIELD, "BEMAIL");
	        }
	  };
	  private static final HashMap<String, String> AUTHOR_END_TAG = new HashMap<String, String>(){
	        {
	            put(AUTHOR_FIELD, "EAUTHOR");
	            put(AUTHOR_FIRST_NAME_FIELD, "EFNAME");
	            put(AUTHOR_LAST_NAME_FIELD, "ELNAME");
	            put(AUTHOR_EMAIL_FIELD, "EEMAIL");
	        }
	  };	
	  
	  
	  // Affiliation Scope Fields (specified in the query)
	  private static final String AFFILIATION_FIELD = "affiliation";
	  private static final String AFFILIATION_CITY_FIELD = "city";
	  private static final String AFFILIATION_COUNTRY_FIELD = "ctry";
	  private static final String AFFILIATION_ORGANIZATION_FIELD = "org";
	  
	  // Affiliation tokens inserted into content when indexing (begin tag and end tag).
	  private static final HashMap<String, String> AFFILIATION_BEGIN_TAG = new HashMap<String, String>(){
	        {
	            put(AFFILIATION_FIELD, "BAFFILIATION");
	            put(AFFILIATION_CITY_FIELD, "BCITY");
	            put(AFFILIATION_COUNTRY_FIELD, "BCOUNTRY");
	            put(AFFILIATION_ORGANIZATION_FIELD, "BORGANIZATION");
	        }
	  };
	  private static final HashMap<String, String> AFFILIATION_END_TAG = new HashMap<String, String>(){
	        {
	            put(AFFILIATION_FIELD, "EAFFILIATION");
	            put(AFFILIATION_CITY_FIELD, "ECITY");
	            put(AFFILIATION_COUNTRY_FIELD, "ECOUNTRY");
	            put(AFFILIATION_ORGANIZATION_FIELD, "EORGANIZATION");
	        }
	  };	  


	  // Reference Scope Fields (specified in the query)
	  private static final String REFERENCE_FIELD = "reference";
	  private static final String REFERENCE_EID_FIELD = "eid";
	  private static final String REFERENCE_AUTHOR_FIELD = "author";
	  private static final String REFERENCE_AUTHOR_FIRST_NAME_FIELD = "fname";
	  private static final String REFERENCE_AUTHOR_LAST_NAME_FIELD = "lname";
	  private static final String REFERENCE_TITLE_FIELD = "title";
	  private static final String REFERENCE_LANGUAGE_FIELD = "lang";
	  private static final String REFERENCE_SOURCE_TITLE_FIELD = "srctitle";
	  private static final String REFERENCE_PUB_YEAR_FIELD = "pubyr";
	  private static final String REFERENCE_PUB_YEAR_TEXT_FIELD = "pubyrtxt";
	  private static final String REFERENCE_PAGE_FIELD = "pg";
	  private static final String REFERENCE_PAGE_FIRST_FIELD = "pgfirst";
	  private static final String REFERENCE_ARTILCE_NUMBER_FIELD = "artnum";
	  private static final String REFERENCE_SCOPUS_ID_FIELD = "scp";
	  private static final String REFERENCE_AUTHOR_ID_FIELD = "authid";
	  
	  // Reference tokens inserted into content when indexing (begin tag and end tag).
	  private static final HashMap<String, String> REFERENCE_BEGIN_TAG = new HashMap<String, String>(){
	        {
	            put(REFERENCE_FIELD, "BREFERENCE");
	            put(REFERENCE_EID_FIELD, "BEID");
	            put(REFERENCE_AUTHOR_FIELD, "BAUTHOR");
	            put(REFERENCE_AUTHOR_FIRST_NAME_FIELD, "BFNAME");
	            put(REFERENCE_AUTHOR_LAST_NAME_FIELD, "BLNAME");
	            put(REFERENCE_TITLE_FIELD, "BTITLE");
	            put(REFERENCE_LANGUAGE_FIELD, "BLANG");
	            put(REFERENCE_SOURCE_TITLE_FIELD, "BSRCTITLE");
	            put(REFERENCE_PUB_YEAR_FIELD, "BPUBYR");
	            put(REFERENCE_PUB_YEAR_TEXT_FIELD, "BPUBYRTXT");
	            put(REFERENCE_PAGE_FIELD, "BPG");
	            put(REFERENCE_PAGE_FIRST_FIELD, "BPGFIRST");
	            put(REFERENCE_ARTILCE_NUMBER_FIELD, "BARTNUM");
	            put(REFERENCE_SCOPUS_ID_FIELD, "BSCP");
	            put(REFERENCE_AUTHOR_ID_FIELD, "BAUTHID");
	        }
	  };
	  private static final HashMap<String, String> REFERENCE_END_TAG = new HashMap<String, String>(){
	        {
	            put(REFERENCE_FIELD, "EREFERENCE");
	            put(REFERENCE_EID_FIELD, "EEID");
	            put(REFERENCE_AUTHOR_FIELD, "EAUTHOR");
	            put(REFERENCE_AUTHOR_FIRST_NAME_FIELD, "EFNAME");
	            put(REFERENCE_AUTHOR_LAST_NAME_FIELD, "ELNAME");
	            put(REFERENCE_TITLE_FIELD, "ETITLE");
	            put(REFERENCE_LANGUAGE_FIELD, "ELANG");
	            put(REFERENCE_SOURCE_TITLE_FIELD, "ESRCTITLE");
	            put(REFERENCE_PUB_YEAR_FIELD, "EPUBYR");
	            put(REFERENCE_PUB_YEAR_TEXT_FIELD, "EPUBYRTXT");
	            put(REFERENCE_PAGE_FIELD, "EPG");
	            put(REFERENCE_PAGE_FIRST_FIELD, "EPGFIRST");
	            put(REFERENCE_ARTILCE_NUMBER_FIELD, "EARTNUM");
	            put(REFERENCE_SCOPUS_ID_FIELD, "ESCP");
	            put(REFERENCE_AUTHOR_ID_FIELD, "EAUTHID");
	        }
	  };

	  
	  // Enumeration for tag types
	  private static  enum TAG_TYPE { BEG_TAG, END_TAG };
	  
	  // CharSequences
	  private static CharSequence singleCs = "?";
	  private static CharSequence multiCs = "*";
	  
	  /**
	   * Get the begin or end tag for the specified scope, field, and type.
	   * 
	   * @param scope
	   * @param field
	   * @param type
	   * @return tag
	   */
	  private String getTag(String scope, String field, TAG_TYPE type) {
		  
		  String tag = null;
		  
		  if (scope.compareTo(AUTHOR_SCOPE_FIELD) == 0) {
			  
			  if (type == TAG_TYPE.BEG_TAG) {
				  tag = AUTHOR_BEGIN_TAG.get(field);
			  } else if (type == TAG_TYPE.END_TAG) {
				  tag = AUTHOR_END_TAG.get(field);
			  } 
			  
		  } else if (scope.compareTo(AFFILIATION_SCOPE_FIELD) == 0) {
			  
			  if (type == TAG_TYPE.BEG_TAG) {
				  tag = AFFILIATION_BEGIN_TAG.get(field);
			  } else if (type == TAG_TYPE.END_TAG) {
				  tag = AFFILIATION_END_TAG.get(field);
			  } 
			  
		  } else if (scope.compareTo(REFERENCE_SCOPE_FIELD) == 0) {

			  if (type == TAG_TYPE.BEG_TAG) {
				  tag = REFERENCE_BEGIN_TAG.get(field);
			  } else if (type == TAG_TYPE.END_TAG) {
				  tag = REFERENCE_END_TAG.get(field);
			  } 
			  
		  }
		  
		  if (tag == null) {
			  tag = "INVALIDSCOPEFIELDTYPE";
		  }
		  
		  return tag;
		  
	  }
	  
	  @Override
	  public void init(NamedList args) {
	  }

	  
	  @Override
	  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
	    return new QParser(qstr, localParams, params, req) {
	      @Override
	      public Query parse() {
	    	  
	    	  String scope = localParams.get(SCOPE_FIELD);	    	  
	    	  System.out.println("*** " + SCOPE_FIELD + " is '" + scope + "'.");
	    	  
	    	  String query = localParams.get(QUERY_FIELD);	    	  
	    	  System.out.println("*** " + QUERY_FIELD + " is '" + query + "'.");
	    	  
	    	  // Get the analyzer
			  Analyzer analyzer = req.getSchema().getQueryAnalyzer();
			  QueryParser parser = new QueryParser(Version.LUCENE_47, DEFAULT_FIELD.get(scope), analyzer);
			  parser.setAllowLeadingWildcard(true);
			  parser.setDefaultOperator(Operator.AND);
			  
			  SpanQuery spanQuery = null;
			  try {
				Query q = parser.parse(query);
				spanQuery = buildQuery(scope, q);
			  } catch (ParseException e) {
				// Auto-generated catch block
				e.printStackTrace();
			  }
	
			  return spanQuery;
			  
	      }
	    };
	  }
	  
	  
	  /**
	   * Construct the query (using spans).  If there are 'boolean' clauses contained in the Query, the
	   * recursive buildQuery_recursive method will be leveraged.
	   * 
	   * @param scope Author, affiliation, or Reference
	   * @param q Query
	   * @return SpanQuery
	   */
	  private SpanQuery buildQuery(String scope, Query q) {
		  
		  SpanQuery spanQuery = null;
		  ArrayList<SpanQuery> spans = null;
		  
		  if( q.getClass() == BooleanQuery.class ){
			  
			  System.out.println("BooleanQuery clause found.");			  
			  spanQuery = buildQuery_recursive(q, scope, 0, new ArrayList<SpanQuery>(), new ArrayList<SpanQuery>(), new ArrayList<SpanQuery>());
			  
		  } else if (q.getClass() == TermQuery.class) {
			  
			  spanQuery = buildTermQuery(scope, q);
			  
		  } else if (q.getClass() == WildcardQuery.class) {
			  
			  spanQuery = buildWildcardQuery(scope, q);
			  
		  } else if (q.getClass() == PrefixQuery.class) {
			  
			  spanQuery = buildPrefixQuery(scope, q);
			  
		  } else if (q.getClass() == PhraseQuery.class) {
			  
			  spanQuery = buildPhraseQuery(scope, q);
			  
		  } else {

			  System.out.println("[" +q.getClass()+"]");
		  }
		  
		  if (spanQuery != null) {
			  
			  return spanQuery;
			  
		  } else {
			  
			  System.out.println("Failed to create a span query.");
			  return null;
			  
		  }
		  
	  }

	  
	  /**
	   * Construct the query (using spans).  This method will be called recursively.
	   * 
	   * @param q Query
	   * @param scope Author, affiliation, or Reference
	   * @param level Used only for formatting (indentation) the level of recursion
	   * @param andSpans ArrayList of Spans that should be 'and'
	   * @param orSpans ArrayList of Spans that should be 'or'
	   * @param notSpans ArrayList of Spans that should be 'not'
	   * @return SpanQuery
	   */
	  private SpanQuery buildQuery_recursive(Query q, String scope, int level, ArrayList<SpanQuery> andSpans, ArrayList<SpanQuery> orSpans, ArrayList<SpanQuery> notSpans) {

		  BooleanQuery castQuery = (BooleanQuery)q;
		  String subscope = null;		  
		  
		  for(BooleanClause clause : castQuery.getClauses() ){
			  
			  Class queryclazz = clause.getQuery().getClass();

			  System.out.println(repeat(' ', level) + "["+queryclazz+"][" + clause.getOccur() + "] " + clause.toString());

			  if(queryclazz == BooleanQuery.class) {
				  
				  System.out.println("Number of Clauses is " + castQuery.clauses().size());
				  System.out.println("Minimum Number to Match is " + castQuery.getMinimumNumberShouldMatch());
				  
				  if (subscope == null) {
					  
					  if (clause.getOccur() == BooleanClause.Occur.MUST) {
						  andSpans.add(buildQuery_recursive(clause.getQuery(), scope, level+1, new ArrayList<SpanQuery>(), new ArrayList<SpanQuery>(), new ArrayList<SpanQuery>()));
					  } else if (clause.getOccur() == BooleanClause.Occur.SHOULD) {
						  orSpans.add(buildQuery_recursive(clause.getQuery(), scope, level+1, new ArrayList<SpanQuery>(), new ArrayList<SpanQuery>(), new ArrayList<SpanQuery>()));
					  } else if (clause.getOccur() == BooleanClause.Occur.MUST_NOT) {
						  //FIX
						  notSpans.add(buildQuery_recursive(clause.getQuery(), scope, level+1, new ArrayList<SpanQuery>(), new ArrayList<SpanQuery>(), new ArrayList<SpanQuery>()));
					  }
					  
				  } else {
					  
					  ArrayList<SpanQuery> subscopeQuery = new ArrayList<SpanQuery>();
					  subscopeQuery.add(new SpanTermQuery(new Term(scope, getTag(scope, subscope, TAG_TYPE.BEG_TAG))));
					  subscopeQuery.add(new SpanTermQuery(new Term(scope, getTag(scope, subscope, TAG_TYPE.END_TAG))));
					  subscopeQuery.add(buildQuery_recursive(clause.getQuery(), scope, level+1, new ArrayList<SpanQuery>(), new ArrayList<SpanQuery>(), new ArrayList<SpanQuery>()));
					  if (clause.getOccur() == BooleanClause.Occur.MUST) {
						  andSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  } else if (clause.getOccur() == BooleanClause.Occur.SHOULD) {
						  orSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  } else if (clause.getOccur() == BooleanClause.Occur.MUST_NOT) {
						  //FIX
						  notSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  }
					  
				  }
				  
			  } else if (queryclazz == TermQuery.class) {
				  
				  TermQuery tq = (TermQuery)clause.getQuery();
				  
				  if (tq.getTerm().field().compareTo(SUBSCOPE_FIELD) == 0) {
					  
					  // Set the subscope
					  subscope = tq.getTerm().text();
					  
					  // Need to add a term here (otherwise we have problems)
					  WildcardQuery wildcard = new WildcardQuery(new Term(scope, "*"));
					  if (clause.getOccur() == BooleanClause.Occur.MUST) {
						  andSpans.add(new SpanMultiTermQueryWrapper<WildcardQuery>(wildcard));
					  } else if (clause.getOccur() == BooleanClause.Occur.SHOULD) {
						  orSpans.add(new SpanMultiTermQueryWrapper<WildcardQuery>(wildcard));
					  } else if (clause.getOccur() == BooleanClause.Occur.MUST_NOT) {
						  notSpans.add(new SpanMultiTermQueryWrapper<WildcardQuery>(wildcard));
					  }
					  
				  } else if (subscope == null) {
					  
					  if (clause.getOccur() == BooleanClause.Occur.MUST) {
						  andSpans.add(buildTermQuery(scope, tq));
					  } else if (clause.getOccur() == BooleanClause.Occur.SHOULD) {
						  orSpans.add(buildTermQuery(scope, tq));
					  } else if (clause.getOccur() == BooleanClause.Occur.MUST_NOT) {
						  notSpans.add(buildTermQuery(scope, tq));
					  }
					  
				  } else {
					  
					  ArrayList<SpanQuery> subscopeQuery = new ArrayList<SpanQuery>();
					  subscopeQuery.add(new SpanTermQuery(new Term(scope, getTag(scope, subscope, TAG_TYPE.BEG_TAG))));
					  subscopeQuery.add(new SpanTermQuery(new Term(scope, getTag(scope, subscope, TAG_TYPE.END_TAG))));
					  subscopeQuery.add(buildTermQuery(scope, tq));
					  if (clause.getOccur() == BooleanClause.Occur.MUST) {
						  andSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  } else if (clause.getOccur() == BooleanClause.Occur.SHOULD) {
						  orSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  } else if (clause.getOccur() == BooleanClause.Occur.MUST_NOT) {
						  //FIX
						  notSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  }
					  
				  }
				  
			  } else if (queryclazz == WildcardQuery.class) {
				  
				  if (subscope == null) {

					  if (clause.getOccur() == BooleanClause.Occur.MUST) {
						  andSpans.add(buildWildcardQuery(scope, clause.getQuery()));
					  } else if (clause.getOccur() == BooleanClause.Occur.SHOULD) {
						  orSpans.add(buildWildcardQuery(scope, clause.getQuery()));
					  } else if (clause.getOccur() == BooleanClause.Occur.MUST_NOT) {
						  //FIX
						  notSpans.add(buildWildcardQuery(scope, clause.getQuery()));
					  }
					  
				  } else {

					  ArrayList<SpanQuery> subscopeQuery = new ArrayList<SpanQuery>();
					  subscopeQuery.add(new SpanTermQuery(new Term(scope, getTag(scope, subscope, TAG_TYPE.BEG_TAG))));
					  subscopeQuery.add(new SpanTermQuery(new Term(scope, getTag(scope, subscope, TAG_TYPE.END_TAG))));
					  subscopeQuery.add(buildWildcardQuery(scope, clause.getQuery()));
					  if (clause.getOccur() == BooleanClause.Occur.MUST) {
						  andSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  } else if (clause.getOccur() == BooleanClause.Occur.SHOULD) {
						  orSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  } else if (clause.getOccur() == BooleanClause.Occur.MUST_NOT) {
						  //FIX
						  notSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  }
					  
				  }
				  
			  } else if (queryclazz == PrefixQuery.class) {
				  
				  if (subscope == null) {
					  
					  if (clause.getOccur() == BooleanClause.Occur.MUST) {
						  andSpans.add(buildPrefixQuery(scope, clause.getQuery()));
					  } else if (clause.getOccur() == BooleanClause.Occur.SHOULD) {
						  orSpans.add(buildPrefixQuery(scope, clause.getQuery()));
					  } else if (clause.getOccur() == BooleanClause.Occur.MUST_NOT) {
						  //FIX
						  notSpans.add(buildPrefixQuery(scope, clause.getQuery()));
					  }
					  
				  } else {
					  
					  ArrayList<SpanQuery> subscopeQuery = new ArrayList<SpanQuery>();
					  subscopeQuery.add(new SpanTermQuery(new Term(scope, getTag(scope, subscope, TAG_TYPE.BEG_TAG))));
					  subscopeQuery.add(new SpanTermQuery(new Term(scope, getTag(scope, subscope, TAG_TYPE.END_TAG))));
					  subscopeQuery.add(buildPrefixQuery(scope, clause.getQuery()));
					  if (clause.getOccur() == BooleanClause.Occur.MUST) {
						  andSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  } else if (clause.getOccur() == BooleanClause.Occur.SHOULD) {
						  orSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  } else if (clause.getOccur() == BooleanClause.Occur.MUST_NOT) {
						  //FIX
						  notSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  }
					  
				  }
				  
			  } else if (queryclazz == PhraseQuery.class) {
				  
				  if (subscope == null) {
					  
					  if (clause.getOccur() == BooleanClause.Occur.MUST) {
						  andSpans.add(buildPhraseQuery(scope, clause.getQuery()));
					  } else if (clause.getOccur() == BooleanClause.Occur.SHOULD) {
						  orSpans.add(buildPhraseQuery(scope, clause.getQuery()));
					  } else if (clause.getOccur() == BooleanClause.Occur.MUST_NOT) {
						  //FIX
						  notSpans.add(buildPhraseQuery(scope, clause.getQuery()));
					  }
					  
				  } else {

					  ArrayList<SpanQuery> subscopeQuery = new ArrayList<SpanQuery>();
					  subscopeQuery.add(new SpanTermQuery(new Term(scope, getTag(scope, subscope, TAG_TYPE.BEG_TAG))));
					  subscopeQuery.add(new SpanTermQuery(new Term(scope, getTag(scope, subscope, TAG_TYPE.END_TAG))));
					  subscopeQuery.add(buildPhraseQuery(scope, clause.getQuery()));
					  if (clause.getOccur() == BooleanClause.Occur.MUST) {
						  andSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  } else if (clause.getOccur() == BooleanClause.Occur.SHOULD) {
						  orSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  } else if (clause.getOccur() == BooleanClause.Occur.MUST_NOT) {
						  //FIX
						  notSpans.add(new SpanBetweenQuery(subscopeQuery.toArray(new SpanQuery[subscopeQuery.size()])));
					  }
					  
				  }
				  
			  } else {

				  System.out.println("[" +q.getClass()+"]");
				  
			  }
			  	  
		  }
		  
		  ArrayList<SpanQuery> includeSpans = new ArrayList<SpanQuery>();;
		  
		  // Add the 'and' queries to the includeSpans (if there were any)
		  if (!andSpans.isEmpty()) {
			  if (andSpans.size() > 1) {
				  includeSpans.add(new SpanAndQuery(andSpans.toArray(new SpanQuery[andSpans.size()])));
			  } else {
				  includeSpans.add(andSpans.get(0));
			  }
		  }
		  
		  // Add the 'or' queries to the includeSpans (if there were any)
		  if (!orSpans.isEmpty()) {
			  includeSpans.add(new SpanOrQuery(orSpans.toArray(new SpanQuery[orSpans.size()])));
		  }
		  
		  // Exclude the 'not' queries from the includeSpans (if there were any) 
		  if (!notSpans.isEmpty()) {
			  if (includeSpans.size() > 1) {
				  if (notSpans.size() > 1) {
					  return new SpanNotQuery(new SpanAndQuery(includeSpans.toArray(new SpanQuery[includeSpans.size()])), new SpanAndQuery(notSpans.toArray(new SpanQuery[notSpans.size()])));
				  } else {
					  return new SpanNotQuery(new SpanAndQuery(includeSpans.toArray(new SpanQuery[includeSpans.size()])), notSpans.get(0));
				  }	  
			  } else {
				  if (notSpans.size() > 1) {
					  return new SpanNotQuery(includeSpans.get(0), new SpanAndQuery(notSpans.toArray(new SpanQuery[notSpans.size()])));
				  } else {
					  return new SpanNotQuery(includeSpans.get(0), notSpans.get(0));
				  }				  
			  }
		  } else {
			  if (includeSpans.size() > 1) {
				  return new SpanAndQuery(includeSpans.toArray(new SpanQuery[includeSpans.size()]));
			  } else {
				  return includeSpans.get(0);
			  }
		  }
		  
	        
	  }

	    

	  private String repeat(char c, int times) {

		  StringBuffer b = new StringBuffer();
		  for(int i=0;i < times;i++){
			  b.append(c);
		  }
		  return b.toString();        
	  }
	    
	    
	  /**
	   * Construct a span term query
	   * @param scope
	   * @param q
	   * @return
	   */
	  private SpanQuery buildTermQuery(String scope, Query q) {

		  TermQuery tq = (TermQuery)q;
		  ArrayList<SpanQuery> spans = new ArrayList<SpanQuery>();
		  spans.add(new SpanTermQuery(new Term(scope, getTag(scope, tq.getTerm().field(), TAG_TYPE.BEG_TAG))));
		  spans.add(new SpanTermQuery(new Term(scope, getTag(scope, tq.getTerm().field(), TAG_TYPE.END_TAG))));
		  spans.add(new SpanTermQuery(new Term(scope, tq.getTerm().text())));
		  return new SpanBetweenQuery(spans.toArray(new SpanQuery[spans.size()]));
		  		  
	  }
	 
	  
	  /**
	   * Construct a span wildcard query
	   * @param scope
	   * @param q
	   * @return
	   */
	  private SpanQuery buildWildcardQuery(String scope, Query q) {

		  WildcardQuery wq = (WildcardQuery)q;
		  ArrayList<SpanQuery> spans = new ArrayList<SpanQuery>();
		  spans.add(new SpanTermQuery(new Term(scope, getTag(scope, wq.getTerm().field(), TAG_TYPE.BEG_TAG))));
		  spans.add(new SpanTermQuery(new Term(scope, getTag(scope, wq.getTerm().field(), TAG_TYPE.END_TAG))));
		  WildcardQuery wildcard = new WildcardQuery(new Term(scope, wq.getTerm().text()));
		  spans.add(new SpanMultiTermQueryWrapper<WildcardQuery>(wildcard));
		  return new SpanBetweenQuery(spans.toArray(new SpanQuery[spans.size()]));		  
		  
	  }
	  
	  
	  /**
	   * Construct a span prefix query
	   * @param scope
	   * @param q
	   * @return
	   */
	  private SpanQuery buildPrefixQuery(String scope, Query q) {

		  PrefixQuery pq = (PrefixQuery)q;
		  ArrayList<SpanQuery> spans = new ArrayList<SpanQuery>();
		  spans.add(new SpanTermQuery(new Term(scope, getTag(scope, pq.getField(), TAG_TYPE.BEG_TAG))));
		  spans.add(new SpanTermQuery(new Term(scope, getTag(scope, pq.getField(), TAG_TYPE.END_TAG))));
		  PrefixQuery prefix = new PrefixQuery(new Term(scope, pq.getPrefix().text()));
		  spans.add(new SpanMultiTermQueryWrapper<PrefixQuery>(prefix));
		  return new SpanBetweenQuery(spans.toArray(new SpanQuery[spans.size()]));			  
		  
	  }
	  

	  /**
	   * Construct a span phrase query.
	   * TODO While we have code set up to check for wildcard characters in the query, they have
	   * already been stripped so this will never happen.  This is one thing that would need to be 
	   * addressed.
	   * @param scope
	   * @param q
	   * @return
	   */
	  private SpanQuery buildPhraseQuery(String scope, Query q) {		  
		  
		  PhraseQuery pq = (PhraseQuery)q;
		  Term[] terms = pq.getTerms();
		  ArrayList<SpanQuery> spans = new ArrayList<SpanQuery>();
		    
		  spans.add(new SpanTermQuery(new Term(scope, getTag(scope, terms[0].field(), TAG_TYPE.BEG_TAG))));
		  spans.add(new SpanTermQuery(new Term(scope, getTag(scope, terms[0].field(), TAG_TYPE.END_TAG))));

		  ArrayList<SpanQuery> phraseSpan = new ArrayList<SpanQuery>();
		  for (int i=0; i<terms.length; i++) {
			  String term = terms[i].text();
			  if (term.endsWith("*")) {
				  WildcardQuery wildcard = new WildcardQuery(new Term(scope, term));
				  phraseSpan.add( new SpanMultiTermQueryWrapper<WildcardQuery>(wildcard));				  
			  } else  if (term.contains(singleCs) || term.contains(multiCs)) {
				  WildcardQuery wildcard = new WildcardQuery(new Term(scope, term));
				  phraseSpan.add( new SpanMultiTermQueryWrapper<WildcardQuery>(wildcard));			  
			  } else {
				  phraseSpan.add(new SpanTermQuery(new Term(scope, term)));
			  }
		  }
		  spans.add(new SpanNearQuery(phraseSpan.toArray(new SpanQuery[phraseSpan.size()]),phraseSpan.size(),true));
		  
		  return new SpanBetweenQuery(spans.toArray(new SpanQuery[spans.size()]));
 
	  }
	  
}