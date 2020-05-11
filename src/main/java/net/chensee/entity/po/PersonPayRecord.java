package net.chensee.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * 每个人对应的刷卡记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_personPayRecord")
public class PersonPayRecord {
    @Id
    private String id;
    private String userId;
    private List<Card> cardList;
    private Date createTime;

}
