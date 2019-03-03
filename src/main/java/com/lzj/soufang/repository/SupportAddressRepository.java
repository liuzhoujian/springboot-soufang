package com.lzj.soufang.repository;

import com.lzj.soufang.entity.SupportAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportAddressRepository extends JpaRepository<SupportAddress, Integer> {
    /**
     * 获取所有对应行政级别的信息
     * @return
     */
    List<SupportAddress> findAllByLevel(String level);

    /**
     * 根据所选的城市获取其下的所有区域
     * @return
     */
    List<SupportAddress> findAllByLevelAndBelongTo(String level, String cityName);

    /**
     * 根据城市英文名获取城市信息
     * @param cityEnName
     * @param level
     * @return
     */
    SupportAddress findByEnNameAndLevel(String cityEnName, String level);

    /**
     * 根据选取的城市英文名和城区英文名获取城区信息
     * @param regionEnName
     * @param cityEnName
     * @return
     */
    SupportAddress findByEnNameAndBelongTo(String regionEnName, String cityEnName);

    /**
     * 根据城市英文名获取城市详细信息
     * @param cityEnName
     * @return
     */
    SupportAddress findByEnName(String cityEnName);
}
