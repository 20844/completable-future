package com.py._01_future_vs_completablefuture;

import com.py.utils.CommonUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        // step 1: 读取敏感词汇 => thread1
        Future<String[]> filterWordFuture = executor.submit(() -> {
            String content = CommonUtils.readFile("filter_words.txt");
            String[] filterWords = content.split(",");
            return filterWords;
        });
        // step 2: 读取新闻稿 => thread2
        Future<String> newsFuture = executor.submit(() -> {
            String content = CommonUtils.readFile("news.txt");
            return content;
        });
        // step 3: 替换操作 => thread3
        Future<String> replaceFuture = executor.submit(() -> {
            String[] words = filterWordFuture.get();
            String news = newsFuture.get();

            for (String word : words) {
                if (news.indexOf(word) >= 0){
                    news = news.replace(word,"**");
                }
            }

            return news;
        });
        // step 4: 打印输出替换后的新闻稿 => main
        String result = replaceFuture.get();
        System.out.println("过滤后的新闻稿："+result);


        /***
         * 通过上面的代码，我们会发现，Future相比于所有任务都直接在主线程处理，有很多优势，但同时也存在不足， 至少表现如下：
         *
         * 在没有阻塞的情况下，无法对Future的结果执行进一步的操作。Future不会告知你它什么时候完成，你如果想要得到结果，必须通过一个get()方法，该方法会阻塞直到结果可用为止。它不具备将回调函数附加到 Future后并在Future的结果可用时自动调用回调的能力。
         *
         * 无法解决任务相互依赖的问题。filterWordFuture和newsFuture的结果不能自动发送给replaceFuture，需要在replaceFutur心中手动获取，所以使用Future不能轻而易举地创建异步工作流。
         *
         * 不能将多个Future合并在一起。假设你有多种不同的Future，你想在它们全部并行完成后然信再运行某个函数，Future很难独立完成这一需要。
         *
         * 没有异常处理。Future提供的方法中没有专门的API应对异常处理，还是需要开发者自己手动异常处理。
         */
    }
}
