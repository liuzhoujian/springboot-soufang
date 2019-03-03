package com.lzj.soufang.service.search;

public interface ISearchService {
    /**
     * 索引目标房源
     * @param houseId
     */
    boolean index(Integer houseId);

    /**
     * 移除房源索引
     * @param houseId
     */
    void remove(Integer houseId);
}
