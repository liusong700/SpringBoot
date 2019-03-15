package com.test.springboot;

public class TestThread extends Thread {

    @Override
    public void run() {
        try {
            sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("独立线程2");
    }

}
