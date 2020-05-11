//package net.chensee.service.impl;
//
//import lombok.extern.slf4j.Slf4j;
//import net.chensee.common.Constants;
//import net.chensee.common.DateUtil;
//import net.chensee.common.LocationUtil;
//import net.chensee.common.RulesUtil;
//import net.chensee.dao.jdbc.MysqlDao;
//import net.chensee.entity.po.RecentStationRange;
//import net.chensee.entity.po.StationGPSData;
//import net.chensee.entity.vo.CarTrail;
//import net.chensee.entity.vo.EachRecentStationRange;
//import net.chensee.service.MysqlService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//import static net.chensee.common.Constants.configProperties;
//
//@Service
//@Slf4j
//public class MysqlServiceImpl implements MysqlService {
//
//    @Autowired
//    private MysqlDao mysqlDao;
//    //总数
//    private int totalNum = 0;
//
//    @Override
//    public Map<String, List<CarTrail>> getAllCarTrails() {
//        Map<String, List<CarTrail>> carTrailMap = new HashMap<>(6000);
//        List<CarTrail> carTrails;
//        Integer page = configProperties.getPageNumber();
//        Integer pageSize = configProperties.getPageSize();
//        while (true) {
//            carTrails = mysqlDao.getBusInfo(page, pageSize, Constants.queryTime);
//            if (carTrails != null && carTrails.size() != 0) {
//                this.handleDataAndRule(carTrails);
//                this.getCarTrailMap(carTrailMap, carTrails);
//                page++;
//                totalNum = totalNum + carTrails.size();
//            } else {
//                log.error("运调总数：{}", totalNum);
//                totalNum = 0;
//                break;
//            }
//        }
//        this.handleCarTrailTime(carTrailMap);
//        return carTrailMap;
//    }
//
//    private void handleCarTrailTime(Map<String, List<CarTrail>> carTrailMap) {
//        for (Map.Entry<String, List<CarTrail>> entry : carTrailMap.entrySet()) {
//            String key = entry.getKey();
//            List<CarTrail> carTrails = entry.getValue();
//            List<CarTrail> carTrailList = this.carTrailOrderAsc(carTrails);
//            this.addCarTrailInOutTime(carTrailList);
//            carTrailMap.put(key, carTrailList);
//        }
//    }
//
//    private List<CarTrail> carTrailOrderAsc(List<CarTrail> carTrails) {
//        List<CarTrail> ct = new ArrayList<>();
//        Map<String, List<CarTrail>> map = new HashMap<>();
//        for (CarTrail carTrail : carTrails) {
//            String lineNo = carTrail.getLineNo();
//            List<CarTrail> carTrailList = new ArrayList<>();
//            if (map.containsKey(lineNo)) {
//                carTrailList = map.get(lineNo);
//            }
//            carTrailList.add(carTrail);
//            map.put(lineNo, carTrailList);
//        }
//        for (Map.Entry<String, List<CarTrail>> entry : map.entrySet()) {
//            List<CarTrail> value = entry.getValue();
//            this.orderAsc(value);
//            ct.addAll(value);
//        }
//        return ct;
//    }
//
//    private void orderAsc(List<CarTrail> carTrails) {
//        for (int i = 0; i < carTrails.size(); i++) {
//            for (int j = i; j < carTrails.size(); j++) {
//                CarTrail carTraili = carTrails.get(i);
//                CarTrail carTrailj = carTrails.get(j);
//                if (carTrailj.getInTime().getTime() < carTraili.getInTime().getTime()) {
//                    carTrails.set(i, carTrailj);
//                    carTrails.set(j, carTraili);
//                }
//            }
//        }
//    }
//
//
//    private void addCarTrailInOutTime(List<CarTrail> carTrails) {
//        Integer inTimeValue = configProperties.getInTimeValue();
//        Integer outTimeValue = configProperties.getOutTimeValue();
//        for (int k = 0; k < carTrails.size(); k++) {
//            Integer inTimeAddValue = inTimeValue;
//            Integer outTimeAddValue = outTimeValue;
//            CarTrail carTrail = carTrails.get(k);
//            if (k == 0) {
//                Date inTime = DateUtil.setDateSecond(carTrail.getInTime(), -inTimeValue);
//                carTrail.setInTime(inTime);
//            }
//            if (k == carTrails.size()-1) {
//                Date outTime = DateUtil.setDateSecond(carTrail.getOutTime(), outTimeValue);
//                carTrail.setOutTime(outTime);
//                break;
//            }
//            CarTrail carTrail2 = carTrails.get(k + 1);
//            int l = (int) ((carTrail2.getInTime().getTime() - carTrail.getOutTime().getTime()) / 1000);
//            if (l > 0 && l < inTimeValue + outTimeValue) {
//                inTimeAddValue = l / 2;
//                outTimeAddValue = l / 2;
//            }
//            Date outTime = DateUtil.setDateSecond(carTrail.getOutTime(), outTimeAddValue);
//            carTrail.setOutTime(outTime);
//            Date inTime = DateUtil.setDateSecond(carTrail2.getInTime(), -inTimeAddValue);
//            carTrail2.setInTime(inTime);
//        }
//    }
//
//    @Override
//    public List<StationGPSData> getStationGpsData() {
//        List<StationGPSData> stationGPSDataList = new ArrayList<>();
//        List<StationGPSData> stationGPSDatas;
//        int page = configProperties.getPageNumber();
//        Integer pageSize = configProperties.getPageSize();
//        while (true) {
//            stationGPSDatas = mysqlDao.getStationGPSData(page, pageSize);
//            if (stationGPSDatas != null && stationGPSDatas.size() != 0) {
//                this.getStationUniqueKey(stationGPSDatas);
//                stationGPSDataList.addAll(stationGPSDatas);
//                page++;
//            } else {
//                break;
//            }
//        }
//        return stationGPSDataList;
//    }
//
//    private void handleDataAndRule(List<CarTrail> carTrails) {
////        Integer inTimeValue = configProperties.getInTimeValue();
////        Integer outTimeValue = configProperties.getOutTimeValue();
//        for (CarTrail carTrail : carTrails) {
////            Date inTime = DateUtil.setDateSecond(carTrail.getInTime(), -inTimeValue);
////            carTrail.setInTime(inTime);
////            Date outTime = DateUtil.setDateSecond(carTrail.getOutTime(), outTimeValue);
////            carTrail.setOutTime(outTime);
//            this.handleCarTrailRule(carTrail);
//        }
//    }
//
//    private void handleCarTrailRule(CarTrail carTrail) {
//        String bus_no = RulesUtil.getBusNoRuleOfZeros(carTrail.getBusNo());
//        if (bus_no != null) {
//            carTrail.setBusNo(bus_no);
//        }
//        carTrail.setStationUniqueKey(carTrail.getLineNo() + "," + carTrail.getStationNo() + "," + carTrail.getDirection());
//    }
//
//    private void getCarTrailMap(Map<String, List<CarTrail>> carTrailMap, List<CarTrail> carTrails) {
//        for (CarTrail carTrail : carTrails) {
//            String busNo = carTrail.getBusNo();
//            List<CarTrail> carTrailList;
//            if (carTrail.getLineNo() != null && !"".equals(carTrail.getLineNo())) {
//                if (carTrailMap.containsKey(busNo)) {
//                    carTrailList = carTrailMap.get(busNo);
//                } else {
//                    carTrailList = new ArrayList<>();
//                }
//                carTrailList.add(carTrail);
//                carTrailMap.put(busNo, carTrailList);
//            }
//        }
//    }
//
//    @Override
//    public List<RecentStationRange> getRecentStationRanges() {
//        Map<String, List<EachRecentStationRange>> stationRangeMap = new HashMap<>(30000);
//        List<StationGPSData> stationGPSDataList = this.getStationGpsData();
//        this.handleStationGPSData(stationRangeMap, stationGPSDataList);
//        return this.MapConvertToList(stationRangeMap);
//    }
//
//    private void getStationUniqueKey(List<StationGPSData> stationGPSDatas) {
//        for (StationGPSData stationGPSData : stationGPSDatas) {
//            stationGPSData.setStationUniqueKey(stationGPSData.getLineNo() + "," + stationGPSData.getStationNo() + "," + stationGPSData.getDirection());
//        }
//    }
//
//    private List<RecentStationRange> MapConvertToList(Map<String, List<EachRecentStationRange>> stationRangeMap) {
//        List<RecentStationRange> recentStationRanges = new ArrayList<>(stationRangeMap.size());
//        Date currentNYRDate = DateUtil.getNYRDate(new Date());
//        Double distanceValue = configProperties.getDistanceValue();
//        for (Map.Entry<String, List<EachRecentStationRange>> entry : stationRangeMap.entrySet()) {
//            String key = entry.getKey();
//            List<EachRecentStationRange> value = entry.getValue();
//            RecentStationRange recentStationRange = new RecentStationRange();
//            recentStationRange.setStationUniqueKey(key);
//            recentStationRange.setDistanceValue(distanceValue);
//            recentStationRange.setRangeList(value);
//            recentStationRange.setCreateTime(currentNYRDate);
//            recentStationRanges.add(recentStationRange);
//        }
//        return recentStationRanges;
//    }
//
//    private void handleStationGPSData(Map<String, List<EachRecentStationRange>> stationRangeMap, List<StationGPSData> stationGPSDataList) {
//        Double distanceValue = configProperties.getDistanceValue();
//        for (StationGPSData stationGPSData : stationGPSDataList) {
//            Double lat = stationGPSData.getLat();
//            Double lng = stationGPSData.getLng();
//            String stationUniqueKey = stationGPSData.getStationUniqueKey();
//            List<EachRecentStationRange> rangeList = new ArrayList<>(1000);
//            for (StationGPSData gpsData : stationGPSDataList) {
//                if (gpsData.getStationUniqueKey().equals(stationGPSData.getStationUniqueKey())) {
//                    continue;
//                }
//                //TODO
//                // 计算距离
//                double distance = LocationUtil.getDistance(lat, lng, gpsData.getLat(), gpsData.getLng());
//                if (distance < distanceValue) {
//                    EachRecentStationRange eachRecentStationRange = new EachRecentStationRange();
//                    eachRecentStationRange.setStationNo(gpsData.getStationNo());
//                    eachRecentStationRange.setStationUniqueKey(gpsData.getStationUniqueKey());
//                    eachRecentStationRange.setStationName(gpsData.getStationName());
//                    eachRecentStationRange.setDirection(gpsData.getDirection());
//                    eachRecentStationRange.setLineNo(gpsData.getLineNo());
//                    eachRecentStationRange.setDistance(distance);
//                    rangeList.add(eachRecentStationRange);
//                }
//            }
//            stationRangeMap.put(stationUniqueKey, rangeList);
//        }
//    }
//
//}
