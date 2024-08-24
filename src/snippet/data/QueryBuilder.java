package snippet.data;

/* Extracted interface to accommodate different data source types (lucene, solr, etc) */
public abstract class QueryBuilder {
	
	public QueryBuilder() {
		super();
	}
	
	public abstract String buildFromString(String input);

}