package wzm.threadops;

import wzm.threadops.ThreadSyncAndAsynUserCaseFaxPrinterOps.Fax;
import wzm.threadops.ThreadSyncAndAsynUserCaseFaxPrinterOps.Printer;
import wzm.threadops.ThreadSyncAndAsynUserCaseFaxPrinterOps.User;

public class ThreadSyncAndAsynUserCaseFaxPrinterOps {

	public static void main(String[] args) {

		Fax f = new Fax("fax");
		Printer p = new Printer("printer");

		User user1 = new User("user1", f, p);
		Thread a = new Thread(user1);

		User user2 = new User("user2", f, p);
		Thread b = new Thread(user2);

		for (int i = 0; i < 10; i++) {
			Thread temp = new Thread(new User(String.valueOf(i)));
			temp.start();
		}

	}

	public static class User implements Runnable {

		String name;
		static Fax fax;
		static Printer printer;

		public User(String n) {
			name = n;
		}

		public User(String n, Fax f, Printer p) {
			name = n;
			fax = f;
			printer = p;
		}

		public boolean getFax() {
			synchronized (fax) {
				if (fax.isUsing) {
					try {
						fax.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				fax.setName(name);
				fax.setUse();
				System.out.println(name + " get the fax.");

			}
			return true;
		}

		public boolean getPrinter() {
			synchronized (printer) {
				if (printer.isUsing) {
					try {
						System.out.println(name + " is waiting for the printer.");
						printer.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				printer.setName(name);
				printer.setUse();
				System.out.println(name + " get the printer.");
				return true;
			}

		}

		@Override
		public void run() {

			// If we don't handle the printer.isUsing case, all thread will first get printer then get fax.
			if (printer.isUsing) {
				synchronized (fax) {
					if (getFax()) {
						System.out.println("This is log :" + name + " get the fax, but wait for printer");
					}
					
					synchronized (printer) {
						if (getPrinter()) {

							System.out.println("This is log :" + name + " is sending");
							fax.send("In thread :" + name);
							fax.setFree();
							fax.notify();

							System.out.println("This is log :" + name + " is printing");
							printer.print("In thread :" + name);
							printer.setFree();
							printer.notify();
						}

					}
				}
			}

			synchronized (printer) {
				if (getPrinter()) {
					try {
						
						System.out.println("This is log :" + name + " get the printer, will try to get fax.");
						// Let other thread do sth, to let other thread get the fax lock
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					synchronized (fax) {
						if (getFax()) {
							System.out.println("This is log :" + name + " is sending");
							fax.send("In thread :" + name);
							fax.setFree();
							fax.notify();

						}
					}
					System.out.println("This is log :" + name + " is printing");
					printer.print("In thread :" + name);
					printer.setFree();
					printer.notify();
				}
			}
		}

	}

	public static class Fax {
		public String name;
		private boolean isUsing = false;

		Fax(String caller) {
			name = caller;
		}

		public void setName(String n) {
			name = n;
		}

		public void send(String message) {
			System.out.println(name + " is sending :" + message);
		}

		public boolean isUsing() {
			return isUsing;
		}

		public void setFree() {
			if (isUsing) {
				isUsing = false;
			}
		}

		public void setUse() {
			if (!isUsing) {
				isUsing = true;
			}
		}

	}

	public static class Printer {
		public String name;
		public boolean isUsing = false;

		Printer(String caller) {
			name = caller;
		}

		public void setName(String n) {
			name = n;
		}

		public void print(String message) {
			System.out.println(name + " is printing : " + message);
		}

		public boolean isUsing() {
			return isUsing;
		}

		public void setFree() {
			if (isUsing) {
				isUsing = false;
			}
		}

		public void setUse() {
			if (!isUsing) {
				isUsing = true;
			}
		}
	}
}
