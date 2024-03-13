package com.example.Uncha_Muncha_Bot.repository;

import com.example.Uncha_Muncha_Bot.entity.AutoSalonEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalTime;

public interface AutoSalonRepository extends CrudRepository<AutoSalonEntity,Long> {
    @Transactional
    @Modifying
    @Query("update AutoSalonEntity set activeStatus=?1 where id=?2")
    void changeStatus(ActiveStatus status, Long autoSalonId);

    @Transactional
    @Modifying
    @Query("update AutoSalonEntity set startTime=?1 where id=?2")
    void setStartTime(LocalTime parse,Long id);

    @Transactional
    @Modifying
    @Query("update AutoSalonEntity set endTime=?1 where id=?2")
    void setEndTime(LocalTime parse,Long id);

    @Transactional
    @Modifying
    @Query("update AutoSalonEntity set username=?1 where id=?2")
    void setUsername(String username, Long autoSalonId);

    @Transactional
    @Modifying
    @Query("update AutoSalonEntity set phone=?1 where id=?2")
    void setPhone(String phone, Long autoSalonId);

    @Transactional
    @Modifying
    @Query("update AutoSalonEntity set salonName=?1 where id=?2")
    void setBrand(String brandName, Long autoSalonId);

    @Transactional
    @Modifying
    @Query("update AutoSalonEntity set infoUz=?1 where id=?2")
    void setInfoUz(String info, Long autoSalonId);

    @Transactional
    @Modifying
    @Query("update AutoSalonEntity set infoTr=?1 where id=?2")
    void setInfoTr(String info, Long autoSalonId);

    @Transactional
    @Modifying
    @Query("update AutoSalonEntity set infoRu=?1 where id=?2")
    void setInfoRu(String info, Long autoSalonId);

    @Transactional
    @Modifying
    @Query("update AutoSalonEntity set infoEn=?1 where id=?2")
    void setInfoEn(String info, Long autoSalonId);

    @Transactional
    @Modifying
    @Query("update AutoSalonEntity set latitude=?1, longitude=?2 where id=?3")
    void setLocation(Double latitude, Double longitude, Long autoSalonId);
}
