package net.chensee.thread;

import lombok.Data;
import net.chensee.common.Constants;
import net.chensee.dao.mongo.MongoDao;
import net.chensee.service.VisualService;
import net.chensee.task.Task;
import net.chensee.task.TaskGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class ThreadService {

    private MongoDao mongoDao;

    private VisualService visualService;

    private static ThreadPoolExecutor threadPoolExecutor = null;

    private static AtomicInteger atomicInteger;

    public ThreadService(AtomicInteger ai) {
        threadPoolExecutor = new ThreadPoolExecutor(16, 16,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        atomicInteger = ai;
    }

    public void execute(final TaskGroup taskGroup, String tempTaskId) {
        threadPoolExecutor.execute(() -> {
            executeManager(taskGroup);
            updateTempTaskStatus(tempTaskId);
            close();
        });
    }

    private void updateTempTaskStatus(String tempTaskId) {
        boolean existBaseProcess = visualService.isExistBaseProcess(Constants.queryTime);
        if (tempTaskId != null && existBaseProcess) {
            mongoDao.updateTempCalculateTaskPoStatus(tempTaskId);
        }
    }

    private void executeManager(final TaskGroup taskGroup) {
        if (taskGroup == null || taskGroup.getTasks() == null) {
            return ;
        }
        taskGroup.init();
        final List<Task> tasks = taskGroup.getTasks();
        final List<Task> tasks2 = new ArrayList<>(tasks.size());
        Iterator<Task> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (TaskGroup.class.isAssignableFrom(task.getClass())) {
                executeManager(TaskGroup.class.cast(task));
            } else {
                task.init();
                tasks2.add(task);
            }
        }
        CountDownLatch countDownLatch = new CountDownLatch(tasks2.size());
        for (Task task : tasks2) {
            executeCommand(new TaskCommand(countDownLatch, task,mongoDao));
        }
        try {
            countDownLatch.await();
            taskGroup.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void executeCommand(TaskCommand taskCommand) {
        threadPoolExecutor.execute(taskCommand);
    }

    public void close() {
        try {
            atomicInteger.getAndDecrement();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void ExceptionClose() {
        try {
//            atomicInteger.getAndDecrement();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            threadPoolExecutor.shutdown();
        }
    }

}
