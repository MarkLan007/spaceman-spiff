package cardGame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class MiniClient {

	static OutputStream outStream=null;
	static InputStream inStream=null;
	
	public static void main(String[] args)  {
		Socket internetSocket=null;
		try {
			internetSocket = new Socket("localhost", 1337);
			outStream = internetSocket.getOutputStream();
			inStream = internetSocket.getInputStream();
			ttyHandler();
			if (internetSocket != null)
				internetSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}

	/*
	 * read keyboard lines and write to server, then print out.
	 */
	static void ttyHandler() {
		String input = "";
		int i=0;
		Scanner scanner = new Scanner(System.in);
		byte[] serverBuff=new byte[1024];
		byte[] ttyBuff=new byte[1024];
		
		// Subdeck deck=new Subdeck();
		while (input != null) {
			System.out.printf(". ", i);

			input = scanner.nextLine();
			input = input + "\r\n";
			for (i=0; i<input.length(); i++)
				ttyBuff[i] = (byte)input.charAt(i);

			try {
				outStream.write(ttyBuff, 0, i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int iRead=0;
			try {
				iRead = inStream.read(serverBuff);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String s="";
			for (i=0; i<iRead; i++)
				s = s + (char)serverBuff[i];
			System.out.println("gotback>"+s);
			}
		scanner.close();
		} // ttyHandler();

}
