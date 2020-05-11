package net.chensee.task;

import lombok.extern.slf4j.Slf4j;
import net.chensee.common.Constants;
import net.chensee.common.DateUtil;
import net.chensee.entity.po.BaseProcess;
import net.chensee.service.VisualService;

import java.util.*;

@Slf4j
public class SimpleTaskGroup implements TaskGroup {

    private VisualService visualService;
    private TaskGroup taskGroup;
    private String name;
    private List<Task> tasks = new ArrayList<Task>();

    private Date startTime;
    private Date endTime;

    public SimpleTaskGroup(String name, VisualService visualService) {
        this.name = name;
        this.visualService = visualService;
    }

    @Override
    public void addTask(Task task) {
        tasks.add(task);
        task.setTaskGroup(this);
    }

    @Override
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    @Override
    public void removeTask(Task task) {
        tasks.remove(task);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void init() {
        startTime = new Date();
        log.error("{}  {}开始时间为：{}", Thread.currentThread().getName(), this.getName(), DateUtil.getNYRSFMDateStr(new Date()));
    }

    @Override
    public void execute() throws Exception {
        this.finish();
    }

    @Override
    public void finish() throws Exception {
        log.error("{}  {}结束时间为：{}", Thread.currentThread().getName(), this.getName(), DateUtil.getNYRSFMDateStr(new Date()));
        endTime = new Date();
        BaseProcess baseProcess = new BaseProcess();
        baseProcess.setId(UUID.randomUUID().toString());
        if (this.taskGroup != null) {
            baseProcess.setTaskGroupName(this.taskGroup.getName());
        } else {
            baseProcess.setTaskGroupName(this.name);
            baseProcess.setChanceValue(Constants.calculateCardsChanceValue);
        }
        baseProcess.setTaskName(this.name);
        baseProcess.setStartTime(this.startTime);
        baseProcess.setEndTime(this.endTime);
        baseProcess.setDataTime(Constants.queryTime);
        visualService.addProcess(baseProcess);
    }

    @Override
    public void setTaskGroup(TaskGroup taskGroup) {
        this.taskGroup = taskGroup;
    }

    @Override
    public TaskGroup getTaskGroup() {
        return taskGroup;
    }

}
