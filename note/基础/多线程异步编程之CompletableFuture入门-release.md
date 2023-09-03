# CompletableFuture入门

## 学习内容

- 为什么会选择 CompletableFuture 
- 创建CompletableFuture异步任务
- CompletableFuture异步任务回调 
- CompletableFuture异步任务编排
- CompletableFuture的异常处理

## 学习目标

- 了解 CompletableFuture 的优点
- 掌握创建异步任务
  - 创建异步任务的2种方式
  - 知道异步任务中线程池的作用
  - 理解异步编程思想
- 掌握异步任务回调
  - thenApply / thenAccept / thenRun 3类方法使用和区别
  - 解锁一系列Async版本回调（thenXxxAsync） 
- 掌握异步任务编排
  - 会对2个异步任务的依赖关系、并行关系进行编排
  - 会对n个任务的合并进行编排 
- 掌握异步任务的异常处理
  - 会对异步任务进行异常处理
  - 会对回调链上对单个异步任务的异常进行现场恢复

## 课程学习说明

- 熟悉多线程理论知识
- 接触过 Future 和 线程池 的经历 
- 会使用Lambda表达式和 Stream-API



## 1、Future vs CompletableFuture

### 1.1 准备工作

为了便于后续更好地调试和学习，我们需要定义一个工具类辅助我们对知识的理解。

```java
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class CommonUtils {

    public static String readFile(String pathToFile) {
        try {
            return Files.readString(Paths.get(pathToFile));
        } catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void sleepMillis(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleepSecond(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void printThreadLog(String message) {
        String result = new StringJoiner(" | ")
                .add(String.valueOf(System.currentTimeMillis()))
                .add(String.format("%2d",Thread.currentThread().getId()))
                .add(String.valueOf(Thread.currentThread().getName()))
                .add(message)
                .toString();
        System.out.println(result);
    }
}

```

### 1.2 Future 的局限性

需求：替换新闻稿 ( news.txt ) 中敏感词汇 ，把敏感词汇替换成*，敏感词存储在 filter_words.txt 中

```java
public class FutureDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(5);
        // step 1: 读取敏感词汇
        Future<String[]> filterWordFuture = executor.submit(() -> {
            String str = CommonUtils.readFile("filter_words.txt");
            String[] filterWords = str.split(",");
            return filterWords;
        });

        // step 2: 读取新闻稿文件内容
        Future<String> newsFuture = executor.submit(() -> {
            return CommonUtils.readFile("news.txt");
        });

        // step 3: 替换操作(当敏感词汇很多，文件很多，替换也会是个耗时的任务)
        Future<String> replaceFuture = executor.submit(() -> {
            String[] words = filterWordFuture.get();
            String news = newsFuture.get();

            // 替换敏感词汇
            for (String word : words) {
                if (news.indexOf(word) >= 0) {
                    news = news.replace(word, "**");
                }
            }
            return news;
        });

        String filteredNews = replaceFuture.get();
        System.out.println("过滤后的新闻稿:" + filteredNews);

        executor.shutdown();
    }
}

```

通过上面的代码，我们会发现，Future相比于所有任务都直接在主线程处理，有很多优势，但同时也存在不足，至少表现如下：

- **在没有阻塞的情况下，无法对Future的结果执行进一步的操作**。Future不会告知你它什么时候完成，你如果想要得到结果，必须通过一个get()方法，该方法会阻塞直到结果可用为止。 它不具备将回调函数附加到Future后并在Future的结果可用时自动调用回调的能力。
- **无法解决任务相互依赖的问题**。filterWordFuture和newsFuture的结果不能自动发送给replaceFuture，需要在replaceFuture中手动获取，所以使用Future不能轻而易举地创建异步工作流。
- **不能将多个Future合并在一起**。假设你有多种不同的Future，你想在它们全部并行完成后然后再运行某个函数，Future很难独立完成这一需要。
- **没有异常处理**。Future提供的方法中没有专门的API应对异常处理，还是需要开发者自己手动异常处理。



