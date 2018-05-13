package wzm.ioops;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

public class ChatServerNew {

	Selector selector = null;
	public static HashMap<String, SocketChannel> socketList = new HashMap<String, SocketChannel>();
	
	public static void main(String[] args) {
		
		ChatServerNew cs = new ChatServerNew();
		
		try {
			cs.initServer(10086);
			cs.listen();
		} catch (IOException e) {
			// TODO Auto-generated catch block 
			e.printStackTrace();
		}

	}
	
	public void initServer(int port) throws IOException {
		
		ServerSocketChannel ssChannel = ServerSocketChannel.open();
		ssChannel.configureBlocking(false);
		ssChannel.socket().bind(new InetSocketAddress(port));
		
		// Initialize selector, and register for selector and channel.
		this.selector = Selector.open();
		ssChannel.register(this.selector, SelectionKey.OP_ACCEPT);
		
		
	}
	
	public void listen() throws IOException {
		
		System.out.println("Now listen to the channels.");
		
		while( true ) {
			// Not sure the function.
			this.selector.select();
			
			Iterator<SelectionKey> selectionIterator = this.selector.selectedKeys().iterator();
			while( selectionIterator.hasNext() ) {
				SelectionKey sk = selectionIterator.next();
				selectionIterator.remove();
				
				// TODO Debug info, need to be deleted.
				checkChannelState();
				
				if( sk.isValid() && sk.isAcceptable() ) {
					System.out.println("Server is isAcceptable.");
					ServerSocketChannel ssChannel = (ServerSocketChannel) sk.channel();
					SocketChannel sChannel = ssChannel.accept();
					sChannel.configureBlocking(false);
					sChannel.register(this.selector, SelectionKey.OP_READ);
				} else if( sk.isValid() && sk.isReadable() ) {
					System.out.println("Server is isReadable.");
					SocketChannel sChannel = (SocketChannel)sk.channel();
					//System.out.println(sk.isConnectable());
					// Chat between client and client.
					try {
						readFromSocketChannelAndTransfer(sChannel);
					} catch (IOException e) {
						System.out.println(e.getMessage());
						System.out.println(e.getStackTrace().toString());
						sChannel.shutdownInput();
						sChannel.shutdownOutput();
						sk.cancel();
					}
					
					
					// Chat between server and client.
					//readFromSocketChannel(sChannel);
				} else if( sk.isValid() && sk.isConnectable() ) {
					System.out.println("Server is isConnectable.");
					
				} else if( sk.isValid() && sk.isWritable() ) {
					System.out.println("Server is isWritable.");
					SocketChannel sChannel = (SocketChannel)sk.channel();
					
					
				} else {
					System.out.println("Invalid state or not managed.");
				}
				
			}
			
		}
	}

	
	// Get message from client, and transfer message to the target client.
	private void readFromSocketChannelAndTransfer(SocketChannel sChannel) throws IOException {
		
		// TODO Debug info, need delete
		//System.out.println(socketList.keySet().toString());
		//System.out.println(socketList.values().toString());
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		
		// TODO when to check the sChannel state to make sure the opeartion is ok
		sChannel.read(buffer);
		
		String clientInfo = sChannel.socket().getRemoteSocketAddress().toString();
		String messageMeta = new String(buffer.array()).trim();
		String[] messageInfo = decodeMessage(messageMeta);
		
		// Check client registered or not.
		if( socketList.containsKey(clientInfo) ) {
			System.out.println("The client already registered on server:" + clientInfo);
			// Memory issue check, whether should update or just use the older one?
			
			socketList.replace(clientInfo, sChannel);
		} else {
			socketList.put(clientInfo, sChannel);
			System.out.println("Add client to server: "+ clientInfo);
			if( "Hello server!".equalsIgnoreCase(messageInfo[0]) ) {
				sChannel.write(ByteBuffer.wrap("Hello client, you have been registered.".getBytes()));
				return;
			}
		}
		
		
		SocketChannel targetChannel = null;
		// Check target is registered or not.
		if( socketList.containsKey(messageInfo[0]) ) {
			System.out.println("Write to client:" + messageInfo[0] + ", msg:" + messageInfo[1]);
			targetChannel = socketList.get(messageInfo[0]);
			targetChannel.write(ByteBuffer.wrap( messageInfo[1].getBytes() ));
		} else {
			System.out.println("Target is not registered on server : " + messageInfo[0]);
			sChannel.write(ByteBuffer.wrap("Target is not found.".getBytes()));
		}
		
	}
	
	private String[] decodeMessage(String messageMeta) {
		// TODO This could be specified in more detail format. 
		// Right now the message format is : "Address#Body"
		String[] splittedByFlag = messageMeta.split("#");
		return splittedByFlag;
		
	}
	
	private void checkChannelState() {
		Iterator<Entry<String, SocketChannel>> iterator = socketList.entrySet().iterator();
		while( iterator.hasNext() ) {
			Entry<String, SocketChannel> a = iterator.next();
			System.out.println(a.getKey() + ":" + a.getValue());
			System.out.println(a.getKey() +" isOpen() :"+ a.getValue().isOpen());
		}
	}
	

	// This is for client and server chat process.
	private void readFromSocketChannel(SocketChannel sChannel) throws IOException {
		//System.out.println("in readFromSocketChannel");
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		sChannel.read(buffer);
		
		String clientInfo = sChannel.socket().getRemoteSocketAddress().toString();
		
		System.out.println("From client:" + clientInfo + ", message:" + new String(buffer.array()).trim());
		
		// This should be do when the buffer is over the capcity. 
		buffer.flip();
		buffer.clear();
		String echoToClient = "This is welcom message from server";
		//buffer.put(echoToClient.getBytes());
		
		ByteBuffer sendBf = ByteBuffer.wrap(echoToClient.getBytes());
		// 这里因为IO阻塞了
		Scanner in=new Scanner(System.in);
		String input = in.nextLine();
		sChannel.write(ByteBuffer.wrap(input.getBytes()));
		
	}

}
