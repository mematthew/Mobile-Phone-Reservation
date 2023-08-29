package com.example.model.dao;

import com.example.model.entity.MobilePhoneEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MobilePhoneDao extends JpaRepository<MobilePhoneEntity, Long> {

    Optional<MobilePhoneEntity> findByImei(String imei);

}
