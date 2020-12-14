package com.github.coco.controller;

import com.alibaba.fastjson.JSON;
import com.github.coco.annotation.WebLog;
import com.github.coco.constant.DockerConstant;
import com.github.coco.constant.GlobalConstant;
import com.github.coco.constant.dict.ErrorCodeEnum;
import com.github.coco.socket.SocketEventHandle;
import com.github.coco.utils.DockerFilterHelper;
import com.github.coco.utils.LoggerHelper;
import com.github.coco.utils.ThreadPoolHelper;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.exceptions.ImagePullFailedException;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageSearchResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author Yan
 */
@RestController
@RequestMapping(value = "/api/image")
public class ImageController extends BaseController {

    @WebLog
    @PostMapping(value = "/pull")
    public void pullImage(@RequestBody Map<String, Object> params) {
        String registry  = Objects.toString(params.get("registry"), DockerConstant.IMAGE_DEFAULT_REGISTRY);
        String imageName = Objects.toString(params.get("imageName"), "");
        if (!imageName.contains(GlobalConstant.SPACEMARK_COLON)) {
            imageName = String.format("%s:%s", imageName, DockerConstant.IMAGE_DEFAULT_VERSION);
        }
        // TODO 对接Docker Registry
        final String imageFullName = imageName;
        ThreadPoolExecutor threadPool = ThreadPoolHelper.provideThreadPool(ThreadPoolHelper.ProvideModeEnum.SINGLE);
        threadPool.execute(() -> {
            try {
                getDockerClient().pull(imageFullName, message -> {
                    if (message.error() != null) {
                        if (Objects.requireNonNull(message.error()).contains("404") ||
                            Objects.requireNonNull(message.error()).contains("not found")) {
                            throw new ImageNotFoundException(imageFullName, message.toString());
                        } else {
                            LoggerHelper.fmtInfo(getClass(), "拉取镜像失败");
                            throw new ImagePullFailedException(imageFullName, message.toString());
                        }
                    } else {
                        LoggerHelper.fmtInfo(getClass(), "推送消息：%s", JSON.toJSONString(message));
                        SocketEventHandle.clientMap.forEach((token, client) -> client.sendEvent("pull", message));
                    }
                });
            } catch (Exception e) {
                LoggerHelper.fmtError(getClass(), e, "拉取镜像失败");
            }
        });
        threadPool.shutdown();
    }

    @WebLog
    @PostMapping(value = "/import")
    public Map<String, Object> importImage(MultipartHttpServletRequest request) {
        try {
            getDockerClient().load(null, message -> {
                message.error();
            });
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "导入镜像文件成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "导入镜像文件失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/export")
    public byte[] exportImage(@RequestBody Map<String, Object> params) {
        String[] imageIds = Objects.toString(params.get("imageIds"), "").split(",");
        String imageFile  = "";
        try {
            getDockerClient().save(imageIds);
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "导出镜像文件[%s]失败", imageFile);
        }
        return null;
    }

    @WebLog
    @PostMapping(value = "/tag")
    public Map<String, Object> tagImage(@RequestBody Map<String, Object> params) {
        String imageId       = Objects.toString(params.get("imageId"), "");
        String imageFullName = Objects.toString(params.get("imageFullName"), "");
        try {
            getDockerClient().tag(imageId, imageFullName);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "制作镜像标签成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "制作镜像新标签[%s]失败", imageFullName);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/remove")
    public Map<String, Object> removeImage(@RequestBody Map<String, Object> params) {
        String imageId  = Objects.toString(params.get("imageId"), "");
        boolean force   = Boolean.parseBoolean(Objects.toString(params.get("force"), "false"));
        boolean noPrune = Boolean.parseBoolean(Objects.toString(params.get("noPrune"), "false"));
        try {
            getDockerClient().removeImage(imageId, force, noPrune);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, "移除镜像成功");
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "删除镜像[%s]失败", imageId);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/search")
    public Map<String, Object> searchImages(@RequestBody Map<String, Object> params) {
        String imageName = Objects.toString(params.get("imageName"), "");
        try {
            List<ImageSearchResult> searchResults = getDockerClient().searchImages(imageName);
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, searchResults);
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "搜索镜像[%s]失败", imageName);
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/list")
    public Map<String, Object> getPageImages(@RequestBody Map<String, Object> params) {
        int pageNo   = Integer.parseInt(Objects.toString(params.get("pageNo"), "1"));
        int pageSize = Integer.parseInt(Objects.toString(params.get("pageSize"), "10"));
        List<DockerClient.ListImagesParam> filters = new ArrayList<>();
        if (params.get(DockerFilterHelper.FILTER_KEY) != null) {
            String filter = JSON.toJSONString(params.get(DockerFilterHelper.FILTER_KEY));
            filters = DockerFilterHelper.getImageFilter(filter);
        }
        try {
            List<Image> images = getDockerClient().listImages(DockerFilterHelper.toArray(filters,
                                                                                         DockerClient.ListImagesParam.class));
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               apiResponseDTO.tableResult(pageNo, pageSize, images));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取镜像列表失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/get")
    public Map<String, Object> getImage(@RequestBody Map<String, Object> params) {
        String imageId = Objects.toString(params.get("imageId"), "");
        try {
            List<Image> images = getDockerClient().listImages()
                                                  .stream()
                                                  .filter(item -> item.id().equals(imageId))
                                                  .collect(Collectors.toList());
            if (images.isEmpty()) {
                return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), "找不到该镜像");
            } else {
                return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE, images.iterator().next());
            }
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取镜像信息失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/inspect")
    public Map<String, Object> getImageInspect(@RequestBody Map<String, Object> params) {
        String imageId = Objects.toString(params.get("imageId"), "");
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               getDockerClient().inspectImage(imageId));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取镜像信息失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }

    @WebLog
    @PostMapping(value = "/history")
    public Map<String, Object> getImageHistory(@RequestBody Map<String, Object> params) {
        String imageId = Objects.toString(params.get("imageId"), "");
        try {
            return apiResponseDTO.returnResult(GlobalConstant.SUCCESS_CODE,
                                               getDockerClient().history(imageId));
        } catch (Exception e) {
            LoggerHelper.fmtError(getClass(), e, "获取镜像层信息失败");
            return apiResponseDTO.returnResult(ErrorCodeEnum.EXCEPTION.getCode(), e);
        }
    }
}
