package cardGame;

import java.awt.image.BufferedImage;
import java.time.Duration;

import javafx.animation.TranslateTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

/*
 * CardTable holds the BoardManager, and all the canvas display functions.
 * Initializing sets the size of the table both for the number of players and the size of the canvas
 * 
 * The idea is that the update functionts can call displayCard(nplayer, card) and the game player nplayer will show
 * up with the right card at that seat in the table.
 */
public class CardTable {
	int nplayers;
	BoardManager boardManager=null;
	public Canvas gameCanvas;
	double[] vertices;
	
	CardTable(int n, double width, double height) {
		nplayers = n;
		boardManager = new BoardManager(nplayers);
		gameCanvas = new Canvas(width, height);
		displayGameCanvas(gameCanvas, height, width);
		switch (nplayers) {
		case 4:
			makeRectTable(width, height);
			break;
		case 6:
			makeHexTable(width, height);
			break;
		default:
			System.out.println("Can't Happen! unrecognized CardTable nplayers:" + nplayers);				
			}
		
		}
	
	int rotation;
	double angleOfRotation;
	void makeRectTable(double width, double height) {
		//Rectangle rect=new Rectangle((double) 22.5, (double) 35.);
		//
		// Does this work? We are assigning a local object into a persistent one...
		double w=width/2;
		double h=height/2;
		double[] vertices4 =  
			{	w, 0.0,
				w + 10, h/2,
				w-100, h-20,
				0, h/8
			};
		rotation = 90;
		vertices=vertices4;
		}
	
	void makeHexTable(double width, double height) {
		Hexagon hex = new Hexagon((double) 22.5, (double) 35.);	// ratio of a bridge card
        double h = height / 2.;
        double w = width / 2.;

		hex.computePoints(w, h);
		vertices = hex.getPoints(w, h);

		rotation = 60;
		}
	
	/*
	 * Write splash screen on Game Canvas cn
	 * Note (as written) this is called in the GUI thread
	 * (was mkSplashScreen)
	 * TODO: distinguish between board display and BoardManager game operations
	 */
	void displayGameCanvas(Canvas cn, double initialHeight, double  initialWidth) {
		
		cn.setWidth(initialWidth);
		cn.setHeight(initialHeight);
		GraphicsContext gc = cn.getGraphicsContext2D();
		 
		gc.setFill(Color.ALICEBLUE);
		gc.fillRect(0, 0, initialWidth, initialHeight);

		//gc.fillRect(75,75,100,100);
	     //cn.setStyle("-fx-background-color: aliceblue;");
	     

			double firstSeat = initialWidth / 2; 
			Polygon p = new Polygon();
			/*
			 * polygon.getPoints().addAll(new Double[]{ 0.0, 0.0, 20.0, 10.0, 10.0, 20.0 });
			 */
			
			Hexagon h = new Hexagon((double) initialWidth, (double) initialHeight);
			h.computePoints((double) initialWidth, (double) initialHeight);
			//Polygon hexagon = new Polygon(h.getPoints(initialWidth, initialHeight));
			gc.setFill(Color.BLACK);
			gc.fillPolygon(h.getPointsX(), h.getPointsY(), 6);
			

			//
			// Write something at each vertex...
			// I.e. getpoints again and then modify so that I can print the coords within
			// the canvas...
			// this is to debug Hexagon...
			// yyy
			double[] vertices = h.getPoints(initialWidth, initialHeight);
			int v, v2;
			int fsize = 12;
			Font labelFont = new Font("Verdana", fsize);
			gc.setFont(labelFont);
			Text l = null;
			String sLabel = "";
			double labelWidth = 0.0;
			double labelHeight = 0.0;

			for (v = 0; v < 6; v++) {
				int x, y;
				x = v * 2;
				y = x + 1;

				l = new Text(); // New text to use as a label of the vertex
				l.setFont(labelFont);
				final double lwidth = l.getLayoutBounds().getWidth();
				final double lheight = l.getLayoutBounds().getHeight();
				/*
				 * Push the y-label for the vertices inside the height of the printable canvas
				 */
				if (vertices[y] <= lheight) { // to vertex
					vertices[y] = vertices[y] + lheight;
				}
				if (vertices[y] >= initialHeight - 1)
					vertices[y] = initialHeight - 1;
				l.setX(vertices[x]);
				l.setY(vertices[y]);
				l.setFill(Color.RED);
				sLabel = "(" + (int) vertices[x] + "," + (int) vertices[y] + ")";
				l.setText(sLabel);		
				
				gc.setStroke(Paint.valueOf("red"));
				gc.strokeText(sLabel, vertices[x], vertices[y]);
				//root.getChildren().add(l);
			}
			
			//
			// write window coordinates at the lower right of the window.
			l = new Text(); // Text
			// l.setcolor?

			double vx = initialWidth; // first approx...
			int vy = (int) initialHeight - 1; // bottom-right
			sLabel = "(" + (int)vx + "," + (int)vy + ")";
			/*
			 * recompute labelWidth down to a gnat's ass...
			 */
			//labelWidth = l.getLayoutBounds().getWidth();
			vx = initialWidth - labelWidth;
			l.setText(sLabel);
			l.applyCss();
			double width = l.getLayoutBounds().getWidth();
			vx = initialWidth - width;
			//l.setX(vx);
			sLabel = "(" + (int)vx + "," + (int)vy + ")";
			gc.setStroke(Paint.valueOf("green"));
			gc.strokeText(sLabel, vx-12.0, vy);	// hack...

	     return;
		}	// displayGameCanvas
	
