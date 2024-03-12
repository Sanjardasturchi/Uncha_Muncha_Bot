package com.example.Uncha_Muncha_Bot.mapper;

import java.time.LocalTime;

public interface PharmacyMapper {
    Long getPharmacyId();

    String getPharmacyName();
    String getPharmacyPhone();
    LocalTime getPharmacyStartTime();
    LocalTime getPharmacyEndTime();
    String getInfoUz();
    String getInfoRu();
    String getInfoEn();
    String getInfoTr();
    String getPharmacyUserName();
    Double getDistance();

}
