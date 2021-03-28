package com.github.coco.controller;

import com.github.coco.annotation.WebLog;
import com.github.coco.constant.ErrorConstant;
import com.github.coco.constant.GlobalConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * @author Yan
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/file")
public class FileController extends BaseController {
    @WebLog
    @PostMapping(value = "/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return apiResponseDTO.returnResult(ErrorConstant.ERR_API_INVOKE, "上传失败，请选择文件");
            } else {
                String uuid = UUID.randomUUID().toString();
                IOUtils.copy(file.getInputStream(), new FileOutputStream(String.format("%s/%s.data",
                                                                                       GlobalConstant.TEMP_STORAGE_PATH,
                                                                                       uuid)));
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, uuid);
            }
        } catch (IOException e) {
            log.error("上传文件发生异常", e);
            return apiResponseDTO.returnResult(ErrorConstant.ERR_API_INVOKE, "上传文件发生异常");
        }
    }
}
