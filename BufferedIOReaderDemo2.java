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

import javafx.application.Platform;
import javafx.concurrent.Task;

public class BufferedIOReaderDemo2 {	
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//BufferedIONetReaderDemo bufferedIONetReaderDemo = new BufferedIONetReaderDemo();
		URLConnectionReader urlConnectionReader = new URLConnectionReader("localhost", 1081);

		if (urlConnectionReader.checkConnection())
			; // urlConnectionReader.backgroundReader();
		else
			System.out.println("URLConnectionReader: No Connection. Connection failed closed or was closed.");
		int i=0; 
		while (urlConnectionReader.checkConnection()) {
			Thread.sleep(1000);
			i++;
			}
		System.out.println("connection closed.");
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
			try {
				backgroundReader();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/* Don't read here... spawn background reader
	        String inputLine;
	        try {
				while ((inputLine = inputReader.readLine()) != null) 
				    System.out.println("http says:" + inputLine);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
	        	inputReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
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
						    /*Platform.runLater(new Runnable() {

						        @Override
						        public void run() {
						        */
						        	processText(value);
						        	/*
						            //ta.appendText(value + System.getProperty("line.separator"));
						        	}
						    		});
						    		*/
								}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    			connectionOpen = false;
		        		}
					
				   
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
		 
		void backgroundReader() throws IOException {

			System.out.println("starting background reader...");
    		Runnable r=new MReader();
    		Thread t;
    		t = new Thread(r);
    		t.start();

    		/*
    		final Callable c=new NReader();

    		try {
				c.call();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		*/
    		
			//Thread auxThread1 = new Thread(t1); 
    			
    	/*	
    	 * No workee...
    	 * Future<Void> future = null;
			future = null;
			Future<?> submit = ExecutorService.submit(() -> {
				}
			);
			*/
    		
    		/*auxThread1.setDaemon(true);
    		auxThread1.start();
    		auxThread1.run();*/

				
		    final Task<Void> task = new Task<Void>() {
		        @Override
		        protected Void call() throws Exception {
					System.out.println("starting background reader in background...");

		            String output;
		            while ((output = inputReader.readLine()) != null) {
		                final String value = output;
		                /*Platform.runLater(new Runnable() {

		                    @Override
		                    public void run() {
		                    */
		                    	processText(value);
		                    	/*
		                        //ta.appendText(value + System.getProperty("line.separator"));
		                    	}
		                		});
		                		*/
		            		}
		    			connectionOpen = false;
		            	return null;
		        		}
		    		};	// Task
		    System.out.println("calling start");
		    new Thread(task).start();
			}	// backgroundReader	
	
		static void processText(String text) {
			System.out.println("read>>>" + text);
			}

	}

}