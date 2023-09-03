# CompletableFuture进阶

## 学习内容

- 异步任务的交互
- get方法和join方法区别
- CompletableFuture 在流式编程（Stream API）的优势
- CompletableFutrue实战应用之大数据商品比价

## 学习目标

- 掌握异步任务的交互操作
- 了解get方法和join方法区别
- 掌握CompletableFuture 结合Stream API进阶应用
- 掌握CompletableFuture 在实战中的应用



## 1、异步任务的交互

异步任务交互指将异步任务获取结果的**速度相比较**，按一定的规则( **先到先用** )进行下一步处理。

### 1.1 applyToEither

`applyToEither()`  把两个异步任务做比较，异步任务先到结果的，就对先到的结果进行下一步的操作。

```java
CompletableFuture<R> applyToEither(CompletableFuture<T> other, Function<T,R> func)
```

演示案例：使用最先完成的异步任务的结果

```java
public class ApplyToEitherDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 开启异步任务1
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            int x = new Random().nextInt(3);
            CommonUtils.sleepSecond(x);
            CommonUtils.printThreadLog("任务1耗时:" + x + "秒");
            return x;
        });

        // 开启异步任务2
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            int y = new Random().nextInt(3);
            CommonUtils.sleepSecond(y);
            CommonUtils.printThreadLog("任务2耗时:" + y + "秒");
            return y;
        });

        // 哪些异步任务的结果先到达，就使用哪个异步任务的结果
        CompletableFuture<Integer> future = future1.applyToEither(future2, (result -> {
            CommonUtils.printThreadLog("最先到达的结果:" + result);
            return result;
        }));

        // 主线程休眠4秒，等待所有异步任务完成
        CommonUtils.sleepSecond(4);
        Integer ret = future.get();
        CommonUtils.printThreadLog("ret = " + ret);
    }
}

```

速记心法：任务1、任务2就像两辆公交，哪路公交先到，就乘坐(使用)哪路公交。

以下是applyToEither 和其对应的异步回调版本

```java
CompletableFuture<R> applyToEither(CompletableFuture<T> other, Function<T,R> func)
CompletableFuture<R> applyToEitherAsync(CompletableFuture<T> other, Function<T,R> func)
CompletableFuture<R> applyToEitherAsync(CompletableFuture<T> other, Function<T,R> func,Executor executor)
```

### 1.2 acceptEither

`acceptEither()` 把两个异步任务做比较，异步任务先到结果的，就对先到的结果进行下一步操作 ( 消费使用 )。

```java
CompletableFuture<Void> acceptEither(CompletableFuture<T> other, Consumer<T> action)
CompletableFuture<Void> acceptEitherAsync(CompletableFuture<T> other, Consumer<T> action)  
CompletableFuture<Void> acceptEitherAsync(CompletableFuture<T> other, Consumer<T> action,Executor executor)
```

演示案例：使用最先完成的异步任务的结果

```java
public class AcceptEitherDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 异步任务交互
        CommonUtils.printThreadLog("main start");
        // 开启异步任务1
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            int x = new Random().nextInt(3);
            CommonUtils.sleepSecond(x);
            CommonUtils.printThreadLog("任务1耗时:" + x + "秒");
            return x;
        });

        // 开启异步任务2
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            int y = new Random().nextInt(3);
            CommonUtils.sleepSecond(y);
            CommonUtils.printThreadLog("任务2耗时:" + y + "秒");
            return y;
        });

        // 哪些异步任务的结果先到达，就使用哪个异步任务的结果
        future1.acceptEither(future2,result -> {
            CommonUtils.printThreadLog("最先到达的结果:" + result);
        });

        // 主线程休眠4秒，等待所有异步任务完成
        CommonUtils.sleepSecond(4);
        CommonUtils.printThreadLog("main end");
    }
}
```

### 1.3 runAfterEither

如果不关心最先到达的结果，只想在有一个异步任务先完成时得到完成的通知，可以使用 `runAfterEither()` ，以下是它的相关方法：

```java
CompletableFuture<Void> runAfterEither(CompletableFuture<T> other, Runnable action)
CompletableFuture<Void>	runAfterEitherAsync(CompletableFuture<T> other, Runnable action)
CompletableFuture<Void>	runAfterEitherAsync(CompletableFuture<T> other, Runnable action, Executor executor)
```

