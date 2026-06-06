package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadPool {

    private final BlockingQueue<FutureTask<?>> taskQueue;
    private final List<Thread> workers;

    private volatile boolean shutdown;

    public ThreadPool(int poolSize, int queueSize) {
        this.taskQueue = new LinkedBlockingQueue<>(queueSize);
        this.workers = new ArrayList<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
            Thread worker = new Thread(new Worker(), "worker-" + i);
            workers.add(worker);
            worker.start(); // FIX: start(), not run()
        }
    }

    public <T> Future<T> submit(Callable<T> callable)
            throws InterruptedException {

        if (shutdown) {
            throw new IllegalStateException("ThreadPool is shut down");
        }

        FutureTask<T> task = new FutureTask<>(callable);

        if (!taskQueue.offer(task, 30, TimeUnit.MILLISECONDS)) {
            throw new RejectedExecutionException("Queue is full");
        }

        return task;
    }

    public void shutdown() {
        shutdown = true;

        // Wake up workers blocked on take()
        for (Thread worker : workers) {
            worker.interrupt();
        }
    }

    public void awaitTermination() throws InterruptedException {
        for (Thread worker : workers) {
            worker.join();
        }
    }

    private class Worker implements Runnable {

        @Override
        public void run() {

            while (true) {

                // Graceful shutdown:
                // exit only after shutdown requested
                // and all queued tasks are processed
                if (shutdown && taskQueue.isEmpty()) {
                    break;
                }

                try {
                    FutureTask<?> task =
                            taskQueue.poll(500, TimeUnit.MILLISECONDS);

                    if (task != null) {
                        task.run();
                    }

                } catch (InterruptedException e) {

                    // During shutdown we expect interrupts
                    if (shutdown) {
                        continue;
                    }

                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
