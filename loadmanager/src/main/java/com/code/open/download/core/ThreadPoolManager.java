package com.code.open.download.core;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ThreadPoolManager {
    private int corePoolSize;//核心线程池的数量，能够同时执行的线程数量
    private int maximumPoolSize;//最大线程池的数量
    private static ThreadPoolManager mInstance = new ThreadPoolManager();
    private ThreadPoolExecutor executor;

    private ThreadPoolManager() {

        //核心线程池的数量: 当前设备可用处理器的核数 + 1,这样的算法能够让cpu的效率得到最大程度的执行
        corePoolSize = 3;//Runtime.getRuntime().availableProcessors() + 1;
        maximumPoolSize = corePoolSize;//给最大线程池随便赋值
        executor = new ThreadPoolExecutor(
                corePoolSize, //3
                maximumPoolSize,//5，当缓冲队列满的时候，就会放入最大线程池，但是最大线程池是包含corePoolSize
                1,//表示最大线程池中的等待任务的存活时间
                TimeUnit.HOURS,
                new LinkedBlockingQueue<Runnable>(),//缓冲队列,超过corePoolSize的任务会放入缓冲队列等着
                Executors.defaultThreadFactory(), //创建线程的工厂
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static ThreadPoolManager getInstance() {
        return mInstance;
    }

    /**
     * 设置核心线程数量
     */
    public void setCorePoolSize(int num) {
        corePoolSize = num;
        executor.setCorePoolSize(corePoolSize);
    }

    /**
     * 设置线程池中最大线程数量
     */
    public void setMaximumPoolSize(int max) {
        if (max < corePoolSize) {
            throw new IllegalArgumentException();
        }
        maximumPoolSize = max;
        executor.setMaximumPoolSize(max);
    }

    /**
     * 添加要执行的任务
     *
     * @param command 任务
     */
    public void execute(Runnable command) {
        //强壮性检查
        if (command == null) return;
        executor.execute(command);
    }

    /**
     * 移除任务
     */
    public boolean remove(Runnable command) {
        //强壮性检查
        if (command == null) return true;
        return executor.remove(command);
    }
}
