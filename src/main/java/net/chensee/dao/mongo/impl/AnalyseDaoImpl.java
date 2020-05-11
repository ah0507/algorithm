package net.chensee.dao.mongo.impl;

import net.chensee.dao.mongo.AnalyseDao;
import net.chensee.entity.po.analyse.company.*;
import net.chensee.entity.po.analyse.group.AvgStatisticsPo;
import net.chensee.entity.po.analyse.line.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class AnalyseDaoImpl implements AnalyseDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void addEachLineODStatisticsPos(List<EachLineODStatisticsPo> eachLineODStatisticsPos) {
        mongoTemplate.insertAll(eachLineODStatisticsPos);
    }

    @Override
    public void createEachLineODStationInfoIndex() {
        boolean b = mongoTemplate.collectionExists(EachLineODStatisticsPo.class);
        if (!b) {
            mongoTemplate.indexOps(EachLineODStatisticsPo.class)
                    .ensureIndex(new Index()
                            .on("lineNo", Sort.Direction.ASC)
                            .on("queryTime", Sort.Direction.ASC));
        }
    }

    @Override
    public void addEachLineBasicStatisticsPos(List<EachLineBasicStatisticsPo> eachLineBasicStatisticsPos) {
        mongoTemplate.insertAll(eachLineBasicStatisticsPos);
    }

    @Override
    public void createEachLineStationInfoStatisticsTempPosIndex() {
        mongoTemplate.dropCollection(EachLineStationInfoStatisticsTempPo.class);
        boolean b = mongoTemplate.collectionExists(EachLineStationInfoStatisticsTempPo.class);
        if (!b) {
            mongoTemplate.indexOps(EachLineStationInfoStatisticsTempPo.class)
                    .ensureIndex(new Index()
                            .on("lineNo", Sort.Direction.ASC)
                            .on("stationNo", Sort.Direction.ASC)
                            .on("queryTime", Sort.Direction.ASC));
        }
    }

    @Override
    public void addEachLineStationInfoStatisticsTempPos(List<EachLineStationInfoStatisticsTempPo> estps) {
        mongoTemplate.insertAll(estps);
    }

    @Override
    public List<EachLineStationInfoStatisticsTempPo> getEachLineStationInfoStatisticsTempPosOrder(Integer page, Integer pageSize) {
        Sort sort = new Sort(Sort.Direction.ASC, "queryTime","lineNo","stationNo");
        Query query = new Query()
                .with(sort)
                .skip((page - 1) * pageSize).limit(pageSize);
        return mongoTemplate.find(query, EachLineStationInfoStatisticsTempPo.class);
    }

    @Override
    public void addEachLineStationInfoStatisticsPos(List<EachLineStationInfoStatisticsPo> espList) {
        mongoTemplate.insertAll(espList);
    }

    @Override
    public void createEachLineStationInfoStatisticsPosIndex() {
        boolean b = mongoTemplate.collectionExists(EachLineStationInfoStatisticsPo.class);
        if (!b) {
            mongoTemplate.indexOps(EachLineStationInfoStatisticsPo.class)
                    .ensureIndex(new Index()
                            .on("lineNo",Sort.Direction.ASC)
                            .on("stationNo",Sort.Direction.ASC)
                            .on("queryTime", Sort.Direction.ASC));
        }
    }

    @Override
    public void createEachLineEachTimeBasicStatisticsPosIndex() {
        boolean b = mongoTemplate.collectionExists(EachLineEachTimeBasicStatisticsPo.class);
        if (!b) {
            mongoTemplate.indexOps(EachLineEachTimeBasicStatisticsPo.class)
                    .ensureIndex(new Index()
                            .on("lineNo",Sort.Direction.ASC)
                            .on("queryTime", Sort.Direction.ASC));
        }
    }

    @Override
    public void addEachLineEachTimeBasicStatisticsPos(List<EachLineEachTimeBasicStatisticsPo> etbspList) {
        mongoTemplate.insertAll(etbspList);
    }

    @Override
    public void createEachLineFivePeakStatisticsPoIndex() {
        boolean b = mongoTemplate.collectionExists(EachLineFivePeakStatisticsPo.class);
        if (!b) {
            mongoTemplate.indexOps(EachLineFivePeakStatisticsPo.class)
                    .ensureIndex(new Index()
                            .on("lineNo", Sort.Direction.ASC)
                            .on("startTime", Sort.Direction.ASC)
                            .on("queryTime", Sort.Direction.ASC));
        }
    }

    @Override
    public void addEachLineFivePeakStatisticsPos(List<EachLineFivePeakStatisticsPo> efppList) {
        mongoTemplate.insertAll(efppList);
    }

    @Override
    public void addEachCompBasicStatisticsPos(List<EachCompBasicStatisticsPo> ebspList) {
        boolean b = mongoTemplate.collectionExists(EachCompBasicStatisticsPo.class);
        if (!b) {
            mongoTemplate.indexOps(EachCompBasicStatisticsPo.class)
                    .ensureIndex(new Index()
                            .on("deptNo", Sort.Direction.ASC)
                            .on("queryTime", Sort.Direction.ASC));
        }
        mongoTemplate.insertAll(ebspList);
    }

    @Override
    public void addEachCompCardTypeConsumeStatisticsPos(List<EachCompCardTypeConsumeStatisticsPo> ectcspList) {
        boolean b = mongoTemplate.collectionExists(EachCompCardTypeConsumeStatisticsPo.class);
        if (!b) {
            mongoTemplate.indexOps(EachCompCardTypeConsumeStatisticsPo.class)
                    .ensureIndex(new Index()
                            .on("deptNo", Sort.Direction.ASC)
                            .on("queryTime", Sort.Direction.ASC));
        }
        mongoTemplate.insertAll(ectcspList);
    }

    @Override
    public void addEachCompLineConsumeStatisticsPos(List<EachCompLineConsumeStatisticsPo> elcspList) {
        boolean b = mongoTemplate.collectionExists(EachCompLineConsumeStatisticsPo.class);
        if (!b) {
            mongoTemplate.indexOps(EachCompLineConsumeStatisticsPo.class)
                    .ensureIndex(new Index()
                            .on("deptNo", Sort.Direction.ASC)
                            .on("queryTime", Sort.Direction.ASC));
        }
        mongoTemplate.insertAll(elcspList);
    }

    @Override
    public void addEachCompFivePeakStatisticsPos(List<EachCompFivePeakStatisticsPo> efpspList) {
        boolean b = mongoTemplate.collectionExists(EachCompFivePeakStatisticsPo.class);
        if (!b) {
            mongoTemplate.indexOps(EachCompFivePeakStatisticsPo.class)
                    .ensureIndex(new Index()
                            .on("deptNo", Sort.Direction.ASC)
                            .on("queryTime", Sort.Direction.ASC));
        }
        mongoTemplate.insertAll(efpspList);
    }

    @Override
    public void createEachCompStationConsumeStatisticsPosIndex() {
        boolean b = mongoTemplate.collectionExists(EachCompStationConsumeStatisticsPo.class);
        if (!b) {
            mongoTemplate.indexOps(EachCompStationConsumeStatisticsPo.class)
                    .ensureIndex(new Index()
                            .on("deptNo", Sort.Direction.ASC)
                            .on("queryTime", Sort.Direction.ASC));
        }
    }

    @Override
    public void addEachCompStationConsumeStatisticsPos(List<EachCompStationConsumeStatisticsPo> escsps) {
        mongoTemplate.insertAll(escsps);
    }

    @Override
    public void addAvgStatisticsPo(AvgStatisticsPo avgStatisticsPo) {
        mongoTemplate.insert(avgStatisticsPo);
    }

    @Override
    public void delOldEachCompBasicStatisticsPos(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("queryTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, EachCompBasicStatisticsPo.class);
    }

    @Override
    public void delOldEachCompCardTypeConsumeStatisticsPos(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("queryTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, EachCompCardTypeConsumeStatisticsPo.class);
    }

    @Override
    public void delOldEachCompLineConsumeStatisticsPos(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("queryTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, EachCompLineConsumeStatisticsPo.class);
    }

    @Override
    public void delOldEachCompFivePeakStatisticsPos(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("queryTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, EachCompFivePeakStatisticsPo.class);
    }

    @Override
    public void delOldEachCompStationConsumeStatisticsPos(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("queryTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, EachCompStationConsumeStatisticsPo.class);
    }

    @Override
    public void delOldEachLineBasicStatisticsPos(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("queryTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, EachLineBasicStatisticsPo.class);
    }

    @Override
    public void delOldEachLineODStationInfos(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("queryTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, EachLineODStatisticsPo.class);
    }

    @Override
    public void delOldEachLineStationInfoStatisticsPos(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("queryTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, EachLineStationInfoStatisticsPo.class);
    }

    @Override
    public void delOldEachLineEachTimeBasicStatisticsPos(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("queryTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, EachLineEachTimeBasicStatisticsPo.class);
    }

    @Override
    public void delOldEachLineFivePeakStatisticsPos(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("queryTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, EachLineFivePeakStatisticsPo.class);
    }

    @Override
    public void delAvgStatisticsPo(Date queryTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("queryTime").is(queryTime);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, AvgStatisticsPo.class);
    }
}
