package net.chensee.entity.po.analyse.line;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author ah
 * @title: 用于排序计算在车上人数
 * @date 2019/12/26 14:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_l_station_info_statistics_temp")
public class EachLineStationInfoStatisticsTempPo extends LineBasePo{

    private String lineStationDirection;

    private Integer stationNo;

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