> 提示
>
> 异步任务交互的三个方法和之前学习的异步的回调方法 thenApply、thenAccept、thenRun 有异曲同工之妙。

## 2、get() 和 join() 区别

get() 和 join() 都是CompletableFuture提供的以阻塞方式获取结果的方法。

那么该如何选用呢？请看如下案例：

```java
public class GetOrJoinDemo {
    public static void main(String[] args) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return "hello";
        });

        String ret = null;
        // 抛出检查时异常，必须处理
        try {
            ret = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("ret = " + ret);

        // 抛出运行时异常，可以不处理
        ret = future.join();
        System.out.println("ret = " + ret);
    }
}
```

使用时，我们发现，get() 抛出检查时异常 ，需要程序必须处理；而join() 方法抛出运行时异常，程序可以不处理。所以，join() 更适合用在流式编程中。

## 3、ParallelStream VS CompletableFuture

CompletableFuture 虽然提高了任务并行处理的能力，如果它和 Stream API 结合使用，能否进一步多个任务的并行处理能力呢？

同时，对于 Stream API 本身就提供了并行流ParallelStream，它们有什么不同呢？

我们将通过一个耗时的任务来体现它们的不同，更重要地是，我们能进一步加强 CompletableFuture 和 Stream API 的结合使用，同时搞清楚CompletableFuture 在流式操作的优势



需求：创建10个MyTask耗时的任务，统计它们执行完的总耗时

定义一个MyTask类，来模拟耗时的长任务

```java
public class MyTask {
    private int duration;

    public MyTask(int duration) {
        this.duration = duration;
    }

    // 模拟耗时的长任务
    public int doWork() {
        CommonUtils.printThreadLog("doWork");
        CommonUtils.sleepSecond(duration);
        return duration;
    }
}
```

同时，我们创建10个任务，每个持续1秒。

```java
IntStream intStream = IntStream.range(0, 10);
List<MyTask> tasks = intStream.mapToObj(item -> {
    return new MyTask(1);
}).collect(Collectors.toList());
```

### 3.1 并行流的局限

我们先使用串行执行，让所有的任务都在主线程 main 中执行。

```java
public class SequenceDemo {
    public static void main(String[] args) {
        // 方案一：在主线程中使用串行执行
        // step 1: 创建10个MyTask对象,每个任务持续1s,存入list集合便于启动Stream操作
        IntStream intStream = IntStream.range(0, 10);
        List<MyTask> tasks = intStream.mapToObj(item -> {
            return new MyTask(1);
        }).collect(Collectors.toList());

        // step 2: 执行tasks集合中的每个任务，统计总耗时
        long start = System.currentTimeMillis();
        List<Integer> result = tasks.stream().map(myTask -> {
            return myTask.doWork();
        }).collect(Collectors.toList());
        long end = System.currentTimeMillis();
        double costTime = (end - start) / 1000.0;

        System.out.printf("processed %d tasks cost %.2f second",tasks.size(),costTime);
    }
}
```

它花费了10秒, 因为每个任务在主线程一个接一个的执行。

因为涉及 Stream API，而且存在耗时的长任务，所以，我们可以使用 `parallelStream()`

```java
public class ParallelDemo {
    public static void main(String[] args) {
        // 方案二：使用并行流
        // step 1: 创建10个MyTask对象，每个任务持续1s，存入List集合
        IntStream intStream = IntStream.range(0, 10);
        List<MyTask> tasks = intStream.mapToObj(item -> {
            return new MyTask(1);
        }).collect(Collectors.toList());

        // step 2: 执行10个MyTask,统计总耗时
        long start = System.currentTimeMillis();
        List<Integer> results = tasks.parallelStream().map(myTask -> {
            return myTask.doWork();
        }).collect(Collectors.toList());
        long end = System.currentTimeMillis();

        double costTime = (end - start) / 1000.0;
        System.out.printf("processed %d tasks %.2f second",tasks.size(),costTime);
    }
}
```

