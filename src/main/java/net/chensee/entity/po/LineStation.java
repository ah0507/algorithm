package net.chensee.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_lineStation")
public class LineStation implements Serializable{

    @Id
    private String id;

    private String lineNo;

    private String direction;

    private Integer startStation;

    private String startStationName;

    private Integer endStation;

    private String endStationName;

    private Date createTime;

}
