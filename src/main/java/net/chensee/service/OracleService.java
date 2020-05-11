package net.chensee.service;

import net.chensee.entity.po.CompanyToLinePo;
import net.chensee.entity.po.RecentStationRange;
import net.chensee.entity.po.StationGPSData;
import net.chensee.entity.vo.CarTrail;

import java.util.List;
import java.util.Map;

public interface OracleService {

    /**
     * 城市卡消费
     *
     * @return
     */
    void getCITYCARDTRADE() throws Exception;

    /**
     * 交通部卡，京津冀
     *
     * @return
     */
    void getMOTCARDTRADE() throws Exception;

    /**
     * CPU月票交易数据
     *
     * @return
     */
    void getCPUCardMTRADE() throws Exception;

    /**
     * CPU客票交易数据
     *
     * @return
     */
    void getCPUCardTRADE() throws Exception;

    /**
     * 客票交易数据
     *
     * @return
     */
    void getCardTRADE() throws Exception;

    /**
     * 月票交易数据
     *
     * @return
     */
    void getMCardTRADE() throws Exception;

    /**
     * 银联卡数据
     *
     * @return
     */
    void getUPAYCARDTRADE() throws Exception;

    /**
     * 银联码数据
     *
     * @return
     */
    void getUPAYQRTRADE() throws Exception;

    /**
     * 微信数据
     *
     * @return
     */
    void getWXMINITRADE() throws Exception;

    /**
     * 支付宝数据
     *
     * @return
     */
    void getALIPAYTRADE() throws Exception;

    /**
     * 每日刷卡总数据
     */
    void getTodayCards() throws Exception;

    /**
     * 获得公司线路对应表
     * @return
     */
    List<CompanyToLinePo> getCompanyToLinePos();

    /**
     * 最近站距表总数居
     * @return
     */
    List<RecentStationRange> getRecentStationRanges();

    /**
     * 每日车辆轨迹
     * @throws
     */
    Map<String, List<CarTrail>> getAllCarTrails();

    List<StationGPSData> getStationGpsData();
}
