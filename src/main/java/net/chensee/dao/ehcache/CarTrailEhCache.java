package net.chensee.dao.ehcache;

import net.chensee.entity.vo.CarTrail;
import net.chensee.entity.po.CarTrailMap;

import java.util.List;

public interface CarTrailEhCache {

    void batchAdd(List<CarTrailMap> carTrails);

    List<CarTrail> get(String busNo);

    void batchRemove();

    void update(String busNo, List<CarTrail> carTrailList);

    int getSize();
}
