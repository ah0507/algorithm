package net.chensee.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.chensee.common.Constants;
import net.chensee.common.ConvertUtil;
import net.chensee.common.DateUtil;
import net.chensee.common.DoubleHandleUtil;
import net.chensee.dao.mongo.AnalyseDao;
import net.chensee.dao.mongo.MongoDao;
import net.chensee.entity.po.*;
import net.chensee.entity.po.analyse.company.*;
import net.chensee.entity.po.analyse.group.AvgStatisticsPo;
import net.chensee.entity.po.analyse.line.*;
import net.chensee.entity.vo.CarTrail;
import net.chensee.entity.vo.RelationStation;
import net.chensee.enums.CardHandlerEnum;
import net.chensee.enums.EachScopeTimeEnum;
import net.chensee.service.AnalyseService;
import net.chensee.service.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static net.chensee.common.Constants.configProperties;
import static net.chensee.common.Constants.queryTime;

@Service
@Slf4j
public class AnalyseServiceImpl implements AnalyseService {

    @Autowired
    private MongoService mongoService;
    @Autowired
    private MongoDao mongoDao;
    @Autowired
    private AnalyseDao analyseDao;

    /**
     * 没有算出下车时间的消费数据量
     */
    private int noOffTimeCount = 0;
    /**
     * 消费数据在线路站点数据中中找不到对应站点的数据量
     */
    private int noGetOnCarTrailCount = 0;

    @Override
    public void addLineStationsPeople() throws Exception {
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();

        //得到线路站点GPS数据
        List<StationGPSData> stationGPSDatas = mongoService.getStationGPSDatas();

        // 通过车辆轨迹信息获取每条线路的每个方向的车辆集合
        List<CarTrailMap> carTrailMaps = this.getCarTrails();

        /**
         * 按公司分
         */
        //以公司为key，统计每天每个公司换乘次数，拥挤度
        Map<String, EachCompBasicStatisticsPo> basicStatisticsPoMap = new HashMap<>();
        //以公司ID,卡类型为key，每个公司每种卡类型的消费人数的统计
        Map<String, EachCompCardTypeConsumeStatisticsPo> cardTypeConsumeStatisticsPoMap = new HashMap<>();
        //以公司ID,线路号为key，统计每个公司每条线路的消费人数
        Map<String, EachCompLineConsumeStatisticsPo> lineConsumeStatisticsPoMap = new HashMap<>();
        //以公司ID为key,统计每天每个公司五个峰值的消费数据
        Map<String, List<EachCompFivePeakStatisticsPo>> peakStatisticsPoMap = new HashMap<>();
        //以公司ID,站点名称为key，统计每个公司每个站点上车的消费人数
        Map<String, EachCompStationConsumeStatisticsPo> stationConsumeStatisticsPoMap = new HashMap<>();
        //公司生成各容器
        this.getAllCompanyStatisticsPack(stationGPSDatas, carTrailMaps, basicStatisticsPoMap, cardTypeConsumeStatisticsPoMap, lineConsumeStatisticsPoMap, peakStatisticsPoMap, stationConsumeStatisticsPoMap);

        /**
         * 按集团分（将所有公司合并为集团）
         */
        AvgStatisticsPo avgStatisticsPo = this.getAvgStatisticsPo();

        /**
         * 按线路分
         */
        //以线路号，方向为key，统计每天每条线路每个方向的换乘次数，拥挤度，消费人数
        Map<String, EachLineBasicStatisticsPo> eachLineBasicStatisticsPoMap = new HashMap<>();
        //以线路号，方向，站点编号为key，OD分析 每天每条线路每个方向的每个站点的上下车人数及其两辆站点间出行人数
        Map<String, EachLineODStatisticsPo> eachLineODStatisticsPoMap = this.getAnalyseODMap(stationGPSDatas);
        //以线路号，方向，站点编号为key，统计每天每条线路每个方向每个站点的上下车、在车上、换乘人数
        Map<String, EachLineStationInfoStatisticsPo> eachLineStationInfoStatisticsPoMap = new HashMap<>();
        //以线路号，方向为key，统计每天每个时间段（每隔一小时）每条线路每个方向的上车人数、下车人数、换乘人数
        Map<String, List<EachLineEachTimeBasicStatisticsPo>> eachLineEachTimeBasicStatisticsPoMap = new HashMap<>();
        //以线路号，方向为key，统计每天每条线路每个方向五个峰值的消费数据
        Map<String, List<EachLineFivePeakStatisticsPo>> eachLineFivePeakStatisticsPoMap = new HashMap<>();
        //线路生成容器
        this.getAllLineStatisticsPack(stationGPSDatas, carTrailMaps, eachLineBasicStatisticsPoMap, eachLineStationInfoStatisticsPoMap, eachLineEachTimeBasicStatisticsPoMap, eachLineFivePeakStatisticsPoMap);

        List<PersonPayRecordInfo> personPayRecordInfos;
        while (true) {
            personPayRecordInfos = mongoDao.getPersonPayRecordInfos(page, pageSize);
            if (personPayRecordInfos != null && personPayRecordInfos.size() != 0) {
                for (PersonPayRecordInfo personPayRecordInfo : personPayRecordInfos) {
                    List<Card> cardList = personPayRecordInfo.getCardList();
                    // 统计公司模块的非分析数据
                    this.calculateEachCompStatistics(cardList, basicStatisticsPoMap, cardTypeConsumeStatisticsPoMap, lineConsumeStatisticsPoMap, peakStatisticsPoMap, stationConsumeStatisticsPoMap);
                    //统计线路模块的各分析数据
                    this.calculateEachLineStatistics(cardList, eachLineBasicStatisticsPoMap, eachLineODStatisticsPoMap, eachLineStationInfoStatisticsPoMap, eachLineEachTimeBasicStatisticsPoMap, eachLineFivePeakStatisticsPoMap);
                    // 统计集团分析
                    this.calculateGroup(cardList,avgStatisticsPo);
                }
                page++;
            } else {
                log.error("没有算出下车时间的消费数据:{}，消费数据在线路站点中找不到对应站点：{}", noOffTimeCount, noGetOnCarTrailCount);
                noOffTimeCount = 0;
                noGetOnCarTrailCount = 0;
                break;
            }
        }

        /**
         * 按照公司补全各分析并且保存
         * 需要补全每条线路的刷卡扩样系数用于扩大线路下车人数和换乘人数，票款扩样系数扩大补全硬币消费
         */
        //统计每天每个公司换乘次数，拥挤度，消费人数
        this.analyseEachCompBasicStatisticsPoMap(basicStatisticsPoMap);

        //每天每个公司每种卡类型的消费人数的统计（ 查询每种消费方式和全部消费的占比）
        this.analyseEachCompCardTypeConsumeStatisticsPoMap(cardTypeConsumeStatisticsPoMap);

        //统计每天每个公司每条线路的消费人数(线路排名)
        this.analyseEachCompLineConsumeStatisticsPoMap(lineConsumeStatisticsPoMap);

        //统计每天每个公司五个峰值的消费数据
        this.analyseEachCompFivePeakStatisticsPoMap(peakStatisticsPoMap);

        //统计每天每个公司每个站点的消费人数（站点消费排名图和热力图）
        this.analyseEachCompStationConsumeStatisticsPoMap(stationConsumeStatisticsPoMap, basicStatisticsPoMap);

        /**
         * 按照线路补全各分析并且保存
         * 需要补全每条线路的刷卡扩样系数用于扩大线路下车人数和换乘人数,票款扩样系数扩大补全硬币消费
         */
        //统计每天每条线路每个方向的换乘次数，拥挤度，消费人数
        this.analyseEachLineBasicStatisticsPoMap(eachLineBasicStatisticsPoMap);

        //OD客流图
        this.analyseEachLineODStatisticsPoMap(eachLineODStatisticsPoMap, eachLineBasicStatisticsPoMap);

        //统计每天每条线路每个方向每个站点的上下车、在车上、换乘人数
        this.analyseEachLineStationInfoStatisticsPoMap(eachLineStationInfoStatisticsPoMap, eachLineBasicStatisticsPoMap);

        //统计每天每个时间段（每隔一小时）每条线路每个方向的上车人数、下车人数、换乘人数
        this.analyseEachLineEachTimeBasicStatisticsPoMap(eachLineEachTimeBasicStatisticsPoMap, eachLineBasicStatisticsPoMap);

        //统计每天每条线路每个方向五个峰值的消费数据
        this.analyseEachLineFivePeakStatisticsPoMap(eachLineFivePeakStatisticsPoMap);

        /**
         * 统计集团分析
         */
        this.analyseAvgStatisticsPo(avgStatisticsPo);
    }