### 1.3 CompletableFuture 的优势

![image-20221107102538728](img\01-completable-future.png)

**CompletableFuture** 实现了**Future**和**CompletionStage**接口

CompletableFuture 相对于 Future 具有以下优势：

- 为快速创建、链接依赖和组合多个Future提供了大量的便利方法。
- 提供了适用于各种开发场景的回调函数，它还提供了非常全面的异常处理支持。
- 无缝衔接和亲和 lambda 表达式 和 Stream - API 。
- 我见过的真正意义上的异步编程，把异步编程和函数式编程、响应式编程多种高阶编程思维集于一身，设计上更优雅。



## 2、创建异步任务

### 2.1 runAsync 

如果你要异步运行某些耗时的后台任务,并且不想从任务中返回任何内容，则可以使用`CompletableFuture.runAsync()`方法。它接受一个Runnable接口的实现类对象，方法返回`CompletableFuture<Void>` 对象

```java
static CompletableFuture<Void> runAsync(Runnable runnable);
```

演示案例：开启一个不从任务中返回任何内容的CompletableFuture异步任务

```java
public class RunAsyncDemo {
    public static void main(String[] args) {
        // runAsync 创建异步任务
        CommonUtils.printThreadLog("main start");
        // 使用Runnable匿名内部类
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                CommonUtils.printThreadLog("读取文件开始");
                // 使用睡眠来模拟一个长时间的工作任务(例如读取文件，网络请求等)
                CommonUtils.sleepSecond(3);
                CommonUtils.printThreadLog("读取文件结束");
            }
        });

        CommonUtils.printThreadLog("here are not blocked,main continue");
        CommonUtils.sleepSecond(4); //  此处休眠为的是等待CompletableFuture背后的线程池执行完成。
        CommonUtils.printThreadLog("main end");
    }
}
```

我们也可以以Lambda表达式的形式传递Runnable接口实现类对象

```java
public class RunAsyncDemo2 {
    public static void main(String[] args) {
        // runAsync 创建异步任务
        CommonUtils.printThreadLog("main start");
        // 使用Lambda表达式
        CompletableFuture.runAsync(() -> {
            CommonUtils.printThreadLog("读取文件开始");
            CommonUtils.sleepSecond(3);
            CommonUtils.printThreadLog("读取文件结束");
        });

        CommonUtils.printThreadLog("here are not blocked,main continue");
        CommonUtils.sleepSecond(4);
        CommonUtils.printThreadLog("main end");
    }
}

```

需求：使用CompletableFuture开启异步任务读取 news.txt 文件中的新闻稿，并打印输出。

```java
public class RunAsyncDemo3 {
    public static void main(String[] args) {
        // 需求：使用多线程异步读取 words.txt 中的敏感词汇，并打印输出。
        CommonUtils.printThreadLog("main start");

        CompletableFuture.runAsync(()->{
            String news = CommonUtils.readFile("news.txt");
            CommonUtils.printThreadLog(news);
        });

        CommonUtils.printThreadLog("here are not blocked,main continue");
        CommonUtils.sleepSecond(4);
        CommonUtils.printThreadLog("main end");
    }
}
```

在后续的章节中，我们会经常使用Lambda表达式。

### 2.2 supplyAsync

`CompletableFuture.runAsync()` 开启不带返回结果异步任务。但是，如果您想从后台的异步任务中返回一个结果怎么办？此时，`CompletableFuture.supplyAsync()`是你最好的选择了。

```java
static CompletableFuture<U>	supplyAsync(Supplier<U> supplier)
```

它入参一个 Supplier<U> 供给者，用于供给带返回值的异步任务
并返回`CompletableFuture<U>`，其中U是供给者给程序供给值的类型。

需求：开启异步任务读取 news.txt 文件中的新闻稿，返回文件中内容并在主线程打印输出

