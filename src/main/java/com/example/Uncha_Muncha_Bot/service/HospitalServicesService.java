package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.dto.HospitalServiceDTO;
import com.example.Uncha_Muncha_Bot.entity.HospitalServiceEntity;
import com.example.Uncha_Muncha_Bot.repository.HospitalServicesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HospitalServicesService {
    @Autowired
    private HospitalServicesRepository hospitalServicesRepository;

    public void checkService(String serviceName, Long hospitalId) {
        Optional<HospitalServiceEntity> optional=hospitalServicesRepository.findByServiceNameAndHospitalId(serviceName,hospitalId);
        if (optional.isPresent()) {
            deleteService(serviceName,hospitalId);
        } else {
            saveService(serviceName,hospitalId);
        }
    }

    public void deleteService(String serviceName, Long hospitalId) {
        hospitalServicesRepository.deleteByServiceAndHospitalId(serviceName,hospitalId);
    }
    public void saveService(String serviceName, Long hospitalId) {
        HospitalServiceEntity entity=new HospitalServiceEntity();
        entity.setServiceName(serviceName);
        entity.setHospitalId(hospitalId);
        hospitalServicesRepository.save(entity);
    }

    public Iterable<HospitalServiceEntity> getByHospitalId(Long hospitalId) {
        return hospitalServicesRepository.findByHospitalId(hospitalId);
    }
}
