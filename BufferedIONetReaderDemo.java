package cardGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;

public class BufferedIONetReaderDemo {	

	static boolean bGuiThread=true;
	
	public static void main(String[] args) {
		bGuiThread = false;
		URLConnectionReader urlConnectionReader = new URLConnectionReader("localhost", 1081);

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
	
	
	static class URLConnectionReader {	// must be static here in demo, since it's nested...

        BufferedReader inputReader=null;
		Boolean connectionOpen = false;

		boolean checkConnection() {
			return connectionOpen;
			}
		
	    //public static void main(String[] args) throws Exception {
		URLConnectionReader(String serverName, int portid) {
			// aaa
			URL oracle = null;
			try {
		        oracle = new URL("https://www.oracle.com/index.html");
				//oracle = new URL("http", serverName, portid, "");
				connectionOpen = true;

				//oracle = new URL("ftp", serverName, portid, "");
				//oracle = new URL("ftp", serverName, portid, "");
				//URL(String protocol, String host, int port, String file)
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				connectionOpen = false;
			}	// Fix for portid
	        URLConnection yc=null;
			try {
				yc = oracle.openConnection();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				connectionOpen = false;
			}
			try {
				inputReader = new BufferedReader(new InputStreamReader(
				                            yc.getInputStream()));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				connectionOpen = false;
			}
			/*
			 * Don's start here...
			 backgroundReader();
			 */
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

		 class MReader implements Runnable {
			   public void run() {
				     // read and service request on socket
					System.out.println("*** MReader:: background call succeeded! Yea! ***");
		            String output;
		            try {
						while ((output = inputReader.readLine()) != null) {
						    final String value = output;
						    if (bGuiThread)
						    	processTextOnGuiThread(value);
						    else
						    	processText(value);						    		
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

}