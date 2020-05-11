package net.chensee.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.chensee.common.Constants;
import net.chensee.common.ConvertUtil;
import net.chensee.common.DateUtil;
import net.chensee.common.RulesUtil;
import net.chensee.dao.ehcache.CarTrailEhCache;
import net.chensee.dao.ehcache.LineStationEhCache;
import net.chensee.dao.ehcache.RecentStationRageEhCache;
import net.chensee.dao.ehcache.StationDirectionEhcache;
import net.chensee.dao.mongo.MongoDao;
import net.chensee.entity.po.*;
import net.chensee.entity.vo.CarTrail;
import net.chensee.enums.OffStationHandleTypeEnum;
import net.chensee.enums.OnStationHandleTypeEnum;
import net.chensee.exception.CarTrailNullException;
import net.chensee.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static net.chensee.common.Constants.configProperties;
import static net.chensee.common.Constants.queryTime;

@Service
@Slf4j
public class BusServiceImpl implements BusService {

    @Autowired
    private MongoDao mongoDao;
    @Autowired
    private MongoService mongoService;
    @Autowired
    private OracleService oracleService;
    @Autowired
    private AnalyseService analyseService;
    @Autowired
    private CarTrailEhCache carTrailEhCache;
    @Autowired
    private RecentStationRageEhCache recentStationRageEhCache;
    @Autowired
    private StationDirectionEhcache stationDirectionEhcache;
    @Autowired
    private LineStationEhCache lineStationEhCache;

    @Override
    public void addHistoryData() throws Exception {
        carTrailEhCache.batchRemove();
        recentStationRageEhCache.batchRemove();
        stationDirectionEhcache.batchRemove();
        lineStationEhCache.batchRemove();
        mongoService.addHistoryCards();
    }

    @Override
    public void handleAllCarTrails() throws Exception {
        Map<String, List<CarTrail>> carTrailMap = oracleService.getAllCarTrails();
        List<CarTrailMap> carTrails = this.handle(carTrailMap);
        if (carTrails.size() == 0) {
            throw new CarTrailNullException("运调数据为空！");
        }
        mongoService.addCarTrails(carTrails);
        carTrailEhCache.batchRemove();
        carTrailEhCache.batchAdd(carTrails);
        this.handleBusRunNumber(carTrails);
        carTrails.clear();
    }

    @Override
    public void handleBusRunNumber(List<CarTrailMap> carTrails) {
        List<BusRunningNumberPo> busRunningNumberPos = new ArrayList<>();
        //上行轨迹集合
        List<CarTrail> onCarTrails;
        //下行轨迹集合
        List<CarTrail> upCarTrails;
        for (CarTrailMap carTrailMap : carTrails) {
            onCarTrails = new ArrayList<>();
            upCarTrails = new ArrayList<>();
            List<CarTrail> carTrailList = carTrailMap.getCarTrails();
            this.getOnUpCarTrails(carTrailList,onCarTrails,upCarTrails);
            //统计线路上下行的运行次数
            if (onCarTrails.size() > 0) {
                Integer onBusRunNumber = this.getBusRunNumber(onCarTrails);
                BusRunningNumberPo onBrnp = this.getBusRunningNumberPo(onCarTrails, onBusRunNumber);
                busRunningNumberPos.add(onBrnp);
            }
            if (upCarTrails.size() > 0) {
                Integer upBusRunNumber = this.getBusRunNumber(upCarTrails);
                BusRunningNumberPo offBrnp = this.getBusRunningNumberPo(upCarTrails, upBusRunNumber);
                busRunningNumberPos.add(offBrnp);
            }
        }
        this.addBusRunningNumberPos(busRunningNumberPos);
    }

