package cardGame;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class MiniServer {

	public static void main(String[] args) throws Throwable {
		new MiniServer(new InetSocketAddress("localhost", 1337));
		}

	ServerSocketChannel serverChannel;
	Selector selector;
	SelectionKey serverKey;

	MiniServer(InetSocketAddress listenAddress) throws Throwable {
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverKey = serverChannel.register(selector = Selector.open(), SelectionKey.OP_ACCEPT);
		serverChannel.bind(listenAddress);

		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
			try {
				loop();
				} catch (Throwable t) {
					t.printStackTrace();
					}
			}, 0, 500, TimeUnit.MILLISECONDS);
	}

	static HashMap<SelectionKey, ClientSession> clientMap = new HashMap<SelectionKey, ClientSession>();

	void loop() throws Throwable {
		selector.selectNow();

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
					ClientSession clientSession = new ClientSession(readKey, acceptedChannel);
					clientMap.put(readKey, clientSession);
					System.out.println("New client ip=" + acceptedChannel.getRemoteAddress() + ", total clients=" + MiniServer.clientMap.size());
					}
				if (key.isReadable()) {
					ClientSession sesh = clientMap.get(key);
					if (sesh == null)
						continue;
					sesh.read();
					}

			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		selector.selectedKeys().clear();
	}
	
// ClientSession:
	
	class ClientSession {
		SelectionKey selkey;
		SocketChannel chan;
		ByteBuffer inBuffer, outBuffer;
		final int MAXSIZE=128;
		ClientSession(SelectionKey selkey, SocketChannel chan) throws Throwable {
			this.selkey = selkey;
			this.chan = (SocketChannel) chan.configureBlocking(false); // asynchronous/non-blocking
			inBuffer = ByteBuffer.allocateDirect(MAXSIZE); 
			outBuffer = ByteBuffer.allocateDirect(MAXSIZE);
			}

		void disconnect() {
			MiniServer.clientMap.remove(selkey);
			try {
				if (selkey != null)
					selkey.cancel();

				if (chan == null)
					return;

				System.out.println("bye bye " + (InetSocketAddress) chan.getRemoteAddress());
				chan.close();
			} catch (Throwable t) { /** quietly ignore  */ }
		}

		char toUpper(char c) {
			if (c >= 'a' && c <= 'z')
				return (char) ('A' + (c - 'a'));
			return c;
			}
		
		void read() {
			try {
				int amount_read = -1;

				inBuffer.clear();
				try { amount_read = chan.read(inBuffer);
				} catch (Throwable t) { }

				if (amount_read == -1)
					disconnect();

				if (amount_read < 1)
					return; // if zero
				int i;
				String s="";
				inBuffer.flip();
				outBuffer.clear();
				for(i=0; i<amount_read; i++) {
					char c = (char) inBuffer.get();
					s = s + toUpper(c);
					outBuffer.put((byte) toUpper(c));
					}
				System.out.println("Char<" + s + ">");
				System.out.println("sending back " + i + " bytes");

				// turn this bus right around and send it back!
				outBuffer.flip();
				chan.write(outBuffer);
			} catch (Throwable t) {
				disconnect();
				t.printStackTrace();
			}
		}
		
	}
	
}