package com.atguigu.gulimall.product.web;

import com.alibaba.nacos.common.util.UuidUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 车库停车，一个信号量相当于一个车位
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        //获取一个信号量（车位），会持续等待
        // park.acquire();
        //  尝试获取一个信息量（车位），获取不到直接得到 false
        boolean b = park.tryAcquire();
        if (b) {
            // 执行业务
        } else {
            return "error";
        }
        return "ok=>" + b;
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release();// 释放一个信号量（车位）
        return "ok";
    }

    /**
     * 放假，锁门
     * 1班没人了，2
     * 5个班全部走完，我们可以锁大门
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        door.await(); //等待闭锁都完成
        return "放假了...";
    }

    @ResponseBody
    @GetMapping("/gogogo/{id}")
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();//计数减一；
        return id + "班的人都走了...";
    }

    /**
     * 测试 lock 锁的使用
     * @return
     */
    @RequestMapping("/hello")
    public String hello() {
        // 获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");

        //加锁，默认加的锁都是30s时间
        lock.lock();
        try {
            log.info("加锁成功，执行业务..." + Thread.currentThread().getId());
            Thread.sleep(15000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //3解锁
            log.info("释放锁..." + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    /**
     * 测试读写锁
     */
    @ResponseBody
    @RequestMapping("/writeValue")
    public String writeValue() {
        // 获取读写锁，并设置写锁
        String s = null;
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        RLock writeLock = readWriteLock.writeLock();
        try {
            log.info("成功加上写锁...");
            writeLock.lock();
            s = UuidUtils.generateUuid();
            Thread.sleep(20000);
            redisTemplate.opsForValue().set("value", s);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            writeLock.unlock();
        }
        return s;
    }

    @ResponseBody
    @RequestMapping("/readValue")
    public String readValue() {
        String s = null;
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        RLock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            log.info("获取读锁...");
            s = redisTemplate.opsForValue().get("value");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            readLock.unlock();
        }
        return s;
    }



    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        List<CategoryEntity> categoryEntities =  categoryService.getLevel1Categorys();
        model.addAttribute("categorys",categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }

}
