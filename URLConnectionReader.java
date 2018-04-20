package cardGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;

public class URLConnectionReader {	

	static boolean bGuiThread=true;
	static Semaphore readAccess=new Semaphore(1);	// xyzzy synchronization
	ByteBuffer outBuff=ByteBuffer.allocate(1024);

	public static void mainXXX(String[] args) {
		bGuiThread = false;
		URLConnectionReader urlConnectionReader = new URLConnectionReader("localhost", 1081, null);

		if (urlConnectionReader.checkConnection())
			urlConnectionReader.backgroundReader();
		else
			System.out.println("URLConnectionReader: No Connection. Connection failed closed or was closed.");
		int i=0; 
		while (urlConnectionReader.checkConnection()) {
			try {
				Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("Exception: interrupted... (good!)");
					break;
					}
			i++;
			}
		System.out.println("connection closed. (looped " + i + "Times.)");
		}
	
	
		BufferedReader inputReader=null;
		/* kill ->
        public OutputStreamWriter outStream=null;
        */
	
		Boolean connectionOpen = false;

		boolean checkConnection() {
			return connectionOpen;
			}
		
		ClientScene callback=null;
		/* kill ->
        URLConnection yc=null;
        URLConnection yc2=null;
        URLConnection writeConnection=null;
        */

		// Single socket approach
		Socket internetSocket=null;
		OutputStream outStream=null;
		InputStream inStream=null;

        //public static void main(String[] args) throws Exception {
		URLConnectionReader(String serverName, int portid, ClientScene callbackParam) {
			// aaa
			callback = callbackParam;
			/* kill ->
			URL oracle = null;
			URL oracle2= null;
			*/
			
			try {
				/* kill ->
		        //oracle = new URL("https://www.oracle.com/index.html");
				oracle = new URL("http", serverName, portid, "");
				oracle2 = new URL("http", serverName, portid, "");
				*/
				internetSocket = new Socket(serverName, portid);
				outStream = internetSocket.getOutputStream();
				inStream = internetSocket.getInputStream();

				connectionOpen = true;

				//oracle = new URL("ftp", serverName, portid, "");
				//oracle = new URL("ftp", serverName, portid, "");
				//URL(String protocol, String host, int port, String file)
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				connectionOpen = false;
			}
			/* kill ->
			try {
				yc = oracle.openConnection();
				yc.setAllowUserInteraction(true);
				yc.setDoInput(true);
				yc.setDoOutput(true);
				//
				yc2 = oracle2.openConnection();
				writeConnection = oracle2.openConnection();
				writeConnection.setDoOutput(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			connectionOpen = false;
			}
			*/
			openReader();
	    	}
	
		/*
		 * use backgroundReader as its own thread/task
		 */
			
		final Task<Void> t1 = new Task<Void>() {
	        @Override
	        protected Void call() throws Exception {
	        	System.out.println("*** Yea! *** inside t1...");
	        	return null;
    			}
			};	// Task t1

