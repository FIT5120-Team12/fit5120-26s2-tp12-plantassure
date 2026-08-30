package com.plantky.common.exception;

import com.plantky.common.enums.ErrorCode;

/**
 * 根据 plantId 查询不到植物时抛出的业务异常。
 *
 * <p>该异常与“搜索无匹配结果”不同：</p>
 * <ul>
 *     <li>Search API 没有匹配结果是正常业务结果，返回 HTTP 200 + results: []；</li>
 *     <li>Assessment API 指定了不存在的 plantId，则资源不存在，返回 HTTP 404。</li>
 * </ul>
 */
public class PlantNotFoundException extends BusinessException {

    public PlantNotFoundException() {
        super(ErrorCode.PLANT_NOT_FOUND);
    }
}
