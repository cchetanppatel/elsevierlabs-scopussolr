package com.elsevier.solr.queryparser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;


/**
 * Create a scope query.
 * 
 * <br>Example: <code>{!scope field='author' author='AND(sir)' fname='AND(darin,william)' lname='OR(mcbeath,mcbeth),NOT(fulford)'}</code> 
 * 
 * An implicit AND is applied across the specified fields in a query.
 * 
 * In reality, we would like something like the following for a syntax:
 * 
 * author:(AND(fname:(AND(darin,william)),lname:(OR(mcbeath,mcbeth))))
 * 
 * Syntax is:
 *   fn:(connector(values))
 *   
 *   where values can be a token or values can be another fn:(connector(values))
 *   will likely want to generate a parser to accomplish the task.  Needs to be a deep parser.
 *   Pattern is the syntax above.
 *   Should investigate Javacc (at least as a starting point).
 *   
 *   Need to think about when to apply order and when not
 *    within a field, the tokens are unordered
 *    whenever you 'close' a field the tokens (beg, innerspan(s), end) with NOT on the separator must be ordered
 * 
 */
public class ScopeQParserPlugin extends QParserPlugin{

	  // Name for the Plugin
	  public static final String NAME = "scope";
	  
	  // Types of clauses allowed in the value for a specific field 
	  private static final String AND_CLAUSE = "AND(";
	  private static final String OR_CLAUSE = "OR(";
	  private static final String NOT_CLAUSE = "NOT(";
	  
	  // Identifies the field for scoping
	  private static final String SCOPE_FIELD = "field";
	  	  
	  // Possible values for the field to scope
	  private static final String AUTHOR_SCOPE_FIELD = "author";
	  private static final String AFFILIATION_SCOPE_FIELD = "affiliation";
	  private static final String REFERENCE_SCOPE_FIELD = "reference";

	  // Author Scope Fields (specified in the query)
	  private static final String AUTHOR_FIELD = "author";
	  private static final String AUTHOR_FIRST_NAME_FIELD = "fname";
	  private static final String AUTHOR_LAST_NAME_FIELD = "lname";
	  private static final String AUTHOR_EMAIL_FIELD = "email";
	  
	  // Author tokens inserted into content when indexing
	  // TODO create 3 separate hashmaps (begin token, end token separator)
	  private static final String BEG_AUTHOR_TOKEN = "BAUTHOR";
	  private static final String END_AUTHOR_TOKEN = "EAUTHOR";
	  private static final String SEP_AUTHOR_TOKEN = "SAUTHOR";
	  private static final String BEG_AUTHOR_LNAME_TOKEN = "BLNAME";
	  private static final String END_AUTHOR_LNAME_TOKEN = "ELNAME";
	  private static final String SEP_AUTHOR_LNAME_TOKEN = "SLNAME";
	  private static final String BEG_AUTHOR_FNAME_TOKEN = "BFNAME";
	  private static final String END_AUTHOR_FNAME_TOKEN = "EFNAME";
	  private static final String SEP_AUTHOR_FNAME_TOKEN = "SFNAME";
	  private static final String BEG_AUTHOR_EMAIL_TOKEN = "BEMAIL";
	  private static final String END_AUTHOR_EMAIL_TOKEN = "EEMAIL";
	  private static final String SEP_AUTHOR_EMAIL_TOKEN = "SEMAIL";
	  
	  // Affiliation Scope Fields (specified in the query)
	  private static final String AFFILIATION_FIELD = "affil";
	  private static final String AFFILIATION_CITY_FIELD = "city";
	  private static final String AFFILIATION_COUNTRY_FIELD = "ctry";
	  private static final String AFFILIATION_ORGANIZATION_FIELD = "org";
	  
