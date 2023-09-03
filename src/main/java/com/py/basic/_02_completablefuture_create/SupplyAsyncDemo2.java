package com.py.basic._02_completablefuture_create;

import com.py.basic.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SupplyAsyncDemo2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //指定线程池，开启异步任务读取 news.txt文件中的新闻稿，返回文件中内容并在主线程打印输出
        ExecutorService excutor = Executors.newFixedThreadPool(5);
        CommonUtils.printThreadLog("main start");
        CompletableFuture<String> newsFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("异步读取文件开始");
            String news = CommonUtils.readFile("news.txt");
            return news;
        },excutor);
        CommonUtils.printThreadLog("here are not blocked,mian continue");
        //阻塞并等待newsFuture完成
        String news = newsFuture.get();
        CommonUtils.printThreadLog("news: " + news);
        //关闭线程池
        excutor.shutdown();
        CommonUtils.printThreadLog("main end");
    }


}
