package snippet.view.fx;

/**
 * A place to secure the input before it goes to the solr/lucene 
 * 	(error-prone characters, syntaxes, DOS attacks, etc...).
 */
public class IOFilter {

	
	public final String normalizeQuery(String q) {
		return q;
	}
	
	public final String sanitizeQuery(String q) {
		return q;
	}
	
	public final boolean validateQuery(String q) {		
		return true;
	}
	
}
