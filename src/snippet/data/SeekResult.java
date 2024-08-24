package snippet.data;

import snippet.view.fx.DocumentList;

public class SeekResult {
	
	private DocumentList<SnippetDocument> docs;
	private String performance;
	private String query;
	
	public SeekResult(DocumentList<SnippetDocument> l, String query, String perf) {
		this.docs = l;
		this.performance = perf;
		this.query = query;
	}

	
	public DocumentList<SnippetDocument> getDocs() {
		return docs;
	}
	
	public void setDocs(DocumentList<SnippetDocument> docs) {
		this.docs = docs;
	}

	public String getPerformance() {
		return performance;
	}

	public void setPerformance(String performance) {
		this.performance = performance;
	}
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
}
