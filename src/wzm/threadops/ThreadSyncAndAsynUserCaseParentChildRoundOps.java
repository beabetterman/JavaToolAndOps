package wzm.threadops;

import wzm.threadops.ThreadSyncAndAsynUserCaseParentChildRoundOps.Business;

/*
 * 情景案例：父进程执行10次，然后子进程执行20次。总共执行的轮数由参数决定。
 * 
 */
public class ThreadSyncAndAsynUserCaseParentChildRoundOps {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// Method 1.
//		nestedThread(20);
		
		// Method 2.
//		try {
//			individulaOps(20);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	
	
	/**
	 * Method 1
	 * 父进程执行10次，然后子进程执行20次。总共执行的轮数由参数决定。
	 * 可以实现运行结束后线程自动结束。
	 * @param roundNum 循环的次数
	 * @return
	 */
	public static void nestedThread(int roundNum) {
		final int round = roundNum;
		// 用于同步的标志变量。
		String flag = "";
		Thread parent_thread = new Thread() {
			@Override
			public void run() {
				
				Thread childThread = new Thread() {
					@Override
					public void run() {
						synchronized(flag) {
							for(int i=0; i<round; i++) {
								for(int j=0; j<20; j++) {
									System.out.println("In child thread, round:"+i+", index:"+j);
								}
								flag.notify();

								if(i == round-1) {
									break;
								}
								try {
									flag.wait();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				};
				synchronized(flag) {
					childThread.start();
					for(int i=0; i<round; i++) {
						for(int j=0; j<10; j++) {
							System.out.println("In main thread, round:"+i+", index:"+j);
						}
						flag.notify();

						if(i == round-1) {
							break;
						}
						
						try {
							flag.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		};
		
		parent_thread.start();
	}
	
	/*
	 * Method 2
	 * 这里需要注意的情况：结束后无法终止，
	 * 需要处理首次执行时 主线程和子线程的启动顺序。这里并不是按照代码写的顺序执行。因为子进程创建需要进行一些操作，会消耗一些时间。
	 */
	public static void individulaOps(int roundCount) throws InterruptedException {
		// Method from others
		Business b = new Business();
		int round = roundCount;
		new Thread() {
			public void run() {
				try {
					for(int i=0; i<round; i++) {
						System.out.print("Round :"+i+" ");
						b.subThread();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		
		synchronized(b) {
			for (int i = 0; i < round; i++) {
				System.out.print("Round :"+i+" ");
				b.mainThread();
				b.notifyAll();
				if( i == round-1) {
					System.out.println("No need wait.");
					continue;
				} else {
					System.out.println("Main Wait. round "+i);
					b.wait();
				}
				
			}
		}
	}
	
	static class Business {
		public void subThread() throws InterruptedException {
			synchronized(this) {
				for(int i=0; i<20; i++) {
					System.out.println("SubThread , index:" + i);
				}
				this.notifyAll();
				this.wait();
			}
		}
		
		public void mainThread() throws InterruptedException {
			
			for(int i=0; i<10; i++) {
				System.out.println("MainThread, index:" + i);
			}
		}
	}

}
