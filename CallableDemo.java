package cardGame;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CallableDemo {

	public static void main(String[] args) {
		ExecutorService es = Executors.newFixedThreadPool(3);
		Future<Integer> f1;
		Future<Double> f2;
		Future<Integer> f3;
		
		System.out.println("Ok...");
		f1 = es.submit(new Sum(10));
		f2 = es.submit(new Hypot(3, 4, 10));	// deliberately slowed down...
		f3 = es.submit(new Factorial(5));
			
		int i=0;
		try {
			while (!f2.isDone()) {
				boolean b1=false, b2=false;
				if (f1.isDone()) {
					System.out.println("sum:" + f1.get());
					b1 = true;
					}
				if (f2.isDone()) {
					System.out.println("Fact:" + f3.get());
					b2 = true;
					}
				if (b1 && b2) break;
				i++;
				}
			System.out.println("Iterations:" + i + " Slow...Hypot:" + f2.get());
			} catch (ExecutionException | InterruptedException exc) {
				System.out.println(exc);
			}
		es.shutdown();
		System.out.println("Done.");		
		} // main()
		
	}
	
	class Sum implements Callable<Integer> {
		int stop;
		Sum(int v) { stop = v; }
		public Integer call() {
			int sum = 0;
			for (int i=1; i<=stop; i++)
				sum += i;
			return sum;
			} // call		
		
		} // class Sum
	
	class Factorial implements Callable<Integer> {
		int stop;
		Factorial (int v) { stop = v; }
		public Integer call() {
			int fact = 1;
			for (int i=2; i<= stop; i++) {
				fact *= i;
				}
			return fact;
			}	// call()		
		} // class factorial
	
	class Hypot implements Callable<Double> {
		double side1, side2;
		int drag; 	//  time to drag your feet.
		Hypot (double s1, double s2, int d) { side1 = s1; side2 = s2; drag = d;}
		public Double call() {
			try {
				Thread.sleep(drag);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
			return Math.sqrt(side1*side1 + side2*side2);
		} // class Hypot
	

}
