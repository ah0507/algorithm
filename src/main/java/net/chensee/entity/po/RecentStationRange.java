package net.chensee.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chensee.entity.vo.EachRecentStationRange;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

/**
 * 最近站距表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_recentStationRange")
public class RecentStationRange{

    @Id
    //站点唯一标识 由（lineNo,stationNo,direction）组成
    @Field("stationUniqueKey")
    private String stationUniqueKey;

    private double distanceValue;

    private List<EachRecentStationRange> rangeList;

    private Date createTime;

}