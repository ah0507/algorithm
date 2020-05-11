package net.chensee.dao.ehcache;

import net.chensee.entity.po.LineStation;

import java.util.List;

public interface LineStationEhCache {

    void batchAdd(List<LineStation> lineStations);

    LineStation get(String lineNoAndDirection);

    void batchRemove();

    int getSize();
}
