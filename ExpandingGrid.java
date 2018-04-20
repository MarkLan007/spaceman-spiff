package cardGame;

import java.awt.image.BufferedImage;
import java.util.Random;

//import javax.swing.text.html.ImageView;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ExpandingGrid extends Application {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Expanding Grid");
		ExpandingGridScene expandingGrid= new ExpandingGridScene();
		primaryStage.setScene(expandingGrid);
		primaryStage.show();		
		}
	
	public class ExpandingGridScene extends Scene {
	private static final double defaultSceneHeight=450;	// cannot call in constructor unless static...
	private static final double defaultSceneWidth=520;
	BorderPane contentPane;
	//FlowPane contentPane;
	

	//int[] arr = {1, 2, 3, 4, 5};
	Color[] randomColors = {
			Color.ALICEBLUE,	
			Color.GREEN,
			Color.BLUE,	
			Color.YELLOW,
			Color.AZURE,
			Color.AQUAMARINE,
			Color.PALETURQUOISE,
			Color.BEIGE,
			Color.BISQUE,
			Color.PURPLE,
			Color.BLACK,
			Color.BLANCHEDALMOND,
			Color.PEACHPUFF,
			Color.SALMON,
			Color.DARKSALMON,
			Color.SEAGREEN,
			Color.CORNFLOWERBLUE,
			Color.BROWN,
			Color.GAINSBORO,
			Color.DARKGOLDENROD,
			Color.DARKCYAN,
			Color.PINK,
			Color.HOTPINK,
			Color.HONEYDEW,
		};
	
	Random rand = new Random();	// TODO: should seed with system seconds.... 
	private int randomColorIndex() {
	int iBound=randomColors.length-1;
  	int  n = rand.nextInt(iBound) ;
  	return n;
  	}

	/*
	 * Does this work?
	BorderPane pane = new BorderPane();
    ImageView img = new ImageView("http://...");

    img.fitWidthProperty().bind(stage.widthProperty()); 

    pane.setCenter(img);

    Scene scene = new Scene(pane);
    stage.setScene(scene);
    stage.show();
	 */
	
	FlowPane flowPane=null;
	Button[] buttonArray = new Button[20];

	private void doCardButtonPressed(ActionEvent ae) {
		// Figure out which button I am, and then try to play that card...
		System.out.println("Whoa Baby! Button Pressed:" + ae);
		Button b=(Button) ae.getSource();
		int index = (int) b.getUserData();
		String s="";
		if (buttonArray[index] == b) {
			s = " Checks out!";
		}
		else
			s = "But Doesn't check out...";
		System.out.println("Found the button: " + index + s);
		flowPane.getChildren().remove(b);

	}
	
	// Constructor
	public ExpandingGridScene() {
		super(new Group(), defaultSceneWidth, defaultSceneHeight);
		contentPane = new BorderPane(); 
		int i;
		
		// TODO: Top. Toolbar
		// Center Stage
		Button centerNode=new Button("Center Stage");
		centerNode.setMaxHeight(Double.MAX_VALUE);
		centerNode.setMaxWidth(Double.MAX_VALUE);
		/*
		 * This doesn't work...
		centerNode.setPrefHeight(Double.MAX_VALUE);
		centerNode.setPrefWidth(Double.MAX_VALUE);
		 */
		centerNode.setMinSize(200,  200.);
		centerNode.setPrefHeight(300.);
		centerNode.setPrefWidth(300.);
	    //centerNode.fitWidthProperty().bind(stage.widthProperty()); 
		contentPane.setCenter(centerNode);
		contentPane.setCenterShape(true);
		
		// Stage Right	
		// Stage Right as a FlowPane
		flowPane = new FlowPane();
		// Standard Bridge Card is 2.25 x 3.5
		final double cardRatioHeight = 3.5;
		final double cardRatioWidth = 2.25;
		for (i=0; i<17; i++) {
			BufferedImage bi=CardFace.getCardFace(i);
			Image image = SwingFXUtils.toFXImage(bi, null);
			ImageView imageView = new ImageView(image);
			Button b=new Button();
			b.setUserData((Object) i);
			buttonArray[i] = b;

			//
			// save-off the card so that I can find it and delete it when it's played...
			b.setOnAction(new EventHandler<ActionEvent> () {
			    @Override 
			    public void handle(ActionEvent e) {
			        //label.setText("Accepted");
			    	doCardButtonPressed(e);
			    }});

			ImageView iv3 = new ImageView();
	         iv3.setImage(image);
	         double cHeight=cardRatioHeight * 20;
	         double cWidth=cardRatioWidth * 20;
	         Rectangle2D viewportRect = new Rectangle2D(1., 1., cHeight, cWidth);
	         iv3.setViewport(viewportRect);
	         //iv3.setRotate(90);	// Note for future use...
			b.setGraphic(iv3); 
			b.setMinWidth(5.5);
			b.setMaxWidth(cHeight);
			b.setMaxWidth(cWidth);
			b.setPrefHeight(35);
			flowPane.getChildren().add(b);
			}
	     flowPane.setPrefWrapLength(200); // preferred width = 300

		contentPane.setRight(flowPane);
		
		// Stage Bottom
		Button bottomNode=new Button("Bottom");
		contentPane.setBottom(bottomNode);
		
		Group root = (Group) this.getRoot();	
	    root.getChildren().add(contentPane);
		}
	
	public void ExpandingGridSceneOld() {
		//super(new Group(), defaultSceneWidth, defaultSceneHeight);
		//contentPane = new GridPane(); // GP	Note: Column Major Order!!!
		//contentPane = new FlowPane();
		contentPane.setPadding(new Insets(10,10,10,10));
		int i, j, k=0;
		for (i=0; i<3; i++) {
			//HBox hrow=new HBox();
			for (j=0; j<4; j++) {
				ResizableCanvasM rc=new ResizableCanvasM();
				//
				// Note: setting to Double.MAX_VALUE does nothing good...
				rc.setHeight(100);
				rc.setWidth(100);
				int index=randomColorIndex() ;
				rc.setColor(randomColors[index]);
				rc.drawCanvasContent();
				String labelString="Loc(" + i + "," + j + ")";
				rc.putString(labelString);
				Button b = new Button(labelString);
				b.setUserData((Object) k);
				buttonArray[k++] = b;
				//contentPane.add(b, i, j); // column=1 row=0
				//contentPane.add(rc,  i,  j);
				
				if (i == 2 && j == 2) {	// random button
					b.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
					}

				//contentPane.addRow(i,  rc);
				//contentPane.getChildren().add(rc);
				//hrow.getChildren().add(rc);
				//contentPane.getChildren().add(hrow);
				//HBox.setHgrow(rc, Priority.ALWAYS);
				
				}
			//HBox.setHgrow(hrow, Priority.ALWAYS);
			//hrow.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
			//contentPane.addRow(i,  hrow);
			}
		
		RowConstraints row1=new RowConstraints(100., 120., 120.);
		row1.setVgrow(Priority.ALWAYS);
		// RowConstraints(double minHeight, double prefHeight, double maxHeight)
		RowConstraints row2=new RowConstraints(100., 120., Double.MAX_VALUE);
		row2.setVgrow(Priority.SOMETIMES);
		//RowConstraints(double minHeight, double prefHeight, double maxHeight, Priority vgrow, VPos valignment, boolean fillHeight)
		RowConstraints row3=new RowConstraints(100, 120, Double.MAX_VALUE, Priority.ALWAYS, VPos.CENTER, true);
		//gridpane.getColumnConstraints().addAll(column1, column2);
		
		//contentPane.getRowConstraints().addAll(row1, row2, row3);
		
			
	Group root = (Group) this.getRoot();	
    root.getChildren().add(contentPane);
	}
	
	private class ResizableCanvasM extends Canvas {
		private double minCardtableHeight=100.0;

		Color instanceColor=null;
		
		void drawCanvasContent() {
			GraphicsContext gc = getGraphicsContext2D();
			 
			if (instanceColor == null)
				instanceColor = Color.ALICEBLUE;
			gc.setFill(instanceColor);
			//gc.fillRect(0, 0, 100, 100);

			gc.fillRect(0, 0, super.getWidth(), super.getHeight());

			}
		
		public void putString(String s) {
			int fsize = 12;
			Font labelFont = new Font("Verdana", fsize);
			GraphicsContext gc=getGraphicsContext2D();
			gc.setStroke(Paint.valueOf("green"));
			gc.strokeText(s, 10, 10);	// hack...
			}
		
		public void setColor(Color c) {
			GraphicsContext gc = getGraphicsContext2D();
			instanceColor = c;
			gc.fillRect(0, 0, super.getWidth(),super.getHeight());
			}
		
		ResizableCanvasM() {
			super();
			drawCanvasContent();
			}
		
		@Override
		public double minHeight(double width)
		{
		    return minCardtableHeight;
		}

		@Override
		public double maxHeight(double width)
		{
		    return Double.MAX_VALUE;
		}

		/*
		 * Make it greedy...
		 */
		@Override
		public double prefHeight(double height)
		{
		    return height; //Double.MAX_VALUE;
		}

		@Override
		public double prefWidth(double width)
		{
		    return width; //Double.MAX_VALUE;
		}

		@Override
		public double minWidth(double height)
		{
		    return 100;
		}

		@Override
		public double maxWidth(double height)
		{
		    return Double.MAX_VALUE;
		}

		@Override
		public boolean isResizable()
		{
		    return true;
		}

		@Override
		public void resize(double width, double height)
		{
		    super.setWidth(width);
		    super.setHeight(height);
		    //ClientScene.mkSplashScreen(this, (int)width, (int)height);
		    
		    //
		    // Paint? Can't resolve paint...
		    //paint();
		}
		//Note that the resize method cannot simply call Node.resize(width,height), because the standard implementation is effectivele empty.
		// from stackoverflow: https://stackoverflow.com/questions/23449932/javafx-resize-canvas-when-screen-is-resized
	}
	// - Resizable Canvas

	}

}
