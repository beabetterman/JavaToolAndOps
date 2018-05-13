package wzm.ioops;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class ChatClientNew {

	Selector selector = null;
	
	public static boolean running = true;
	
	public static void main(String[] args) {
		
		
		ChatClientNew cc = new ChatClientNew();
		try {
			cc.initClient("localhost", 10086);
			cc.listen();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initClient(String ip, int port) throws IOException {
		
		SocketChannel sChannel = SocketChannel.open();
		sChannel.configureBlocking(false);
		// 客户端连接服务器,其实方法执行并没有实现连接，需要在listen()方法中调用channel.finishConnect();才能完成连接  
		sChannel.connect(new InetSocketAddress(ip, port));
		
		this.selector = Selector.open();
		
		sChannel.register(this.selector, SelectionKey.OP_CONNECT);
	}
	
	
	public void listen() throws IOException {
		
		while( running ) {
			this.selector.select();
			
			Iterator<SelectionKey> selectionIterator = this.selector.selectedKeys().iterator();
			while( selectionIterator.hasNext() ) {
				SelectionKey sk = selectionIterator.next();
				selectionIterator.remove();
				
				if(sk.isValid() && sk.isConnectable()) {
					SocketChannel sChannel = (SocketChannel) sk.channel();
					if( sChannel.isConnectionPending() ) {
						sChannel.finishConnect();
					}
					sChannel.configureBlocking(false);
					//ByteBuffer bf = ByteBuffer.allocate(1024);
					//bf.put(new String("Hello server!").getBytes());
					
					String echoToClient = "Hello server!#This is hello message.";
					ByteBuffer sendBf = ByteBuffer.wrap(echoToClient.getBytes());
					sChannel.write(ByteBuffer.wrap(new String("Hello server!").getBytes()));
					
					//bf.clear();
					sChannel.register(this.selector, SelectionKey.OP_READ);
				} else if(sk.isValid() && sk.isReadable()) {
					
					SocketChannel sChannel = (SocketChannel) sk.channel();
					readFromSocket(sChannel);
				}
			}
			
		}
	}

	private void readFromSocket(SocketChannel sChannel) throws IOException {
		
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		sChannel.read(buffer);
		
		String serverInfo = sChannel.getRemoteAddress().toString();
		String msg = new String(buffer.array());
		System.out.println("From server " + serverInfo +", message:" + msg.trim());
		
		// 这里因为IO阻塞了
		Scanner in=new Scanner(System.in);
		String input = in.nextLine();
		sChannel.write(ByteBuffer.wrap(input.getBytes()));
		
		if( "quit".equalsIgnoreCase(input) ) {
			sChannel.close();
			System.out.println("Client is quiting.");
			running = false;
		}
	}

}
