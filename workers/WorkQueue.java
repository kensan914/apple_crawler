package apple.workers;
import java.util.LinkedList;

public class WorkQueue {
	private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;

    public WorkQueue(int nThreads)
    {
        this.nThreads = nThreads;
        this.queue = new LinkedList<Runnable>();
        this.threads = new PoolWorker[this.nThreads];
        for (PoolWorker thread: this.threads) {
        	thread = new PoolWorker();
        	thread.start();
        }
    }

    public void execute(Runnable r) {
        synchronized(this.queue) {
        	this.queue.addLast(r);
        	this.queue.notifyAll();
        }
    }

    private class PoolWorker extends Thread {
        public void run() {
            Runnable r;
            while (true) {
                synchronized(queue) {
                    while (queue.isEmpty()) {
                        try
                        {
                            queue.wait();
                        }
                        catch (InterruptedException e)
                        {
                        	e.printStackTrace();
                        }
                    }
                    r = (Runnable) queue.removeFirst();
                }
                try {
                    r.run();
                }
                catch (RuntimeException e) {
                	e.printStackTrace();
                }
            }
        }
    }
}
