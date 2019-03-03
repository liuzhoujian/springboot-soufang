package com.lzj.soufang.web.controller.admin;

import com.google.gson.Gson;
import com.lzj.soufang.base.ApiDataTableResponse;
import com.lzj.soufang.base.ApiResponse;
import com.lzj.soufang.base.HouseOperation;
import com.lzj.soufang.base.HouseStatus;
import com.lzj.soufang.entity.SupportAddress;
import com.lzj.soufang.service.ServiceMultiResult;
import com.lzj.soufang.service.ServiceResult;
import com.lzj.soufang.service.house.IAddressService;
import com.lzj.soufang.service.house.IHouseService;
import com.lzj.soufang.service.house.IQiNiuService;
import com.lzj.soufang.web.dto.*;
import com.lzj.soufang.web.form.DataTableSearch;
import com.lzj.soufang.web.form.HouseForm;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@Controller
public class AdminController {

    //注入七牛服务
    @Autowired
    private IQiNiuService qiNiuService;

    //注入地址服务
    @Autowired
    private IAddressService addressService;

    //注入房源服务
    @Autowired
    private IHouseService houseService;

    @Autowired
    private Gson gson;

    //来到管理员数据中心页面
    @GetMapping("/admin/center")
    public String toCenterPage() {
        return "admin/center";
    }

    //来到欢迎页
    @GetMapping("/admin/welcome")
    public String toWelcomePage() {
        return "admin/welcome";
    }

    //来到登录页面
    @GetMapping("/admin/login")
    public String toAdminLoginPage() {
        return "admin/login";
    }

    //来到新增房源页面
    @GetMapping("/admin/add/house")
    public String toHouseAddPage() {
        return "admin/house-add";
    }

