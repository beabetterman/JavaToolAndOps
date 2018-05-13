package wzm.threadops;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 * 1. 自定义线程池处理。 ThreadPoolExecutor
 * 2. 线程时间调度处理。 ScheduledExecutorService.避免使用Timer的原因：Timer运行多个TimeTask时，只要其中之一没有捕获抛出的异常，其它任务便会自动终止运行
 * 3. 获取单例对象需要保证线程安全，其中的方法也要保证线程安全。 资源驱动类、工具类、单例工厂类
 * 4. SimpleDateFormat的处理： 使用private static final ThreadLocal<SimpleDateFormat> df = new ...
 * 		JDK8的应用，可以使用Instant代替Date，LocalDateTime代替Calendar，DateTimeFormatter代替SimpleDateFormat，官方给出的解释：simple beautiful strong immutable thread-safe。
 * 5. 锁的使用，高并发时，尽量使用无锁数据结构。尽可能使加锁的代码块工作量尽可能的小，避免在锁代码块中调用RPC方法。
 * 6. 避免死锁：对多个资源、数据库表、对象同时加锁时，需要保持一致的加锁顺序，否则可能会造成死锁。 说明：线程一需要对表A、B、C依次全部加锁后才可以进行更新操作，那么线程二的加锁顺序也必须是A、B、C，否则可能出现死锁。
 * 7. 并发修改同一记录时，避免更新丢失，需要加锁。要么在应用层加锁，要么在缓存加锁，要么在数据库层使用乐观锁，使用version作为更新依据。
 * 		如果每次访问冲突概率小于20%，推荐使用乐观锁，否则使用悲观锁。乐观锁的重试次数不得小于3次
 * 8. 双重检查的问题：解决方法是在双重检查基础上，对实例加上volatile修饰符。（静态内部类也是一种解决方法） 具体说明：https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
 * 9. 使用CountDownLatch进行异步转同步操作，每个线程退出前必须调用countDown方法，线程执行代码注意catch异常，确保countDown方法被执行到，避免主线程无法执行至await方法，直到超时才返回结果。 
 * 		说明：注意，子线程抛出异常堆栈，不能在主线程try-catch到。
 * 10. 避免Random实例被多线程使用，虽然共享该实例是线程安全的，但会因竞争同一seed 导致的性能下降。
 * 		说明：Random实例包括java.util.Random 的实例或者 Math.random()的方式。 正例：在JDK7之后，可以直接使用API ThreadLocalRandom，而在 JDK7之前，需要编码保证每个线程持有一个实例。
 * 11. volatile解决多线程内存不可见问题。对于一写多读，是可以解决变量同步问题，但是如果多写，同样无法解决线程安全问题。
 * 		多写的情况：如果是count++操作，使用如下类实现：
 * 		AtomicInteger count = new AtomicInteger(); count.addAndGet(1); 如果是JDK8，推荐使用LongAdder对象，比AtomicLong性能更好（减少乐观锁的重试次数）。
 * 		在Java语言中，++i和i++操作并不是线程安全的，在使用的时候，不可避免的会用到synchronized关键字。而AtomicInteger则通过一种线程安全的加减操作接口。
 * 12. HashMap在容量不够进行resize时由于高并发可能出现死链，导致CPU飙升，在开发过程中可以使用其它数据结构或加锁来规避此风险。
 * 13. 对ThreadLocal的理解：ThreadLocal无法解决共享对象的更新问题，ThreadLocal对象建议使用static修饰。（弱引用的原因）
 * 
 * 其他线程操作代码实例，在本目录下的ThreadXXXOps.java中
 * 
 */
public class ThreadPoolOps {
	
	
	
