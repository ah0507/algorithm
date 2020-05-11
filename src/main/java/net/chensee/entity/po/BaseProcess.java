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
@Document(value = "od_baseProcess")
public class BaseProcess {
    @Id
    private String id;

    private String taskGroupName;

    private String taskName;

    private Date startTime;

    private Date endTime;

    private String dataTime;

    private Double chanceValue;

}
