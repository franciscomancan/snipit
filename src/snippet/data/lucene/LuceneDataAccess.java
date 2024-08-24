package snippet.data.lucene;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import snippet.Field;
import snippet.SnipbitLogger;
import snippet.Statics;
import snippet.data.DataAccess;
import snippet.data.SeekResult;
import snippet.data.SnippetDocument;
import snippet.view.fx.DocumentList;

public class LuceneDataAccess implements DataAccess, Statics, SnipbitLogger {

	static StandardAnalyzer analyzer = new StandardAnalyzer();
	
	public Document getLuceneDocumentById(String indexStoragePath, String uid) throws Exception {
		
		if(uid == null || uid.isEmpty())
			return null;
		
		try(IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexStoragePath))))
		{
			IndexSearcher searcher = new IndexSearcher(reader);
		    TopDocs topDocs = searcher.search(
		    	new QueryBuilder(analyzer).createBooleanQuery(Field.ID.key(), uid.trim()), 5);
		    
		    if(topDocs.totalHits.value > 1 || topDocs.totalHits.value < 1) {
		    	log.severe("Receiving improper hits for uid: " + uid);
		    	return null;
		    }
		    
		    return searcher.doc(topDocs.scoreDocs[0].doc);
		}
	}
	
	public String exportFullIndex(String indexStoragePath, String destination) throws Exception
	{
		try(IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexStoragePath))))
		{
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs topDocs = searcher.search(new MatchAllDocsQuery(), 99999999);
		    
			p("found documents: " + topDocs.totalHits);
		    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
		        Document d = searcher.doc(scoreDoc.doc);
		        /*
		        SnippetDocument sd = new SnippetDocument(
		        	d.get(Field.ID.key()), 
		        	d.get(Field.CREATED.key()),
		        	d.get(Field.UPDATED.key()), 
		        	d.get(Field.TITLE.key()), 
		        	d.get(Field.TAG.key()), 
		        	d.get(Field.FULLTEXT.key()), 
		        	d.get(Field.CITATION.key()),
		        	scoreDoc.score + "",
		        	scoreDoc.shardIndex + "");
		        */
		        
		        p("___________________________________________________________________");
		        p(d.get(Field.CREATED.key()));
		        p(d.get(Field.TITLE.key()));
		        p(d.get(Field.TAG.key()));
		        p(d.get(Field.FULLTEXT.key()));		        
		    }
		}
		catch (IndexNotFoundException nf) {
			return "No index found at provided directory: " + indexStoragePath;
		}
		
		return "Completed";
	}
	
	@Override
	public SeekResult queryDocuments(String q, String indexStoragePath) throws Exception 
	{
		long clock = System.currentTimeMillis();
		DocumentList<SnippetDocument> docs = new DocumentList<SnippetDocument>();
		try(IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexStoragePath))))
		{
		    IndexSearcher searcher = new IndexSearcher(reader);
	
		    Query query = new QueryParser(Field.TAG.key(), analyzer).parse(q);
		    TopDocs topDocs = searcher.search(query, MAX_RESULTS_INT);
		    		    
		    p("found documents: " + topDocs.totalHits);
		    List<Document> documents = new ArrayList<>();
		    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
		        Document d = searcher.doc(scoreDoc.doc);
		        SnippetDocument sd = new SnippetDocument(
		        	d.get(Field.ID.key()), 
		        	d.get(Field.CREATED.key()),
		        	d.get(Field.UPDATED.key()), 
		        	d.get(Field.TITLE.key()), 
		        	d.get(Field.TAG.key()), 
		        	d.get(Field.FULLTEXT.key()), 
		        	d.get(Field.CITATION.key()),
		        	scoreDoc.score + "",
		        	scoreDoc.shardIndex + "");
		        
		        System.out.println(d.getFields());		       
		        
		        docs.add(sd);
		    }
		 
		    documents.stream().forEach(d -> p(d.hashCode() + " -> " + d.getFields()));
		}
		
		return new SeekResult(docs, q, (System.currentTimeMillis() - clock) + "ms");
	}

	@Override
	public List<SnippetDocument> queryDocumentsAsList(String q, String indexStoragePath) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeekResult getAllDocuments(String q, String indexStoragePath) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String removeDocument(String indexStoragePath, String id)
	{		
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		try(Directory index = FSDirectory.open(Paths.get(indexStoragePath));
			IndexWriter writer = new IndexWriter(index, indexWriterConfig))
		{
			long seq = writer.deleteDocuments(new QueryBuilder(analyzer).createBooleanQuery(Field.ID.key(), id.trim()));			
			p("deleted document at sequence: " + seq);
		}
		catch(Exception e) {
			e.printStackTrace();
			return "Error: " + e.getMessage();
		}
		finally {
			indexWriterConfig = null;
		}		
		
		return "Removed: " + id;	
	
	}

	@Override
	public String logicalRemoveDocument(String indexStoragePath, String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String addDocument(Map<String, String> keyValues, String indexStoragePath) 
	{
		String ttl = keyValues.get(Field.TITLE.key());
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		try(Directory index = FSDirectory.open(Paths.get(indexStoragePath));
			IndexWriter writer = new IndexWriter(index, indexWriterConfig))
		{
			p(index);
			Document document = new Document();
			for(String key : new ArrayList<String>(keyValues.keySet())) {
	            p("adding pair: " + key + "=" + keyValues.get(key));
	            document.add(new TextField(key, keyValues.get(key), Store.YES));
	        }
			
			p("writing document: " + ttl);
			writer.addDocument(document);
		}
		catch(Exception e) {
			e.printStackTrace();
			return "Error: " + e.getMessage();
		}
		
		return "Added: " + ttl;
	}

	@Override
	public String getUrl() {
		return null;
	}

	@Override
	public void setUrl(String url) {
		// TODO Auto-generated method stub

	}

	public static void p(Object o) {
		System.out.println("" + o);
	}
}
