package net.chensee.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.chensee.common.Constants;
import net.chensee.common.DateUtil;
import net.chensee.common.LocationUtil;
import net.chensee.common.RulesUtil;
import net.chensee.dao.ehcache.CarTrailEhCache;
import net.chensee.dao.jdbc.OracleDao;
import net.chensee.entity.po.Card;
import net.chensee.entity.po.CompanyToLinePo;
import net.chensee.entity.po.RecentStationRange;
import net.chensee.entity.po.StationGPSData;
import net.chensee.entity.vo.CarTrail;
import net.chensee.entity.vo.EachRecentStationRange;
import net.chensee.enums.CardHandlerEnum;
import net.chensee.exception.ConsumeDataException;
import net.chensee.service.MongoService;
import net.chensee.service.OracleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static net.chensee.common.Constants.configProperties;

@Service
@Slf4j
public class OracleServiceImpl implements OracleService{

    @Autowired
    private OracleDao oracleDao;
    @Autowired
    private CarTrailEhCache carTrailEhCache;
    @Autowired
    private MongoService mongoService;

    //总数
    private int totalNum = 0;

    private int noFindBusNoCardCount = 0;

    private int allCardCount = 0;

    private Set<String> busSet = new HashSet<>();

    @Override
    public void getCITYCARDTRADE() throws Exception {
        boolean isComplete = this.HandleIsCompleteData();
        if (!isComplete) {
            throw new ConsumeDataException("消费数据异常！");
        }
        this.batchHandler(CardHandlerEnum.CITY_CARD_TRADE);
    }

    @Override
    public void getMOTCARDTRADE() throws Exception {
        this.batchHandler(CardHandlerEnum.MOT_CARD_TRADE);
    }

    @Override
    public void getCPUCardMTRADE() throws Exception {
        this.batchHandler(CardHandlerEnum.CPU_CARD_MTRADE);
    }

    @Override
    public void getCPUCardTRADE() throws Exception {
        this.batchHandler(CardHandlerEnum.CPU_CARD_TRADE);
    }

    @Override
    public void getCardTRADE() throws Exception {
        this.batchHandler(CardHandlerEnum.CARD_TRADE);
    }

    @Override
    public void getMCardTRADE() throws Exception {
        this.batchHandler(CardHandlerEnum.MCARD_TRADE);
    }

    @Override
    public void getUPAYCARDTRADE() throws Exception {
        this.batchHandler(CardHandlerEnum.UPAY_CARD_TRADE);
    }

    @Override
    public void getUPAYQRTRADE() throws Exception {
        this.batchHandler(CardHandlerEnum.UPAY_QR_TRADE);
    }

    @Override
    public void getWXMINITRADE() throws Exception {
        this.batchHandler(CardHandlerEnum.WXMINI_TRADE);
    }

    @Override
    public void getALIPAYTRADE() throws Exception {
        this.batchHandler(CardHandlerEnum.ALIPAY_TRADE);
    }

    @Override
    public void getTodayCards() throws Exception {
        this.getCITYCARDTRADE();
        this.getMOTCARDTRADE();
        this.getCPUCardMTRADE();
        this.getCPUCardTRADE();
        this.getWXMINITRADE();
        this.getALIPAYTRADE();
        this.getUPAYCARDTRADE();
        this.getUPAYQRTRADE();
        this.getMCardTRADE();
        this.getCardTRADE();
        log.error("未在运调数据中找到对应bus_no:{}", noFindBusNoCardCount);
        log.error("未在运调数据中找到对应bus_no:{}", busSet);
        log.error("每日刷卡总量：{}", allCardCount);
        noFindBusNoCardCount = 0;
        busSet = new HashSet<>();
        allCardCount = 0;
    }

    @Override
    public List<CompanyToLinePo> getCompanyToLinePos() {
        return oracleDao.getCompanyToLinePos();
    }

