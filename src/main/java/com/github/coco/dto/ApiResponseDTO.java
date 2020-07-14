package com.github.coco.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Yan
 */
public class ApiResponseDTO {
    /**
     * 前端接口返回格式
     *
     * @param pageNo
     * @param pageSize
     * @param data
     * @return
     */
    public Map<String, Object> tableResult(int pageNo, int pageSize, List<?> data) {
        Map<String, Object> result = new HashMap<>(8);
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        result.put("totalCount", data.size());
        result.put("totalPage", data.size() / pageSize + 1);
        result.put("data", data.stream()
                               .skip((pageNo - 1) * pageSize)
                               .limit(pageSize)
                               .collect(Collectors.toList()));
        return result;
    }

    /**
     * 前端接口调用返回格式
     *
     * @param code
     * @param data
     * @return
     */
    public Map<String, Object> returnResult(int code, Object data) {
        Map<String, Object> result = new HashMap<>(2);
        result.put("code", code);
        result.put("data", data);
        return result;
    }

    @Override
    public String toString() {
        return "ApiResultDTO{}";
    }
}
