package snippet.data;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DataAccess {

	/**
	 *********************************************************************************************************
	 * todo: use the query builder
	 *
	 * @return DocumentList, suitable for the fx framework only, based upon parent class
	 * @throws IOException
	 */
	SeekResult queryDocuments(String q, String collection) throws Exception;

	/**
	 *********************************************************************************************************
	 * @return List<SnipbitDocument>, general to the SnipbitDocument (suitable for swing)
	 * @throws IOException
	 */
	List<SnippetDocument> queryDocumentsAsList(String q, String collection) throws IOException;

	SeekResult getAllDocuments(String q, String collection) throws IOException;

	/********************************************************************************************************
	 * Remove used to actually delete documents from the index, which is (currently)
	 * only used when the document is being edited (delete existing doc and create a new version).
	 * Don't want duplication (design preference).
	 */
	String removeDocument(String collection, String id);

	/********************************************************************************************************
	 * Logical removal chosen for those documents that are directly 'deleted' and where there
	 * will be no trace of their existence.
	 */
	String logicalRemoveDocument(String collection, String id);

	/**
	 * SEVERE: Cannot rollback: Error from server at http://localhost:9876/solr: Expected mime type application/octet-stream but got text/html. <html>
	 * <head>
	 * <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
	 * <title>Error 404 Not Found</title>
	 * </head>
	 * <body><h2>HTTP ERROR 404</h2>
	 * <p>Problem accessing /solr/update. Reason:
	 * <pre>    Not Found</pre></p>
	 * </body>
	 * </html>
	 *
	 */
	String addDocument(Map<String, String> keyValues, String collection);

	String getUrl();

	void setUrl(String url);

}