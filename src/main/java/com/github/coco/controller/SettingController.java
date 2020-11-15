package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/setting")
public class SettingController extends BaseController {
    @WebLog
    @PostMapping(value = "/install")
    public Map<String, Object> install(@RequestBody Map<String, Object> params) {
        return null;
    }
}
