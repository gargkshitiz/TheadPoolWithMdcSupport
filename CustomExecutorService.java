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

import com.testorg.soa.dao.ActivityCodeModalDao;
import com.testorg.soa.dao.CustomTransactionManager;
import com.testorg.soa.dao.ModalActivityDao;
import com.testorg.soa.dao.ModalInstanceDao;
import com.testorg.soa.dao.PublicModalActivityDao;
import com.testorg.soa.dao.PublicUserActivityCountDao;
import com.testorg.soa.dao.UserActivityCountDao;
import com.testorg.soa.service.impl.ModalActivityRecordTask;
import com.testorg.soa.service.impl.UserActivityRecordTask;
/**
 * 
 * @author kgarg
 *
 */
@Service
public class CustomExecutorService {
	private ExecutorService backgroundExecutor;
	private static final Logger log = LoggerFactory.getLogger(CustomExecutorService.class);
	
	@Value("${activityCapturerThreadsCount}")
	private int activityCapturerThreadsCount;
	
    private BasicThreadFactory threadFactory;

	@Autowired
	private ActivityCodeModalDao activityCodeModalDao; 
	
	@Autowired
	private PublicUserActivityCountDao publicUserActivityCountDao;
	
	@Autowired
	private CustomTransactionManager customTransactionManager;
	
	@Autowired
	private UserActivityCountDao userActivityCountDao;
	
	@Autowired
	private ModalInstanceDao modalInstanceDao;
	
	@Autowired
	private ModalActivityDao modalActivityDao;
	
	@Autowired
	private PublicModalActivityDao publicModalActivityDao;
	
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
        backgroundExecutor = MdcThreadPoolExecutor.newWithInheritedMdc(activityCapturerThreadsCount,threadFactory);
        log.info("Created thread pool for recording activities with pool size: {}", activityCapturerThreadsCount);
    }
    
	public Future<?> submit(UserActivityRecordTask task) {
		task.setActivityCodeModalDao(activityCodeModalDao);
		task.setCustomTransactionManager(customTransactionManager);
		task.setUserActivityCountDao(userActivityCountDao);
		task.setPublicUserActivityCountDao(publicUserActivityCountDao);
		return backgroundExecutor.submit(task);
	}

	public Future<?> submit(ModalActivityRecordTask task) {
		task.setCustomTransactionManager(customTransactionManager);
		task.setModalActivityDao(modalActivityDao);
		task.setModalInstanceDao(modalInstanceDao);
		task.setPublicModalActivityDao(publicModalActivityDao);
		task.setPublicUserActivityCountDao(publicUserActivityCountDao);
		task.setUserActivityCountDao(userActivityCountDao);
		return backgroundExecutor.submit(task);
	}

}
