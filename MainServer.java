package cardGame;

//import java.io.BufferedReader;
//import java.io.Console;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.SwingUtilities;

import javafx.application.Platform;

//import cardClient.ClientFrame;

/*
 * MainServer has the main read-eval-print loop for developing and testing the game implementation
 * Currently run from ttyHandler. 
 * Will eventually run after being spun off and using sockets
 */
public class MainServer {

	static boolean bForceRead=false;
	static CardGameKernel cgk = null;
	static private boolean ttyMode = true; // Debugmode console input...
	// CardGame g=new CardGame();
	/*
	 * This is a stub! Not implemented! TODO: implement spawnServer
	 * 
	 * SwingUtilities.invokeLater(new Runnable() { public void run() {
	 * createAndShowGUI(); } });
	 */

	public static void spawnServer() {
		/*
		 * Create and launch the clientframe
		 */
		/*
		 * Create and display the form was java.awt.EventQueue
		 */
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// new HelloWorldJFrame().setVisible(true);
				// new PlayFelt().setVisible(true);
				// GameBoard gb=new GameBoard(); // Game creates a gameboard...
				// ClientFrame cf = new ClientFrame();
				// cf.setVisible(true);
				// cf.play();

				/*
				 * Decommission CardGameKernel.listener(); in favor of MainNIOServer.listener();
				 */
				// CardGameKernel.listener();

				try {
					MainNIOServer.listener();
				} catch (Throwable th) {
					// Something bad happened
					th.printStackTrace();
				}
			}
		});

	}

	/*
	 * SwingUtilities.invokeLater(new Runnable() { public void run() {
	 * createAndShowGUI(); } });
	 */
	public static void spawnClient() {
		/*
		 * Create and launch the clientframe
		 */
		/* Create and display the form */
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String[] args = { "a", "b" };
				//JCFrameClient.main(args);
			}
		});

	}

	public static void spawnTTYHandler() {
		/*
		 * Create and launch the clientframe
		 */
		/* Create and display the form */
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					// JCFrameClient.XXXmain();
					ttyhandler();
					// ttyhandler();
					System.exit(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	public static void main(String[] args) {

		// spawnServer();
		// spawnClient();
		// spawnTTYHandler();

		if (ttyMode) {
			try {
				// JCFrameClient.XXXmain();
				// spawnTTYHandler();
				ttyhandler();
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} // TTYmode

	} // static void main(...)

	static public void echo(String s) {
		System.out.printf("echo> %s\n", s);
	}

	static public void ttyLogString(String s) {
		System.out.printf("gameserver> %s\n", s);
	}

	static public void ttyClientLogString(String s) {
		System.out.printf("localplayer> %s\n", s);
		}
	
	static Future runStepAndReturn(int iArg) {
		ExecutorService exec=Executors.newCachedThreadPool();
		Future f=exec.submit( 
		  new Runnable() {
			@Override
			public void run() {
				cgk.step(iArg);
			}
			
		});
		return f;
		
		/*Platform.runLater(new Runnable() {
			@Override
			public void run() {
				cgk.step(iArg);
				}
			});
			*/
	}


	static void ttyhandler() throws IOException {

		/// instance of the game being played from the console. Be careful about
		/// reentrancy when you expand this to be a real server
		CardGame g = new CardGame();
		/*
		 * TODO: Temporary global for the game
		 */
		CardGame.theGame = g;
		// And give this to the message processor
		cgk = new CardGameKernel();
		cgk.setCardGame(g);
		// Note: in the ttyHandler there are two ways to get things to g; build a
		// message and send or enqueue a message and resume 'r'

		/*
		 * Protocol Message to build and send
		 */
		ProtocolMessage m = null; // Keep this at the level of main loop so it can be sent to players and game...
		String sMsgEncode = "";
		// input variables
		String input = "";
		Boolean bShuffle = false;
		int i = 0;
		// BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.printf("CardGame test console v1.0 quit to exit\n");
		System.out.flush();
		Scanner scanner = new Scanner(System.in);
		// Subdeck deck=new Subdeck();
		while (input != null) {
			// Card c;
			System.out.printf(". ", i);

			input = scanner.nextLine();
			// System.out.flush();
			if (input == null) {
				echo("null. quitting...");
				break;
			} else if (input.startsWith("j")) { // join the game
				String player = input.substring(1);
				g.join(player);
			} else if (input.equals("a")) {
				echo("Adding message to queue");
				if (m != null) {
					cgk.enqueue(m);
					m = null;
				} else
					echo("Must build a message with M command before enqueing it");
				// c = new Card(i++);
				// deck.add(c);
				// System.out.printf("added %d", c.cardindex);
				// System.out.printf("echo> %s", input);
			} else if (input.startsWith("reset")) {
				g.reset();
				echo("game reset.");
			} else if (input.startsWith("kill")) {
				MainNIOServer.bKillIO = true;
				System.out.println("IO Kill sent. Ok to restart when iterations stop");				
			} 	else if (input.startsWith("restart")) {
				System.out.println("Emergency restart of listener");
				MainNIOServer.restart();
				echo("listener restart.");
			}else if (input.startsWith("force")) {
				MainServer.bForceRead = true;
			} 
			else if (input.startsWith("step")) {
			
				// Step and go for stepping through kernel queue messages
				int j; 
				for (j=0; (j < input.length()) && (input.charAt(j) < '0' || input.charAt(j) > '9'); j++)
					;
				String sParam="";
				for (; (j < input.length()) && (input.charAt(j) >= '0' && input.charAt(j) <= '9'); j++)
					sParam = sParam + input.charAt(j);
				int iArg=0;
				if (sParam.equals(""))
					iArg = 1;
				else
					iArg = Integer.parseInt(sParam);
				runStepAndReturn(iArg);
				echo("back.");
			} else if (input.startsWith("go")) {
				runStepAndReturn(-1);
				echo("Back...");
			} else if (input.startsWith("res")) {
				runStepAndReturn(-1);
				// cgk.resume(); // resume is depracated and probably doesn't work anymore
				echo("back...");
			} else if (input.startsWith("ui")) {
				System.out.printf("starting UI is currently disabled...");
				//spawnClient();
			} else if (input.startsWith("sleep")) {
				try {
					System.out.printf("Sleeping...");
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					System.out.printf("Exception caught");
				}
			} else if (input.startsWith("listen")) {
				System.out.printf("starting listener");
				spawnServer();
			} else if (input.equals("p")) { // print players
				int nturn = g.getTurn();
				for (i = 0; i < 4; i++) {
					Player p = g.getPlayer(i);
					String sTurn = "";
					if (p != null) {
						if (i == nturn)
							sTurn = "*";
						else
							sTurn = " ";
						if (p.isRobot()) {
							sTurn = sTurn + "R-" + p.getName();
						} else {
							sTurn = sTurn + "H-" + p.getName();
						}
						String asynch="";
						if (p.getAsynch())
							asynch = "-Async";
						else
							asynch = "";
						System.out.printf("player %s%d. %s%s", sTurn, i, p.getName(), asynch);
						Subdeck hand = g.getCards(i); // make sure you get the game's hand, not the players...
						/*
						 * TODO: use iterator for subdeck! Yes this really should use an iterator, but
						 * not now...
						 */
						String sServerHand = hand.sdump();
						echo(sServerHand);
						if (p.isRobot()) {
							Subdeck remoteHand = p.getRemoteHand(); // This probably only works for robots
							String sRemoteHand = remoteHand.sdump();
							echo(sRemoteHand);
							}
						// System.out.printf("Serv:%s", sServerHand);

					}
				}
				// show the current Trick also
				Trick t=g.getCurrentTrick();
				String s=null;
				if (t == null)
					s = "No current trick";
				else
					s=t.encode();
				echo("Current Trick:" + s);
				/*
				 * // System.out.printf("printing: (%d)", deck.size()); int decksize = 0; // for
				 * (int j=0; j<deck.size(); j++) { for (int j = 0; j < decksize; j++) { if (j >
				 * 0) System.out.printf(", "); // c = deck.get(j); // System.out.printf("%d",
				 * c.cardindex); echo(input); // System.out.printf("echo> %s\n", input);
				 * 
				 * 
				 * }
				 */
			} else if (input.equals("s")) {
				// int n;
				// n = deck.size();
				// System.out.printf("there are %d elements.\n", n);
				bShuffle = !bShuffle;
				if (bShuffle)
					echo("Shuffle is" + "true");
				else
					echo("Shuffle is" + "false");
				// System.out.printf("echo> %s", input);

			} else if (input.startsWith("d")) { // deal and restart the game
				g.reset(bShuffle); // this isn't reset game. It's deal a new hand.
				// c = deck.pullTopCard();
				// System.out.printf("deleted %d", c.cardindex);
				// System.out.printf("echo> %s", input);
				echo(input);
			} else if (input.startsWith("quit")) {
				// c = deck.pullTopCard();
				// System.out.printf("deleted %d", c.cardindex);
				echo("quitting...");
				break;
			} else if (input.startsWith("q")) {
				cgk.qDump();
				echo("Q dumped.");
				break;
			} else if (input.startsWith("c")) {
				int iIndex = 1; // cAS cDC for example
				if (input.length() < 3) {
					echo("Please input a valid card with a Rank and Suit");
					continue;
				}
				echo("round-trip testing..");
				String sTemp = input.substring(iIndex, iIndex + 2);
				echo(sTemp);
				Card c = new Card(input.substring(iIndex, iIndex + 2));
				String sTemp2 = c.encode();
				echo(sTemp2);
				if (sTemp2.equals(sTemp) || sTemp2.equals(sTemp.toUpperCase()))
					echo("OK");
				else
					echo("error formulating:" + sTemp2);
			} else if (input.startsWith("h")) {
				echo("help for protocol messages sent to game from player; format: M[=~][0-n]Cards*");
			} else if (input.startsWith("M")) {
				// create a protocol message with the substring...
				// g.exec(input.substring(2));
				echo("Before: " + input.substring(1));
				m = new ProtocolMessage(input.substring(1));
				sMsgEncode = m.encode();
				echo("After.: " + sMsgEncode);
			} else if (input.startsWith("S")) {
				// create a protocol message with the substring...
				// g.exec(input.substring(2));
				echo("SU: " + input.substring(1));
				echo("TBD...");
			} else if (input.startsWith("Q") || input.startsWith("q")) {
				String squery = g.getGameStatus();
				echo(squery);
			} else if (input.startsWith("n")) { // next move
				// String player = input.substring(1);
				echo("Next...");
				g.sendNextMove();
			} else if (input.startsWith("->")) {
				// create a protocol message with the substring...
				// g.exec(input.substring(2));
				if (m != null && m.sender != -1) { // i.e. the sender is not the game server...
					echo("Sending M to gameserver: " + input.substring(0));
					g.process(m);
					echo("Server Ok.");
				} else if (m != null) {
					String sClientId = input.substring(2);
					int iRecipientId = Integer.parseInt(sClientId);
					echo("Sending M to player: " + input.substring(0));
					Player p = g.getPlayer(iRecipientId);
					if (p != null) {
						p.process(m);
					} else
						echo("Send to Player can't be executed.");
				} else
					echo("No msg to send.");

			} else {
				echo("commands: h c[RankSuit] ->[SP] j[oin]playername p[print players] gtrace ktrace? d[eal] s[size] q[uery] a[message to queue] quit");
				runStepAndReturn(1);
				echo("_Step>");
				if (m != null) {
					echo("M:<" + sMsgEncode + ">");
				}
			}

			i++;
		}
		scanner.close();
	} // ttyHandler

}
