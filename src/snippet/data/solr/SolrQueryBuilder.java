package snippet.data.solr;

import snippet.Field;
import snippet.data.QueryBuilder;
import snippet.view.fx.IOFilter;

public class SolrQueryBuilder extends QueryBuilder {

	// todo: do this in actual builder convention..
	// todo: sanitize/normalize, security, etc..

	/**
	 * TODO: 'gw-535' seems to be returning several unrelated results, probably
	 * related to the dash; need to fix
	 */
	@Override
	public String buildFromString(String input) {
		String query;
		if (input == null || input.trim().equals("")) // returns all docs if no search string provided
			query = "*:*";
		else if (input.contains(":")) // uses search string as is if it contains a reference to a particular tag
			query = input;
		else // else, searches for the string in primary index fields
			query = "(" + "tag:" + input + " OR " + Field.FULLTEXT + ":" + input + " OR " + Field.TITLE.key() + ":"
					+ input + " OR " + Field.CREATED.key() + ":" + input + "*" + " OR " + Field.UPDATED.key() + ":"
					+ input + "*" + ")"
			// " AND deleted is not null....
			;

		return new IOFilter().sanitizeQuery(query);
	}

}
