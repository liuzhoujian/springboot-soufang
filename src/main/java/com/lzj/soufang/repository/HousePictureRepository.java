package com.lzj.soufang.repository;

import com.lzj.soufang.entity.HousePicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HousePictureRepository extends JpaRepository<HousePicture, Integer> {

    /**
     * 根据房源id查房源的照片信息
     * @param id
     * @return
     */
    List<HousePicture> findByHouseId(Integer id);

    /**
     * 根据照片id查找照片信息
     * @param photoId
     * @return
     */
    Optional<HousePicture> findById(Integer photoId);

    /**
     * 根据id删除图片
     * @param photoId
     */
    void deleteById(Integer photoId);
}