    private void addBusRunningNumberPos(List<BusRunningNumberPo> busRunningNumberPos) {
        if (busRunningNumberPos.size() == 0) {
            return;
        }
        mongoDao.createBusRunningNumberPoIndex();
        mongoDao.delOldBusRunningNumberPos(DateUtil.getNYRDateByStr(queryTime));
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        while (true) {
            int startNum = (page - 1) * pageSize;
            int endNum = pageSize * page;
            if (startNum < busRunningNumberPos.size()) {
                if (endNum > busRunningNumberPos.size()) {
                    endNum = busRunningNumberPos.size();
                }
                List<BusRunningNumberPo> busRunningNumberPoList = busRunningNumberPos.subList(startNum, endNum);
                page++;
                mongoDao.addBusRunningNumberPos(busRunningNumberPoList);
            } else {
                busRunningNumberPos.clear();
                break;
            }
        }
    }

    private void getOnUpCarTrails(List<CarTrail> carTrailList, List<CarTrail> onCarTrails, List<CarTrail> upCarTrails) {
        for (CarTrail carTrail : carTrailList) {
            if ("上行".equals(carTrail.getDirection())) {
                onCarTrails.add(carTrail);
            } else {
                upCarTrails.add(carTrail);
            }
        }
    }

    private BusRunningNumberPo getBusRunningNumberPo(List<CarTrail> carTrailList, Integer busRunNumber) {
        BusRunningNumberPo busRunningNumberPo = new BusRunningNumberPo();
        CarTrail carTrail = carTrailList.get(0);
        busRunningNumberPo.setLineNo(carTrail.getLineNo());
        busRunningNumberPo.setDirection(carTrail.getDirection());
        busRunningNumberPo.setBusNo(carTrail.getBusNo());
        busRunningNumberPo.setBusRunningNumber(busRunNumber);
        busRunningNumberPo.setQueryTime(DateUtil.getNYRDateByStr(Constants.queryTime));
        busRunningNumberPo.setCreateTime(new Date());
        return busRunningNumberPo;
    }

    private Integer getBusRunNumber(List<CarTrail> carTrailList) {
        Map<Integer, Integer> map = new HashMap<>();
        for (CarTrail carTrail : carTrailList) {
            Integer stationNo = carTrail.getStationNo();
            if (!map.containsKey(stationNo)) {
                map.put(stationNo, 1);
                continue;
            }
            Integer num = map.get(stationNo);
            map.put(stationNo, num+1);
        }
        Integer busRunNumber = 0;
        for (Integer stationNo : map.keySet()) {
            Integer number = map.get(stationNo);
            if (number > busRunNumber) {
                busRunNumber = number;
            }
        }
        return busRunNumber;
    }

    private List<CarTrailMap> handle(Map<String, List<CarTrail>> carTrailMap) {
        List<CarTrailMap> carTrails = new ArrayList<>(6000);
        for (Map.Entry<String, List<CarTrail>> entry : carTrailMap.entrySet()) {
            String key = entry.getKey();
            List<CarTrail> value = entry.getValue();
            CarTrailMap carTrailMap1 = new CarTrailMap();
            carTrailMap1.setBusNo(key);
            carTrailMap1.setCarTrails(value);
            carTrails.add(carTrailMap1);
        }
        return carTrails;
    }

    @Override
    public void handleCards() throws Exception {
        this.addCarTrailCache();
        mongoDao.createCardPayTimeIndex();
        oracleService.getTodayCards();
    }

    @Override
    public void addCarTrailCache() {
        int size = carTrailEhCache.getSize();
        if (size == 0) {
            carTrailEhCache.batchRemove();
            List<CarTrailMap> carTrailMaps;
            int page = configProperties.getPageNumber();
            int pageSize = configProperties.getPageSize();
            while (true) {
                carTrailMaps = mongoDao.getCarTrails(page, pageSize);
                if (carTrailMaps != null && carTrailMaps.size() != 0) {
                    carTrailEhCache.batchAdd(carTrailMaps);
                    page++;
                } else {
                    log.error("车辆轨迹缓存已插入");
                    break;
                }
            }
        }
    }

    @Override
    public void handleLineStations() throws Exception {
        mongoService.addLineStations();
    }

