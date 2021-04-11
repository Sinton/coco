package com.github.coco.utils;

import com.github.coco.constant.dict.ContainerStatusEnum;
import com.spotify.docker.client.DockerClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yan
 */
public class DockerFilterHelper {
    public static final String FILTER_KEY = "filter";

    /**
     * 构建镜像过滤参数
     *
     * @param filter
     * @return
     */
    public static List<DockerClient.ListImagesParam> getImageFilter(String filter) {
        return getImageFilter(StringHelper.stringConvertMap(filter));
    }

    public static List<DockerClient.ListImagesParam> getImageFilter(Map<String, Object> filter) {
        List<DockerClient.ListImagesParam> filters = new ArrayList<>();
        filter.forEach((key, value) -> {
            switch (key) {
                case "all":
                    filters.add(DockerClient.ListImagesParam.allImages(Boolean.parseBoolean(value.toString())));
                    break;
                case "dangling":
                    filters.add(DockerClient.ListImagesParam.danglingImages(Boolean.parseBoolean(value.toString())));
                    break;
                case "name":
                    filters.add(DockerClient.ListImagesParam.byName(value.toString()));
                    break;
                case "digests":
                    filters.add(DockerClient.ListImagesParam.digests());
                    break;
                case "label":
                    if (value instanceof Map) {
                        filters.add(DockerClient.ListImagesParam.withLabel("", ""));
                    } else {
                        filters.add(DockerClient.ListImagesParam.withLabel(value.toString()));
                    }
                    break;
                default:
                    filters.add(DockerClient.ListImagesParam.filter(key, value.toString()));
                    break;
            }
        });
        return filters;
    }

    /**
     * 构建容器过滤参数
     *
     * @param filter
     * @return
     */
    public static List<DockerClient.ListContainersParam> getContainerFilter(String filter) {
        return getContainerFilter(StringHelper.stringConvertMap(filter));
    }

    public static List<DockerClient.ListContainersParam> getContainerFilter(Map<String, Object> filter) {
        List<DockerClient.ListContainersParam> filters = new ArrayList<>();
        filter.forEach((key, value) -> {
            switch (key) {
                case "all":
                    filters.add(DockerClient.ListContainersParam.allContainers(Boolean.parseBoolean(value.toString())));
                    break;
                case "limit":
                    filters.add(DockerClient.ListContainersParam.limitContainers(Integer.parseInt(value.toString())));
                    break;
                case "status":
                    switch (ContainerStatusEnum.valueOf(value.toString())) {
                        case CREATED:
                            filters.add(DockerClient.ListContainersParam.withStatusCreated());
                            break;
                        case RUNNING:
                            filters.add(DockerClient.ListContainersParam.withStatusRunning());
                            break;
                        case PAUSED:
                            filters.add(DockerClient.ListContainersParam.withStatusPaused());
                            break;
                        case RESTARTING:
                            filters.add(DockerClient.ListContainersParam.withStatusRestarting());
                            break;
                        case EXITED:
                            filters.add(DockerClient.ListContainersParam.withStatusExited());
                            break;
                        default:
                            break;
                    }
                    break;
                case "label":
                    if (value instanceof Map) {
                        ((Map<?, ?>) value).forEach((labelKey, labelValue) -> filters.add(DockerClient.ListContainersParam.withLabel(String.valueOf(labelKey), String.valueOf(labelValue))));
                    } else {
                        filters.add(DockerClient.ListContainersParam.withLabel(value.toString()));
                    }
                    break;
                case "size":
                    filters.add(DockerClient.ListContainersParam.withContainerSizes(Boolean.parseBoolean(value.toString())));
                    break;
                default:
                    filters.add(DockerClient.ListContainersParam.filter(key, value.toString()));
                    break;
            }
        });
        return filters;
    }

    /**
     * 构建挂载卷过滤参数
     *
     * @param filter
     * @return
     */
    public static List<DockerClient.ListVolumesParam> getVolumesFilter(String filter) {
        return getVolumesFilter(StringHelper.stringConvertMap(filter));
    }

    public static List<DockerClient.ListVolumesParam> getVolumesFilter(Map<String, Object> filter) {
        List<DockerClient.ListVolumesParam> filters = new ArrayList<>();
        filter.forEach((key, value) -> {
            switch (key) {
                case "name":
                    filters.add(DockerClient.ListVolumesParam.name(value.toString()));
                    break;
                case "dangling":
                    filters.add(DockerClient.ListVolumesParam.dangling());
                    break;
                case "driver":
                    filters.add(DockerClient.ListVolumesParam.driver(value.toString()));
                    break;
                default:
                    filters.add(DockerClient.ListVolumesParam.filter(key, value.toString()));
                    break;
            }
        });
        return filters;
    }

    /**
     * 构建网络过滤参数
     *
     * @param filter
     * @return
     */
    public static List<DockerClient.ListNetworksParam> getNetworkFilter(String filter) {
        return getNetworkFilter(StringHelper.stringConvertMap(filter));
    }

    public static List<DockerClient.ListNetworksParam> getNetworkFilter(Map<String, Object> filter) {
        List<DockerClient.ListNetworksParam> filters = new ArrayList<>();
        filter.forEach((key, value) -> {
            switch (key) {
                case "networkName":
                    filters.add(DockerClient.ListNetworksParam.byNetworkName(value.toString()));
                    break;
                case "networkId":
                    filters.add(DockerClient.ListNetworksParam.byNetworkId(value.toString()));
                    break;
                case "driver":
                    filters.add(DockerClient.ListNetworksParam.withDriver(value.toString()));
                    break;
                case "builtIn":
                    filters.add(DockerClient.ListNetworksParam.builtInNetworks());
                    break;
                case "custom":
                    filters.add(DockerClient.ListNetworksParam.customNetworks());
                    break;
                case "laebl":
                    if (value instanceof Map) {
                        filters.add(DockerClient.ListNetworksParam.withLabel("", ""));
                    } else {
                        filters.add(DockerClient.ListNetworksParam.withLabel(value.toString()));
                    }
                    break;
                default:
                    filters.add(DockerClient.ListNetworksParam.filter(key, value.toString()));
                    break;
            }
        });
        return filters;
    }

    /**
     * 过滤参数集合数组化
     *
     * @param filters
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T[] toArray(List<T> filters, Class<T> clazz) {
        if (clazz == DockerClient.ListImagesParam.class) {
            return (T[]) filters.toArray(new DockerClient.ListImagesParam[filters.size()]);
        }
        if (clazz == DockerClient.ListContainersParam.class) {
            return (T[]) filters.toArray(new DockerClient.ListContainersParam[filters.size()]);
        }
        if (clazz == DockerClient.ListNetworksParam.class) {
            return (T[]) filters.toArray(new DockerClient.ListNetworksParam[filters.size()]);
        }
        if (clazz == DockerClient.ListVolumesParam.class) {
            return (T[]) filters.toArray(new DockerClient.ListVolumesParam[filters.size()]);
        }
        return null;
    }
}
