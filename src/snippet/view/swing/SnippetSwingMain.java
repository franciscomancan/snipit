package snippet.view.swing;

import javax.swing.*;
import java.awt.*;

public class SnippetSwingMain {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        JFrame frame = new JFrame("snipbit");
        frame.setResizable(false);
        frame.setMaximumSize(new Dimension(800,600));
        frame.setContentPane(new SnippetSwingView().SnipbitRootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
