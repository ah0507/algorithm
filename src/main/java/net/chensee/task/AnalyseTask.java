package net.chensee.task;

import lombok.extern.slf4j.Slf4j;
import net.chensee.service.BusService;
import net.chensee.service.VisualService;

@Slf4j
public class AnalyseTask extends AbstractTask {

    public AnalyseTask(String name, BusService busService, VisualService visualService) {
        this.name = name;
        this.busService = busService;
        this.visualService = visualService;
    }

    @Override
    public void execute() throws Exception {
        busService.analyse();
    }

    @Override
    public void finish() {
    }
}
