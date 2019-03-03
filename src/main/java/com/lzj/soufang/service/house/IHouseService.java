package com.lzj.soufang.service.house;

import com.lzj.soufang.service.ServiceMultiResult;
import com.lzj.soufang.service.ServiceResult;
import com.lzj.soufang.web.dto.HouseDTO;
import com.lzj.soufang.web.form.DataTableSearch;
import com.lzj.soufang.web.form.HouseForm;
import com.lzj.soufang.web.form.RentSearch;

/**
 *  房屋管理服务接口
 */
public interface IHouseService {
    /**
     * 新增房源接口
     * @param houseForm
     * @return
     */
    ServiceResult<HouseDTO> save(HouseForm houseForm);

    /**
     * 管理员浏览房源
     * @param searchBody
     * @return
     */
    ServiceMultiResult<HouseDTO> adminQuery(DataTableSearch searchBody);

    /**
     * 根据房源id，查找完整的房源信息：house、houseDetail、housePicture、houseTag
     * @param id
     * @return
     */
    ServiceResult<HouseDTO> findCompleteOne(Integer id);

    /**
     * 更新房源信息
     * @param houseForm
     * @return
     */
    ServiceResult<HouseDTO> update(HouseForm houseForm);

    /**
     * 移除图片
     * @param id
     * @return
     */
    ServiceResult removePhoto(Integer id);

    /**
     * 更新封面
     * @param coverId
     * @param targetId
     * @return
     */
    ServiceResult updateCover(Integer coverId, Integer targetId);

    /**
     * 新增标签
     * @param houseId
     * @param tag
     * @return
     */
    ServiceResult addTag(Integer houseId, String tag);

    /**
     * 移除标签
     * @param houseId
     * @param tag
     * @return
     */
    ServiceResult removeTag(Integer houseId, String tag);

    /**
     * 更新房源状态
     * @param houseId
     * @param status
     * @return
     */
    ServiceResult updateHouseStatus(int houseId, int status);


    /**
     * 查询房源信息集
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<HouseDTO> query(RentSearch rentSearch);
}
