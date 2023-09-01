package com.py._03_completablefuture_callback;

import com.py.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThenApplyAsyncDemo2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //需求：异步读取 filter_words.txt 文件中的内容，读取完成后，把内容转换成数组(敏感词数组），主线程获取异步任务结果，然后打印敏感词数组
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CommonUtils.printThreadLog("main start");
        CompletableFuture<String[]> filterwordsFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterwords = CommonUtils.readFile("filter_words.txt");
            return filterwords;
        },executor).thenApplyAsync((content) -> {
            CommonUtils.printThreadLog("内容转换成数组(敏感词数组)");
            String[] filterwords = content.split(",");
            return filterwords;
        },executor);

        CommonUtils.printThreadLog("main continue");
        String[] filterwords = filterwordsFuture.get();
        CommonUtils.printThreadLog("filterwords: " + Arrays.toString(filterwords));
        executor.shutdown();
        CommonUtils.printThreadLog("main end");
    }

}
