package net.chensee.common;

import net.chensee.entity.po.ConfigProperties;

/**
 * 静态变量缓存
 */
public class Constants {

    /**
     * 数据查询时间
     */
    public static String queryTime;

    /**
     * 算法参数配置
     */
    public static ConfigProperties configProperties;

    /**
     * 计算下车成功率
     */
    public static Double calculateCardsChanceValue;

    /**
     *  可视化分析时统计的时间段范围（每一个小时）
     */
    public static final int TIMEVALUE = 1;

    /**
     * 公司集合
     */
    public static final String[] DEPTNOS = {"01", "02", "03", "04", "07", "08", "12", "15"};
}
