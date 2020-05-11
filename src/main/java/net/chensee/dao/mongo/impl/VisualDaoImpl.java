package net.chensee.dao.mongo.impl;

import net.chensee.dao.mongo.VisualDao;
import net.chensee.entity.po.BaseProcess;
import net.chensee.entity.po.ConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class VisualDaoImpl implements VisualDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ConfigProperties getConfig(Date queryTime) {
        Query query = new Query(Criteria.where("createTime").lte(queryTime))
                .with(new Sort(Sort.Direction.DESC, "createTime"))
                .limit(1);
        ConfigProperties configProperties = mongoTemplate.findOne(query, ConfigProperties.class);
        return configProperties;
    }

    @Override
    public void addProcess(BaseProcess baseProcess) {
        mongoTemplate.save(baseProcess);
    }

    @Override
    public void delOldProcesses(String queryTime) {
        Query query = new Query(Criteria.where("dataTime").is(queryTime));
        mongoTemplate.remove(query, BaseProcess.class);
    }

    @Override
    public boolean isExistBaseProcess(String dataTime) {
        Query query = new Query(Criteria.where("dataTime").is(dataTime)
                                .and("taskName").is("root")
                                .and("chanceValue").exists(true));
        return mongoTemplate.exists(query, BaseProcess.class);
    }
}
