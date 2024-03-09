package com.example.Uncha_Muncha_Bot.repository;

import com.example.Uncha_Muncha_Bot.entity.HospitalServiceEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface HospitalServicesRepository extends CrudRepository<HospitalServiceEntity,Long> {
    @Query("from HospitalServiceEntity where serviceName=?1 and hospitalId=?2")
    Optional<HospitalServiceEntity> findByServiceNameAndHospitalId(String serviceName, Long hospitalId);

    @Query("from HospitalServiceEntity where hospitalId=?1")
    Iterable<HospitalServiceEntity> findByHospitalId(Long hospitalId);

    @Transactional
    @Modifying
    @Query("delete from HospitalServiceEntity where serviceName=?1 and hospitalId=?2")
    void deleteByServiceAndHospitalId(String serviceName, Long hospitalId);
}
