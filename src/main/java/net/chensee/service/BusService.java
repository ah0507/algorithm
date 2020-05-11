package net.chensee.service;

import net.chensee.entity.po.CarTrailMap;
import net.chensee.entity.po.Card;

import java.util.List;

public interface BusService {

    /**
     * 运调数据的统一处理
     * @throws Exception
     */
    void handleAllCarTrails() throws Exception;

    /**
     * 生成每条线路每个方向每辆车对应的运行次数
     * @param carTrails
     * @throws Exception
     */
    void handleBusRunNumber(List<CarTrailMap> carTrails) throws Exception;

    /**
     * 消费数据的统一处理
     * @throws Exception
     */
    void handleCards() throws Exception;

    /**
     * 线路站点数据的统一处理
     * @throws Exception
     */
    void handleLineStations() throws Exception;

    /**
     * 站点GPS数据和上下行对应站点数据的处理
     * @throws Exception
     */
    void handleStationDirection() throws Exception;

    /**
     * 最近站距表的统一处理
     * @throws Exception
     */
    void handleRecentStationRanges() throws Exception;

    /**
     * 计算上车信息的统一处理
     * @throws Exception
     */
    void handleOnStationInfo() throws Exception;

    /**
     *  往历史表添加每日消费信息和计算成功的每人消费信息
     */
    void addHistoryData() throws Exception;

    /**
     * 计算每次消费的上车信息
     * @return 每个人每日消费的信息
     */
    List<Card> calculateOnStationInfo();

    /**
     * 可视化分析统一处理
     * @throws Exception
     */
    void analyse() throws Exception;

    /**
     * 过期的站距表和历史消费数据的统一清理
     * @throws Exception
     */
    void cleanExpireData() throws Exception;

    void addRecentStationRanges();

    void addCarTrailCache();

    void addStationDirectionCache();

    void addLineStationCache();

    /***
     * 计算成功算出消费数据的概率
     */
    void handleCardsChanceValue();

    /**
     * 总人数（去重后的总卡数）
     * @return
     */
    long getPersonPayRecordsSize();

    /**
     * 统计每种算法算出的概率及总概率
     */
    void calculateChance();

}
