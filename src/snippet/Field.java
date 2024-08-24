package snippet;

public enum Field {

	ID("uid"),
	TITLE("title"),
	TAG("tag"),
	FULLTEXT("fulltext"),
	CREATED("created"),
	UPDATED("updated"),
	DELETED("deleted"),
	CITATION("citation");
	
	private String fieldName;
	
	private Field(String name) {
		fieldName = name;
	}
	
	public String key() {
		return fieldName;
	}
	
}
