package com.lzj.soufang.base;

import lombok.Data;

/**
 * datatable插件要求返回的字段
 * https://datatables.net/manual/server-side#Returned-data
 */
@Data
public class ApiDataTableResponse extends ApiResponse {
    private int draw;
    private long recordsTotal;
    private long recordsFiltered;

    public ApiDataTableResponse(ApiResponse.Status status) {
        this(status.getCode(), status.getStandardMessage(), null);
    }

    public ApiDataTableResponse(Integer code, String message, Object data) {
        super(code, message, data);
    }
}