    @Override
    public void handleStationDirection() throws Exception {
        List<StationGPSData> stationGPSDataList = oracleService.getStationGpsData();
        mongoService.addStationGPSDatas(stationGPSDataList);
        List<StationDirectionPo> stationDirectionPos = this.handleStationDirection(stationGPSDataList);
        mongoService.addStationDirectionPo(stationDirectionPos);
        stationDirectionEhcache.batchRemove();
        stationDirectionEhcache.batchAdd(stationDirectionPos);
        stationDirectionPos.clear();
        stationGPSDataList.clear();
    }

    private List<StationDirectionPo> handleStationDirection(List<StationGPSData> stationGPSDataList) {
        Map<String, List<StationDirection>> map = new HashMap<>();
        for (StationGPSData stationGPSData : stationGPSDataList) {
            String lineNo = stationGPSData.getLineNo();
            String stationName = stationGPSData.getStationName();
            String key = lineNo + "," + stationName;
            List<StationDirection> stationDirections;
            if (map.containsKey(key)) {
                stationDirections = map.get(key);
            } else {
                stationDirections = new ArrayList<>();
            }
            StationDirection stationDirection = new StationDirection();
            stationDirection.setStationNo(stationGPSData.getStationNo());
            stationDirection.setDirection(stationGPSData.getDirection());
            stationDirections.add(stationDirection);
            map.put(key, stationDirections);
        }
        return ConvertUtil.mapToListStationDirectionPo(map);
    }



    @Override
    public void handleRecentStationRanges() throws Exception {
        mongoService.handleRecentStationRanges();
    }

    @Override
    public void handleOnStationInfo() throws Exception {
        this.addCarTrailCache();
        List<Card> getOnStationCards = this.calculateOnStationInfo();
        Map<String, List<Card>> map = this.ListToMapByUserIdKey(getOnStationCards);
        List<PersonPayRecord> personPayRecords = this.convertToList(map);
        log.error("每日刷卡总人数：{}", personPayRecords.size());
        mongoService.delPersonPayRecords();
        mongoService.addPersonPayRecords(personPayRecords);
    }

    @Override
    public void addLineStationCache() {
        int size = lineStationEhCache.getSize();
        if (size == 0) {
            lineStationEhCache.batchRemove();
            List<LineStation> lineStations = mongoDao.getLineStations();
            lineStationEhCache.batchAdd(lineStations);
            lineStations.clear();
        }
    }

    @Override
    public void handleCardsChanceValue() {
        mongoService.handleCardsChanceValue();
    }

    @Override
    public long getPersonPayRecordsSize() {
        return mongoService.getPersonPayRecordsSize();
    }

    @Override
    public void addStationDirectionCache() {
        int size = stationDirectionEhcache.getSize();
        if (size == 0) {
            stationDirectionEhcache.batchRemove();
            List<StationDirectionPo> stationDirectionPos;
            int page = configProperties.getPageNumber();
            int pageSize = configProperties.getPageSize();
            while (true) {
                stationDirectionPos = mongoDao.getStationDirectionPos(page, pageSize);
                if (stationDirectionPos != null && stationDirectionPos.size() != 0) {
                    stationDirectionEhcache.batchAdd(stationDirectionPos);
                    stationDirectionPos.clear();
                    page++;
                } else {
                    log.error("站点线路缓存已插入");
                    break;
                }
            }
        }
    }

    @Override
    public void addRecentStationRanges() {
        int size = recentStationRageEhCache.getSize();
        if (size == 0) {
            recentStationRageEhCache.batchRemove();
            List<RecentStationRange> recentStationRanges;
            int page = configProperties.getPageNumber();
            int pageSize = configProperties.getPageSize();
            Double distanceValue = configProperties.getDistanceValue();
            while (true) {
                recentStationRanges = mongoDao.getRecentStationRanges(page, pageSize, distanceValue);
                if (recentStationRanges != null && recentStationRanges.size() != 0) {
                    recentStationRageEhCache.batchAdd(recentStationRanges);
                    recentStationRanges.clear();
                    page++;
                } else {
                    log.error("最近站距缓存已插入");
                    break;
                }
            }
        }
    }

