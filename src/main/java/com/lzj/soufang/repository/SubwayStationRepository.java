package com.lzj.soufang.repository;

import com.lzj.soufang.entity.SubwayStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubwayStationRepository extends JpaRepository<SubwayStation, Integer> {
    /**
     * 根据地铁线路编号，获取对应的所有站点
     * @param subwayId
     * @return
     */
    List<SubwayStation> findAllBySubwayId(Integer subwayId);

    /**
     * 根据站点编号获取站点信息
     * @param subwayStationId
     * @return
     */
    Optional<SubwayStation> findById(Integer subwayStationId);
}
