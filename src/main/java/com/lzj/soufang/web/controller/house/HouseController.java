package com.lzj.soufang.web.controller.house;

import com.lzj.soufang.base.ApiResponse;
import com.lzj.soufang.base.RentValueBlock;
import com.lzj.soufang.entity.SupportAddress;
import com.lzj.soufang.service.ServiceMultiResult;
import com.lzj.soufang.service.ServiceResult;
import com.lzj.soufang.service.house.IAddressService;
import com.lzj.soufang.service.house.IHouseService;
import com.lzj.soufang.service.user.IUserService;
import com.lzj.soufang.web.dto.*;
import com.lzj.soufang.web.form.RentSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class HouseController {

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IHouseService houseService;

    @Autowired
    private IUserService userService;

    /**
     * 获取支持的城市列表
     * @return
     */
    @GetMapping("address/support/cities")
    @ResponseBody
    public ApiResponse getSupportCities() {
        ServiceMultiResult<SupportAddressDTO> result = addressService.findAllCities();

        if(result.getResultSize() == 0) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }

        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取对应城市支持区域列表
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/regions")
    @ResponseBody
    public ApiResponse getSupportRegions(@RequestParam("city_name") String cityEnName) {
        ServiceMultiResult<SupportAddressDTO> result = addressService.findAllRegionsByCityName(cityEnName);
        if(result.getResult() == null || result.getResult().size() < 1) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }

        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取对应城市支持的地铁线路
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/subway/line")
    @ResponseBody
    public ApiResponse getSupportLines(@RequestParam("city_name") String cityEnName) {
        List<SubwayDTO> subways = addressService.findAllSubwayByCity(cityEnName);
        if(subways == null || subways.size() < 1) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }

        return ApiResponse.ofSuccess(subways);
    }

    /**
     * 获取所选地铁的所有站点
     */
    @GetMapping("address/support/subway/station")
    @ResponseBody
    public ApiResponse getAllSubwayStationBySubwayId(@RequestParam("subway_id") Integer subwayId) {
        List<SubwayStationDTO> subwayStations = addressService.findAllStationBySubway(subwayId);

        if(subwayStations == null || subwayStations.size() < 1) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(subwayStations);
    }

    /**
     * 根据城市查找租房信息
     * @return
     */
    @GetMapping("rent/house")
    public String rentHouse(@ModelAttribute RentSearch rentSearch,
                            Model model, HttpSession session,
                            RedirectAttributes redirectAttributes) {
        //首先从封装的请求体里面看有没有cityEnName，没有的话从session中取，session中要是没有，就报错
        String cityEnName = rentSearch.getCityEnName();
        if(cityEnName == null) {
            cityEnName = (String) session.getAttribute("cityEnName");
            if(cityEnName == null) {
                redirectAttributes.addAttribute("msg", "must_chose_a_city");
                return "redirect:/index";
            } else {
                //从session中取出设置进rentSearch请求体中
                rentSearch.setCityEnName(cityEnName);
            }
        } else {
            //设置进session中
            session.setAttribute("cityEnName", rentSearch.getCityEnName());
        }


        //1、根据城市英文名获取城市详细信息
        ServiceResult<SupportAddressDTO> result = addressService.findCity(rentSearch.getCityEnName());
        if(!result.isSuccess()) {
            redirectAttributes.addAttribute("msg", "must_chose_a_city");
            return "redirect:/index";
        }

        //返回当前城市
        model.addAttribute("currentCity", result.getResult());

        //2、获取选定城市下的所有区域信息
        ServiceMultiResult<SupportAddressDTO> addressResult = addressService.findAllRegionsByCityName(rentSearch.getCityEnName());
        if(addressResult == null || addressResult.getResultSize() < 1) {
            redirectAttributes.addAttribute("msg", "must_chose_a_city");
            return "redirect:/index";
        }
        //返回当前城市下的所有小区
        model.addAttribute("regions", addressResult.getResult());

        //3、根据rentSearch请求体执行查询
        ServiceMultiResult<HouseDTO> houseResult = houseService.query(rentSearch);

        //返回查询结果总数
        model.addAttribute("total", houseResult.getTotal());
        //返回查询结果
        model.addAttribute("houses", houseResult.getResult());

        //回显查询条件
        model.addAttribute("searchBody", rentSearch);

        //不限区域
        if (rentSearch.getRegionEnName() == null) {
            rentSearch.setRegionEnName("*");
        }

        //页面显示价格区间
        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        //页面显示面积区间
        model.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);

        //页面显示当前的价格区间和面积区间
        model.addAttribute("currentPriceBlock", RentValueBlock.matchPrice(rentSearch.getPriceBlock()));
        model.addAttribute("currentAreaBlock", RentValueBlock.matchArea(rentSearch.getAreaBlock()));

        return "rent-list";
    }

    @GetMapping("/rent/house/show/{id}")
    public String show(@PathVariable("id") Integer id, Model model) {
        if(id <= 0) {
            return "error/404";
        }

        //根据id查询房源整体信息
        ServiceResult<HouseDTO> result = houseService.findCompleteOne(id);
        if(!result.isSuccess()) {
            return "error/404";
        }

        HouseDTO houseDTO = result.getResult();
        Map<SupportAddress.Level, SupportAddressDTO> cityAndRegionMap =
                addressService.findCityAndRegion(houseDTO.getCityEnName(), houseDTO.getRegionEnName());
        SupportAddressDTO city = cityAndRegionMap.get(SupportAddress.Level.CITY);
        SupportAddressDTO region = cityAndRegionMap.get(SupportAddress.Level.REGION);
        model.addAttribute("house", houseDTO);
        model.addAttribute("city", city);
        model.addAttribute("region", region);

        ServiceResult<UserDTO> userDTOServiceResult = userService.findById(houseDTO.getAdminId());
        model.addAttribute("agent", userDTOServiceResult.getResult());

        model.addAttribute("houseCountInDistrict", 0);

        return "house-detail";
    }

}
