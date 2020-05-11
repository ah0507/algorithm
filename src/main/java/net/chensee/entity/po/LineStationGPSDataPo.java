package net.chensee.entity.po;

import lombok.Data;

@Data
public class LineStationGPSDataPo {

    private String lineNo;

    private String lineName;

    private String direction;

    private Integer stationNo;

    private String stationName;

    private Double lat;

    private Double lng;
}
