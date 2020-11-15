package com.github.coco.controller;

import com.github.coco.dto.ApiResponseDTO;
import com.spotify.docker.client.DockerClient;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yan
 */
@RestController
public class BaseController {
    protected ApiResponseDTO apiResponseDTO = new ApiResponseDTO();
    protected static DockerClient dockerClient;
    protected static Map<String, DockerClient> dockerClients = new ConcurrentHashMap<>(16);
}
