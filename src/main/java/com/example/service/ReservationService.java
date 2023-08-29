package com.example.service;

import com.example.config.ApplicationConfig;
import com.example.model.dao.MobilePhoneDao;
import com.example.model.entity.MobilePhoneEntity;
import com.example.model.exception.MobilePhoneNotAvailableException;
import com.example.model.exception.MobilePhoneNotBookedException;
import com.example.model.exception.MobilePhoneNotFoundException;
import com.example.rabbitmq.QueueMessageSender;
import jakarta.transaction.Transactional;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final MobilePhoneDao mobilePhoneDao;
    private final QueueMessageSender queueMessageSender;
    private final ApplicationConfig applicationConfig;


    /**
     * Books a mobile phone with the specified IMEI.
     *
     * @param imei     The IMEI of the mobile phone to be booked.
     * @param bookedBy The username of the user booking the phone.
     * @throws MobilePhoneNotFoundException     If the mobile phone with the given IMEI is not found.
     * @throws MobilePhoneNotAvailableException If the mobile phone is not available for booking.
     */
    @Transactional
    public void bookPhone(String imei, String bookedBy) {
        log.debug("Booking phone with IMEI: {} by user: {}", imei, bookedBy);

        MobilePhoneEntity mobilePhoneEntity = mobilePhoneDao.findByImei(imei)
            .orElseThrow(() -> new MobilePhoneNotFoundException(imei + " is not valid"));

        if (mobilePhoneEntity.isAvailable()) {
            mobilePhoneEntity.setAvailable(false);
            mobilePhoneEntity.setBookedBy(bookedBy);
            mobilePhoneEntity.setBookedDate(new Date());
            mobilePhoneDao.save(mobilePhoneEntity); // Save changes to the database
            queueMessageSender.send(applicationConfig.getBookPhoneExchange(), null, imei + " is booked");
            log.debug("Phone with IMEI: {} is successfully booked by user: {}", imei, bookedBy);
        } else {
            log.debug("Phone with IMEI: {} is being used by someone else", imei);
            throw new MobilePhoneNotAvailableException(imei + " is being used by " + mobilePhoneEntity.getBookedBy());
        }
    }

    /**
     * Returns a mobile phone with the specified IMEI.
     *
     * @param imei The IMEI of the mobile phone to be returned.
     * @throws MobilePhoneNotFoundException  If the mobile phone with the given IMEI is not found.
     * @throws MobilePhoneNotBookedException If the mobile phone is not currently booked.
     */
    public void returnPhone(String imei) {
        log.debug("Returning phone with IMEI: {}", imei);

        MobilePhoneEntity mobilePhoneEntity = mobilePhoneDao.findByImei(imei)
            .orElseThrow(() -> new MobilePhoneNotFoundException(imei + " is not valid"));

        if (!mobilePhoneEntity.isAvailable()) {
            mobilePhoneEntity.setAvailable(true);
            mobilePhoneEntity.setBookedBy(null);
            mobilePhoneEntity.setBookedDate(null);
            mobilePhoneDao.save(mobilePhoneEntity); // Save changes to the database
            queueMessageSender.send(applicationConfig.getReturnPhoneExchange(), null, imei + " is returned");
            log.debug("Phone with IMEI: {} is successfully returned", imei);
        } else {
            throw new MobilePhoneNotBookedException(imei + " is not booked");
        }
    }
}