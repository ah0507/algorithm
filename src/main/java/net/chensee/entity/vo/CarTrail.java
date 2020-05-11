package net.chensee.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 车辆轨迹
 */
@Data
public class CarTrail implements Serializable {

    private String id;
    //车辆号
    private String BusNo;
    //进站时间
    private Date inTime;
    //出站时间
    private Date outTime;
    //线路号
    private String lineNo;
    //站点唯一标识
    private String stationUniqueKey;
    //站点序号
    private Integer stationNo;
    //站点名称
    private String stationName;
    //上下行标识
    private String direction;

}