```java
public class SupplyAsyncDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String> newsFuture = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                String news = CommonUtils.readFile("news.txt");
                return news;
            }
        });

        CommonUtils.printThreadLog("here are not blocked,main continue");
        // 阻塞并等待newsFuture完成
        String news = newsFuture.get();
        CommonUtils.printThreadLog("news = " + news);
        CommonUtils.printThreadLog("main end");
    }
}
```

如果想要获取newsFuture结果，可以调用completableFuture.get()方法，get()方法将阻塞，直到newsFuture完成。

我们依然可以使用Java 8的Lambda表达式使上面的代码更简洁。

```java
CompletableFuture<String> newsFuture = CompletableFuture.supplyAsync(() -> {
    String news = CommonUtils.readFile("news.txt");
    return news;
});
```

### 2.3 异步任务中的线程池

大家已经知道，`runAsync()`和`supplyAsync()`方法都是开启单独的线程中执行异步任务。但是，我们从未创建线程对吗？ 不是吗！

CompletableFuture 会从全局的`ForkJoinPool.commonPool() ` 线程池获取线程来执行这些任务

当然，你也可以创建一个线程池，并将其传递给`runAsync()`和`supplyAsync()`方法，以使它们在从您指定的线程池获得的线程中执行任务。

CompletableFuture API中的所有方法都有两种变体,一种是接受传入的`Executor`参数作为指定的线程池，而另一种则使用默认的线程池 (`ForkJoinPool.commonPool() `) 。

```java
// runAsync() 的重载方法 
static CompletableFuture<Void>  runAsync(Runnable runnable)
static CompletableFuture<Void>  runAsync(Runnable runnable, Executor executor)
// supplyAsync() 的重载方法 
static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
```

需求：指定线程池，开启异步任务读取 news.txt 中的新闻稿，返回文件中内容并在主线程打印输出

```java
ExecutorService executor = Executors.newFixedThreadPool(4);
CompletableFuture<String> newsFuture = CompletableFuture.supplyAsync(() -> {
    CommonUtils.printThreadLog("异步读取文件开始");
    String news = CommonUtils.readFile("news.txt");
    CommonUtils.printThreadLog("异步读取文件完成");
    return news;
},executor);
```

> 最佳实践：创建属于自己的业务线程池
>
> 如果所有`CompletableFuture`共享一个线程池，那么一旦有异步任务执行一些很慢的 I/O 操作，就会导致线程池中所有线程都阻塞在 I/O 操作上，从而造成线程饥饿，进而影响整个系统的性能。
>
> 所以，强烈建议你要根据不同的业务类型创建不同的线程池，以避免互相干扰。

### 2.4 异步编程思想

​		综合上述，看到了吧，我们没有显式地创建线程，更没有涉及线程通信的概念，整个过程根本就没涉及线程知识吧，以上专业的说法是：**线程的创建和线程负责的任务进行解耦，它给我们带来的好处线程的创建和启动全部交给线程池负责，具体任务的编写就交给程序员，专人专事**。

​		**异步编程**是可以让程序并行( 也可能是并发 )运行的一种手段，其可以让程序中的一个工作单元作为异步任务与主线程分开独立运行，并且在异步任务运行结束后，会通知主线程它的运行结果或者失败原因，毫无疑问，一个异步任务其实就是开启一个线程来完成的，使用异步编程可以提高应用程序的性能和响应能力等。

作为开发者，只需要有一个意识：

开发者只需要把耗时的操作交给CompletableFuture开启一个异步任务，然后继续关注主线程业务，当异步任务运行完成时会通知主线程它的运行结果。我们把具备了这种编程思想的开发称为**异步编程思想**。



## 3、异步任务回调

`CompletableFuture.get()`方法是阻塞的。调用时它会阻塞等待 直到这个Future完成，并在完成后返回结果。 但是，很多时候这不是我们想要的。

对于构建异步系统，我们应该能够将**回调**附加到CompletableFuture上，当这个Future完成时，该回调应自动被调用。 这样，我们就不必等待结果了，然后在Future的回调函数内编写完成Future之后需要执行的逻辑。 您可以使用`thenApply()`，`thenAccept()`和`thenRun()`方法，它们可以把回调函数附加到CompletableFuture

