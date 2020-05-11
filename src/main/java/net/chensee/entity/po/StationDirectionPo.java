package net.chensee.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 *  每条线路每个站点名称 对应上下行不同站点编号不同
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "od_stationDirection")
public class StationDirectionPo {

    @Id
    private String id;
    private String lineNoAndStationNo;
    private List<StationDirection> stationDirections;
}
