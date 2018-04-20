package cardGame;

import java.awt.Dialog.ModalityType;
import java.awt.image.BufferedImage;
//import java.awt.Canvas;
//import java.awt.event.ActionEvent;
import javafx.scene.transform.Rotate;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.net.*;
import java.io.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
//import cardGame.JCFrameClient.Hexagon;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ClientScene extends Scene {
	// clip
	BorderPane contentPane;
	TextField textField;
	Label promptLabel;

	int nTableSize=4;
	
	//Canvas canvasTop = null;
	//Canvas canvasBottom = null;
	//final Canvas gameCanvas;
	CardTable cardTable=null;
	
	final Canvas handCanvas;
	Button btnEnterButton = null;
	Button btnConnectButton = null;
	Label lblStatusLine = null;
	Label lblCardsLabel = null; // added to display cards right above the input text box; TODO: may conflict
								// with canvasBottom placement

	/*
	 * the input/output streams created with the server when user connects So the
	 * client will be doing its IO asynchrously also, through: abstract class
	 * AsynchronousSocketChannel that implements AsynchronousByteChannel
	 */

	/*
	 * supercedes attempts to usee AsynchronousSocketChannel in the client.
	 */
	URLConnectionReader urlConnectionReader=null;
	
	/*
	 * Thanks to http://www.baeldung.com/java-nio2-async-socket-channel for help
	 * with the asynch socket connections.
	 */
	AsynchronousSocketChannel sockINetConnection = null;
	Future<Void> future = null;
	Future<Integer> futurei = null;

	InputStream in = null; // simplify...

	BufferedReader inputINetStream;
	PrintWriter outputINetStream;
	boolean isConnected = false;

	ByteBuffer outbuff = ByteBuffer.allocateDirect(NIOClientSession.BUFFMAX);
	ByteBuffer inbuff = ByteBuffer.allocateDirect(NIOClientSession.BUFFMAX);

	final String CONNECTBUTTON_LABEL = "Connect";
	final String ENTERBUTTON_LABEL = "Enter";

	String oldString="";
	void updateStatusString(String st) {
		oldString = st;
		lblStatusLine.setText(st);
	}
	
	void appendStatusString(String st) {
		//String t=lblStatusLine.getText();
		lblStatusLine.setText(oldString + st);
		}

	void sendPlayCard(Card card) {
		String st;
		if (!isConnected) {	// Or not your_turn xxx
			updateStatusString("Can't play card " + card.encode() + ": Not connected.");
			return;
			}

		st = "Playing:" + card.encode();
		updateStatusString(st);
		int i;
		outbuff.clear();
		/*
		 * create an actual protocol message with the card, and send to server
		 * What's my PID here??? Doesn't need to know... Handled by server in NIO read functions...
		 */
		ProtocolMessage pm=new ProtocolMessage(ProtocolMessageTypes.PLAY_CARD, card);
		String s=pm.encode();
		for (i=0; i<s.length(); i++) {
			outbuff.putChar(s.charAt(i));
			}
		outbuff.putChar('\r');
		outbuff.putChar('\n');
		System.out.println("Trying to write:" + s);
		outbuff.flip();
		urlConnectionReader.write(s);
		
		}
		
	
	/*
	 * The action taken on an enterButton press or the action pushbutton
	 */
	static int iTrick=0;

	private void doSendMethod() {
		String st = textField.getText();
		if(st.startsWith("trick6")) {
			displayTrick(6, 6, null);
			return;
			}
		else if (st.startsWith("clear")) {
			clearTrick(4);
			return;
			}
		else if (st.startsWith("trick4")) {
			displayTrick(6, 4, null);
			return;
			}
		else if (st.startsWith("d")) {
			processEnqueuedStrings();
			return;
			}
		else if (st.startsWith("card")) {
			Card c=Subdeck.randomCard();
			cardTable.displayCard(iTrick, c);
			iTrick++;
			if (iTrick>=nTableSize) iTrick=0;
			return;
			}
		else if (st.startsWith("trick")) {
			// TODO: Put stub for trick-update here
			Trick t=new Trick(0);
			t.bHeartsBroken = true;
			displayTrickUpdate(t);
			return;
			}
		/* else if (st.startsWith("d")) {
			String s1=st.substring(1);
			int i = Integer.parseInt(s1);
			System.out.println("Deleting button: " + i);
			return;
			} */
		if (isConnected) {
			// outputINetStream.println(st);
			/*
			 * For now just send whatever is typed to the server... Obviously temporary
			 */
			updateStatusString(st);
			// lblStatusLine.setText(st);
			int i;
			//outbuff.clear();
			/*
			 * create an actual protocol message with the card, and send to server
			 * What's my PID here??? Doesn't need to know... Handled by server in NIO read functions...
			 */
			Card c=new Card(st);
			/*
			// delete following:
			ProtocolMessage pm=new ProtocolMessage(ProtocolMessageTypes.PLAY_CARD, c);
			String s=pm.encode();
			for (i=0; i<s.length(); i++) {
				outbuff.putChar(s.charAt(i));
				}
			outbuff.putChar('\n');
	
			//System.out.println("Trying to write:" + s);
			outbuff.flip();
			*/
			// xyzzy -- is this used?
			/* 
			 * sockINetConnection.write(outbuff);
			 * Yes, but it shouldn't be... Instead:
			 */
			sendPlayCard(c);			
			}
		textField.setText("");
		if (!isConnected)
			lblStatusLine.setText("Not Connected>" + st);
	}

	// for javafx +
	
	EventHandler<ActionEvent> sendHandler = new EventHandler<ActionEvent>() {
	    @Override
	    public void handle(ActionEvent event) {
	    	doSendMethod();
	        //label.setText("Accepted");
	        event.consume();
	    }
	};
	
	//You can now add your defined buttonHandler to the onAction of your button via:
	//button.setOnAction(sendHandler);	
	
	// for javafx -
	/*
	AbstractAction doF2 = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Yay! Got a super-double-secret alt-F2!");
			lblStatusLine.setText("Yay! Got a super-double-secret alt-F2!");
			// do nothing
		}
	};
	*/
	
	void doF2Method() {
		System.out.println("Yay!! Got a super-double-secret alt-F2!");
		lblStatusLine.setText("Yay!! Got a super-double-secret alt-F2!");		
		}

	void serverCrashed() {
		btnConnectButton.setDisable(true);

		System.err.println("Server crashed. Reconnect.");
		lblStatusLine.setText("Server crashed. Try again.");
		// System.exit(0);
	}

	/*
	 * statusUpdate -- rewrite the text in the screen
	 */
	// TODO: These seem to be unused...
	final int canvasWidth = 550;	// was 424; 
	final int canvasHeight = 150;	// was 200 in the old JPanel Swing country

	Text fxText = new Text();
	private void statusUpdate(String s) {
		fxText.setText(s);
	}
	
	void processServerLine(String s) {
		System.out.println("enqueServerLine>>>" + s);
		// works with buffered lines:
		//  enqueue a whole line for processor
		enqueStringToProcess(s);		
		}
	
	/*
	 * Assumed to be run on it's own thread, this can unbundle long server packets into individual 
	 * processable commands, and enqueue them for later work
	 */
	static String savePartialPacket="";
	void processLongServerString(String s) {
		int i, lim=s.length();
		String str="";
		
		for (i=0; i<lim; i++) {
			char c = s.charAt(i);
			if (c == '\n' || c == '\r' || c == '\0') {
				// if I'm at a break char, and not at the end of the string, there are multiple
				// commands in the read!
				// Gack!
				// process each in order before initiating another read!
				// TODO: suppose there are multiple lines and it ends with a partial? Fuck me.
				if (str.length() > 0) {
					String rString="";
					if (i < lim)
						rString = " with " + (lim - i) + "chars remaining in packet...";
					System.out.println("read>>>" + str + rString);
					// enqueue for processor
					enqueStringToProcess(savePartialPacket + str);
					savePartialPacket = "";
					//processServerString(str);
					}
				str = "";
			} else if (!isprintable(c))
				continue; // ignore everything after the first of the unprintable buffer junk that gets
							// put into network packets
			else
				str = str + c;
		}
		if (str.length() > 0) {
			// we have a partial...
			System.out.println("saving partial>>>" + "old:" + savePartialPacket + " new:" + str);
			savePartialPacket = savePartialPacket + str;

			/*
			System.out.println("read partial>>>" + str);
			// enqueue for processor
			enqueStringToProcess(str);
			//processServerString(str);
			 * 
			 */
			}
		//
		// Only allow another read to start after all these are processed...
		readInProgress = false;
		

		}

	LinkedBlockingQueue<String> lbq=new LinkedBlockingQueue<String>();
	
	void enqueStringToProcess(String s) {
		if (s.contains("HTTP")) {
			System.out.println("Discarding:<" + s + ">");
			return;
			}
		System.out.println("Enqueuing:<" + s + ">");
		if (s == null || s.length() == 0)
			return;
		lbq.add(s);		
		}
	
	void processEnqueuedStrings() {
		String s="";
		boolean work=true;
		//
		// check to see if there is data to be read...
		// favor enqueing things to processing them
		while (work) {
			if (lbq.isEmpty())
				break;
			try {
				s = (String) lbq.take();
				System.out.println("dequeuing:{" + s + "}");
				processServerString(s);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("interrupt/crash in dequeue");
				e.printStackTrace();
				}
			}
		}
	
	void processServerString(String s) {
		int i, len;
		Card c;
		if (s == null || s.length() == 0)
			return;
		System.out.println("processing(" + s + ")");
		
		// TODO: (in progress) handle hand messages here; update updates the board (bad idea to do hand here?)
			ProtocolMessage pm=new ProtocolMessage(s);
			switch (pm.type) {
			case ADD_CARDS:
				if (pm.subdeck == null) {
					System.out.println("processServerString: unrecognized message" + s);					
					return;
					}
				// add to cards array, and update the flowPlane
				Platform.runLater(new Runnable() {
					  @Override public void run() {
						  // reset flowplane and populate with these cards
						  //flowPane.xxx
							String ss=pm.subdeck.encode();	// round trip...
							System.out.println("cards>>>" + ss);

							showCards(ss);
							// Make a subdeck, sort it and display it
							clearFlowPane();
							addCardstoHandPanel(pm.subdeck);
					  }
					});
				break;
			case DELETE_CARDS:
				System.out.println("delete message seen:");
				
				len=pm.subdeck.size();
				for (i=0; i<len; i++) {
					c=pm.subdeck.pop();
					System.out.println("ClientScene:<" + c.rank + c.suit + ">.");
					//sTemp = sTemp + "<" + c.rank + c.suit + ">" ;
					Card t=c;
					Platform.runLater(new Runnable() {
						  @Override public void run() {
								deleteCard(t);	// xxx
						  }
						});
					}
				break;
			case PLAYER_ERROR:
				// These are messages from the screen that should be displayed in the status line
				Platform.runLater(new Runnable() {
					  @Override public void run() {
						  updateStatusString(pm.usertext);
					  }
					});
				break;
			case YOUR_TURN:
				Platform.runLater(new Runnable() {
					  @Override public void run() {
						  updateStatusString("Your turn!");
					  }
					});

				break;
			case CURRENT_TRICK:
				Card cTemp=new Card(Rank.QUEEN, Suit.SPADES);
				if (pm.subdeck != null) {
					if (pm.subdeck.size() > 1) {
						// Not really a current trick... it's a trick update. Ignore...
						//clearTrick(4);
						break;
						}
					cTemp=pm.subdeck.pop();
					}
				final Card ct=cTemp;
				Platform.runLater(new Runnable() {
					  @Override public void run() {
							cardTable.displayCard(pm.sender, ct);
					  }
					});
				
				System.out.println("Updating seat:" + pm.sender + " Card:" + cTemp.encode());
				break;
			case TRICK_CLEARED:	// TODO: clientScene Trick Cleared...
				System.out.println("Trick_cleared: " + pm.usertext);
				String str=pm.usertext;
				Trick t=pm.trick;
				if (t != null) {
					str = t.encode(true);
					}
				final String fstr=str;	// One of life's many indignities...
				Platform.runLater(new Runnable() {
					  @Override public void run() {	
							showCards(fstr);
							clearTrick(4);
					  	}
					  });				
				break;
			default:
				BoardManager.update(pm);	// TODO: this is in the wrong place
				;
			}
			
	}

	/*
	 * backgroundReader if a read hasn't been initiated, initiate one; if it has,
	 * check isdone(); when done(), then send the data to the app; initiate another
	 * read -- Note that there are two guards here -- one for the wrapper
	 * (inprogress) and one for within (readinprogress) to avoid accessing the same
	 * data structures at the same time.
	 */
	private boolean readInProgress = false;
	static boolean inProgress = false;

	private void backgroundReaderWrapper() {
		if (inProgress)
			return;
		else {
			inProgress = true;
			backgroundReader();
			processEnqueuedStrings();	// here? zzz
			inProgress = false;
		}
	}

	private void initiateRead() {
		readInProgress = true;
		inbuff.clear();
		futurei = sockINetConnection.read(inbuff);// .read(dst, timeout, unit, attachment, handler); //
													// .getInputStream();
	}

	static boolean isprintable(char c) {
		if (c >= ' ' && c <= '~')
			return true;
		return false;
	}

	private void backgroundReader() {

		String sImmed="";
		if (!readInProgress) {
			readInProgress = true;
			initiateRead();
			sImmed="Immedeate ";
			return;
		}

		if (futurei.isDone())
			System.out.println(sImmed + "Read Done -> won't block!");
		else {
			/*
			 * Not done; just return; don't block the gui!
			 */
			return;
		}
		int iBytes;
		try {
			iBytes = futurei.get();
			System.err.println("read [" + iBytes + "] chars.");
			if (iBytes == -1) {
				System.out.println("Read EOF. Can't happen. Network connection");
				iBytes = 0;
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			System.err.println("Shouldn't happen; blocked behind io despite best efforts. Server crash?");
			serverCrashed();
			return;
		}

		/*
		 * Now try to get the data out of the buffer...
		 */
		inbuff.flip();
		String str = "";
		// int lim=inbuff.limit() - 3;
		int lim = iBytes / 2; // actual number of characters read -- getChar reads 16 bits believe it or
								// not...
		/*
		 * get the whole buffer and let process long server string break it up...
		 * 
		 */
		String strX="";
		for (int j = 0; j < lim; j++) {
			char c = inbuff.getChar();
			strX = strX + c;
			}
		String t=strX;
		//Ok, try this by processing the packet in the background, and initiating a new read immediately
		// I doubt this will work. will lost packets...
		/*
		Platform.runLater(new Runnable() {
			  @Override public void run() {
			  */
					processLongServerString(t);
					// processLong simply enqueues...
			  	/*}
			  });*/

		//initiateRead();
		
		/*
		for (int j = 0; j < lim; j++) {
			char c = inbuff.getChar();
			if (c == '\n' || c == '\r' || c == '\0') {
				// if I'm at a break char, and not at the end of the string, there are multiple
				// commands in the read!
				// Gack!
				// process each in order before initiating another read!
				// TODO: suppose there are multiple lines and it ends with a partial? Fuck me.
				if (str.length() > 0) {
					String rString="";
					if (j < lim)
						rString = " with " + (lim - j) + "chars remaining in packet...";
					System.out.println("read>>>" + str + rString);
					String xx=str;
					Platform.runLater(new Runnable() {
						  @Override public void run() {
								processServerString(xx);
							  
						  }
						});
					
				}
				str = "";
			} else if (!isprintable(c))
				continue; // ignore everything after the first of the unprintable buffer junk that gets
							// put into network packets
			else
				str = str + c;
		}
		if (str.length() > 0) {
			// we have a partial...
			System.out.println("read partial>>>" + str);
			processServerString(str);
		}
		*/

		/*
		 * There are no reads going on because we just cleared the buffer... initiate
		 * one.
		 */
		// initiateRead();
		/*
		 * no, just wait to get polled again
		 * ... we could still get run over
		 * ... probably should reset this after the processing is run...
		 */
		// readInProgress = false;
	}

	public void doConnectMethod() {
		String command = "Connect";
		
		lblStatusLine.setText(command + "ing...");
		getServerInfo();
		ClientConnectDialog dialog = new ClientConnectDialog();
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		// ModalityType.APPLICATION_MODAL
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		ModalityType type = dialog.getModalityType();
		dialog.setVisible(true);
		int portId;
		// dialog.wait();
		if (dialog.dialogStatus) {
			System.out.println(
					"Connecting to " + dialog.serverName + " Game: " + dialog.gameName + " Port: " + dialog.portId);
			// TODO: when you connect, make the connect button inactive, until you are
			// disconnected...
			isConnected = true;
			portId = Integer.parseInt(dialog.portId);
			urlConnectionReader = new URLConnectionReader(dialog.serverName, 1081, this);
			// expect reconstruction here
			// aaa
			if (urlConnectionReader.checkConnection())
				urlConnectionReader.backgroundReader();
			
			// aaa -- old and removed...
			/*
			try {
				sockINetConnection = AsynchronousSocketChannel.open();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// send servername and portid to URL
			InetSocketAddress hostAddress = new InetSocketAddress(dialog.serverName, portId);
			future = sockINetConnection.connect(hostAddress);

			int i = 0;
			while (!future.isDone()) {
				if (i > 3) {
					// admit defeat: no server
					System.out.println("Cannot connect. (try: " + i + ") ");
					lblStatusLine.setText("Cannot connect to Cardserver after multiple retries.");
					return;
				}
				System.out.println("Not Connected yet. (try: " + i + ")");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					// interrupted... ok?
					e.printStackTrace();
				}
				i++;
			}

			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Connected to Cardserver.");
			updateStatusString("Connected to Cardserver.");
			// lblStatusLine.setText("Connected to Cardserver.");
			// Make the connect button inactive xxx yyy

			btnConnectButton.setDisable(true);
			*/
			/*
			 * spawn port reader in its own thread to call backgroundReader
			 */
			//spawnBackgroundReader();
			}
	
		}
	
	//@Override
	public void actionPerformed(ActionEvent ev) {
		/*
		 * Which button was pressed
		 */
// Permanently broken; closed for business.
		String command = ""; // ev.getActionCommand();
		if (command.equals(CONNECTBUTTON_LABEL)) {
			// Try to connect to server...
		}

		else if (command.equals(ENTERBUTTON_LABEL)) {
			/*
			 * Just send what the user types to the server right now...
			 */
			doSendMethod();
		}

	}

	/*
	 * This gets called every 500 miliseconds
	 */
	int nreads = 0;
	Future<Integer> readResult = null; // = sockINetConnection.read(buffer);
	ExecutorService es = Executors.newFixedThreadPool(3);

	/*
	 * Background read routines 
	 */
	
	/*
	 * spawnBackgroundReader -- reworked with buffered read
	 */


	void spawnBackgroundReader() {
		//
		// Fuck me. This is correct syntax...
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			try {
				backgroundReaderWrapper();
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}, 0, 1, TimeUnit.MILLISECONDS);
	}

	/*
	 * getServerInfo -- get info on server to connect to
	 */
	void getServerInfo() {

		// Alert alert = new Alert(Alert.AlertType.ERROR);

	}

	private void showCards(String st) {
		// make sure you do this in the UI thread...
		Platform.runLater(new Runnable() {
			  @Override public void run() {
				  lblCardsLabel.setText(st);
			  }
			});

	}

	/*
	 * playCardToCanvas -- splat card onto the gamecanvas at the position
	 * yyy
	 */
	void playCardToCanvas(int oclock, Card c) {
		BufferedImage bi=CardFace.getCardFace(c.cardindex);
		Image image = SwingFXUtils.toFXImage(bi, null);
		//ImageView imageView = new ImageView(image);
		//ImageView iv3 = new ImageView();
        //iv3.setImage(image);
		
        //GraphicsContext gc=gameCanvas.getGraphicsContext2D();
        GraphicsContext gc=cardTable.gameCanvas.getGraphicsContext2D();
		// Image img, double x, double y, double w, double h)

        Rectangle2D viewportRect = new Rectangle2D(1., 1., 20, 20);

        // 
        // put at the lower 6 oclock position
        double height = cardTable.gameCanvas.getHeight();
        double width = cardTable.gameCanvas.getWidth();
        double x = height / 2.;
        double w = width / 2.;
        //gc.rect(10, 10, 20, 20);
        gc.drawImage(image, x, w, x/2., w/2.);
		}
	
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

	void displayTrickUpdate(Trick t) {
		Canvas canvas=cardTable.gameCanvas;
		GraphicsContext gc = canvas.getGraphicsContext2D();
		String s1="Trick " + t.trickid
				+ " L:" + t.leader
				+ " W:" + t.winner ;
		if (t.bHeartsBroken)
			s1 = s1 +  "HBroken";
		String s2= "[subdeck...]";
				
		gc.setStroke(Paint.valueOf("red"));
		gc.strokeText(s1, 0, 15);	// upper left for now
		gc.strokeText(s2,  0, 30);
		}
	
	/*
	 * display a (scaled) card at (x,y) with rotation
	 */
	void displayCard(Card c, double x, double y, double rotation) {
        GraphicsContext gc=cardTable.gameCanvas.getGraphicsContext2D();
		BufferedImage bi=CardFace.getCardFace(c.cardindex);
		Image image = SwingFXUtils.toFXImage(bi, null);		
		double w=22.5;
		double h=35.;
        drawRotatedImage(gc, image, rotation, x, y, 5.*w, 5.*h);		
		}
	
	void clearTrick(int nhands) {
		Subdeck sd = new Subdeck();
		for (int i=0; i<nhands; i++) {
			Card c=Card.cardBack();
			sd.add(c);			
			}
		displayTrick(0, nhands, sd);			
		}
	
	void displayTrick(int startOclock, int n, Subdeck sd) {
		//
		// current scenario: generate n random cards to display
        GraphicsContext gc=cardTable.gameCanvas.getGraphicsContext2D();
		int i;
		if (sd == null) {
			sd = new Subdeck();
			for (i=0; i<n; i++)
				sd.add(Subdeck.randomCard());
			}
		double angleOfRotation = 0.;
        double height = cardTable.gameCanvas.getHeight();
        double width = cardTable.gameCanvas.getWidth();
        double h = height / 2.;
        double w = width / 2.;
		// TODO: start OClock is ignored right now...
		int rotation;
		if (n == 4) {
			//Rectangle rect=new Rectangle((double) 22.5, (double) 35.);
			double[] vertices4 =  
				{	w, 0.0,
					w + 10, h/2,
					w-100, h-20,
					0, h/8
				};
			rotation = 90;
			for (i=0; i<n*2; i+=2, angleOfRotation += rotation) {
				Card c=sd.pop();
				BufferedImage bi=CardFace.getCardFace(c.cardindex);
				Image image = SwingFXUtils.toFXImage(bi, null);				
		        //drawRotatedImage(gc, image, angleOfRotation, vertices4[i]/*+w/3.*/, vertices4[i+1]/*+h/3*/, w/2, h/2 );
				displayCard(c, vertices4[i], vertices4[i+1], angleOfRotation);
				}
			}
		if (n == 6) {
			Hexagon hex = new Hexagon((double) 22.5, (double) 35.);	// ratio of a bridge card
			hex.computePoints(w, h);
			double[] vertices = hex.getPoints(w, h);

			rotation = 60;
			for (i=0; i<n*2; i+=2, angleOfRotation += 60.) {
				Card c=sd.pop();
				BufferedImage bi=CardFace.getCardFace(c.cardindex);
				Image image = SwingFXUtils.toFXImage(bi, null);
				
		        /*ImageView iv3 = new ImageView();
		         iv3.setImage(image);
		         Rectangle2D viewportRect = new Rectangle2D(40, 35, 110, 110);
		         iv3.setViewport(viewportRect);
		         iv3.setRotate(60);*/

		         // Works, but does not rotate:
		        //gc.drawImage(image, vertices[i]+w/3., vertices[i+1]+h/3, w/2., h/2.);
		         /*
		          * private void drawRotatedImage(GraphicsContext gc, Image image, double angle, double tlpx, double tlpy) {
        gc.save(); // saves the current state on stack, including the current transform
        rotate(gc, angle, tlpx + image.getWidth() / 2, tlpy + image.getHeight() / 2);
        gc.drawImage(image, tlpx, tlpy);
        gc.restore(); // back to original state (before rotation)
    }
		          */
		        //drawRotatedImage(gc, image, angleOfRotation, vertices[i]/*+w/3.*/, vertices[i+1]/*+h/3*/, w/2, h/2 );
				displayCard(c, vertices[i], vertices[i+1], angleOfRotation);
		        //angleOfRotation += 60.;
		        /*BufferedImage sourceImage;
		        BufferedImage outputImage;
		        Scalr.rotate(sourceImage, Scalr.Rotation.CW_90, outputImage); */

				}
			}
		/*
		BufferedImage bi=CardFace.getCardFace(c.cardindex);
		Image image = SwingFXUtils.toFXImage(bi, null);
		//ImageView imageView = new ImageView(image);
		//ImageView iv3 = new ImageView();
        //iv3.setImage(image);
		
        GraphicsContext gc=gameCanvas.getGraphicsContext2D();
		// Image img, double x, double y, double w, double h)

        Rectangle2D viewportRect = new Rectangle2D(1., 1., 20, 20);

        // 
        // put at the lower 6 oclock position
        //gc.rect(10, 10, 20, 20);
        gc.drawImage(image, x, w, x/2., w/2.);
		 */
	}
	
	/*
	 * Write splash screen on Game Canvas cn
	 * Note (as written) this is called in the GUI thread
	 * (was mkSplashScreen)
	 * TODO: distinguish between board display and BoardManager game operations
	 */
	static void displayGameCanvas(Canvas cn, int initialHeight, int  initialWidth) {
		
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
		}
	
	void mkSplashScreen2(Canvas cn, int initialWidth, int initialHeight) {
	     cn.setStyle("-fx-background-color: POWDERBLUE;");
			cn.setWidth(initialWidth);
			cn.setHeight(initialHeight);
			GraphicsContext gc = cn.getGraphicsContext2D();
			 
			gc.setFill(Color.POWDERBLUE);
			gc.fillRect(0, 0,initialWidth,initialHeight);

		}
	
	private double minRowHeight=20.0;
	private double minCardtableHeight=320.0;
	private static final double defaultSceneHeight=450;	// cannot call in constructor unless static...
	private static final double defaultSceneWidth=650;
	
	// Constructor
	public ClientScene() {
		super(new Group(), defaultSceneWidth, defaultSceneHeight);
		contentPane = new BorderPane(); // GP
		
		/*
		 * Keystroke events for entire pane
		 */

        this.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ENTER: doSendMethod(); break;
                    case F2:
                    	/*
                    	 * Only consume the alt-f2 or meta-f2 or ctrl-f2
                    	 */
                    	if (event.isMetaDown() || event.isAltDown() || event.isControlDown())
                    		doF2Method(); 
                    	else
                    		;
                    	break;
                    default:
                    	break;    
                	}
            	}  });
            
		//
		// Buttons
		btnEnterButton = new Button(ENTERBUTTON_LABEL);
		btnEnterButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	doSendMethod();
		        //label.setText("Accepted");
		    }
		});
		
		btnConnectButton = new Button(CONNECTBUTTON_LABEL);
		btnConnectButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	doConnectMethod();
		        //label.setText("Accepted");
		    }
		});

		/*
		 * the bottompane of the borderpane layout is a grid;
		 */
		GridPane bottom = new GridPane();
		
		// Row0: <cardsLabel>
		lblCardsLabel = new Label("Display Cards in Hand Here");
		lblCardsLabel.setMinHeight(minRowHeight);
		bottom.addRow(0, lblCardsLabel);

		//
		// row1: > editline [b1][b2]
		promptLabel=new Label(">");		
		textField = new TextField();
		textField.setMinHeight(minRowHeight);
		lblStatusLine = new Label("New label"); // GB Row 1 (i.e. second)
		lblStatusLine.setMinHeight(minRowHeight);
		HBox hrow=new HBox();
		hrow.getChildren().addAll(promptLabel, textField, btnEnterButton, btnConnectButton);
		bottom.addRow(1, hrow);
		//
		// row2: status line
		bottom.addRow(2,  lblStatusLine);
		contentPane.setBottom(bottom);

		/*
		 * Create Canvases to be the game and hand panels
		 * set as center and right
		 */
		BoardManager.init();
		//nTableSize=4;
		cardTable=new CardTable(nTableSize, 350, 350);
		//gameCanvas = new Canvas();

		//displayGameCanvas(gameCanvas, 350, 350);
		contentPane.setCenter(cardTable.gameCanvas);
		contentPane.setCenterShape(true);
		
		handCanvas = new Canvas();
		mkSplashScreen2(handCanvas, 250, 350);
		contentPane.setRight(handCanvas);
		
		flowPane = new FlowPane();
		makeHandPanel(flowPane);
		contentPane.setRight(flowPane);
		
		Group root = (Group) this.getRoot();
	    root.getChildren().add(contentPane);
	        
		}	// ClientScene()

	/*
	 * Lookup card in button list and delete first instance of it
	 */
	private void deleteCard(Card c) {
		int i;
		boolean bFound = false;
		System.out.println("*** Deleting: " + c.encode() + " ***");
		for (i=0; i<maxCardsInHand; i++) {
			Card c2=cardArray[i];
			if (c2 == null)
				continue;
			System.out.println("Comparing to:" + c2.encode());
			if (c.equals(c2)) {
				// zero out arrays and delete button from flow
				Button b = buttonArray[i];
				System.out.println("Deleting button for:" + c.encode());
				/*
				 * remove the button on the gui-thread
				 */
				Platform.runLater(new Runnable() {
					  @Override public void run() {
							flowPane.getChildren().remove(b);
					  }
					});				
				flowPane.getChildren().remove(b);

				buttonArray[i] = null;
				cardArray[i] = null;
				bFound = true;
				break;
				}
			
			}
		System.out.println("Found " + c.encode() + "? " + bFound);
		}
	
	/*
	 * Is this routine dead? replaced by other (javafx) mechanism???
	 */
	private void doCardButtonPressed(ActionEvent ae) {
		// Figure out which button I am, and then try to play that card...
		System.out.println("Whoa Baby! Button Pressed:" + ae);
		Button b=(Button) ae.getSource();
		int index = (int) b.getUserData();
		String s="";
		if (buttonArray[index] == b) {
			s = " Checks out!";
			// splat the card down whether it gets rejected by the server or not...
			if (cardArray[index] != null)
				sendPlayCard(cardArray[index]);
			}
		else
			s = "But Doesn't check out...";
		//end by printing status msg
		System.out.println("Found the button: " + index + s);
		// experiment...
		//flowPane.getChildren().remove(b);
		// This is what is screwed up...
		// I can remove here, but not from the protocol message:
		// my thread management is broken...
		// Time to read...
		// xxx
		}

	final int maxCardsInHand=20;
	Button[] buttonArray = new Button[maxCardsInHand];
	Card[] cardArray = new Card[maxCardsInHand];
	FlowPane flowPane=null;
	
	void clearFlowPane() {
		int i;
		for (i=0; i<maxCardsInHand; i++)
			if (buttonArray[i] != null) {
				Button b=buttonArray[i];
				flowPane.getChildren().remove(b);
				buttonArray[i] = null;
				cardArray[i] = null;
				}
			
		}
	
	void addCardstoHandPanel(Subdeck sd) {
		//FlowPane fp=new FlowPane();

		int i=0;
		
		final double cardRatioHeight = 3.5;
		final double cardRatioWidth = 2.25;
		for (Card c : sd.subdeck) {
			BufferedImage bi=CardFace.getCardFace(c.cardindex);
			Image image = SwingFXUtils.toFXImage(bi, null);
			ImageView imageView = new ImageView(image);
			Button b=new Button();
			b.setUserData((Object) i);
			buttonArray[i] = b;
			cardArray[i] = c;
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
			i++;
			}
	     flowPane.setPrefWrapLength(200); // preferred width = 300

	     
		/*contentPane.setRight(fp);
		fp.applyCss();
		flowPane = fp;*/
		}
	
	void makeHandPanel(FlowPane flowpane) {
		// Stage Right	
		// Stage Right as a FlowPane
		// Standard Bridge Card is 2.25 x 3.5
		int i;
		final double cardRatioHeight = 3.5;
		final double cardRatioWidth = 2.25;
		for (i=0; i<17; i++) {
			BufferedImage bi=CardFace.getCardFace(i);
			Image image = SwingFXUtils.toFXImage(bi, null);
			ImageView imageView = new ImageView(image);
			Button b=new Button();
			b.setUserData((Object) i);
			buttonArray[i] = b;
			cardArray[i] = null;
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
		
		}
	
	// Resizable Canvas...
	public class ResizableCanvas extends Canvas {

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

		@Override
		public double prefHeight(double width)
		{
		    return minHeight(width);
		}

		@Override
		public double minWidth(double height)
		{
		    return height;
		}

		@Override
		public double maxWidth(double height)
		{
		    return 10000;
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
		    ClientScene.displayGameCanvas(this, (int)width, (int)height);
		    
		    //
		    // Paint? Can't resolve paint...
		    //paint();
		}
		//Note that the resize method cannot simply call Node.resize(width,height), because the standard implementation is effectivele empty.
		// from stackoverflow: https://stackoverflow.com/questions/23449932/javafx-resize-canvas-when-screen-is-resized
	}
	// - Resizable Canvas

}



