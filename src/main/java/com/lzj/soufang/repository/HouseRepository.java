package com.lzj.soufang.repository;

import com.lzj.soufang.entity.House;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HouseRepository extends JpaRepository<House, Integer>, JpaSpecificationExecutor<House> {
    //更新封面
    @Modifying
    @Query("UPDATE House as house set house.cover = :cover WHERE house.id = :houseId")
    void updateCover(@Param("houseId") Integer houseId, @Param("cover") String cover);

    //更新状态
    @Modifying
    @Query("UPDATE House as house set house.status = :status WHERE house.id = :houseId")
    void updateStatus(@Param("houseId") Integer houseId, @Param("status") Integer status);
}
