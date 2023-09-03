package com.py.basic._04_completablefuture_arrange;

import com.py.basic.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThenComposeDemo2 {


    public static CompletableFuture<String[]> splitFuture(String content){
        return CompletableFuture.supplyAsync(()-> {
            String[] filterWords = content.split(",");
            return filterWords;
        });
    }


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //需求：异步读取 filter_words.txt 文件中的内容，读取完成后，把内容转换成数组(敏感词数组），主线程获取异步任务结果，然后打印敏感词数组
        ExecutorService executor = Executors.newFixedThreadPool(6);
        CompletableFuture<String[]> filterwordsFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
        },executor).thenCompose(content -> CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("转敏感词数组");
            String[] words = content.split(",");
            return words;
        },executor));

        CommonUtils.printThreadLog("main continue");
        String[] filterwords = filterwordsFuture.get();
        CommonUtils.printThreadLog("filterwords: " + Arrays.toString(filterwords));
        CommonUtils.printThreadLog("main end");
    }
}
