package net.chensee.task;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.chensee.common.Constants;
import net.chensee.common.DateUtil;
import net.chensee.dao.ehcache.CarTrailEhCache;
import net.chensee.dao.ehcache.LineStationEhCache;
import net.chensee.dao.ehcache.RecentStationRageEhCache;
import net.chensee.dao.ehcache.StationDirectionEhcache;
import net.chensee.dao.mongo.MongoDao;
import net.chensee.entity.po.BaseProcess;
import net.chensee.service.BusService;
import net.chensee.service.VisualService;

import java.util.*;

@Data
@Slf4j
public class OffStationTaskGroup implements TaskGroup {

    private VisualService visualService;
    private BusService busService;
    private MongoDao mongoDao;
    private CarTrailEhCache carTrailEhCache;
    private RecentStationRageEhCache recentStationRageEhCache;
    private StationDirectionEhcache stationDirectionEhcache;
    private LineStationEhCache lineStationEhCache;

    private TaskGroup taskGroup;
    private String name;
    private List<Task> tasks = new ArrayList<Task>();

    private Date startTime;
    private Date endTime;

    public OffStationTaskGroup(String name) {
        this.name = name;
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
        log.error("{}开始时间：{}", this.name,DateUtil.getNYRSFMDateStr(new Date()));
        startTime = new Date();
        this.getCaches();
        this.addOffStationTasks();
    }

    private void getCaches() {
        mongoDao.delPersonPayRecordInfos();
        mongoDao.delUserStatistics();
        busService.addRecentStationRanges();
        busService.addCarTrailCache();
        busService.addStationDirectionCache();
        busService.addLineStationCache();
    }

    private void addOffStationTasks() {
        long personPayRecordsSize = busService.getPersonPayRecordsSize();
        Integer pageSize = Constants.configProperties.getPageSize();
        long pageCount;
        if (personPayRecordsSize % pageSize == 0) {
            pageCount = personPayRecordsSize / pageSize;
        } else {
            pageCount = personPayRecordsSize / pageSize+1;
        }
        for (int i = 0; i < pageCount; i++) {
            CalculateOffTaskGroup calculateOffTaskGroup = new CalculateOffTaskGroup(i,pageSize);
            calculateOffTaskGroup.setBusService(busService);
            calculateOffTaskGroup.setVisualService(visualService);
            calculateOffTaskGroup.setMongoDao(mongoDao);
            calculateOffTaskGroup.setCarTrailEhCache(carTrailEhCache);
            calculateOffTaskGroup.setLineStationEhCache(lineStationEhCache);
            calculateOffTaskGroup.setRecentStationRageEhCache(recentStationRageEhCache);
            calculateOffTaskGroup.setStationDirectionEhcache(stationDirectionEhcache);
            this.addTask(calculateOffTaskGroup);
        }
    }

    @Override
    public void execute() throws Exception {
        this.finish();
    }

    @Override
    public void finish() throws Exception {
        log.error("{}  {}结束时间为：{}", Thread.currentThread().getName(), this.getName(), DateUtil.getNYRSFMDateStr(new Date()));
        busService.handleCardsChanceValue();
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
