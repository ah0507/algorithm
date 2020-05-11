package net.chensee.entity.po.analyse.line;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author ah
 * @title: 统计每天每条线路每个方向五个峰值的消费数据
 * @date 2019/12/26 14:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_l_five_peak_statistics")
public class EachLineFivePeakStatisticsPo extends LineBasePo{

    /**
     * 消费数量
     */
    private int consumeCount;

    /**
     * 峰值
     */
    private String scopeTimeName;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

}
