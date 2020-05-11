package net.chensee.dao.ehcache.impl;

import net.chensee.dao.ehcache.StationDirectionEhcache;
import net.chensee.entity.po.StationDirectionPo;
import net.chensee.entity.po.StationDirection;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Component
@CacheConfig(cacheNames = "stationDirection")
public class StationDirectionEhcacheImpl implements StationDirectionEhcache{

    @Resource
    private CacheManager cacheManager;

    private Cache cacheOpr;

    @PostConstruct
    private void init() {
        cacheOpr = cacheManager.getCache("stationDirection");
    }

    @Override
    public void batchAdd(List<StationDirectionPo> stationDirectionPos) {
        for (StationDirectionPo stationDirectionPo : stationDirectionPos) {
            cacheOpr.put(stationDirectionPo.getLineNoAndStationNo(),stationDirectionPo.getStationDirections());
        }
    }

    @Override
    public List<StationDirection> get(String lineAndStation) {
        List<StationDirection> stationDirections = null;
        Cache.ValueWrapper valueWrapper = cacheOpr.get(lineAndStation);
        if (valueWrapper != null) {
            stationDirections = (List<StationDirection>) valueWrapper.get();
        }
        return stationDirections;
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