### 3.1 thenApply

使用 `thenApply()` 方法可以处理和转换CompletableFuture的结果。 它以Function<T，R>作为参数。 Function<T，R>是一个函数式接口，表示一个转换操作，它接受类型T的参数并产生类型R的结果

```java
CompletableFuture<R> thenApply(Function<T,R> fn)
```

需求：异步读取 filter_words.txt 文件中的内容，读取完成后，把内容转换成数组( 敏感词数组 )，异步任务返回敏感词数组

```java
public class ThenApplyDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {  
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String> readFileFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
        });

        CompletableFuture<String[]> filterWordsFuture = readFileFuture.thenApply((content) -> {
            CommonUtils.printThreadLog("文件内容转换成敏感词数组");
            String[] filterWords = content.split(",");
            return filterWords;
        });

        CommonUtils.printThreadLog("main continue");
        
        String[] filterWords = filterWordsFuture.get();
        CommonUtils.printThreadLog("filterWords = " + Arrays.toString(filterWords));
        CommonUtils.printThreadLog("main end");
    }
}
```

你还可以通过附加一系列`thenApply()`回调方法，在CompletableFuture上编写一系列转换序列。一个`thenApply()`方法的结果可以传递给序列中的下一个，如果你对链式操作很了解，你会发现结果可以在链式操作上传递。

```java
CompletableFuture<String[]> filterWordsFuture = CompletableFuture.supplyAsync(() -> {
    CommonUtils.printThreadLog("读取filter_words文件");
    String filterWordsContent = CommonUtils.readFile("filter_words.txt");
    return filterWordsContent;
}).thenApply((content) -> {
    CommonUtils.printThreadLog("转换成敏感词数组");
    String[] filterWords = content.split(",");
    return filterWords;
});
```

### 3.2 thenAccept

如果你不想从回调函数返回结果，而只想在Future完成后运行一些代码，则可以使用`thenAccept()` 

 这些方法是入参一个 Consumer<T>，它可以对异步任务的执行结果进行消费使用，方法返回CompletableFuture<Void>。 

```java
CompletableFuture<Void>	thenAccept(Consumer<T> action)
```

通常用作回调链中的最后一个回调。

需求：异步读取 filter_words.txt 文件中的内容，读取完成后，转换成敏感词数组，然后打印敏感词数组

```java
public class ThenAcceptDemo {
    public static void main(String[] args) {
        CommonUtils.printThreadLog("main start");

        CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
        }).thenApply((content) -> {
            CommonUtils.printThreadLog("转换成敏感词数组");
            String[] filterWords = content.split(",");
            return filterWords;
        }).thenAccept((filterWords) -> {
            CommonUtils.printThreadLog("filterWords = " + Arrays.toString(filterWords));
        });

        CommonUtils.printThreadLog("main continue");
        CommonUtils.sleepSecond(4);
        CommonUtils.printThreadLog("main end");
    }
}
```

### 3.3 thenRun

前面我们已经知道，通过thenApply( Function<T,R> ) 对链式操作中的上一个异步任务的结果进行转换，返回一个新的结果；

通过thenAccept( Consumer<T> ) 对链式操作中上一个异步任务的结果进行消费使用，不返回新结果；

如果我们只是想从CompletableFuture的链式操作得到一个完成的通知，甚至都不使用上一步链式操作的结果，那么 CompletableFuture.thenRun() 会是你最佳的选择，它需要一个Runnable并返回`CompletableFuture<Void>`。

```java
CompletableFuture<Void> thenRun(Runnable action);
```

演示案例：我们仅仅想知道 filter_words.txt 的文件是否读取完成

```java
public class ThenRunDemo {
    public static void main(String[] args) {
        CommonUtils.printThreadLog("main start");

        CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
        }).thenRun(() -> {
            CommonUtils.printThreadLog("读取filter_words文件读取完成");
        });

        CommonUtils.printThreadLog("main continue");
        CommonUtils.sleepSecond(4);
        CommonUtils.printThreadLog("main end");
    }
}
```