    @Override
    public List<Card> calculateOnStationInfo() {
        return this.batchGetOnStationInfo();
    }

    private List<Card> batchGetOnStationInfo() {
        List<Card> getOnStationCards = new ArrayList<>(1000000);
        List<NoMatchOutTimeCard> noMatchOutTimeCards = new ArrayList<>(200000);
        List<Card> noMatchInTimeCards = new ArrayList<>(100000);

        //cards 按照刷卡的时间升序排列
        List<Card> cards;
        Date currentNYRDate = DateUtil.getNYRDate(new Date());
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        while (true) {
            cards = mongoDao.getCardsByAsc(page, pageSize, currentNYRDate);
            if (cards != null && cards.size() != 0) {
                //刷卡时间在进出站范围内
                this.handle1(cards, getOnStationCards, noMatchOutTimeCards);
                page++;
            } else {
                break;
            }
        }
        log.error("计算上车信息未匹配在进出站时间内的数据有：{}", noMatchOutTimeCards.size());
        //刷卡时间和进站时间之差在阈值内
        this.handle2(getOnStationCards, noMatchOutTimeCards, noMatchInTimeCards);

        //刷卡时间不在进出站范围内，刷卡时间和进出站时间之差不在阈值内，只能取绝对值距离哪个站近在哪个站下车
        this.handle3(getOnStationCards, noMatchInTimeCards);
        log.error("计算上车信息匹配距离两站之间时间取近的为上车点：{}", noMatchInTimeCards.size());

        return getOnStationCards;
    }

    private void handle1(List<Card> cards, List<Card> getOnStationCards, List<NoMatchOutTimeCard> noMatchOutTimeCards) {
        if (cards != null && cards.size() != 0) {
            for (Card card : cards) {
                this.handleCardRule(card);
                List<CarTrail> carTrailList = carTrailEhCache.get(card.getBusNo());
                if (carTrailList != null && carTrailList.size() != 0) {
                    CarTrail ct = this.getOnStationInfo1(carTrailList, card);
                    if (ct != null) {
                        // 在进出站范围内得到上车点，并且匹配刷卡时间离离站时间小于区间内的扩大离站时间区间
                        getOnStationCards.add(card);
                    } else {
                        NoMatchOutTimeCard noMatchOutTimeCard = ConvertUtil.convertToNoMatchOutTimeCard(card);
                        noMatchOutTimeCards.add(noMatchOutTimeCard);
                    }
                }
            }
        }
    }

    private void handleCardRule(Card card) {
        String bus_no = RulesUtil.getBusNoRuleOfZeros(card.getBusNo());
        if (bus_no != null) {
            card.setBusNo(bus_no);
        }
    }