    private void analyseAvgStatisticsPo(AvgStatisticsPo avgStatisticsPo) {
        Long personPayRecordCount = mongoService.getPersonPayRecordCount();

        if (avgStatisticsPo.getSuccessCalculateGetOffTotal() != 0) {
            double avgGetOnStationCount = (double) avgStatisticsPo.getGetOnStationTotal() / avgStatisticsPo.getSuccessCalculateGetOffTotal();
            double avgOnTime = (double) avgStatisticsPo.getOnTimeTotal() / avgStatisticsPo.getSuccessCalculateGetOffTotal();
            avgStatisticsPo.setAvgGetOnStationCount(DoubleHandleUtil.convertTo3Decimal(avgGetOnStationCount));
            avgStatisticsPo.setAvgOnTime(DoubleHandleUtil.convertTo3Decimal(avgOnTime));
        }
        if (personPayRecordCount != 0) {
            double avgRideCount = (double) avgStatisticsPo.getSuccessCalculateGetOffTotal() / personPayRecordCount;
            avgStatisticsPo.setAvgRideCount(DoubleHandleUtil.convertTo3Decimal(avgRideCount));
            avgStatisticsPo.setPersonPayRecordCount(personPayRecordCount);
        }

        analyseDao.delAvgStatisticsPo(DateUtil.getNYRDateByStr(queryTime));
        analyseDao.addAvgStatisticsPo(avgStatisticsPo);
    }

    private void calculateGroup(List<Card> cardList, AvgStatisticsPo avgStatisticsPo) {
        for (Card card : cardList) {
            if (card.getOffStationUniqueKey() == null || card.getOffTime() == null) {
                continue;
            }
            Integer number = card.getOffStationNo() - card.getOnStationNo();
            avgStatisticsPo.setGetOnStationTotal(avgStatisticsPo.getGetOnStationTotal() + number);
            long spendTime = (card.getOffTime().getTime() - card.getPayTime().getTime())/1000;
            avgStatisticsPo.setOnTimeTotal(avgStatisticsPo.getOnTimeTotal() + spendTime);
            avgStatisticsPo.setSuccessCalculateGetOffTotal(avgStatisticsPo.getSuccessCalculateGetOffTotal() + 1);
        }
    }

    private AvgStatisticsPo getAvgStatisticsPo() {
        AvgStatisticsPo avgStatisticsPo = new AvgStatisticsPo();
        avgStatisticsPo.setGetOnStationTotal(0L);
        avgStatisticsPo.setOnTimeTotal(0L);
        avgStatisticsPo.setSuccessCalculateGetOffTotal(0L);
        avgStatisticsPo.setAvgGetOnStationCount(0.0);
        avgStatisticsPo.setAvgOnTime(0.0);
        avgStatisticsPo.setAvgRideCount(0.0);
        avgStatisticsPo.setQueryTime(DateUtil.getNYRDateByStr(queryTime));
        avgStatisticsPo.setQueryTimeStr(queryTime);
        avgStatisticsPo.setCreateTime(new Date());
        return avgStatisticsPo;
    }

    private void analyseEachCompBasicStatisticsPoMap(Map<String, EachCompBasicStatisticsPo> basicStatisticsPoMap) {
        List<EachCompBasicStatisticsPo> ebspList = new ArrayList<>();
        for (Map.Entry<String, EachCompBasicStatisticsPo> entry : basicStatisticsPoMap.entrySet()) {
            EachCompBasicStatisticsPo value = entry.getValue();
            if (value.getOffCount() != 0) {
                value.setUseCardValue((double) value.getConsumeCount() / value.getOffCount() - 1);
                basicStatisticsPoMap.put(entry.getKey(), value);
            }
            ebspList.add(value);
        }
        for (EachCompBasicStatisticsPo ep : ebspList) {
            ep.setConsumeCount((int) (ep.getConsumeCount() * (1 + configProperties.getAddTicketValue())));
            //处理得到该条线路的多次运行的所有车辆的总人数
            Integer total = this.handleEachCompRadio(ep);
            if (total != 0) {
                ep.setRatio((double) ep.getConsumeCount() / total);
            }
            ep.setTransferCount((int) (ep.getTransferCount() * (1 + ep.getUseCardValue()) * (1 + configProperties.getAddTicketValue())));
            ep.setOffCount((int) (ep.getOffCount() * (1 + ep.getUseCardValue()) * (1 + configProperties.getAddTicketValue())));
        }
        analyseDao.delOldEachCompBasicStatisticsPos(DateUtil.getNYRDateByStr(queryTime));
        analyseDao.addEachCompBasicStatisticsPos(ebspList);
    }

    private Integer handleEachCompRadio(EachCompBasicStatisticsPo ebsp) {
        Set<String> busNos = ebsp.getBusNos();
        List<String> busNoList = new ArrayList<>(busNos);
        List<BusRunningNumberPo> busRunningNumberPos = mongoService.getBusRunningNumberPoByBusNos(busNoList, DateUtil.getNYRDateByStr(queryTime));
        Integer total = 0;
        for (BusRunningNumberPo busRunningNumberPo : busRunningNumberPos) {
            total += busRunningNumberPo.getBusRunningNumber() * configProperties.getRatedNumber();
        }
        return total;
    }

    private void analyseEachCompCardTypeConsumeStatisticsPoMap(Map<String, EachCompCardTypeConsumeStatisticsPo> cardTypeConsumeStatisticsPoMap) {
        List<EachCompCardTypeConsumeStatisticsPo> ectcspList = new ArrayList<>();
        for (String key : cardTypeConsumeStatisticsPoMap.keySet()) {
            EachCompCardTypeConsumeStatisticsPo ectcsp = cardTypeConsumeStatisticsPoMap.get(key);
            ectcsp.setConsumeCount((int) (ectcsp.getConsumeCount() * (1 + configProperties.getAddTicketValue())));
            ectcspList.add(ectcsp);
        }

        analyseDao.delOldEachCompCardTypeConsumeStatisticsPos(DateUtil.getNYRDateByStr(queryTime));
        analyseDao.addEachCompCardTypeConsumeStatisticsPos(ectcspList);
    }

    private void analyseEachCompLineConsumeStatisticsPoMap(Map<String, EachCompLineConsumeStatisticsPo> lineConsumeStatisticsPoMap) {
        List<EachCompLineConsumeStatisticsPo> elcspList = new ArrayList<>();
        for (String key : lineConsumeStatisticsPoMap.keySet()) {
            EachCompLineConsumeStatisticsPo elcsp = lineConsumeStatisticsPoMap.get(key);
            elcsp.setConsumeCount((int) (elcsp.getConsumeCount() * (1 + configProperties.getAddTicketValue())));
            elcspList.add(elcsp);
        }

        analyseDao.delOldEachCompLineConsumeStatisticsPos(DateUtil.getNYRDateByStr(queryTime));
        analyseDao.addEachCompLineConsumeStatisticsPos(elcspList);
    }

    private void analyseEachCompFivePeakStatisticsPoMap(Map<String, List<EachCompFivePeakStatisticsPo>> peakStatisticsPoMap) {
        List<EachCompFivePeakStatisticsPo> efpspList = new ArrayList<>();
        for (String key : peakStatisticsPoMap.keySet()) {
            List<EachCompFivePeakStatisticsPo> efpsps = peakStatisticsPoMap.get(key);
            for (EachCompFivePeakStatisticsPo efpsp : efpsps) {
                efpsp.setConsumeCount((int) (efpsp.getConsumeCount() * (1 + configProperties.getAddTicketValue())));
            }
            efpspList.addAll(efpsps);
        }

        analyseDao.delOldEachCompFivePeakStatisticsPos(DateUtil.getNYRDateByStr(queryTime));
        analyseDao.addEachCompFivePeakStatisticsPos(efpspList);
    }

