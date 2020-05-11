package net.chensee.entity.po.analyse.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author ah
 * @title: 统计每天每个公司每个站点的消费次数（站点消费排名图和热力图）
 * @date 2019/12/25 11:20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_cp_station_consume_statistics")
public class EachCompStationConsumeStatisticsPo extends CompanyBasePo{

    /**
     * 站点名称
     */
    private String stationName;

    /**
     * 经度
     */
    private Double lat;

    /**
     * 纬度
     */
    private Double lng;

    /**
     * 消费人数
     */
    private Integer consumeCount;

    /**
     * 下车人数
     */
    private Integer offCount;

    /**
     * 换乘次数
     */
    private Integer transferCount;
}
