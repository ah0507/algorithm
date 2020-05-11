package net.chensee.dao.ehcache;

import net.chensee.entity.po.StationDirectionPo;
import net.chensee.entity.po.StationDirection;

import java.util.List;

public interface StationDirectionEhcache {

    void batchAdd(List<StationDirectionPo> stationDirectionPos);

    List<StationDirection> get(String lineAndStation);

    void batchRemove();

    int getSize();
}