它花费了2秒多，因为此次并行执行使用了8个线程 (7个是ForkJoinPool线程池中的, 一个是 main 线程)，需要注意是：运行结果由自己电脑CPU的核数决定。

### 3.2 CompletableFuture 在流式操作的优势

让我们看看使用CompletableFuture是否执行的更有效率

```java
public class CompletableFutureDemo {
    public static void main(String[] args) {
        // 需求：创建10MyTask耗时的任务，统计它们执行完的总耗时
        // 方案三：使用CompletableFuture
        // step 1: 创建10个MyTask对象，每个任务持续1s，存入List集合
        IntStream intStream = IntStream.range(0, 10);
        List<MyTask> tasks = intStream.mapToObj(item -> {
            return new MyTask(1);
        }).collect(Collectors.toList());

        // step 2: 根据MyTask对象构建10个耗时的异步任务
        long start = System.currentTimeMillis();
        List<CompletableFuture<Integer>> futures = tasks.stream().map(myTask -> {
            return CompletableFuture.supplyAsync(() -> {
                return myTask.doWork();
            });
        }).collect(Collectors.toList());

        // step 3: 当所有任务完成时，获取每个异步任务的执行结果，存入List集合中
        List<Integer> results = futures.stream().map(future -> {
            return future.join();
        }).collect(Collectors.toList());
        long end = System.currentTimeMillis();

        double costTime = (end - start) / 1000.0;
        System.out.printf("processed %d tasks cost %.2f second",tasks.size(),costTime);
    }
}
```

运行发现，两者使用的时间大致一样。能否进一步优化呢？

CompletableFutures 比 ParallelStream 优点之一是你可以指定Executor去处理任务。你能选择更合适数量的线程。我们可以选择大于Runtime.getRuntime().availableProcessors() 数量的线程，如下所示：

```java
public class CompletableFutureDemo2 {
    public static void main(String[] args) {
        // 需求：创建10MyTask耗时的任务，统计它们执行完的总耗时
        // 方案三：使用CompletableFuture
        // step 1: 创建10个MyTask对象，每个任务持续1s，存入List集合
        IntStream intStream = IntStream.range(0, 10);
        List<MyTask> tasks = intStream.mapToObj(item -> {
            return new MyTask(1);
        }).collect(Collectors.toList());

        // 准备线程池
        final int N_CPU = Runtime.getRuntime().availableProcessors();
        // 设置线程池的数量最少是10个，最大是16个
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(tasks.size(), N_CPU * 2));

        // step 2: 根据MyTask对象构建10个耗时的异步任务
        long start = System.currentTimeMillis();
        List<CompletableFuture<Integer>> futures = tasks.stream().map(myTask -> {
            return CompletableFuture.supplyAsync(() -> {
                return myTask.doWork();
            },executor);
        }).collect(Collectors.toList());

        // step 3: 当所有任务完成时，获取每个异步任务的执行结果，存入List集合中
        List<Integer> results = futures.stream().map(future -> {
            return future.join();
        }).collect(Collectors.toList());
        long end = System.currentTimeMillis();

        double costTime = (end - start) / 1000.0;
        System.out.printf("processed %d tasks cost %.2f second",tasks.size(),costTime);
        // 关闭线程池
        executor.shutdown();
    }
}
```

测试代码时，电脑配置是4核8线程，而我们创建的线程池中线程数最少也是10个，所以，每个线程负责一个任务( 耗时1s )，总体来说，处理10个任务总共需要约1秒。

### 3.3  合理配置线程池中的线程数

正如我们看到的，**CompletableFuture 可以更好地控制线程池中线程的数量，而 ParallelStream 不能**。

问题1：如何选用 CompletableFuture 和  ParallelStream ？

如果你的任务是IO密集型的，你应该使用CompletableFuture；

如果你的任务是CPU密集型的，使用比处理器更多的线程是没有意义的，所以选择ParallelStream ，因为它不需要创建线程池，更容易使用。



问题2：IO密集型任务和CPU密集型任务的区别？

**CPU密集型**也叫计算密集型，此时，系统运行时大部分的状况是CPU占用率近乎100%，I/O在很短的时间就可以完成，而CPU还有许多运算要处理，CPU 使用率很高。比如说要计算1+2+3+…+ 10万亿、天文计算、圆周率后几十位等， 都是属于CPU密集型程序。

