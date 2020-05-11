package net.chensee.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.chensee.common.Constants;
import net.chensee.common.ConvertUtil;
import net.chensee.common.DateUtil;
import net.chensee.common.DoubleHandleUtil;
import net.chensee.dao.ehcache.LineStationEhCache;
import net.chensee.dao.ehcache.RecentStationRageEhCache;
import net.chensee.dao.jdbc.OracleDao;
import net.chensee.dao.mongo.MongoDao;
import net.chensee.entity.po.*;
import net.chensee.entity.po.history.HistoryCard;
import net.chensee.entity.po.history.HistoryCardCopy;
import net.chensee.service.MongoService;
import net.chensee.service.OracleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.chensee.common.Constants.configProperties;
import static net.chensee.common.Constants.queryTime;

@Service
@Slf4j
public class MongoServiceImpl implements MongoService {

    @Autowired
    private MongoDao mongoDao;
    @Autowired
    private OracleDao oracleDao;
    @Autowired
    private OracleService oracleService;

    @Autowired
    private RecentStationRageEhCache recentStationRageEhCache;
    @Autowired
    private LineStationEhCache lineStationEhCache;

    private int allCardsCount = 0;
    private int completeCardsCount = 0;

    @Override
    public void addRecentStationRanges(List<RecentStationRange> recentStationRanges) throws Exception {
        if (recentStationRanges.size() != 0) {
            mongoDao.createRecentStationRangeIndex();
            int page = configProperties.getPageNumber();
            int pageSize = configProperties.getPageSize();
            while (true) {
                int startNum = (page - 1) * pageSize;
                int endNum = pageSize * page;
                if (startNum < recentStationRanges.size()) {
                    if (endNum > recentStationRanges.size()) {
                        endNum = recentStationRanges.size();
                    }
                    List<RecentStationRange> rsrList = recentStationRanges.subList(startNum, endNum);
                    page++;
                    mongoDao.addRecentStationRanges(rsrList);
                } else {
                    log.error("车辆站点距离总数据已插入成功！");
                    recentStationRanges.clear();
                    break;
                }
            }
        }
    }

    @Override
    public void addPersonPayRecords(List<PersonPayRecord> personPayRecords) throws Exception {
        if (personPayRecords.size() != 0) {
            mongoDao.delPersonPayRecords();
            int page = configProperties.getPageNumber();
            int pageSize = configProperties.getPageSize();
            while (true) {
                int startNum = (page - 1) * pageSize;
                int endNum = pageSize * page;
                if (startNum < personPayRecords.size()) {
                    if (endNum > personPayRecords.size()) {
                        endNum = personPayRecords.size();
                    }
                    List<PersonPayRecord> prrList = personPayRecords.subList(startNum, endNum);
                    page++;
                    mongoDao.addPersonPayRecords(prrList);
                } else {
                    log.error("每日每人刷卡总数据已插入成功！");
                    personPayRecords.clear();
                    break;
                }
            }
        }
    }

    @Override
    public void addLineStations() throws Exception {
        List<LineStation> lineStations = oracleDao.getLineStations();
        this.handleStationName(lineStations);
        mongoDao.delLineStations();
        mongoDao.addLineStations(lineStations);
        lineStationEhCache.batchRemove();
        lineStationEhCache.batchAdd(lineStations);
        lineStations.clear();
        log.error("站点线路总数据已插入成功！");
    }

    private void handleStationName(List<LineStation> lineStations) {
        for (LineStation lineStation : lineStations) {
            int direction = this.handleDirection(lineStation.getDirection());
            String startName = oracleDao.getStartStationName(lineStation.getLineNo(),direction,lineStation.getStartStation());
            String endName = oracleDao.getEndStationName(lineStation.getLineNo(), direction, lineStation.getEndStation());
            if (startName != null) {
                lineStation.setStartStationName(startName);
            }
            if (endName != null) {
                lineStation.setEndStationName(endName);
            }
        }
    }

    private Integer handleDirection(String direction) {
        Integer direct = null;
        if ("下行".equals(direction)) {
            direct = 1;
        } else if ("上行".equals(direction)) {
            direct = 0;
        }else{
        }
        return direct;
    }

    @Override
    public void addCards(List<Card> cards){
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        while (true) {
            int startNum = (page - 1) * pageSize;
            int endNum = pageSize * page;
            if (startNum < cards.size()) {
                if (endNum > cards.size()) {
                    endNum = cards.size();
                }
                List<Card> cardList = cards.subList(startNum, endNum);
                page++;
                mongoDao.addCards(cardList);
            } else {
                cards.clear();
                break;
            }
        }
    }

