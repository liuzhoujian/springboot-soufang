package com.lzj.soufang.repository;

import com.lzj.soufang.entity.House;
import com.lzj.soufang.entity.HouseDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseDetailRepository extends JpaRepository<HouseDetail, Integer> {
    /**
     * 根据id查询房源详细信息
     * @param id
     * @return
     */
    HouseDetail findByHouseId(Integer id);

    /**
     * 批量查询房源详细信息
     * @param ids
     * @return
     */
    List<HouseDetail> findAllByHouseIdIn(List<Integer> ids);
}
