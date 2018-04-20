package cardGame;

/*
 * This was version 1.0 of listener. 
 * It tries to use threads and never really worked right. Development abandoned for NIOServer (i.e. MainNIOServer, and NIOClientSession)
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/*
 * CardGameKernel handles the enqueing of protocol messages, and resume to process them
 * It provides the mutual exclusion so that various threads can receive input and by calling enqueue make sure that they 
 * are thread-safe.
 * 
 * MutexK protects all (user i.e. robot or local human) threads accessing the kernel.
 */
public class CardGameKernel {
	static Semaphore mutexK=new Semaphore(1);	/// only one thread can be in the Kernel at a time... (Is this a problem? Isn't only one active?)
	static Semaphore mutexQ=new Semaphore(1);	// only one process (included Kernel process) can access the messageQueue at a time
	static LinkedList<ProtocolMessage> messageQueue=new LinkedList<ProtocolMessage>();
	static Boolean kernelPause=false;
	static CardGame g=null;
	
	/*
	 * This only called (hopefully) when kernel is paused and hasn't been restarted
	 */
	void qDump() {
		if (g == null)
			return;
		for(ProtocolMessage pm : messageQueue) {
			g.gameErrorLog(pm.encode());
			}
		}
	
	/*
	 * setCardGame -- take a previously constructed cardgame (with players) TODO:
	 * This is (hopefully) temporary; need protocol messages to start and join a
	 * game
	 */
	void setCardGame(CardGame game) {
		g = game;		
		}
	
	void pause() {
		try {
			mutexK.acquire();
			kernelPause = true;
			mutexK.release();
			}
		catch(InterruptedException e) {
			// can't happen... TODO: Log something
			}

		
		}
	
	void enqueue(ProtocolMessage pm) {
		try {
			mutexQ.acquire();
			messageQueue.addLast(pm);
			mutexQ.release();
			}
		catch(InterruptedException e) {
			// can't happen... TODO: Log something
			}

		}

	/*
	 * step(n) step through the queue n times. -1 for fearlessly and indefinitely
	 * -- Note: this should be on its own thread -- i.e. either as a background process or from the ttyhandler
	 * can be paused by setting kernelPause = true
	 */
	void step(int n) {
		boolean bForever= (n == -1);
		int i;
		for (i=0; bForever || i<n; i++) {
			try {
			mutexQ.acquire();		// P()
			} catch(InterruptedException e) {
				// can't happen... TODO: Log something
				}
			/*
			 * Access the queue, and release before processing the message.
			 * i.e. allow access to the queue by other processes here...
			 * 		Suppose the process calls enqueue? That's fine
			 */
			ProtocolMessage pm=null;
			if(messageQueue.size() > 0)	
				pm = messageQueue.removeFirst();
			mutexQ.release();		// V()
			if (pm != null) {
				/*
				 * There can only be one thread processing moves at a time; 
				 *  this is independently of the queue access.
				 *  Anyone who accesses g.process must defend it with P-s and V-s
				 *  Maybe process should just do this...
				 */
				g.process(pm);
				}
			else if (bForever){	// nothing to do. Sleep 100 milliseconds
				msleep(100);
				break;
				}
			if (kernelPause)
				break;
			}
			
	}

	/*
	 * Kernel resume. Dequeue messages while there are any
	 *  if someone says pause, stop messages and break.
	 *  
	 *  This is probably not the best way to do this. Instead, see step()
	 */
	void resume() {
		try {
				while (true) {
						if (kernelPause) {
							/*
							 * Someone i.e. a robot or a most likely a local (i.e. non remote) human said pause
							 * leave things in the queue and return
							 */
							kernelPause = false;
							break;
							}
						mutexQ.acquire();		// P()
						/*
						 * Access the queue, and release before processing the message.
						 * i.e. allow access to the queue by other processes here...
						 * 		Suppose the process calls enqueue? That's fine
						 */
						ProtocolMessage pm=null;
						if(messageQueue.size() > 0)	
							pm = messageQueue.removeFirst();
						mutexQ.release();		// V()
						if (pm != null) {
							/*
							 * There can only be one thread processing moves at a time; 
							 *  this is independently of the queue access.
							 *  Anyone who accesses g.process must defend it with P-s and V-s
							 *  Maybe process should just do this...
							 */
							g.process(pm);
							}
						else	// nothing to do. wait to get resumed
							break;
						}
					
				}
		catch(InterruptedException e) {
				// can't happen... TODO: Log something
				}
		
		}
	
	public static final short PORT = 1081;
	public static final String INTERNAL_HOST_NAME = "127.0.0.1"; // "localhost";
	public static final int BACKLOG = 10;
	
	//GameKernel() {	}
	
	/*
	 * open port and listen for connections
	 * This should fork on the handler
	 */
	public static void listener() {
		ServerSocket sock;
		Socket clientSocket;
		System.out.println("Listener started...");

		try {
			sock = new ServerSocket(PORT, BACKLOG, InetAddress.getByName(INTERNAL_HOST_NAME));
			// sock = new ServerSocket(PORT);
			if (sock == null)
				System.out.println("can't happen: socket error.");
			else
				System.out.println("socket created:" + sock.getLocalSocketAddress());

			//sock.b
			while ((clientSocket = sock.accept()) != null) {
				processConversation(clientSocket);
				}
			
		} catch (IOException e) {
			System.out.println(e);	// not err... using ttyclient.			
			}
		
		}
	
	public static void connect() {
		
		}
	
	static void msleep(int ms) {
		try {
			System.out.printf("Sleeping...");
			Thread.sleep(ms);
			} catch (InterruptedException e) {
				System.out.printf("Exception caught");
				}

	}
	
	static void processConversation(Socket s) throws IOException {
		BufferedReader is;	// input stream, from
		PrintWriter os=null;		// output stream, to
		String request;
		String str="";	// make it enter the loop
		
		try {
			String from = s.getInetAddress().toString();
			System.out.println("Accepted connection from: " + from);
			is = new BufferedReader(new InputStreamReader(s.getInputStream()));
			//if (!is.ready()) { 	msleep(500); }
			//str = is.readLine();
			while (str != null) {
				if (os == null)	// called only one time...
					System.out.println("ReadFromNet>" + str + "...");
				if (os == null) {	// called only one time; throw exception on failure
					os = new PrintWriter(s.getOutputStream(), true);
					if (os == null) {
						throw new IOException();
						}
					os.println("welcome to gameserver");
					}
				// using ready() doesn't help.
				if (!is.ready()) { 	msleep(5000); continue; }
				str = is.readLine();
				System.out.println(str);
				}
			s.close();
			
			} catch (IOException e) {
				System.out.println("IOexception reading socket");
				}
		}
	
	static void processConversationXXX(Socket s) throws IOException {
		System.out.println("Connection from " + INTERNAL_HOST_NAME + ":" + s.getInetAddress());
		// read-eval-print loop
		Scanner scanner = new Scanner((Readable) s);
		String input = scanner.next();
		while (input != null) {
			input = scanner.next();
			System.out.println("ReadFromNet>" + input);
			}
		scanner.close();
		}

	static void process (String sInput, PrintStream outStream) {
		
		if (sInput.contains("exit")) {
			//ServerMainREPLoop.serverShutdown();
			System.exit(0);
			}
		else {
			outStream.print("Processing:" + sInput + "\r\n");
			outStream.flush();
			}
		
	}

}
