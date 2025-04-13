import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool implements CustomExecutor {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;
    private final int minSpareThreads;
    private final ThreadFactory threadFactory;
    private final RejectedExecutionHandler rejectedHandler;
    private final List<Worker> workers = new ArrayList<>();
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
    private volatile boolean isShutdown = false;

    public ThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit,
                      int queueSize, int minSpareThreads, ThreadFactory threadFactory,
                      RejectedExecutionHandler rejectedHandler) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.minSpareThreads = minSpareThreads;
        this.threadFactory = threadFactory;
        this.rejectedHandler = rejectedHandler;
        initializeWorkers(corePoolSize);
    }

    private void initializeWorkers(int count) {
        for (int i = 0; i < count; i++) {
            Worker worker = new Worker(threadFactory, queueSize, keepAliveTime, timeUnit);
            workers.add(worker);
            worker.start();
        }
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) {
            rejectedHandler.rejectedExecution(command, null);
            System.out.println("[Pool] Task rejected due to shutdown: " + command.toString());
            return;
        }

        int index = roundRobinIndex.getAndIncrement() % workers.size();
        Worker worker = workers.get(index);
        if (!worker.addTask(command)) {
            rejectedHandler.rejectedExecution(command, null);
            System.out.println("[Pool] Task rejected due to full queue: " + command.toString());
        } else {
            System.out.println("[Pool] Task accepted into queue #" + index + ": " + command.toString());
        }

        // Проверка на необходимость создания резервных потоков
        int idleWorkers = countIdleWorkers();
        if (idleWorkers < minSpareThreads && workers.size() < maxPoolSize) {
            Worker newWorker = new Worker(threadFactory, queueSize, keepAliveTime, timeUnit);
            workers.add(newWorker);
            newWorker.start();
            System.out.println("[Pool] Added spare worker, total workers: " + workers.size());
        }
    }

    private int countIdleWorkers() {
        int idleCount = 0;
        for (Worker worker : workers) {
            if (worker.isIdle()) idleCount++;
        }
        return idleCount;
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        // Заглушка для submit, требует доработки для полной поддержки Future
        FutureTask<T> future = new FutureTask<>(callable);
        execute(future);
        return future;
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        System.out.println("[Pool] Initiating shutdown...");
        for (Worker worker : workers) {
            worker.stopGracefully();
        }
    }

    @Override
    public void shutdownNow() {
        isShutdown = true;
        System.out.println("[Pool] Initiating immediate shutdown...");
        for (Worker worker : workers) {
            worker.interrupt();
        }
    }
}