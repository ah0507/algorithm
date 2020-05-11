package net.chensee.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "od_configProperties")
public class ConfigProperties {

    @Id
    private String id;

    private Integer pageNumber;

    private Integer pageSize;
    //进站时间阈值(秒)
    private Integer inTimeValue;
    //离站时间阈值(秒)
    private Integer outTimeValue;
    //刷卡时间和进出站时间差值的阈值(秒)
    private Integer addTimeValue;
    //进出站时间增加的阈值(秒)
    private Integer addInAndOutTimeValue;
    //终点站的进站时间的阈值(秒)
    private Integer endStationTimeValue;
    //最近站距的阈值(米)
    private Double distanceValue;
    //规律出行一段时间范围(天)
    private Integer regularTravelTimeValue;
    //规律出行的概率阈值
    private Double regularTravelChanceValue;
    //票款扩样系数
    private Double addTicketValue;
    //每辆车额定人数
    private Integer ratedNumber;
    //算法每日运行时间段
    private String calculateHandleTime;
    //数据过期清理机制(天)
    private Integer expireTimeRangeValue;

    private Date createTime;
}
