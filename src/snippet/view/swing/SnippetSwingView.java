package snippet.view.swing;

import javax.swing.*;

import snippet.SnipbitLogger;
import snippet.Statics;
import snippet.data.SnippetDocument;
import snippet.data.solr.SolrDataAccess;

import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

public class SnippetSwingView implements SnipbitLogger {

    JPanel SnipbitRootPanel;
    JTabbedPane tabs;

    // atts of the detail tab
    private JTextField created = new JTextField();
    private JTextField title = new JTextField();
    private JTextField tags = new JTextField();
    private JTextArea text = new JTextArea();
    private JTextField source = new JTextField();

    private JTextField searchString;
    private JList queryResults = new JList<SnippetDocument>();
    private JPanel configTab;
    private JPanel browseTab;
    private JPanel detailTab;
    private JTextField solrUrl = new JTextField();
    private JButton testButton;
    private JButton newDocumentButton;
    private JButton commitButton;
    //private JLabel status;

    private SnippetDocument current = new SnippetDocument();

    public SnippetSwingView() {
        SolrDataAccess data = new SolrDataAccess();

        solrUrl.setText(data.getUrl());
        created.setText("20180505");
        title.setText("title information");
        tags.setText("tag1, tag2, tag3..");
        text.setText("what we have here... is failure to communicate.");
        source.setText("wikipedia.com");

        try {
            //queryResults.setListData(new Vector(data.queryDocumentsAsList(null /* NOT UPDATED AS OF IMPLEMENTING QUERY LOGIC */, "NOT IMPLEMENTED AS OF DYNAMIC COLLECTION UPDATE")));
        	queryResults.setListData(new Vector(data.queryDocumentsAsList("the", "computation")));
        }
        catch(IOException io) {
            log.info("Unable to reach index - double check the config tab");
        }

        queryResults.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                current = (SnippetDocument) queryResults.getSelectedValue();

                created.setText(current.getCreated());
                title.setText(current.getTitle());
                tags.setText(current.getTag());
                source.setText(current.getCitation());
                text.setText(current.getText());

                tabs.setSelectedIndex(1);

                //configTab.repaint();
                //configTab.revalidate();

            }
        });
        commitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("Action on commit button: " + e.getActionCommand());
            }
        });
    }
}
