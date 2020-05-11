package net.chensee.entity.po.analyse.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author ah
 * @title: 每日出行平均乘坐站数、平均在车上时间、平均乘车次数
 * @date 2020/1/2 10:06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_group_avg_statistics")
public class AvgStatisticsPo {

    private String id;

    /**
     * 消费乘坐总站数
     */
    private Long getOnStationTotal;

    /**
     * 消费乘车所用总时间
     */
    private Long onTimeTotal;

    /**
     *  成功算出下车信息的消费数据总数
     */
    private Long successCalculateGetOffTotal;

    /**
     * 人数（也就是卡去重后的数量）
     */
    private Long personPayRecordCount;

    /**
     * 出行平均乘坐站数(消费乘坐的站数总和/算出来的总消费次数)
     */
    private Double avgGetOnStationCount;

    /**
     * 平均在车上时间(乘车所用时间总和/算出来的总消费次数)
     */
    private Double avgOnTime;

    /**
     * 平均乘车次数(算出来的总消费次数/人数（也就是卡去重后的数量）)
     */
    private Double avgRideCount;

    private Date queryTime;

    private String queryTimeStr;

    private Date createTime;
}
