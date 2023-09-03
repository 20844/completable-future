package com.py.basic._03_completablefuture_callback;

import com.py.basic.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ThenApplyAsyncDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //需求：异步读取 filter_words.txt 文件中的内容，读取完成后，把内容转换成数组(敏感词数组），主线程获取异步任务结果，然后打印敏感词数组
        CommonUtils.printThreadLog("main start");
        CompletableFuture<String[]> filterwordsFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            // String filterwords = CommonUtils.readFile("filter_words.txt");
            // return filterwords;
            return "尼玛,NB,tmd,gun";
        }).thenApply((content) -> {
            CommonUtils.printThreadLog("内容转换成数组(敏感词数组)");
            String[] filterwords = content.split(",");
            return filterwords;
        });

        CommonUtils.printThreadLog("main continue");
        String[] filterwords = filterwordsFuture.get();
        CommonUtils.printThreadLog("filterwords: " + Arrays.toString(filterwords));
        CommonUtils.printThreadLog("main end");
    }
    /**
     * 总结：
     * 一般而言，commonPool为了提高性能
     * thenApply中回调任务和suppLyAsync中的异步任务使用的是同一个线程
     * 特殊情况：了解即可
     * 如果suppLyAsync中的在务是立即返回结果(不是耗时的任务)，thnApply回调任务也会在主线程执行。
     */
}
