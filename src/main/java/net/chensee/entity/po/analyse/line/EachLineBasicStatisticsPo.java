package net.chensee.entity.po.analyse.line;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

/**
 * @author ah
 * @title: 统计每天每条线路每个方向的换乘次数，拥挤度，消费人数
 * @date 2019/12/26 14:08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_l_basic_statistics")
public class EachLineBasicStatisticsPo extends LineBasePo{

    /**
     * 跑这条线路的车辆号集合
     */
    @Transient
    private Set<String> busNos;

    /**
     * 刷卡扩样系数
     * 计算出下车人数 *（1+useCardValue） = 消费人数
     */
    @Transient
    private Double useCardValue;

    /**
     * 拥挤度：消费次数/（运行次数*车数*每辆车的额定人数）
     */
    private Double ratio;

    /**
     * 换乘次数
     */
    private Integer transferCount;

    /**
     * 消费人数
     */
    private Integer consumeCount;

    /**
     * 下车人数
     */
    @Transient
    private Integer offCount;
}
