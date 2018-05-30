package com.wdjr.service.house;

import com.wdjr.dto.SubwayDTO;
import com.wdjr.dto.SubwayStationDTO;
import com.wdjr.dto.SupportAddressDTO;
import com.wdjr.entity.Subway;
import com.wdjr.entity.SubwayStation;
import com.wdjr.entity.SupportAddress;
import com.wdjr.repository.SubwayRepository;
import com.wdjr.repository.SubwayStationRepository;
import com.wdjr.repository.SupportAddressRepository;
import com.wdjr.service.ServiceMultiResult;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        //根据level获取所有地址
        List<SupportAddress> addresses = supportAddressRepository.findAllByLevel(SupportAddress.Level.CITY.getValue());
        //转换成DTO格式
        List<SupportAddressDTO> addressDTOS = new ArrayList<>();

        for (SupportAddress supportAddress : addresses) {
            SupportAddressDTO target = modelMapper.map(supportAddress, SupportAddressDTO.class);
            addressDTOS.add(target);
        }

        return new ServiceMultiResult<>(addressDTOS.size(), addressDTOS);
    }

    @Override
    public Map<SupportAddress.Level, SupportAddressDTO> findCityAndRegion(String cityEnName, String regionEnName) {
        return null;
    }

    @Override
    public ServiceMultiResult<SupportAddressDTO> findAllRegionsByCityName(String cityName) {
        if (cityName==null){
            return new ServiceMultiResult<>(0, null);
        }
        List<SupportAddressDTO> result = new ArrayList<>();
        List<SupportAddress> regions = supportAddressRepository.findAllByLevelAndBelongTo(SupportAddress.Level.REGION.getValue(), cityName);
        for (SupportAddress region : regions) {
            result.add(modelMapper.map(region, SupportAddressDTO.class) );
        }
        return new ServiceMultiResult<>(regions.size(),result );
    }

    @Override
    public List<SubwayDTO> findAllSubwayByCityEnName(String cityEnName) {
        List<SubwayDTO> result = new ArrayList<>();
        List<Subway> subways = subwayRepository.findAllByCityEnName(cityEnName);
        if(subways.isEmpty()){
            return result;
        }
        subways.forEach(subway -> result.add(modelMapper.map(subway, SubwayDTO.class)));
        return result;
    }

    @Override
    public List<SubwayStationDTO> findAllStationBySubway(Long subwayId) {
        List<SubwayStationDTO> result = new ArrayList<>();
        List<SubwayStation> subwayStations = subwayStationRepository.findAllBySubwayId(subwayId);
        if(subwayStations.isEmpty()){
            return result;
        }
        subwayStations.forEach(subwayStation -> result.add(modelMapper.map(subwayStation, SubwayStationDTO.class) ));
        return result;
    }
}
