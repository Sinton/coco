package com.github.coco.controller;

import com.github.coco.dto.ApiResponseDTO;
import com.github.coco.utils.DockerConnectorHelper;
import com.spotify.docker.client.DockerClient;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yan
 */
@RestController
public class BaseController {
    protected ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
    protected DockerClient dockerClient = DockerConnectorHelper.getDockerClient("192.168.3.140", 2375);
}
