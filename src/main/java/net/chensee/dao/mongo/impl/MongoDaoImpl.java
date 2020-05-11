package net.chensee.dao.mongo.impl;

import net.chensee.dao.mongo.MongoDao;
import net.chensee.entity.po.*;
import net.chensee.entity.po.history.HistoryCard;
import net.chensee.entity.po.history.HistoryCardCopy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public class MongoDaoImpl implements MongoDao {

    @Autowired
    @Qualifier("mongoTemplate")
    private MongoTemplate mongoTemplate;

    @Override
    public void addRecentStationRanges(List<RecentStationRange> recentStationRanges) {
        mongoTemplate.insertAll(recentStationRanges);
    }

    @Override
    public void addPersonPayRecords(List<PersonPayRecord> personPayRecords) {
        mongoTemplate.insertAll(personPayRecords);
    }

    @Override
    public List<PersonPayRecordInfo> getPersonPayRecordInfos(int pageNumber, int pageSize) {
        Query query = new Query();
        query.skip((pageNumber - 1) * pageSize).limit(pageSize);
        return mongoTemplate.find(query, PersonPayRecordInfo.class);
    }

    @Override
    public List<PersonPayRecord> getPersonPayRecords(int pageNumber, int pageSize) {
        Query query = new Query();
        query.skip((pageNumber - 1) * pageSize).limit(pageSize);
        return mongoTemplate.find(query, PersonPayRecord.class);
    }

    @Override
    public void addUserStatistics(UserStatistics userStatistics) {
        mongoTemplate.save(userStatistics);
    }

    @Override
    public void delUserStatistics() {
        mongoTemplate.dropCollection(UserStatistics.class);
    }

    @Override
    public List<UserStatistics> getUserStatistics(int page, int pageSize) {
        Query query = new Query();
        query.skip((page - 1) * pageSize).limit(pageSize);
        return mongoTemplate.find(query, UserStatistics.class);
    }

    @Override
    public long getPersonPayRecordsSize() {
        return mongoTemplate.count(new Query(),PersonPayRecord.class);
    }

    @Override
    public void delStationDirectionPo() {
        mongoTemplate.dropCollection(StationDirectionPo.class);
    }

    @Override
    public void delHistoryCards(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("createTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query,HistoryCard.class);
    }

    @Override
    public List<RecentStationRange> getRecentStationRanges(int pageNumber, int pageSize, double distanceValue) {
        Query query = new Query(Criteria.where("distanceValue").is(distanceValue));
        query.skip((pageNumber - 1) * pageSize).limit(pageSize);
        return mongoTemplate.find(query, RecentStationRange.class);
    }

    @Override
    public void addLineStations(List<LineStation> lineStations) {
        mongoTemplate.insertAll(lineStations);
        lineStations.clear();
    }

    @Override
    public boolean isExistRecentStationRange(double distanceValue) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("distanceValue").is(distanceValue);
        query.addCriteria(criteria);
        return mongoTemplate.exists(query, RecentStationRange.class);
    }

    @Override
    public void delLineStations() {
        mongoTemplate.dropCollection(LineStation.class);
    }

    @Override
    public void addCards(List<Card> cardList) {
        mongoTemplate.insertAll(cardList);
    }

    @Override
    public List<Card> getCardsByAsc(int pageNumber, int pageSize, Date currentNYRDate) {
        Query query = new Query();
//        Criteria criteria = new Criteria();
//        criteria.and("createTime").is(currentNYRDate);
//        query.addCriteria(criteria);
        query.with(new Sort(Sort.Direction.ASC, "payTime"));
        query.skip((pageNumber - 1) * pageSize).limit(pageSize);
        return mongoTemplate.find(query, Card.class);
    }

    @Override
    public void createCardPayTimeIndex() {
        mongoTemplate.dropCollection(Card.class);
        boolean b = mongoTemplate.collectionExists(Card.class);
        if (!b) {
            mongoTemplate.indexOps(Card.class).ensureIndex(new Index().on("payTime", Sort.Direction.ASC));
        }
    }

    @Override
    public List<HistoryCard> getHistoryCardsByBusInfo(String userId, Date beforeDate, Date nowDate, String lineNo, String direction) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("lineNo").is(lineNo)
                .and("direction").is(direction)
                .andOperator(
                        Criteria.where("payTime").gte(beforeDate),
                        Criteria.where("payTime").lt(nowDate)
                )
        );
        return mongoTemplate.find(query, HistoryCard.class);
    }

    @Override
    public void addNoMatchOutTimeCards(List<NoMatchOutTimeCard> noMatchOutTimeCards) {
        mongoTemplate.insertAll(noMatchOutTimeCards);
    }

    @Override
    public List<NoMatchOutTimeCard> getNoMatchOutTimeCardsDesc(int pageNumber, int pageSize) {
        Query query = new Query()
                .with(new Sort(Sort.Direction.DESC, "payTime"))
                .skip((pageNumber - 1) * pageSize).limit(pageSize);
        return mongoTemplate.find(query, NoMatchOutTimeCard.class);
    }

    @Override
    public void delNoMatchOutTimeCardsCreateIndex() {
        mongoTemplate.dropCollection(NoMatchOutTimeCard.class);
        boolean b = mongoTemplate.collectionExists(NoMatchOutTimeCard.class);
        if (!b) {
            mongoTemplate.indexOps(NoMatchOutTimeCard.class).ensureIndex(new Index().on("payTime", Sort.Direction.ASC));
        }
    }

    @Override
    public void createRecentStationRangeIndex() {
        boolean b = mongoTemplate.collectionExists(RecentStationRange.class);
        if (!b) {
            mongoTemplate.indexOps(RecentStationRange.class).ensureIndex(new Index().on("distanceValue", Sort.Direction.ASC));
        }
    }

    @Override
    public void delPersonPayRecords() {
        mongoTemplate.dropCollection(PersonPayRecord.class);
    }

    @Override
    public ConfigProperties getConfigProperties() {
        return mongoTemplate.findAll(ConfigProperties.class).get(0);
    }

    @Override
    public List<HistoryCard> getHistoryCards(int pageNumber, int pageSize, String queryTime) {
        Query query = new Query(/*Criteria.where("createTime").is(queryTime)*/).skip((pageNumber - 1) * pageSize).limit(pageSize);
        return mongoTemplate.find(query, HistoryCard.class);
    }

    @Override
    public void addHistoryCards(List<HistoryCard> historyCards) {
        boolean b = mongoTemplate.collectionExists(HistoryCard.class);
        if (!b) {
            Index index = new Index();
            index.on("userId", Sort.Direction.ASC);
            index.on("busNo", Sort.Direction.ASC);
            index.on("lineNo", Sort.Direction.ASC);
            index.on("createTime", Sort.Direction.ASC);
            mongoTemplate.indexOps(HistoryCard.class).ensureIndex(index);
        }
        mongoTemplate.insertAll(historyCards);
    }

    @Override
    public void addCarTrails(List<CarTrailMap> cardList) {
        mongoTemplate.createCollection(CarTrailMap.class);
        mongoTemplate.insertAll(cardList);
    }

    @Override
    public void delCarTrails() {
        mongoTemplate.dropCollection(CarTrailMap.class);
    }

    @Override
    public List<CarTrailMap> getCarTrails(int pageNumber, int pageSize) {
        Query query = new Query().skip((pageNumber - 1) * pageSize).limit(pageSize);
        return mongoTemplate.find(query, CarTrailMap.class);
    }

    @Override
    public void addStationDirectionPo(List<StationDirectionPo> stationDirectionPos) {
        mongoTemplate.insertAll(stationDirectionPos);
    }

    @Override
    public List<StationDirectionPo> getStationDirectionPos(int page, int pageSize) {
        Query query = new Query().skip((page - 1) * pageSize).limit(pageSize);
        return mongoTemplate.find(query, StationDirectionPo.class);
    }

    @Override
    public List<LineStation> getLineStations() {
        return mongoTemplate.findAll(LineStation.class);
    }

    @Override
    public List<StationGPSData> getStationGPSDatas(int page, int pageSize) {
        Query query = new Query().skip((page - 1) * pageSize).limit(pageSize);
        return mongoTemplate.find(query, StationGPSData.class);
    }

    @Override
    public void addStationGPSDatas(List<StationGPSData> gpsDatas) {
        mongoTemplate.insertAll(gpsDatas);
    }

    @Override
    public void delStationGPSDatas() {
        mongoTemplate.dropCollection(StationGPSData.class);
    }

    @Override
    public void removeExpireHistoryCards(Date expireTime) {
        Query query = new Query(Criteria.where("createTime").lt(expireTime));
        mongoTemplate.remove(query, HistoryCard.class);
    }

    @Override
    public void removeExpireRecentStationRanges(Date expireTime) {
        Query query = new Query(Criteria.where("createTime").lt(expireTime));
        mongoTemplate.remove(query, RecentStationRange.class);
    }

    @Override
    public void addPersonPayRecordInfo(PersonPayRecordInfo ppr) {
        mongoTemplate.save(ppr);
    }

    @Override
    public void delPersonPayRecordInfos() {
        mongoTemplate.dropCollection(PersonPayRecordInfo.class);
    }

    @Override
    public void addLoggingPo(LoggingPo loggingPo) {
        boolean b = mongoTemplate.collectionExists(LoggingPo.class);
        if (!b) {
            mongoTemplate.createCollection(LoggingPo.class);
        }
        mongoTemplate.insert(loggingPo);
    }

    @Override
    public void addHistoryCardCopies(List<HistoryCardCopy> historyCardCopies) {
        boolean b = mongoTemplate.collectionExists(HistoryCardCopy.class);
        if (!b) {
            Index index = new Index();
            index.on("userId", Sort.Direction.ASC);
            index.on("busNo", Sort.Direction.ASC);
            index.on("lineNo", Sort.Direction.ASC);
            index.on("createTime", Sort.Direction.ASC);
            mongoTemplate.indexOps(HistoryCardCopy.class).ensureIndex(index);
        }
        mongoTemplate.insertAll(historyCardCopies);
    }

    @Override
    public void createBusRunningNumberPoIndex() {
        boolean b = mongoTemplate.collectionExists(BusRunningNumberPo.class);
        if (!b) {
            Index index = new Index();
            index.on("queryTime", Sort.Direction.ASC);
            index.on("busNo", Sort.Direction.ASC);
            index.on("lineNo", Sort.Direction.ASC);
            mongoTemplate.indexOps(BusRunningNumberPo.class).ensureIndex(index);
        }
    }

    @Override
    public void addBusRunningNumberPos(List<BusRunningNumberPo> busRunningNumberPoList) {
        mongoTemplate.insertAll(busRunningNumberPoList);
    }

    @Override
    public List<BusRunningNumberPo> getBusRunningNumberPo(List<String> busNoList, String lineNo, String direction, Date queryTime) {
        Query query = new Query(Criteria.where("queryTime").is(queryTime)
                .and("busNo").in(busNoList)
                .and("lineNo").is(lineNo)
                .and("direction").is(direction)
        );
        return mongoTemplate.find(query, BusRunningNumberPo.class);
    }

    @Override
    public List<BusRunningNumberPo> getBusRunningNumberPoByBusNos(List<String> busNoList, Date queryTime) {
        Query query = new Query(Criteria.where("queryTime").is(queryTime)
                .and("busNo").in(busNoList)
        );
        return mongoTemplate.find(query, BusRunningNumberPo.class);
    }

    @Override
    public void addCompanyToLinePos(List<CompanyToLinePo> ctpList) {
        mongoTemplate.dropCollection(CompanyToLinePo.class);
        mongoTemplate.insertAll(ctpList);
    }

    @Override
    public List<CompanyToLinePo> getCompanyToLinePos() {
        return mongoTemplate.find(new Query(),CompanyToLinePo.class);
    }

    @Override
    public Long getPersonPayRecordCount() {
        return mongoTemplate.count(new Query(), PersonPayRecord.class);
    }

    @Override
    public void delHistoryCardCopiess(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("createTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query,HistoryCardCopy.class);
    }

    @Override
    public void delOldBusRunningNumberPos(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("queryTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query,BusRunningNumberPo.class);
    }

    @Override
    public void addTempCalculateTaskPo(TempCalculateTaskPo tctp) {
        mongoTemplate.insert(tctp);
    }

    @Override
    public TempCalculateTaskPo getTempCalculateTaskPoByStatus() {
        Query query = new Query(Criteria.where("status").is(0))
                .with(new Sort(Sort.Direction.ASC, "queryTime"))
                .limit(1);
        return mongoTemplate.findOne(query,TempCalculateTaskPo.class);
    }

    @Override
    public void updateTempCalculateTaskPoStatus(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update();
        update.set("status", 1);
        mongoTemplate.updateFirst(query,update,TempCalculateTaskPo.class);
    }
}
