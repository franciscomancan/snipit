package snippet.data.solr;

import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import snippet.Statics;
import snippet.data.SeekResult;
import snippet.data.SnippetDocument;
import snippet.view.fx.DocumentList;

public class SolrDataExport implements Statics {

	public static void main(String[] argv) throws Exception {
		SolrDataAccess solrAccess = new SolrDataAccess();
		SeekResult res = solrAccess.queryDocuments("*", "life", 1000);
		
		if(res != null && res.getDocs() != null) {
			System.out.println("result size: " + res.getDocs().size());
			DocumentList<SnippetDocument> docs = res.getDocs();
			for(int i=0; i<docs.size(); ++i)
				System.out.println(docs.get(i));
				
		}
	}
	
	public void createCSVFile() throws IOException {
	    FileWriter out = new FileWriter("book_new.csv");
	    try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
	      .withHeader("","",""))) 
	    {
	    	printer.printRecord("","","");
	    	
	    	/*
	        AUTHOR_BOOK_MAP.forEach((author, title) -> {
	            printer.printRecord(author, title);
	        });
	        */
	    }
	}
	
}