    @Override
    public void addHistoryCards() throws Exception {
        List<PersonPayRecordInfo> personPayRecordInfos;
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        mongoDao.delHistoryCards(DateUtil.getNYRDateByStr(queryTime));
        log.error("HistoryCards清除已有当天消费数据，当前时间时间：{}",DateUtil.getNYRSFMDateStr(new Date()));
        mongoDao.delHistoryCardCopiess(DateUtil.getNYRDateByStr(queryTime));
        log.error("HistoryCardCopiess清除已有当天消费数据，当前时间时间：{}",DateUtil.getNYRSFMDateStr(new Date()));
        List<Card> cards;
        List<HistoryCard> historyCards;
        List<HistoryCardCopy> historyCardCopies;
        while (true) {
            log.error("当前时间时间：{}",DateUtil.getNYRSFMDateStr(new Date()));
            personPayRecordInfos = mongoDao.getPersonPayRecordInfos(page, pageSize);
            if (personPayRecordInfos != null && personPayRecordInfos.size() != 0) {
                cards = new ArrayList<>();
                historyCards = new ArrayList<>();
                historyCardCopies = new ArrayList<>();
                log.error("personPayRecordInfos查询结束时间：{}",DateUtil.getNYRSFMDateStr(new Date()));
                this.handleToHistoryCards(cards, personPayRecordInfos, historyCards, historyCardCopies);
                log.error("handleToHistoryCards结束时间：{}",DateUtil.getNYRSFMDateStr(new Date()));
                mongoDao.addHistoryCards(historyCards);
                log.error("addHistoryCards结束时间：{}",DateUtil.getNYRSFMDateStr(new Date()));
                mongoDao.addHistoryCardCopies(historyCardCopies);
                log.error("addHistoryCardCopies结束时间：{}",DateUtil.getNYRSFMDateStr(new Date()));
                page++;
            } else {
                break;
            }
        }
        log.error("当前时间：{}",DateUtil.getNYRSFMDate(new Date()));
    }

    @Override
    public void addLoggingPo(LoggingPo loggingPo) {
        mongoDao.addLoggingPo(loggingPo);
    }

    private void handleToHistoryCards(List<Card> cards, List<PersonPayRecordInfo> personPayRecords, List<HistoryCard> historyCards, List<HistoryCardCopy> historyCardCopies) {
        for (PersonPayRecordInfo personPayRecordInfo : personPayRecords) {
            List<Card> cardList = personPayRecordInfo.getCardList();
            for (Card card : cardList) {
                card.setCreateTime(DateUtil.getNYRDateByStr(queryTime));
            }
            cards.addAll(cardList);
        }
        ConvertUtil.convertToHistoryCard(historyCards,cards);
        ConvertUtil.convertToHistoryCardCopy(historyCardCopies,cards);
    }

    @Override
    public ConfigProperties getConfigProperties() {
        return mongoDao.getConfigProperties();
    }

    @Override
    public void handleRecentStationRanges() throws Exception {
        boolean exitRecentStationRange = mongoDao.isExistRecentStationRange(configProperties.getDistanceValue());
        List<RecentStationRange> recentStationRangeList;
        if (exitRecentStationRange) {
            recentStationRangeList = this.getRecentStationRages();
        } else {
            recentStationRangeList = oracleService.getRecentStationRanges();
            this.addRecentStationRanges(recentStationRangeList);
        }
        if (recentStationRangeList != null && recentStationRangeList.size() != 0) {
            recentStationRageEhCache.batchRemove();
            recentStationRageEhCache.batchAdd(recentStationRangeList);
            recentStationRangeList.clear();
        }
    }

    @Override
    public void addCarTrails(List<CarTrailMap> carTrails) throws Exception {
        if (carTrails.size() != 0) {
            mongoDao.delCarTrails();
            int page = configProperties.getPageNumber();
            int pageSize = configProperties.getPageSize();
            while (true) {
                int startNum = (page - 1) * pageSize;
                int endNum = pageSize * page;
                if (startNum < carTrails.size()) {
                    if (endNum > carTrails.size()) {
                        endNum = carTrails.size();
                    }
                    List<CarTrailMap> cardList = carTrails.subList(startNum, endNum);
                    page++;
                    mongoDao.addCarTrails(cardList);
                } else {
                    log.error("车辆轨迹数据已插入成功！");
                    break;
                }
            }
        }

    }

    @Override
    public void addStationDirectionPo(List<StationDirectionPo> stationDirectionPos) throws Exception{
        mongoDao.delStationDirectionPo();
        mongoDao.addStationDirectionPo(stationDirectionPos);
    }

    @Override
    public void delPersonPayRecords() throws Exception {
        mongoDao.delPersonPayRecords();
    }

