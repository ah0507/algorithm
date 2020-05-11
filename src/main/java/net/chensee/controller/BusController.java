package net.chensee.controller;

import lombok.extern.slf4j.Slf4j;
import net.chensee.entity.po.resp.ObjectResponse;
import net.chensee.service.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("bus")
@Slf4j
public class BusController {

    @Autowired
    private MongoService mongoService;

    @RequestMapping(value = "/calculate/{queryTime}", method = RequestMethod.GET)
    @ResponseBody
    public ObjectResponse tempCalculate(@PathVariable(value = "queryTime")String queryTime,
                            @RequestParam(value = "executeTime")String executeTime) {
        mongoService.addTempCalculateTaskPo(queryTime, executeTime);
        return ObjectResponse.ok();
    }

}