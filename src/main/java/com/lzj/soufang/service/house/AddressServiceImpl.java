package com.lzj.soufang.service.house;

import com.lzj.soufang.entity.Subway;
import com.lzj.soufang.entity.SubwayStation;
import com.lzj.soufang.entity.SupportAddress;
import com.lzj.soufang.repository.SubwayRepository;
import com.lzj.soufang.repository.SubwayStationRepository;
import com.lzj.soufang.repository.SupportAddressRepository;
import com.lzj.soufang.service.ServiceMultiResult;
import com.lzj.soufang.service.ServiceResult;
import com.lzj.soufang.web.dto.SubwayDTO;
import com.lzj.soufang.web.dto.SubwayStationDTO;
import com.lzj.soufang.web.dto.SupportAddressDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AddressServiceImpl implements IAddressService {

    @Autowired
    private SupportAddressRepository supportAddressRepository;

    @Autowired
    private SubwayRepository subwayRepository;

    @Autowired
    private SubwayStationRepository subwayStationRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllCities() {
        //将level为city的城市查出来
        List<SupportAddress> citys = supportAddressRepository.findAllByLevel(SupportAddress.Level.CITY.getValue());

        //将从数据库查询的数据放入DTO对象中
        List<SupportAddressDTO> cityDTOS = new ArrayList<>();
        for(SupportAddress city : citys) {
            SupportAddressDTO addressDTO = modelMapper.map(city, SupportAddressDTO.class);
            cityDTOS.add(addressDTO);
        }

        return new ServiceMultiResult<>(cityDTOS.size(), cityDTOS);
    }

    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String cityName) {
        if(cityName == null) {
            return new ServiceMultiResult<>(0, null);
        }

        List<SupportAddress> regions = supportAddressRepository.findAllByLevelAndBelongTo(SupportAddress.Level.REGION.getValue(), cityName);
        List<SupportAddressDTO> regionDTOs = new ArrayList<>();
        for(SupportAddress region : regions) {
            SupportAddressDTO regionDTO = modelMapper.map(region, SupportAddressDTO.class);
            regionDTOs.add(regionDTO);
        }

        return new ServiceMultiResult<>(regionDTOs.size(), regionDTOs);
    }

    @Override
    public List<SubwayDTO> findAllSubwayByCity(String cityEnName) {
        if(cityEnName == null) {
            return null;
        }
        List<Subway> subways = subwayRepository.findAllByCityEnName(cityEnName);
        List<SubwayDTO> subwayDTOS = new ArrayList<>();
        subways.forEach(subway -> subwayDTOS.add(modelMapper.map(subway, SubwayDTO.class)));
        return subwayDTOS;
    }

    @Override
    public List<SubwayStationDTO> findAllStationBySubway(Integer subwayId) {
        List<SubwayStation> subwayStations = subwayStationRepository.findAllBySubwayId(subwayId);
        //将entry对象转为DTOS对象
        List<SubwayStationDTO> subwayStationDTOS = new ArrayList<>();
        subwayStations.forEach(subwayStation ->
                subwayStationDTOS.add(modelMapper.map(subwayStation, SubwayStationDTO.class)));
        return subwayStationDTOS;
    }

    @Override
    public ServiceResult<SubwayDTO> findSubway(Integer subwayId) {
        Subway subway = subwayRepository.findById(subwayId).get();
        SubwayDTO subwayDTO = modelMapper.map(subway, SubwayDTO.class);
        return ServiceResult.of(subwayDTO);
    }

    @Override
    public ServiceResult<SubwayStationDTO> findSubwayStation(Integer stationId) {
        SubwayStation subwayStation = subwayStationRepository.findById(stationId).get();
        SubwayStationDTO stationDTO = modelMapper.map(subwayStation, SubwayStationDTO.class);
        return ServiceResult.of(stationDTO);
    }

    @Override
    public Map<SupportAddress.Level, SupportAddressDTO> findCityAndRegion(String cityEnName, String regionEnName) {
        Map<SupportAddress.Level, SupportAddressDTO> addressDTOMap = new HashMap<>();

        //获取所选城市的详细信息
        SupportAddress city = supportAddressRepository.findByEnNameAndLevel(cityEnName, SupportAddress.Level.CITY.getValue());
        //获取城区详细信息
        SupportAddress region = supportAddressRepository.findByEnNameAndBelongTo(regionEnName, city.getEnName());

        addressDTOMap.put(SupportAddress.Level.CITY, modelMapper.map(city, SupportAddressDTO.class));
        addressDTOMap.put(SupportAddress.Level.REGION, modelMapper.map(region, SupportAddressDTO.class));

        return addressDTOMap;
    }

    @Override
    public ServiceResult<SupportAddressDTO> findCity(String cityEnName) {
        SupportAddress supportAddress = supportAddressRepository.findByEnName(cityEnName);
        if(supportAddress == null) {
            return ServiceResult.notFound();
        }

        SupportAddressDTO supportAddressDTO = modelMapper.map(supportAddress, SupportAddressDTO.class);

        return ServiceResult.of(supportAddressDTO);
    }
}
