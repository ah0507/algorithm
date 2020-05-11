package net.chensee.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author gh
 * 多数据源配置
 */
@Component
public class DataSourceConfig {

    @Autowired
    private Environment env;

    //destroy-method="close"的作用是当数据库连接不使用的时候,就把该连接重新放到数据池中,方便下次使用调用.
    @Bean(destroyMethod =  "close")
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        dataSource.setInitialSize(2);//初始化时建立物理连接的个数
        dataSource.setMaxActive(20);//最大连接池数量
        dataSource.setMinIdle(0);//最小连接池数量
        dataSource.setMaxWait(60000);//获取连接时最大等待时间，单位毫秒。
        dataSource.setValidationQuery("SELECT 1 FROM DUAL");//用来检测连接是否有效的sql
        dataSource.setTestOnBorrow(false);//申请连接时执行validationQuery检测连接是否有效
        dataSource.setTestWhileIdle(true);//建议配置为true，不影响性能，并且保证安全性。
        dataSource.setPoolPreparedStatements(false);//是否缓存preparedStatement，也就是PSCache
        return dataSource;
    }

//    @Bean(name = "primaryDataSource")
//    //该注解指定注入的Bean的名称，Spring框架使用byName方式寻找合格的 bean，这样就消除了byType方式产生的歧义
//    @Qualifier("primaryDataSource")
//    //读取配置文件里前缀 为"spring.datasource.primary"的语句
//    @ConfigurationProperties(prefix = "spring.datasource.primary")
//    public DataSource primaryDataSource() {
//        return DataSourceBuilder.create().build();
//
//    }
//
//    @Bean(name = "secondaryDataSource")
//    @Qualifier("secondaryDataSource")
//    //有时候我们能保证同一个类型在spring容器中只有一个实例，有时候我们保证不了，此时不讨论by name注入。这个时候@Primary注解就非常重要了
//    @Primary
//    @ConfigurationProperties(prefix = "spring.datasource.secondary")
//    public DataSource secondaryDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "primaryJdbcTemplate")
//    public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
//        return new JdbcTemplate(dataSource);
//    }
//
//    @Bean(name = "secondaryJdbcTemplate")
//    public JdbcTemplate secondaryJdbcTemplate(@Qualifier("secondaryDataSource") DataSource dataSource) {
//        return new JdbcTemplate(dataSource);
//    }
}
