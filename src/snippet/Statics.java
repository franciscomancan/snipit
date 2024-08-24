package snippet;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.event.EventType;

public interface Statics {

    final static String DefaultSolrServerUrl = "http://localhost:9876/solr";
    final static String DefaultLuceneIndex = "./snipitindex";
    final static String DefaultLogFile = "./snipit.log";
    final static int SolrConnectionTimeout = 10000;
    final static int SolrSocketTimeout = 60000;

    // Used to alter the number of results that display in from a query, should be moved to configuration page
    final static String MAX_RESULTS = "40";
    final static int MAX_RESULTS_INT = new Integer(MAX_RESULTS);
    
    @Deprecated		// see Field enum
    final static String DOCUMENT_ID = "id";
    @Deprecated
    final static String DOCUMENT_CREATED = "created";
    @Deprecated
    final static String DOCUMENT_UPDATED = "updated";
    @Deprecated
    final static String DOCUMENT_TITLE = "title";
    @Deprecated
    final static String DOCUMENT_TAG = "tag";
    @Deprecated
    final static String DOCUMENT_INFO = "info";
    @Deprecated
    final static String DOCUMENT_CITATION = "citation";

    final static EventType EVENT_BIT_HIT_SELECTED = new EventType("BitHitSelected");
    
    /** collections/indexes already created - 0th element is default on startup */
    final static List<String> collections = Arrays.asList("life","computation");
    
    final static String title1 = "snippet - information sandwich of ";
    final static String title2 = "a bit of this, a bit of that... ";	
    final static String currentTitle = title1;
    
    public static String getCurrentTimestamp() {
        Date d = new Date();
        return
            new SimpleDateFormat("YYYYMMdd").format(d)
            + "." +
            new SimpleDateFormat("kkmm").format(d);
    }
}
