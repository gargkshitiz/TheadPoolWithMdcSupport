package com.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * @author kgarg
 *
 */
@Service
public class CustomExecutorService {
	private ExecutorService backgroundExecutor;
	private static final Logger log = LoggerFactory.getLogger(CustomExecutorService.class);
	
	private BasicThreadFactory threadFactory;

	private Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler(){
        	@Override
        	public void uncaughtException(Thread thread, Throwable throwable) {
            		log.error("Exception in thread pool worker thread:"+thread.getName()+" :"+throwable.getMessage());
        	}
    	};

    	public CustomExecutorService(){
        	threadFactory = new BasicThreadFactory.Builder().uncaughtExceptionHandler(exceptionHandler).build();
	}
	
    	@PostConstruct
    	public void createExecutor(){
        	backgroundExecutor = MdcThreadPoolExecutor.newWithInheritedMdc(10,threadFactory);
        	log.info("Created thread pool with pool size: {}", 10);
    	}
    
	public Future<?> submit(Runnable task) {
		return backgroundExecutor.submit(task);
	}

}
