package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.DbConstant;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.WhetherEnum;
import com.github.coco.entity.Host;
import com.github.coco.service.HostService;
import com.github.coco.utils.EnumHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/host")
public class HostController extends BaseController {
    @Resource
    private HostService hostService;

    @WebLog
    @PostMapping("/create")
    public Map<String, Object> createHost(@RequestBody Map<String, Object> params) {
        String ip = params.get("ip").toString();
        Integer port = Integer.parseInt(Objects.toString(params.get("port"), "22"));
        String user = Objects.toString(params.get("user"), "root");
        String password = params.get("password").toString();
        Host host = Host.builder()
                        .ip(ip)
                        .port(port)
                        .user(user)
                        .password(password)
                        .build();
        hostService.createHost(host);
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, null);
    }

    @WebLog
    @PostMapping("/remove")
    public Map<String, Object> removeHost(@RequestBody Map<String, Object> params) {
        String id = params.get("id").toString();
        Host host = Host.builder().id(id).build();
        hostService.removeHost(host);
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, null);
    }

    @WebLog
    @PostMapping("/modify")
    public Map<String, Object> modifyHost(@RequestBody Map<String, Object> params) {
        Host host = JSON.parseObject(JSON.toJSONString(params), Host.class);
        hostService.modifyHost(host);
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, null);
    }

    @WebLog
    @PostMapping("/inspect")
    public Map<String, Object> getHost(@RequestBody Map<String, Object> params) {
        Host host = JSON.parseObject(JSON.toJSONString(params), Host.class);
        hostService.modifyHost(host);
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, null);
    }

    @WebLog
    @PostMapping("/list")
    public Map<String, Object> getPageHosts(@RequestBody Map<String, Object> params) {
        int pageNo = Integer.parseInt(params.getOrDefault("pageNo", 1).toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", 10).toString());
        return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                           apiResponseDTO.tableResult(pageNo, pageSize, hostService.getHosts()));
    }

    @WebLog
    @PostMapping("/dockerized")
    public Map<String, Object> dockerizedHost(@RequestBody Map<String, Object> params) {
        String id = params.get("id").toString();
        Host host = hostService.getHostById(id);
        if (EnumHelper.getEnumType(WhetherEnum.class, host.getDockerized().toString()).getValue()) {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "主机已Docker化");
        } else {
            // TODO 对宿主机进行Docker化
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, null);
        }
    }
}
