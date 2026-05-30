package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadPool {
    // list of tasks to be supported
    private LinkedBlockingQueue<FutureTask<?>> taskQueue;
    private int poolSize;
    private List<Thread> threads;
    private volatile boolean shutdown = false;

    public ThreadPool(int poolSize) {
        this.poolSize = poolSize;
        taskQueue = new LinkedBlockingQueue<>(poolSize);
        threads = new ArrayList<>();
        for (int i = 0 ; i < poolSize ; i++) {
            Worker worker = new Worker();
            Thread thread = new Thread(worker);
            threads.add(thread);
            thread.run();
        }
    }

    public FutureTask<?> submitTask (Callable<?> callable) throws InterruptedException {
        if (shutdown) throw new IllegalStateException();
        // wrap it in a FutureTask
        FutureTask<?> futureTask = new FutureTask<>(callable);
        if (taskQueue.offer(futureTask, 30, TimeUnit.MILLISECONDS)) {
            return futureTask;
        }
        throw new RuntimeException();
    }

    public void shutdown() {

        shutdown = true;

        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    // Runnable is just a unit of work = WHAT to execute
    // It is executed on a thread.

    class Worker implements Runnable {
        @Override
        public void run(){
            while (true) {
                if (shutdown && taskQueue.isEmpty()) {
                    break;
                }
                try {
                    FutureTask<?> futureTask = taskQueue.poll(30, TimeUnit.MILLISECONDS);
                    futureTask.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