    private void analyseEachCompStationConsumeStatisticsPoMap(Map<String, EachCompStationConsumeStatisticsPo> stationConsumeStatisticsPoMap, Map<String, EachCompBasicStatisticsPo> basicStatisticsPoMap) {
        List<EachCompStationConsumeStatisticsPo> escspList = new ArrayList<>();
        for (String key : stationConsumeStatisticsPoMap.keySet()) {
            EachCompStationConsumeStatisticsPo escsp = stationConsumeStatisticsPoMap.get(key);
            escsp.setConsumeCount((int) (escsp.getConsumeCount() * (1 + configProperties.getAddTicketValue())));
            EachCompBasicStatisticsPo ebsp = basicStatisticsPoMap.get(escsp.getDeptNo());
            escsp.setOffCount((int) (escsp.getOffCount() * (1 + ebsp.getUseCardValue()) * (1 + configProperties.getAddTicketValue())));
            escsp.setTransferCount((int) (escsp.getTransferCount() * (1 + ebsp.getUseCardValue()) * (1 + configProperties.getAddTicketValue())));
            escspList.add(escsp);
        }
        this.addEachCompStationConsumeStatisticsPos(escspList);
    }

    private void addEachCompStationConsumeStatisticsPos(List<EachCompStationConsumeStatisticsPo> escspList) {
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        if (escspList.size() != 0) {
            analyseDao.createEachCompStationConsumeStatisticsPosIndex();
            analyseDao.delOldEachCompStationConsumeStatisticsPos(DateUtil.getNYRDateByStr(queryTime));
            while (true) {
                int startNum = (page - 1) * pageSize;
                int endNum = pageSize * page;
                if (startNum < escspList.size()) {
                    if (endNum > escspList.size()) {
                        endNum = escspList.size();
                    }
                    List<EachCompStationConsumeStatisticsPo> escsps = escspList.subList(startNum, endNum);
                    page++;
                    analyseDao.addEachCompStationConsumeStatisticsPos(escsps);
                } else {
                    break;
                }
            }
        }
    }

    private void calculateEachCompStatistics(List<Card> cardList, Map<String, EachCompBasicStatisticsPo> basicStatisticsPoMap, Map<String, EachCompCardTypeConsumeStatisticsPo> cardTypeConsumeStatisticsPoMap, Map<String, EachCompLineConsumeStatisticsPo> lineConsumeStatisticsPoMap, Map<String, List<EachCompFivePeakStatisticsPo>> peakStatisticsPoMap, Map<String, EachCompStationConsumeStatisticsPo> stationConsumeStatisticsPoMap) {
        for (Card card : cardList) {
            this.handleEachCompBasicStatisticsPoMap(basicStatisticsPoMap, card);
            this.handleEachCompCardTypeConsumeStatisticsPoMap(cardTypeConsumeStatisticsPoMap, card);
            this.handleEachCompLineConsumeStatisticsPoMap(lineConsumeStatisticsPoMap, card);
            this.handleEachCompFivePeakStatisticsPoMap(peakStatisticsPoMap, card);
            this.handleEachCompStationConsumeStatisticsPoMap(stationConsumeStatisticsPoMap, card);
        }
    }

    private void handleEachCompStationConsumeStatisticsPoMap(Map<String, EachCompStationConsumeStatisticsPo> stationConsumeStatisticsPoMap, Card card) {
        String key = card.getDeptNo() + "," + card.getOnStationName();
        if (!stationConsumeStatisticsPoMap.containsKey(key)) {
            return;
        }
        EachCompStationConsumeStatisticsPo escsp = stationConsumeStatisticsPoMap.get(key);
        if (escsp == null) {
            return;
        }
        escsp.setConsumeCount(escsp.getConsumeCount() + 1);
        if (card.getOffStationUniqueKey() != null && card.getOffTime() != null) {
            escsp.setOffCount(escsp.getOffCount() + 1);
        }
        if (card.getTransferId() != null) {
            escsp.setTransferCount(escsp.getTransferCount() + 1);
        }
        stationConsumeStatisticsPoMap.put(key, escsp);
    }

    private void handleEachCompFivePeakStatisticsPoMap(Map<String, List<EachCompFivePeakStatisticsPo>> peakStatisticsPoMap, Card card) {
        String deptNo = card.getDeptNo();
        if (!peakStatisticsPoMap.containsKey(deptNo)) {
            return;
        }
        List<EachCompFivePeakStatisticsPo> efpsps = peakStatisticsPoMap.get(deptNo);
        if (efpsps == null) {
            return;
        }
        for (EachCompFivePeakStatisticsPo efpsp : efpsps) {
            if (card.getPayTime().getTime() > efpsp.getStartTime().getTime() && card.getPayTime().getTime() <= efpsp.getEndTime().getTime()) {
                efpsp.setConsumeCount(efpsp.getConsumeCount() + 1);
                break;
            }
        }
        peakStatisticsPoMap.put(deptNo, efpsps);
    }

    private void handleEachCompLineConsumeStatisticsPoMap(Map<String, EachCompLineConsumeStatisticsPo> lineConsumeStatisticsPoMap, Card card) {
        String key = card.getDeptNo() + "," + card.getLineNo();
        if (!lineConsumeStatisticsPoMap.containsKey(key)) {
            return;
        }
        EachCompLineConsumeStatisticsPo elcsp = lineConsumeStatisticsPoMap.get(key);
        elcsp.setConsumeCount(elcsp.getConsumeCount() + 1);
        lineConsumeStatisticsPoMap.put(key, elcsp);
    }

    private void handleEachCompCardTypeConsumeStatisticsPoMap(Map<String, EachCompCardTypeConsumeStatisticsPo> cardTypeConsumeStatisticsPoMap, Card card) {
        String key = card.getDeptNo() + "," + card.getCardType();
        if (!cardTypeConsumeStatisticsPoMap.containsKey(key)) {
            return;
        }
        EachCompCardTypeConsumeStatisticsPo eccsp = cardTypeConsumeStatisticsPoMap.get(key);
        eccsp.setConsumeCount(eccsp.getConsumeCount() + 1);
        cardTypeConsumeStatisticsPoMap.put(key, eccsp);
    }

    private void handleEachCompBasicStatisticsPoMap(Map<String, EachCompBasicStatisticsPo> basicStatisticsPoMap, Card card) {
        String deptNo = card.getDeptNo();
        if (!basicStatisticsPoMap.containsKey(deptNo)) {
            return;
        }
        EachCompBasicStatisticsPo ebsp = basicStatisticsPoMap.get(deptNo);
        if (ebsp == null) {
            return;
        }
        ebsp.setConsumeCount(ebsp.getConsumeCount() + 1);
        if (card.getOffStationUniqueKey() != null && card.getOffTime() != null) {
            ebsp.setOffCount(ebsp.getOffCount() + 1);
        }
        if (card.getTransferId() != null) {
            ebsp.setTransferCount(ebsp.getTransferCount() + 1);
        }
        basicStatisticsPoMap.put(deptNo, ebsp);
    }

    private void getAllCompanyStatisticsPack(List<StationGPSData> stationGPSDatas, List<CarTrailMap> carTrailMaps, Map<String, EachCompBasicStatisticsPo> basicStatisticsPoMap, Map<String, EachCompCardTypeConsumeStatisticsPo> cardTypeConsumeStatisticsPoMap, Map<String, EachCompLineConsumeStatisticsPo> lineConsumeStatisticsPoMap, Map<String, List<EachCompFivePeakStatisticsPo>> peakStatisticsPoMap, Map<String, EachCompStationConsumeStatisticsPo> stationConsumeStatisticsPoMap) {
        // 得到公司对应的线路集合
        List<CompanyToLinePo> companyToLinePos = mongoService.getCompanyToLinePos();
        for (CompanyToLinePo companyToLinePo : companyToLinePos) {
            this.getEachCompBasicStatisticsPo(companyToLinePo, basicStatisticsPoMap, carTrailMaps);
            this.getEachCompCardTypeConsumeStatisticsPo(companyToLinePo, cardTypeConsumeStatisticsPoMap);
            this.getEachCompLineConsumeStatisticsPo(companyToLinePo, lineConsumeStatisticsPoMap);
            this.getEachCompFivePeakStatisticsPo(companyToLinePo, peakStatisticsPoMap);
            this.getEachCompStationConsumeStatisticsPo(companyToLinePo, stationConsumeStatisticsPoMap, stationGPSDatas);
        }
    }

    private void getEachCompStationConsumeStatisticsPo(CompanyToLinePo companyToLinePo, Map<String, EachCompStationConsumeStatisticsPo> stationConsumeStatisticsPoMap, List<StationGPSData> stationGPSDatas) {
        String deptNo = companyToLinePo.getDeptNo();
        EachCompStationConsumeStatisticsPo escsp;
        for (StationGPSData stationGPSData : stationGPSDatas) {
            String stationName = stationGPSData.getStationName();
            String key = deptNo + "," + stationName;
            if (stationConsumeStatisticsPoMap.containsKey(key)) {
                continue;
            }
            escsp = new EachCompStationConsumeStatisticsPo();
            escsp.setDeptNo(deptNo);
            escsp.setStationName(stationName);
            escsp.setLat(stationGPSData.getLat());
            escsp.setLng(stationGPSData.getLng());
            escsp.setConsumeCount(0);
            escsp.setOffCount(0);
            escsp.setTransferCount(0);
            escsp.setQueryTime(DateUtil.getNYRDateByStr(queryTime));
            escsp.setQueryTimeStr(queryTime);
            escsp.setCreateTime(new Date());
            stationConsumeStatisticsPoMap.put(key, escsp);
        }
    }

