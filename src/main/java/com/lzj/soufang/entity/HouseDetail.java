package com.lzj.soufang.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "house_detail")
public class HouseDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "house_id")
    private Integer houseId;

    private String description;

    @Column(name = "layout_desc")
    private String layoutDesc;

    private String traffic;

    @Column(name = "round_service")
    private String roundService;

    @Column(name = "rent_way")
    private int rentWay;

    @Column(name = "address")
    private String detailAddress;

    @Column(name = "subway_line_id")
    private Integer subwayLineId;

    @Column(name = "subway_station_id")
    private Integer subwayStationId;

    @Column(name = "subway_line_name")
    private String subwayLineName;

    @Column(name = "subway_station_name")
    private String subwayStationName;
}