### 3.4 更进一步提升并行化

CompletableFuture 提供的所有回调方法都有两个异步变体

```java
CompletableFuture<U> thenApply(Function<T,U> fn)
// 回调方法的异步变体(异步回调)
CompletableFuture<U> thenApplyAsync(Function<T,U> fn)
CompletableFuture<U> thenApplyAsync(Function<T,U> fn, Executor executor)
```

注意：这些带了Async的异步回调 **通过在单独的线程中执行回调任务** 来帮助您进一步促进并行化计算。

回顾需求：异步读取 filter_words.txt 文件中的内容，读取完成后，转换成敏感词数组，主线程获取结果打印输出这个数组

```java
public class ThenApplyAsyncDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String[]> filterWordFuture = CompletableFuture.supplyAsync(() -> {
            /*
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
            */

            // 此时，立即返回结果
            return "尼玛, NB, tmd";
        }).thenApply((content) -> {
            /**
             * 一般而言，thenApply任务的执行和supplyAsync()任务执行可以使用同一线程执行
             * 如果supplyAsync()任务立即返回结果，则thenApply的任务在主线程中执行
             */
            CommonUtils.printThreadLog("把内容转换成敏感词数组");
            String[] filterWords = content.split(",");
            return filterWords;
        });

        CommonUtils.printThreadLog("main continue");

        String[] filterWords = filterWordFuture.get();
        CommonUtils.printThreadLog("filterWords = " + Arrays.toString(filterWords));
        CommonUtils.printThreadLog("main end");
    }
}

```

要更好地控制执行回调任务的线程，可以使用异步回调。如果使用`thenApplyAsync()`回调，那么它将在从` ForkJoinPool.commonPool() `  获得的另一个线程中执行

```java
public class ThenApplyAsyncDemo2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String[]> filterWordFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
        }).thenApplyAsync((content) -> {
            CommonUtils.printThreadLog("把内容转换成敏感词数组");
            String[] filterWords = content.split(",");
            return filterWords;
        });

        CommonUtils.printThreadLog("main continue");

        String[] filterWords = filterWordFuture.get();
        CommonUtils.printThreadLog("filterWords = " + Arrays.toString(filterWords));
        CommonUtils.printThreadLog("main end");
    }
}
```

以上程序一种可能的运行结果（需要多运行几次）：

```java
1672885914481 |  1 | main | main start
1672885914511 | 16 | ForkJoinPool.commonPool-worker-1 | 读取filter_words.txt文件
1672885914511 |  1 | main | main continue
1672885914521 | 17 | ForkJoinPool.commonPool-worker-2 | 把内容转换成敏感词数组
1672885914521 |  1 | main | filterWords = [尼玛, NB, tmd]
1672885914521 |  1 | main | main end
```

此外，如果将Executor传递给`thenApplyAsync()`回调，则该回调的异步任务将在从Executor的线程池中获取的线程中执行;

```java
ExecutorService executor = Executors.newFixedThreadPool(5);
CompletableFuture<String[]> filterWordFuture = CompletableFuture.supplyAsync(() -> {
    CommonUtils.printThreadLog("读取filter_words文件");
    String filterWordsContent = CommonUtils.readFile("filter_words.txt");
    return filterWordsContent;
}).thenApplyAsync((content) -> {
    CommonUtils.printThreadLog("把内容转换成敏感词数组");
    String[] filterWords = content.split(",");
    return filterWords;
},executor);
executor.shutdown();
```

其他两个回调的变体版本如下：

```java
// thenAccept和其异步回调
CompletableFuture<Void>	thenAccept(Consumer<T> action)
CompletableFuture<Void>	thenAcceptAsync(Consumer<T> action)
CompletableFuture<Void>	thenAcceptAsync(Consumer<T> action, Executor executor)

// thenRun和其异步回调
CompletableFuture<Void>	thenRun(Runnable action)
CompletableFuture<Void>	thenRunAsync(Runnable action)
CompletableFuture<Void>	thenRunAsync(Runnable action, Executor executor)
```