    /**
     * 新增房源
     * @return
     */
    @PostMapping("/admin/add/house")
    @ResponseBody
    public ApiResponse addHouse(@Valid @ModelAttribute("form-house-add") HouseForm houseForm, BindingResult bindingResult) {//绑定表单并且校验
        //按照校验规则判断是否有错误
        if(bindingResult.hasErrors()) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), bindingResult.getAllErrors().get(0).getDefaultMessage(), null);
        }

        //判断是否上传照片和封面
        if(houseForm.getPhotos() == null || houseForm.getCover() == null){
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "必须上传图片");
        }

        //校验所选择的城市和区域是否对应
        Map<SupportAddress.Level, SupportAddressDTO> addressDTOMap = addressService.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());
        if(addressDTOMap.keySet().size() != 2) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        //保存房源
        ServiceResult<HouseDTO> result = houseService.save(houseForm);
        if(result.isSuccess()) {
            return ApiResponse.ofSuccess(result.getResult());
        }


        return ApiResponse.ofSuccess(ApiResponse.Status.NOT_VALID_PARAM);
    }

    //来到房源列表页面
    @GetMapping("/admin/house/list")
    public String toHouseList() {
        return "admin/house-list";
    }

    @PostMapping("admin/houses")
    @ResponseBody
    public ApiDataTableResponse houses(@ModelAttribute DataTableSearch searchBody) {
        ServiceMultiResult<HouseDTO> result = houseService.adminQuery(searchBody);

        //向前端返回结果
        ApiDataTableResponse dataTableResponse = new ApiDataTableResponse(ApiResponse.Status.SUCCESS);
        dataTableResponse.setData(result.getResult());
        dataTableResponse.setRecordsTotal(result.getTotal());
        dataTableResponse.setRecordsFiltered(result.getTotal());
        dataTableResponse.setDraw(searchBody.getDraw());
        return dataTableResponse;
    }

    //来到房源编辑页面
    @GetMapping("admin/house/edit")
    public String toHouseEditPage(@RequestParam("id") Integer id, Model model) {
        if(id == null || id < 1) {
            return "error/404";
        }

        ServiceResult<HouseDTO> result = houseService.findCompleteOne(id);
        if(!result.isSuccess()) {
            return "error/404";
        }

        //将结果放入model中
        HouseDTO houseDTO = result.getResult();
        model.addAttribute("house", houseDTO);

        //查询地址信息放入model中：city、region、subway、subwayStation
        Map<SupportAddress.Level, SupportAddressDTO> addressMap = addressService.findCityAndRegion(houseDTO.getCityEnName(), houseDTO.getRegionEnName());
        model.addAttribute("city", addressMap.get(SupportAddress.Level.CITY));
        model.addAttribute("region", addressMap.get(SupportAddress.Level.REGION));

        HouseDetailDTO houseDetail = houseDTO.getHouseDetail();
        ServiceResult<SubwayDTO> subwayResult = addressService.findSubway(houseDetail.getSubwayLineId());
        if(subwayResult.isSuccess()) {
            model.addAttribute("subway", subwayResult.getResult());
        }
        ServiceResult<SubwayStationDTO> stationResult = addressService.findSubwayStation(houseDetail.getSubwayStationId());
        if(stationResult.isSuccess()) {
            model.addAttribute("station", stationResult.getResult());
        }

        return "admin/house-edit";
    }

    //提交房源更新
    @PostMapping("admin/house/edit")
    @ResponseBody
    public ApiResponse update(@Valid @ModelAttribute("form-house-edit") HouseForm houseForm, BindingResult bindingResult) {
        //校验表单是否出错
        if(bindingResult.hasErrors()) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        //校验所选择的城市和区域是否对应
        Map<SupportAddress.Level, SupportAddressDTO> addressDTOMap = addressService.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());
        if(addressDTOMap.keySet().size() != 2) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        //执行更新操作
        ServiceResult<HouseDTO> result = houseService.update(houseForm);
        if(result.isSuccess()) {
            return ApiResponse.ofSuccess(null);
        }

        //返回错误信息
        ApiResponse response = ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        response.setMessage(response.getMessage());
        return response;
    }

    /**
     * 增加房源标签
     * @return
     */
    @PostMapping("admin/house/tag")
    @ResponseBody
    public ApiResponse addTag(@RequestParam("house_id") Integer houseId,
                               @RequestParam("tag") String tag) {
        if(houseId < 1 || StringUtils.isEmpty(tag)) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        ServiceResult result = houseService.addTag(houseId, tag);
        if(result.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        }

        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
    }

    /**
     * 移除房源标签
     * @param houseId
     * @param tag
     * @return
     */
    @DeleteMapping("admin/house/tag")
    @ResponseBody
    public ApiResponse removeTags(@RequestParam("house_id") Integer houseId,
                                  @RequestParam("tag") String tag) {
        if(houseId < 1 || StringUtils.isEmpty(tag)) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        ServiceResult result = houseService.removeTag(houseId, tag);
        if(result.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        }

        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
    }

    /**
     * 删除图片
     * @param photoId
     * @return
     */
    @DeleteMapping("admin/house/photo")
    @ResponseBody
    public ApiResponse removePhoto(@RequestParam("id") Integer photoId) {
        if(photoId == null) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        ServiceResult result = houseService.removePhoto(photoId);
        if(result.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        }

        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
    }

    /**
     * 设置新的封面
     * @param coverId
     * @param houdId
     * @return
     */
    @PostMapping("admin/house/cover")
    @ResponseBody
    public ApiResponse cover(@RequestParam("cover_id") Integer coverId,
                             @RequestParam("target_id") Integer houdId) {
        ServiceResult result = this.houseService.updateCover(coverId, houdId);

        if (result.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }
    }

    @GetMapping("admin/house/show")
    public String show(@RequestParam("id") Integer id) {
        //与update功能类似
        return "admin/house-show";
    }


    //审核接口
    @PutMapping("admin/house/operate/{houseId}/{operation}")
    @ResponseBody
    public ApiResponse operateHouse(@PathVariable("houseId") int houseId,
                                    @PathVariable("operation") int operation) {
        if(houseId <= 0) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        ServiceResult result;

        //根据前端的不同操作，传递不同的房屋状态码
        switch (operation) {
            case HouseOperation.PASS: //发布
                result = this.houseService.updateHouseStatus(houseId, HouseStatus.PASSED.getCode());
                break;
            case HouseOperation.PULL_OUT: //下架
                result = this.houseService.updateHouseStatus(houseId, HouseStatus.UN_AUDIT.getCode());
                break;
            case HouseOperation.DELETE: //删除
                result = this.houseService.updateHouseStatus(houseId, HouseStatus.DELETED.getCode());
                break;
            case HouseOperation.RENT: //出租
                result = this.houseService.updateHouseStatus(houseId, HouseStatus.RETENDED.getCode());
                break;
            default:
                return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }

        if(result.isSuccess()) {
            return ApiResponse.ofSuccess(null);
        }

        return ApiResponse.ofMessage(ApiResponse.Status.BAD_REQUEST.getCode(), result.getMessage());
    }



    //图片上传-七牛云
    @PostMapping(value = "/admin/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse uploadPhoto(@RequestParam("file") MultipartFile file) {
        if(file.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        //获取文件名
        String filename = file.getOriginalFilename();

        try {
            //上传文件
            Response response = qiNiuService.uploadFile(file.getInputStream());
            if(response.isOK()) {
                //上传成功，给前端返回信息
                QiNiuPutRet ret = gson.fromJson(response.bodyString(), QiNiuPutRet.class);
                return ApiResponse.ofSuccess(ret);
            } else {
                //上传失败,给前端返回信息
                return ApiResponse.ofMessage(response.statusCode, response.getInfo());
            }
        } catch (QiniuException e) {
            Response response = e.response;
            try {
                return ApiResponse.ofMessage(response.statusCode, response.bodyString());
            } catch (QiniuException e1) {
                e1.printStackTrace();
                return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
        }
    }

    //图片上传-本地
    /*@PostMapping(value = "/admin/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse uploadPhoto(@RequestParam("file") MultipartFile file) {
        //校验传输的文件
        if(file.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        //得到上传文件的名称
        String filename = file.getOriginalFilename();

        String tagPath = "C:\\个人文件\\ideaSpace\\springboot-soufang\\tmp\\";
        File saveFile = new File(tagPath + filename);
        try {
            //将文件保存在上面的指定位置
            file.transferTo(saveFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ApiResponse.ofSuccess(null);
    }*/
}
