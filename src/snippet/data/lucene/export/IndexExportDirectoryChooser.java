package snippet.data.lucene.export;

import java.awt.Dimension;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import javafx.stage.DirectoryChooser;
import snippet.data.lucene.LuceneDataAccess;

public class IndexExportDirectoryChooser
{
	static String dialogueTitle = "Select the root index folder to export";
	static String openInPath = "/";
	static boolean allowMultiSelect = false;
	
	public static void main(String[] args) throws Exception {

		JFrame frame = new JFrame(dialogueTitle);		
		JFileChooser chooser = new JFileChooser(openInPath);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);		
		//chooser.setMultiSelectionEnabled(allowMultiSelect);
		chooser.setFileHidingEnabled(false);
		chooser.setToolTipText(dialogueTitle);
		chooser.showOpenDialog(frame);
		chooser.setPreferredSize(new Dimension(350, 500));
		chooser.setApproveButtonText("Select");
		chooser.setDialogTitle(dialogueTitle);		
		
		frame.setPreferredSize(new Dimension(350, 500));
		
		File file = chooser.getSelectedFile();
		System.out.println("Directory chosen: " + file.getCanonicalPath());		
		
		String home = System.getProperty("user.home");
		System.out.println("user.home: " + home);
		
		String result = new LuceneDataAccess().exportFullIndex(file.getAbsolutePath(), "need to figure this out");
		
		System.out.println(result);
		
		System.exit(0);

	}

}