	  // Affiliation tokens inserted into content when indexing
	  // TODO create 3 separate hashmaps (begin token, end token separator)
	  private static final String BEG_AFFILIATION_TOKEN = "BAFFILIATION";
	  private static final String END_AFFILIATION_TOKEN = "EAFFILIATION";
	  private static final String SEP_AFFILIATION_TOKEN = "SAFFILIATION";
	  private static final String BEG_AFFILIATION_CITY_TOKEN = "BCITY";
	  private static final String END_AFFILIATION_CITY_TOKEN = "ECITY";
	  private static final String SEP_AFFILIATION_CITY_TOKEN = "SCITY";
	  private static final String BEG_AFFILIATION_COUNTRY_TOKEN = "BCOUNTRY";
	  private static final String END_AFFILIATION_COUNTRY_TOKEN = "ECOUNTRY";
	  private static final String SEP_AFFILIATION_COUNTRY_TOKEN = "SCOUNTRY";
	  private static final String BEG_AFFILIATION_ORGANIZATION_TOKEN = "BORGANIZATION";
	  private static final String END_AFFILIATION_ORGANIZATION_TOKEN = "EORGANIZATION";
	  private static final String SEP_AFFILIATION_ORGANIZATION_TOKEN = "SORGANIZATION";
	  
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
	  
	  // Reference tokens inserted into content when indexing
	  // TODO create 3 separate hashmaps (begin token, end token separator)
	  private static final String BEG_REFERENCE_TOKEN = "BREFERENCE";
	  private static final String END_REFERENCE_TOKEN = "EREFERENCE";
	  private static final String BEG_REFERENCE_EID_TOKEN = "BEID";
	  private static final String END_REFERENCE_EID_TOKEN = "EEID";
	  private static final String BEG_REFERENCE_AUTHOR_TOKEN = "BAUTHOR";
	  private static final String END_REFERENCE_AUTHOR_TOKEN = "EAUTHOR";
	  private static final String BEG_REFERENCE_AUTHOR_LNAME_TOKEN = "BLNAME";
	  private static final String END_REFERENCE_AUTHOR_LNAME_TOKEN = "ELNAME";
	  private static final String BEG_REFERENCE_AUTHOR_FNAME_TOKEN = "BFNAME";
	  private static final String END_REFERENCE_AUTHOR_FNAME_TOKEN = "EFNAME";
	  private static final String BEG_REFERENCE_TITLE_TOKEN = "BTITLE";
	  private static final String END_REFERENCE_TITLE_TOKEN = "ETITLE";
	  private static final String BEG_REFERENCE_LANGUAGE_TOKEN = "BLANG";
	  private static final String END_REFERENCE_LANGUAGE_TOKEN = "ELANG";
	  private static final String BEG_REFERENCE_SOURCE_TITLE_TOKEN = "BSRCTITLE";
	  private static final String END_REFERENCE_SOURCE_TITLE_TOKEN = "ESRCTITLE";
	  private static final String BEG_REFERENCE_PUB_YEAR_TOKEN = "BPUBYR";
	  private static final String END_REFERENCE_PUB_YEAR_TOKEN = "EPUBYR";
	  private static final String BEG_REFERENCE_PUB_YEAR__TEXT_TOKEN = "BPUBYRTXT";
	  private static final String END_REFERENCE_PUB_YEAR_TEXT_TOKEN = "EPUBYRTXT";
	  private static final String BEG_REFERENCE_PAGE_TOKEN = "BPG";
	  private static final String END_REFERENCE_PAGE_TOKEN = "EPG";
	  private static final String BEG_REFERENCE_PAGE_FIRST_TOKEN = "BPGFIRST";
	  private static final String END_REFERENCE_PAGE_FIRST_TOKEN = "EPGFIRST";
	  private static final String BEG_REFERENCE_ARTILCE_NUMBER_TOKEN = "BARTNUM";
	  private static final String END_REFERENCE_ARTILCE_NUMBER_TOKEN = "EARTNUM";
	  private static final String BEG_REFERENCE_SCOPUS_ID_TOKEN = "BSCP";
	  private static final String END_REFERENCE_SCOPUS_ID_TOKEN = "ESCP";
	  private static final String BEG_REFERENCE_AUTHOR_ID_TOKEN = "BAUTHID";
	  private static final String END_REFERENCE_AUTHOR_ID_TOKEN = "EAUTHID";

	  
	  @Override
	  public void init(NamedList args) {
	  }