	private void rotate(GraphicsContext gc, double angle, double px, double py) {
        Rotate r = new Rotate(angle, px, py);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
    }
	private void drawRotatedImage(GraphicsContext gc, Image image, double angle, double tlpx, double tlpy, double tlpwidth, double tlpheight) {
        gc.save(); // saves the current state on stack, including the current transform

        //rotate(gc, angle, tlpx + image.getWidth() / 2, tlpy + image.getHeight() / 2);	// rotate at center of image...
        rotate(gc, angle, tlpx + tlpwidth, tlpy + tlpwidth);	// rotate at center of image...
        /*
         * Ok, so if the dimensions push it off the table, push back on
         */
        /*
         * pre-rotation dimensions
         */
        double maxX = tlpx + image.getWidth() / 2;
        double maxY = tlpy + image.getHeight() / 2;
        double cwidth = gc.getCanvas().getWidth();
        double cheight = gc.getCanvas().getHeight();
        if (maxX > cwidth)
        	tlpx -= (maxX - cwidth);
        if (maxY > cheight)
        	tlpy -= maxY - cheight;
        gc.drawImage(image, tlpx, tlpy, 
        		tlpwidth, tlpheight);
        gc.restore(); // back to original state (before rotation)
    }

	/*
	 * display a (scaled) card at (x,y) with rotation
	 */
	void displayCard(Card c, double x, double y, double rotation) {
        GraphicsContext gc=gameCanvas.getGraphicsContext2D();
		BufferedImage bi=CardFace.getCardFace(c.cardindex);
		Image image = SwingFXUtils.toFXImage(bi, null);		
		double w=22.5;
		double h=35.;
        drawRotatedImage(gc, image, rotation, x, y, 5.*w, 5.*h);		
		}
	
	/*
	 * display card c at (for the moment) vertexposition seatPosition and proper rotation
	 */
	void displayCard(int seatPosition, Card c) {
		int i, r=0;
		for (i=0; i<seatPosition; i++)
			r += rotation;
		displayCard(c, vertices[2*seatPosition], vertices[2*seatPosition+1], r);
		}	// displayCard
	
/*	void rake() {
		double w=22.5;
		double h=35.;
		Rectangle r=new Rectangle(vertices[0], vertices[1], 5*w, 5*h);
		TranslateTransition t=new TranslateTransition();
		
		} */

}	// CardTable
