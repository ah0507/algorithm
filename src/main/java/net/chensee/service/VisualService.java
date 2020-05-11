package net.chensee.service;

import net.chensee.entity.po.BaseProcess;
import net.chensee.entity.po.ConfigProperties;

import java.util.Date;

public interface VisualService {

    ConfigProperties getConfig(Date queryTime);

    void addProcess(BaseProcess baseProcess) throws Exception;

    void delOldProcesses(String queryTime);

    boolean isExistBaseProcess(String dataTime);
}
