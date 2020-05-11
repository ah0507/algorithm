package net.chensee.task;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.chensee.dao.ehcache.CarTrailEhCache;
import net.chensee.dao.ehcache.LineStationEhCache;
import net.chensee.dao.ehcache.RecentStationRageEhCache;
import net.chensee.dao.ehcache.StationDirectionEhcache;
import net.chensee.dao.mongo.MongoDao;
import net.chensee.entity.po.PersonPayRecord;
import net.chensee.service.BusService;
import net.chensee.service.VisualService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
@Slf4j
public class CalculateOffTaskGroup implements TaskGroup {

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
    private int pageNumber;
    private int pageSize;

    private Date startTime;
    private Date endTime;

    public CalculateOffTaskGroup(int pageNumber,int pageSize) {
        this.pageNumber = pageNumber+1;
        this.pageSize = pageSize;
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
        this.addTasks();
    }

    @Override
    public void execute() {
        this.finish();
    }

    @Override
    public void finish() {
    }

    @Override
    public void setTaskGroup(TaskGroup taskGroup) {
        this.taskGroup = taskGroup;
    }

    @Override
    public TaskGroup getTaskGroup() {
        return taskGroup;
    }

    private void addTasks() {
        List<PersonPayRecord> personPayRecords = mongoDao.getPersonPayRecords(pageNumber, pageSize);
        for (PersonPayRecord personPayRecord : personPayRecords) {
            CalculateOffStationTask calculateOffStationTask = new CalculateOffStationTask(personPayRecord);
            calculateOffStationTask.setMongoDao(mongoDao);
            calculateOffStationTask.setCarTrailEhCache(carTrailEhCache);
            calculateOffStationTask.setLineStationEhCache(lineStationEhCache);
            calculateOffStationTask.setRecentStationRageEhCache(recentStationRageEhCache);
            calculateOffStationTask.setStationDirectionEhcache(stationDirectionEhcache);
            this.tasks.add(calculateOffStationTask);
        }
    }
}
