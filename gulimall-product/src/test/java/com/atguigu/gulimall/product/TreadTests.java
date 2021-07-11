package com.atguigu.gulimall.product;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CompletableFuture 异步编排
 */
@Slf4j
@SpringBootTest
public class TreadTests {

    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);

    @Test
    public void anyOfDemo() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                log.info("任务1开始执行....");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 10;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                log.info("任务2开始执行....");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 20;
        });
        CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> {
            try {
                log.info("任务3开始执行....");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 30;
        });
        CompletableFuture<Object> future = CompletableFuture.anyOf(future1, future2, future3);
        // get 方法阻塞等待所有任务执行完毕，future3 最先执行，返回值为 30
        log.info("子任务全部执行后继续执行>>>>>" + future.get());
    }

    @Test
    public void allOfDemo() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            log.info("任务1开始执行....");
            return 10;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            log.info("任务2开始执行....");
            return 20;
        });
        CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> {
            log.info("任务3开始执行....");
            return 30;
        });
        CompletableFuture<Void> future = CompletableFuture.allOf(future1, future2, future3);
        // 阻塞等待所有任务执行完毕
        future.get();
        log.info("子任务全部执行后继续执行>>>>>");
    }

    @Test
    public void applyToEither() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                log.info("任务1开始执行....");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 10;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                log.info("任务2开始执行....");
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 20;
        });
        // R apply(T t);
        // future1 或 future2 谁先执行完成，就是谁的结果值
        CompletableFuture<Integer> future = future1.applyToEither(future2, res -> {
            log.info("任务3开始执行...." + res);
            return res * 10;
        });
        log.info("执行结果：" + future.get());
    }

    @Test
    public void runAfterBoth() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            return 10;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            return 20;
        });
        // void run();
        CompletableFuture<Void> future = future1.runAfterBoth(future2, () -> {
            log.info("start ....");
        });
    }

    @Test
    public void thenAcceptBoth() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            return 10;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            return 20;
        });
        // void accept(T t, U u);
        CompletableFuture<Void> future = future1.thenAcceptBoth(future2, (res1, res2) -> {
            log.info("res1:{}", res1);
            log.info("res2:{}", res2);
        });
    }

    @Test
    public void thenCombine() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            return 10;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            return 20;
        });
        // R apply(T t, U u);
        CompletableFuture<Integer> future = future1.thenCombine(future2, (res1, res2) -> {
            log.info("res1:{}", res1);
            log.info("res2:{}", res2);
            return res1 + res2;
        });
        log.info("res1 + res2 = " + future.get());
    }

    @Test
    public void createDemo() throws ExecutionException, InterruptedException {

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            log.info("runAsync...");
        }, threadPool);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            log.info("supplyAsync...");
            return "哈哈哈哈";
        });
        log.info("任务执行结果：{}", future2.get());

    }

    @Test
    public void callbackDemo() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            int i = 1 / 0;
            return "任务1结果";
        });
        /**
         * whenComplete 接收参数为函数接口接口 BiConsumer
         * BiConsumer 定义有 void accept(T t, U u); 该方法
         * t 表示调用 whenComplete 的任务执行返回结果
         * u 表示调用 whenComplete 的任务执行返回异常
         */
        CompletableFuture<String> future = future1.whenComplete((res, ex) -> {
            log.info("future1执行结果：{}", res);
            log.info("future1执行异常：{}", ex.getMessage());
        }).exceptionally((ex) -> {
            log.info("exceptionally 仅负责处理 future1 的异常信息");
            return ex.getMessage();
        });
        log.info("end ==========>>" + future.get());
    }

    @Test
    public void callbackHandleDemo() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            return "任务1结果";
        });
        // R apply(T t, U u);
        CompletableFuture<String> future = future1.handle((res, ex) -> {
            if (res != null) {
                log.info("future1执行结果：{}", res);
            }
            if (ex != null) {
                log.info("future1执行异常：{}", ex.getMessage());
            }
            return "新的结果";
        });
        log.info("end ==========>>" + future.get());
    }

    @Test
    public void thenApplyDemo() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            return "任务1结果";
        });
        // R apply(T t);
        CompletableFuture<String> future = future1.thenApply(t -> {
            // 上一个任务的执行结果：任务1结果
            log.info("上一个任务的执行结果：{}", t);
            return "新的执行结果";
        });
        // 新的任务执行结果：新的执行结果
        log.info("新的任务执行结果：{}", future.get());
    }

    @Test
    public void thenAcceptDemo() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            return "任务1结果";
        });
        // void accept(T t);
        future1.thenAccept(t -> {
            log.info("上一个任务的执行结果：{}", t);
        });
    }

    @Test
    public void thenRunDemo() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            return "任务1结果";
        });
        // void run();
        future1.thenRun(() -> {
            log.info("future1 执行完后继续执行");
        });
    }

}
