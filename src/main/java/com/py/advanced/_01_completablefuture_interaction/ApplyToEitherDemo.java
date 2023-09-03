package com.py.advanced._01_completablefuture_interaction;


import com.py.basic.utils.CommonUtils;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ApplyToEitherDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 异步任务交互

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

        // 哪个异步任务结果先到达，使用哪个异步任务的结果
        CompletableFuture<Integer> future = future1.applyToEither(future2, result -> {
            CommonUtils.printThreadLog("最先到达的结果：" + result);
            return result;
        });

        CommonUtils.sleepSecond(4);

        Integer ret = future.get();
        CommonUtils.printThreadLog("ret = " + ret);

        /**
         * 异步任务交互指两个异步任务，哪个结果先到，就使用哪个结果( 先到先用 )
         */
    }
}
