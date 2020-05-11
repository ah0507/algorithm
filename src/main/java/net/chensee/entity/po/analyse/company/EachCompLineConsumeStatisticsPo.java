package net.chensee.entity.po.analyse.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author ah
 * @title: 统计每天每个公司每条线路的消费人数(线路排名)
 * @date 2019/12/25 11:17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_cp_line_consume_statistics")
public class EachCompLineConsumeStatisticsPo extends CompanyBasePo {

    /**
     * 线路号
     */
    private String lineNo;

    /**
     * 消费人数
     */
    private Integer consumeCount;
}
