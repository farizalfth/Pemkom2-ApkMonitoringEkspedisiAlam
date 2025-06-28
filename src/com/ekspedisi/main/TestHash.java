package com.ekspedisi.main;
import com.ekspedisi.util.PasswordUtil;

public class TestHash {
    public static void main(String[] args) {
        String passwordToTest = "admin123";
        String generatedHash = PasswordUtil.hashPassword(passwordToTest);
        System.out.println(generatedHash);
    }
}