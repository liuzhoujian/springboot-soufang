package com.lzj.soufang.repository;

import com.lzj.soufang.entity.Subway;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubwayRepository extends JpaRepository<Subway, Integer> {
    /**
     * 根据城市获取对应的地铁线路
     * @param cityName
     * @return
     */
    List<Subway> findAllByCityEnName(String cityName);

    /**
     * 根据地铁id获取地铁信息
     * @param subwayId
     * @return
     */
    Optional<Subway> findById(Integer subwayId);
}
