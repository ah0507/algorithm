package net.chensee.entity.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author ah
 * @title: TempAnalyseTaskPo
 * @date 2020/1/10 11:01
 */
@Data
@Document(value = "od_temp_calculate_task")
public class TempCalculateTaskPo {

    @Id
    private String id;
    /**
     * 查询日期（2019-01-02）
     */
    private String queryTime;

    /**
     * 算法执行时间
     */
    private Date executeTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 状态：0 未执行，1 已执行
     */
    private Integer status;

}