CPU密集型任务的特点：大量计算，CPU占用率一般都很高，I/O时间很短

**IO密集型**指大部分的状况是CPU在等I/O (硬盘/内存) 的读写操作，但CPU的使用率不高。

简单的说，就是需要大量的输入输出，例如读写文件、传输文件、网络请求。

IO密集型任务的特点：大量网络请求，文件操作，CPU运算少，很多时候CPU在等待资源才能进一步操作。



问题3：既然要控制线程池中线程的数量，多少合适呢？

如果是CPU密集型任务，就需要尽量压榨CPU，参考值可以设为 Ncpu+1

如果是IO密集型任务，参考值可以设置为 2 * Ncpu，其中Ncpu 表示 核心数。

注意的是：以上给的是参考值，详细配置超出本次课程的范围，选不赘述。



## 4、大数据商品比价

### 4.1 需求描述和分析

**需求描述**： 实现一个大数据比价服务，价格数据可以从京东、天猫、拼多多等平台去获取指定商品的价格、优惠金额，然后计算出实际付款金额 ( 商品价格 - 优惠金额 )，最终返回价格最优的平台与价格信息。

<img src="img\01-需求.png" style="zoom: 65%;" />

### 4.2 构建工具类和实体类

定义价格实体类 PriceResult

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceResult {
    private int price;			// 平台价格
    private int discount;		// 折扣
    private int realPrice;		// 最终价
    private String platform;	// 商品平台

    public PriceResult(String platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "PriceResult{" +
                "平台=" + platform +
                ", 价格=" + price +
                ", 折扣=" + discount +
                ", 最终价=" + realPrice +
                '}';
    }
}
```

修改工具类 CommonUtils，添加 getCurrentTime() 方法获取当前时间并格式化，修改 printThreadLog() 方法，把时间戳替换成当前时间。

```java
public class CommonUtils {

    private static String getCurrentTime() {
        LocalTime time = LocalTime.now();
        return time.format(DateTimeFormatter.ofPattern("[HH:mm:ss.SSS]"));
    }

    // 打印输出带线程信息的日志
    public static void printThreadLog(String message) {
        // 时间戳 | 线程id | 线程名 | 日志信息
        String result = new StringJoiner(" | ")
                .add(getCurrentTime())
                .add(String.format("%2d", Thread.currentThread().getId()))
                .add(Thread.currentThread().getName())
                .add(message)
                .toString();
        System.out.println(result);
    }

}
```

### 4.3 构建 HttpRequest

HttpRequest 用于模拟网络请求 ( 耗时的操作 )

```java
public class HttpRequest {

    // 获取指定商品的淘宝价
    public static PriceResult getTaoBaoPrice(String productName) {
        CommonUtils.printThreadLog("获取淘宝上" + productName + "价格");
        mockCostTimeOperation();

        PriceResult priceResult = new PriceResult("淘宝");
        priceResult.setPrice(5199);
        CommonUtils.printThreadLog("获取淘宝上" + productName + "价格完成：5199");
        return priceResult;
    }
    // 获取指定商品的淘宝优惠
    public static int getTaoBaoDiscount(String productName) {
        CommonUtils.printThreadLog("获取淘宝上" + productName + "优惠");
        mockCostTimeOperation();
        CommonUtils.printThreadLog("获取淘宝上" + productName + "优惠完成：-200");
        return 200;
    }

    // 获取指定商品的JD价
    public static PriceResult getJDongPrice(String productName) {
        CommonUtils.printThreadLog("获取京东上" + productName + "价格");
        mockCostTimeOperation();

        PriceResult priceResult = new PriceResult("京东");
        priceResult.setPrice(5299);
        CommonUtils.printThreadLog("获取京东上" + productName + "价格完成：5299");
        return priceResult;
    }
    // 获取指定商品的JD优惠
    public static int getJDongDiscount(String productName) {
        CommonUtils.printThreadLog("获取京东上" + productName + "优惠");
        mockCostTimeOperation();
        CommonUtils.printThreadLog("获取京东上" + productName + "优惠完成：-150");
        return 150;
    }


