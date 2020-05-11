//package net.chensee.scheduledTasks;
//
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import net.chensee.common.Constants;
//import net.chensee.common.DateUtil;
//import net.chensee.dao.ehcache.CarTrailEhCache;
//import net.chensee.dao.ehcache.LineStationEhCache;
//import net.chensee.dao.ehcache.RecentStationRageEhCache;
//import net.chensee.dao.ehcache.StationDirectionEhcache;
//import net.chensee.dao.mongo.MongoDao;
//import net.chensee.service.BusService;
//import net.chensee.service.VisualService;
//import net.chensee.task.*;
//import net.chensee.thread.ThreadService;
//
//import java.util.Date;
//
//@Slf4j
//@Data
//public class StartCalculateTask implements Runnable {
//
//    private BusService busService;
//
//    private VisualService visualService;
//
//    private MongoDao mongoDao;
//
//    private CarTrailEhCache carTrailEhCache;
//
//    private RecentStationRageEhCache recentStationRageEhCache;
//
//    private StationDirectionEhcache stationDirectionEhcache;
//
//    private LineStationEhCache lineStationEhCache;
//
//    @Override
//    public void run() {
//        Date nowDate = new Date();
//        Date queryDate = DateUtil.setDateDay(nowDate, -1);
//        Constants.queryTime = DateUtil.getNYRDateStr(queryDate);
//
//        visualService.delOldProcesses(Constants.queryTime);
//        log.error("开始时间：{}", DateUtil.getNYRSFMDateStr(new Date()));
//        SimpleTaskGroup root = new SimpleTaskGroup("root",visualService);
//        SimpleTaskGroup taskGroup1 = new SimpleTaskGroup("基础数据预处理",visualService);
//        SimpleTaskGroup taskGroup2 = new SimpleTaskGroup("计算上车信息",visualService);
//        OffStationTaskGroup offStationTaskGroup = new OffStationTaskGroup("计算下车信息");
//        offStationTaskGroup.setBusService(busService);
//        offStationTaskGroup.setVisualService(visualService);
//        offStationTaskGroup.setMongoDao(mongoDao);
//        offStationTaskGroup.setCarTrailEhCache(carTrailEhCache);
//        offStationTaskGroup.setLineStationEhCache(lineStationEhCache);
//        offStationTaskGroup.setRecentStationRageEhCache(recentStationRageEhCache);
//        offStationTaskGroup.setStationDirectionEhcache(stationDirectionEhcache);
//        SimpleTaskGroup taskGroup4 = new SimpleTaskGroup("缓存清除和历史数据存储",visualService);
//        SimpleTaskGroup taskGroup5 = new SimpleTaskGroup("公交数据分析",visualService);
//        SimpleTaskGroup taskGroup11 = new SimpleTaskGroup("taskGroup11",visualService);
//        root.addTask(taskGroup1);
//        root.addTask(taskGroup2);
//        root.addTask(offStationTaskGroup);
//        root.addTask(taskGroup4);
//        root.addTask(taskGroup5);
//        taskGroup1.addTask(taskGroup11);
//        Task carTrailTask = new CarTrailTask("运调数据任务",busService,visualService);
//        Task cardTask = new CardTask("消费数据任务",busService,visualService);
//        Task lineStationTask = new LineStationTask("线路站点任务",busService,visualService);
//        Task stationDirectionTask = new StationDirectionTask("线路方向任务",busService,visualService);
//        Task recentStationRangeTask = new RecentStationRangeTask("最近站距表任务",busService,visualService);
//        Task calculateOnStationTask = new CalculateOnStationTask("计算上车任务",busService,visualService);
//        Task historyDataTask = new HistoryDataTask("卡和消费数据增加到历史表", busService, visualService);
//        Task analyseTask = new AnalyseTask("数据分析", busService, visualService);
//        taskGroup11.addTask(carTrailTask);
//        taskGroup1.addTask(cardTask);
//        taskGroup1.addTask(stationDirectionTask);
//        taskGroup1.addTask(lineStationTask);
//        taskGroup1.addTask(recentStationRangeTask);
//        taskGroup2.addTask(calculateOnStationTask);
//        taskGroup4.addTask(historyDataTask);
//        taskGroup5.addTask(analyseTask);
//
//        ThreadService threadService = new ThreadService();
//        threadService.setMongoDao(mongoDao);
//        threadService.execute(root);
//    }
//}