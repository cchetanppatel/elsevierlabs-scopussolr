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
 * <br>Example: <code>{!scope field='authscope' query='fname:Darin AND lname:McBeath'}</code> 
 * 
 * Default operator is AND
 * Leading, Embedded, and Trailing wildcards are supported
 * TODO Wildcards in phrases are not supported
 * TODO NOT operator is currently not supported
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
	  
	  // Author tokens inserted into content when indexing (begin tag, end tag, and separator tag).
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
	  private static final String AFFILIATION_FIELD = "affil";
	  private static final String AFFILIATION_CITY_FIELD = "city";
	  private static final String AFFILIATION_COUNTRY_FIELD = "ctry";
	  private static final String AFFILIATION_ORGANIZATION_FIELD = "org";
	  
	  // Affiliation tokens inserted into content when indexing (begin tag, end tag, and separator tag).
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
	  
	  // Reference tokens inserted into content when indexing (begin tag, end tag, and separator tag).
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
	   * Get the begin, end, or separator tag for the specified scope, field, and type.
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			  }
	
			  return spanQuery;
	      }
	    };
	  }
	  
	  
	  /**
	   * 
	   * @param q
	   * @return
	   */
	  private SpanQuery buildQuery(String scope, Query q) {
		  
		  SpanQuery spanQuery = null;
		  ArrayList<SpanQuery> spans = null;
		  
		  if( q.getClass() == BooleanQuery.class ){
			  
			  System.out.println("BooleanQuery");
			  spanQuery = buildQuery_recursive(q, scope, 0, new ArrayList<SpanQuery>());
			  
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
			  
			  // Wrap the query with the outer begin/end field for either author, affiliation, or reference
			  ArrayList<SpanQuery> outerSpanQuery = new ArrayList<SpanQuery>();
			  outerSpanQuery.add(new SpanTermQuery(new Term(scope, getTag(scope, DEFAULT_FIELD.get(scope), TAG_TYPE.BEG_TAG))));
			  outerSpanQuery.add(new SpanTermQuery(new Term(scope, getTag(scope, DEFAULT_FIELD.get(scope), TAG_TYPE.END_TAG))));
			  outerSpanQuery.add(spanQuery);
			  return new SpanBetweenQuery(outerSpanQuery.toArray(new SpanQuery[outerSpanQuery.size()]));
			  
		  } else {
			  
			  System.out.println("Failed to create a span query.");
			  return null;
			  
		  }
		  
	  }

	  
	  // level is for indentation
	  private SpanQuery buildQuery_recursive(Query q, String scope, int level, ArrayList<SpanQuery> spans) {

		  BooleanQuery castQuery = (BooleanQuery)q;
		  
		  for(BooleanClause clause : castQuery.getClauses() ){
			  
			  Class queryclazz = clause.getQuery().getClass();

			  System.out.println(repeat(' ', level) + "["+queryclazz+"][" + clause.getOccur() + "] " + clause.toString());

			  if(queryclazz == BooleanQuery.class) {
				  
				  spans.add(buildQuery_recursive(clause.getQuery(), scope, level+1, new ArrayList<SpanQuery>()));
				  
			  } else if (queryclazz == TermQuery.class) {
				  
				  spans.add(buildTermQuery(scope, clause.getQuery()));
				  
			  } else if (queryclazz == WildcardQuery.class) {
				  
				  spans.add(buildWildcardQuery(scope, clause.getQuery()));
				  
			  } else if (queryclazz == PrefixQuery.class) {
				  
				  spans.add(buildPrefixQuery(scope, clause.getQuery()));
				  
			  } else if (queryclazz == PhraseQuery.class) {
				  
				  spans.add(buildPhraseQuery(scope, clause.getQuery()));
				  
			  } else {

				  System.out.println("[" +q.getClass()+"]");
				  
			  }
			  	  
		  }
		  
		  return new SpanAndQuery(spans.toArray(new SpanQuery[spans.size()]));
	        
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