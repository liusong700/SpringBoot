package com.test.springboot;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyRunnable implements Runnable {

    private Account account;
    private double drawAmount;

    private final Lock lock = new ReentrantLock();

    public MyRunnable(Account account, double drawAmount) {
        super();
        this.account = account;
        this.drawAmount = drawAmount;
    }

    @Override
    public void run() {
        boolean tryLock = lock.tryLock();
        System.out.println(tryLock);
        if (tryLock) {
            try {
                wait(1000);
                if (account.getBalance() >= drawAmount) {
                    System.out.println("取钱成功， 取出钱数为：" + drawAmount);
                    double balance = account.getBalance() - drawAmount;
                    account.setBalance(balance);
                    System.out.println("余额为：" + balance);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
    }

}
