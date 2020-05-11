package net.chensee.scheduledTasks;

import lombok.extern.slf4j.Slf4j;
import net.chensee.common.Constants;
import net.chensee.common.DateUtil;
import net.chensee.dao.ehcache.CarTrailEhCache;
import net.chensee.dao.ehcache.LineStationEhCache;
import net.chensee.dao.ehcache.RecentStationRageEhCache;
import net.chensee.dao.ehcache.StationDirectionEhcache;
import net.chensee.dao.mongo.MongoDao;
import net.chensee.entity.po.CompanyToLinePo;
import net.chensee.entity.po.ConfigProperties;
import net.chensee.entity.po.LoggingPo;
import net.chensee.entity.po.TempCalculateTaskPo;
import net.chensee.exception.ParamsException;
import net.chensee.service.BusService;
import net.chensee.service.MongoService;
import net.chensee.service.OracleService;
import net.chensee.service.VisualService;
import net.chensee.task.*;
import net.chensee.thread.ThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ah
 * @title: 定时任务
 * @date 2019/12/30 11:50
 */
@Component
@Slf4j
public class CronTask {

    @Autowired
    private BusService busService;
    @Autowired
    private VisualService visualService;
    @Autowired
    private MongoDao mongoDao;
    @Autowired
    private CarTrailEhCache carTrailEhCache;
    @Autowired
    private RecentStationRageEhCache recentStationRageEhCache;
    @Autowired
    private StationDirectionEhcache stationDirectionEhcache;
    @Autowired
    private LineStationEhCache lineStationEhCache;

    @Autowired
    private OracleService oracleService;

    @Autowired
    private MongoService mongoService;

    /**
     * 每次只执行一次大任务
     */
    private AtomicInteger count = new AtomicInteger(0);

    /**
     * od算法分析入口
     * fixedDelay任务执行完后10秒再进行下一次执行
     */
    @Scheduled(fixedDelay = 10000)
    private void analyseTask() {
        boolean b = count.compareAndSet(0, count.get());
        if (!b) {
            return;
        }
        Date nowDate = new Date();
        ConfigProperties config = visualService.getConfig(nowDate);
        if (config == null) {
            ParamsException e = new ParamsException("初始化参数未设置异常!");
            LoggingPo loggingPo = new LoggingPo();
            loggingPo.setExceptionName(ParamsException.class.getName());
            loggingPo.setExceptionContent(e.getMessage());
            loggingPo.setCreateTime(nowDate);
            mongoService.addLoggingPo(loggingPo);
            return;
        }
        Constants.configProperties = config;
        String calculateHandleTime = config.getCalculateHandleTime();
        String nyrDateStr = DateUtil.getNYRDateStr(nowDate);
        Date calculateTime = DateUtil.getNYRSFMDateByStr(nyrDateStr + " " + calculateHandleTime);

        Date queryTime = DateUtil.setDateDay(nowDate, -1);
        String queryTimeStr = DateUtil.getNYRDateStr(queryTime);

        TempCalculateTaskPo tct = mongoService.getTempCalculateTaskPoByStatus();
        if (tct != null && tct.getExecuteTime().getTime() <= nowDate.getTime()) {
            this.calculateTask(tct.getQueryTime(),tct.getId());
//            mongoService.updateTempCalculateTaskPoStatus(tct.getId());
            return;
        }

        if (calculateTime.getTime() <= nowDate.getTime()) {
            boolean existBaseProcess = visualService.isExistBaseProcess(queryTimeStr);
            if (!existBaseProcess) {
                this.calculateTask(queryTimeStr,null);
            }
        }
    }

