package com.wdjr.controller.house;

import com.wdjr.base.ApiResponse;
import com.wdjr.dto.SubwayDTO;
import com.wdjr.dto.SubwayStationDTO;
import com.wdjr.dto.SupportAddressDTO;
import com.wdjr.service.ServiceMultiResult;
import com.wdjr.service.house.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class HouseController {
    @Autowired
    private IAddressService addressService;

    /**
     * 获取支持城市列表
     * @return
     */
    @GetMapping("/address/support/cities")
    @ResponseBody
    public ApiResponse getSupportCities(){
        ServiceMultiResult<SupportAddressDTO> result = addressService.findAllCities();
        if(result.getResultSize()==0){
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }
    /**
     * 获取对应城市的支持区域列表
     */
    @GetMapping("/address/support/regions")
    @ResponseBody
    public ApiResponse getSupportRegions(@RequestParam(name = "city_name")String cityEnName){
        ServiceMultiResult<SupportAddressDTO> addressResult = addressService.findAllRegionsByCityName(cityEnName);
        if (addressResult.getResult() == null || addressResult.getTotal()<1){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(addressResult.getResult());
    }
    @GetMapping("/address/support/subway/line")
    @ResponseBody
    public ApiResponse getSubway(@RequestParam(name = "city_name")String cityEnName){
        List<SubwayDTO> subway = addressService.findAllSubwayByCityEnName(cityEnName);
        if (subway.isEmpty()){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(subway);
    }

    @GetMapping("/address/support/subway/station")
    @ResponseBody
    public ApiResponse getStation(@RequestParam(name = "subway_id")Long subwayId){
        List<SubwayStationDTO> subwayStations = addressService.findAllStationBySubway(subwayId);
        if(subwayStations.isEmpty()){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(subwayStations);

    }

}
