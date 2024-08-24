package snippet.data.solr;

import java.util.HashMap;
import java.util.Map;
import org.apache.solr.common.params.MapSolrParams;

import snippet.Statics;

public class SolrQuery implements Statics {

	private SolrQuery() {}
	
	public final static MapSolrParams getParameterMap(String qry, int maxResults)
	{
		final Map<String, String> queryParamMap = new HashMap<String, String>();
	    queryParamMap.put("q", qry);
	    queryParamMap.put("rows", String.valueOf(maxResults));				// by default, Solr returns 10 documents at a time, can refactor to paginate
	    	    
	    //queryParamMap.put("fl", "id, name");
	    //queryParamMap.put("sort", "id desc");			// will only work for milt
	    	    
	    return new MapSolrParams(queryParamMap);	    
	}
	
	public final static MapSolrParams getParameterMap(String qry)
	{
		return getParameterMap(qry, MAX_RESULTS_INT);
	}
}
