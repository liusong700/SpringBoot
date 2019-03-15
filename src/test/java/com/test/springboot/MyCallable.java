package com.test.springboot;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class MyCallable implements Callable<String> {

    private int i = 0;

    @Override
    public String call() throws Exception {
        int sum = 0;
        for (; i < 5; i++) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sum += i;
        }
        return String.valueOf(sum);
    }

    public static void main(String[] args) {
        Callable<String> myCallable = new MyCallable();
        FutureTask<String> ft = new FutureTask<>(myCallable);
        Thread thread = new Thread(ft);
        thread.start();
        System.out.println("主线程for循环执行完毕..");
        try {
            String sum = ft.get();            //取得新创建的新线程中的call()方法返回的结果
            System.out.println("sum = " + sum);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

}
