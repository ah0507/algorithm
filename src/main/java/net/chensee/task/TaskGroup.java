package net.chensee.task;

import java.util.List;

/**
 * @program: base-auth
 * @description: 任务
 * @author: xx
 * @create: 2019-06-11 16:48
 */
public interface TaskGroup extends Task {

    void addTask(Task task);

    List<Task> getTasks();

    void removeTask(Task task);

}
