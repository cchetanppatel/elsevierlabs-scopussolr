package com.elsevier.parse;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;

public class TestParser {

	//private static String sQuery = "field1:playing AND (field2:curse OR field3:something) AND field4:somethingother OR field5:blah";
	//private static String sQuery = "field1:(curse OR field3:something*) AND NOT(field4:somethingother OR field5:blah)";
	//private static String sQuery = "field1:playing";
	//private static String sQuery = "playing";
	//private static String sQuery = "-field1:\"heart-attack\"";
	private static String sQuery = "field1:junk AND (field2:boy OR (field3:girl AND field4:dog) AND NOT field5:sister)";
	
	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {

		// TODO Still need to figure out how to handle searching within an author  ... multi-level
		// TODO will want to use my analyzer (may need to specify each field in the schema.xml so the correct analyzer is used)
		QueryParser parser = new QueryParser(Version.LUCENE_47, "author", new StandardAnalyzer(Version.LUCENE_47));
		parser.setAllowLeadingWildcard(true);
		parser.setDefaultOperator(Operator.AND);
		Query q = parser.parse(sQuery);

		if( q.getClass() == BooleanQuery.class ){
			breakQuery_recursive(q, 0);
		} else {
			Class queryclazz = q.getClass();
			TermQuery tq = (TermQuery)q;
			System.out.println("[" +queryclazz+"]" + tq.getTerm());
		}

	}
	
	   // level is for indentation
    private static void breakQuery_recursive(Query q, int level) {

        BooleanQuery castQuery = (BooleanQuery)q;
        for(BooleanClause clause : castQuery.getClauses() ){
            Class queryclazz = clause.getQuery().getClass();

            System.out.println(repeat(' ', level) + "["+queryclazz+"][" + clause.getOccur() + "] " + clause.toString());

            if(queryclazz == BooleanQuery.class) {
                breakQuery_recursive(clause.getQuery(), level+1);
            }
        }
        
    }

    private static String repeat(char c, int times) {

        StringBuffer b = new StringBuffer();
        for(int i=0;i < times;i++){
            b.append(c);
        }
        return b.toString();
        
    }

}
