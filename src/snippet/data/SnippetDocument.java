package snippet.data;

public class SnippetDocument {

    String id = "";
    String created = "";
    String updated = "";
    String title = "";
    String tag = "";
    String text = "";
    String citation = "";
    String score = "";
    String shard = "";

    public SnippetDocument() {}

    public SnippetDocument(String someTitle) {
        this("","","",someTitle,"","","","","");
    }
    
    public SnippetDocument(String id, String created, String updated, 
    		String title, String tag, String text, String src)
    {
    	this(id, created, updated, title, tag, text, src, "","");
    }

    public SnippetDocument(String id, String created, String updated, 
    		String title, String tag, String text, String src, String scr, String shard) {
        this.id = id;
        this.created = created;
        this.updated = updated;
        this.title = title;
        this.tag = tag;
        this.text = text;
        this.citation = src;
        this.score = scr;
        this.shard = shard;
    }

    private final String fieldPadding = "   |   "; 
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(created + fieldPadding);
        buf.append((score != null && !score.isEmpty())? score + fieldPadding: "");
        buf.append((title != null && !title.trim().equals("")) ? title : "");
        buf.append((tag != null && !tag.trim().equals("") && !tag.trim().equals("null")) ? fieldPadding + tag : "");
        
        return buf.toString();
    }

    /*****************************************************************************************************************/
    public String getId() {
        return id;
    }

    public String getCreated() {
        return created;
    }
    
    public String getUpdated() {
        return updated;
    }

    public String getTitle() {
        return title;
    }

    public String getTag() {
        return tag;
    }

    public String getText() {
        return text;
    }

    public String getCitation() {
        return citation;
    }


}
