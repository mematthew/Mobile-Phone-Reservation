package com.example.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.config.ApplicationConfig;
import com.example.config.RabbitInitialise;
import com.example.model.dao.MobilePhoneDao;
import com.example.model.entity.MobilePhoneEntity;
import com.example.model.exception.MobilePhoneNotAvailableException;
import com.example.model.exception.MobilePhoneNotBookedException;
import com.example.rabbitmq.DeclerationUtils;
import com.example.rabbitmq.QueueMessageSender;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;


public class ReservationServiceTest {


    @Mock
    private MobilePhoneDao mobilePhoneDao;

    @Mock
    private QueueMessageSender queueMessageSender;


    @InjectMocks
    private ReservationService reservationService;


    @Mock
    private ApplicationConfig applicationConfig;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // Mock the behavior of applicationConfig
        when(applicationConfig.getBookPhoneExchange()).thenReturn("BookPhoneExchange");
        when(applicationConfig.getReturnPhoneExchange()).thenReturn("ReturnPhoneExchange");

    }
    @Test
    public void testBookPhoneSuccess() {
        String imei = "111111111111111";
        String bookedBy = "user123";
        MobilePhoneEntity mobilePhoneEntity = new MobilePhoneEntity();
        mobilePhoneEntity.setAvailable(true);
        mobilePhoneEntity.setImei(imei);
        doReturn(Optional.of(mobilePhoneEntity)).when(mobilePhoneDao).findByImei(imei);

        reservationService.bookPhone(imei, bookedBy);

        verify(mobilePhoneDao, times(1)).findByImei(imei);
        verify(mobilePhoneDao, times(1)).save(mobilePhoneEntity);
        verify(queueMessageSender, times(1)).send(eq("BookPhoneExchange"), isNull(),
            eq(imei + " is booked"));
    }

    @Test
    public void testBookPhoneNotAvailable() {
        String imei = "123456789012345";
        String bookedBy = "user123";
        MobilePhoneEntity mobilePhoneEntity = new MobilePhoneEntity();
        mobilePhoneEntity.setAvailable(false);
        when(mobilePhoneDao.findByImei(imei)).thenReturn(Optional.of(mobilePhoneEntity));

        // Assert that MobilePhoneNotAvailableException is thrown
        assertThrows(MobilePhoneNotAvailableException.class,
            () -> reservationService.bookPhone(imei, bookedBy));

        verify(mobilePhoneDao, times(1)).findByImei(imei);
        verifyNoMoreInteractions(mobilePhoneDao);
        verifyNoInteractions(queueMessageSender);
    }


    @Test
    public void testReturnPhoneSuccess() {
        String imei = "123456789012345";
        MobilePhoneEntity mobilePhoneEntity = new MobilePhoneEntity();
        mobilePhoneEntity.setAvailable(false);
        mobilePhoneEntity.setBookedBy("user123");
        mobilePhoneEntity.setBookedDate(new Date());
        when(mobilePhoneDao.findByImei(imei)).thenReturn(Optional.of(mobilePhoneEntity));

        reservationService.returnPhone(imei);

        verify(mobilePhoneDao, times(1)).findByImei(imei);
        verify(mobilePhoneDao, times(1)).save(mobilePhoneEntity);
        verify(queueMessageSender, times(1)).send(eq("ReturnPhoneExchange"), isNull(),
            eq(imei + " is returned"));
    }

    @Test
    public void testReturnPhoneNotBooked() {
        String imei = "111111111111111";
        MobilePhoneEntity mobilePhoneEntity = new MobilePhoneEntity();
        mobilePhoneEntity.setAvailable(true);
        when(mobilePhoneDao.findByImei(imei)).thenReturn(Optional.of(mobilePhoneEntity));

        // Assert that MobilePhoneNotBookedException is thrown
        assertThrows(MobilePhoneNotBookedException.class, () -> reservationService.returnPhone(imei));

        verify(mobilePhoneDao, times(1)).findByImei(imei);
        verifyNoMoreInteractions(mobilePhoneDao);
        verifyNoInteractions(queueMessageSender);
    }

}
