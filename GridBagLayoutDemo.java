package cardGame;


//package layout;
 
/*
 * GridBagLayoutDemo.java requires no other files.
 */
 
import java.awt.*;
import javax.swing.JButton;
import javax.swing.JFrame;
 
public class GridBagLayoutDemo {
    final static boolean shouldFill = true;
    final static boolean shouldWeightX = true;
    final static boolean RIGHT_TO_LEFT = false;
 
    public static void addComponentsToPane(Container pane) {
        if (RIGHT_TO_LEFT) {
            pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
 
        JButton button;
    pane.setLayout(new GridBagLayout());
    /*
     * Make each constraint from a different instance
    GridBagConstraints c = new GridBagConstraints();
    if (shouldFill) {
    //natural height, maximum width
    c.fill = GridBagConstraints.VERTICAL;
    }*/
 
    button = new JButton("Button 1");
    GridBagConstraints c1 = new GridBagConstraints();
    if (shouldFill) {
    //natural height, maximum width
    c1.fill = GridBagConstraints.HORIZONTAL;
    }
    
    if (shouldWeightX) {
    c1.weightx = 0.5;
    }
    c1.weightx = 0.5;
    c1.fill = GridBagConstraints.HORIZONTAL;
    c1.gridx = 0;
    c1.gridy = 0;
    pane.add(button, c1);
 
    button = new JButton("Button 2");
    GridBagConstraints c2 = new GridBagConstraints();
    if (shouldFill) {
    //natural height, maximum width
    c2.fill = GridBagConstraints.HORIZONTAL;
    }

    c2.fill = GridBagConstraints.HORIZONTAL;
    c2.weightx = 0.5;
    c2.gridx = 1;
    c2.gridy = 0;
    pane.add(button, c2);
 
    button = new JButton("Button 3");
    GridBagConstraints c3 = new GridBagConstraints();
    if (shouldFill) {
    //natural height, maximum width
    c3.fill = GridBagConstraints.HORIZONTAL;
    }

    c3.fill = GridBagConstraints.HORIZONTAL;
    c3.weightx = 0.5;
    c3.gridx = 0;
    c3.gridy = 3;
    pane.add(button, c3);
 
    button = new JButton("Long-Named Button 4");
    GridBagConstraints c4 = new GridBagConstraints();
    if (shouldFill) {
    //natural height, maximum width
    c4.fill = GridBagConstraints.HORIZONTAL;
    }

    c4.fill = GridBagConstraints.HORIZONTAL;
    c4.ipady = 40;      //make this component tall
    c4.weightx = 0.0;
    c4.gridwidth = 3;
    c4.gridx = 0;
    c4.gridy = 1;
    pane.add(button, c4);
 
    button = new JButton("5");
    GridBagConstraints c5 = new GridBagConstraints();
    if (shouldFill) {
    //natural height, maximum width
    c5.fill = GridBagConstraints.HORIZONTAL;
    }

    c5.fill = GridBagConstraints.HORIZONTAL;
    c5.ipady = 0;       //reset to default
    c5.weighty = 1.0;   //request any extra vertical space
    c5.anchor = GridBagConstraints.PAGE_END; //bottom of space
    c5.insets = new Insets(10,0,0,0);  //top padding
    c5.gridx = 1;       //aligned with button 2
    c5.gridwidth = 2;   //2 columns wide
    c5.gridy = 1;       //third row
    pane.add(button, c5);
    }
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("GridBagLayoutDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Set up the content pane.
        addComponentsToPane(frame.getContentPane());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}