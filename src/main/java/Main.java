import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        CustomThreadFactory threadFactory = new CustomThreadFactory("MyPool");
        CustomRejectedHandler rejectedHandler = new CustomRejectedHandler();
        ThreadPool pool = new ThreadPool(2, 4, 5, TimeUnit.SECONDS, 5, 1, threadFactory, rejectedHandler);

        // Отправка задач
        for (int i = 0; i < 15; i++) {
            final int taskId = i;
            pool.execute(() -> {
                System.out.println("[Task] Starting task " + taskId + " in " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("[Task] Finished task " + taskId);
            });
        }

        // Ожидание и завершение
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pool.shutdown();
        System.out.println("[Main] Pool shutdown initiated.");
    }
}