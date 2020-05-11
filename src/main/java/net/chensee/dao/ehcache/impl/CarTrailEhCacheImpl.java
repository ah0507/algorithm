package net.chensee.dao.ehcache.impl;

import net.chensee.dao.ehcache.CarTrailEhCache;
import net.chensee.entity.vo.CarTrail;
import net.chensee.entity.po.CarTrailMap;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Component
@CacheConfig(cacheNames = "carTrail")
public class CarTrailEhCacheImpl implements CarTrailEhCache {

    @Resource
    private EhCacheCacheManager cacheManager;

    private Cache cacheOpr;

    @PostConstruct
    private void init() {
        cacheOpr = cacheManager.getCache("carTrail");
    }

    @Override
    public void batchAdd(List<CarTrailMap> carTrails) {
        for (CarTrailMap carTrailMap : carTrails) {
            cacheOpr.put(carTrailMap.getBusNo(),carTrailMap.getCarTrails());
        }
    }

    @Override
    public List<CarTrail> get(String busNo) {
        List<CarTrail> carTrails = null;
        Cache.ValueWrapper valueWrapper = cacheOpr.get(busNo);
        if (valueWrapper != null) {
            carTrails = (List<CarTrail>) valueWrapper.get();
        }
        return carTrails;
    }

    @Override
    public void batchRemove() {
        cacheOpr.clear();
    }

    @Override
    public void update(String busNo, List<CarTrail> carTrailList) {
        cacheOpr.put(busNo,carTrailList);
    }

    @Override
    public int getSize() {
        net.sf.ehcache.Cache nativeCache = (net.sf.ehcache.Cache) cacheOpr.getNativeCache();
        return nativeCache.getSize();
    }

}
