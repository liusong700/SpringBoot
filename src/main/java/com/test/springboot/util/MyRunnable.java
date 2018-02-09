package com.test.springboot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MyRunnable.class);

    private String regNo;

    public MyRunnable(String regNo) {
        this.regNo = regNo;
    }

    @Override
    public void run() {
        logger.info("regNo:{}", regNo);
    }

}
