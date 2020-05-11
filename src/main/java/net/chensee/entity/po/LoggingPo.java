package net.chensee.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chensee.entity.vo.CarTrail;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "od_logging")
public class LoggingPo {

    @Id
    private String id;

    private String exceptionName;

    private String exceptionContent;

    private Card card;

    private CarTrail carTrail;

    private String exceptionDetail;

    private Date createTime;

}
