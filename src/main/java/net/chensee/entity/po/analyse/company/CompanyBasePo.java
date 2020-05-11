package net.chensee.entity.po.analyse.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * @author ah
 * @title: BasePo
 * @date 2019/12/25 11:12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyBasePo {

    @Id
    private String id;

    /**
     * 公司ID
     */
    private String deptNo;

    /**
     * 查询时间
     */
    private Date queryTime;

    private String queryTimeStr;

    /**
     * 创建时间
     */
    private Date createTime;
}