    private CarTrail getOnStationInfo1(List<CarTrail> carTrailList, Card card) {
        CarTrail ct = null;
        Integer addTimeValue = configProperties.getAddTimeValue();
        Integer addInAndOutTimeValue = configProperties.getAddInAndOutTimeValue();
        for (int i = 0; i < carTrailList.size(); i++) {
            CarTrail carTrail = carTrailList.get(i);
            if (!card.getLineNo().equals(carTrail.getLineNo())) {
                continue;
            }
            long payTimestamp = card.getPayTime().getTime();
            long inTimestamp = carTrail.getInTime().getTime();
            long outTimestamp = carTrail.getOutTime().getTime();
            if (inTimestamp < payTimestamp && outTimestamp > payTimestamp) {
                ct = carTrail;
                long toOutTime = (outTimestamp - payTimestamp) / 1000;
                int value = OnStationHandleTypeEnum.ACCORIDING_INOUTTIME.getValue();
                if (toOutTime < addTimeValue) {
                    value = OnStationHandleTypeEnum.ACCORIDING_OUTTIME.getValue();
                    if (i < carTrailList.size() - 1) {
                        CarTrail carTrail1 = carTrailList.get(i + 1);
                        long l = (carTrail1.getInTime().getTime() - outTimestamp) / 1000;
                        if (l > addInAndOutTimeValue) {
                            Date outTime = DateUtil.setDateSecond(carTrail.getOutTime(), addInAndOutTimeValue);
                            carTrail.setOutTime(outTime);
                        }
                    } else {
                        Date outTime = DateUtil.setDateSecond(carTrail.getOutTime(), addInAndOutTimeValue);
                        carTrail.setOutTime(outTime);
                    }
                }
                long toInTime = (payTimestamp - inTimestamp) / 1000;
                if (toInTime < addTimeValue) {
                    value = OnStationHandleTypeEnum.ACCORIDING_INTIME.getValue();
                    if (i > 0) {
                        CarTrail carTrail1 = carTrailList.get(i - 1);
                        long l = (inTimestamp - carTrail1.getOutTime().getTime()) / 1000;
                        if (l > addInAndOutTimeValue) {
                            Date inTime = DateUtil.setDateSecond(carTrail.getInTime(), -addInAndOutTimeValue);
                            carTrail.setInTime(inTime);
                        }
                    }else{
                        Date inTime = DateUtil.setDateSecond(carTrail.getInTime(), -addInAndOutTimeValue);
                        carTrail.setInTime(inTime);
                    }
                }
                card.setOnStationHandleType(value);
                card.setOnStationNo(carTrail.getStationNo());
                card.setOnStationUniqueKey(carTrail.getStationUniqueKey());
                card.setOnStationName(carTrail.getStationName());
                card.setDirection(carTrail.getDirection());

                carTrailEhCache.update(card.getBusNo(), carTrailList);
                break;
            }
        }
        return ct;
    }

    private void handle2(List<Card> getOnStationCards, List<NoMatchOutTimeCard> noMatchOutTimeCards, List<Card> noMatchInTimeCards) {
        this.addNoMatchOutTimeCards(noMatchOutTimeCards);
        List<NoMatchOutTimeCard> noMatchOutTimeCardsDesc;
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        while (true) {
            //noMatchOutTimeCards 按照刷卡的时间降序排列（冒泡从大到小）
            noMatchOutTimeCardsDesc = mongoDao.getNoMatchOutTimeCardsDesc(page, pageSize);
            if (noMatchOutTimeCardsDesc != null && noMatchOutTimeCardsDesc.size() != 0) {
                this.handleNoMatchOutTimeCards(noMatchOutTimeCardsDesc, getOnStationCards, noMatchInTimeCards);
                page++;
            } else {
                break;
            }
        }
    }

    private void handleNoMatchOutTimeCards(List<NoMatchOutTimeCard> noMatchOutTimeCardsDesc, List<Card> getOnStationCards, List<Card> noMatchInTimeCards) {
        for (NoMatchOutTimeCard noMatchOutTimeCard : noMatchOutTimeCardsDesc) {
            Card card = ConvertUtil.convertToNoMatchOutTimeCard(noMatchOutTimeCard);
            List<CarTrail> carTrailList = carTrailEhCache.get(card.getBusNo());
            if (carTrailList != null && carTrailList.size() != 0) {
                CarTrail ct = this.getOnStationInfo2(carTrailList, card);
                if (ct != null) {
                    // 匹配刷卡时间离进站时间小于阈值addTime内的得到上车点
                    getOnStationCards.add(card);
                } else {
                    noMatchInTimeCards.add(card);
                }
            }
        }
    }