## 4、异步任务编排

### 4.1 编排2个依赖关系的异步任务 thenCompose()

回顾需求：异步读取 filter_words.txt 文件中的内容，读取完成后，转换成敏感词数组让主线程待用。

关于读取和解析内容，假设使用以下的 readFileFuture(String)  和 splitFuture(String) 方法完成。

```java
public static CompletableFuture<String> readFileFuture(String fileName) {
    return CompletableFuture.supplyAsync(() -> {
        String filterWordsContent = CommonUtils.readFile(fileName);
        return filterWordsContent;
    });
}

public static CompletableFuture<String[]> splitFuture(String context) {
    return CompletableFuture.supplyAsync(() -> {
        String[] filterWords = context.split(",");
        return filterWords;
    });
}
```

现在，让我们先了解如果使用`thenApply()` 结果会发生什么

```java
CompletableFuture<CompletableFuture<String[]>> future = readFileFuture("filter_words.txt")
    .thenApply((context) -> {
       return splitFuture(context);
    });
```

回顾在之前的案例中，`thenApply(Function<T,R>)` 中Function回调会对上一步任务结果转换后得到一个简单值 ，但现在这种情况下，最终结果是嵌套的CompletableFuture，所以这是不符合预期的，那怎么办呢？

我们想要的是：把上一步异步任务的结果，转成一个CompletableFuture对象，这个CompletableFuture对象中包含本次异步任务处理后的结果。也就是说，**我们想组合上一步异步任务的结果到下一个新的异步任务中, 结果由这个新的异步任务返回**

此时，你需要使用`thenCompose()`方法代替，我们可以把它理解为 **异步任务的组合**

```java
CompletableFuture<R> thenCompose(Function<T,CompletableFuture<R>> func)
```
所以，`thenCompose()`用来连接两个有依赖关系的异步任务，结果由第二个任务返回
```java
CompletableFuture<String[]> future = readFileFuture("filter_words.txt")
    .thenCompose((context) -> { 
        return splitFuture(context);
    });
```

因此，这里积累了一个经验：

如果我们想连接( 编排 ) 两个依赖关系的异步任务( CompletableFuture 对象 )  ,请使用 thenCompose() 方法

当然，thenCompose 也存在异步回调变体版本：

```java
CompletableFuture<R> thenCompose(Function<T,CompletableFuture<R>> fn)
    
CompletableFuture<R> thenComposeAsync(Function<T,CompletableFuture<R>> fn)
CompletableFuture<R> thenComposeAsync(Function<T,CompletableFuture<R>> fn, Executor executor)
```



### 4.2 编排2个非依赖关系的异步任务 thenCombine() 

我们已经知道，当其中一个Future依赖于另一个Future，使用`thenCompose()`用于组合两个Future。如果两个Future之间没有依赖关系，你希望两个Future独立运行并在两者都完成之后执行回调操作时，则使用`thenCombine()`;

```java
// T是第一个任务的结果 U是第二个任务的结果 V是经BiFunction应用转换后的结果
CompletableFuture<V> thenCombine(CompletableFuture<U> other, BiFunction<T,U,V> func)
```

需求：替换新闻稿 ( news.txt ) 中敏感词汇 ，把敏感词汇替换成*，敏感词存储在 filter_words.txt 中

```java
public class ThenCombineDemo {
    public static void main(String[] args) throws Exception {
        // 读取敏感词汇的文件并解析到数组中
        CompletableFuture<String[]> future1 = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取敏感词汇并解析");
            String context = CommonUtils.readFile("filter_words.txt");
            String[] words = context.split(",");
            return words;
        });

        // 读取news文件内容
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取news文件内容");
            String context = CommonUtils.readFile("news.txt");
            return context;
        });

        CompletableFuture<String> combinedFuture = future1.thenCombine(future2, (words, context) -> {
            // 替换操作
            CommonUtils.printThreadLog("替换操作");
            for (String word : words) {
                if(context.indexOf(word) > -1) {
                    context = context.replace(word, "**");
                }
            }
            return context;
        });

        String filteredContext = combinedFuture.get();
        System.out.println("filteredContext = " + filteredContext);
    }
}
```

