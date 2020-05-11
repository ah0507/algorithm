package net.chensee.common;

import java.math.BigDecimal;

/**
 * @author ah
 * @title: 保留三位小数
 * @date 2020/1/13 9:20
 */
public class DoubleHandleUtil {

    public static double convertTo3Decimal(Double value){
        BigDecimal b = new BigDecimal(value);
        return b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
