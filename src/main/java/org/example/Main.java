package org.example;

import java.util.concurrent.Callable;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException {
            ThreadPool threadPool = new ThreadPool(5);
            threadPool.submitTask(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    Thread.sleep(10000);
                    return true;
                }
            });

    }
}