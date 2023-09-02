package com.py._05_completablefuture_exception;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ExceptionallyDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // exceptionally 处理回调链中的异常
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            // int r = 1 / 0;
            return "result1";
        }).thenApply(result -> {

            String str = null;
            int len = str.length();

            return result + " result2";
        }).thenApply(result -> {
            return result + " result3";
        }).exceptionally(ex -> {
            System.out.println("出现异常:" + ex.getMessage());
            return "UnKnown";
        });
    }
}
