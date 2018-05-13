package wzm.ioops;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		startServer();
//		ExecutorService executor = Executors.newCachedThreadPool();
//		ServerSocket serverSocket = null;
//		try {
//			serverSocket = new ServerSocket(12345);
//			System.out.println("Server is starting.");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		Socket socket = null;
//		try {
//			System.out.println("Before accpet");
//			socket = serverSocket.accept();
//			System.out.println("After accpet");
//			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//			String receivedLine;
//			while( (receivedLine = br.readLine()) != null ) {
//				System.out.println("In server side, get message: "+ receivedLine);
//			}
//			socket.shutdownInput();
//			
//			PrintWriter pw = new PrintWriter(socket.getOutputStream());
//			pw.write("This is Message from Server");
//			pw.flush();
//			
//			pw.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			
//			socket.close();
//			serverSocket.close();
//		}
		
		
	}
	
	/*
	 * 启动服务器端。
	 */
	public static void startServer() throws IOException{
		
		
		
		ServerSocketChannel scChannel = ServerSocketChannel.open();
		
		scChannel.configureBlocking(false);
		ServerSocket serverSocket = scChannel.socket();
		serverSocket.setReuseAddress(true);
		serverSocket.bind(new InetSocketAddress(10086));
		
		Selector serverSelector = Selector.open();
		scChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
		System.out.println("Before the true loop");
		try {
			while(true) {
				// TODO sth
				if(serverSelector.select(500) == 0) {
					//System.out.println("In check loop");
					continue;
				}
				
				if(serverSelector.select() > 0) {
					System.out.println("In check loop, selected number:" + serverSelector.select());
					continue;
				}
				
				Iterator<SelectionKey> keyIterator = serverSelector.selectedKeys().iterator();
				
				while(keyIterator.hasNext()) {
					SelectionKey sk = keyIterator.next();
					keyIterator.remove();
					System.out.println("selectedKeys size:" + serverSelector.selectedKeys().size());
					SelectableChannel sc = sk.channel();
					System.out.println("before check the key iterator state");
					// 针对各种请求，进行对应的处理
					if( sk.isValid() && sk.isAcceptable() ) {
						
						System.out.println("Server is acceptable");
					
						SocketChannel socketChannel = ((ServerSocketChannel)sc).accept();
						registerSocketChannel(serverSelector, socketChannel);
					} else if( sk.isValid() && sk.isConnectable() ) {
						System.out.println("Server is connectable");
						
					} else if( sk.isValid() && sk.isReadable() ) {
						// 与客户端进行交互
						readSocketChannel(sk);
					}
					
				}
			}
		} catch(Exception e) {
			
		} finally {
			serverSocket.close();
		}
		
		
		
	}

	/*
	 * 从Socket中读取信息。
	 */
	private static void readSocketChannel(SelectionKey sk) throws IOException {
		SocketChannel clientSocketChannel = (SocketChannel)sk.channel();
		InetSocketAddress clientAddress = (InetSocketAddress) clientSocketChannel.getRemoteAddress();
		
		Integer port = clientAddress.getPort();
		String hostString = clientAddress.getHostString();
		
		
		ByteBuffer contextBytes = (ByteBuffer)sk.attachment();
		int realLen = -1;
		
		realLen = clientSocketChannel.read(contextBytes);
		
		if(realLen == -1) {
			System.out.println("No data get from socket:"+hostString+":"+port);
			return;
		}
		
		contextBytes.flip();
		
		// 编解码
		byte[] messageBytes = contextBytes.array();
		String messageEncoded = new String(messageBytes, "UTF-8");
		String message = URLDecoder.decode(messageEncoded, "UTF-8");
		
		// 约定的结束标识。
		if(message.indexOf("over") != -1) {
			contextBytes.clear();
			System.out.println("Get message from "+hostString+":"+port+". Message:" + message);
			
			ByteBuffer sendBuffer = ByteBuffer.wrap(URLEncoder.encode("Feedback from server to:" + hostString +":" + port, "UTF-8").getBytes());
			clientSocketChannel.write(sendBuffer);
			clientSocketChannel.close();
			
			
		} else {
			System.out.println("Message receive job not Done yet. Current message:" + message);
			contextBytes.position(realLen);
			contextBytes.limit(contextBytes.capacity());
		}
		
		
	}

	/*
	 * Register the client socket in selector, to get the message
	 */
	private static void registerSocketChannel(Selector serverSelector, SocketChannel socketChannel) throws IOException {
		socketChannel.configureBlocking(false);
		socketChannel.register(serverSelector, SelectionKey.OP_READ, ByteBuffer.allocate(2048));
		
	}

}
