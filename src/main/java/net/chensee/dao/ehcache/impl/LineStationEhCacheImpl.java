package net.chensee.dao.ehcache.impl;

import net.chensee.dao.ehcache.LineStationEhCache;
import net.chensee.entity.po.LineStation;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
@Component
@CacheConfig(cacheNames = "lineStation")
public class LineStationEhCacheImpl implements LineStationEhCache{

    @Resource
    private CacheManager cacheManager;

    private Cache cacheOpr;

    @PostConstruct
    private void init() {
        cacheOpr = cacheManager.getCache("lineStation");
    }

    @Override
    public void batchAdd(List<LineStation> lineStations) {
        for (LineStation lineStation : lineStations) {
            cacheOpr.put(lineStation.getLineNo()+","+lineStation.getDirection(),lineStation);
        }
    }

    @Override
    public LineStation get(String lineNoAndDirection) {
        return cacheOpr.get(lineNoAndDirection,LineStation.class);
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
