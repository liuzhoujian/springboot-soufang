package com.lzj.soufang.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "subway_station")
public class SubwayStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*所属地铁线id*/
    @Column(name = "subway_id")
    private Integer subwayId;

    /*站点名称*/
    private String name;
}
