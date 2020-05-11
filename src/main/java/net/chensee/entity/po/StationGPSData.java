package net.chensee.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "od_stationGPSData")
public class StationGPSData {

    @Id
    private String id;

    private Integer stationNo;

    private String stationUniqueKey;

    private String stationName;

    private String lineNo;

    private String direction;

    private Double lat;

    private Double lng;

}
