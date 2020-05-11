package net.chensee.service.impl;

import net.chensee.dao.mongo.VisualDao;
import net.chensee.entity.po.BaseProcess;
import net.chensee.entity.po.ConfigProperties;
import net.chensee.service.VisualService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class VisualServiceImpl implements VisualService{

    @Autowired
    private VisualDao visualDao;

    @Override
    public ConfigProperties getConfig(Date queryTime) {
        return visualDao.getConfig(queryTime);
    }

    @Override
    public void addProcess(BaseProcess baseProcess) throws Exception{
        visualDao.addProcess(baseProcess);
    }

    @Override
    public void delOldProcesses(String queryTime) {
        visualDao.delOldProcesses(queryTime);
    }

    @Override
    public boolean isExistBaseProcess(String dataTime) {
        return visualDao.isExistBaseProcess(dataTime);
    }
}
