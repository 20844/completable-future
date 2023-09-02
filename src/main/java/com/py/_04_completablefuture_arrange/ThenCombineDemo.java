package com.py._04_completablefuture_arrange;


import com.py.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThenCombineDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需求: 替换新闻稿 ( news.txt ) 中敏感词汇 ，把敏感词汇替换成*，敏感词存储在 filter_words.txt 中
        CommonUtils.printThreadLog("main start");

        // step 1: 读取filter_words.txt文件内容，并解析成敏感词数组
        CompletableFuture<String[]> future1 = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            String[] filterWords = filterWordsContent.split(",");
            return filterWords;
        });

        // step 2: 读取news.txt 文件内容
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取news文件");
            String newsContent = CommonUtils.readFile("news.txt");
            return newsContent;
        });

        // step 3: 替换操作
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CompletableFuture<String> combineFuture = future1.thenCombineAsync(future2, (filterWords, newsContent) -> {
            CommonUtils.printThreadLog("替换操作");
            for (String word : filterWords) {
                if (newsContent.indexOf(word) >= 0) {
                    newsContent = newsContent.replace(word, "**");
                }
            }
            return newsContent;
        },executor);

        CommonUtils.printThreadLog("main continue");
        String news = combineFuture.get();
        CommonUtils.printThreadLog("news = " + news);

        CommonUtils.printThreadLog("main end");

        /**
         * thenCombine 用于合并2个没有依赖关系的异步任务
         */
    }
}
