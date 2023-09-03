package com.py.basic._02_completablefuture_create;

import com.py.basic.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SupplyAsyncDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //开启异步任务读取 news.txt文件中的新闻稿，返回文件中内容并在主线程打印输出
        CommonUtils.printThreadLog("main start");
        CompletableFuture<String> newsFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("异步读取文件开始");
            String news = CommonUtils.readFile("news.txt");
            return news;
        });
        CommonUtils.printThreadLog("here are not blocked,mian continue");
        //阻塞并等待newsFuture完成
        String news = newsFuture.get();
        CommonUtils.printThreadLog("news: " + news);
        CommonUtils.printThreadLog("main end");
    }

    /**
     * 疑问：get方法会阻塞，会不会影响程序性能？
     */
}