    private void batchHandler(CardHandlerEnum cardHandlerEnum) throws Exception {
        log.error("{}开始查询...",cardHandlerEnum.getKey());
        List<Card> cards;
        int page = configProperties.getPageNumber();
        while (true) {
            cards = this.query(cardHandlerEnum, page);
            if (cards != null && cards.size() != 0) {
                this.handleCards(cards, cardHandlerEnum);
                this.handleCardsLineNoAndAddCards(cards);
                page++;
                totalNum = totalNum + cards.size();
                allCardCount = allCardCount + cards.size();
            } else {
                log.error("{}卡总数：{}", cardHandlerEnum, totalNum);
                totalNum = 0;
                break;
            }
        }
    }

    private void handleCards(List<Card> cards, CardHandlerEnum cardHandlerEnum) {
        Date currentNYRDate = DateUtil.getNYRDate(new Date());
        for (Card card : cards) {
            String bus_no = RulesUtil.getBusNoRuleOfZeros(card.getBusNo());
            if (bus_no != null) {
                card.setBusNo(bus_no);
            }
            if (card.getDeptNo().length() == 1) {
                card.setDeptNo("0" + card.getDeptNo());
            }
            card.setId(UUID.randomUUID().toString());
            card.setCardType(cardHandlerEnum.getKey());
            card.setCreateTime(currentNYRDate);
        }
    }

    private void handleCardsLineNoAndAddCards(List<Card> cards) {
        List<Card> cardList = new ArrayList<>();
        for (Card card : cards) {
            List<CarTrail> carTrails = carTrailEhCache.get(card.getBusNo());
            //取线路号最多的数据作为唯一线路号
            if (carTrails != null && carTrails.size() > 0) {
                List<CarTrail> carTrailList = this.getCarTrailLineNo(carTrails);
                String lineNo = carTrailList.get(0).getLineNo();
                card.setLineNo(lineNo);
                cardList.add(card);
            } else {
                busSet.add(card.getBusNo());
                noFindBusNoCardCount++;
            }
        }
        mongoService.addCards(cardList);
        cardList.clear();
    }

    private List<CarTrail> getCarTrailLineNo(List<CarTrail> carTrails) {
        Map<String, List<CarTrail>> map = new HashMap<>();
        for (CarTrail carTrail : carTrails) {
            String lineNo = carTrail.getLineNo();
            List<CarTrail> carTrails1 = new ArrayList<>();
            if (map.containsKey(lineNo)) {
                carTrails1 = map.get(lineNo);
            }
            carTrails1.add(carTrail);
            map.put(lineNo, carTrails1);
        }
        int num = 0;
        List<CarTrail> list = new ArrayList<>();
        for (Map.Entry<String, List<CarTrail>> entry : map.entrySet()) {
            List<CarTrail> value = entry.getValue();
            if (num < value.size()) {
                num = value.size();
                list = value;
            }
        }
        return list;
    }

    private List<Card> query(CardHandlerEnum cardHandlerEnum, int page) throws Exception {
        List<Card> cards = new ArrayList<>();
        String queryTime = this.getQueryTime();
        String[] split = Constants.queryTime.split("-");
        String ss = "";
        for (String str : split) {
            ss += str;
        }
        Integer pageSize = configProperties.getPageSize();
        switch (cardHandlerEnum) {
            case CITY_CARD_TRADE:
                cards = oracleDao.getCITYCARDTRADE(page, pageSize, queryTime);
                break;
            case MOT_CARD_TRADE:
                cards = oracleDao.getMOTCARDTRADE(page, pageSize, queryTime);
                break;
            case CPU_CARD_MTRADE:
                cards = oracleDao.getCPUCardMTRADE(page, pageSize, queryTime);
                break;
            case CPU_CARD_TRADE:
                cards = oracleDao.getCPUCardTRADE(page, pageSize, queryTime);
                break;
            case CARD_TRADE:
                cards = oracleDao.getCardTRADE(page, pageSize, queryTime);
                break;
            case MCARD_TRADE:
                cards = oracleDao.getMCardTRADE(page, pageSize, queryTime);
                break;
            case UPAY_CARD_TRADE:
                cards = oracleDao.getUPAYCARDTRADE(page, pageSize, queryTime);
                break;
            case UPAY_QR_TRADE:
                cards = oracleDao.getUPAYQRTRADE(page, pageSize, queryTime);
                break;
            case WXMINI_TRADE:
                cards = oracleDao.getWXMINITRADE(page, pageSize, queryTime);
                break;
            case ALIPAY_TRADE:
                cards = oracleDao.getALIPAYTRADE(page, pageSize, ss);
                break;
            default:
                break;
        }
        log.error("查询{}每页{}，第{}页", cardHandlerEnum.getKey(), cards.size(), page);
        return cards;
    }

