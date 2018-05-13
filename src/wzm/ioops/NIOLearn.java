package wzm.ioops;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.text.SimpleDateFormat;

public class NIOLearn {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		// This case no need about the Selector work...
//		Selector selector = Selector.open();

		// 读取文件内容。
		readFromFile();
		
		// 写入文件。
		writeToFile();

	}

	/**
	 * Channel读取文件。
	 */
	public static void readFromFile() {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream("src\\wzm\\ioops\\test.txt");
			FileChannel fileChannel = fileInputStream.getChannel();
			ServerSocketChannel.open();
			ByteBuffer byteBuffer = ByteBuffer.allocate(512);
			
			// 从channel中读取信息到缓冲区。 fc.read(bBuffer) 返回-1时说明本次读取信息时读到了文件末尾符号。
			while (fileChannel.read(byteBuffer) != -1) {
				// 写入缓冲区，一直到缓冲区满。
				if (byteBuffer.hasRemaining()) {
					continue;
				} else {
					// 将缓冲区的值进行对应的处理。并清空缓冲区。
					
					String str = new String(byteBuffer.array(), "UTF-8");
					System.out.print("Check:"+str);
					System.out.print("Each position:"+byteBuffer.position());
					byteBuffer.flip();
					byteBuffer.clear();
				}
			}
			// 处理最后一次读取的信息。
			// IMPORTANT 最后一次读取，要读取真实的值，而不是整个的buffer.读取的方法如下。
			byteBuffer.flip();
			
//			System.out.println("Last time position:"+byteBuffer.position() + ", limit:" + byteBuffer.limit());
			byte[] tbyte = new byte[byteBuffer.limit()];
			byteBuffer.get(tbyte); //byteBuffer.get(tbyte, 0, byteBuffer.limit());
			System.out.print(new String(tbyte, "UTF-8"));
			
			byteBuffer.clear();
			
			System.out.println("Read is Done.");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void writeToFile() {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream("src\\wzm\\ioops\\test.txt");
			FileChannel fileChannel = fileOutputStream.getChannel();
			ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
			SimpleDateFormat sft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
			String str = null;
			for (int i = 0; i < 200; i++) {
				// 插入时间信息
				str = "Line" + i + ", Time:" + sft.format(System.currentTimeMillis()) + ":I'm from output stream.\r";
				str = new String(str.getBytes(), "UTF-8");
				System.out.print("Writing:" + str);
				// 监控缓冲区的大小，对缓冲区进行操作。所剩空间不够写入时，会执行写入操作，并清空缓存。
				if (byteBuffer.remaining() > str.getBytes().length) {
					byteBuffer.put(str.getBytes());
				} else {
					byteBuffer.flip();
					fileChannel.write(byteBuffer);
					byteBuffer.clear();
					byteBuffer.put(str.getBytes());
				}
			}
			// 处理最后一个的写入操作。
			byteBuffer.flip();
			fileChannel.write(byteBuffer);
			byteBuffer.clear();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
