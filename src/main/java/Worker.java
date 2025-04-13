import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Worker extends Thread {
    private final BlockingQueue<Runnable> taskQueue;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private volatile boolean isStopped = false;

    public Worker(ThreadFactory threadFactory, int queueSize, long keepAliveTime, TimeUnit timeUnit) {
        super(threadFactory.newThread(null));
        this.taskQueue = new LinkedBlockingQueue<>(queueSize);
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
    }

    public boolean addTask(Runnable task) {
        if (isStopped) return false;
        return taskQueue.offer(task);
    }

    public boolean isIdle() {
        return taskQueue.isEmpty();
    }

    public void stopGracefully() {
        isStopped = true;
    }

    @Override
    public void run() {
        System.out.println("[Worker] " + getName() + " started.");
        while (!isStopped) {
            try {
                Runnable task = taskQueue.poll(keepAliveTime, timeUnit);
                if (task != null) {
                    System.out.println("[Worker] " + getName() + " executes " + task.toString());
                    task.run();
                } else {
                    System.out.println("[Worker] " + getName() + " idle timeout, stopping.");
                    isStopped = true;
                }
            } catch (InterruptedException e) {
                isStopped = true;
                System.out.println("[Worker] " + getName() + " interrupted.");
            }
        }
        System.out.println("[Worker] " + getName() + " terminated.");
    }
}