    // 获取指定商品的拼多多价
    public static PriceResult getPDDPrice(String productName) {
        CommonUtils.printThreadLog("获取拼多多上" + productName + "价格");
        mockCostTimeOperation();

        PriceResult priceResult = new PriceResult("拼多多");
        priceResult.setPrice(5399);
        CommonUtils.printThreadLog("获取拼多多上" + productName + "价格完成：5399");
        return priceResult;
    }
    // 获取指定商品的拼多多优惠
    public static int getPDDDiscount(String productName) {
        CommonUtils.printThreadLog("获取拼多多上" + productName + "优惠");
        mockCostTimeOperation();
        CommonUtils.printThreadLog("获取拼多多上" + productName + "优惠完成：-5300");
        return 5300;
    }

    // 模拟耗时的操作
    private static void mockCostTimeOperation() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

### 4.4 使用串行方式操作商品比价

```java
public class ComparePriceService {

    // 方案一：串行方式操作商品比价
    public PriceResult getCheapestPlatformPrice(String productName) {
        PriceResult priceResult;
        int discount;

        // 获取淘宝平台的价格和优惠
        priceResult = HttpRequest.getTaoBaoPrice(productName);
        discount = HttpRequest.getTaoBaoDiscount(productName);
        PriceResult taoBaoPriceResult = this.computeRealPrice(priceResult, discount);

        // 获取京东平台的价格和优惠
        priceResult = HttpRequest.getJDongPrice(productName);
        discount = HttpRequest.getJDongDiscount(productName);
        PriceResult jDongPriceResult = this.computeRealPrice(priceResult, discount);

        // 获取拼多多平台的价格和优惠
        priceResult = HttpRequest.getPDDPrice(productName);
        discount = HttpRequest.getPDDDiscount(productName);
        PriceResult pddPriceResult = this.computeRealPrice(priceResult, discount);

        Stream<PriceResult> stream = Stream.of(taoBaoPriceResult, jDongPriceResult, pddPriceResult);
        Optional<PriceResult> minOpt = stream.min(Comparator.comparing(PriceResult::getRealPrice));
        return minOpt.get();
    }


    // 计算商品的最终价格 = 平台价格 - 优惠价
    public PriceResult computeRealPrice(PriceResult priceResult,int discount) {
        priceResult.setRealPrice(priceResult.getPrice() - discount);
        priceResult.setDiscount(discount);
        LogUtils.printLog(priceResult.getPlatform() + "最终价格计算完成:" + priceResult.getRealPrice());
        return priceResult;
    }
}
```

使用串行方式在main线程中执行的测试类

```java
public class ComparePriceDemo {
    public static void main(String[] args) {
        // 方案一测试：串行方式操作商品比价
        ComparePriceService service = new ComparePriceService();

        long start = System.currentTimeMillis();
        PriceResult priceResult = service.getCheapestPlatformPrice("iPhone");
        long end = System.currentTimeMillis();
        
        double costTime = (end - start) / 1000.0;
        System.out.printf("cost %.2f second \n",costTime);
        
        System.out.println("priceResult = " + priceResult);
    }
}
```



### 4.5 使用Future+线程池增强并行

```java
public class ComparePriceService {

    // 方案二：使用Future+线程池增强并行
    public PriceResult getCheapestPlatformPrice2(String productName) {
        // 线程池
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        // 获取淘宝平台的价格和优惠
        Future<PriceResult> taoBaoFuture = executor.submit(() -> {
            PriceResult priceResult = HttpRequest.getTaoBaoPrice(productName);
            int discount = HttpRequest.getTaoBaoDiscount(productName);
            return this.computeRealPrice(priceResult, discount);
        });

        // 获取京东平台的价格和优惠
        Future<PriceResult> jDongFuture = executor.submit(() -> {
            PriceResult priceResult = HttpRequest.getJDongPrice(productName);
            int discount = HttpRequest.getJDongDiscount(productName);
            return this.computeRealPrice(priceResult, discount);
        });

        // 获取拼多多平台的价格和优惠
        Future<PriceResult> pddFuture = executor.submit(() -> {
            PriceResult priceResult = HttpRequest.getPDDPrice(productName);
            int discount = HttpRequest.getPDDDiscount(productName);
            return this.computeRealPrice(priceResult, discount);
        });

        // 比较计算最便宜的平台和价格
        return Stream.of(taoBaoFuture, jDongFuture, pddFuture)
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparing(PriceResult::getRealPrice))
                .get();
    }
}
```

使用Future+线程池的方式的测试类

```java
public class ComparePriceDemo {
    public static void main(String[] args) {
        ComparePriceService service = new ComparePriceService();
        // 方案二测试：使用Future+线程池增强并行
        long start = System.currentTimeMillis();
        PriceResult priceResult = service.getCheapestPlatformPrice2("iPhone");
        long end = System.currentTimeMillis();
        
        double costTime = (end - start) / 1000.0;
        System.out.printf("cost %.2f second \n",costTime);
        
        System.out.println("priceResult = " + priceResult);
    }
}
```

### 4.6 使用 CompletableFuture 进一步增强并行

```java
public class ComparePriceService {

    // 方案三：使用 CompletableFuture 进一步增强并行
    public PriceResult getCheapestPlatformPrice3(String productName) {
        // 获取淘宝平台的价格和优惠
        CompletableFuture<PriceResult> taoBaoCF = CompletableFuture
                .supplyAsync(() -> HttpRequest.getTaoBaoPrice(productName))
                .thenCombine(CompletableFuture.supplyAsync(() -> HttpRequest.getTaoBaoDiscount(productName)), this::computeRealPrice);

        // 获取京东平台的价格和优惠
        CompletableFuture<PriceResult> jDongCF = CompletableFuture
                .supplyAsync(() -> HttpRequest.getJDongPrice(productName))
                .thenCombine(CompletableFuture.supplyAsync(() -> HttpRequest.getJDongDiscount(productName)), this::computeRealPrice);
        // 获取拼多多平台的价格和优惠
        CompletableFuture<PriceResult> pddCF = CompletableFuture
                .supplyAsync(() -> HttpRequest.getPDDPrice(productName))
                .thenCombine(CompletableFuture.supplyAsync(() -> HttpRequest.getPDDDiscount(productName)), this::computeRealPrice);

        return Stream.of(taoBaoCF,jDongCF,pddCF)
                .map(CompletableFuture::join)
                .min(Comparator.comparing(PriceResult::getRealPrice))
                .get();

    }
}
```

使用CompletableFuture方案的测试类

```java
public class ComparePriceDemo {
    public static void main(String[] args) {
        ComparePriceService service = new ComparePriceService();

        // 方案三测试：使用 CompletableFuture 进一步增强并行
        long start = System.currentTimeMillis();
        PriceResult priceResult = service.getCheapestPlatformPrice3("iPhone");
        long end = System.currentTimeMillis();

        double costTime = (end - start) / 1000.0;
        System.out.printf("cost %.2f second \n",costTime);

        System.out.println("priceResult = " + priceResult);
    }
}
```



### 4.7 Stream API 操作批量商品比价

```java
public class ComparePriceService {

    public PriceResult batchComparePrice(List<String> products) {
        // 遍历每个商品，根据商品开启异步任务获取最终价，然后归集到List集合
        List<CompletableFuture<PriceResult>> completableFutures = products.stream()
                .map(product -> {
                    return CompletableFuture
                            .supplyAsync(() -> HttpRequest.getTaoBaoPrice(product))
                            .thenCombine(CompletableFuture.supplyAsync(() -> HttpRequest.getTaoBaoDiscount(product)), this::computeRealPrice);
                }).collect(Collectors.toList());

        // 把多个商品的最终价进行排序比较获取最小值
        return completableFutures
                .stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(PriceResult::getRealPrice))
                .findFirst()
                .get();
    }

}
```

批量商品比价查询测试类

```java
public class ComparePriceDemo {
    public static void main(String[] args) {
        ComparePriceService service = new ComparePriceService();
        // 测试在一个平台比较同款产品(iPhone14)不同色系的价格
        List<String> products = Arrays.asList("iPhone14黑色", "iPhone14白色", "iPhone14玫瑰红");
        PriceResult priceResult = service.batchComparePrice(products);
        System.out.println("priceResult = " + priceResult);
    }
}
```



