package net.chensee.entity.po.analyse.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author ah
 * @title: 每天每个公司每种卡类型的消费人数的统计（ 查询每种消费方式和全部消费的占比）
 * @date 2019/12/25 11:08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_cp_card_type_consume_statistics")
public class EachCompCardTypeConsumeStatisticsPo extends CompanyBasePo {

    /**
     * 卡类型
     */
    private String cardType;

    /**
     * 消费人数
     */
    private Integer consumeCount;

}
