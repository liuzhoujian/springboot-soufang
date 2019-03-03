package com.lzj.soufang.base;

import com.google.common.collect.Sets;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * 排序生成器
 */
public class HouseSort {
    public static final String DEFAULT_SORT_KEY = "lastUpdateTime";
    public static final String DISTANCE_TO_SUBWAY_KEY = "distanceToSubway";
    public static final String PRICE = "price";
    public static final String CRETE_TIME = "createTime";
    public static final String AREA = "area";

    private static final Set<String> SORT_KEYS = Sets.newHashSet(
            DEFAULT_SORT_KEY,
            CRETE_TIME,
            PRICE,
            AREA,
            DISTANCE_TO_SUBWAY_KEY
    );

    /**
     * 获取排序规则
     * @param key 按照哪个字段排序
     * @param directionKey 值有DESC\ASC
     * @return
     */
    public static Sort generateSort(String key, String directionKey) {
        key = getSortKeys(key);

        Sort.Direction direction = Sort.Direction.fromString(directionKey);
        if(direction == null) {
            direction = Sort.Direction.DESC;
        }

        return new Sort(direction, key);
    }

    public static String getSortKeys(String key) {
        if(!SORT_KEYS.contains(key)) {
            key = DEFAULT_SORT_KEY;
        }

        return key;
    }
}
