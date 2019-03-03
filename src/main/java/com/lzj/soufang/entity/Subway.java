package com.lzj.soufang.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "subway")
public class Subway {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*线路名*/
    private String name;

    /*所属城市英文名缩写*/
    @Column(name = "city_en_name")
    private String cityEnName;
}
