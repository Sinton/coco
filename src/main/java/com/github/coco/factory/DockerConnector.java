package com.github.coco.factory;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;

/**
 * @author Yan
 */
public class DockerConnector implements Connector {
    private static final String DEFAULT_HOST = "192.168.3.140";
    private static final String DEFAULT_PORT = "2375";
    private static final String DEFAULT_PROTOCOL = "unix://var/run/docker.sock";
    private volatile static DockerConnector dockerConnector;
    private static DockerClient dockerClient;

    private DockerConnector() {
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public String getPort() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getConfig() {
        return null;
    }

    public static synchronized DockerConnector getInstance() {
        if (dockerConnector == null) {
            synchronized (DockerConnector.class) {
                if (dockerConnector == null) {
                    dockerConnector = new DockerConnector();
                }
            }
        }
        return dockerConnector;
    }

    public DockerClient getDockerClient() {
        return getDockerClient(DEFAULT_HOST, DEFAULT_PORT);
    }

    public DockerClient getDockerClient(String host, String port) {
        if (dockerClient == null) {
            toggleConnection(host, port);
        }
        return dockerClient;
    }

    public void toggleConnection(String host, String port) {
        dockerClient = DefaultDockerClient.builder()
                .uri(String.format("http://%s:%s", host, port))
                .apiVersion("v1.30")
                .connectTimeoutMillis(50 * 1000)
                .readTimeoutMillis(50 * 1000)
                .build();
    }
}