    private void getEachCompFivePeakStatisticsPo(CompanyToLinePo companyToLinePo, Map<String, List<EachCompFivePeakStatisticsPo>> peakStatisticsPoMap) {
        String deptNo = companyToLinePo.getDeptNo();
        EachScopeTimeEnum[] values = EachScopeTimeEnum.values();
        List<EachCompFivePeakStatisticsPo> efpsps = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            EachScopeTimeEnum eachScopeTimeEnum = values[i];
            String scopeTimeName = eachScopeTimeEnum.getKey();
            String value = eachScopeTimeEnum.getValue();
            Date startTime;
            if (i == 0) {
                Date nowTime = DateUtil.getNYRDateByStr(queryTime);
                startTime = DateUtil.setDateHour(nowTime, 4);
            } else {
                EachScopeTimeEnum beforeEachScopeTimeEnum = values[i - 1];
                startTime = DateUtil.getNYRSFMDateByStr(queryTime + beforeEachScopeTimeEnum.getValue());
            }
            Date endTime = DateUtil.getNYRSFMDateByStr(queryTime + value);
            EachCompFivePeakStatisticsPo eachCompFivePeakStatisticsPo = this.getEachCompFivePeakStatistics(startTime, endTime, scopeTimeName, deptNo);
            efpsps.add(eachCompFivePeakStatisticsPo);
        }
        peakStatisticsPoMap.put(deptNo, efpsps);
    }

    private EachCompFivePeakStatisticsPo getEachCompFivePeakStatistics(Date startTime, Date endTime, String scopeTimeName, String deptNo) {
        EachCompFivePeakStatisticsPo efpsp = new EachCompFivePeakStatisticsPo();
        efpsp.setDeptNo(deptNo);
        efpsp.setScopeTimeName(scopeTimeName);
        efpsp.setConsumeCount(0);
        efpsp.setStartTime(startTime);
        efpsp.setEndTime(endTime);
        efpsp.setQueryTime(DateUtil.getNYRDateByStr(queryTime));
        efpsp.setQueryTimeStr(queryTime);
        efpsp.setCreateTime(new Date());
        return efpsp;
    }

    private void getEachCompLineConsumeStatisticsPo(CompanyToLinePo companyToLinePo, Map<String, EachCompLineConsumeStatisticsPo> lineConsumeStatisticsPoMap) {
        String deptNo = companyToLinePo.getDeptNo();
        EachCompLineConsumeStatisticsPo ecsp;
        for (String lineNo : companyToLinePo.getLineNos()) {
            ecsp = new EachCompLineConsumeStatisticsPo();
            String key = deptNo + "," + lineNo;
            if (lineConsumeStatisticsPoMap.containsKey(key)) {
                continue;
            }
            ecsp.setDeptNo(deptNo);
            ecsp.setLineNo(lineNo);
            ecsp.setConsumeCount(0);
            ecsp.setQueryTime(DateUtil.getNYRDateByStr(queryTime));
            ecsp.setQueryTimeStr(queryTime);
            ecsp.setCreateTime(new Date());
            lineConsumeStatisticsPoMap.put(key, ecsp);
        }

    }

    private void getEachCompCardTypeConsumeStatisticsPo(CompanyToLinePo companyToLinePo, Map<String, EachCompCardTypeConsumeStatisticsPo> cardTypeConsumeStatisticsPoMap) {
        String deptNo = companyToLinePo.getDeptNo();
        CardHandlerEnum[] values = CardHandlerEnum.values();
        EachCompCardTypeConsumeStatisticsPo ectcsp;
        for (CardHandlerEnum cardHandlerEnum : values) {
            ectcsp = new EachCompCardTypeConsumeStatisticsPo();
            String cardType = cardHandlerEnum.getKey();
            ectcsp.setDeptNo(deptNo);
            ectcsp.setCardType(cardType);
            ectcsp.setConsumeCount(0);
            ectcsp.setQueryTime(DateUtil.getNYRDateByStr(queryTime));
            ectcsp.setQueryTimeStr(queryTime);
            ectcsp.setCreateTime(new Date());
            cardTypeConsumeStatisticsPoMap.put(deptNo + "," + cardType, ectcsp);
        }
    }

    private void getEachCompBasicStatisticsPo(CompanyToLinePo companyToLinePo, Map<String, EachCompBasicStatisticsPo> basicStatisticsPoMap, List<CarTrailMap> carTrailMaps) {
        String deptNo = companyToLinePo.getDeptNo();
        List<String> lineNos = companyToLinePo.getLineNos();
        EachCompBasicStatisticsPo esp = new EachCompBasicStatisticsPo();
        Set<String> busNos = new HashSet<>();
        //得到每个公司的车辆集合
        for (CarTrailMap carTrailMap : carTrailMaps) {
            String busNo = carTrailMap.getBusNo();
            List<CarTrail> carTrails = carTrailMap.getCarTrails();
            for (CarTrail carTrail : carTrails) {
                if (lineNos.contains(carTrail.getLineNo())) {
                    busNos.add(busNo);
                }
            }
        }
        esp.setDeptNo(deptNo);
        esp.setBusNos(busNos);
        esp.setConsumeCount(0);
        esp.setOffCount(0);
        esp.setRatio(0.0);
        esp.setTransferCount(0);
        esp.setUseCardValue(0.0);
        esp.setQueryTime(DateUtil.getNYRDateByStr(queryTime));
        esp.setQueryTimeStr(queryTime);
        esp.setCreateTime(new Date());
        basicStatisticsPoMap.put(deptNo, esp);
    }

    private void analyseEachLineFivePeakStatisticsPoMap(Map<String, List<EachLineFivePeakStatisticsPo>> eachLineFivePeakStatisticsPoMap) {
        List<EachLineFivePeakStatisticsPo> efpps = new ArrayList<>();
        for (Map.Entry<String, List<EachLineFivePeakStatisticsPo>> entry : eachLineFivePeakStatisticsPoMap.entrySet()) {
            List<EachLineFivePeakStatisticsPo> value = entry.getValue();
            for (EachLineFivePeakStatisticsPo efpp : value) {
                efpp.setConsumeCount((int) (efpp.getConsumeCount() * (1 + configProperties.getAddTicketValue())));
            }
            efpps.addAll(value);
        }
        this.addEachLineFivePeakStatisticsPos(efpps);
    }

    private void addEachLineFivePeakStatisticsPos(List<EachLineFivePeakStatisticsPo> efpps) {
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        if (efpps.size() != 0) {
            analyseDao.createEachLineFivePeakStatisticsPoIndex();
            analyseDao.delOldEachLineFivePeakStatisticsPos(DateUtil.getNYRDateByStr(queryTime));
            while (true) {
                int startNum = (page - 1) * pageSize;
                int endNum = pageSize * page;
                if (startNum < efpps.size()) {
                    if (endNum > efpps.size()) {
                        endNum = efpps.size();
                    }
                    List<EachLineFivePeakStatisticsPo> efppList = efpps.subList(startNum, endNum);
                    page++;
                    analyseDao.addEachLineFivePeakStatisticsPos(efppList);
                } else {
                    break;
                }
            }
        }
    }

    private void analyseEachLineEachTimeBasicStatisticsPoMap(Map<String, List<EachLineEachTimeBasicStatisticsPo>> eachLineEachTimeBasicStatisticsPoMap, Map<String, EachLineBasicStatisticsPo> eachLineBasicStatisticsPoMap) throws Exception {
        List<EachLineEachTimeBasicStatisticsPo> etbspList = new ArrayList<>();
        for (String key : eachLineEachTimeBasicStatisticsPoMap.keySet()) {
            List<EachLineEachTimeBasicStatisticsPo> etbsps = eachLineEachTimeBasicStatisticsPoMap.get(key);
            for (EachLineEachTimeBasicStatisticsPo etbsp : etbsps) {
                EachLineBasicStatisticsPo esp = eachLineBasicStatisticsPoMap.get(etbsp.getLineNo() + "," + etbsp.getDirection());
                etbsp.setGetOnCount((int) (etbsp.getGetOnCount() * (1 + configProperties.getAddTicketValue())));
                etbsp.setTransferCount((int) (etbsp.getTransferCount() * (1 + esp.getUseCardValue()) * (1 + configProperties.getAddTicketValue())));
                etbsp.setGetOffCount((int) (etbsp.getGetOffCount() * (1 + esp.getUseCardValue()) * (1 + configProperties.getAddTicketValue())));
            }
            etbspList.addAll(etbsps);
        }
        this.addEachLineEachTimeBasicStatisticsPos(etbspList);
    }

    private void analyseEachLineStationInfoStatisticsPoMap(Map<String, EachLineStationInfoStatisticsPo> eachLineStationInfoStatisticsPoMap, Map<String, EachLineBasicStatisticsPo> eachLineBasicStatisticsPoMap) throws Exception {
        List<EachLineStationInfoStatisticsTempPo> estpList = this.convertEachLineStationInfoStatisticsTempPos(eachLineStationInfoStatisticsPoMap, eachLineBasicStatisticsPoMap);
        this.addEachLineStationInfoStatisticsTempPos(estpList);
        List<EachLineStationInfoStatisticsPo> esposOrder = this.getEachLineStationInfoStatisticsTempPosOrder();
        // 按站点先后顺序排列计算每一站在车上的人数
        Map<String, Integer> map = new HashMap<>();
        for (EachLineStationInfoStatisticsPo esp : esposOrder) {
            String key = esp.getLineNo() + "," + esp.getDirection();
            int count = esp.getGetOnCount() - esp.getGetOffCount();
            if (map.containsKey(key)) {
                Integer peopleCount = map.get(key);
                peopleCount += count;
                count = peopleCount;
            }
            esp.setPeopleCount(count);
            map.put(key, count);
        }
        this.addEachLineStationInfoStatisticsPos(esposOrder);
    }

    private List<EachLineStationInfoStatisticsTempPo> convertEachLineStationInfoStatisticsTempPos(Map<String, EachLineStationInfoStatisticsPo> eachLineStationInfoStatisticsPoMap, Map<String, EachLineBasicStatisticsPo> eachLineBasicStatisticsPoMap) {
        List<EachLineStationInfoStatisticsPo> elp = new ArrayList<>();
        for (Map.Entry<String, EachLineStationInfoStatisticsPo> entry : eachLineStationInfoStatisticsPoMap.entrySet()) {
            EachLineStationInfoStatisticsPo value = entry.getValue();
            EachLineBasicStatisticsPo ep = eachLineBasicStatisticsPoMap.get(value.getLineNo() + "," + value.getDirection());
            int getOnCount1 = (int) (value.getGetOnCount() * (1 + configProperties.getAddTicketValue()));
            int transferCount1 = (int) (value.getTransferCount() * (1 + ep.getUseCardValue()) * (1 + configProperties.getAddTicketValue()));
            int getOffCount1 = (int) (value.getGetOffCount() * (1 + ep.getUseCardValue()) * (1 + configProperties.getAddTicketValue()));
            value.setGetOnCount(getOnCount1);
            value.setGetOffCount(getOffCount1);
            value.setTransferCount(transferCount1);
            elp.add(value);
        }
        return ConvertUtil.convertToEachLineStationInfoStatisticsTempPo(elp);
    }

    private void analyseEachLineBasicStatisticsPoMap(Map<String, EachLineBasicStatisticsPo> eachLineBasicStatisticsPoMap) {
        List<EachLineBasicStatisticsPo> eachLineBasicStatisticsPos = new ArrayList<>();
        for (Map.Entry<String, EachLineBasicStatisticsPo> entry : eachLineBasicStatisticsPoMap.entrySet()) {
            EachLineBasicStatisticsPo value = entry.getValue();
            if (value.getOffCount() != 0) {
                value.setUseCardValue((double) value.getConsumeCount() / value.getOffCount() - 1);
                eachLineBasicStatisticsPoMap.put(entry.getKey(), value);
            }
            eachLineBasicStatisticsPos.add(value);
        }
        for (EachLineBasicStatisticsPo ep : eachLineBasicStatisticsPos) {
            int consumeCount = (int) (ep.getConsumeCount() * (1 + configProperties.getAddTicketValue()));
            ep.setConsumeCount(consumeCount);
            //处理得到该条线路的多次运行的所有车辆的总人数
            Integer total = this.handleRadio(ep);
            if (total != 0) {
                ep.setRatio((double) ep.getConsumeCount() / total);
            }
            ep.setTransferCount((int) (ep.getTransferCount() * (1 + ep.getUseCardValue()) * (1 + configProperties.getAddTicketValue())));
            ep.setOffCount((int) (ep.getOffCount() * (1 + ep.getUseCardValue()) * (1 + configProperties.getAddTicketValue())));
        }

        analyseDao.delOldEachLineBasicStatisticsPos(DateUtil.getNYRDateByStr(queryTime));
        analyseDao.addEachLineBasicStatisticsPos(eachLineBasicStatisticsPos);
    }

    private Integer handleRadio(EachLineBasicStatisticsPo eachLineBasicStatisticsPo) {
        Set<String> busNos = eachLineBasicStatisticsPo.getBusNos();
        List<String> busNoList = new ArrayList<>(busNos);
        List<BusRunningNumberPo> busRunningNumberPos = mongoService.getBusRunningNumberPo(busNoList, eachLineBasicStatisticsPo.getLineNo(), eachLineBasicStatisticsPo.getDirection(), DateUtil.getNYRDateByStr(queryTime));
        Integer total = 0;
        for (BusRunningNumberPo busRunningNumberPo : busRunningNumberPos) {
            total += busRunningNumberPo.getBusRunningNumber() * configProperties.getRatedNumber();
        }
        return total;
    }

    private void calculateEachLineStatistics(List<Card> cardList, Map<String, EachLineBasicStatisticsPo> eachLineBasicStatisticsPoMap, Map<String, EachLineODStatisticsPo> eachLineODStatisticsPoMap, Map<String, EachLineStationInfoStatisticsPo> eachLineStationInfoStatisticsPoMap, Map<String, List<EachLineEachTimeBasicStatisticsPo>> eachLineEachTimeBasicStatisticsPoMap, Map<String, List<EachLineFivePeakStatisticsPo>> eachLineFivePeakStatisticsPoMap) {
        for (Card card : cardList) {
            this.handleEachLineBasicStatisticsPoMap(eachLineBasicStatisticsPoMap, card);
            this.handleEachLineODStationInfoMap(eachLineODStatisticsPoMap, card);
            this.handleEachLineStationInfoStatisticsPoMap(eachLineStationInfoStatisticsPoMap, card);
            this.handleEachLineEachTimeBasicStatisticsPoMap(eachLineEachTimeBasicStatisticsPoMap, card);
            this.handleEachLineFivePeakStatisticsPoMap(eachLineFivePeakStatisticsPoMap, card);
        }
    }

    private void handleEachLineFivePeakStatisticsPoMap(Map<String, List<EachLineFivePeakStatisticsPo>> eachLineFivePeakStatisticsPoMap, Card card) {
        String key = card.getLineNo() + "," + card.getDirection();
        Date payTime = card.getPayTime();
        List<EachLineFivePeakStatisticsPo> eachLineFivePeakStatisticsPos = eachLineFivePeakStatisticsPoMap.get(key);
        if (eachLineFivePeakStatisticsPos == null || eachLineFivePeakStatisticsPos.size() == 0) {
            return;
        }
        for (EachLineFivePeakStatisticsPo ep : eachLineFivePeakStatisticsPos) {
            if (payTime.getTime() > ep.getStartTime().getTime() && payTime.getTime() <= ep.getEndTime().getTime()) {
                ep.setConsumeCount(ep.getConsumeCount() + 1);
                break;
            }
        }
        eachLineFivePeakStatisticsPoMap.put(key, eachLineFivePeakStatisticsPos);
    }

    private void handleEachLineEachTimeBasicStatisticsPoMap(Map<String, List<EachLineEachTimeBasicStatisticsPo>> eachLineEachTimeBasicStatisticsPoMap, Card card) {
        String key = card.getLineNo() + "," + card.getDirection();
        Date payTime = card.getPayTime();
        Date offTime = card.getOffTime();
        List<EachLineEachTimeBasicStatisticsPo> eachTimeBasicStatisticsList = eachLineEachTimeBasicStatisticsPoMap.get(key);
        if (eachTimeBasicStatisticsList == null || eachTimeBasicStatisticsList.size() == 0) {
            return;
        }
        for (EachLineEachTimeBasicStatisticsPo ep : eachTimeBasicStatisticsList) {
            Date currentTime = ep.getCurrentTime();
            Date startDate = DateUtil.setDateHour(currentTime, -Constants.TIMEVALUE);
            if (payTime.getTime() > startDate.getTime() && payTime.getTime() <= currentTime.getTime()) {
                int getOnCount = ep.getGetOnCount();
                ep.setGetOnCount(getOnCount + 1);
            }

            if (card.getOffStationUniqueKey() == null || offTime == null) {
                continue;
            }
            if (offTime.getTime() > startDate.getTime() && offTime.getTime() <= currentTime.getTime()) {
                int getOffCount = ep.getGetOffCount();
                ep.setGetOffCount(getOffCount + 1);
                if (card.getTransferId() != null) {
                    ep.setTransferCount(ep.getTransferCount() + 1);
                }
            }
        }
        eachLineEachTimeBasicStatisticsPoMap.put(key, eachTimeBasicStatisticsList);
    }

    private void handleEachLineStationInfoStatisticsPoMap(Map<String, EachLineStationInfoStatisticsPo> eachLineStationInfoStatisticsPoMap, Card card) {
        String onStationUniqueKey = card.getOnStationUniqueKey();
        String offStationUniqueKey = card.getOffStationUniqueKey();
        if (!eachLineStationInfoStatisticsPoMap.containsKey(onStationUniqueKey)) {
            noGetOnCarTrailCount++;
            return;
        }
        EachLineStationInfoStatisticsPo onStatisticsPo = eachLineStationInfoStatisticsPoMap.get(onStationUniqueKey);
        if (onStatisticsPo == null) {
            return;
        }
        onStatisticsPo.setGetOnCount(onStatisticsPo.getGetOnCount() + 1);
        eachLineStationInfoStatisticsPoMap.put(onStationUniqueKey, onStatisticsPo);

        if (offStationUniqueKey == null || !eachLineStationInfoStatisticsPoMap.containsKey(offStationUniqueKey)) {
            return;
        }
        if (card.getOffTime() == null) {
            noOffTimeCount++;
            return;
        }
        EachLineStationInfoStatisticsPo offStatisticsPo = eachLineStationInfoStatisticsPoMap.get(offStationUniqueKey);
        if (offStatisticsPo == null) {
            return;
        }
        offStatisticsPo.setGetOffCount(offStatisticsPo.getGetOffCount() + 1);
        if (card.getTransferId() != null) {
            offStatisticsPo.setTransferCount(offStatisticsPo.getTransferCount() + 1);
        }
        eachLineStationInfoStatisticsPoMap.put(offStationUniqueKey, offStatisticsPo);
    }

    private void handleEachLineBasicStatisticsPoMap(Map<String, EachLineBasicStatisticsPo> eachLineBasicStatisticsPoMap, Card card) {
        String key = card.getLineNo() + "," + card.getDirection();
        if (!eachLineBasicStatisticsPoMap.containsKey(key)) {
            return;
        }
        EachLineBasicStatisticsPo eachLineBasicStatisticsPo = eachLineBasicStatisticsPoMap.get(key);
        if (eachLineBasicStatisticsPo == null) {
            return;
        }
        eachLineBasicStatisticsPo.setConsumeCount(eachLineBasicStatisticsPo.getConsumeCount() + 1);
        if (card.getOffStationUniqueKey() != null && card.getOffTime() != null) {
            eachLineBasicStatisticsPo.setOffCount(eachLineBasicStatisticsPo.getOffCount() + 1);
        }
        if (card.getTransferId() != null) {
            eachLineBasicStatisticsPo.setTransferCount(eachLineBasicStatisticsPo.getTransferCount() + 1);
        }
        eachLineBasicStatisticsPoMap.put(key, eachLineBasicStatisticsPo);
    }

    private void getAllLineStatisticsPack(List<StationGPSData> stationGPSDatas, List<CarTrailMap> carTrailMaps, Map<String, EachLineBasicStatisticsPo> eachLineBasicStatisticsPoMap, Map<String, EachLineStationInfoStatisticsPo> eachLineStationInfoStatisticsPoMap, Map<String, List<EachLineEachTimeBasicStatisticsPo>> eachLineEachTimeBasicStatisticsPoMap, Map<String, List<EachLineFivePeakStatisticsPo>> eachLineFivePeakStatisticsPoMap) {
        for (StationGPSData stationGPSData : stationGPSDatas) {
            this.getEachLineBasicStatisticsPo(carTrailMaps, stationGPSData, eachLineBasicStatisticsPoMap);
            this.getEachLineStationInfoStatisticsPo(stationGPSData, eachLineStationInfoStatisticsPoMap);
            this.getEachLineEachTimeBasicStatisticsPos(stationGPSData, eachLineEachTimeBasicStatisticsPoMap);
            this.getEachLineFivePeakStatisticsPos(stationGPSData, eachLineFivePeakStatisticsPoMap);
        }
    }

    private void getEachLineFivePeakStatisticsPos(StationGPSData stationGPSData, Map<String, List<EachLineFivePeakStatisticsPo>> eachLineFivePeakStatisticsPoMap) {
        String key = stationGPSData.getLineNo() + "," + stationGPSData.getDirection();
        if (eachLineFivePeakStatisticsPoMap.containsKey(key)) {
            return;
        }
        List<EachLineFivePeakStatisticsPo> eachLineFivePeakStatisticsPos = new ArrayList<>();
        EachScopeTimeEnum[] values = EachScopeTimeEnum.values();
        for (int i = 0; i < values.length; i++) {
            EachScopeTimeEnum eachScopeTimeEnum = values[i];
            String scopeTimeName = eachScopeTimeEnum.getKey();
            String value = eachScopeTimeEnum.getValue();
            Date startTime;
            if (i == 0) {
                Date nowTime = DateUtil.getNYRDateByStr(queryTime);
                startTime = DateUtil.setDateHour(nowTime, 4);
            } else {
                EachScopeTimeEnum beforeEachScopeTimeEnum = values[i - 1];
                startTime = DateUtil.getNYRSFMDateByStr(queryTime + beforeEachScopeTimeEnum.getValue());
            }
            Date endTime = DateUtil.getNYRSFMDateByStr(queryTime + value);
            EachLineFivePeakStatisticsPo eachLineCardTypeStatistics = this.getEachLineFivePeakStatistics(startTime, endTime, scopeTimeName, stationGPSData);
            eachLineFivePeakStatisticsPos.add(eachLineCardTypeStatistics);
        }
        eachLineFivePeakStatisticsPoMap.put(key, eachLineFivePeakStatisticsPos);
    }

    private void getEachLineEachTimeBasicStatisticsPos(StationGPSData stationGPSData, Map<String, List<EachLineEachTimeBasicStatisticsPo>> eachLineEachTimeBasicStatisticsPoMap) {
        String key = stationGPSData.getLineNo() + "," + stationGPSData.getDirection();
        if (eachLineEachTimeBasicStatisticsPoMap.containsKey(key)) {
            return;
        }
        Date nowDate = DateUtil.getNYRDateByStr(queryTime);
        Date ltDate = DateUtil.setDateDay(nowDate, 1);
        List<EachLineEachTimeBasicStatisticsPo> eachLineEachTimeBasicStatisticsPos = new ArrayList<>();
        EachLineEachTimeBasicStatisticsPo eachLineEachTimeBasicStatisticsPo;
        while (true) {
            eachLineEachTimeBasicStatisticsPo = new EachLineEachTimeBasicStatisticsPo();
            nowDate = DateUtil.setDateHour(nowDate, Constants.TIMEVALUE);
            if (ltDate.getTime() < nowDate.getTime()) {
                break;
            }
            if (ltDate.getTime() == nowDate.getTime()) {
                nowDate = DateUtil.setDateSecond(nowDate, -1);
            }
            this.getEachLineEachTimeBasicStatistics(stationGPSData, eachLineEachTimeBasicStatisticsPo);
            eachLineEachTimeBasicStatisticsPo.setCurrentTime(nowDate);
            eachLineEachTimeBasicStatisticsPo.setCurrentTimeStr(DateUtil.getSFMDateStr(nowDate));
            eachLineEachTimeBasicStatisticsPos.add(eachLineEachTimeBasicStatisticsPo);
        }
        eachLineEachTimeBasicStatisticsPoMap.put(key, eachLineEachTimeBasicStatisticsPos);
    }

    private void getEachLineStationInfoStatisticsPo(StationGPSData stationGPSData, Map<String, EachLineStationInfoStatisticsPo> eachLineStationInfoStatisticsPoMap) {
        EachLineStationInfoStatisticsPo eachLineStationInfoStatisticsPo = new EachLineStationInfoStatisticsPo();
        eachLineStationInfoStatisticsPo.setLineNo(stationGPSData.getLineNo());
        eachLineStationInfoStatisticsPo.setDirection(stationGPSData.getDirection());
        eachLineStationInfoStatisticsPo.setStationNo(stationGPSData.getStationNo());
        eachLineStationInfoStatisticsPo.setStationName(stationGPSData.getStationName());
        eachLineStationInfoStatisticsPo.setGetOnCount(0);
        eachLineStationInfoStatisticsPo.setGetOffCount(0);
        eachLineStationInfoStatisticsPo.setPeopleCount(0);
        eachLineStationInfoStatisticsPo.setTransferCount(0);
        eachLineStationInfoStatisticsPo.setQueryTime(DateUtil.getNYRDateByStr(Constants.queryTime));
        eachLineStationInfoStatisticsPo.setQueryTimeStr(Constants.queryTime);
        eachLineStationInfoStatisticsPo.setCreateTime(new Date());
        eachLineStationInfoStatisticsPoMap.put(stationGPSData.getStationUniqueKey(), eachLineStationInfoStatisticsPo);

    }

    private void getEachLineBasicStatisticsPo(List<CarTrailMap> carTrailMaps, StationGPSData stationGPSData, Map<String, EachLineBasicStatisticsPo> eachLineBasicStatisticsPoMap) {
        String key = stationGPSData.getLineNo() + "," + stationGPSData.getDirection();
        if (eachLineBasicStatisticsPoMap.containsKey(key)) {
            return;
        }
        //获得线路方向对应的车辆集合
        Set<String> busNos = new HashSet<>();
        for (CarTrailMap carTrailMap : carTrailMaps) {
            List<CarTrail> carTrails = carTrailMap.getCarTrails();
            for (CarTrail carTrail : carTrails) {
                String key2 = carTrail.getLineNo() + "," + carTrail.getDirection();
                if (key.equals(key2)) {
                    busNos.add(carTrail.getBusNo());
                }
            }
        }
        EachLineBasicStatisticsPo eachLineBasicStatisticsPo = new EachLineBasicStatisticsPo();
        eachLineBasicStatisticsPo.setLineNo(stationGPSData.getLineNo());
        eachLineBasicStatisticsPo.setDirection(stationGPSData.getDirection());
        eachLineBasicStatisticsPo.setBusNos(busNos);
        eachLineBasicStatisticsPo.setUseCardValue(0.0);
        eachLineBasicStatisticsPo.setConsumeCount(0);
        eachLineBasicStatisticsPo.setTransferCount(0);
        eachLineBasicStatisticsPo.setOffCount(0);
        eachLineBasicStatisticsPo.setRatio(0.0);
        eachLineBasicStatisticsPo.setQueryTime(DateUtil.getNYRDateByStr(Constants.queryTime));
        eachLineBasicStatisticsPo.setQueryTimeStr(Constants.queryTime);
        eachLineBasicStatisticsPo.setCreateTime(new Date());
        eachLineBasicStatisticsPoMap.put(key, eachLineBasicStatisticsPo);
    }

    private void analyseEachLineODStatisticsPoMap(Map<String, EachLineODStatisticsPo> eachLineODStatisticsPoMap, Map<String, EachLineBasicStatisticsPo> eachLineBasicStatisticsPoMap) {
        List<EachLineODStatisticsPo> eachLineODStatisticsPos = new ArrayList<>();
        for (Map.Entry<String, EachLineODStatisticsPo> entry : eachLineODStatisticsPoMap.entrySet()) {
            EachLineODStatisticsPo value = entry.getValue();
            EachLineBasicStatisticsPo eachLineBasicStatisticsPo = eachLineBasicStatisticsPoMap.get(value.getLineNo() + "," + value.getDirection());
            this.setRelationStationOff(value, eachLineBasicStatisticsPo);
            int getOnCount = (int) (value.getGetOnCount() * (1 + configProperties.getAddTicketValue()));
            int getOffCount = (int) (value.getGetOffCount() * (1 + eachLineBasicStatisticsPo.getUseCardValue()) * (1 + configProperties.getAddTicketValue()));
            value.setGetOnCount(getOnCount);
            value.setGetOffCount(getOffCount);
            eachLineODStatisticsPos.add(value);
        }
        this.addEachLineODStatisticsPos(eachLineODStatisticsPos);
    }

    private void setRelationStationOff(EachLineODStatisticsPo value, EachLineBasicStatisticsPo eachLineBasicStatisticsPo) {
        List<RelationStation> relationStationList = value.getRelationStationList();
        if (relationStationList == null || relationStationList.size() == 0) {
            return;
        }
        for (RelationStation relationStation : relationStationList) {
            int tripCount = (int) (relationStation.getTripCount() * (1 + eachLineBasicStatisticsPo.getUseCardValue()) * (1 + configProperties.getAddTicketValue()));
            relationStation.setTripCount(tripCount);
        }
    }

    private void addEachLineODStatisticsPos(List<EachLineODStatisticsPo> eachLineStationInfos) {
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        if (eachLineStationInfos.size() != 0) {
            analyseDao.createEachLineODStationInfoIndex();
            analyseDao.delOldEachLineODStationInfos(DateUtil.getNYRDateByStr(queryTime));
            while (true) {
                int startNum = (page - 1) * pageSize;
                int endNum = pageSize * page;
                if (startNum < eachLineStationInfos.size()) {
                    if (endNum > eachLineStationInfos.size()) {
                        endNum = eachLineStationInfos.size();
                    }
                    List<EachLineODStatisticsPo> eachLineODStatisticsPos = eachLineStationInfos.subList(startNum, endNum);
                    page++;
                    analyseDao.addEachLineODStatisticsPos(eachLineODStatisticsPos);
                } else {
                    break;
                }
            }
        }
    }

    private Map<String, EachLineODStatisticsPo> getAnalyseODMap(List<StationGPSData> stationGPSDatas) {
        Map<String, EachLineODStatisticsPo> eachLineODStatisticsPoMap = new HashMap<>(stationGPSDatas.size());
        Map<String, Map<Integer, EachLineODStatisticsPo>> eachLineStationMap = new HashMap<>();
        for (StationGPSData stationGPSData : stationGPSDatas) {
            String key = stationGPSData.getLineNo() + "," + stationGPSData.getDirection();
            Integer stationNo = stationGPSData.getStationNo();
            EachLineODStatisticsPo eachLineODStatisticsPo = this.getEachLineODStatisticsPo(stationGPSData);
            Map<Integer, EachLineODStatisticsPo> map = new HashMap<>();
            if (eachLineStationMap.containsKey(key)) {
                map = eachLineStationMap.get(key);
                for (Map.Entry<Integer, EachLineODStatisticsPo> entry : map.entrySet()) {
                    Integer currentStationNo = entry.getKey();
                    if (stationNo <= currentStationNo) {
                        continue;
                    }
                    EachLineODStatisticsPo lineODStatisticsPo = entry.getValue();
                    List<RelationStation> relationStationList = lineODStatisticsPo.getRelationStationList();
                    if (relationStationList == null) {
                        relationStationList = new ArrayList<>();
                    }
                    RelationStation relationStation = this.getRelationStation(stationGPSData);
                    relationStationList.add(relationStation);
                    lineODStatisticsPo.setRelationStationList(relationStationList);
                    map.put(currentStationNo, lineODStatisticsPo);
                }
            }
            if (!map.containsKey(stationNo)) {
                map.put(stationNo, eachLineODStatisticsPo);
            }
            eachLineStationMap.put(key, map);
        }
        for (Map.Entry<String, Map<Integer, EachLineODStatisticsPo>> entry : eachLineStationMap.entrySet()) {
            String key1 = entry.getKey();
            Map<Integer, EachLineODStatisticsPo> map = entry.getValue();
            for (Map.Entry<Integer, EachLineODStatisticsPo> infoEntry : map.entrySet()) {
                Integer key2 = infoEntry.getKey();
                String key = key1 + "," + key2;
                EachLineODStatisticsPo eachLineODStatisticsPo = infoEntry.getValue();
                eachLineODStatisticsPoMap.put(key, eachLineODStatisticsPo);
            }
        }
        return eachLineODStatisticsPoMap;
    }

    private RelationStation getRelationStation(StationGPSData stationGPSData) {
        RelationStation relationStation = new RelationStation();
        relationStation.setStationNo(stationGPSData.getStationNo());
        relationStation.setStationName(stationGPSData.getStationName());
        relationStation.setTripCount(0);
        return relationStation;
    }

    private EachLineODStatisticsPo getEachLineODStatisticsPo(StationGPSData stationGPSData) {
        EachLineODStatisticsPo eachLineODStatisticsPo = new EachLineODStatisticsPo();
        eachLineODStatisticsPo.setLineNo(stationGPSData.getLineNo());
        eachLineODStatisticsPo.setDirection(stationGPSData.getDirection());
        eachLineODStatisticsPo.setStationNo(stationGPSData.getStationNo());
        eachLineODStatisticsPo.setStationName(stationGPSData.getStationName());
        eachLineODStatisticsPo.setGetOnCount(0);
        eachLineODStatisticsPo.setGetOffCount(0);
        eachLineODStatisticsPo.setQueryTime(DateUtil.getNYRDateByStr(Constants.queryTime));
        eachLineODStatisticsPo.setQueryTimeStr(Constants.queryTime);
        eachLineODStatisticsPo.setCreateTime(new Date());
        return eachLineODStatisticsPo;
    }

    private List<EachLineStationInfoStatisticsPo> getEachLineStationInfoStatisticsTempPosOrder() {
        Integer page = configProperties.getPageNumber();
        Integer pageSize = configProperties.getPageSize();
        List<EachLineStationInfoStatisticsPo> esps = new ArrayList<>(100000);
        List<EachLineStationInfoStatisticsTempPo> estps;
        while (true) {
            estps = analyseDao.getEachLineStationInfoStatisticsTempPosOrder(page, pageSize);
            if (estps != null && estps.size() != 0) {
                List<EachLineStationInfoStatisticsPo> espList = ConvertUtil.convertToEachLineStationInfoStatisticsPos(estps);
                esps.addAll(espList);
                page++;
            } else {
                break;
            }
        }
        return esps;
    }

    private void addEachLineStationInfoStatisticsTempPos(List<EachLineStationInfoStatisticsTempPo> estpList) {
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        if (estpList.size() != 0) {
            analyseDao.createEachLineStationInfoStatisticsTempPosIndex();
            while (true) {
                int startNum = (page - 1) * pageSize;
                int endNum = pageSize * page;
                if (startNum < estpList.size()) {
                    if (endNum > estpList.size()) {
                        endNum = estpList.size();
                    }
                    List<EachLineStationInfoStatisticsTempPo> estps = estpList.subList(startNum, endNum);
                    page++;
                    analyseDao.addEachLineStationInfoStatisticsTempPos(estps);
                } else {
                    break;
                }
            }
        }
    }

    private void addEachLineStationInfoStatisticsPos(List<EachLineStationInfoStatisticsPo> esps) throws Exception {
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        if (esps.size() != 0) {
            analyseDao.createEachLineStationInfoStatisticsPosIndex();
            analyseDao.delOldEachLineStationInfoStatisticsPos(DateUtil.getNYRDateByStr(queryTime));
            while (true) {
                int startNum = (page - 1) * pageSize;
                int endNum = pageSize * page;
                if (startNum < esps.size()) {
                    if (endNum > esps.size()) {
                        endNum = esps.size();
                    }
                    List<EachLineStationInfoStatisticsPo> espList = esps.subList(startNum, endNum);
                    page++;
                    analyseDao.addEachLineStationInfoStatisticsPos(espList);
                } else {
                    break;
                }
            }
        }
    }

    private EachLineFivePeakStatisticsPo getEachLineFivePeakStatistics(Date beforeDate, Date afterDate, String scopeTimeName, StationGPSData stationGPSData) {
        EachLineFivePeakStatisticsPo eachLineFivePeakStatisticsPo = new EachLineFivePeakStatisticsPo();
        eachLineFivePeakStatisticsPo.setLineNo(stationGPSData.getLineNo());
        eachLineFivePeakStatisticsPo.setDirection(stationGPSData.getDirection());
        eachLineFivePeakStatisticsPo.setConsumeCount(0);
        eachLineFivePeakStatisticsPo.setStartTime(beforeDate);
        eachLineFivePeakStatisticsPo.setEndTime(afterDate);
        eachLineFivePeakStatisticsPo.setQueryTime(DateUtil.getNYRDateByStr(queryTime));
        eachLineFivePeakStatisticsPo.setQueryTimeStr(queryTime);
        eachLineFivePeakStatisticsPo.setCreateTime(new Date());
        eachLineFivePeakStatisticsPo.setScopeTimeName(scopeTimeName);
        return eachLineFivePeakStatisticsPo;
    }

    private void handleEachLineODStationInfoMap(Map<String, EachLineODStatisticsPo> eachLineODStatisticsPoMap, Card card) {
        String onStationKey = card.getLineNo() + "," + card.getDirection() + "," + card.getOnStationNo();
        EachLineODStatisticsPo lineStationInfo = eachLineODStatisticsPoMap.get(onStationKey);
        if (lineStationInfo != null) {
            lineStationInfo.setGetOnCount(lineStationInfo.getGetOnCount() + 1);
        }
        Integer offStationNo = card.getOffStationNo();
        if (offStationNo == null) {
            return;
        }
        String offStationKey = card.getLineNo() + "," + card.getDirection() + "," + card.getOffStationNo();
        EachLineODStatisticsPo eachLineStationInfo = eachLineODStatisticsPoMap.get(offStationKey);
        if (eachLineStationInfo != null) {
            eachLineStationInfo.setGetOffCount(eachLineStationInfo.getGetOffCount() + 1);
        }
        if (lineStationInfo != null) {
            List<RelationStation> relationStationList = lineStationInfo.getRelationStationList();
            if (relationStationList == null || relationStationList.size() == 0) {
                return;
            }
            for (RelationStation relationStation : relationStationList) {
                if (relationStation.getStationNo().equals(offStationNo)) {
                    relationStation.setTripCount(relationStation.getTripCount() + 1);
                    break;
                }
            }
            lineStationInfo.setRelationStationList(relationStationList);
            eachLineODStatisticsPoMap.put(onStationKey, lineStationInfo);
        }
    }

    private void getEachLineEachTimeBasicStatistics(StationGPSData stationGPSData, EachLineEachTimeBasicStatisticsPo eachStationInfo) {
        eachStationInfo.setLineNo(stationGPSData.getLineNo());
        eachStationInfo.setDirection(stationGPSData.getDirection());
        eachStationInfo.setGetOnCount(0);
        eachStationInfo.setGetOffCount(0);
        eachStationInfo.setTransferCount(0);
        eachStationInfo.setQueryTime(DateUtil.getNYRDateByStr(queryTime));
        eachStationInfo.setQueryTimeStr(queryTime);
        eachStationInfo.setCreateTime(new Date());
    }

    private List<CarTrailMap> getCarTrails() {
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        List<CarTrailMap> carTrailMaps = new ArrayList<>();
        List<CarTrailMap> carTrailMapList;
        while (true) {
            carTrailMapList = mongoDao.getCarTrails(page, pageSize);
            if (carTrailMapList != null && carTrailMapList.size() != 0) {
                carTrailMaps.addAll(carTrailMapList);
                page++;
            } else {
                break;
            }
        }
        return carTrailMaps;
    }

    private void addEachLineEachTimeBasicStatisticsPos(List<EachLineEachTimeBasicStatisticsPo> etbsps) throws Exception {
        int page = configProperties.getPageNumber();
        int pageSize = configProperties.getPageSize();
        if (etbsps.size() != 0) {
            analyseDao.createEachLineEachTimeBasicStatisticsPosIndex();
            analyseDao.delOldEachLineEachTimeBasicStatisticsPos(DateUtil.getNYRDateByStr(queryTime));
            while (true) {
                int startNum = (page - 1) * pageSize;
                int endNum = pageSize * page;
                if (startNum < etbsps.size()) {
                    if (endNum > etbsps.size()) {
                        endNum = etbsps.size();
                    }
                    List<EachLineEachTimeBasicStatisticsPo> etbspList = etbsps.subList(startNum, endNum);
                    page++;
                    analyseDao.addEachLineEachTimeBasicStatisticsPos(etbspList);
                } else {
                    break;
                }
            }
        }
    }

}
