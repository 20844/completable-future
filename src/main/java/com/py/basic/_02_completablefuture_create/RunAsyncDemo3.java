package com.py.basic._02_completablefuture_create;

import com.py.basic.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;

public class RunAsyncDemo3 {
    public static void main(String[] args) {
        //需求：使用多线程异步读取 news.txt打印输出。
        CommonUtils.printThreadLog("main start");

        CompletableFuture.runAsync(()->{
            CommonUtils.printThreadLog("读取文件开始");
                //使用睡眠来模拟一个长时间的工作任务（例如读取文件，网络请求等）
            String content = CommonUtils.readFile("news.txt");
            CommonUtils.printThreadLog(content);
        });
        CommonUtils.printThreadLog("here are not blocked,mian continue");
        CommonUtils.sleepSecond(4);
        CommonUtils.printThreadLog("main end");
    }

    /**
     * 疑问：异步任务是并发执行还是并行执行？
     * 如果是单核CPU，那么异步任务之间就是并发执行；如果是多核CPU(多CPU)异步任务就是并行执行
     * 重点(敲黑板）
     * 作为开发者，我们只需要清楚如何开启异步任务，CPU 硬件会把异步任务合理的分配给CPU上的核运行。
     */
}
