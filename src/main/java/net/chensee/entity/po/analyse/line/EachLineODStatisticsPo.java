package net.chensee.entity.po.analyse.line;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chensee.entity.vo.RelationStation;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @author ah
 * @title: OD分析 每天每条线路每个方向的每个站点的上下车人数及其两辆站点间出行人数
 * @date 2019/12/26 14:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "od_l_od_statistics")
public class EachLineODStatisticsPo extends LineBasePo{

    /**
     * 站点编号
     */
    private Integer stationNo;

    /**
     * 站点名称
     */
    private String stationName;

    /**
     * 该线路其他站点的信息
     */
    private List<RelationStation> relationStationList;

    /**
     * 上车人数
     */
    private Integer getOnCount;

    /**
     * 下车人数
     */
    private Integer getOffCount;
}



