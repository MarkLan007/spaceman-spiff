package cardGame;

/*
 * NIOClientSession is the key component of HumanPlayer.
 * It manages:
 * 1. Buffer characters until there is a whole command string
 * 2. Manage the nplayer id based on the actual stream (rather than something the client tells me)
 * 3. Add a timeout so that if I don't get a whole command within a reasonable period of time I process an error
 */
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NIOClientSession {

	SelectionKey selectionKey;
	SocketChannel chan;				// full (half, actually) duplex channel for both reading and writing to client
	SocketChannel incomingChann;		// second channel client sends back moves on
	public static final int BUFFMAX=1024;
	ByteBuffer inbuff;
	ByteBuffer outbuff;
	
	private int nPlayerId=-1;
	void setPID(int n) { nPlayerId = n; }
	int getPID() { return nPlayerId; }
	String commandString=null;

	
	ByteBuffer headerInbuff=ByteBuffer.allocate(1024);

	void writeHTTPHandshake(SocketChannel chan) {
		int i, bytesRead=0;
		if (!chan.isBlocking()) {
			inbuff.clear();
			try {
			bytesRead = chan.read(inbuff);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Unexpectedly Bombed reading http header; moving on.");
				e.printStackTrace();
				}
			inbuff.flip();
			String connectString="";
			for (i=0; i<bytesRead; i++) {
				connectString = connectString + (char) inbuff.get();
				//if (i > 20) break;
				}
			System.out.println("client connectstring:" + connectString);
			}
			
		/*
		 * Only need one line to spoof one magic line as being a valid HTTP processor
		 */
		String[] hstrings= {
				//"HTTP/1.1 200 OK",
				"HTTP/1.1 101 Switching Protocols",
				// -- the bare minimum...
				//"Upgrade: websocket",
				//"Connection: Upgrade",
				//
				//"HTTP/1.1 202 Accepted",
				// "200 OK",
				/*
				"Server: mimitulabs",
				"Date: Fri, 1 Mar 2018 10:55:22 GMT",
				"Content-Type: text/html",
				"Transfer-Encoding: chunked",
				"Connection: keep-alive",
				"Last-Modified: Fri, 1 Mar 2018 10:55:22 GMT",
				"Vary: Accept-Encoding",
				"Cache-Control: max-age=60, private",
				//"Expires: Fri, 1 Mar 2018 10:55:22 GMT",
				//"Content-Encoding: gzip",	
				"<!DOCTYPE html>",
				"<html lang=\"en-US\" class=\"no-js\">",
				"<head>",
				"</head>",
				"<body class=\"f11 f11v5\">",
				"</body>",
				"</html>",
				*/

				};
		System.out.println("Laying down header:");
		for (i=0; i<hstrings.length; i++) {
			System.out.println(i + ". " + hstrings[i]);
			write(hstrings[i] + "\r\n");
			}
		
		}
	
	NIOClientSession(SelectionKey sk, SocketChannel ch) throws IOException {
		selectionKey = sk;
		/*
		 * This is the punchline: make channel non-blocking
		 */
		ch.configureBlocking(false);
		chan = /* (SocketChannel) */ ch; // .configureBlocking(false);		// this could throw an IOException
		/*
		 * allocate buffs once, here
		 */
		inbuff = ByteBuffer.allocateDirect(BUFFMAX);
		outbuff = ByteBuffer.allocateDirect(BUFFMAX);
		
		// spoofing a connect for telnet
		//ProtocolMessage xpm=new ProtocolMessage(ProtocolMessageTypes.PLAYER_ERROR, 
		//		"+OK");
		//		"334 VXNlcm5hbWU6;" );
		//String sxx=xpm.encode();
		
		boolean bWriteHTTPHeader=false;
		if (bWriteHTTPHeader) {
			writeHTTPHandshake(ch);			
			}
		
		//write("%Play me something warm, play me something mellow.");		
		ProtocolMessage pm=new ProtocolMessage(ProtocolMessageTypes.PLAYER_ERROR, 
						"%Play me something warm, play me something mellow.");
		String s=pm.encode();
		write(s);
		pm=new ProtocolMessage(ProtocolMessageTypes.PLAYER_ERROR, 
				"%Play me something I can sink my teeth in like jello.");
		s=pm.encode();
		write(s);
		//write("%Play me something I can sink my teeth in like jello.");
		//outbuff = new ByteBuffer();
		/*
		 * Key point: Do NOT set the pid on creation as the human player can participate in various games which reset the pid
		 */
		}
	
	void disconnect() {
		try {
			/*
			 * First disconnect at the game level
			 * TODO: Note the improvident use of the global static variable...
			 * ... should do this by enqueing a priority message to the game kernel to disconnect this player...
			 */
			CardGame.theGame.disconnect(nPlayerId);
			/*
			 * Clear out the connection-layer data structures.
			 */
			if(selectionKey != null)
				selectionKey.cancel();
			if (chan == null)
				return;
			System.out.println("disconnect:" + (InetSocketAddress) chan.getRemoteAddress());
			chan.close();
			// TODO: delete entry in playersArray and replace with a robot...
		} catch (Throwable th) { /* Ignored for now */ }
	}
	
	/*
	 * Obosolete -- this version of read() is not used; see read();
	 * TODO: eliminate this code when no longer instructive
	 */
	void read(int old) {
		try {
			// TODO: actually read

			} catch (Throwable th) {	// whoops.
				disconnect();
				th.printStackTrace();
				}
		}
		

	/*
	 * Obsolete -- use read()
	 */
    void read(SocketChannel socket) {
    	int iBytes = -1;

    	if (socket == null)
    		socket = incomingChann;
        inbuff.clear();
    	String errstring="incomingChan:" + socket;
        	try {
				iBytes = socket.read(inbuff);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("** read exception on" + errstring + " **"); 
				e.printStackTrace();
			}
        	
        	if (iBytes == 0 && inbuff.position() > 0) {
        		iBytes = inbuff.position();
        		System.out.println("Doctoring iBytes: actually read:" + iBytes);
        		}

                if (iBytes == -1) {	// TODO: use NIOClientLayer variable previously set by call to setPID when we are put in a game
                		System.out.println("Player(" + this.nPlayerId + ") disconnected.");
                        disconnect();
                }


                if (iBytes < 1)
                        return; // if zero; nothing to add

           		System.out.println("happy happy joy joy!");
                inbuff.flip();
                String str="";
                // 	Java reads 16 bits per character; Yeah, yeah; this cost my a year of precious life expectancy, I'm sure
                int limit=iBytes;	
           		System.out.println("starting read:(" + limit + " chars):" + str);
                for (int i=0; i<limit; i++) {
                	char c=(char) inbuff.get();
                	if (JCFrameClient.isprintable(c))
                		str = str + c;
                	else if (c == '\0' || c == '\n' || c == '\r') {
                		str = str + c;
                		// get everything for now...
                		//break;
                		}
                	}
           		System.out.println("I am so happy to be here... reading(" + limit + " chars):" + str);
            	/*
            	 * Fragmentation/reassembly -- add to command string; forward on if complete command (i.e. has \n)
            	 */
            	commandString = str;
            	if (commandString.contains("\n")) {             		
            		ProtocolMessage msg=new ProtocolMessage(str);    	
	                /*
	                 * Enqueue this as a message for the GameKernel when complete msg seen
	                 */
           			MainServer.cgk.enqueue(msg);
           			commandString = "";
            		}

    }
	
    /*
     * write -- convert string to buffer and write
     */
    void write(String msg) {

    	/*
    	 * Clear outbuff before you write to it, even the first time. 
    	 * Otherwise it will crash.
    	 * And then make sure you remember to flip buffers, not burgers. 
    	 * The last vestige of seven days I'll never get back, having been flumoxed by a failure to flip.
    	 */
		outbuff.clear();
		int i=0;
    	for (char c: msg.toCharArray()) {
    		outbuff.put((byte) c);
    		i++;
    		}
		outbuff.put((byte)'\n');
		//outbuff.put((byte)'\r');
		//outbuff.put((byte) 0);		// Do I really have to do this? yes. the reader looks for end of line to stop processing in the buffer

    	System.out.println("NIOwrite:length[" + (i+1) + "] Outbuff stats: position:" + outbuff.position() + ", pos:" + (outbuff.limit() - outbuff.remaining()) + " limit:" + outbuff.limit());
    	/*
    	 * This bug makes no fucking sense.
    	 * So outbuff has the right things in it.
    	 * but when I call chan.write it sends a different string.
    	 * But JCFrame client was reading in the same thing that chan.write was actually writing (before I completely changed it.)
    	 * When I connect with TELNET I get the first message (or rather the last 24 characters of it) 4 different times.
    	 * So there are 2 completely inscrutable bugs here.
    	 * 
    	 * Hmmm: maybe outbuff when cast to a string gives it's name...
    	 */
        try {
        	outbuff.flip();			// Go from writing-into outbuff to preparing it go from writing into with put to outgoing with write
			chan.write(outbuff);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("write crashed. Not sure why. Moving on.");
				}
    	
    }
	void read() {
		try {
			System.out.println("happy, happy, joy, joy!");
			int nBytes = -1;

			inbuff.clear();
			try { nBytes = chan.read(inbuff);
			} catch (Throwable t) { }

			if (nBytes == -1)
				disconnect();

			if (nBytes < 1)
				return; // if zero
			int i;
			String s="";
			inbuff.flip();
			for(i=0; i<nBytes; i++) {
				char c = (char) inbuff.get();
				s = s + c;
				}
			System.out.println("I'm so happy to be here:<" + s + ">");
			// process...
        	commandString = s;
        	if (commandString.contains("\n")) {             		
        		ProtocolMessage msg=new ProtocolMessage(commandString);    	
                /*
                 * Enqueue this as a message for the GameKernel when complete msg seen
                 */
       			MainServer.cgk.enqueue(msg);
       			commandString = "";
        		}

		} catch (Throwable t) {
			disconnect();
			t.printStackTrace();
		}
	}

	
}
