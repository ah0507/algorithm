package net.chensee.task;

import lombok.extern.slf4j.Slf4j;
import net.chensee.common.Constants;
import net.chensee.common.DateUtil;
import net.chensee.entity.po.BaseProcess;
import net.chensee.service.BusService;
import net.chensee.service.VisualService;

import java.util.Date;
import java.util.UUID;

@Slf4j
public abstract class AbstractTask implements Task {

    protected VisualService visualService;
    protected BusService busService;
    protected TaskGroup taskGroup;
    protected String name;
    protected Date startTime;
    protected Date endTime;

    @Override
    public void init() {
        startTime = new Date();
        log.error("{}开始时间：{}", this.name,DateUtil.getNYRSFMDateStr(new Date()));
    }



    @Override
    public void finish() throws Exception{
        log.error("{}  {}结束时间为：{}", Thread.currentThread().getName(), this.getName(), DateUtil.getNYRSFMDateStr(new Date()));
        if (this.name == null) {
            return;
        }
        endTime = new Date();
        BaseProcess baseProcess = new BaseProcess();
        baseProcess.setId(UUID.randomUUID().toString());
        baseProcess.setTaskGroupName(this.taskGroup.getName());
        baseProcess.setTaskName(this.name);
        baseProcess.setStartTime(this.startTime);
        baseProcess.setEndTime(this.endTime);
        baseProcess.setDataTime(Constants.queryTime);
        visualService.addProcess(baseProcess);
    }

    @Override
    public TaskGroup getTaskGroup() {
        return taskGroup;
    }

    @Override
    public void setTaskGroup(TaskGroup taskGroup) {
        this.taskGroup = taskGroup;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
