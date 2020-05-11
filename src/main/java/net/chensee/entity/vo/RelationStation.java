package net.chensee.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelationStation {

    /**
     * 关联站点
     */
    private Integer stationNo;

    /**
     * 关联站点名称
     */
    private String stationName;

    /**
     * 出行人数
     */
    private Integer tripCount;

}