			void semaphoreP() {
            	try {
					readAccess.acquire(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("Cannot acquire sempahore for readaccess");
					e.printStackTrace();
					}
				
				}

			void semaphoreV() {
            	readAccess.release(1);				
				}
			
			/*
			 * prepareForWrite -- a 'your_turn' has been seen.
			 *   wait for a write to complete before returning to reading.
			 *   this does NOT do block or do the write
			 */
			void prepareForWrite() {
				/*
				 * Do not close the input reader... xyzzy
				try {
					inputReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				inputReader = null;
				 */
				openWriter();
				
				}
			/* kill ->
			OutputStream actualOutputStream=null;
			InputStream actualInputStream=null;
			*/
			
			/*
			 * openReader -- close outStream (if open) and open the reader for mult reads
			 */
			void openReader() {
				
				/*
				if (outStream != null) {
					try {
						outStream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							}
					outStream = null;
				}
				*/
				/* kill -> -- this doesn't need to do anything anymore...
				if (actualInputStream != null)
					return;
					
				try {
					actualInputStream = yc.getInputStream();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				        */
				if (inputReader == null)
					inputReader = new BufferedReader(new InputStreamReader(
				        inStream));
				}

			void openWriter() {
				/* kill ->
				if (actualOutputStream != null)
					return;
				try {
					System.out.println("Opening output stream");
					actualOutputStream = writeConnection.getOutputStream();
					outStream = new OutputStreamWriter(actualOutputStream);
					outStream.write("PUT /new.html HTTP/1.1 \n\r" );
					outStream.flush();
					System.out.println("Just wrote: PUT...");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					System.out.println("Error Opening output stream/streamwriter");					
					e1.printStackTrace();
				}
				*/
			}

			/*
			 * write -- simplified
			 */
			byte[] ttyBuff=new byte[1024];

			void write(String s) {
				int i;
				s = s + "\r\n";
				for (i=0; i<s.length(); i++)
					ttyBuff[i] = (byte)s.charAt(i);

				try {
					outStream.write(ttyBuff, 0, i);
					outStream.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
				semaphoreV();	// xyzzy -- let reading resume.				
				}
			
			/*
			 * write -- close the reader, open the write, write, and reopen the reader
			 */
			// kill -> this is void now...
			/*
			byte[] bytebuff=new byte[1024];
			void writeXXX(String s) {
				prepareForWrite();
				try {
					System.out.println("?URLConnectionClient:write:" + s);
					String sDelimitted=s + "\r\n";
					//outStream.w
					//outStream.write(s + "\r\n");
					// bbb
					int i;
					for (i=0; i<sDelimitted.length(); i++)
						bytebuff[i] = (byte) sDelimitted.charAt(i);
					actualOutputStream.write(bytebuff, 0, i);
					outStream.write(sDelimitted, 0, sDelimitted.length());
					outStream.flush();
					outStream.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				openReader();
				semaphoreV();	// xyzzy -- let reading resume.
				}
			*/
			
		 class MReader implements Runnable {
			   public void run() {
				     // read and service request on socket
					System.out.println("*** MReader:: background call succeeded! Yea! ***");
		            String line;
		            try {
		            	int iteration=0;
		            	semaphoreP();
		            	boolean stopReader=false;
						while ((line = inputReader.readLine()) != null) {
						    final String value = line;
						    /*if (value.indexOf('?') != -1) {
						    	stopReader = true;
						    	} 
						    	*/
						    if (line.length() == 0)
								try {
									Thread.sleep(10);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										System.out.println("mysterious sleep awakening... Ignored");
										//e.printStackTrace();
									}
						    else {
						    	if (bGuiThread)
						    		processTextOnGuiThread(value);
						    	else
						    		processText(value);
						    	}
							// process the strings that are read...
							// aaa
							// Mostly works... But doesn't seem like the right place...
							callback.processEnqueuedStrings();
							// Ok, so this shows that enqueing is completely misguided, now
							// It should just read a line, process a line...
						    if (stopReader) {
						    	prepareForWrite();
						    	semaphoreP();
						    	System.out.println("Reader resumed... iter=" + iteration);
						    	}
						    iteration++;
							}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    			connectionOpen = false;
		        		}
					
				   
			 }

		 /*
		  * use runlater to be on the gui thread...
		  */
		 void processTextOnGuiThread(String value) {
			    Platform.runLater(new Runnable() {
			        @Override
			        public void run() {						        
			        	processText(value);		
			        	// callback.processLongServerString(value);
			        	callback.processServerLine(value);
			        	}
			    		});

		 	}
		 
		 class NReader implements Callable {
		        @Override
				public Void call() throws Exception {
					System.out.println("*** NReader: background call succeeded! Yea! ***");
					return null;
		        }
			 
		 }

		 /*
		  * Never worked..
		  *
		 private void process(String messageId) {
			 executorService.submit(() -> {
			 final Thread currentThread = Thread.currentThread();
			 final String oldName = currentThread.getName();
			 currentThread.setName("Processing-" + messageId);
			 try {
			 //real logic here...
			 } finally {
			 currentThread.setName(oldName);
			 }
			 });
			 }
		 */
		 
		void backgroundReader() {
			System.out.println("starting background reader...");
    		Runnable r=new MReader();
    		Thread t;
    		t = new Thread(r);
    		t.start();

		    System.out.println("calling start");
			}	// backgroundReader	
	
		static void processText(String text) {
			System.out.println("read>>>" + text);
			}

	}
