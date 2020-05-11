package net.chensee.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 每个人每日消费数量和成功算出下车数量
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "od_userStatistics")
public class UserStatistics {
    @Id
    private String id;
    private String userId;
    private int allCardsCount;
    private int completeCardsCount;
}
