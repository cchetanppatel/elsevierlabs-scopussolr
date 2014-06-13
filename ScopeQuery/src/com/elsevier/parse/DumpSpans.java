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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.Version;

import com.searchtechnologies.solr.queryoperators.SpanBetweenQuery;


public class DumpSpans {
	
	public static String [] DOCS = {
		"one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen",
		"ready set one two three, one two three, one two three",
		"one two three four five four three two one",
		"one two three four now only odds they are one three five seven nine",
		"bauthors  bauthor blname  mcbeath elname slname  bfname  darin william efname sfname  eauthor sauthor  bauthor blname  fulford elname slname  bfname  darby efname sfname  eauthor sauthor  bauthor blname  mcbeath elname slname  bfname  darby efname sfname  eauthor sauthor  eauthors sauthors",
		"bauthors  bauthor blname  mcbeath elname slname  bfname  darin efname sfname  eauthor sauthor  bauthor blname fulford elname slname bfname darin efname sfname eauthor sauthor  eauthors sauthors",
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

		// Simple query for fname:darin (using Between Query)		
		SpanQuery[] innerSpan = new SpanQuery[3];
		innerSpan[0] = new SpanTermQuery(new Term("content","bfname"));
		innerSpan[1] = new SpanTermQuery(new Term("content","efname"));
		innerSpan[2] = new SpanTermQuery(new Term("content","darin"));
		//innerSpan[3] = new SpanTermQuery(new Term("content","sfname"));
		SpanQuery innerQuery = new SpanBetweenQuery(innerSpan);
		//doSpanQuery(innerQuery, searcher, "fname:darin");


		SpanQuery[] outerSpan = new SpanQuery[3];
		outerSpan[0] = new SpanTermQuery(new Term("content", "bauthor"));
		outerSpan[1] = innerQuery;
		outerSpan[2] = new SpanTermQuery(new Term("content", "eauthor"));
		//outerSpan[3] = new SpanTermQuery(new Term("content", "sauthor"));

		SpanQuery outerQuery = new SpanBetweenQuery(outerSpan);	
		doSpanQuery(outerQuery, searcher, "fname:darin");
		
		
		/** Simple query for fname:darin
		ArrayList<SpanQuery> spans = new ArrayList<SpanQuery>();
		spans.add(new SpanTermQuery(new Term("content", "bfname")));
		spans.add(new SpanTermQuery(new Term("content", "darin")));		  
		spans.add(new SpanTermQuery(new Term("content", "efname")));
		SpanNearQuery innerIncludeQuery = new SpanNearQuery(spans.toArray(new SpanQuery[spans.size()]), Integer.MAX_VALUE, true);
		  
		// Add the sep marker to the not clause
		SpanQuery innerExcludeQuery = new SpanTermQuery(new Term("content", "sfname"));
		SpanNotQuery innerSpanQuery = new SpanNotQuery(innerIncludeQuery,innerExcludeQuery);

		ArrayList<SpanQuery> outerSpanQuery = new ArrayList<SpanQuery>();
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "bauthor")));
		outerSpanQuery.add(innerSpanQuery);
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "eauthor")));
		SpanNearQuery includeQuery = new SpanNearQuery(outerSpanQuery.toArray(new SpanQuery[outerSpanQuery.size()]), Integer.MAX_VALUE, true);
			
		// Add the sep marker to the not clause
		SpanQuery excludeQuery = new SpanTermQuery(new Term("content", "sauthor"));
		SpanNotQuery finalQuery = new SpanNotQuery(includeQuery,excludeQuery);		
		doSpanQuery(finalQuery, searcher, "fname:darin");
		**/
		
		
		/** Simple query for fname:darin and lname:fulford ... DOES NOT WORK
		ArrayList<SpanQuery> innerSpans = new ArrayList<SpanQuery>();
		
		ArrayList<SpanQuery> spansln = new ArrayList<SpanQuery>();
		spansln.add(new SpanTermQuery(new Term("content", "blname")));
		spansln.add(new SpanTermQuery(new Term("content", "fulford")));		  
		spansln.add(new SpanTermQuery(new Term("content", "elname")));
		SpanNearQuery lnInnerIncludeQuery = new SpanNearQuery(spansln.toArray(new SpanQuery[spansln.size()]), Integer.MAX_VALUE, true);
		  
		// Add the sep marker to the not clause
		SpanQuery lnInnerExcludeQuery = new SpanTermQuery(new Term("content", "slname"));
		innerSpans.add(new SpanNotQuery(lnInnerIncludeQuery,lnInnerExcludeQuery));
		
		ArrayList<SpanQuery> spansfn = new ArrayList<SpanQuery>();
		spansfn.add(new SpanTermQuery(new Term("content", "bfname")));
		spansfn.add(new SpanTermQuery(new Term("content", "darin")));		  
		spansfn.add(new SpanTermQuery(new Term("content", "efname")));
		SpanNearQuery fnInnerIncludeQuery = new SpanNearQuery(spansfn.toArray(new SpanQuery[spansfn.size()]), Integer.MAX_VALUE, true);
		
		SpanNearQuery innerSpanQuery = new SpanNearQuery(innerSpans.toArray(new SpanQuery[innerSpans.size()]), Integer.MAX_VALUE, true);
		
		ArrayList<SpanQuery> outerSpanQuery = new ArrayList<SpanQuery>();
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "bauthor")));
		outerSpanQuery.add(innerSpanQuery);
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "eauthor")));
		SpanNearQuery includeQuery = new SpanNearQuery(outerSpanQuery.toArray(new SpanQuery[outerSpanQuery.size()]), Integer.MAX_VALUE, true);
			
		// Add the sep marker to the not clause
		SpanQuery excludeQuery = new SpanTermQuery(new Term("content", "sauthor"));
		SpanNotQuery finalQuery = new SpanNotQuery(includeQuery,excludeQuery);		
		doSpanQuery(finalQuery, searcher, "fname:darin AND lname:fulford");
		**/
		
		
		/** Simple query for fname:darin and lname:fulford ... WORKS
		ArrayList<SpanQuery> outerSpanQuery = new ArrayList<SpanQuery>();
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "bauthor")));
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "blname")));
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "fulford")));		  
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "elname")));	
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "bfname")));
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "darin")));		  
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "efname")));
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "eauthor")));
		SpanNearQuery includeQuery = new SpanNearQuery(outerSpanQuery.toArray(new SpanQuery[outerSpanQuery.size()]), Integer.MAX_VALUE, true);
			
		// Add the sep marker to the not clause
		SpanQuery excludeQuery = new SpanTermQuery(new Term("content", "sauthor"));
		SpanNotQuery finalQuery = new SpanNotQuery(includeQuery,excludeQuery);		
		doSpanQuery(finalQuery, searcher, "fname:darin AND lname:fulford");		
		**/
		
		
		/** Simple query for fname:darin and lname:fulford 
		ArrayList<SpanQuery> innerSpans = new ArrayList<SpanQuery>();
		
		// Construct the last name span
		ArrayList<SpanQuery> spansln = new ArrayList<SpanQuery>();
		spansln.add(new SpanTermQuery(new Term("content", "blname")));
		spansln.add(new SpanTermQuery(new Term("content", "fulford")));		  
		spansln.add(new SpanTermQuery(new Term("content", "elname")));
		
		SpanNearQuery lnInnerIncludeQuery = new SpanNearQuery(spansln.toArray(new SpanQuery[spansln.size()]), Integer.MAX_VALUE, true);	  
		// Add the sep marker to the not clause
		SpanQuery lnInnerExcludeQuery = new SpanTermQuery(new Term("content", "slname"));
		innerSpans.add(new SpanNotQuery(lnInnerIncludeQuery,lnInnerExcludeQuery));
		
		// Construct the first name span
		ArrayList<SpanQuery> spansfn = new ArrayList<SpanQuery>();
		spansfn.add(new SpanTermQuery(new Term("content", "bfname")));
		spansfn.add(new SpanTermQuery(new Term("content", "darin")));		  
		spansfn.add(new SpanTermQuery(new Term("content", "efname")));
		SpanNearQuery fnInnerIncludeQuery = new SpanNearQuery(spansfn.toArray(new SpanQuery[spansfn.size()]), Integer.MAX_VALUE, true);	  
		// Add the sep marker to the not clause
		SpanQuery fnInnerExcludeQuery = new SpanTermQuery(new Term("content", "sfname"));
		innerSpans.add(new SpanNotQuery(fnInnerIncludeQuery,fnInnerExcludeQuery));
		
		// Make the first/last name spans ordered
		SpanNearQuery innerSpanQuery = new SpanNearQuery(innerSpans.toArray(new SpanQuery[innerSpans.size()]), Integer.MAX_VALUE, true);
		
		ArrayList<SpanQuery> outerSpanQuery = new ArrayList<SpanQuery>();
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "bauthor")));
		outerSpanQuery.add(innerSpanQuery);
		outerSpanQuery.add(new SpanTermQuery(new Term("content", "eauthor")));
		SpanNearQuery includeQuery = new SpanNearQuery(outerSpanQuery.toArray(new SpanQuery[outerSpanQuery.size()]), Integer.MAX_VALUE, true);
			
		// Add the sep marker to the not clause
		SpanQuery excludeQuery = new SpanTermQuery(new Term("content", "sauthor"));
		SpanNotQuery finalQuery = new SpanNotQuery(includeQuery,excludeQuery);		
		doSpanQuery(finalQuery, searcher, "fname:darin AND lname:fulford");		
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

