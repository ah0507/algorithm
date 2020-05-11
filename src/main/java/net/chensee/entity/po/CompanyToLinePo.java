package net.chensee.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @author ah
 * @title: 每个公司对应线路
 * @date 2019/12/28 10:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "od_company_to_line")
public class CompanyToLinePo {

    @Id
    private String id;

    private String deptNo;

    @Transient
    private String lineNo;

    private List<String> lineNos;
}
