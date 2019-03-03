package com.lzj.soufang.base;

/**
 * 房屋状态定义:房屋状态 0-未审核 1-审核通过 2-已出租 3-逻辑删除
 */
public enum HouseStatus {
    UN_AUDIT(0),
    PASSED(1),
    RETENDED(2),
    DELETED(3);

    private int code;

    HouseStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
