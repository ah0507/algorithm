package net.chensee.thread;

import net.chensee.dao.mongo.MongoDao;
import net.chensee.entity.po.LoggingPo;
import net.chensee.exception.CarTrailNullException;
import net.chensee.exception.ConsumeDataException;
import net.chensee.task.Task;

import java.util.Date;
import java.util.concurrent.CountDownLatch;


public class TaskCommand implements Runnable {

    private final CountDownLatch countDownLatch;

    private final Task task;

    private MongoDao mongoDao;

    public TaskCommand(CountDownLatch countDownLatch, Task task, MongoDao mongoDao) {
        this.countDownLatch = countDownLatch;
        this.task = task;
        this.mongoDao = mongoDao;
    }

    @Override
    public void run() {
        try {
            task.execute();
            task.finish();
        } catch (Exception e) {
            e.printStackTrace();
            LoggingPo loggingPo = new LoggingPo();
            if (e instanceof ConsumeDataException) {
                loggingPo.setExceptionName(ConsumeDataException.class.getSimpleName());
                ThreadService.ExceptionClose();
            } else if (e instanceof CarTrailNullException) {
                loggingPo.setExceptionName(CarTrailNullException.class.getSimpleName());
            }
            addLog(loggingPo,e);
        }  finally {
            countDownLatch.countDown();
        }
    }

    private void addLog(LoggingPo loggingPo, Exception e) {
        loggingPo.setExceptionContent(e.getMessage());
        loggingPo.setCreateTime(new Date());
        loggingPo.setExceptionDetail(e.fillInStackTrace().toString());
        mongoDao.addLoggingPo(loggingPo);
    }

}
