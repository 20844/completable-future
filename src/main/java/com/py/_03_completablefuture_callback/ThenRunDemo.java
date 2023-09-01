package com.py._03_completablefuture_callback;

import com.py.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ThenRunDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //我们仅仅想知這敏感词汇的文件是否读取完成
        CommonUtils.printThreadLog("main start");
        CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterwords = CommonUtils.readFile("filter_words.txt");
            return filterwords;
        }).thenRun(()-> {
            CommonUtils.printThreadLog("读取filter_words完成");
        });

        CommonUtils.printThreadLog("main continue");
        CommonUtils.sleepSecond(3);
        CommonUtils.printThreadLog("main end");

        /**
         * 总结
         * thenRun(Runnable action）
         * 当异步任务完成后，只想得到一个完成的通知，不使用上一步异步任务的结果,就可以thenRun
         * 通过会把它用在链式操作的末端
         */

    }
}
