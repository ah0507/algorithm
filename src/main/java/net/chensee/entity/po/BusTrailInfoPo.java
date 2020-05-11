package net.chensee.entity.po;

import lombok.Data;

import java.util.Date;

@Data
public class BusTrailInfoPo {

    //车辆号
    private String BusNo;
    //进站时间
    private Date inTime;
    //出站时间
    private Date outTime;
    //线路号
    private String lineNo;
    //站点序号
    private Integer stationNo;
    //上下行标识
    private String direction;
    private Date createTime;
}
