package net.chensee.dao.ehcache;

import net.chensee.entity.po.RecentStationRange;
import net.chensee.entity.vo.EachRecentStationRange;

import java.util.List;

public interface RecentStationRageEhCache {

    void batchAdd(List<RecentStationRange> recentStationRangeList);

    List<EachRecentStationRange> get(String stationUniqueKey);

    void batchRemove();

    int getSize();
}
