package buffer_bci.javaserver;

public class MemoryMonitor extends Thread {
	private final Runtime runtime;

	public MemoryMonitor() {
		runtime = Runtime.getRuntime();
	}

	private double Memory() {
		return (runtime.totalMemory() - runtime.freeMemory())
				/ (1024.0 * 1024.0);
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();
		long time = start;

		while (true) {
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			time = (System.currentTimeMillis() - start) / 1000;

			System.out.println(time + "\t" + Memory());

			if (time > 5 * 60) {
				System.exit(0);
			}
		}
	}
}