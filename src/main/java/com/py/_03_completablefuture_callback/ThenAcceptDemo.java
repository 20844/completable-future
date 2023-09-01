package com.py._03_completablefuture_callback;

import com.py.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ThenAcceptDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //需求：异步读取 filter_words.txt 文件中的内容，读取完成后，把内容转换成数组(敏感词数组），然后打印敏感词数组
        CommonUtils.printThreadLog("main start");
        CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterwords = CommonUtils.readFile("filter_words.txt");
            return filterwords;
        }).thenApply((content) -> {
            CommonUtils.printThreadLog("内容转换成数组(敏感词数组)");
            String[] filterwords = content.split(",");
            return filterwords;
        }).thenAccept((content)->{
            CommonUtils.printThreadLog("filterwords: " + Arrays.toString(content));
        });

        CommonUtils.printThreadLog("main continue");
        CommonUtils.sleepSecond(3);
        CommonUtils.printThreadLog("main end");

        /**
         * 总结
         * thenAccept(Consumer<T>）可以对异步任务的结果进行消费使用，
         * 方法返回一个不带返回值结果的CompletableFuture对象
         * 一般最后一步使用
         */

    }
}
