package net.chensee.entity.po.analyse.line;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author ah
 * @title: 统计每天每条线路每个方向每个站点的上下车、在车上、换乘人数
 * @date 2019/12/26 14:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_l_station_info_statistics")
public class EachLineStationInfoStatisticsPo extends LineBasePo{

    /**
     * 站点编号
     */
    private Integer stationNo;

    /**
     * 站点名称
     */
    private String stationName;
    /**
     * 上车人数
     */
    private Integer getOnCount;

    /**
     * 下车人数
     */
    private Integer getOffCount;

    /**
     * 在车上的人数
     */
    private Integer peopleCount;

    /**
     * 换乘人数
     */
    private Integer transferCount;
}
