package com.py._04_completablefuture_arrange;

import com.py.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ThenComposeDemo {
    public static CompletableFuture<String> readFileFuture(String fileName){
        return CompletableFuture.supplyAsync(()-> {
            String filterWordsContent = CommonUtils.readFile(fileName);
            return filterWordsContent;
        });
    }

    public static CompletableFuture<String[]> splitFuture(String content){
        return CompletableFuture.supplyAsync(()-> {
            String[] filterWords = content.split(",");
            return filterWords;
        });
    }


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //编排2个依赖关系的异步任务ThenCompose()

        //使用thenApply
        CompletableFuture<CompletableFuture<String[]>> f = readFileFuture("filter_words.txt").thenApply(content -> {
            return splitFuture(content);
        });
        String[] result = f.get().get();
        CommonUtils.printThreadLog("result: " + Arrays.toString(result));


        //使用thenCompose
        CompletableFuture<String[]> f2 = readFileFuture("filter_words.txt").thenCompose(content -> {
            return splitFuture(content);
        });
        String[] result2 = f2.get();
        CommonUtils.printThreadLog("result2: " + Arrays.toString(result2));

        /**
         * thenApply(Function<T,R>)
         * 重心在于对上一步异步任务的结果下进行应用转换，经Function回调转换后的结果R是一个简单的值
         *
         * thenCompose(Function<T, CompletableFuture<R>)
         * 重心在于对上一步异步任务的结果T进行应用，经Function回调转换后的结果是一个CompletableFuture对象
         *
         * 结论：
         * 编排两个依赖关系的异步任务(CompletableFuture对象），请使用 thenCompose() 方法
         */
    }
}
