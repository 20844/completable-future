package com.py.advanced._01_completablefuture_interaction;


import com.py.basic.utils.CommonUtils;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AcceptEitherDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 异步任务交互 acceptEither
        // 异步任务1
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            int x = new Random().nextInt(3);
            CommonUtils.sleepSecond(x);
            CommonUtils.printThreadLog("任务1耗时" + x + "秒");
            return x;
        });

        // 异步任务2
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            int y = new Random().nextInt(3);
            CommonUtils.sleepSecond(y);
            CommonUtils.printThreadLog("任务2耗时" + y + "秒");
            return y;
        });

        // 哪个异步任务先完成，就使用异步任务的结果
        ExecutorService executor = Executors.newFixedThreadPool(4);
        future1.acceptEitherAsync(future2,result -> {
            CommonUtils.printThreadLog("最先到达的结果:" + result);
        },executor);

        CommonUtils.sleepSecond(4);
        executor.shutdown();
    }
}
