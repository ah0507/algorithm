package net.chensee.dao.ehcache.impl;

import net.chensee.dao.ehcache.RecentStationRageEhCache;
import net.chensee.entity.po.RecentStationRange;
import net.chensee.entity.vo.EachRecentStationRange;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Component
@CacheConfig(cacheNames = "recentStationRange")
public class RecentStationRageEhCacheImpl implements RecentStationRageEhCache {

    @Resource
    private CacheManager cacheManager;

    private Cache cacheOpr;

    @PostConstruct
    private void init() {
        cacheOpr = cacheManager.getCache("recentStationRange");
    }


    @Override
    public void batchAdd(List<RecentStationRange> recentStationRangeList) {
        for (RecentStationRange recentStationRange : recentStationRangeList) {
            cacheOpr.put(recentStationRange.getStationUniqueKey(),recentStationRange.getRangeList());
        }
    }

    @Override
    public List<EachRecentStationRange> get(String stationUniqueKey) {
        List<EachRecentStationRange> recentStationRanges = null;
        Cache.ValueWrapper valueWrapper = cacheOpr.get(stationUniqueKey);
        if (valueWrapper != null) {
            recentStationRanges = (List<EachRecentStationRange>) valueWrapper.get();
        }
        return recentStationRanges;
    }

    @Override
    public void batchRemove() {
        cacheOpr.clear();
    }

    @Override
    public int getSize() {
        net.sf.ehcache.Cache nativeCache = (net.sf.ehcache.Cache) cacheOpr.getNativeCache();
        return nativeCache.getSize();
    }
}
