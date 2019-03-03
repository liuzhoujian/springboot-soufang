package com.lzj.soufang.repository;

import com.lzj.soufang.entity.HouseTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseTagRepository extends JpaRepository<HouseTag, Integer> {

    /**
     * 根据id查找房源标签
     * @param id
     * @return
     */
    List<HouseTag> findByHouseId(Integer id);

    /**
     * 获取标签信息
     * @param houseId
     * @param tagName
     * @return
     */
    HouseTag findByHouseIdAndAndName(Integer houseId, String tagName);

    /**
     * 根据tag编号删除tag
     * @param tagId
     */
    void deleteById(Integer tagId);

    /**
     * 批量查询标签
     * @param ids
     * @return
     */
    List<HouseTag> findAllByHouseIdIn(List<Integer> ids);
}
