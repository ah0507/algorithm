package net.chensee.service;

import net.chensee.entity.po.*;

import java.util.Date;
import java.util.List;

public interface MongoService {

    void addRecentStationRanges(List<RecentStationRange> recentStationRanges) throws Exception;

    void addPersonPayRecords(List<PersonPayRecord> personPayRecords) throws Exception;

    void addLineStations() throws Exception;

    void addCards(List<Card> cardList);

    void addHistoryCards() throws Exception;

    void addLoggingPo(LoggingPo loggingPo);

    ConfigProperties getConfigProperties();

    void handleRecentStationRanges() throws Exception;

    void addCarTrails(List<CarTrailMap> carTrails) throws Exception;

    void addStationDirectionPo(List<StationDirectionPo> stationDirectionPos) throws Exception;

    void delPersonPayRecords() throws Exception;

    List<StationGPSData> getStationGPSDatas();

    void addStationGPSDatas(List<StationGPSData> stationGPSDataList);

    void removeExpireHistoryCards(Date expireTime) throws Exception;

    void removeExpireRecentStationRanges(Date expireTime) throws Exception;

    void handleCardsChanceValue();

    long getPersonPayRecordsSize();

    List<BusRunningNumberPo> getBusRunningNumberPo(List<String> busNoList, String lineNo, String direction, Date nyrsfmDateByStr);

    List<BusRunningNumberPo> getBusRunningNumberPoByBusNos(List<String> busNoList, Date queryTime);

    void addCompanyToLinePos(List<CompanyToLinePo> ctpList);

    List<CompanyToLinePo> getCompanyToLinePos();

    Long getPersonPayRecordCount();

    /**
     * 增加指定日期的临时算法任务
     * @param queryTime
     * @param executeTime
     */
    void addTempCalculateTaskPo(String queryTime, String executeTime);

    /**
     * 得到未执行的临时算法的一个任务(获得开始时间最早的任务)
     */
    TempCalculateTaskPo getTempCalculateTaskPoByStatus();

    /**
     * 执行完毕后将算法任务状态置为已执行
     * @param id
     */
    void updateTempCalculateTaskPoStatus(String id);
}
