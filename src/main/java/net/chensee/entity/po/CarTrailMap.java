package net.chensee.entity.po;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.chensee.entity.vo.CarTrail;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(value = "od_carTrails")
public class CarTrailMap {

    @Id
    private String id;

    private String busNo;

    private List<CarTrail> carTrails;
}
