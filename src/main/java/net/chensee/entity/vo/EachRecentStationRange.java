package net.chensee.entity.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class EachRecentStationRange implements Serializable {

    private Integer stationNo;

    private String stationUniqueKey;

    private String stationName;

    private String direction;

    private String lineNo;

    private Double distance;
}