	  @Override
	  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
	    return new QParser(qstr, localParams, params, req) {
	      @Override
	      public Query parse() {
	    	  
	    	  String field = localParams.get(SCOPE_FIELD);
	    	  
	    	  System.out.println("*** " + SCOPE_FIELD + " is '" + field + "'.");

	    	  if (field.compareTo(AUTHOR_SCOPE_FIELD) == 0) {    	   
	    		  
	    		  return authorScope(localParams, req);
	        
	    	  } else if (field.compareTo(AFFILIATION_SCOPE_FIELD) == 0) {
	    		  	    		  
	    		  return affiliationScope(localParams, req);
	    	   
	    	  } else if (field.compareTo(REFERENCE_SCOPE_FIELD) == 0) {
	    		      		  
	    		  return referenceScope(localParams, req);
	    	   
	    	  } else {
	    	   // Problem, but should not happen
	    	   System.out.println("Invalid scope field '" + field + "' found.");
	    	   return null;
	    	  }		
	    	 
	      }
	    };
	  }
	  
	  
	  /**
	   * Construct an scope query for the author field.
	   * 
	   * @param localParams
	   * @param req
	   * @return SpanQuery
	   */
	  private Query authorScope(SolrParams localParams, SolrQueryRequest req) {

		  // Get the analyzer
		  Analyzer analyzer = req.getSchema().getQueryAnalyzer();
		  
		  // Container for all of the inner spans
		  ArrayList<SpanQuery> spans = new ArrayList<SpanQuery>();
		  
		  // Get the values for for all possible fields that can be specified for an author scope query
		  String authField = localParams.get(AUTHOR_FIELD);
		  String authFirstnameField = localParams.get(AUTHOR_FIRST_NAME_FIELD);
		  String authLastnameField = localParams.get(AUTHOR_LAST_NAME_FIELD);
		  String authEmailField = localParams.get(AUTHOR_EMAIL_FIELD);
		
		  System.out.println("*** " + AUTHOR_FIELD + " is '" + authField + "'.");
		  System.out.println("*** " + AUTHOR_FIRST_NAME_FIELD + " is '" + authFirstnameField + "'.");
		  System.out.println("*** " + AUTHOR_LAST_NAME_FIELD + " is '" + authLastnameField + "'.");
		  System.out.println("*** " + AUTHOR_EMAIL_FIELD + " is '" + authEmailField + "'.");
		  
		  // Construct a scope query for the author first name field
		  SpanQuery authFirstnameSpanQuery = getSpanQuery(AUTHOR_SCOPE_FIELD, authFirstnameField, analyzer, BEG_AUTHOR_FNAME_TOKEN, END_AUTHOR_FNAME_TOKEN, SEP_AUTHOR_FNAME_TOKEN);
		  if (authFirstnameSpanQuery != null) {			  
			  spans.add(authFirstnameSpanQuery);
		  }
		  
		  // Construct a scope query for the author last name field
		  SpanQuery authLastnameSpanQuery = getSpanQuery(AUTHOR_SCOPE_FIELD, authLastnameField, analyzer, BEG_AUTHOR_LNAME_TOKEN, END_AUTHOR_LNAME_TOKEN, SEP_AUTHOR_LNAME_TOKEN);
		  if (authLastnameSpanQuery != null) {
			  spans.add(authLastnameSpanQuery);
		  }
		  
		  // Construct a scope query for the author email field
		  SpanQuery authEmailSpanQuery = getSpanQuery(AUTHOR_SCOPE_FIELD, authEmailField, analyzer, BEG_AUTHOR_EMAIL_TOKEN, END_AUTHOR_EMAIL_TOKEN, SEP_AUTHOR_EMAIL_TOKEN);
		  if (authEmailSpanQuery != null) {
			  spans.add(authEmailSpanQuery);
		  }
		  
		  // Construct the resultant author scope query (including terms specified in the author field of the query)
		  return getSpanQuery(AUTHOR_SCOPE_FIELD, authField, analyzer, spans, BEG_AUTHOR_TOKEN, END_AUTHOR_TOKEN, SEP_AUTHOR_TOKEN);
		  
	  }

	  
	  /**
	   * Construct an scope query for the affiliation field.
	   * 
	   * @param localParams
	   * @param req
	   * @return SpanQuery
	   */	  
	  private Query affiliationScope(SolrParams localParams, SolrQueryRequest req) {

		  // Get the analyzer
		  Analyzer analyzer = req.getSchema().getQueryAnalyzer();
		  
		  // Container for all of the inner spans
		  ArrayList<SpanQuery> spans = new ArrayList<SpanQuery>();
		  
		  // Get the values for for all possible fields that can be specified for an affiliation scope query
		  String affilField = localParams.get(AFFILIATION_FIELD);
		  String affilCityField = localParams.get(AFFILIATION_CITY_FIELD);
		  String affilCtryField = localParams.get(AFFILIATION_COUNTRY_FIELD);
		  String affilOrgField = localParams.get(AFFILIATION_ORGANIZATION_FIELD);
		  
		  System.out.println("*** " + AFFILIATION_FIELD + " is '" + affilField + "'.");
		  System.out.println("*** " + AFFILIATION_CITY_FIELD + " is '" + affilCityField + "'.");
		  System.out.println("*** " + AFFILIATION_COUNTRY_FIELD + " is '" + affilCtryField + "'.");
		  System.out.println("*** " + AFFILIATION_ORGANIZATION_FIELD + " is '" + affilOrgField + "'.");
		  
		  // Construct a scope query for the affiliation city field
		  SpanQuery affilCitySpanQuery = getSpanQuery(AFFILIATION_SCOPE_FIELD, affilCityField, analyzer, BEG_AFFILIATION_CITY_TOKEN, END_AFFILIATION_CITY_TOKEN, SEP_AFFILIATION_CITY_TOKEN);
		  if (affilCitySpanQuery != null) {			  
			  spans.add(affilCitySpanQuery);
		  }
		  
		  // Construct a scope query for the affiliation country field
		  SpanQuery affilCtrySpanQuery = getSpanQuery(AFFILIATION_SCOPE_FIELD, affilCtryField, analyzer, BEG_AFFILIATION_COUNTRY_TOKEN, END_AFFILIATION_COUNTRY_TOKEN, SEP_AFFILIATION_COUNTRY_TOKEN);
		  if (affilCtrySpanQuery != null) {
			  spans.add(affilCtrySpanQuery);
		  }
		  
		  // Construct a scope query for the affiliation organization field
		  SpanQuery affilOrgSpanQuery = getSpanQuery(AFFILIATION_SCOPE_FIELD, affilOrgField, analyzer, BEG_AFFILIATION_ORGANIZATION_TOKEN, END_AFFILIATION_ORGANIZATION_TOKEN, SEP_AFFILIATION_ORGANIZATION_TOKEN);
		  if (affilOrgSpanQuery != null) {
			  spans.add(affilOrgSpanQuery);
		  }
		  
		  // Construct the resultant affiliation scope query (including terms specified in the affiliation field of the query)
		  return getSpanQuery(AFFILIATION_SCOPE_FIELD, affilField, analyzer, spans, BEG_AFFILIATION_TOKEN, END_AFFILIATION_TOKEN, SEP_AFFILIATION_TOKEN);
		  
	  }

	  
	  /**
	   * Construct an scope query for the reference field.
	   * 
	   * @param localParams
	   * @param req
	   * @return SpanQuery
	   */	
	  private Query referenceScope(SolrParams localParams, SolrQueryRequest req) {

		  // Get the analyzer
		  Analyzer analyzer = req.getSchema().getQueryAnalyzer();
		  
		  System.out.println("*** " + REFERENCE_FIELD + " is '" + localParams.get(REFERENCE_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_EID_FIELD + " is '" + localParams.get(REFERENCE_EID_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_AUTHOR_FIELD + " is '" + localParams.get(REFERENCE_AUTHOR_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_AUTHOR_FIRST_NAME_FIELD + " is '" + localParams.get(REFERENCE_AUTHOR_FIRST_NAME_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_AUTHOR_LAST_NAME_FIELD + " is '" + localParams.get(REFERENCE_AUTHOR_LAST_NAME_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_TITLE_FIELD + " is '" + localParams.get(REFERENCE_TITLE_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_LANGUAGE_FIELD + " is '" + localParams.get(REFERENCE_LANGUAGE_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_SOURCE_TITLE_FIELD + " is '" + localParams.get(REFERENCE_SOURCE_TITLE_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_PUB_YEAR_FIELD + " is '" + localParams.get(REFERENCE_PUB_YEAR_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_PUB_YEAR_TEXT_FIELD + " is '" + localParams.get(REFERENCE_PUB_YEAR_TEXT_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_PAGE_FIELD + " is '" + localParams.get(REFERENCE_PAGE_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_PAGE_FIRST_FIELD + " is '" + localParams.get(REFERENCE_PAGE_FIRST_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_ARTILCE_NUMBER_FIELD + " is '" + localParams.get(REFERENCE_ARTILCE_NUMBER_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_SCOPUS_ID_FIELD + " is '" + localParams.get(REFERENCE_SCOPUS_ID_FIELD) + "'.");
		  System.out.println("*** " + REFERENCE_AUTHOR_ID_FIELD + " is '" + localParams.get(REFERENCE_AUTHOR_ID_FIELD) + "'.");
		  
		  return null;
		  
	  }

	  
	  // TODO Need to check for phrases
	  //   SpanTermQuery stq1 = new SpanTermQuery(new Term("author", "darin"));
	  //   SpanTermQuery stq2 = new SpanTermQuery(new Term("author", "william"));
	  //   SpanQuery[] sq1 = new SpanQuery[] {stq1,stq2};
	  //   SpanNearQuery pq = new SpanNearQuery(sq1,1,true);
	  // TODO Need to analyze term

	  
	  /**
	   * Construct a span query for the specified scope.
	   * 
	   * @param field (author, affiliation, or reference)
	   * @param query Terms that should be applied to the specified scope
	   * @param analyzer
	   * @param begMarker starting token for the scope
	   * @param endMarker ending token for the scope
	   * @param sepMarker separator token for the scope
	   * @return SpanQuery
	   */
	  private SpanQuery getSpanQuery(String field, String query, Analyzer analyzer, String begMarker, String endMarker, String sepMarker) {
		  
		  	if (query == null) {
		  		return null;
		  	}
		  	
		  	// Get the AND,OR,NOT sub queries from the query for the scope  	
		  	SpanQuery andQuery = getAndQuery(field, query, analyzer);
		  	SpanQuery orQuery = getOrQuery(field, query, analyzer);
		  	SpanOrQuery notQuery = getNotQuery(field, query, analyzer);

		  	ArrayList<SpanQuery> innerSpans = new ArrayList<SpanQuery>();
		  	if (andQuery != null) {
		  		innerSpans.add(andQuery);
		  	}
		  	if (orQuery != null) {
		  		innerSpans.add(orQuery);
		  	}
		  	
		    // We don't care about the order for the inner query(s)
		  	SpanQuery innerQuery = new SpanNearQuery(innerSpans.toArray(new SpanQuery[innerSpans.size()]), Integer.MAX_VALUE,false);
		  	
		  	// Create the 'outer' span.  Here we are applying the 'begin token' for the field and the
		  	// 'end token' for the field, so we now need to enforce a specific order.
		  	ArrayList<SpanQuery> outerSpanQuery = new ArrayList<SpanQuery>();
		  	outerSpanQuery.add(new SpanTermQuery(new Term(field, begMarker)));
		  	outerSpanQuery.add(innerQuery);
		  	outerSpanQuery.add(new SpanTermQuery(new Term(field, endMarker)));
			SpanNearQuery nq = new SpanNearQuery(outerSpanQuery.toArray(new SpanQuery[outerSpanQuery.size()]), Integer.MAX_VALUE, true);
			
			// Add the sep marker to the not clause
			notQuery.addClause(new SpanTermQuery(new Term(field, sepMarker)));
			return new SpanNotQuery(nq,notQuery);	

	  }

	  
	  /**
	   * Construct a span query with the specified sub-scopes.  Think of this as the outer wrapper that might contain
	   * lower level fields that need to be scoped.  For example, 'author' would be an outer wrapper (and there can
	   * be queries specified at this level.  Within 'author', a user can further scope a query based on 'fname' and
	   * 'lname'.  The 'fname' and 'lname' would be considered sub-scopes.
	   * 
	   * @param field (author, affiliation, or reference)
	   * @param query Additional search terms that should be applied at the highest level (author, affiliation, or reference)
	   * @param analyzer
	   * @param spans Nested spans that should be included within this scope (think of these as sub scopes)
	   * @param begMarker starting token for the scope
	   * @param endMarker ending token for the scope
	   * @param sepMarker separator token for the scope
	   * @return SpanQuery
	   */
	  private SpanQuery getSpanQuery(String field, String query, Analyzer analyzer, ArrayList<SpanQuery> spans, String begMarker, String endMarker, String sepMarker) {
		  
		    ArrayList<SpanQuery> innerSpans = new ArrayList<SpanQuery>();
		    
		  	// Get the AND,OR,NOT sub queries from the query for the scope  	
		  	SpanQuery andQuery = getAndQuery(field, query, analyzer);
		  	SpanQuery orQuery = getOrQuery(field, query, analyzer);
		  	SpanOrQuery notQuery = getNotQuery(field, query, analyzer);
		  	
		  	if (andQuery != null) {
		  		innerSpans.add(andQuery);
		  	}
		  	if (orQuery != null) {
		  		innerSpans.add(orQuery);
		  	}
		  	
		  	// Add the sub-scope spans
		  	innerSpans.addAll(spans);
		  	
		    // We don't care about the order for the inner query(s)
		  	SpanQuery innerQuery = new SpanNearQuery(innerSpans.toArray(new SpanQuery[innerSpans.size()]), Integer.MAX_VALUE,false);
		  
		  	// Create the 'outer' span.  Here we are applying the 'begin token' for the field and the
		  	// 'end token' for the field, so we now need to enforce a specific order.
		  	ArrayList<SpanQuery> outerSpanQuery = new ArrayList<SpanQuery>();
		  	outerSpanQuery.add(new SpanTermQuery(new Term(field, begMarker)));
		  	outerSpanQuery.add(innerQuery);
		  	outerSpanQuery.add(new SpanTermQuery(new Term(field, endMarker)));
			SpanNearQuery nq = new SpanNearQuery(outerSpanQuery.toArray(new SpanQuery[outerSpanQuery.size()]), Integer.MAX_VALUE, true);
			
			// Add the sep marker to the not clause
			notQuery.addClause(new SpanTermQuery(new Term(field, sepMarker)));
			return new SpanNotQuery(nq,notQuery);	

	  }
	  
	  
	  /**
	   * Get the AND clause specified in the field specific query.  For example,
	   * a query for an author field  might be something like AND(darin),OR(mcbeath,mcbeth),NOT(william).
	   * The value returned for the AND clause would be 'darin'.
	   * 
	   * @param field (author, affiliation, or reference)
	   * @param fieldValue query for a given field
	   * @param analyzer
	   * @return SpanQuery
	   */
	  private SpanQuery getAndQuery(String field, String fieldValue, Analyzer analyzer) {
		    
		  	String query = getClause(AND_CLAUSE,  fieldValue);
		  	
		  	if (query == null) {
		  		return null;
		  	}
		  	
		  	String[] terms = query.split(",");
		  	SpanQuery[] andSpanQuery = new SpanQuery[terms.length];
		  	
		  	for (int i=0; i < terms.length; i++) {
		  		
		  		// Analyze the term
		  		// TODO If more than one term after analysis, make it a phrase
		  		// TODO Apply same logic to the OR and NOT logic as well
		  		// TODO abstract query building into a method call (so it is not repeated in AND,OR,NOT logic)
		  		List<String> tokens = tokenizeString(analyzer, field, terms[i]);
		  		for (Iterator<String> iter = tokens.iterator(); iter.hasNext(); ) {
		  		    String token = iter.next();
		  		    System.out.println("***token*** '" + token + "'");
		  		}
		  		
		  		if (terms[i].contains("?") || terms[i].contains("*")) {
		  			WildcardQuery wildcard = new WildcardQuery(new Term(field, terms[i]));
		  			andSpanQuery[i] = new SpanMultiTermQueryWrapper<WildcardQuery>(wildcard);  			
		  		} else {
		  			andSpanQuery[i] = new SpanTermQuery(new Term(field, terms[i]));
		  		}
		  	}
		  	
		  	// We don't care about the order for the AND query
		  	return new SpanNearQuery(andSpanQuery, Integer.MAX_VALUE, false);
		  	
	  }

	  
	  /**
	   * Get the OR clause specified in the field specific query.  For example,
	   * a query for an author field  might be something like AND(darin),OR(mcbeath,mcbeth),NOT(william).
	   * The value returned for the OR clause would be 'mcbeath,mcbeth'.
	   * 
	   * @param field (author, affiliation, or reference)
	   * @param fieldValue query for a given field
	   * @param analyzer
	   * @return SpanQuery
	   */	  
	  private SpanQuery getOrQuery(String field, String fieldValue, Analyzer analyzer) {
		  
		    String query = getClause(OR_CLAUSE,  fieldValue);
		    
		  	if (query == null) {
		  		return null;
		  	}
		  	
		  	String[] terms = query.split(",");
		  	SpanOrQuery sqor = new SpanOrQuery();
		  	
		  	for (int i=0; i < terms.length; i++) {
		  		
		  		// TODO Analyze the term
		  		
		  		if (terms[i].contains("?") || terms[i].contains("*")) {
		  			WildcardQuery wildcard = new WildcardQuery(new Term(field, terms[i]));
		  			sqor.addClause(new SpanMultiTermQueryWrapper<WildcardQuery>(wildcard));  			
		  		} else {
		  			sqor.addClause(new SpanTermQuery(new Term(field, terms[i])));
		  		}
		  	}
		  	
		    // We don't care about the order for the OR query
		  	return new SpanNearQuery(new SpanQuery[] {sqor}, Integer.MAX_VALUE, false);
		  	
	  }
	  

	  /**
	   * Get the NOT clause specified in the field specific query.  For example,
	   * a query for an author field  might be something like AND(darin),OR(mcbeath,mcbeth),NOT(william).
	   * The value returned for the NOT clause would be 'william'.
	   * 
	   * @param field (author, affiliation, or reference)
	   * @param fieldValue query for a given field
	   * @param analyzer
	   * @return SpanOrQuery
	   */
	  private SpanOrQuery getNotQuery(String field, String fieldValue, Analyzer analyzer) {

		    String query = getClause(NOT_CLAUSE,  fieldValue);		  
		    SpanOrQuery sqnot = new SpanOrQuery();
		    
		  	if (query == null) {
		  		return sqnot;
		  	}
		  	
		  	String[] terms = query.split(",");
		  	
		  	for (int i=0; i < terms.length; i++) {
		  		
		  		// TODO Analyze the term
		  		
		  		if (terms[i].contains("?") || terms[i].contains("*")) {
		  			WildcardQuery wildcard = new WildcardQuery(new Term(field, terms[i]));
		  			sqnot.addClause(new SpanMultiTermQueryWrapper<WildcardQuery>(wildcard));  			
		  		} else {
		  			sqnot.addClause(new SpanTermQuery(new Term(field, terms[i])));
		  		}
		  	}
		  	
		  	return sqnot;
		  	
	  }
	  
	  
	  /**
	   * Get the AND, OR, or NOT clause specified in the field specific query.  For example,
	   * a query for an author field  might be something like AND(darin),OR(mcbeath,mcbeth),NOT(william).
	   * This is a 'helper' method used by getAndQuery, getOrQuery, and getNotQuery.
	   * 
	   * @param clause type of clause to extract (AND,OR,NOT)
	   * @param query field specific queries
	   * @return query for the specified clause
	   */
	  private String getClause(String clause, String query) {
		  
		  String str = null;
		  
		  if (query != null) {
			  int spos = query.indexOf(clause);
			  if (spos > -1) {
				  int epos = query.indexOf(")", spos);
				  str = (String)query.subSequence(spos + clause.length(), epos);
			  } 
		  }
		  
		  return str;
		  
	  }	  
	  
	  
	  /**
	   * Analyze (Tokenize) the string
	   * 
	   * @param analyzer Analyzer 
	   * @param field Field to use for the analysis
	   * @param string Query piece to analyze
	   * @return
	   */
	  private static List<String> tokenizeString(Analyzer analyzer, String field, String string) {
		  
		    List<String> result = new ArrayList<String>();
		    
		    try {
		    	
		      TokenStream stream  = analyzer.tokenStream(field, new StringReader(string));
		      stream.reset();
		      while (stream.incrementToken()) {
		        result.add(stream.getAttribute(CharTermAttribute.class).toString());
		      }
		      stream.close();
		      
		    } catch (IOException e) {
		    	
		      // not thrown b/c we're using a string reader...
		      throw new RuntimeException(e);
		      
		    }
		    
		    return result;
		    
		  }
	  
}
