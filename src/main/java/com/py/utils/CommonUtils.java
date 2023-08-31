package com.py.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/***
 * 异步任务辅助工具类
 * @author py
 */
public class CommonUtils {

    /**
     * 读取指定路径的文件
     * @param pathToFile
     * @return
     */
    public static String readFile(String pathToFile){
        return readLineByLineJava8(pathToFile);
    }

    private static String readLineByLineJava8(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }


    //休眠指定的毫秒数
    public static void sleepMillis(long millis){
        try {
            TimeUnit.MICROSECONDS.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //休眠指定的秒数
    public static void sleepSecond(long seconds){
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    //打印输出带线程信息的日志
    public static void printThreadLog(String message){
        // 时间戳 | 线程id | 线程名 | 日志信息
        String result = new StringJoiner(" | ")
                .add(String.valueOf(System.currentTimeMillis()))
                .add(String.format("%2d",Thread.currentThread().getId()))
                .add(Thread.currentThread().getName())
                .add(message)
                .toString();
        System.out.println(result);
    }

}
