package com.netease.onmyouji.controller;

import com.netease.onmyouji.entity.Shikigami;
import com.netease.onmyouji.service.ShikigamiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@SessionAttributes({"Shikigami"})
@RequestMapping(value = "api/*")
public class ShikigamiController {

    @Autowired
    private ShikigamiService shikigamiService;

    @RequestMapping(value = "shikigami", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public HashMap<String, Object> getShikigami() {
        List<Shikigami> shikigamis = shikigamiService.getAllShikigamis();
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("shikigamis", shikigamis);
        return resultMap;
    }
}