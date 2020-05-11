package net.chensee.config;

import net.sf.ehcache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author gh
 * 缓存配置
 */
@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean(){
        EhCacheManagerFactoryBean factoryBean = new EhCacheManagerFactoryBean();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource ehcacheResource = resolver.getResource("classpath:ehcache.xml");
        factoryBean.setConfigLocation(ehcacheResource);
        factoryBean.setShared(true);
        factoryBean.setCacheManagerName("ehcacheManager");
        return factoryBean;
    }

    @Bean
    public EhCacheCacheManager mCacheManager(CacheManager cacheManager) {
        return new EhCacheCacheManager(cacheManager);
    }
}
