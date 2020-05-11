package net.chensee.entity.po.analyse.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author ah
 * @title: 统计每天每个公司五个峰值的消费数据
 * @date 2019/12/25 10:35
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_cp_five_peak_statistics")
public class EachCompFivePeakStatisticsPo extends CompanyBasePo {

    /**
     * 消费人数
     */
    private Integer consumeCount;

    /**
     * 峰值名称
     */
    private String scopeTimeName;

    private Date startTime;

    private Date endTime;

}
