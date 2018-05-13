package wzm.threadops;

import java.io.File;

public class FinalFinallyOps {

	public static String static_str;
	// final 的static 变量需要在static模块初始化
	public final static String static_final_str;
	public final String final_str;
	public String normal_str;
	
	static {
		static_final_str = "abc";
	}
	
	public FinalFinallyOps() {
		final_str = "Initial str value";
	}
	
	public static void main(String[] args) {
		
		System.out.println("Result:" + testFinally());
		
		threadCheck();
	}
	
	// 线程中引用变量（原始类型 和 对象类型）的情况。
	public static void threadCheck() {
		final int finalInt =0;
		// 线程中可以读，但不可以改变值。
		int j = 1;
		// 线程中不改变指向的地址（即指向的对象不能改变），但是可以方法改变指向的实例的值。。。
		FinalFinallyOps fInstance = new FinalFinallyOps();
		fInstance.normal_str = "Before the thread process.";
		
		new Thread() {
			public void run() {
				for(int i =0; i<5; i++) {
					// j = i; // Local variable j defined in an enclosing scope must be final or effectively final
					System.out.println(i + ":" +j);
					// fInstance = new FinalStaticCheck(); // Error
					fInstance.normal_str = "index value:"+i; // OK
					System.out.println(i + ":" +fInstance.normal_str);
				}
			
			}
		}.start();
	}
	
	// Check finally 的执行结果。执行的操作是嵌套的，finally会在当前层的try/catch操作后执行，而外层不会继续执行。
	public static String testFinally() {
		String result = "init";
		String fileName = null;
		try {
			try {
				File f = new File(fileName);
			} catch(Exception e) {
				e.printStackTrace();
				//System.out.println(e.getMessage()+e.getCause());
			} finally {
				result = "inner finally";
				System.out.println("Inner finally");
				File n = new File(fileName);
			}
			System.out.println("Inner after inner try/catch/finally");
		} catch(Exception e) {
			//System.out.println(e.getMessage()+e.getCause());
			e.printStackTrace();
			System.out.println("A new exception from finnally");
			System.out.println("Before set the result, the result is :" + result);
			result = "outer exception";
		}
		System.out.println("Outer after outer try/catch");
		return result;
	}
	
	
	class InnerClass{
		public void printInfo() {
			normal_str = "abcd";
			System.out.println(normal_str);
		}
	}

}
