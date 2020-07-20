package com.github.coco.utils;

import com.github.coco.support.BaseCallBack;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.*;

/**
 * @author Yan
 */
public class ThreadPoolHelper {
    /**
     * 线程处理密集型枚举
     */
    public enum TaskIntensiveEnum {
        /**
         * IO密集型
         */
        IO(1),
        /**
         * 巨量IO密集型
         */
        HUGE_IO(2),
        /**
         * CPU密集型
         */
        CPU(3);
        int code;

        TaskIntensiveEnum(int code) {
            this.code = code;
        }
    }

    public enum ProvideModeEnum {
        /**
         * 单线程
         */
        Single(1),
        /**
         * 自动适配
         */
        AUTO(2);
        int code;

        ProvideModeEnum(int code) {
            this.code = code;
        }
    }

    /**
     * 默认密集型为CPU密集型
     */
    private static final TaskIntensiveEnum DEFAULT_INTENSIVE = TaskIntensiveEnum.CPU;
    /**
     * 默认堵塞因子
     */
    private static final float DEFAULT_BLOCKING_FACTOR       = 0.85f;
    /**
     * 线程池核心数
     */
    private static final int CORE_POOL_SIZE                  = getCorePoolSize(DEFAULT_INTENSIVE);
    /**
     * 线程池最大数
     */
    private static final int MAX_POOL_SIZE                   = getMaxPoolSize(DEFAULT_INTENSIVE);
    /**
     * 线程活动保持时间
     */
    private static final long KEEP_ALIVE_TIME                = 2;
    /**
     * 线程活动时间单位
     */
    private static final TimeUnit TIME_UNIT                  = TimeUnit.SECONDS;
    /**
     * 任务队列最大容器大小
     */
    private static final int MAXIMUM_CAPACITY                = 1 << 10;
    /**
     * 有界堵塞任务队列容量
     */
    private static final int WORK_QUEUE_CAPACITY             = getWorkQueueCapacity(DEFAULT_INTENSIVE);

    /**
     * 线程名格式
     */
    private static final String THREAD_NAME_FORMAT           = "thread-task-%d";

    /**
     * 提供线程任务处理结果队列
     *
     * @param intensive
     * @param processCallBack
     * @param args
     * @param <T>
     * @return
     */
    public static <T, K> ConcurrentLinkedQueue<Future<T>> provideThreadTask(TaskIntensiveEnum intensive,
                                                                            BaseCallBack processCallBack,
                                                                            K... args) {
        return provideThreadTask(getCorePoolSize(intensive),
                                 getMaxPoolSize(intensive),
                                 KEEP_ALIVE_TIME,
                                 TIME_UNIT,
                                 getWorkQueueCapacity(intensive),
                                 THREAD_NAME_FORMAT,
                                 processCallBack,
                                 args);
    }

    /**
     * 提供线程任务处理结果队列
     *
     * @param processCallBack
     * @param args
     * @param <T>
     * @return
     */
    public static <T, K> ConcurrentLinkedQueue<Future<T>> provideThreadTask(BaseCallBack processCallBack,
                                                                            K... args) {
        return provideThreadTask(CORE_POOL_SIZE,
                                 MAX_POOL_SIZE,
                                 KEEP_ALIVE_TIME,
                                 TIME_UNIT,
                                 WORK_QUEUE_CAPACITY,
                                 THREAD_NAME_FORMAT,
                                 processCallBack,
                                 args);
    }

    /**
     * 提供线程任务处理结果队列
     *
     * @param corePoolSize
     * @param maxPoolSize
     * @param keepAliveTime
     * @param threadNameFormat
     * @param workQueueCapacity
     * @param timeUnit
     * @param processCallBack
     * @param args
     * @return
     */
    public static <T, K> ConcurrentLinkedQueue<Future<T>> provideThreadTask(int corePoolSize,
                                                                            int maxPoolSize,
                                                                            long keepAliveTime,
                                                                            TimeUnit timeUnit,
                                                                            int workQueueCapacity,
                                                                            String threadNameFormat,
                                                                            BaseCallBack processCallBack,
                                                                            K... args) {
        // 获取线程池
        ThreadPoolExecutor threadPool = provideThreadPool(corePoolSize,
                                                          maxPoolSize,
                                                          keepAliveTime,
                                                          timeUnit,
                                                          workQueueCapacity,
                                                          threadNameFormat);
        ConcurrentLinkedQueue<Future<T>> taskQueue = processCallBack.invoke(threadPool, args);
        threadPool.shutdown();
        return taskQueue;
    }

    /**
     * 提供线程池
     *
     * @param provideMode
     * @return
     */
    public static ThreadPoolExecutor provideThreadPool(ProvideModeEnum provideMode) {
        if (provideMode == ProvideModeEnum.AUTO) {
            return provideThreadPool(getCorePoolSize(DEFAULT_INTENSIVE),
                                     getMaxPoolSize(DEFAULT_INTENSIVE),
                                     KEEP_ALIVE_TIME,
                                     TIME_UNIT,
                                     getWorkQueueCapacity(DEFAULT_INTENSIVE),
                                     THREAD_NAME_FORMAT);
        }
        return provideThreadPool(1, 1, 2, TIME_UNIT, 3, THREAD_NAME_FORMAT);
    }

    /**
     * 提供线程池
     *
     * @param corePoolSize
     * @param maxPoolSize
     * @param keepAliveTime
     * @param timeUnit
     * @param workQueueCapacity
     * @param threadNameFormat
     * @return
     */
    public static ThreadPoolExecutor provideThreadPool(int corePoolSize,
                                                       int maxPoolSize,
                                                       long keepAliveTime,
                                                       TimeUnit timeUnit,
                                                       int workQueueCapacity,
                                                       String threadNameFormat) {
        // 有界堵塞任务队列
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(workQueueCapacity);
        // 线程工厂
        ThreadFactory threadFactory;
        if (StringUtils.isNotBlank(threadNameFormat)) {
            threadFactory = new ThreadFactoryBuilder().setNameFormat(threadNameFormat).build();
        } else {
            threadFactory = new ThreadFactoryBuilder().build();
        }
        // 线程池
        return new ThreadPoolExecutor(corePoolSize,
                                      maxPoolSize,
                                      keepAliveTime,
                                      timeUnit,
                                      workQueue,
                                      threadFactory);
    }

    /**
     * 获取核心线程数大小
     *
     * @param intensive
     * @return
     */
    private static int getCorePoolSize(TaskIntensiveEnum intensive) {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        switch (intensive) {
            case IO:
                return 2 * cpuCount;
            case HUGE_IO:
                return (int) (cpuCount / (1 - DEFAULT_BLOCKING_FACTOR));
            case CPU:
                return cpuCount + 1;
            default:
                return cpuCount;
        }
    }

    /**
     * 获取最大线程数大小
     *
     * @param intensive
     * @return
     */
    private static int getMaxPoolSize(TaskIntensiveEnum intensive) {
        return getCorePoolSize(intensive) * 2;
    }

    /**
     * 获取线程池工作任务队列容量大小
     *
     * @param intensive
     * @return
     */
    private static int getWorkQueueCapacity(TaskIntensiveEnum intensive) {
        int capacity = 2 * getCorePoolSize(intensive) - 1;
        capacity |= capacity >>> 1;
        capacity |= capacity >>> 2;
        capacity |= capacity >>> 4;
        capacity |= capacity >>> 8;
        capacity |= capacity >>> 16;
        return (capacity < 0) ? 1 : (capacity >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : capacity + 1;
    }
}