    private void addNoMatchOutTimeCards(List<NoMatchOutTimeCard> noMatchOutTimeCards) {
        if (noMatchOutTimeCards != null && noMatchOutTimeCards.size() != 0) {
            mongoDao.delNoMatchOutTimeCardsCreateIndex();
            int page = configProperties.getPageNumber();
            int pageSize = configProperties.getPageSize();
            while (true) {
                int startNum = (page - 1) * pageSize;
                int endNum = pageSize * page;
                if (startNum < noMatchOutTimeCards.size()) {
                    if (endNum > noMatchOutTimeCards.size()) {
                        endNum = noMatchOutTimeCards.size();
                    }
                    List<NoMatchOutTimeCard> noMatchOutTimeCardList = noMatchOutTimeCards.subList(startNum, endNum);
                    page++;
                    mongoDao.addNoMatchOutTimeCards(noMatchOutTimeCardList);
                } else {
                    noMatchOutTimeCards.clear();
                    break;
                }
            }
        }
    }

    private CarTrail getOnStationInfo2(List<CarTrail> carTrailList, Card card) {
        CarTrail ct = null;
        Integer addTimeValue = configProperties.getAddTimeValue();
        Integer addInAndOutTimeValue = configProperties.getAddInAndOutTimeValue();
        for (int i = 0; i < carTrailList.size(); i++) {
            CarTrail carTrail = carTrailList.get(i);
            if (!card.getLineNo().equals(carTrail.getLineNo())) {
                continue;
            }
            long dTime = (carTrail.getInTime().getTime() - card.getPayTime().getTime()) / 1000;
            if (dTime < addTimeValue && dTime > 0) {
                ct = carTrail;
                if (i > 0) {
                    CarTrail carTrail1 = carTrailList.get(i - 1);
                    long l = (carTrail.getInTime().getTime() - carTrail1.getOutTime().getTime()) / 1000;
                    if (l > addInAndOutTimeValue) {
                        Date inTime = DateUtil.setDateSecond(carTrail.getInTime(), -addInAndOutTimeValue);
                        carTrail.setInTime(inTime);
                    }
                }else{
                    Date inTime = DateUtil.setDateSecond(carTrail.getInTime(), -addInAndOutTimeValue);
                    carTrail.setInTime(inTime);
                }
                card.setOnStationHandleType(OnStationHandleTypeEnum.ACCORIDING_OUTTIME.getValue());
                card.setOnStationNo(carTrail.getStationNo());
                card.setOnStationUniqueKey(carTrail.getStationUniqueKey());
                card.setOnStationName(carTrail.getStationName());
                card.setDirection(carTrail.getDirection());

                carTrailEhCache.update(card.getBusNo(), carTrailList);
                break;
            }
        }
        return ct;
    }

    private void handle3(List<Card> getOnStationCards, List<Card> noMatchInOutTimeCards) {
        for (Card card : noMatchInOutTimeCards) {
            List<CarTrail> carTrailList = carTrailEhCache.get(card.getBusNo());
            if (carTrailList != null && carTrailList.size() != 0) {
                this.getOnStationInfo3(getOnStationCards, carTrailList, card);
            }
        }
    }

    private void getOnStationInfo3(List<Card> getOnStationCards, List<CarTrail> carTrailList, Card card) {
        CarTrail ct = carTrailList.get(0);
        long minInTime = ct.getInTime().getTime();
        CarTrail minInTimeCarTrail = ct;
        long minOutTime = ct.getOutTime().getTime();
        CarTrail minOutTimeCarTrail = ct;
        for (int i = 1; i < carTrailList.size(); i++) {
            CarTrail carTrail = carTrailList.get(i);
            if (!card.getLineNo().equals(carTrail.getLineNo())) {
                continue;
            }
            long in = Math.abs(card.getPayTime().getTime() - carTrail.getInTime().getTime());
            long out = Math.abs(card.getPayTime().getTime() - carTrail.getOutTime().getTime());
            if (in < minInTime) {
                minInTime = in;
                minInTimeCarTrail = carTrail;
            }
            if (out < minOutTime) {
                minOutTime = out;
                minOutTimeCarTrail = carTrail;
            }
        }

        CarTrail carTrail;
        if (minInTime < minOutTime) {
            carTrail = minInTimeCarTrail;
        } else {
            carTrail = minOutTimeCarTrail;
        }
        card.setOnStationHandleType(OnStationHandleTypeEnum.ACCORIDING_ABSOLUTEVALUE.getValue());
        card.setOnStationNo(carTrail.getStationNo());
        card.setOnStationUniqueKey(carTrail.getStationUniqueKey());
        card.setOnStationName(carTrail.getStationName());
        card.setDirection(carTrail.getDirection());
        getOnStationCards.add(card);
    }

