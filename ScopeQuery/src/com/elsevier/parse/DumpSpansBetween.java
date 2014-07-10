package com.elsevier.parse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.Version;

import com.searchtechnologies.solr.queryoperators.SpanAndQuery;
import com.searchtechnologies.solr.queryoperators.SpanBetweenQuery;


public class DumpSpansBetween {
	
	public static String [] DOCS = {
		"one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen",
		"ready set one two three, one two three, one two three",
		"one two three four five four three two one",
		"one two three four now only odds they are one three five seven nine",
		"bauthors  bauthor blname  mcbeath elname slname  bfname  darin william efname sfname  eauthor sauthor  bauthor blname  fulford elname slname  bfname  darby efname sfname  eauthor sauthor  bauthor blname  mcbeath elname slname  bfname  darby efname sfname  eauthor sauthor  eauthors sauthors",
		"bauthors  bauthor blname  mcbeath elname slname  bfname  darin efname sfname  eauthor sauthor  bauthor blname fulford elname slname bfname annie efname sfname eauthor sauthor  eauthors sauthors",
	};

	static IndexSearcher searcher;
	static IndexReader reader;
  static IndexReaderContext iReaderContext;
  static AtomicReader atomicIndexReaderWrapper;

	public static void main(String[] args) throws IOException {

		RAMDirectory ramDir = new RAMDirectory();
		//Index some made up content
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_47, new StandardAnalyzer(Version.LUCENE_47));
		IndexWriter writer = new IndexWriter(ramDir, indexWriterConfig);
		for (int i = 0; i < DOCS.length; i++){
			Document doc = new Document();
			Field id = new Field("id", "doc_" + i, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
			doc.add(id);
			//Store both position and offset information
			Field text = new Field("content", DOCS[i], Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS);
			doc.add(text);
			writer.addDocument(doc);
		}
		writer.close();

		reader = DirectoryReader.open(ramDir);
		searcher = new IndexSearcher(reader);
		iReaderContext = searcher.getTopReaderContext();
		atomicIndexReaderWrapper = SlowCompositeReaderWrapper.wrap(reader);

		
		SpanQuery[] span1 = new SpanQuery[3];
		span1[0] = new SpanTermQuery(new Term("content","bfname"));
		span1[1] = new SpanTermQuery(new Term("content","efname"));
		span1[2] = new SpanTermQuery(new Term("content","darin"));
		SpanQuery spanQuery1 = new SpanBetweenQuery(span1);		
		
		SpanQuery[] span2 = new SpanQuery[3];
		span2[0] = new SpanTermQuery(new Term("content","bfname"));
		span2[1] = new SpanTermQuery(new Term("content","efname"));
		span2[2] = new SpanTermQuery(new Term("content","darby"));
		SpanQuery spanQuery2 = new SpanBetweenQuery(span2);			

		SpanQuery[] span3 = new SpanQuery[3];
		span3[0] = new SpanTermQuery(new Term("content","bfname"));
		span3[1] = new SpanTermQuery(new Term("content","efname"));
		span3[2] = new SpanTermQuery(new Term("content","annie"));
		SpanQuery spanQuery3 = new SpanBetweenQuery(span3);	
		
		SpanAndQuery spanAnd = new SpanAndQuery(new SpanQuery[]{spanQuery1, spanQuery2, spanQuery3});
		doSpanQuery(spanAnd, searcher, "fname:darin AND fname:darby and fname:annie");
		
		
		/** Query for fname:darin 		

		SpanQuery[] innerSpan = new SpanQuery[4];
		innerSpan[0] = new SpanTermQuery(new Term("content","bfname"));
		innerSpan[1] = new SpanTermQuery(new Term("content","efname"));
		innerSpan[2] = new SpanTermQuery(new Term("content","darin"));
		innerSpan[3] = new SpanTermQuery(new Term("content","sfname"));
		SpanQuery innerQuery = new SpanBetweenQuery(innerSpan);		
		
		SpanQuery[] outerSpan = new SpanQuery[4];
		outerSpan[0] = new SpanTermQuery(new Term("content", "bauthor"));
		outerSpan[1] = new SpanTermQuery(new Term("content", "eauthor"));
		outerSpan[2] = innerQuery;
		outerSpan[3] = new SpanTermQuery(new Term("content","sauthor"));
		
		SpanQuery outerQuery = new SpanBetweenQuery(outerSpan);	
		doSpanQuery(outerQuery, searcher, "fname:darin");
		
		**/
		
		
		/** Query for fname:darin and lname:fulford within the same author
		 
		SpanQuery[] innerSpanFN = new SpanQuery[4];
		innerSpanFN[0] = new SpanTermQuery(new Term("content","bfname"));
		innerSpanFN[1] = new SpanTermQuery(new Term("content","efname"));
		innerSpanFN[2] = new SpanTermQuery(new Term("content","darin"));
		innerSpanFN[3] = new SpanTermQuery(new Term("content","sfname"));
		SpanQuery innerQueryFN = new SpanBetweenQuery(innerSpanFN);

		SpanQuery[] innerSpanLN = new SpanQuery[4];
		innerSpanLN[0] = new SpanTermQuery(new Term("content","blname"));
		innerSpanLN[1] = new SpanTermQuery(new Term("content","elname"));
		innerSpanLN[2] = new SpanTermQuery(new Term("content","fulford"));
		innerSpanLN[3] = new SpanTermQuery(new Term("content","slname"));
		SpanQuery innerQueryLN = new SpanBetweenQuery(innerSpanLN);
		
		SpanQuery innerQuery = new SpanAndQuery(new SpanQuery[]{ innerQueryFN, innerQueryLN});
		
		SpanQuery[] outerSpan = new SpanQuery[4];
		outerSpan[0] = new SpanTermQuery(new Term("content", "bauthor"));
		outerSpan[1] = new SpanTermQuery(new Term("content", "eauthor"));
		outerSpan[2] = innerQuery;
		outerSpan[3] = new SpanTermQuery(new Term("content","sauthor"));
		
		SpanQuery outerQuery = new SpanBetweenQuery(outerSpan);	
		doSpanQuery(outerQuery, searcher, "fname:darin AND lname:fulford");
		
		**/
		
		
		/** Query for (fname:darin OR fname:darby) and lname:fulford within the same author
		 
		SpanQuery[] innerSpanFN = new SpanQuery[3];
		innerSpanFN[0] = new SpanTermQuery(new Term("content","bfname"));
		innerSpanFN[1] = new SpanTermQuery(new Term("content","efname"));
		SpanOrQuery orQuery = new SpanOrQuery();
		orQuery.addClause(new SpanTermQuery(new Term("content","darin")));
		orQuery.addClause(new SpanTermQuery(new Term("content","darby")));
		innerSpanFN[2] = orQuery;
		//innerSpanFN[3] = new SpanTermQuery(new Term("content","sfname"));
		SpanQuery innerQueryFN = new SpanBetweenQuery(innerSpanFN);

		SpanQuery[] innerSpanLN = new SpanQuery[3];
		innerSpanLN[0] = new SpanTermQuery(new Term("content","blname"));
		innerSpanLN[1] = new SpanTermQuery(new Term("content","elname"));
		innerSpanLN[2] = new SpanTermQuery(new Term("content","mcbeath"));
		//innerSpanLN[3] = new SpanTermQuery(new Term("content","slname"));
		SpanQuery innerQueryLN = new SpanBetweenQuery(innerSpanLN);		
		
		SpanQuery innerQuery = new SpanAndQuery(new SpanQuery[]{ innerQueryFN, innerQueryLN});
		
		SpanQuery[] outerSpan = new SpanQuery[3];
		outerSpan[0] = new SpanTermQuery(new Term("content", "bauthor"));
		outerSpan[1] = new SpanTermQuery(new Term("content", "eauthor"));
		outerSpan[2] = innerQuery;
		//outerSpan[3] = new SpanTermQuery(new Term("content","sauthor"));
		
		SpanQuery outerQuery = new SpanBetweenQuery(outerSpan);	
		doSpanQuery(outerQuery, searcher, "(fname:darin OR fn:darby) AND lname:fulford");		
		
		**/

		
		/** Query for (fname:darin AND fname:william) and lname:mcbeath within the same author
		  
		SpanQuery[] innerSpanFN = new SpanQuery[4];
		innerSpanFN[0] = new SpanTermQuery(new Term("content","bfname"));
		innerSpanFN[1] = new SpanTermQuery(new Term("content","efname"));
		SpanAndQuery andQuery = new SpanAndQuery(new SpanQuery[]{ new SpanTermQuery(new Term("content","darin")), new SpanTermQuery(new Term("content","william"))});
		innerSpanFN[2] = andQuery;
		innerSpanFN[3] = new SpanTermQuery(new Term("content","sfname"));
		SpanQuery innerQueryFN = new SpanBetweenQuery(innerSpanFN);

		SpanQuery[] innerSpanLN = new SpanQuery[4];
		innerSpanLN[0] = new SpanTermQuery(new Term("content","blname"));
		innerSpanLN[1] = new SpanTermQuery(new Term("content","elname"));
		innerSpanLN[2] = new SpanTermQuery(new Term("content","mcbeath"));
		innerSpanLN[3] = new SpanTermQuery(new Term("content","slname"));
		SpanQuery innerQueryLN = new SpanBetweenQuery(innerSpanLN);	
		
		SpanQuery innerQuery = new SpanAndQuery(new SpanQuery[]{ innerQueryFN, innerQueryLN});
		
		SpanQuery[] outerSpan = new SpanQuery[4];
		outerSpan[0] = new SpanTermQuery(new Term("content", "bauthor"));
		outerSpan[1] = new SpanTermQuery(new Term("content", "eauthor"));
		outerSpan[2] = innerQuery;
		outerSpan[3] = new SpanTermQuery(new Term("content","sauthor"));
		
		SpanQuery outerQuery = new SpanBetweenQuery(outerSpan);	
		doSpanQuery(outerQuery, searcher, "(fname:darin AND fn:william) AND lname:mcbeath");	
		
		**/

		
		/** Query for fname:dar* and lname:mcbeath within the same author
		
		SpanQuery[] innerSpanFN = new SpanQuery[4];
		innerSpanFN[0] = new SpanTermQuery(new Term("content","bfname"));
		innerSpanFN[1] = new SpanTermQuery(new Term("content","efname"));
		PrefixQuery prefix = new PrefixQuery(new Term("content", "dar"));	
		innerSpanFN[2] = new SpanMultiTermQueryWrapper<PrefixQuery>(prefix);
		innerSpanFN[3] = new SpanTermQuery(new Term("content","sfname"));
		SpanQuery innerQueryFN = new SpanBetweenQuery(innerSpanFN);

		SpanQuery[] innerSpanLN = new SpanQuery[4];
		innerSpanLN[0] = new SpanTermQuery(new Term("content","blname"));
		innerSpanLN[1] = new SpanTermQuery(new Term("content","elname"));
		innerSpanLN[2] = new SpanTermQuery(new Term("content","mcbeath"));
		innerSpanLN[3] = new SpanTermQuery(new Term("content","slname"));
		SpanQuery innerQueryLN = new SpanBetweenQuery(innerSpanLN);
		
		SpanQuery innerQuery = new SpanAndQuery(new SpanQuery[]{ innerQueryFN, innerQueryLN});
		
		SpanQuery[] outerSpan = new SpanQuery[4];
		outerSpan[0] = new SpanTermQuery(new Term("content", "bauthor"));
		outerSpan[1] = new SpanTermQuery(new Term("content", "eauthor"));
		outerSpan[2] = innerQuery;
		outerSpan[3] = new SpanTermQuery(new Term("content","sauthor"));
		
		SpanQuery outerQuery = new SpanBetweenQuery(outerSpan);	
		doSpanQuery(outerQuery, searcher, "fname:dar* AND lname:mcbeath");	
		
		**/
		
		
		/** Query for fname:da?in AND lname:mcbeath within the same author
		 
		SpanQuery[] innerSpanFN = new SpanQuery[4];
		innerSpanFN[0] = new SpanTermQuery(new Term("content","bfname"));
		innerSpanFN[1] = new SpanTermQuery(new Term("content","efname"));
		WildcardQuery wildcard = new WildcardQuery(new Term("content", "da?in"));	
		innerSpanFN[2] = new SpanMultiTermQueryWrapper<WildcardQuery>(wildcard);
		innerSpanFN[3] = new SpanTermQuery(new Term("content","sfname"));
		SpanQuery innerQueryFN = new SpanBetweenQuery(innerSpanFN);

		SpanQuery[] innerSpanLN = new SpanQuery[4];
		innerSpanLN[0] = new SpanTermQuery(new Term("content","blname"));
		innerSpanLN[1] = new SpanTermQuery(new Term("content","elname"));
		innerSpanLN[2] = new SpanTermQuery(new Term("content","mcbeath"));
		innerSpanLN[3] = new SpanTermQuery(new Term("content","slname"));
		SpanQuery innerQueryLN = new SpanBetweenQuery(innerSpanLN);
		
		SpanQuery innerQuery = new SpanAndQuery(new SpanQuery[]{ innerQueryFN, innerQueryLN});
		
		SpanQuery[] outerSpan = new SpanQuery[4];
		outerSpan[0] = new SpanTermQuery(new Term("content", "bauthor"));
		outerSpan[1] = new SpanTermQuery(new Term("content", "eauthor"));
		outerSpan[2] = innerQuery;
		outerSpan[3] = new SpanTermQuery(new Term("content","sauthor"));
		
		SpanQuery outerQuery = new SpanBetweenQuery(outerSpan);	
		doSpanQuery(outerQuery, searcher, "fname:da?in AND lname:mcbeath");	
		
		**/
		
		
		/** Query for fname:"darin william" AND lname:mcbeath within the same author
		 
		SpanQuery[] innerSpanFN = new SpanQuery[4];
		innerSpanFN[0] = new SpanTermQuery(new Term("content","bfname"));
		innerSpanFN[1] = new SpanTermQuery(new Term("content","efname"));
		SpanNearQuery phrase = new SpanNearQuery(new SpanQuery[]{new SpanTermQuery(new Term("content","darin")), new SpanTermQuery(new Term("content","william"))},2,true);
		innerSpanFN[2] = phrase;
		innerSpanFN[3] = new SpanTermQuery(new Term("content","sfname"));
		SpanQuery innerQueryFN = new SpanBetweenQuery(innerSpanFN);

		SpanQuery[] innerSpanLN = new SpanQuery[4];
		innerSpanLN[0] = new SpanTermQuery(new Term("content","blname"));
		innerSpanLN[1] = new SpanTermQuery(new Term("content","elname"));
		innerSpanLN[2] = new SpanTermQuery(new Term("content","mcbeath"));
		innerSpanLN[3] = new SpanTermQuery(new Term("content","slname"));
		
		SpanQuery innerQueryLN = new SpanBetweenQuery(innerSpanLN);
		
		SpanQuery innerQuery = new SpanAndQuery(new SpanQuery[]{ innerQueryFN, innerQueryLN});
		SpanQuery[] outerSpan = new SpanQuery[4];
		outerSpan[0] = new SpanTermQuery(new Term("content", "bauthor"));
		outerSpan[1] = new SpanTermQuery(new Term("content", "eauthor"));
		outerSpan[2] = innerQuery;
		outerSpan[3] = new SpanTermQuery(new Term("content","sauthor"));
		
		SpanQuery outerQuery = new SpanBetweenQuery(outerSpan);	
		doSpanQuery(outerQuery, searcher, "(fname:\"darin william\") AND lname:mcbeath");	
		
		**/
		
		
		/** Query for fname:darin and not fname:william and lname:mcbeath within the same author
		 
		SpanQuery[] innerSpanFN = new SpanQuery[4];
		innerSpanFN[0] = new SpanTermQuery(new Term("content","bfname"));
		innerSpanFN[1] = new SpanTermQuery(new Term("content","efname"));
		innerSpanFN[2] = new SpanTermQuery(new Term("content","darin"));
		SpanOrQuery notClause = new SpanOrQuery();
		notClause.addClause(new SpanTermQuery(new Term("content","sfname")));
		notClause.addClause(new SpanTermQuery(new Term("content","william")));
		innerSpanFN[3] = notClause;
		SpanQuery innerQueryFN = new SpanBetweenQuery(innerSpanFN);

		SpanQuery[] innerSpanLN = new SpanQuery[4];
		innerSpanLN[0] = new SpanTermQuery(new Term("content","blname"));
		innerSpanLN[1] = new SpanTermQuery(new Term("content","elname"));
		innerSpanLN[2] = new SpanTermQuery(new Term("content","mcbeath"));
		innerSpanLN[3] = new SpanTermQuery(new Term("content","slname"));
		SpanQuery innerQueryLN = new SpanBetweenQuery(innerSpanLN);
		
		SpanQuery innerQuery = new SpanAndQuery(new SpanQuery[]{ innerQueryFN, innerQueryLN});
		
		SpanQuery[] outerSpan = new SpanQuery[4];
		outerSpan[0] = new SpanTermQuery(new Term("content", "bauthor"));
		outerSpan[1] = new SpanTermQuery(new Term("content", "eauthor"));
		outerSpan[2] = innerQuery;
		outerSpan[3] = new SpanTermQuery(new Term("content","sauthor"));
		
		SpanQuery outerQuery = new SpanBetweenQuery(outerSpan);	
		doSpanQuery(outerQuery, searcher, "fname:darin AND NOT fname:william AND lname:mcbeath");
		
		**/

		
		/** Query for fname:"da?in william" AND lname:mcbeath within the same author
		 
		SpanQuery[] innerSpanFN = new SpanQuery[4];
		innerSpanFN[0] = new SpanTermQuery(new Term("content","bfname"));
		innerSpanFN[1] = new SpanTermQuery(new Term("content","efname"));
		WildcardQuery wildcard = new WildcardQuery(new Term("content", "da?in"));	
		SpanNearQuery phrase = new SpanNearQuery(new SpanQuery[]{new SpanMultiTermQueryWrapper<WildcardQuery>(wildcard), new SpanTermQuery(new Term("content","william"))},2,true);
		innerSpanFN[2] = phrase;
		innerSpanFN[3] = new SpanTermQuery(new Term("content","sfname"));
		SpanQuery innerQueryFN = new SpanBetweenQuery(innerSpanFN);

		SpanQuery[] innerSpanLN = new SpanQuery[4];
		innerSpanLN[0] = new SpanTermQuery(new Term("content","blname"));
		innerSpanLN[1] = new SpanTermQuery(new Term("content","elname"));
		innerSpanLN[2] = new SpanTermQuery(new Term("content","mcbeath"));
		innerSpanLN[3] = new SpanTermQuery(new Term("content","slname"));
		
		SpanQuery innerQueryLN = new SpanBetweenQuery(innerSpanLN);
		
		SpanQuery innerQuery = new SpanAndQuery(new SpanQuery[]{ innerQueryFN, innerQueryLN});
		SpanQuery[] outerSpan = new SpanQuery[4];
		outerSpan[0] = new SpanTermQuery(new Term("content", "bauthor"));
		outerSpan[1] = new SpanTermQuery(new Term("content", "eauthor"));
		outerSpan[2] = innerQuery;
		outerSpan[3] = new SpanTermQuery(new Term("content","sauthor"));
		
		SpanQuery outerQuery = new SpanBetweenQuery(outerSpan);	
		doSpanQuery(outerQuery, searcher, "(fname:\"da?in william\") AND lname:mcbeath");	
		**/

		/*******************************************************************************/

		SpanQuery spanQuery = new SpanTermQuery(new Term("content", "darin"));
		doSpanQuery(spanQuery, searcher, "single term");

		/********************************************************************************/

		spanQuery = new SpanNearQuery(new SpanQuery[] {
				new SpanTermQuery(new Term("content", "two")),
				new SpanTermQuery(new Term("content", "five"))}, 5, true);
		doSpanQuery(spanQuery, searcher, "ordered near");

		/********************************************************************************/

		spanQuery = new SpanNearQuery(new SpanQuery[] {
				new SpanTermQuery(new Term("content", "two")),
				new SpanTermQuery(new Term("content", "five"))}, 5, false);
		doSpanQuery(spanQuery, searcher, "unordered near");

		/********************************************************************************/

		SpanNearQuery spanNear = new SpanNearQuery(new SpanQuery[] {
				new SpanTermQuery(new Term("content", "two")),
				new SpanTermQuery(new Term("content", "four"))},
				5,
				true);

		spanQuery = new SpanNearQuery(new SpanQuery[] {
				spanNear,
				new SpanTermQuery(new Term("content", "seven"))}, 4, true);
		doSpanQuery(spanQuery, searcher, "double near, ordered");

		/********************************************************************************/
		
		SpanNearQuery spanOneThree = new SpanNearQuery(new SpanQuery[] {
				new SpanTermQuery(new Term("content", "one")),
				new SpanTermQuery(new Term("content", "three"))}, 1, true);

		spanQuery = new SpanNotQuery( spanOneThree, new SpanTermQuery(new Term("content", "two")));
		doSpanQuery(spanQuery, searcher, "span one three, no two");

		/********************************************************************************/
		
		// simulate a SpanAndQuery
		SpanNearQuery spanPartOne = new SpanNearQuery(new SpanQuery[] {
				new SpanTermQuery(new Term("content", "two")),
				new SpanTermQuery(new Term("content", "three"))}, 0, true);
		SpanNearQuery spanPartTwo = new SpanNearQuery(new SpanQuery[] {
				new SpanTermQuery(new Term("content", "three")),
				new SpanTermQuery(new Term("content", "two"))}, 0, true);
		spanQuery = new SpanNearQuery(new SpanQuery[] {
				spanPartOne, spanPartTwo}, Integer.MAX_VALUE, false);
				
		
		doSpanQuery(spanQuery, searcher, "span and query");
		
		System.out.println("\nDONE.");
	}

	private static void doSpanQuery(SpanQuery spanQuery, IndexSearcher searcher, String description) throws IOException {
		System.out.println("BEGIN QUERY (" + description + "): " + spanQuery.toString());
		TopDocs results = searcher.search(spanQuery, 10);
		for (int i = 0; i < results.scoreDocs.length; i++) {
			ScoreDoc scoreDoc = results.scoreDocs[i];
			System.out.println("Score Doc: " + scoreDoc);
			Document doc = searcher.doc(scoreDoc.doc);
			System.out.println("'" + doc.get("content") + "'");
			System.out.println("");
		}

		Map<Term, TermContext> termContexts = new HashMap<Term, TermContext>();
		Spans spans = spanQuery.getSpans(atomicIndexReaderWrapper.getContext(), new Bits.MatchAllBits(reader.numDocs()), termContexts);
		while (spans.next() == true){
			System.out.println("Doc: " + spans.doc() + " Start: " + spans.start() + " End: " + spans.end());
		}

		System.out.println("END QUERY (" + description + "): " + spanQuery.toString() + "\n");
		System.out.println("----------------------------------------------------------\n\n");
		java.io.BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
		String keyPressed = br.readLine();  
	}
}