	public static ThreadPoolExecutor tpe = new ThreadPoolExecutor(5, 10, 20, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10) );
	
	public static void main(String[] args) {
		
		// 优先使用ScheduledExecutorServicer 而不是Timer
		ScheduledExecutorService  sec = Executors.newScheduledThreadPool(3);
		//scheduleTest(sec);
		
		// User customer ThreadFactory and RejectedExecutionHandler. Also set the queue capacity!
		ThreadPoolExecutor myTPE = new ThreadPoolExecutor(5, 10, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10), myThreadFactory, myRejectedExecutionHandler );
		myTPE.allowCoreThreadTimeOut(true);
		customerThreadPoolTest(myTPE);
		
		// 需要设置合理的结束标志，然后进行相应的处理。 合理配置使用场景。强制退出的shutdownNow()的处理。。。
		// TODO 这里的场景设置并不一定合理，有时候queue的大小为0后，可能依然会有新的请求进来。具体情景具体分析。
		if (myTPE.getQueue().size() == 0) {
			myTPE.shutdown(); // 不再接受新的task，执行完当前的task列表后退出。
		}
		
	}
	
	// 测试自定义的ThreadPoolExecutor实例。 自定义ThreadFactory、queue容量以及 RejectedExecutorHandler
	public static void customerThreadPoolTest(ThreadPoolExecutor myTPE) {
		System.out.println("Begin time:"+System.currentTimeMillis() / 1000);
		for (int i=0; i<55; i++) {
			final int indexFlag = i;
			myTPE.execute(new Runnable() {

				@Override
				public void run() {
					System.out.println("Name:"+Thread.currentThread().getName() + ", index:"+indexFlag+", time:"+System.currentTimeMillis() / 1000+", poolSize:" +myTPE.getPoolSize());
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			});
		}
		System.out.println("After time:"+System.currentTimeMillis() / 1000);
		// Check the pool size
		for (int i=0; i<41; i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Active:Pool:Core:"+myTPE.getActiveCount()+","
				+myTPE.getPoolSize()+","+myTPE.getCorePoolSize() +", Time:" + System.currentTimeMillis() / 1000);
		}
	}
	
	
	// 测试ScheduledExecutorService，分别确认三种执行模式的不同。
	public static void scheduleTest(ScheduledExecutorService  sec) {
//		schedule(sec);
		scheduledFixedRate(sec);
//		scheduledFixdDecay(sec);
	}
	
	// 下面是针对ScheduledExecutorService的三种调用方式
	/*
	 * 指定时间 后执行只一次操作。
	 */
	public static void schedule(ScheduledExecutorService mScheduledExecutorService) {
		mScheduledExecutorService.schedule(new Runnable(){
			@Override
			public void run() {
				System.out.println("method : schedule");
				
			}}, 1, TimeUnit.SECONDS);
	}
	
	/*
	 * 两个Thread的间隔：Max(前一个执行的结束时间点, period的设置值)
	 */
	public static void scheduledFixedRate(ScheduledExecutorService mScheduledExecutorService) {
		mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("FixedRate,"+Thread.currentThread().getName() + ", Time:" + System.currentTimeMillis() / 1000);
                
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
	}
	
	/*
	 * 两个Thread的间隔：前一个的执行结束时间点 + decay的设置值
	 */
	public static void scheduledFixdDecay(ScheduledExecutorService mScheduledExecutorService) {
		mScheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                System.out.println("FixedDecay,"+Thread.currentThread().getName() + ", Time:" + System.currentTimeMillis() / 1000);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 1, 5, TimeUnit.SECONDS);
	}
	
	/*
	 * Customer ThreadFactory.
	 */
	private static ThreadFactory myThreadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable r) {
            return new Thread(r) {
                @Override
                public void run() {
                	// We could do some pre work here.
                    r.run();
                    // But we can't do post work here.(won't work)
                }
            };
        }
    };
    
    /*
	 * Customer RejectedExecutionHandler.
	 */
    private static RejectedExecutionHandler myRejectedExecutionHandler = new RejectedExecutionHandler() {
		
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			// Output the error log.
			System.out.println("Log for the out of capacity error.");
			
			// Or just put the Runnable r into executor in blocking mode as below.
			try {
				executor.getQueue().put(r);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
}