    private List<PersonPayRecord> convertToList(Map<String, List<Card>> map) {
        List<PersonPayRecord> personPayRecords = new ArrayList<>();
        Date currentNYRDate = DateUtil.getNYRDate(new Date());
        for (Map.Entry<String, List<Card>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<Card> value = entry.getValue();
            PersonPayRecord personPayRecord = new PersonPayRecord();
            personPayRecord.setUserId(key);
            personPayRecord.setCardList(value);
            personPayRecord.setCreateTime(currentNYRDate);
            personPayRecords.add(personPayRecord);
        }
        return personPayRecords;
    }

    private Map<String, List<Card>> ListToMapByUserIdKey(List<Card> getOnStationCards) {
        Map<String, List<Card>> map = new HashMap<>();
        for (Card card : getOnStationCards) {
            List<Card> cardList = new ArrayList<>();
            String userId = card.getUserId();
            if (map.containsKey(userId)) {
                cardList = map.get(userId);
            }
            cardList.add(card);
            map.put(userId, cardList);
        }
        return map;
    }

    @Override
    public void analyse() throws Exception{
        analyseService.addLineStationsPeople();
        this.calculateChance();
    }

    @Override
    public void cleanExpireData() throws Exception {
        Integer expireTimeRangeValue = configProperties.getExpireTimeRangeValue();
        Date nowDate = DateUtil.getNYRDateByStr(Constants.queryTime);
        Date date = DateUtil.setDateDay(nowDate, -expireTimeRangeValue);
        Date expireTime = DateUtil.getNYRDate(date);
        mongoService.removeExpireHistoryCards(expireTime);
        mongoService.removeExpireRecentStationRanges(expireTime);
    }

    @Override
    public void calculateChance() {
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        List<PersonPayRecordInfo> personPayRecordList;
        int cardsCount = 0;
        int accoriding_endstation = 0;
        int accoriding_transfer = 0;
        int accoriding_chance = 0;
        while (true) {
            personPayRecordList = mongoDao.getPersonPayRecordInfos(page, pageSize);
            if (personPayRecordList != null && personPayRecordList.size() != 0) {
                for (PersonPayRecordInfo personPayRecordInfo : personPayRecordList) {
                    List<Card> cardList = personPayRecordInfo.getCardList();
                    cardsCount += cardList.size();
                    for (Card card : cardList) {
                        if (card.getOffStationHandleType() == null) {
                            continue;
                        }
                        if (card.getOffStationHandleType() == OffStationHandleTypeEnum.ACCORIDING_ENDSTATION.getValue()) {
                            accoriding_endstation++;
                        } else if (card.getOffStationHandleType() == OffStationHandleTypeEnum.ACCORIDING_TRANSFER.getValue()) {
                            accoriding_transfer++;
                        } else if (card.getOffStationHandleType() == OffStationHandleTypeEnum.ACCORIDING_CHANCE.getValue()) {
                            accoriding_chance++;
                        }
                    }
                }
                page++;
            } else {
                break;
            }
        }
        log.error("{}根据上车点是终点站算出的概率为：{}", Constants.queryTime, (double) accoriding_endstation / cardsCount);
        log.error("{}根据换乘出行链算出的概率为：{}", Constants.queryTime,(double) accoriding_transfer / cardsCount);
        log.error("{}根据规律出行概率算出的概率为：{}", Constants.queryTime,(double) accoriding_chance / cardsCount);
        log.error("{}总概率为：{}", Constants.queryTime,(double) (accoriding_endstation + accoriding_transfer + accoriding_chance) / cardsCount);
    }

}
