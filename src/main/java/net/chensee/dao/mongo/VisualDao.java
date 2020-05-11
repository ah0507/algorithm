package net.chensee.dao.mongo;

import net.chensee.entity.po.BaseProcess;
import net.chensee.entity.po.ConfigProperties;

import java.util.Date;

public interface VisualDao {

    ConfigProperties getConfig(Date queryTime);

    void addProcess(BaseProcess baseProcess);

    void delOldProcesses(String queryTime);

    boolean isExistBaseProcess(String dataTime);
}
