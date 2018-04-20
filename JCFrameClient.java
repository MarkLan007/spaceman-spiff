package cardGame;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.sun.glass.events.KeyEvent;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.awt.GridBagLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;

import java.awt.Canvas;
import java.awt.Desktop.Action;

import javafx.application.Application;
//
// Java FX adds here...
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class JCFrameClient extends Application implements ActionListener {
	// clip
	/**
	 * Launch the application.
	 */
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("JCFrameClient...");
		//JCFrameClient frame = new JCFrameClient();
		//Scene scene = JCFrameClient.makeClientFrameAsScene();
		ClientScene clientScene= new ClientScene();
		primaryStage.setScene(clientScene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	/*
	 * @Override public void start(Stage stage) { stage.setTitle("ComboBoxSample");
	 * }
	 */
	// public class Main extends Application {.JCFrameClient...}

	public static void mainXyzzy(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// initAndShowGUI();

					//
					// JCFrameClient frame = new JCFrameClient();

					// frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// clip
	GridPane contentPane;
	TextField textField;

	Canvas canvasTop = null;
	Canvas canvasBottom = null;
	final Pane gamePanel;
	final Pane handPanel;
	Button btnEnterButton = null;
	Button btnConnectButton = null;
	Label lblNewLabel = null;
	Label lblCardsLabel = null; // added to display cards right above the input text box; TODO: may conflict
								// with canvasBottom placement

	/*
	 * the input/output streams created with the server when user connects So the
	 * client will be doing its IO asynchrously also, through: abstract class
	 * AsynchronousSocketChannel that implements AsynchronousByteChannel
	 */

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

	void updateStatusString(String st) {
		// This should be on the UI thread... so
		// lblNewLabel.setText(st);
		Platform.runLater(new Runnable() {
			  @Override public void run() {
				  lblNewLabel.setText(st);
			  }
			});
	}

	/*
	 * The action taken on an enterButton press or the action pushbutton
	 */
	private void doSendMethod() {
		String st = textField.getText();
		if (isConnected) {
			// outputINetStream.println(st);
			/*
			 * For now just send whatever is typed to the server... Obviously temporary
			 */
			updateStatusString(st);
			// lblNewLabel.setText(st);
			int i;
			outbuff.clear();
			for (i = 0; i < st.length(); i++)
				outbuff.putChar(st.charAt(i));
			outbuff.putChar('\n');
			outbuff.putChar('\n');
			outbuff.putChar('\n');
			System.out.println("Trying to write:" + st);
			outbuff.flip();
			sockINetConnection.write(outbuff);
		}
		textField.setText("");
		if (!isConnected)
			lblNewLabel.setText("Not Connected>" + st);
	}

	AbstractAction doSend = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Yay! intercepted a SEND keystroke!");
			lblNewLabel.setText("Yay! intercepted a <SEND> keystroke!");
			// Actual send to server
			doSendMethod();
		}
	};

	AbstractAction doF2 = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Yay! Got a super-double-secret alt-F2!");
			lblNewLabel.setText("Yay! Got a super-double-secret alt-F2!");
			// do nothing
		}
	};

	void serverCrashed() {
		btnConnectButton.setDisable(true);

		System.err.println("Server crashed. Reconnect.");
		lblNewLabel.setText("Server crashed. Try again.");
		System.exit(0);	// TODO: reset isConnected, ungray the button, and reset the connect
	}

	void processServerString(String s) {
		// TODO: parse message; update board; update hand or enable user to play
		/*
		 * So if the string has an & in it, it's a trick update... do something in the
		 * top canvas...
		 */
		if (s.contains("&")) {
			fxText.setText(s);
		} else if (s.startsWith("%")) {
			// It's an informative status message for the user. Put on the status line
			updateStatusString(s);
		} else if (s.startsWith("+")) {
			// Process the add cards message
			}
		else {

			// for now just splat the command string...
			showCards(s);
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

		if (!readInProgress) {
			readInProgress = true;
			initiateRead();
			// return;
		}

		if (futurei.isDone())
			System.out.println("Read Done -> won't block!");
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
		for (int j = 0; j < lim; j++) {
			char c = inbuff.getChar();
			if (c == '\n' || c == '\r' || c == '\0') {
				// if I'm at a break char, and not at the end of the string, there are multiple
				// commands in the read!
				// Gack!
				// process each in order before initiating another read!
				// TODO: suppose there are multiple lines and it ends with a partial? Fuck me.
				if (str.length() > 0) {
					System.out.println("read>>>" + str);
					processServerString(str);
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

		/*
		 * There are no reads going on because we just cleared the buffer... initiate
		 * one.
		 */
		// initiateRead();
		/*
		 * no, just wait to get polled again
		 */
		readInProgress = false;
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		/*
		 * Which button was pressed
		 */
		String command = ev.getActionCommand();
		if (command.equals(CONNECTBUTTON_LABEL)) {
			// Try to connect to server...
			lblNewLabel.setText(command + "ing...");
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
				try {
					sockINetConnection = AsynchronousSocketChannel.open();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				InetSocketAddress hostAddress = new InetSocketAddress(dialog.serverName, portId);
				future = sockINetConnection.connect(hostAddress);

				int i = 0;
				while (!future.isDone()) {
					if (i > 3) {
						// admit defeat: no server
						System.out.println("Cannot connect. (try: " + i + ") ");
						lblNewLabel.setText("Cannot connect to Cardserver after multiple retries.");
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
				// lblNewLabel.setText("Connected to Cardserver.");
				// Make the connect button inactive xxx yyy

				btnConnectButton.setDisable(true);
				/*
				 * spawn port reader in its own thread to call backgroundReader
				 */
				spawnBackgroundReader();
			}
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
	 * Background read routines -- these have been superceded...
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
		lblCardsLabel.setText(st);
	}


	/*
	 * This will probably get junked.
	 * Code hoisted into makeClientFrameAsScene
	 */
	public JCFrameClient() {
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// setBounds(100, 100, 462, 355);
		// JWindow settings...

		// contentPane = new JPanel();
		contentPane = new GridPane(); // GP
		// contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		// setContentPane(contentPane);
		//
		// Hacking here...

		// contentPane.setLayout(null);
		// contentPane.setLayout(new GridBagLayout()); // GB

		//
		lblNewLabel = new Label("New label"); // GB Row 1 (i.e. second)
		/*
		 * //lblNewLabel.setBounds(10, 299, 365, 14); GridBagConstraints r3 = new
		 * GridBagConstraints(); // GB r3.gridheight = 4; // 4 rows... r3.fill =
		 * GridBagConstraints.HORIZONTAL; r3.weightx = 0.5; r3.ipadx = 10; // Pad
		 * heavily -- this is the status line r3.gridx = 0; r3.gridy = 3; // GB Row 1
		 * (i.e. second row below fx panels)
		 */
		// contentPane.addRow(arg0, arg1);
		contentPane.addRow(3, lblNewLabel); // GP

		textField = new TextField();
		int textY = 268;
		int textHeight = 20;
		// textField.setBounds(10, textY, 249, textHeight);
		// Disabled events and action listeners for now...
		// textField.addActionListener(this); // capture the vk_enter key

		//
		// put in cardsLabel just above textField
		lblCardsLabel = new Label("Display Cards in Hand Here");
		// lblCardsLabel.setBounds(10, textY - textHeight, 249, textHeight);
		// GridBagConstraints r1 = new GridBagConstraints(); // GB
		/*
		 * r1.gridheight = 4; // 4 rows... r1.fill = GridBagConstraints.HORIZONTAL;
		 * r1.weightx = 0.5; r1.ipadx = 20; // This can display the whole hand r1.gridx
		 * = 0; r1.gridy = 1; // GB Row 1 (i.e. second row below fx panels)
		 */
		contentPane.addRow(1, lblCardsLabel);

		/*
		 * Keystroke event for entire pane
		 */
		/*
		 * No keystrokes for now...
		 */
		/*
		 * textField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "doSend"); //
		 * See https://docs.oracle.com/javase/tutorial/uiswing/misc/keybinding.html
		 * textField.getInputMap().put(KeyStroke.getKeyStroke("alt F2"), "doF2");
		 * textField.getActionMap().put("doSend", doSend);
		 * textField.getActionMap().put("doF2", doF2); textField.setColumns(10);
		 * GridBagConstraints r2a = new GridBagConstraints(); // GB r2a.fill =
		 * GridBagConstraints.HORIZONTAL; r2a.fill = GridBagConstraints.BOTH;
		 * 
		 * //r2a.gridwidth = GridBagConstraints.RELATIVE; r2a.gridwidth = 3; r2a.weightx
		 * = 0.5; r2a.ipadx = 1; // Pad heavily -- this is the edit box r2a.ipady = 0;
		 * r2a.gridx = 0; r2a.gridy = 2; // GB Row 1 (i.e. second row below fx panels)
		 */
		contentPane.addRow(2, textField);

		//
		// Buttons
		Button btnEnterButton = new Button(ENTERBUTTON_LABEL);
		/*
		 * btnEnterButton.setBounds(258, 265, 89, 23);
		 *
		 * btnEnterButton.addActionListener(this); GridBagConstraints r2b = new
		 * GridBagConstraints(); // GB //r2b.fill = GridBagConstraints.HORIZONTAL;
		 * r2b.fill = GridBagConstraints.BOTH; r2b.gridwidth = 3; r2b.weightx = 0.2;
		 * r2b.ipadx = 1; r2b.gridx = 1; r2b.gridy = 2; // GB Row 2 (i.e. third row)
		 */
		contentPane.addRow(2, btnEnterButton);
		//

		btnConnectButton = new Button(CONNECTBUTTON_LABEL);
		// btnConnectButton.setBounds(350, 265, 89, 23); // 351, 267, 84, 23
		// btnConnectButton.addActionListener(this);
		contentPane.addRow(2, btnConnectButton);
		/*
		 * GridBagConstraints r2c = new GridBagConstraints(); // GB r2c.fill =
		 * GridBagConstraints.HORIZONTAL; r2c.fill = GridBagConstraints.BOTH;
		 * r2c.gridwidth = 3; // GridBagConstraints.RELATIVE; r2c.weightx = 0.2;
		 * r2c.ipadx = 1; r2c.gridx = 2; r2c.gridy = 2; // GB Row 1 (i.e. second row
		 * below fx panels) contentPane.add(btnConnectButton, r2c);
		 */

		/*
		 * Canvas canvasTop = new Canvas(); canvasTop.setBounds(0, 10, 424, 100);
		 * contentPane.add(canvasTop);
		 */
		// Add jvx panel where I had canvastop...
		gamePanel = new Pane();
		// fxPanel.setSize(300, 200);
		gamePanel.setPrefSize(canvasWidth, canvasHeight);
		// gamePanel.setBounds(10, 10, canvasWidth, canvasHeight); // same as
		// canvastop...
		gamePanel.setVisible(true);
		// contentPane.add(gamePanel);

		/*
		 * This is the first row...
		 */

		// GridBagConstraints r0a = new GridBagConstraints();
		// natural height, maximum width
		/*
		 * r0a.fill = GridBagConstraints.HORIZONTAL;
		 * 
		 * r0a.ipady = 300; //make this component tall r0a.ipadx = 300; r0a.weightx =
		 * 0.0; r0a.gridwidth = 3; r0a.gridx = 0; r0a.gridy = 0;
		 */
		contentPane.addRow(0, gamePanel);

		handPanel = new Pane();
		handPanel.setPrefSize(canvasWidth, canvasHeight); // (10, canvasHeight, 425, textY);
		handPanel.setVisible(true);
		/*
		 * GridBagConstraints r0b = new GridBagConstraints(); //natural height, maximum
		 * width r0b.fill = GridBagConstraints.HORIZONTAL;
		 * 
		 * r0b.ipady = 300; //make this component tall r0b.weightx = 0.0;
		 * //r0b.gridwidth = 2; r0b.gridx = 1; r0b.gridy = 0;
		 */
		contentPane.addRow(0, handPanel);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initHandPanel(handPanel);
				initGamePanel(gamePanel);
			}
		});

		/*
		 * canvasBottom = new Canvas(); canvasBottom.setBounds(0, 116, 425, 143);
		 * contentPane.add(canvasBottom);
		 */
	}

	// Insert private javaFX class here...
	/*
	 * private static void initAndShowGUI() { // This method is invoked on the EDT
	 * thread JFrame frame = new JFrame("Swing and JavaFX"); final JFXPanel fxPanel
	 * = new JFXPanel(); frame.add(fxPanel); frame.setSize(300, 200);
	 * frame.setVisible(true); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 * 
	 * Platform.runLater(new Runnable() {
	 * 
	 * @Override public void run() { initFX(fxPanel); } }); }
	 */

	private /* static */ void initGamePanel(Pane gp) {
		// This method is invoked on the JavaFX thread
		Scene scene = createGameScene();
		// gp.setScene(scene);
	}

	private void initHandPanel(Pane hp) {
		// This method is invoked on the JavaFX thread
		Scene scene = createHandScene();
		// hp.setSc
		// hp.setScene(scene);
	}

	/*
	 * sceneUpdate -- rewrite the text in the screen
	 */
	Text fxText = new Text();

	private void sceneUpdate(String s) {
		fxText.setText(s);
	}

	final int canvasWidth = 600;
	final int canvasHeight = 200;

	private Scene createHandScene() {
		Group root = new Group();
		Scene scene = new Scene(root, Color.POWDERBLUE);
		return scene;
	}

	private Scene createGameScene() {
		Group root = new Group();
		Scene scene = new Scene(root, Color.ALICEBLUE);

		// initial setbounds is: 10, 10, 424, 100
		String welcomeMsg = "Welcome to card game client!";
		final int initialX = 10;
		final int initialY = 10;
		final int initialWidth = canvasWidth /*- initialX */;
		final int initialHeight = canvasHeight /*- initialY */;
		fxText.setX(initialWidth);
		fxText.setY(initialHeight);
		Font font = new Font(25);
		fxText.setFont(font);
		final FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(font);

		// final Canvas canvas = new Canvas(fm.computeStringWidth(s), fm.getAscent() +
		// fm.getDescent());

		final Text text = new Text(welcomeMsg);
		//
		// Create a new scene so I can figure out how big the text will be
		new Scene(new Group(text));
		text.setFont(font);
		text.applyCss();
		final double width = text.getLayoutBounds().getWidth();
		final double height = text.getLayoutBounds().getHeight();

		// no longer available: int width = fm.computeStringWidth(welcomeMsg);
		// FontMetrics fm=fxText.getFontMetrics(); //.stringWidth(welcomeMsg);

		//
		// Position the splash screen text
		fxText.setText(welcomeMsg);
		int startx = (int) ((((double) initialWidth) - width) / 2.0);
		int starty = (int) ((((double) initialHeight) - height) / 2.0); // NO TOUCH
		fxText.setX(startx);
		fxText.setY(starty + height / 2);

		double firstSeat = startx; // this is a guess based on the splash text...
		Polygon p = new Polygon();
		/*
		 * polygon.getPoints().addAll(new Double[]{ 0.0, 0.0, 20.0, 10.0, 10.0, 20.0 });
		 */

		Hexagon h = new Hexagon((double) initialWidth, (double) initialHeight);
		Polygon hexagon = new Polygon(h.getPoints(initialWidth, initialHeight));
		root.getChildren().add(hexagon);
		root.getChildren().add(fxText);

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
			root.getChildren().add(l);
		}
		//
		// write window coordinates at the lower right of the window.
		l = new Text(); // Text?
		// l.setcolor?

		int vx = (int) initialWidth - 60; // first approx...
		int vy = (int) initialHeight - 1; // Not fsize
		l.setX(vx);
		l.setY(vy);
		l.setFont(labelFont);
		l.setFill(Color.RED);
		sLabel = "(" + vx + "," + vy + ")";
		l.setText(sLabel);
		/*
		 * recompute labelWidth down to a gnat's ass...
		 */
		labelWidth = l.getLayoutBounds().getWidth();
		vx = initialWidth - (int) labelWidth;
		l.setX(vx);
		root.getChildren().add(l);

		return (scene);
	}

	// Hexagon drawing
	class Hexagon {
		double[] points;
		double[] proportionatePoints;
		double center;
		//

		public Hexagon(double width, double height) {
			center = hypot(width, height);
			points = new double[12];
			proportionatePoints = new double[12];
			// Hexagon (unscaled) with Radius R=1; between 0 and 2 on each axis
			// X Y
			proportionatePoints[0] = 1.0;
			proportionatePoints[1] = 0.0; // 12 0'Clock
			proportionatePoints[2] = 1.833;
			proportionatePoints[3] = 0.5; // 2... x is Cos and Y is sin of 30 Degrees
			proportionatePoints[4] = 1.833;
			proportionatePoints[5] = 1.5; // 4....
			proportionatePoints[6] = 1.0;
			proportionatePoints[7] = 2.0; // 6 O'Clock
			proportionatePoints[8] = 0.17;
			proportionatePoints[9] = 1.5; // 8 O'Clock
			proportionatePoints[10] = 0.17;
			proportionatePoints[11] = .5; // 10 O'Clock

		}

		private double hypot(double width, double height) {
			return ((Math.sqrt(width * width + height * height)));
		}

		public double[] getPoints(double width, double height) {
			double h = hypot(width, height);
			double aspectRatio = width / height;
			int i, j;
			double xoffset = 0.0, yoffset = 0.0;
			double scaleFactor = h / aspectRatio;
			double scaleto = 0.0;
			double xscale = 0.0, yscale = 0.0;
			double radius;

			if (width > height) {// scaleto height; that's what the limiting dimension is
				radius = height / 2.0;
				xoffset = width / 2 - radius;
				scaleto = height;
				xscale = height;
				yscale = height;
			} else { // otherwise scale to width;
				radius = width / 2.0;
				yoffset = height / 2 - radius;
				scaleto = width;
				xscale = width;
				yscale = width;
			}
			for (j = 0; j < 6; j++) {
				int x, y;
				// position the hexagon by scaling the coordinates to the window size
				x = j * 2;
				y = x + 1;
				points[x] = xoffset + proportionatePoints[x] * radius;
				points[y] = yoffset + proportionatePoints[y] * radius;
			}
			return points;
		}
	}
	// And you can use it for create a polygon just like this:

	// Polygon hexagon = new Polygon(new Hexagon(100d).getPoints());
	// End Hexagon drawing
	class Test {

		/* public static */ void mainXXXX(String[] args) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// initAndShowGUI();
				}
			});
		}
	}

}

/*
 * Where I started... with a side parallel to the top... I want 12 to be at the
 * top of the table... public void Hexagon2(double width, double height){ center
 * = hypot(width, height); points = new double[12]; // X Y points[0] = startX;
 * points[1] = 0.0; points[2] = startX + side; points[3] = 0.0; points[4] =
 * startX + side+(side/2); points[5] = center; points[6] = startX + side;
 * points[7] = center * 2; points[8] = startX; points[9] = center * 2;
 * points[10] = startX -side/2; points[11] = center;
 * 
 * }
 */
