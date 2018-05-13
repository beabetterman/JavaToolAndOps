package wzm.threadops;

import java.util.concurrent.CountDownLatch;

/*
 * 1. 死锁模拟。 多线程对多个资源进行加锁时，资源的申请顺序不同会导致死锁。
 * 2. 同步转异步。CountDownLatch 和 thread.join()的不同。
 * 3. Deamon设置。 当剩下的进程都是守护进程时，所有守护进程都会终止。。。
 *      thread.setDeamon(true) 要在thread.start()之前设置。
 * 
 */
public class ThreadSyncAndAsynOps {

    
    public static void main(String[] args) throws InterruptedException {
        
        
        //deadLockControl();
        
        // threadX.join()，只有threadX完全执行完后，主线程才能重新执行。
        joinControl();
        
        // CountDownLatch 可以更灵活的控制同步转异步的执行。在countDownLatch.countDown()为0后，主线程就可以执行。
        countDownLatchControl();
        
    }
    
    // 通过CountDownLatch来控制异步和同步执行。
    /**
     * CountDownLatch 可以更灵活的控制同步转异步的执行。
     * 情景：比如说线程threadA和threadB各自执行完某个步骤PeriodA, PeroidB后，通过countDownLatch.countDown()进行标记。在countDown到0的时候，就可以通知让主线程重新开始执行。
     *      此例子中PeroidA 和 PeroidB是同一个操作。。。
     */
    public static void countDownLatchControl() {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        System.out.println("Main thread is beginning. time:"+System.currentTimeMillis() / 1000);
        
        Runnable runnable = new Runnable(){

            @Override
            public void run() {
                
                System.out.println("Thread :" + Thread.currentThread().getName() +" is beginning. Time:" + System.currentTimeMillis() / 1000);
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("Thread :" + Thread.currentThread().getName() +" first Period is Done. Do the count down. Time:" + System.currentTimeMillis() / 1000);
                countDownLatch.countDown();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("Thread :" + Thread.currentThread().getName() +" is Done. Time:" + System.currentTimeMillis() / 1000);
            }
            
        };
        
        Thread threadA = new Thread(runnable);
        Thread threadB = new Thread(runnable);
        
        threadA.start();
        threadB.start();
        System.out.println("Main thread: All threads have been invoked started. Time:" + System.currentTimeMillis() / 1000);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Main thread: Now all threads have count down. Time:" + System.currentTimeMillis() / 1000);
        
    }
    
    
    /**
     * 通过join来控制异步和同步执行。
     * threadX.join()，只有threadX完全执行完后，主线程才能重新执行。
     */
    public static void joinControl() {
        System.out.println("Main thread is beginning. time:"+System.currentTimeMillis() / 1000);
        Runnable runnable = new Runnable(){

            @Override
            public void run() {
                System.out.println("Thread :" + Thread.currentThread().getName() +" is beginning. Time:" + System.currentTimeMillis() / 1000);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.out.println("Thread :" + Thread.currentThread().getName() +" is Done. Time:" + System.currentTimeMillis() / 1000);
            }
            
        };
        
        Thread threadA = new Thread(runnable);
        Thread threadB = new Thread(runnable);
        
        threadA.start();
        threadB.start();
        System.out.println("Main thread: All threads have been invoked started. Time:" + System.currentTimeMillis() / 1000);
        try {
            threadA.join();
            threadB.join();
            // Main thread will stuck, until threadA and threadB are finished. 
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Main thread: Now all threads have been done. Time:" + System.currentTimeMillis() / 1000);
        
    }
    
    
    /**
     * 模拟两个资源，两个线程的导致死锁的情况。
     * 两个线程申请的资源的顺序不同，导致了死锁。
     */
    public static void deadLockControl() {
        String resourceA = "resoueceA";
        String resourceB = "resouceB";
        deadLockThread(resourceA, resourceB);
        deadLockThread(resourceB, resourceA);
        
    }
    
    /**
     * 针对两个参数资源，进行同步操作。
     * 虽然每个进程的code的申请资源的顺序是一样的，但是参数的顺序是调用方决定的。调用方在申请资源时，如果顺序不一致就会导致死锁。
     */
    public static void deadLockThread(String resourceOfPositionA, String resourceOfPositionB) {
        Thread threadA = new Thread() {

            @Override
            public void run() {
                String threadName = Thread.currentThread().getName();
                synchronized(resourceOfPositionA) {
                    System.out.println(threadName + " got " + resourceOfPositionA +" lock.");
                    try {
                        // 方便演示，这样其他线程就可以先获取resourceB，从而导致死锁。
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    System.out.println(threadName + " try to get " + resourceOfPositionB +"  lock.");
                    synchronized(resourceOfPositionB) {
                        System.out.println(threadName + " get the " +  resourceOfPositionB + " lock.");
                    }
                }
            }
            
        };
        threadA.start();
    }
    
    
    
    
}