注意：当两个`Future`都完成时，才将两个异步任务的结果传递给`thenCombine()`的回调函数做进一步处理。

和以往一样，thenCombine 也存在异步回调变体版本

```java
CompletableFuture<V> thenCombine(CompletableFuture<U> other, BiFunction<T,U,V> func)
CompletableFuture<V> thenCombineAsync(CompletableFuture<U> other, BiFunction<T,U,V> func)
CompletableFuture<V> thenCombineAsync(CompletableFuture<U> other, BiFunction<T,U,V> func,Executor executor)
```



### 4.3 合并多个异步任务 allOf / anyOf

我们使用`thenCompose()`和`thenCombine()`将两个CompletableFuture组合和合并在一起。

如果要编排任意数量的CompletableFuture怎么办？可以使用以下方法来组合任意数量的CompletableFuture

```java
public static CompletableFuture<Void>	allOf(CompletableFuture<?>... cfs)
public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs)
```

`CompletableFuture.allOf()`用于以下情形中：有多个需要独立并行运行的`Future`，并在所有这些`Future` 都完成后执行一些操作。

需求：统计news1.txt、new2.txt、new3.txt 文件中包含CompletableFuture关键字的文件的个数

```java
public class AllOfDemo {

    public static CompletableFuture<String> readFileFuture(String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            String content = CommonUtils.readFile(fileName);
            return content;
        });
    }

    public static void main(String[] args) {
        // step 1: 创建List集合存储文件名
        List<String> fileList = Arrays.asList("news1.txt", "news2.txt", "news3.txt");

        // step 2: 根据文件名调用readFileFuture创建多个CompletableFuture，并存入List集合中
        List<CompletableFuture<String>> readFileFutureList = fileList.stream().map(fileName -> {
            return readFileFuture(fileName);
        }).collect(Collectors.toList());

        // step 3: 把List集合转换成数组待用，以便传入allOf方法中
        int len = readFileFutureList.size();
        CompletableFuture[] readFileFutureArr = readFileFutureList.toArray(new CompletableFuture[len]);

        // step 4: 使用allOf方法合并多个异步任务
        CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(readFileFutureArr);

        // step 5: 当多个异步任务都完成后，使用回调操作文件结果，统计符合条件的文件个数
        CompletableFuture<Long> countFuture = allOfFuture.thenApply(v -> {
            return readFileFutureList.stream()
                    .map(future -> future.join())
                    .filter(content -> content.contains("CompletableFuture"))
                    .count();
        });

        // step 6: 主线程打印输出文件个数,这里join()也可以换成get(),效果一样的
        Long count = countFuture.join();
        System.out.println("count = " + count);
    }
}
```



顾名思义，当给定的多个异步任务中的有任意Future一个完成时，需要执行一些操作，可以使用 anyOf 方法

```java
public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs)
```

`anyOf()`返回一个新的CompletableFuture，新的CompletableFuture的结果和 cfs中已完成的那个异步任务结果相同。

演示案例：anyOf 执行过程

```java
public class AnyOfDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            Tools.sleepMillis(2);
            return "Future1的结果";
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            Tools.sleepMillis(1);
            return "Future2的结果";
        });

        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            Tools.sleepMillis(3);
            return "Future3的结果";
        });

        CompletableFuture<Object> anyOfFuture = CompletableFuture.anyOf(future1, future2, future3);

        // 输出Future2的结果
        System.out.println(anyOfFuture.get());
    }
}
```

在上面的示例中，当三个CompletableFuture中的任意一个完成时，anyOfFuture就完成了。 由于future2的睡眠时间最少，因此它将首先完成，最终结果将是"Future2的结果"。

注意：

- `anyOf()` 方法返回类型必须是 `CompletableFuture <Object>`。 
- `anyOf()`的问题在于，如果您拥有返回不同类型结果的CompletableFuture，那么您将不知道最终CompletableFuture的类型。



