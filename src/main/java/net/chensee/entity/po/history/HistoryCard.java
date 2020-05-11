package net.chensee.entity.po.history;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "od_history_card")
public class HistoryCard{

    @Id
    private String id;
    //用户
    private String userId;
    //卡号
    private String cardId;
    //卡类型
    private String cardType;
    //刷卡时间
    private Date payTime;
    //公司号
    private String deptNo;
    //线路号
    private String lineNo;
    //车辆号
    private String busNo;
    //上下行标识
    private String direction;
    //上车站点的算法类型
    private Integer onStationHandleType;
    //上车站点编号
    private Integer onStationNo;
    //上车站点唯一标识
    private String onStationUniqueKey;
    //上车站点名称
    private String onStationName;
    //下车时间
    private Date offTime;
    //下车车站点编号
    private Integer offStationNo;
    //下车站点唯一标识
    private String offStationUniqueKey;
    //下车站点名称
    private String offStationName;

    private Integer offStationHandleType;

    private String transferId;

    private Date createTime;

}
