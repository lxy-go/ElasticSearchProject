package com.wdjr.service.search;

/**
 * 检索接口
 */
public interface ISearchService {
    /**
     * 索引目标房源
     */
    boolean index(Long houseId);

    /**
     * 移除房源索引
     * @param houseId
     */
    void remove(Long houseId);
}
