package com.example.Uncha_Muncha_Bot.repository;

import com.example.Uncha_Muncha_Bot.entity.PharmacyEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.PharmacyType;
import com.example.Uncha_Muncha_Bot.mapper.PharmacyMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalTime;
import java.util.List;

public interface PharmacyRepository extends CrudRepository<PharmacyEntity,Long> {
    @Transactional
    @Modifying
    @Query("update PharmacyEntity set startTime=?1 where id=?2")
    void setStartTime(LocalTime startTime, Long pharmacyId);
    @Transactional
    @Modifying
    @Query("update PharmacyEntity set endTime=?1 where id=?2")
    void setEndTime(LocalTime endTime, Long pharmacyId);
    @Transactional
    @Modifying
    @Query("update PharmacyEntity set username=?1 where id=?2")
    void setUsername(String username, Long pharmacyId);
    @Transactional
    @Modifying
    @Query("update PharmacyEntity set pharmacyName=?1 where id=?2")
    void setPharmacyName(String pharmacyName, Long pharmacyId);
    @Transactional
    @Modifying
    @Query("update PharmacyEntity set phone=?1 where id=?2")
    void setPharmacyPhone(String phone, Long pharmacyId);
    @Transactional
    @Modifying
    @Query("update PharmacyEntity set infoUz=?1 where id=?2")
    void setUzInfo(String uzInfo, Long pharmacyId);
    @Transactional
    @Modifying
    @Query("update PharmacyEntity set infoTr=?1 where id=?2")
    void setTrInfo(String trInfo, Long pharmacyId);
    @Transactional
    @Modifying
    @Query("update PharmacyEntity set infoRu=?1 where id=?2")
    void setRuInfo(String ruInfo, Long pharmacyId);
    @Transactional
    @Modifying
    @Query("update PharmacyEntity set infoEn=?1 where id=?2")
    void setEnInfo(String enInfo, Long pharmacyId);
    @Transactional
    @Modifying
    @Query("update PharmacyEntity set latitude=?1, longitude=?2 where id=?3")
    void setLocation(Double latitude, Double longitude, Long pharmacyId);
    @Transactional
    @Modifying
    @Query("update PharmacyEntity set activeStatus=?1 where id=?2")
    void markAsDone(ActiveStatus activeStatus, Long pharmacyId);
    @Transactional
    @Modifying
    @Query("update PharmacyEntity set activeStatus=?1 where id=?2")
    void changeStatus(ActiveStatus status, Long pharmacyId);

    @Query(value = "SELECT get_nearest_pharmacies(?1,?2,?3)",nativeQuery = true)
    List<PharmacyMapper> get10pharmacy(Double latitude, Double longitude, PharmacyType pharmacy);
}
