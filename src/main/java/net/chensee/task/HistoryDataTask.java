package net.chensee.task;

import lombok.extern.slf4j.Slf4j;
import net.chensee.service.BusService;
import net.chensee.service.VisualService;

@Slf4j
public class HistoryDataTask extends AbstractTask{

    public HistoryDataTask(String name, BusService busService, VisualService visualService) {
        this.name = name;
        this.busService = busService;
        this.visualService = visualService;
    }

    @Override
    public void execute() throws Exception {
        busService.addHistoryData();
        busService.cleanExpireData();
    }

}
