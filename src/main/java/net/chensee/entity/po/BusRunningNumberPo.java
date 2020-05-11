package net.chensee.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author ah
 * @title: 车辆运行次数
 * @date 2019/12/27 10:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "od_bus_running_number")
public class BusRunningNumberPo {

    @Id
    private String id;

    private String lineNo;

    private String direction;

    private String busNo;

    /**
     * 运行次数
     */
    private Integer busRunningNumber;

    private Date queryTime;

    private Date createTime;
}