    @Override
    public List<StationGPSData> getStationGPSDatas() {
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        List<StationGPSData> stationGPSDataList = new ArrayList<>();
        List<StationGPSData> stationGPSDatas;
        while (true) {
            stationGPSDatas = mongoDao.getStationGPSDatas(page, pageSize);
            if (stationGPSDatas != null && stationGPSDatas.size() != 0) {
                stationGPSDataList.addAll(stationGPSDatas);
                page++;
            } else {
                break;
            }
        }
        return stationGPSDataList;
    }

    @Override
    public void addStationGPSDatas(List<StationGPSData> stationGPSDataList) {
        mongoDao.delStationGPSDatas();
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        while (true) {
            int startNum = (page - 1) * pageSize;
            int endNum = pageSize * page;
            if (startNum < stationGPSDataList.size()) {
                if (endNum > stationGPSDataList.size()) {
                    endNum = stationGPSDataList.size();
                }
                List<StationGPSData> gpsDatas = stationGPSDataList.subList(startNum, endNum);
                page++;
                mongoDao.addStationGPSDatas(gpsDatas);
            } else {
                log.error("车辆轨迹数据已插入成功！");
                break;
            }
        }
    }

    @Override
    public void removeExpireHistoryCards(Date expireTime) throws Exception{
        mongoDao.removeExpireHistoryCards(expireTime);
    }

    @Override
    public void removeExpireRecentStationRanges(Date expireTime) {
        mongoDao.removeExpireRecentStationRanges(expireTime);
    }

    @Override
    public void handleCardsChanceValue() {
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        List<UserStatistics> userStatisticsList;
        while (true) {
            userStatisticsList = mongoDao.getUserStatistics(page, pageSize);
            if (userStatisticsList != null && userStatisticsList.size() != 0) {
                this.handleUserStatistics(userStatisticsList);
                page++;
            } else {
                break;
            }
        }
        if (allCardsCount > 0) {
            double chanceValue = (double) completeCardsCount / allCardsCount;
            Constants.calculateCardsChanceValue = DoubleHandleUtil.convertTo3Decimal(chanceValue);
        } else {
            Constants.calculateCardsChanceValue = 0.0;
        }
        allCardsCount = 0;
        completeCardsCount = 0;
    }

    @Override
    public long getPersonPayRecordsSize() {
        return mongoDao.getPersonPayRecordsSize();
    }

    @Override
    public List<BusRunningNumberPo> getBusRunningNumberPo(List<String> busNoList, String lineNo, String direction, Date queryTime) {
        return mongoDao.getBusRunningNumberPo(busNoList,lineNo,direction,queryTime);
    }

    @Override
    public List<BusRunningNumberPo> getBusRunningNumberPoByBusNos(List<String> busNoList, Date queryTime) {
        return mongoDao.getBusRunningNumberPoByBusNos(busNoList,queryTime);
    }

    @Override
    public void addCompanyToLinePos(List<CompanyToLinePo> ctpList) {
        mongoDao.addCompanyToLinePos(ctpList);
    }

    @Override
    public List<CompanyToLinePo> getCompanyToLinePos() {
        return mongoDao.getCompanyToLinePos();
    }

    private void handleUserStatistics(List<UserStatistics> userStatisticsList) {
        for (UserStatistics userStatistics : userStatisticsList) {
            allCardsCount += userStatistics.getAllCardsCount();
            completeCardsCount += userStatistics.getCompleteCardsCount();
        }
    }

    public List<RecentStationRange> getRecentStationRages() {
        List<RecentStationRange> recentStationRangeList = new ArrayList<>(30000);
        List<RecentStationRange> recentStationRanges;
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        double distanceValue = configProperties.getDistanceValue();
        while (true) {
            recentStationRanges = mongoDao.getRecentStationRanges(page, pageSize, distanceValue);
            if (recentStationRanges != null && recentStationRanges.size() != 0) {
                recentStationRangeList.addAll(recentStationRanges);
                page++;
            } else {
                break;
            }
        }
        return recentStationRangeList;
    }

    @Override
    public Long getPersonPayRecordCount() {
        return mongoDao.getPersonPayRecordCount();
    }


    @Override
    public void addTempCalculateTaskPo(String queryTime, String executeTime) {
        TempCalculateTaskPo tctp = new TempCalculateTaskPo();
        tctp.setQueryTime(queryTime);
        tctp.setExecuteTime(DateUtil.getNYRSFMDateByStr(executeTime));
        tctp.setCreateTime(new Date());
        tctp.setStatus(0);
        mongoDao.addTempCalculateTaskPo(tctp);
    }

    @Override
    public TempCalculateTaskPo getTempCalculateTaskPoByStatus() {
        return mongoDao.getTempCalculateTaskPoByStatus();
    }

    @Override
    public void updateTempCalculateTaskPoStatus(String id) {
        mongoDao.updateTempCalculateTaskPoStatus(id);
    }
}
