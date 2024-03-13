package com.example.Uncha_Muncha_Bot.mapper;

import java.time.LocalTime;

public interface PharmacyMapper {
    Long getPharmacyId();
    String getPharmacyName();
    String getPhone();
    LocalTime getPharmacyStartTime();
    LocalTime getPharmacyEndTime();
    String getPharmacyUz();
    String getPharmacyRu();
    String getPharmacyEn();
    String getPharmacyTr();
    String getUserName();
    Double getDistance();
    Double getLatitude();
    Double getLongitude();

}
