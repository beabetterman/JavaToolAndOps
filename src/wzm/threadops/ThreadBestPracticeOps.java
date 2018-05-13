package wzm.threadops;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/*
 * 1. ThreadLocal
 * 2. 单例模式
 * 3. AtomicInteger count = new AtomicInteger(); count.addAndGet(1); 如果是JDK8，推荐使用LongAdder对象，比AtomicLong性能更好（减少乐观锁的重试次数）。
 * 		在Java语言中，++i和i++操作并不是线程安全的，在使用的时候，不可避免的会用到synchronized关键字。而AtomicInteger则通过一种线程安全的加减操作接口。
 * 
 * 
 */
public class ThreadBestPracticeOps {
	// Don't use int for counter in multi-thread environment.
	private static int badUseCounter = 0;
	// 一写多读的计数器情况：
	private volatile static int oneWriterCounter=0;
	// 多写的计数器情况：
	private static AtomicInteger manyWriterCounter = new AtomicInteger(0);
	// 如果是JDK8，推荐使用LongAdder对象，比AtomicLong性能更好（减少乐观锁的重试次数）。
	private static LongAdder longAddCounter = new LongAdder();
	
	// SimpleDateFormat 处理方法。Note: JDK8的应用，可以使用Instant代替Date，LocalDateTime代替Calendar，DateTimeFormatter代替SimpleDateFormat
	private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};
	
	// 关键就在于这里的volatile，可以避免双重检查锁的，可能指向未初始化内存的问题。 https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
	// Method 1: 这种方法足够简单，当然你也可以使用静态内部类中的方式实现。饿汉加载就是在类被加载的时候不管有没有被调用都直接初始化（在资源开销大时比较浪费）。
	private static volatile SingletonClass singletonInstance = null;
	
	// 单例模式
	public static SingletonClass getSingletonInstance() {
		if (singletonInstance == null) {
			synchronized(singletonInstance) {
				if (singletonInstance == null) {
					singletonInstance = new SingletonClass();
				}
				// Method 2: 静态内部类，通过JVM的加载机制来实现线程安全和运行时加载。
				//singletonInstance = SingletonClass.INSTANCE;
			}
		}
		return singletonInstance;
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// 计数器测试
		checkCounter();
		
	}
	
	/*
	 * 测试各种类型计数器，在高并发时的实际运行结果。
	 */
	static void checkCounter() {
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				badUseCounter++;
				System.out.println("Thread: "+ Thread.currentThread().getName() + ",badUseCounter: " + badUseCounter);
				oneWriterCounter++;
				System.out.println("Thread: "+ Thread.currentThread().getName() + ",oneWriterCounter: " + oneWriterCounter);
				System.out.println("Thread: "+ Thread.currentThread().getName() + ",manyWriterCounter: " + manyWriterCounter.incrementAndGet());
				longAddCounter.increment();
				System.out.println("Thread: "+ Thread.currentThread().getName() + ",longAddCounter: " + longAddCounter.longValue());
			}
		};
		
		// 使用ThreadPoolExecutor需要把线程池设置的非常大，才容易复现错误。
		//ThreadPoolExecutor myTPE = new ThreadPoolExecutor(2000, 20000, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		
		// 出现特别多线程的情况，简单测试时的写法，正常情况下使用线程池来运行线程，而不要单独声明Thread使用。
		Thread thread = new Thread(runnable);
		for(int i=0; i<20000; i++) {
			//myTPE.execute(runnable);
			thread = new Thread(runnable);
			thread.start();
		}
		
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 结果实例：badUseCounter:19995, oneWriterCounter:19998, manyWriterCounter:20000, longAddCounter:20000
		System.out.println("badUseCounter:" +badUseCounter +", oneWriterCounter:"+oneWriterCounter+", manyWriterCounter:"+ manyWriterCounter +", longAddCounter:" +longAddCounter);
		
		//myTPE.shutdown();
	}

	
	static class SingletonClass{
		// Do some work as needed.
		public SingletonClass() {
			// TODO Auto-generated constructor stub
			System.out.println("SingletonClass constructor.");
		}
		
		// Method 2: 静态内部类，通过JVM的加载机制来实现线程安全和运行时加载。
		//private static SingletonClass INSTANCE = new SingletonClass();
		
	}
}
