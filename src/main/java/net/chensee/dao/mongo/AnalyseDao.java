package net.chensee.dao.mongo;

import net.chensee.entity.po.analyse.company.*;
import net.chensee.entity.po.analyse.group.AvgStatisticsPo;
import net.chensee.entity.po.analyse.line.*;

import java.util.Date;
import java.util.List;

public interface AnalyseDao {

    void addEachLineODStatisticsPos(List<EachLineODStatisticsPo> eachLineODStatisticsPos);

    void createEachLineODStationInfoIndex();

    void addEachLineBasicStatisticsPos(List<EachLineBasicStatisticsPo> eachLineBasicStatisticsPos);

    void createEachLineStationInfoStatisticsTempPosIndex();

    void addEachLineStationInfoStatisticsTempPos(List<EachLineStationInfoStatisticsTempPo> estps);

    List<EachLineStationInfoStatisticsTempPo> getEachLineStationInfoStatisticsTempPosOrder(Integer page, Integer pageSize);

    void addEachLineStationInfoStatisticsPos(List<EachLineStationInfoStatisticsPo> espList);

    void createEachLineStationInfoStatisticsPosIndex();

    void createEachLineEachTimeBasicStatisticsPosIndex();

    void addEachLineEachTimeBasicStatisticsPos(List<EachLineEachTimeBasicStatisticsPo> etbspList);

    void createEachLineFivePeakStatisticsPoIndex();

    void addEachLineFivePeakStatisticsPos(List<EachLineFivePeakStatisticsPo> efppList);

    void addEachCompBasicStatisticsPos(List<EachCompBasicStatisticsPo> ebspList);

    void addEachCompCardTypeConsumeStatisticsPos(List<EachCompCardTypeConsumeStatisticsPo> ectcspList);

    void addEachCompLineConsumeStatisticsPos(List<EachCompLineConsumeStatisticsPo> elcspList);

    void addEachCompFivePeakStatisticsPos(List<EachCompFivePeakStatisticsPo> efpspList);

    void createEachCompStationConsumeStatisticsPosIndex();

    void addEachCompStationConsumeStatisticsPos(List<EachCompStationConsumeStatisticsPo> escsps);

    void addAvgStatisticsPo(AvgStatisticsPo avgStatisticsPo);

    void delOldEachCompBasicStatisticsPos(Date queryTime);

    void delOldEachCompCardTypeConsumeStatisticsPos(Date queryTime);

    void delOldEachCompLineConsumeStatisticsPos(Date queryTime);

    void delOldEachCompFivePeakStatisticsPos(Date queryTime);

    void delOldEachCompStationConsumeStatisticsPos(Date queryTime);

    void delOldEachLineBasicStatisticsPos(Date queryTime);

    void delOldEachLineODStationInfos(Date queryTime);

    void delOldEachLineStationInfoStatisticsPos(Date queryTime);

    void delOldEachLineEachTimeBasicStatisticsPos(Date queryTime);

    void delOldEachLineFivePeakStatisticsPos(Date queryTime);

    void delAvgStatisticsPo(Date queryTime);
}
