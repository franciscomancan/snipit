package snippet.view.fx;

import java.io.File;
import java.io.PrintStream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import snippet.Statics;

/**
 * https://docs.oracle.com/javase/8/javase-clienttechnologies.htm
 */
public class SnippetFxMain extends Application implements Statics {	
	
    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("snippet.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle(currentTitle + collections.get(0));
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.getIcons().add(new Image("ninjaGaiden.gif"));
        primaryStage.setResizable(false);
        primaryStage.show();
               
        Controller c = loader.getController();
        c.rootStage = primaryStage;
        c.initialize();
    }

    public static void main(String[] args) {
    	try(PrintStream log = new PrintStream(new File(DefaultLogFile))) 
    	{
    		System.setOut(log);
        	System.setErr(log);
        	System.in.close();
        	launch(args);
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	System.out.println(SnippetFxMain.class.getSimpleName() + ": main method complete.");
    }
}
