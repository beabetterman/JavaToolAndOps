package wzm.ioops;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {
	
	//public static Socket clientSocket = null;

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		startClients(5);
		
//		Socket socket = null;
//		try {
//			socket = new Socket("localhost", 10086);
//			System.out.println("Client has started");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		PrintWriter pw = new PrintWriter(socket.getOutputStream());
//		pw.write("This is message from Client. over");
//		pw.flush();
//		
//		socket.shutdownOutput();
//		
//		BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//		String receivedLine = null;
//		while( (receivedLine = bf.readLine()) != null ) {
//			System.out.println("In Client side, get Message: "+ receivedLine);
//		}
//		
//		bf.close();
//		pw.close();
//		socket.close();
		
	}
	
	public static void startClients(int number) {
		
		ExecutorService executor = Executors.newCachedThreadPool();
		for(int i =0; i< number; i++) {
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						Socket clientSocket = null;
						clientSocket = new Socket("localhost", 10086);
						//clientSocket.setKeepAlive(true);
						PrintWriter pw = null;
						
						BufferedReader bf = null;
						for(int index = 0; index < 3; index++) {
							System.out.println("The index is :" + index);
							//clientSocket = new Socket("localhost", 10086);
							pw = new PrintWriter(clientSocket.getOutputStream());
							
							pw.write("This is from client:" + Thread.currentThread().getName()+", index is :" + index+", over");
							pw.flush();
							
							clientSocket.shutdownOutput();
							bf = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
							String receivedLine = null;
							System.out.println("Before bf.readLine()");
							while( (receivedLine = bf.readLine()) != null ) {
								System.out.println("In Client side, get Message: "+ receivedLine);
							}
							Thread.sleep(100);
							
						}
						
						
						
						bf.close();
						pw.close();
						clientSocket.close();
						
					} catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
		
	}

}
