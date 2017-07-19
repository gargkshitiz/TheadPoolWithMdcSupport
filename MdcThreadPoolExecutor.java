package com.test;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.*;
/**
 * 
 * @author kgarg
 *
 */
public class MdcThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * Pool where task threads take MDC from the submitting thread.
     */
    public static MdcThreadPoolExecutor newWithInheritedMdc(int nThreads, ThreadFactory threadFactory) {
        return new MdcThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
    }

    private MdcThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * All executions will have MDC injected. {@code ThreadPoolExecutor}'s submission methods ({@code submit()} etc.)
     * all delegate to this.
     */
    @Override
    public void execute(Runnable command) {
        super.execute(wrap(command, MDC.getCopyOfContextMap()));
    }

    private static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
        return new Runnable() {
            @Override
            public void run() {
            	if (context != null) {
            		MDC.setContextMap(context);
            	}
                try {
                    runnable.run();
                } 
                finally {
                	MDC.clear();
                }
            }
        };
    }

}
