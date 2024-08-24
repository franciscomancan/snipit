package snippet.data.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;

import snippet.SnipbitLogger;
import snippet.Statics;
import snippet.data.DataAccess;
import snippet.data.SeekResult;
import snippet.data.SnippetDocument;
import snippet.view.fx.DocumentList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SolrDataAccess implements SnipbitLogger, Statics, DataAccess
{
    String url;

    public SolrDataAccess() { this(DefaultSolrServerUrl); }
    public SolrDataAccess(String uri) {
        url = uri;
    }    

    /**
     *********************************************************************************************************
     * todo: use the query builder
     *
     * @return DocumentList, suitable for the fx framework only, based upon parent class
     * @throws IOException
     */
    @Override
    public SeekResult queryDocuments(String q, String collection) throws IOException
    {
    	return queryDocuments(q, collection, MAX_RESULTS_INT);
    }
    
    
	public SeekResult queryDocuments(String q, String collection, int resultSize) throws IOException
    {
        DocumentList<SnippetDocument> docs = new DocumentList<SnippetDocument>();
        long clock = System.currentTimeMillis();
        SolrClient client = getSolrClient(collection);

        log.info("client creation: " + (System.currentTimeMillis() - clock) + " ms");
        clock = System.currentTimeMillis();

        try {
        	final MapSolrParams params = SolrQuery.getParameterMap(new SolrQueryBuilder().buildFromString(q), resultSize);
            final QueryResponse response = client.query(params);
            final SolrDocumentList documents = response.getResults();

            log.info("Found " + documents.getNumFound() + " documents");
            for (SolrDocument document : documents) {
                final String id = (String) document.getFirstValue("id");
                final String created = document.getFirstValue("created") + "";
                final String updated = document.getFirstValue("updated") + "";
                final String title = (String) document.getFirstValue("title");
                final String tag = (String) document.getFirstValue("tag");
                final String text = (String) document.getFirstValue("info");
                final String citing = (String) document.getFirstValue("citation");

                docs.add(new SnippetDocument(id, created, updated, title, tag, text, citing));
            }
        }
        catch(SolrServerException server) {
            server.printStackTrace();
            throw new IOException("Unable to reach the document index.");
        }
        catch(Throwable t) {
        	t.printStackTrace();
        	throw new IOException(t.getMessage());
        }

        log.info("query completed: " + (System.currentTimeMillis() - clock) + " ms");
        return new SeekResult(docs, q, "Completed in " + (System.currentTimeMillis() - clock) + " ms");
    }

    /**
     *********************************************************************************************************
     * @return List<SnipbitDocument>, general to the SnipbitDocument (suitable for swing)
     * @throws IOException
     */
    @Override
	public List<SnippetDocument> queryDocumentsAsList(String q, String collection) throws IOException
    {
        List<SnippetDocument> docs = new ArrayList();
        long clock = System.currentTimeMillis();
        SolrClient client = getSolrClient(collection);

        log.info("client creation: " + (System.currentTimeMillis() - clock) + " ms");
        clock = System.currentTimeMillis();

        try {
        	final MapSolrParams params = SolrQuery.getParameterMap(new SolrQueryBuilder().buildFromString(q));
            final QueryResponse response = client.query(params);
            final SolrDocumentList documents = response.getResults();

            log.info("Found " + documents.getNumFound() + " documents");
            for (SolrDocument document : documents) 
            {
                docs.add(new SnippetDocument(
                	(String) document.getFirstValue("id"), 
                	(String) document.getFirstValue("created"), 
                	(String) document.getFirstValue("updated"), 
                	(String) document.getFirstValue("title"), 
                	(String) document.getFirstValue("tag"), 
                	(String) document.getFirstValue("text"), 
                	(String) document.getFirstValue("citing")
                ));
            }
        }
        catch(Throwable t) {
            log.info("throwable: " + t.getClass());
            throw new IOException("Unable to reach the document index.");
        }

        log.info("query completed: " + (System.currentTimeMillis() - clock) + " ms");
        return docs;
    }

    @Override
	public SeekResult getAllDocuments(String q, String collection) throws IOException
    {
        return queryDocuments(q, collection);
    }

    /********************************************************************************************************
     * Remove used to actually delete documents from the index, which is (currently)
     * only used when the document is being edited (delete existing doc and create a new version).
     * Don't want duplication (design preference).
     */
    @Override
	public String removeDocument(String collection, String id) {
    	SolrClient client = getSolrClient(collection);
    	try {					// document not an autoclosable resource
            client.deleteById(id);
            client.commit();
            log.info("Document deleted: " + id);
            return("Document deleted: " + id);
        }
        catch(Throwable t) {
            try {
                client.rollback();
            }
            catch(Exception cantrollback) {
                log.severe("Cannot rollback: " + cantrollback.getMessage());
            }
            log.severe(t.getMessage());
            return("Problem deleting document: " + id);
        }
    }
    
    /********************************************************************************************************
     * Logical removal chosen for those documents that are directly 'deleted' and where there
     * will be no trace of their existence.
     */
    @Override
	public String logicalRemoveDocument(String collection, String id) {						// NEEDS WORK
    	SolrClient client = getSolrClient(collection);
    	try {					// document not an autoclosable resource
            SolrDocument doc = client.getById(collection, id);
            
            doc.replace("deleted", Statics.getCurrentTimestamp());    
            
            client.commit();
            
            log.info("Document deleted: " + id);
            return("Document deleted: " + id);
        }
        catch(Throwable t) {
            try {
                client.rollback();
            }
            catch(Exception cantrollback) {
                log.severe("Cannot rollback: " + cantrollback.getMessage());
            }
            log.severe(t.getMessage());
            return("Problem deleting document: " + id);
        }
    }
    
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
    @Override
	public String addDocument(Map<String,String> keyValues, String collection)
    {
        String ttl = keyValues.get("title");
        SolrClient client = getSolrClient(collection);

        SolrInputDocument doc = new SolrInputDocument();
        for(String key : new ArrayList<String>(keyValues.keySet())) {
            log.info("adding pair: " + key + "=" + keyValues.get(key));
            doc.addField(key, keyValues.get(key));
        }

        try {					// document not an autoclosable resource
            client.add(doc);
            client.commit();
            log.info("Document added: " + ttl);
            return("Document added: " + ttl);
        }
        catch(Throwable t) {
            try {
                client.rollback();
            }
            catch(Exception cantrollback) {
                log.severe("Cannot rollback: " + cantrollback.getMessage());
            }
            log.severe(t.getMessage());
            return("Problem adding document: " + ttl);
        }
    }

    /** Wanted to default to embedded server but class is missing from dist ?!? */
    SolrClient getSolrClient(String collection)
    {
        String endpoint = url + "/" + collection;
        log.info("reaching out to " + endpoint);
        return new HttpSolrClient.Builder(endpoint)          // figure out how to share/pool/manage this client
            .withConnectionTimeout(SolrConnectionTimeout)
            .withSocketTimeout(SolrSocketTimeout)
            .build();

    }

    @Override
	public String getUrl() { return url; }
    @Override
	public void setUrl(String url) { this.url = url; }

}
