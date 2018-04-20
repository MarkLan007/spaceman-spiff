package cardGame;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cardGame.MiniServer.ClientSession;
import cardGame.URLConnectionReader.MReader;

public class MainNIOServer {
	public static final short PORT = 1081;
	
	public static void main(String[] args) throws Throwable {
		new MainNIOServer (new InetSocketAddress("localhost", PORT));
	}
	
	
ServerSocketChannel serverChannel;	
//ServerSocketChannel socketChannel;	// changed to serverChannel
Selector selector;
SelectionKey serverKey;
static MainNIOServer staticCopy=null;

public static void listener() throws Throwable {
	/*
	 * Experiment with this latter:
	 * Navigator.registerProtocolHandler("web+burger",
            "http://www.google.co.uk/?uri=%s",
            "Burger handler");
     */

	staticCopy = new MainNIOServer (new InetSocketAddress("localhost", PORT));
	}

static boolean bInProgress=false;
public static boolean bKillIO = false;

static void restart() {
	bInProgress = false;
	bKillIO = false;
	staticCopy.doLoop();
	}

MainNIOServer(InetSocketAddress isockListenAddress) {
	try {
		/*
		 * Open the server's port (or die.)
		 */
		serverChannel = ServerSocketChannel.open();
	} catch (IOException e) {
		System.err.println("Fatal error: failed to open server socket. Exiting.");
		e.printStackTrace();
		System.exit(0);
		}	
	try {
		serverChannel.configureBlocking(false);
	  } catch (IOException e) {
		System.err.println("Fatal error: IOException when tried to make port non-blocking. Exiting.");
		e.printStackTrace();
		System.exit(0);
		}

	try {
		serverKey = serverChannel.register(selector = Selector.open(), 
												SelectionKey.OP_ACCEPT);
	} catch (ClosedChannelException e) {
		System.err.println("Fatal error: Server channel unexpectedly closed. Exiting.");
		e.printStackTrace();
		System.exit(0);
	} catch (IOException e) {
		System.err.println("Fatal error: IOException Server channel unexpectedly failed. Exiting.");
		e.printStackTrace();
		System.exit(0);
		}
	try {
		serverChannel.bind(isockListenAddress);
	} catch (IOException e) {
		e.printStackTrace();
		System.err.println("MainNIOServer: Fatal error: ava.net.BindException: address already in use: bind failed. Exiting.");
		System.exit(0);
	}
	//
	// Fuck me. This is correct syntax...
	Executors.newSingleThreadScheduledExecutor().
		scheduleAtFixedRate(() -> {
			loop();
		}, 0, 500, TimeUnit.MILLISECONDS);				
	}

/*
 * called in debugging when threadscheduler appears to crash and don't want to completely re-init
 */
void doLoop() {
	Executors.newSingleThreadScheduledExecutor().
	scheduleAtFixedRate(() -> {
			loop();
		/*} catch (Throwable th) {
			// TODO: Client disconnected; Need to replace with robot
			System.err.println("Client disconnected. Replace with robot player and move on.");
			th.printStackTrace(); 
			} */
	}, 0, 500, TimeUnit.MILLISECONDS);				

	
}

static HashMap<SelectionKey, NIOClientSession> clientMap = new HashMap<SelectionKey, NIOClientSession>();

SelectionKey r1=null, r2=null;
boolean bForceRead=false;

class SReader implements Runnable {
		SocketChannel sChannel=null;
	   public void run() {
		     // read and service request on socket
			System.out.println("*** MReader:: background call succeeded! Yea! ***");
         String output;
         try {
         	int iteration=0;
         	int i;
         	//semaphoreP();
         	ByteBuffer inbuff=ByteBuffer.allocate(1024);
         	boolean stopReader=false;
				while (true) {
					inbuff.clear();
	         		int nBytes=sChannel.read(inbuff);
	         		System.out.print("Read=" + nBytes);
	         		inbuff.flip();
	         		String value="";
	         		for (i=0; i<nBytes; i++) {
	         			value = value + inbuff.get();
	         			}
						// (output = inputReader.readLine()) != null) {
				    //final String value = output;
	         		/*
				    if (value.indexOf('?') != -1) {
				    	stopReader = true;
				    	}
				    	*/
				    // enquee string...
	         		System.out.println("I have a string:" + value);
	         		// TODO: actual enqueue
				    iteration++;
					}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
     		}
			
	   SReader(SocketChannel readSocket) {
		   super();
		   sChannel = readSocket;   
	   		}
	 }

void spawnServerRead(SocketChannel readChannel) {
	System.out.println("(server) starting background reader...");
	Runnable r=new SReader(readChannel);
	Thread t;
	t = new Thread(r);
	t.start();

    System.out.println("Initiating... calling start on readchannel");
	
	}

static int jLoop=0;

boolean bDistrustAsynchRead=true;

// Drop Everything and Read channel...
static int iteration;
void dearChannel(SocketChannel acceptedChannel) {
	System.out.println("DropEverythingAndRead... I'm waiting:");
 	ByteBuffer inbuff=ByteBuffer.allocate(1024);
 	try {
		acceptedChannel.configureBlocking(true);
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		System.out.println("DEAR: (ignoring) Config failure:"); 
		e1.printStackTrace();
	}
 	int i;
		while (true) {
			inbuff.clear();
     		int nBytes=0;
			try {
				nBytes = acceptedChannel.read(inbuff);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("DEAR: (ignoring) Read failure:"); 
				e.printStackTrace();
			}	
     		System.out.print("Read=" + nBytes);
     		inbuff.flip();
     		String value="";
     		for (i=0; i<nBytes; i++) {
     			value = value + inbuff.get();
     			}
     		System.out.println("I have a string:" + value);
     		// TODO: actual enqueue
		    iteration++;
		}
	
	}

//static HashMap<SelectionKey, ClientSession> clientMap = new HashMap<SelectionKey, ClientSession>();

void loop() /*throws Throwable*/ {
	try {
		selector.selectNow();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	for (SelectionKey key : selector.selectedKeys()) {
		try {
			if (!key.isValid())
				continue;
			if (key == serverKey) {
				SocketChannel acceptedChannel = serverChannel.accept();
				if (acceptedChannel == null)
					continue;
				acceptedChannel.configureBlocking(false);
				SelectionKey readKey = acceptedChannel.register(selector, SelectionKey.OP_READ);
				NIOClientSession humanPlayer = new NIOClientSession(readKey, acceptedChannel);
                CardGame.theGame.join(humanPlayer);
            	System.out.println("creating new player:" + humanPlayer.getPID()); 
				
				clientMap.put(readKey, humanPlayer);
				System.out.println("New client ip=" + acceptedChannel.getRemoteAddress() + ", total clients=" + MiniServer.clientMap.size());
				}
			if (key.isReadable()) {
				NIOClientSession sesh = clientMap.get(key);
				if (sesh == null)
					continue;
				sesh.read();
				}

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	selector.selectedKeys().clear();
	//
	// deque messages if in queue... zzz
	// This is questionable. Dubious...
	MainServer.cgk.step(1);
	
}

/*
 * kill ->
 */
void loopOldDeadLoop() throws IOException {
	if (bInProgress)
		return;
	if (bKillIO)
		return;
	selector.selectNow();
	System.out.println("Starting Loop iteration: " + jLoop + " times:");
	for (SelectionKey key : selector.keys()) {
		if (!key.isValid())
			continue;
		if (key.isAcceptable()) {
			/*
			 * Go accept the connection
			 */
			 if (key != serverKey) { 
				System.out.println("Anomolous accept: Can't happen..."); 
			 	}
             // Otherwise accept the channel, map it, and initiate read/processing
			 SocketChannel acceptedChannel = serverChannel.accept();
             if (acceptedChannel == null)
            	 continue;
             SelectionKey readKey=null;
             if (!bDistrustAsynchRead) {
            	 acceptedChannel.configureBlocking(false);             	
            	 readKey = acceptedChannel.register(selector, SelectionKey.OP_READ);
             	}
             NIOClientSession humanPlayer;
             humanPlayer = CardGame.theGame.getChannelOwner(acceptedChannel);
             if (humanPlayer == null)  {
                 	/*
                 	 * Now put a humanPlayer into the game
                 	 */
               	  	humanPlayer = new NIOClientSession(readKey, acceptedChannel);
                    CardGame.theGame.join(humanPlayer);
                	System.out.println("creating new player:" + humanPlayer.getPID()); 
                    clientMap.put(readKey, humanPlayer);
                	}
             else {
                	System.out.println("adding channel to existing player:" + humanPlayer.getPID());
                	// Still need to send http jazz
                	// should do this but... not working: new NIOClientSession(readKey, acceptedChannel);
                	// Now (just to see if this is the problem...)
                	// spawn a blocking read on this channel...
                	//acceptedChannel.configureBlocking(true);
                	System.out.println("Creating read channel for player:" + humanPlayer.getPID());
                	// Now explicitly write HTTPHeader(acceptedChannel)
                	/* No! Do NOT create a new sessionl...
                	 * NIOClientSession cs=new NIOClientSession(readKey, acceptedChannel);
                	 */
                	humanPlayer.writeHTTPHandshake(acceptedChannel);
                	if (bDistrustAsynchRead)
                		dearChannel(acceptedChannel);
                	if (bDistrustAsynchRead) 
                		spawnServerRead(acceptedChannel);
                	else
                        clientMap.put(readKey, humanPlayer);

                 	}
			}	// serverkey (i.e. an accept)
		if (key.isReadable()) {
			System.out.println("Readable! Progress. Initiating read.");
            NIOClientSession sesh = clientMap.get(key);            
            if (sesh == null) {
            	// No place to put data...
            	System.out.println("Internal Error: Can't find session.");
            	continue;
            	}
            SocketChannel sc= (SocketChannel) key.channel();
            sesh.read(sc);	
			}
		if (key.isWritable()) {
			// Ready to write again...
			//  just set the bit and send packets outbound if they were waiting...
			System.out.println("Write completed... A little at a losss for what to do...");
			}
 		jLoop++;
			
		}
	selector.selectedKeys().clear();
	bInProgress = false;
                 
	}
		

void loopXXX() throws IOException {
	/*
	 * Non-blocking select is selectNow()
	 */
	//selector.selectNow();	// selector may throw...
	//System.out.println("Loop:");
	int readyChannels = selector.selectNow();
	  if(readyChannels == 0) return;
	  
	Set<SelectionKey> selectedKeys = selector.selectedKeys();

	Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
	while(keyIterator.hasNext()) {
		SelectionKey key = keyIterator.next();
	// for (SelectionKey key : selector.keys()) {
		System.out.println("Starting Loop iteration: " + jLoop + " times:");
		
		// 
		// Not sure how this is possible...
		if (!key.isValid())
			continue;
		if (key.isAcceptable()) {
			/*
			 * Go accept the connection
			 */
			 if (key == serverKey) {
                 SocketChannel acceptedChannel = serverChannel.accept();

                 if (acceptedChannel == null)
                         continue;
                 // Otherwise accept the channel, map it, and initiate read/processing
                 acceptedChannel.configureBlocking(false);
             	
                 SelectionKey readKey = acceptedChannel.register(selector, SelectionKey.OP_READ);
                 if (r1 == null) r1 = readKey;
                 else if(r2 == null) r2 = readKey;
                 NIOClientSession humanPlayer;
                 humanPlayer = CardGame.theGame.getChannelOwner(acceptedChannel);
                 if (humanPlayer != null) {
                	System.out.println("adding channel to existing player:" + humanPlayer.getPID());
                	// Still need to send http jazz
                	// should do this but... not working: new NIOClientSession(readKey, acceptedChannel);
                	// Now (just to see if this is the problem...)
                	// spawn a blocking read on this channel...
                	//acceptedChannel.configureBlocking(true);
                	System.out.println("Spawning server read.");
                	spawnServerRead(acceptedChannel);
            		keyIterator.remove();
            		jLoop++;
                	continue; // i.e. don't wait for reads here...
                 	}
                 if (humanPlayer == null)  {
                	  humanPlayer = new NIOClientSession(readKey, acceptedChannel);
                     CardGame.theGame.join(humanPlayer);
                 	System.out.println("creating new player:" + humanPlayer.getPID()); 
                 	}
                 clientMap.put(readKey, humanPlayer);
                
                 /*
                  * Now put humanPlayer into the game
                  */

			 	}
			}

		if (key.isReadable() || MainServer.bForceRead) {
			/*
			 * Go read data from the connection
			 * 
			 * ... get the actual channel from:
			 * 			key.channel()
			 */
			System.out.println("Readable! Progress.");
            NIOClientSession sesh = clientMap.get(key);            
            if (sesh == null)
                    continue;
            sesh.read(null);	// loopXXX shouldn't get called...
			}
		if (key.isWritable()) {
			// Ready to write again...
			//  just set the bit and send packets outbound if they were waiting...
			System.out.println("Write completed... A little at a losss for what to do...");
			}
		jLoop++;
		
		keyIterator.remove();

		}

	System.out.println("Loop iteration finished " + jLoop + " times:");
	
	} // loop()
/*
 * Hmmm. bug with 
 * 		.keys().clear();
 * suppose another key was set before we could read it... fix:
 */
//selector.selectedKeys().clear();

}
