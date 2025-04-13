import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        CustomThreadFactory threadFactory = new CustomThreadFactory("MyPool");
        CustomRejectedHandler rejectedHandler = new CustomRejectedHandler();
        ThreadPool pool = new ThreadPool(2, 4, 5, TimeUnit.SECONDS, 5, 1, threadFactory, rejectedHandler);

        for (int i = 0; i < 15; i++) {
            final int taskId = i;
            pool.execute(() -> {
                logger.info("Starting task {} in {}", taskId, Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                logger.info("Finished task {}", taskId);
            });
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pool.shutdown();
        logger.info("Pool shutdown initiated.");
    }
}