    private boolean HandleIsCompleteData() throws Exception{
        boolean flag = false;
        String queryTime = this.getQueryTime();
        for (String deptNo : Constants.DEPTNOS) {
            int count = oracleDao.isHaveCITYCARDTRADEPage(deptNo, queryTime, 1, 1);
            log.error("查询{}公司消费数：{}，{}", deptNo, count, DateUtil.getNYRSFMDateStr(new Date()));
            if (count > 0) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 通过入库时间查询要比当天时间后一天
     * @return
     */
    private String getQueryTime() {
        Date date = DateUtil.setDateDay(DateUtil.getNYRDateByStr(Constants.queryTime), 1);
        String queryTime = DateUtil.getNYRDateStr(date);
        String[] split = queryTime.split("-");
        String ss = "";
        for (String str : split) {
            ss += str;
        }
        return ss;
    }

    @Override
    public Map<String, List<CarTrail>> getAllCarTrails() {
        //获得每个站点的名称Map
        Map<String, String> stationNameMap = this.getStationNameMap();
        Map<String, List<CarTrail>> carTrailMap = new HashMap<>(6000);
        List<CarTrail> carTrails;
        Integer page = configProperties.getPageNumber();
        Integer pageSize = configProperties.getPageSize();
        String[] split = Constants.queryTime.split("-");
        String queryTime = "";
        for (String str : split) {
            queryTime += str;
        }
        while (true) {
            carTrails = oracleDao.getBusInfo(page, pageSize,queryTime);
            if (carTrails != null && carTrails.size() != 0) {
                this.handleDataAndRule(carTrails,stationNameMap);
                this.getCarTrailMap(carTrailMap, carTrails);
                page++;
                totalNum = totalNum + carTrails.size();
            } else {
                log.error("运调总数：{}", totalNum);
                totalNum = 0;
                break;
            }
        }
        this.handleCarTrailTime(carTrailMap);
        return carTrailMap;
    }

    private Map<String, String> getStationNameMap() {
        List<StationGPSData> stationGPSDataList = this.getStationGpsData();
        Map<String, String> map = new HashMap<>();
        for (StationGPSData stationGPSData : stationGPSDataList) {
            String key = stationGPSData.getLineNo() + "," + stationGPSData.getDirection() + "," + stationGPSData.getStationNo();
            map.put(key, stationGPSData.getStationName());
        }
        return map;
    }

    private void handleCarTrailTime(Map<String, List<CarTrail>> carTrailMap) {
        for (Map.Entry<String, List<CarTrail>> entry : carTrailMap.entrySet()) {
            String key = entry.getKey();
            List<CarTrail> carTrails = entry.getValue();
            List<CarTrail> carTrailList = this.carTrailOrderAsc(carTrails);
            this.addCarTrailInOutTime(carTrailList);
            carTrailMap.put(key, carTrailList);
        }
    }

    private List<CarTrail> carTrailOrderAsc(List<CarTrail> carTrails) {
        List<CarTrail> ct = new ArrayList<>();
        Map<String, List<CarTrail>> map = new HashMap<>();
        for (CarTrail carTrail : carTrails) {
            String lineNo = carTrail.getLineNo();
            List<CarTrail> carTrailList = new ArrayList<>();
            if (map.containsKey(lineNo)) {
                carTrailList = map.get(lineNo);
            }
            carTrailList.add(carTrail);
            map.put(lineNo, carTrailList);
        }
        for (Map.Entry<String, List<CarTrail>> entry : map.entrySet()) {
            List<CarTrail> value = entry.getValue();
            this.orderAsc(value);
            ct.addAll(value);
        }
        return ct;
    }

    /**
     * 冒泡排序
     * @param carTrails
     */
    private void orderAsc(List<CarTrail> carTrails) {
        for (int i = 0; i < carTrails.size(); i++) {
            for (int j = i; j < carTrails.size(); j++) {
                CarTrail carTraili = carTrails.get(i);
                CarTrail carTrailj = carTrails.get(j);
                if (carTrailj.getInTime().getTime() < carTraili.getInTime().getTime()) {
                    carTrails.set(i, carTrailj);
                    carTrails.set(j, carTraili);
                }
            }
        }
    }


    private void addCarTrailInOutTime(List<CarTrail> carTrails) {
        Integer inTimeValue = configProperties.getInTimeValue();
        Integer outTimeValue = configProperties.getOutTimeValue();
        for (int k = 0; k < carTrails.size(); k++) {
            Integer inTimeAddValue = inTimeValue;
            Integer outTimeAddValue = outTimeValue;
            CarTrail carTrail = carTrails.get(k);
            if (k == 0) {
                Date inTime = DateUtil.setDateSecond(carTrail.getInTime(), -inTimeValue);
                carTrail.setInTime(inTime);
            }
            if (k == carTrails.size()-1) {
                Date outTime = DateUtil.setDateSecond(carTrail.getOutTime(), outTimeValue);
                carTrail.setOutTime(outTime);
                break;
            }
            CarTrail carTrail2 = carTrails.get(k + 1);
            int l = (int) ((carTrail2.getInTime().getTime() - carTrail.getOutTime().getTime()) / 1000);
            if (l > 0 && l < inTimeValue + outTimeValue) {
                inTimeAddValue = l / 2;
                outTimeAddValue = l / 2;
            }
            Date outTime = DateUtil.setDateSecond(carTrail.getOutTime(), outTimeAddValue);
            carTrail.setOutTime(outTime);
            Date inTime = DateUtil.setDateSecond(carTrail2.getInTime(), -inTimeAddValue);
            carTrail2.setInTime(inTime);
        }
    }

    @Override
    public List<StationGPSData> getStationGpsData() {
        List<StationGPSData> stationGPSDataList = new ArrayList<>();
        List<StationGPSData> stationGPSDatas;
        int page = configProperties.getPageNumber();
        Integer pageSize = configProperties.getPageSize();
        while (true) {
            stationGPSDatas = oracleDao.getStationGPSData(page, pageSize);
            if (stationGPSDatas != null && stationGPSDatas.size() != 0) {
                this.getStationUniqueKey(stationGPSDatas);
                stationGPSDataList.addAll(stationGPSDatas);
                page++;
            } else {
                break;
            }
        }
        return stationGPSDataList;
    }

    private void handleDataAndRule(List<CarTrail> carTrails, Map<String, String> stationNameMap) {
//        Integer inTimeValue = configProperties.getInTimeValue();
//        Integer outTimeValue = configProperties.getOutTimeValue();
        for (CarTrail carTrail : carTrails) {
            String key = carTrail.getLineNo() + "," + carTrail.getDirection() + "," + carTrail.getStationNo();
            String stationName = stationNameMap.get(key);
            if (stationName != null) {
                carTrail.setStationName(stationName);
            }
//            Date inTime = DateUtil.setDateSecond(carTrail.getInTime(), -inTimeValue);
//            carTrail.setInTime(inTime);
//            Date outTime = DateUtil.setDateSecond(carTrail.getOutTime(), outTimeValue);
//            carTrail.setOutTime(outTime);
            this.handleCarTrailRule(carTrail);
        }
    }

    private void handleCarTrailRule(CarTrail carTrail) {
        String bus_no = RulesUtil.getBusNoRuleOfZeros(carTrail.getBusNo());
        if (bus_no != null) {
            carTrail.setBusNo(bus_no);
        }
        carTrail.setStationUniqueKey(carTrail.getLineNo() + "," + carTrail.getStationNo() + "," + carTrail.getDirection());
    }

    private void getCarTrailMap(Map<String, List<CarTrail>> carTrailMap, List<CarTrail> carTrails) {
        for (CarTrail carTrail : carTrails) {
            String busNo = carTrail.getBusNo();
            List<CarTrail> carTrailList;
            if (carTrail.getLineNo() != null && !"".equals(carTrail.getLineNo())) {
                if (carTrailMap.containsKey(busNo)) {
                    carTrailList = carTrailMap.get(busNo);
                } else {
                    carTrailList = new ArrayList<>();
                }
                carTrailList.add(carTrail);
                carTrailMap.put(busNo, carTrailList);
            }
        }
    }

    @Override
    public List<RecentStationRange> getRecentStationRanges() {
        Map<String, List<EachRecentStationRange>> stationRangeMap = new HashMap<>(30000);
        List<StationGPSData> stationGPSDataList = this.getStationGpsData();
        this.handleStationGPSData(stationRangeMap, stationGPSDataList);
        return this.MapConvertToList(stationRangeMap);
    }

    private void getStationUniqueKey(List<StationGPSData> stationGPSDatas) {
        for (StationGPSData stationGPSData : stationGPSDatas) {
            stationGPSData.setStationUniqueKey(stationGPSData.getLineNo() + "," + stationGPSData.getStationNo() + "," + stationGPSData.getDirection());
        }
    }

    private List<RecentStationRange> MapConvertToList(Map<String, List<EachRecentStationRange>> stationRangeMap) {
        List<RecentStationRange> recentStationRanges = new ArrayList<>(stationRangeMap.size());
        Date currentNYRDate = DateUtil.getNYRDate(new Date());
        Double distanceValue = configProperties.getDistanceValue();
        for (Map.Entry<String, List<EachRecentStationRange>> entry : stationRangeMap.entrySet()) {
            String key = entry.getKey();
            List<EachRecentStationRange> value = entry.getValue();
            RecentStationRange recentStationRange = new RecentStationRange();
            recentStationRange.setStationUniqueKey(key);
            recentStationRange.setDistanceValue(distanceValue);
            recentStationRange.setRangeList(value);
            recentStationRange.setCreateTime(currentNYRDate);
            recentStationRanges.add(recentStationRange);
        }
        return recentStationRanges;
    }

    private void handleStationGPSData(Map<String, List<EachRecentStationRange>> stationRangeMap, List<StationGPSData> stationGPSDataList) {
        Double distanceValue = configProperties.getDistanceValue();
        for (StationGPSData stationGPSData : stationGPSDataList) {
            Double lat = stationGPSData.getLat();
            Double lng = stationGPSData.getLng();
            String stationUniqueKey = stationGPSData.getStationUniqueKey();
            List<EachRecentStationRange> rangeList = new ArrayList<>(1000);
            for (StationGPSData gpsData : stationGPSDataList) {
                if (gpsData.getStationUniqueKey().equals(stationGPSData.getStationUniqueKey())) {
                    continue;
                }
                //TODO
                // 计算距离
                double distance = LocationUtil.getDistance(lat, lng, gpsData.getLat(), gpsData.getLng());
                if (distance < distanceValue) {
                    EachRecentStationRange eachRecentStationRange = new EachRecentStationRange();
                    eachRecentStationRange.setStationNo(gpsData.getStationNo());
                    eachRecentStationRange.setStationUniqueKey(gpsData.getStationUniqueKey());
                    eachRecentStationRange.setStationName(gpsData.getStationName());
                    eachRecentStationRange.setDirection(gpsData.getDirection());
                    eachRecentStationRange.setLineNo(gpsData.getLineNo());
                    eachRecentStationRange.setDistance(distance);
                    rangeList.add(eachRecentStationRange);
                }
            }
            stationRangeMap.put(stationUniqueKey, rangeList);
        }
    }

}
