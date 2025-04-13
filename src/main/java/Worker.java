import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Worker extends Thread {
    private static final Logger logger = LogManager.getLogger(Worker.class);
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
        logger.info("{} started.", getName());
        while (!isStopped) {
            try {
                Runnable task = taskQueue.poll(keepAliveTime, timeUnit);
                if (task != null) {
                    logger.info("{} executes {}", getName(), task);
                    task.run();
                } else {
                    logger.info("{} idle timeout, stopping.", getName());
                    isStopped = true;
                }
            } catch (InterruptedException e) {
                isStopped = true;
                logger.warn("{} interrupted.", getName());
            }
        }
        logger.info("{} terminated.", getName());
    }
}