package com.test.springboot;

import lombok.Data;

@Data
public class Account {

    private String accountNo;
    private double balance;

    public void test() {
        System.out.println("accountNo:" + accountNo + ",balance:" + balance);
    }

}
