package wzm.threadops;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/*
 * Two method to get return value of the Runnable
 * 	1. 利用ExecutorService的submit方法去启动call方法自执行任务，而ExecutorService的submit又返回一个Future类型的结果，因此Callable通常也与Future一起使用
 * 	2. 利用FutureTask封装Callable再由Thread去启动（少用）
 * 
 * 
 */
public class ThreadReturnValueOps {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ExecutorService executor = Executors.newFixedThreadPool(3);
		Future<String> f = executor.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				// TODO Auto-generated method stub
				System.out.println("This is called by executor.submit(new Callable()).");
				return "This is return value from Future.";
			}
		});
		
		FutureTask<String> ft = new FutureTask<String>(new Callable<String>() {

			@Override
			public String call() throws Exception {
				// TODO Auto-generated method stub
				System.out.println("This is called by new Thread(new FutureTask) mode.");
				return "This is return value in FutureTask mode";
			}
			
		});
		
		
		
		
		// Future task mode call.
		//new Thread(ft).start();
		executor.submit(ft);
		
		try {
			//Thread.sleep(1000);
			// Output the returned value.
			System.out.println(ft.get());
		} catch (InterruptedException | ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		// Get the return value of executor.submit(new Callable()...) 
		try {
			System.out.println(f.get());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				// Give some time to let the thread work out the task.
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("before shutdown");
			// This will shutdown the thread pool immediately
			executor.shutdown();
		}
		
		// Use CompletionService get the result
		checkFutureResult();
	}
	
	public static void checkFutureResult() {
		
		ExecutorService threadPool = Executors.newCachedThreadPool();
        CompletionService<Integer> cs = new ExecutorCompletionService<Integer>(threadPool);
        
        for(int i = 1; i < 5; i++) {
            final int taskID = i;
            cs.submit(new Callable<Integer>() {
                public Integer call() throws Exception {
                    return taskID;
                }
            });
        }
        // 可能做一些事情
        for(int i = 1; i < 5; i++) {
            try {
                System.out.println(cs.take().get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        
	}

}
