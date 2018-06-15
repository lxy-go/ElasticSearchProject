package com.wdjr.controller.house;

import com.wdjr.base.ApiResponse;
import com.wdjr.base.RentValueBlock;
import com.wdjr.dto.HouseDTO;
import com.wdjr.dto.SubwayDTO;
import com.wdjr.dto.SubwayStationDTO;
import com.wdjr.dto.SupportAddressDTO;
import com.wdjr.form.RentSearch;
import com.wdjr.service.ServiceMultiResult;
import com.wdjr.service.ServiceResult;
import com.wdjr.service.house.IAddressService;
import com.wdjr.service.house.IHouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HouseController {
    @Autowired
    private IHouseService houseService;

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

    @GetMapping("rent/house")
    public String rentHousePage(@ModelAttribute RentSearch rentSearch,
                                Model model, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        //是否点击了城市信息
        if (rentSearch.getCityEnName() == null) {
            String cityEnNameInSession = (String) session.getAttribute("cityEnName");
            //没有就去Session中看有没有
            if (cityEnNameInSession == null) {
                redirectAttributes.addAttribute("msg", "must_chose_city");
                return "redirect:/index";
            } else {
                //如果存在session信息，则将session得到城市信息存储到表单中
                rentSearch.setCityEnName(cityEnNameInSession);
            }
        } else {
            //如果有点击了城市信息，信息将会保存在session中
            session.setAttribute("cityEnName", rentSearch.getCityEnName());
        }
        //查询城市信息
        ServiceResult<SupportAddressDTO> city = addressService.findCity(rentSearch.getCityEnName());
        //是否存在城市
        if (!city.isSuccess()) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }
        model.addAttribute("currentCity", city.getResult());
        //城市区域信息获取
        ServiceMultiResult<SupportAddressDTO> addressResult = addressService.findAllRegionsByCityName(rentSearch.getCityEnName());
        if (addressResult.getResult() == null || addressResult.getTotal() < 1) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }
        //房屋信息
        ServiceMultiResult<HouseDTO> serviceMultiResult = houseService.query(rentSearch);

        model.addAttribute("total", serviceMultiResult.getTotal());
        model.addAttribute("houses", serviceMultiResult.getResult());

        if (rentSearch.getRegionEnName() == null) {
            rentSearch.setRegionEnName("*");
        }

        model.addAttribute("searchBody", rentSearch);
        model.addAttribute("regions", addressResult.getResult());

        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        model.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);

        model.addAttribute("currentPriceBlock", RentValueBlock.matchPrice(rentSearch.getPriceBlock()));
        model.addAttribute("currentAreaBlock", RentValueBlock.matchArea(rentSearch.getAreaBlock()));

        return "rent-list";
    }
}
