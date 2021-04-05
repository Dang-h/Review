package threadLearn;

/**
 * @version 1.0
 * @Description: TODO
 * @Date 2020/8/30 18:43
 **/

public class ThreadTest{
	public static void main(String[] args) {
		MyThread myThread = new MyThread();

		//启动当前线程；调用当前线程得run()
		myThread.start();
	}
}

class MyThread extends Thread{

	//线程执行得操作放在run()中
	@Override
	public void run() {
		for (int i = 0; i < 100; i++) {
			if (i % 2 == 0) {
				System.out.println(i);
			}
		}
	}
}
