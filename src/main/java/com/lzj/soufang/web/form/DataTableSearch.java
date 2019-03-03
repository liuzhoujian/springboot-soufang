package com.lzj.soufang.web.form;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 封装Datatable前端表单条件查询和分页
 */
@Data
public class DataTableSearch {
    /*datatable要求返回的字段*/
    private int draw;

    /*datatable的分页字段*/
    private int start;
    private int length;

    /*自定义的条件查询字段*/

    /*按照不同的城市查询*/
    private String city;

    /*按照不同的状态查询：房屋状态 0-未审核 1-审核通过 2-已出租 3-逻辑删除*/
    private Integer status;

    /*按照创建时间段查询*/
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createTimeMin;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date getCreateTimeMax;

    /*按照标题查询*/
    private String title;

    private String direction;
    private String orderBy;
}
