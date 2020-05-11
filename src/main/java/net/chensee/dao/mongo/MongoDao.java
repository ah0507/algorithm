package net.chensee.dao.mongo;

import net.chensee.entity.po.*;
import net.chensee.entity.po.history.HistoryCard;
import net.chensee.entity.po.history.HistoryCardCopy;

import java.util.Date;
import java.util.List;

public interface MongoDao {

    void addRecentStationRanges(List<RecentStationRange> recentStationRanges);

    void addPersonPayRecords(List<PersonPayRecord> personPayRecords);

    List<PersonPayRecordInfo> getPersonPayRecordInfos(int pageNumber, int pageSize);

    List<RecentStationRange> getRecentStationRanges(int pageNumber, int pageSize,double distanceValue);

    void addLineStations(List<LineStation> lineStations);

    boolean isExistRecentStationRange(double distanceValue);

    void delLineStations();

    void addCards(List<Card> cardList);

    List<Card> getCardsByAsc(int pageNumber, int pageSize, Date currentNYRDate);

    void createCardPayTimeIndex();

    List<HistoryCard> getHistoryCardsByBusInfo(String userId, Date beforeDate, Date nowDate, String lineNo, String direction);

    void addNoMatchOutTimeCards(List<NoMatchOutTimeCard> noMatchOutTimeCards);

    List<NoMatchOutTimeCard> getNoMatchOutTimeCardsDesc(int pageNumber, int pageSize);

    void delNoMatchOutTimeCardsCreateIndex();

    void createRecentStationRangeIndex();

    void delPersonPayRecords();

    ConfigProperties getConfigProperties();

    List<HistoryCard> getHistoryCards(int pageNumber, int pageSize, String queryTime);

    void addHistoryCards(List<HistoryCard> historyCards);

    void addCarTrails(List<CarTrailMap> cardList);

    void delCarTrails();

    List<CarTrailMap> getCarTrails(int pageNumber, int pageSize);

    void addStationDirectionPo(List<StationDirectionPo> stationDirectionPos);

    List<StationDirectionPo> getStationDirectionPos(int page, int pageSize);

    List<LineStation> getLineStations();

    List<StationGPSData> getStationGPSDatas(int page, int pageSize);

    void addStationGPSDatas(List<StationGPSData> gpsDatas);

    void delStationGPSDatas();

    void removeExpireHistoryCards(Date expireTime);

    void removeExpireRecentStationRanges(Date expireTime);

    void addPersonPayRecordInfo(PersonPayRecordInfo ppr);

    void delPersonPayRecordInfos();

    List<PersonPayRecord> getPersonPayRecords(int page, int pageSize);

    void addUserStatistics(UserStatistics userStatistics);

    void delUserStatistics();

    List<UserStatistics> getUserStatistics(int page, int pageSize);

    long getPersonPayRecordsSize();

    void delStationDirectionPo();

    void delHistoryCards(Date queryTime);

    void addLoggingPo(LoggingPo loggingPo);

    void addHistoryCardCopies(List<HistoryCardCopy> historyCardCopies);

    void createBusRunningNumberPoIndex();

    void addBusRunningNumberPos(List<BusRunningNumberPo> busRunningNumberPoList);

    List<BusRunningNumberPo> getBusRunningNumberPo(List<String> busNoList, String lineNo, String direction, Date queryTime);

    List<BusRunningNumberPo> getBusRunningNumberPoByBusNos(List<String> busNoList, Date queryTime);

    void addCompanyToLinePos(List<CompanyToLinePo> ctpList);

    List<CompanyToLinePo> getCompanyToLinePos();

    Long getPersonPayRecordCount();

    void delHistoryCardCopiess(Date queryTime);

    void delOldBusRunningNumberPos(Date queryTime);

    void addTempCalculateTaskPo(TempCalculateTaskPo tctp);

    TempCalculateTaskPo getTempCalculateTaskPoByStatus();

    void updateTempCalculateTaskPoStatus(String id);
}
