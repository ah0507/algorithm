//package net.chensee.scheduledTasks;
//
//import com.mongodb.lang.Nullable;
//import lombok.Setter;
//import net.chensee.common.Constants;
//import net.chensee.dao.ehcache.CarTrailEhCache;
//import net.chensee.dao.ehcache.LineStationEhCache;
//import net.chensee.dao.ehcache.RecentStationRageEhCache;
//import net.chensee.dao.ehcache.StationDirectionEhcache;
//import net.chensee.dao.mongo.MongoDao;
//import net.chensee.entity.po.ConfigProperties;
//import net.chensee.entity.po.LoggingPo;
//import net.chensee.exception.ParamsException;
//import net.chensee.service.BusService;
//import net.chensee.service.VisualService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.Trigger;
//import org.springframework.scheduling.TriggerContext;
//import org.springframework.scheduling.annotation.SchedulingConfigurer;
//import org.springframework.scheduling.config.ScheduledTaskRegistrar;
//import org.springframework.scheduling.support.CronTrigger;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//
//@Component
//@Setter
//public class ScheduledTasks implements SchedulingConfigurer {
//
//    @Autowired
//    private BusService busService;
//    @Autowired
//    private VisualService visualService;
//    @Autowired
//    private MongoDao mongoDao;
//    @Autowired
//    private CarTrailEhCache carTrailEhCache;
//    @Autowired
//    private RecentStationRageEhCache recentStationRageEhCache;
//    @Autowired
//    private StationDirectionEhcache stationDirectionEhcache;
//    @Autowired
//    private LineStationEhCache lineStationEhCache;
//
//    private String cron;
//
//    @Override
//    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//        handleConfigProperties();
//        StartCalculateTask startCalculateTask = new StartCalculateTask();
//        startCalculateTask.setBusService(busService);
//        startCalculateTask.setVisualService(visualService);
//        startCalculateTask.setMongoDao(mongoDao);
//        startCalculateTask.setCarTrailEhCache(carTrailEhCache);
//        startCalculateTask.setRecentStationRageEhCache(recentStationRageEhCache);
//        startCalculateTask.setStationDirectionEhcache(stationDirectionEhcache);
//        startCalculateTask.setLineStationEhCache(lineStationEhCache);
//        //每天早上0点30分  "0 30 0 * * ?"
//        taskRegistrar.addTriggerTask(startCalculateTask, new Trigger() {
//            @Nullable
//            @Override
//            public Date nextExecutionTime(TriggerContext triggerContext) {
//                // 任务触发，可修改任务的执行周期
//                CronTrigger trigger = new CronTrigger(cron);
//                Date nextExecutor = trigger.nextExecutionTime(triggerContext);
//                return nextExecutor;
//            }
//        });
//
//    }
//
//    private void handleConfigProperties() {
//        Date nowDate = new Date();
//        ConfigProperties config = visualService.getConfig(nowDate);
//        if (config == null) {
//            ParamsException e = new ParamsException("初始化参数未设置异常!");
//            LoggingPo loggingPo = new LoggingPo();
//            loggingPo.setExceptionName(ParamsException.class.getName());
//            loggingPo.setExceptionContent(e.getMessage());
//            loggingPo.setCreateTime(new Date());
//            mongoDao.addLoggingPo(loggingPo);
//            return;
//        }
//        cron = config.getCalculateHandleTime();
//        Constants.configProperties = config;
//    }
//
//}
//
//
