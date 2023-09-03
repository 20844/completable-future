package com.py.basic._03_completablefuture_callback;

import com.py.basic.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ThenApplyDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //需求：异步读取 filter_words.txt 文件中的内容，读取完成后，把内容转换成数组(敏感词数组），异步任务返回敏感词数组
        CommonUtils.printThreadLog("main start");
        CompletableFuture<String> readFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterwords = CommonUtils.readFile("filter_words.txt");
            return filterwords;
        });

        CompletableFuture<String[]> filterwordsFuture = readFuture.thenApply((content) -> {
            CommonUtils.printThreadLog("内容转换成数组(敏感词数组)");
            String[] filterwords = content.split(",");
            return filterwords;
        });

        CommonUtils.printThreadLog("main continue");
        String[] filterwords = filterwordsFuture.get();
        CommonUtils.printThreadLog("filterwords: " + Arrays.toString(filterwords));
        CommonUtils.printThreadLog("main end");

        /**
         * 总结
         * thenAppLy(Function<T，R >）可以对异步任务的结果进一步应用Function转换
         * 转换后的结果可以在主线程获取，也可以进行下一步的转换。
         */

    }
}
