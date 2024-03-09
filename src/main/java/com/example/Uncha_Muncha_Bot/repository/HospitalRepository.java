package com.example.Uncha_Muncha_Bot.repository;

import com.example.Uncha_Muncha_Bot.entity.HospitalEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalTime;

public interface HospitalRepository extends CrudRepository<HospitalEntity, Long> {
    @Transactional
    @Modifying
    @Query("update HospitalEntity set startTime=?1 where id=?2")
    void setStartTime(LocalTime time, Long hospitalId);

    @Transactional
    @Modifying
    @Query("update HospitalEntity set endTime=?1 where id=?2")
    void setEndTime(LocalTime time, Long hospitalId);

    @Transactional
    @Modifying
    @Query("update HospitalEntity set username=?1 where id=?2")
    void setUsername(String username, Long hospitalId);

    @Transactional
    @Modifying
    @Query("update HospitalEntity set phone=?1 where id=?2")
    void setPhone(String phone, Long hospitalId);

    @Transactional
    @Modifying
    @Query("update HospitalEntity set hospitalName=?1 where id=?2")
    void setName(String name, Long hospitalId);

    @Transactional
    @Modifying
    @Query("update HospitalEntity set infoUz=?1 where id=?2")
    void setInfoUz(String infoUz, Long hospitalId);

    @Transactional
    @Modifying
    @Query("update HospitalEntity set infoTr=?1 where id=?2")
    void setInfoTr(String infoTr, Long hospitalId);

    @Transactional
    @Modifying
    @Query("update HospitalEntity set infoRu=?1 where id=?2")
    void setInfoRu(String infoRu, Long hospitalId);

    @Transactional
    @Modifying
    @Query("update HospitalEntity set infoEn=?1 where id=?2")
    void setInfoEn(String infoEn, Long hospitalId);

    @Transactional
    @Modifying
    @Query("update HospitalEntity set latitude=?1, longitude=?2 where id=?3")
    void setLocation(Double latitude, Double longitude, Long id);

    @Transactional
    @Modifying
    @Query("update HospitalEntity set activeStatus=?1 where id=?2")
    void changeStatus(ActiveStatus status, Long hospitalId);
}