## 5、异步任务的异常处理

在前面的章节中，我们并没有更多地关心异常处理的问题，其实，CompletableFuture 提供了优化处理异常的方式。

首先，让我们了解**异常如何在回调链中传播**。

```java
public static void main(String[] args) throws ExecutionException, InterruptedException {

        CompletableFuture<Void> cf = CompletableFuture.supplyAsync(() -> {
            int r = 1 / 0;
            return "result1";
        }).thenApply(result -> {
            return result + " result2";
        }).thenApply(result -> {
            return result + " result3";
        }).thenAccept((result)->{
            System.out.println(result);
        });
    }
```

如果在 supplyAsync 任务中出现异常，后续的 thenApply 和 thenAccept 回调都不会执行，CompletableFuture 将转入异常处理

如果在第一个 thenApply 任务中出现异常，第二个 thenApply 和 最后的 thenAccept 回调不会被执行，CompletableFuture 将转入异常处理，依次类推。

### 5.1 exceptionally()

exceptionally 用于处理回调链上的异常，回调链上出现的任何异常，回调链不继续向下执行，都在exceptionally中处理异常。

```java
// Throwable表示具体的异常对象e
CompletableFuture<R> exceptionally(Function<Throwable, R> func)
```

```java
public class ExceptionallyDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            int r = 1 / 0;
            return "result1";
        }).thenApply(result -> {
            String str = null;
            int len = str.length();
            return result + " result2";
        }).thenApply(result -> {
            return result + " result3";
        }).exceptionally(ex -> {
            System.out.println("出现异常:" + ex.getMessage());
            return "Unknown";
        });

        String ret = future.get();
        Tools.printThreadLog("最终结果:" + ret);
    }
}

```

因为exceptionally只处理一次异常，所以常常用在回调链的末端。

### 5.2 handle()

CompletableFuture API 还提供了一种更通用的方法 `handle()` 表示从异常中恢复

handle() 常常被用来恢复回调链中的一次特定的异常，回调链恢复后可以进一步向下传递。

```java
CompletableFuture<R> handle(BiFunction<T, Throwable, R> fn)
```

```java
public class HandleDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            int r = 1 / 0;
            return "result";
        }).handle((ret, ex) -> {
            if(ex != null) {
                System.out.println("我们得到异常：" + ex.getMessage());
                return "Unknown!";
            }
            return ret;
        });

        String ret = future.get();
        CommonUtils.printThreadLog(ret);
    }
}
```

如果发生异常，则res参数将为null，否则ex参数将为null。

需求：对回调链中的一次异常进行恢复处理

```java
public class HandleExceptionDemo2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            int r = 1 / 0;
            return "result1";
        }).handle((ret, ex) -> {
            if (ex != null) {
                System.out.println("我们得到异常：" + ex.getMessage());
                return "Unknown1";
            }
            return ret;
        }).thenApply(result -> {
            String str = null;
            int len = str.length();
            return result + " result2";
        }).handle((ret, ex) -> {
            if (ex != null) {
                System.out.println("我们得到异常：" + ex.getMessage());
                return "Unknown2";
            }
            return ret;
        }).thenApply(result -> {
            return result + " result3";
        });

        String ret = future.get();
        Tools.printThreadLog("最终结果:" + ret);
    }
}
```

和以往一样，为了提升并行化，异常处理可以方法单独的线程执行，以下是它们的异步回调版本

```java
CompletableFuture<R> exceptionally(Function<Throwable, R> fn)
CompletableFuture<R> exceptionallyAsync(Function<Throwable, R> fn)  
CompletableFuture<R> exceptionallyAsync(Function<Throwable, R> fn,Executor executor)

CompletableFuture<R> handle(BiFunction<T,Throwable,R> fn)
CompletableFuture<R> handleAsync(BiFunction<T,Throwable,R> fn)
CompletableFuture<R> handleAsync(BiFunction<T,Throwable,R> fn, Executor executor)
```



