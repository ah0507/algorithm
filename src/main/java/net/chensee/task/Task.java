package net.chensee.task;

/**
 * @program: base-auth
 * @description: 任务
 * @author: xx
 * @create: 2019-06-11 16:48
 */
public interface Task {

    String getName();

    void setName(String name);

    void init();

    void execute() throws Exception;

    void finish() throws Exception;

    TaskGroup getTaskGroup();

    void setTaskGroup(TaskGroup taskGroup);

}
