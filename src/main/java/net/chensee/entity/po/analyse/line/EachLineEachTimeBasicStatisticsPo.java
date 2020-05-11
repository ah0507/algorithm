package net.chensee.entity.po.analyse.line;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author ah
 * @title: 统计每天每个时间段（每隔一小时）每条线路每个方向的上车人数、下车人数、换乘人数
 * @date 2019/12/26 14:26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_l_each_time_basic_statistics")
public class EachLineEachTimeBasicStatisticsPo extends LineBasePo{

    /**
     * 上车人数
     */
    private Integer getOnCount;
    /**
     * 下车人数
     */
    private Integer getOffCount;
    /**
     * 换乘人数
     */
    private Integer transferCount;
    /**
     * 当前时间（前一小时到当前时间的时间段）
     */
    private Date currentTime;

    private String currentTimeStr;

}
