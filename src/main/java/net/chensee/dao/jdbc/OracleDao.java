package net.chensee.dao.jdbc;

import net.chensee.entity.po.Card;
import net.chensee.entity.po.CompanyToLinePo;
import net.chensee.entity.po.LineStation;
import net.chensee.entity.po.StationGPSData;
import net.chensee.entity.vo.CarTrail;

import java.util.List;

public interface OracleDao {

    List<Card> getCITYCARDTRADE(int pageNumber, int pageSize, String queryTime);

    List<Card> getMOTCARDTRADE(int pageNumber, int pageSize,String queryTime);

    List<Card> getCPUCardMTRADE(int pageNumber, int pageSize,String queryTime);

    List<Card> getCPUCardTRADE(int pageNumber, int pageSize,String queryTime);

    List<Card> getCardTRADE(int pageNumber, int pageSize,String queryTime);

    List<Card> getMCardTRADE(int pageNumber, int pageSize,String queryTime);

    List<Card> getUPAYCARDTRADE(int pageNumber, int pageSize,String queryTime);

    List<Card> getUPAYQRTRADE(int pageNumber, int pageSize,String queryTime);

    List<Card> getWXMINITRADE(int pageNumber, int pageSize,String queryTime);

    List<Card> getALIPAYTRADE(int pageNumber, int pageSize,String queryTime);

    int isHaveCITYCARDTRADEPage(String deptNo, String queryTime, Integer page, Integer pageSize);

    /**
     * 获得公司对应的线路集合
     * @return
     */
    List<CompanyToLinePo> getCompanyToLinePos();


    /**
     *  获得车辆运行轨迹（运调数据）
     * @param pageNumber
     * @param pageSize
     * @param queryTime
     * @return
     */
    List<CarTrail> getBusInfo(int pageNumber, int pageSize, String queryTime);

    /**
     * 获得站点GPS数据
     * @param pageNumber
     * @param pageSize
     * @return
     */
    List<StationGPSData> getStationGPSData(int pageNumber, int pageSize);

    /**
     * 获得每条线路开始站点和结束站点
     * @return
     */
    List<LineStation> getLineStations();

    /**
     * 获得开始站点的名称
     * @param lineNo
     * @param direction
     * @param startStation
     * @return
     */
    String getStartStationName(String lineNo, int direction, Integer startStation);

    /**
     * 获得结束站点的名称
     * @param lineNo
     * @param direction
     * @param endStation
     * @return
     */
    String getEndStationName(String lineNo, int direction, Integer endStation);
}
