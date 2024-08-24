package snippet.view.fx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import snippet.Field;
import snippet.SnipbitLogger;
import snippet.Statics;
import snippet.data.DataAccess;
import snippet.data.SeekResult;
import snippet.data.SnippetDocument;
import snippet.data.lucene.LuceneDataAccess;
import snippet.data.lucene.LuceneQueryBuilder;
import snippet.data.solr.SolrQueryBuilder;
import snippet.data.solr.SolrDataAccess;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller implements SnipbitLogger, Statics, EventHandler<Event>
{
    static int READ = 0;
    static int CREATE = 1;
    static int UPDATE = 2;
    static int DELETE = 3;

    AtomicInteger state = new AtomicInteger(READ);
    AtomicInteger timesHit = new AtomicInteger(0);
    AtomicBoolean wrapText = new AtomicBoolean(false);
    AtomicBoolean usingSolr = new AtomicBoolean(false);
    
    volatile String currentCollection = "";    
    volatile SnippetDocument currentDocument;
    volatile SeekResult lastResult;
    volatile int currentPage;

    Stage rootStage;
    
    @FXML
    Accordion snipbitAccordian;
    /******************************************************* SEEK TAB */
    @FXML
    Label seekBoxLabel;
    @FXML
    TitledPane seekTitledPane;
    @FXML
    TextField seekTextField;
    @FXML
    Label seekStatus;
    @FXML
    ListView<SnippetDocument> seekResultsListView;
    @FXML
    Label queryLabel;
    @FXML
    Label performanceLabel;
    @FXML
    Label resultCountLabel;
    @FXML
    Label exampleLabel;
    
    /******************************************************* DETAIL TAB */
    @FXML
    TitledPane snipbitTitledPane;
    @FXML
    Button snipbitCreateCancel;
    @FXML
    Button editModeButton;
    @FXML
    Button wrapTextButton;
    @FXML
    Button nextRecordButton;
    @FXML
    Button prevRecordButton;
    @FXML
    Label snipbitStatusLabel;
    @FXML
    TextField createdTextField;
    @FXML
    TextField updatedTextField;
    @FXML
    TextField tagTextField;
    @FXML
    TextField titleTextField;
    @FXML
    TextField citationTextField;
    @FXML
    TextArea detailTextArea;
    @FXML
    Button deleteRecordButton;
    @FXML
    Button confirmDeleteRecordButton;
    @FXML
    Button cancelDeleteRecordButton;
    @FXML
    Button decreaseFontTextButton;
    @FXML
    Button increaseFontTextButton;
    
    /********************************************************* CONGIG TAB */
    ObservableList<String> options =
    		FXCollections.observableArrayList(collections);
    @FXML
    ComboBox<String> collectionComboBox;
    
    @FXML
    Button indexSelectionButton;
    @FXML
    TextField luceneIndexDirectoryTextField;

    DataAccess data = (usingSolr.get())? new SolrDataAccess(): new LuceneDataAccess();
    
    public void initialize() {
    	collectionComboBox.setItems(options);
    	currentCollection = (usingSolr.get())? collections.get(0): DefaultLuceneIndex;
    	if(!usingSolr.get()) {
    		collectionComboBox.setDisable(true);
    		luceneIndexDirectoryTextField.setText(currentCollection);
    	}
    	
    	seekBoxLabel.setText("Search " + currentCollection + " index");
    	
    	confirmDeleteRecordButton.setDisable(true);
    	confirmDeleteRecordButton.setVisible(false);
    	cancelDeleteRecordButton.setDisable(true);
    	cancelDeleteRecordButton.setVisible(false);
    	
    	snipbitAccordian.setExpandedPane(seekTitledPane);		// no worky
    }
    
    public void handleCollectionComboBoxEvent(Event e) {
    	log.info(collectionComboBox.getSelectionModel().getSelectedItem());
    	currentCollection = collectionComboBox.getSelectionModel().getSelectedItem();
    	rootStage.setTitle(currentTitle + currentCollection);
    	seekBoxLabel.setText("Search " + currentCollection + " index");
    }

    @SuppressWarnings("unchecked")
	public void handleSeekKeyReleased(KeyEvent event) {
        if(event.getCode().getName().equals("Enter")) {        	
            log.info("Enter clicked");
            seekResultsListView.setItems(null);
            resultCountLabel.setText("");
            performanceLabel.setText("-");
            queryLabel.setText("-");
            seekStatus.setText("loading hits...");

            try
            {
                String q = (usingSolr.get())? 
                	new SolrQueryBuilder().buildFromString(seekTextField.getText()):
                	new LuceneQueryBuilder().buildFromString(seekTextField.getText());

                log.info("running query > " + q);
                queryLabel.setText(q);

                lastResult = data.queryDocuments(q, currentCollection);                
                seekResultsListView.setItems(lastResult.getDocs());
                
                String capped = (lastResult.getDocs().size() == MAX_RESULTS_INT)? "(capped at " + MAX_RESULTS + ")": "";
                resultCountLabel.setText(lastResult.getDocs().size() + " documents found " + capped);
                performanceLabel.setText(lastResult.getPerformance());
            }
            catch(Exception e) {
                log.severe(e.getMessage());
                seekStatus.setText(e.getMessage());
                return;
            }

            seekStatus.setText("Loaded completed at " + Statics.getCurrentTimestamp());
            setStatus("waiting");
        }
    }

    public void handleSnipbitCreateCancelRelease(MouseEvent event) {
        if(!createMode()) {
            state.set(CREATE);
            setStatus("write mode...");
            snipbitCreateCancel.setText("save");
            createdTextField.setText(Statics.getCurrentTimestamp());
            updatedTextField.setText("");
            clearSnipbit();
            setSnipbitEditable();
        }
        else if(createMode()) {

            String result = newDocument(
                createdTextField.getText(),
                updatedTextField.getText(),
                titleTextField.getText(),
                tagTextField.getText(),
                detailTextArea.getText(),
                citationTextField.getText()
            );

            state.set(READ);
            setStatus(result);
            snipbitCreateCancel.setText("create");
            setSnipbitUnEditable();
        }
    }

    /**
     * The event of selecting a particular item from the results of a given seek/search.
     * @param event
     */
    public void handleSeekResultsListViewMouseClicked(MouseEvent event) {
        setCurrentDocument(seekResultsListView.getSelectionModel().getSelectedItem());
    }
    public void handleSeekResultsListViewKeyReleased(KeyEvent e) {
    	if(e.getCode().getName().equals("Enter"))
    		setCurrentDocument(seekResultsListView.getSelectionModel().getSelectedItem());
    }
    
    /*******************************************************************************************/
    void setCurrentDocument(SnippetDocument doc) {
    	currentDocument = doc;
    	currentPage = lastResult.getDocs().indexOf(doc);
		setDetailContents(currentDocument);
    	snipbitAccordian.setExpandedPane(snipbitTitledPane);
    }

    void setDetailContents(SnippetDocument doc) {
    	
    	editModeButton.setText("edit");		// each time we set content, assume other ops done and reset
    	snipbitCreateCancel.setText("create");
    	state.set(READ);
    	
        createdTextField.setText(doc.getCreated());        
        updatedTextField.setText((doc.getUpdated() == null || doc.getUpdated().trim().equals("null"))? "": doc.getUpdated());
        titleTextField.setText(doc.getTitle());
        tagTextField.setText(doc.getTag());
        detailTextArea.setText(doc.getText());
        detailTextArea.setWrapText(wrapText.get());        
        citationTextField.setText(doc.getCitation());

        setSnipbitUnEditable();
        log.info("snipbit detail set with document: " + doc.getId());
    }
    /******************************************************************************************* APPLICATION STATES */

    boolean readMode() {
        return state.get() == READ;
    }
    boolean createMode() {
        return state.get() == CREATE;
    }
    boolean updateMode() {
        return state.get() == UPDATE;
    }
    boolean deleteMode() {
        return state.get() == DELETE;
    }

    /******************************************************************************************* DELETION */
    public void handleDeleteButtonClick(MouseEvent evt) {
    	confirmDeleteRecordButton.setDisable(true);
    	confirmDeleteRecordButton.setVisible(false);
    	cancelDeleteRecordButton.setDisable(true);
    	cancelDeleteRecordButton.setVisible(false);
    	
    	log.info("Deleting current doc, need to author actions to logically delete a document as well");
    	String docIdToRemove = currentDocument.getId();
    	String feedback = data.removeDocument(currentCollection, docIdToRemove);
    	clearSnipbit();
    	setSnipbitUnEditable();
    	setStatus(feedback);
    	currentDocument = null;
    }
    
    public void handleDeleteRequestButtonClick(MouseEvent evt) {
    	confirmDeleteRecordButton.setDisable(false);
    	confirmDeleteRecordButton.setVisible(true);
    	cancelDeleteRecordButton.setDisable(false);
    	cancelDeleteRecordButton.setVisible(true);
    }
                
    public void handleCancelDeleteButtonClick(MouseEvent evt) {
    	confirmDeleteRecordButton.setDisable(true);
    	confirmDeleteRecordButton.setVisible(false);
    	cancelDeleteRecordButton.setDisable(true);
    	cancelDeleteRecordButton.setVisible(false);
    }
    /*******************************************************************************************/
    
    public void handlePreviousRecordButtonClick(MouseEvent e) {  
    	log.info("handle PREV record");
    	if(lastResult.getDocs().size() == 1)
    		return;
    	
    	if(currentPage == 0)
    		currentPage = lastResult.getDocs().size() - 1;
    	else
    		currentPage -= 1;
    	
    	setCurrentDocument((SnippetDocument)lastResult.getDocs().get(currentPage));
    }
    
    public void handleNextRecordButtonClick(MouseEvent e) {  
    	log.info("handle NEXT record");
    	if(lastResult.getDocs().size() == 1)
    		return;
    	
    	if(currentPage == (lastResult.getDocs().size() - 1))
    		currentPage = 0;
    	else
    		currentPage += 1;
    	
    	setCurrentDocument((SnippetDocument)lastResult.getDocs().get(currentPage));
    }
    
    void setStatus(String s) {
    	snipbitStatusLabel.setText(s);
    }
    
    public void handleEditModeButtonClick(MouseEvent evt) {
        if(!updateMode()) {
            state.set(UPDATE);
            editModeButton.setText("save");
            setStatus("updating current record...");
            updatedTextField.setText(Statics.getCurrentTimestamp());
            setSnipbitEditable();
        }
        else if (updateMode()) {
        	String docIdToRemove = currentDocument.getId();
        	
        	setStatus("updating record");
        	String result = newDocument(
        			createdTextField.getText(),
        			updatedTextField.getText(),
                    titleTextField.getText(),
                    tagTextField.getText(),
                    detailTextArea.getText(),
                    citationTextField.getText()
            );

        	state.set(READ);
        	setStatus("record saved");
            snipbitCreateCancel.setText("create");
            editModeButton.setText("edit");
            /*
             Have to figure out how I want to handle edits to existing documents:
             	> delete old and insert new
             	> modify existing somehow
             	> just append updates as a new document and keep all versions (might add a 'version' field to document)
             */
            setSnipbitUnEditable();
            
            setStatus(result);
            if(!result.contains("Problem"))		// this won't play nice
            	data.removeDocument(currentCollection, docIdToRemove);            
        }

    }

    public void handleWrapTextButtonClick(MouseEvent evt)
    {
        if(wrapText.get()) {
            detailTextArea.setWrapText(false);
            wrapText.set(false);
            return;
        }

        detailTextArea.setWrapText(true);
        wrapText.set(true);
    }
    
    public void handleIndexSelectionButtonMouseClicked(MouseEvent evt) {
    	DirectoryChooser chooser = new DirectoryChooser();
    	chooser.setInitialDirectory(new File("."));
    	chooser.setTitle("Select lucene index root directory");
    	
    	File selected = chooser.showDialog(rootStage);
    	log.info("Index directory selected: " + selected.getAbsolutePath());
    	currentCollection = selected.getAbsolutePath();
    	luceneIndexDirectoryTextField.setText(currentCollection);
    }
    
    public void handleIncreaseFontButtonClick(MouseEvent evt)
    {
        Font f = detailTextArea.getFont();
        detailTextArea.setFont(Font.font(f.getSize()+1));
    }
    
    public void handleDecreaseFontButtonClick(MouseEvent evt)
    {
        Font f = detailTextArea.getFont();
        detailTextArea.setFont(Font.font(f.getSize()-1));
    }

    protected void setSnipbitEditable() {
        detailTextArea.setEditable(true);
        tagTextField.setEditable(true);
        titleTextField.setEditable(true);
        citationTextField.setEditable(true);
        createdTextField.setEditable(true);
    }

    protected void setSnipbitUnEditable() {
        detailTextArea.setEditable(false);
        tagTextField.setEditable(false);
        titleTextField.setEditable(false);
        citationTextField.setEditable(false);
        createdTextField.setEditable(false);
    }

    protected void clearSnipbit() {
        detailTextArea.setText("");
        tagTextField.setText("");
        titleTextField.setText("");
        citationTextField.setText("");
    }

    protected String newDocument(String created, String updated, String ttl, String tags, String detail, String cited) {
        Map<String,String> atts = new HashMap<String,String>(5);
        
        // this is independent of lucene internals where id can change - this will not change unless by app
        atts.put(Field.ID.key(), UUID.randomUUID().toString());
        
        atts.put(Field.CREATED.key(),  created);
        atts.put(Field.UPDATED.key(), updated);
        atts.put(Field.TITLE.key(), ttl);
        atts.put(Field.TAG.key(), tags);
        atts.put(Field.FULLTEXT.key(), detail);
        atts.put(Field.CITATION.key(), cited);
        return data.addDocument(atts, currentCollection);
    }

	@Override
	public void handle(Event event) {
		if(event.getEventType().equals(EVENT_BIT_HIT_SELECTED))
		{
			log.info(EVENT_BIT_HIT_SELECTED + " triggered");
		}		
	}

}
