package snippet.data.lucene;

import snippet.Field;
import snippet.data.QueryBuilder;
import snippet.view.fx.IOFilter;

public class LuceneQueryBuilder extends QueryBuilder {

	// todo: do this in actual builder convention..
	// todo: sanitize/normalize, security, etc..

	/**
	 * TODO: 'gw-535' seems to be returning several unrelated results, probably
	 * related to the dash; need to fix
	 */
	@Override
	public String buildFromString(String input) {
		StringBuilder query = new StringBuilder();
		if (input == null || input.trim().equals("")) // returns all docs if no search string provided
			query.append("*:*");
		else if (input.contains(":")) // uses search string as is if it contains a reference to a particular tag
			query.append(input);
		else // else, searches for the string in primary index fields
		{
			query.append("(tag:");
			query.append(input); 
			query.append(" OR ");
			query.append(Field.FULLTEXT.key()); 
			query.append(":");
			query.append(input);
			query.append(" OR ");
			query.append(Field.TITLE.key());
			query.append(":");
			query.append(input); 
			query.append(" OR ");
			query.append(Field.CREATED.key());
			query.append(":");
			query.append(input);
			query.append("*");
			query.append(" OR ");
			query.append(Field.UPDATED.key());
			query.append(":");
			query.append(input);
			query.append("*)");
			// " AND deleted is not null....
		}

		return new IOFilter().sanitizeQuery(query.toString());
	}

}