    private void calculateTask(String queryTime,String tempTaskId) {
        count.getAndIncrement();
        Constants.queryTime = queryTime;
        visualService.delOldProcesses(queryTime);
        log.error("开始时间：{}", DateUtil.getNYRSFMDateStr(new Date()));
        SimpleTaskGroup root = new SimpleTaskGroup("root", visualService);
        SimpleTaskGroup taskGroup1 = new SimpleTaskGroup("基础数据预处理", visualService);
        SimpleTaskGroup taskGroup2 = new SimpleTaskGroup("计算上车信息", visualService);
        OffStationTaskGroup offStationTaskGroup = new OffStationTaskGroup("计算下车信息");
        offStationTaskGroup.setBusService(busService);
        offStationTaskGroup.setVisualService(visualService);
        offStationTaskGroup.setMongoDao(mongoDao);
        offStationTaskGroup.setCarTrailEhCache(carTrailEhCache);
        offStationTaskGroup.setLineStationEhCache(lineStationEhCache);
        offStationTaskGroup.setRecentStationRageEhCache(recentStationRageEhCache);
        offStationTaskGroup.setStationDirectionEhcache(stationDirectionEhcache);
        SimpleTaskGroup taskGroup4 = new SimpleTaskGroup("缓存清除和历史数据存储", visualService);
        SimpleTaskGroup taskGroup5 = new SimpleTaskGroup("公交数据分析", visualService);
        SimpleTaskGroup taskGroup11 = new SimpleTaskGroup("taskGroup11", visualService);
        root.addTask(taskGroup1);
        root.addTask(taskGroup2);
        root.addTask(offStationTaskGroup);
        root.addTask(taskGroup4);
        root.addTask(taskGroup5);
        taskGroup1.addTask(taskGroup11);
        Task carTrailTask = new CarTrailTask("运调数据任务", busService, visualService);
        Task cardTask = new CardTask("消费数据任务", busService, visualService);
        Task lineStationTask = new LineStationTask("线路站点任务", busService, visualService);
        Task stationDirectionTask = new StationDirectionTask("线路方向任务", busService, visualService);
        Task recentStationRangeTask = new RecentStationRangeTask("最近站距表任务", busService, visualService);
        Task calculateOnStationTask = new CalculateOnStationTask("计算上车任务", busService, visualService);
        Task historyDataTask = new HistoryDataTask("卡和消费数据增加到历史表", busService, visualService);
        Task analyseTask = new AnalyseTask("数据分析", busService, visualService);
        taskGroup11.addTask(carTrailTask);
        taskGroup1.addTask(cardTask);
        taskGroup1.addTask(stationDirectionTask);
        taskGroup1.addTask(lineStationTask);
        taskGroup1.addTask(recentStationRangeTask);
        taskGroup2.addTask(calculateOnStationTask);
        taskGroup4.addTask(historyDataTask);
        taskGroup5.addTask(analyseTask);

        ThreadService threadService = new ThreadService(count);
        threadService.setMongoDao(mongoDao);
        threadService.setVisualService(visualService);
        threadService.execute(root,tempTaskId);
    }

    /**
     * 公司线路对应表
     * 每隔月
     */
    @Scheduled(cron = "0 0 1 15 * ?")
    private void companyToLineTask() {
        List<CompanyToLinePo> companyToLinePos = oracleService.getCompanyToLinePos();
        Map<String, CompanyToLinePo> map = new HashMap<>();
        for (CompanyToLinePo cp : companyToLinePos) {
            CompanyToLinePo ctl = map.get(cp.getDeptNo());
            if (ctl == null) {
                ctl = new CompanyToLinePo();
            }
            List<String> lineNos = ctl.getLineNos();
            if (lineNos == null) {
                lineNos = new ArrayList<>();
            }
            if (!lineNos.contains(cp.getLineNo())) {
                lineNos.add(cp.getLineNo());
            }
            ctl.setDeptNo(cp.getDeptNo());
            ctl.setLineNos(lineNos);
            map.put(cp.getDeptNo(), ctl);
        }
        List<CompanyToLinePo> ctpList = new ArrayList<>();
        for (String key : map.keySet()) {
            CompanyToLinePo companyToLinePo = map.get(key);
            ctpList.add(companyToLinePo);
        }
        mongoService.addCompanyToLinePos(ctpList);
    }

}
