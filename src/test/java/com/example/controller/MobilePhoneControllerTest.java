package com.example.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.model.exception.MobilePhoneNotAvailableException;
import com.example.model.exception.MobilePhoneNotBookedException;
import com.example.model.exception.MobilePhoneNotFoundException;
import com.example.service.ReservationService;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class MobilePhoneControllerTest {

    @Mock
    private ReservationService reservationService;
    @InjectMocks
    private MobilePhoneController mobilePhoneController;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        authentication = Mockito.mock(Authentication.class);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

    }
    @Test
    public void testBookPhoneSuccess() {

        doNothing().when(reservationService).bookPhone(any(), any());

        ResponseEntity<String> response = mobilePhoneController.bookPhone("123456789012345");

        verify(reservationService, times(1)).bookPhone(any(), any());
        verify(authentication, times(1)).getName();
        verifyNoMoreInteractions(reservationService);

        // Assert the response
        Assertions.assertNotNull(response);
        Assertions.assertEquals("Phone booked successfully", response.getBody());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    public void testBookPhoneNotFound() {


        doThrow(new MobilePhoneNotFoundException("Imei not found")).when(reservationService).bookPhone(any(), any());

        ResponseEntity<String> response = mobilePhoneController.bookPhone("123456789012345");

        verify(reservationService, times(1)).bookPhone(any(), any());
        verifyNoMoreInteractions(reservationService);

        // Assert the response
        Assertions.assertNotNull(response);
        Assertions.assertEquals("Imei not found", response.getBody());

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testBookPhoneNotAvailable() {

        doThrow(new MobilePhoneNotAvailableException("Imei not available")).when(reservationService)
            .bookPhone(any(), any());

        ResponseEntity<String> response = mobilePhoneController.bookPhone("123456789012345");

        verify(reservationService, times(1)).bookPhone(any(), any());
        verifyNoMoreInteractions(reservationService);

        // Assert the response
        Assertions.assertNotNull(response);
        Assertions.assertEquals("Imei not available", response.getBody());
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testBookPhoneException() {

        doThrow(new RuntimeException("Imei not found")).when(reservationService).bookPhone(any(), any());

        ResponseEntity<String> response = mobilePhoneController.bookPhone("123456789012345");

        verify(reservationService, times(1)).bookPhone(any(), any());
        verifyNoMoreInteractions(reservationService);

        // Assert the response
        Assertions.assertNotNull(response);
        Assertions.assertEquals("An error occurred", response.getBody());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testReturnPhoneSuccess() {
        doNothing().when(reservationService).returnPhone(any());

        ResponseEntity<String> response = mobilePhoneController.returnPhone("123456789012345");

        verify(reservationService, times(1)).returnPhone(any());
        verifyNoMoreInteractions(reservationService);

        // Assert the response
        Assertions.assertNotNull(response);
        Assertions.assertEquals("Phone returned successfully", response.getBody());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testReturnPhoneNotFound() {
        doThrow(new MobilePhoneNotFoundException("Imei not found")).when(reservationService).returnPhone(any());

        ResponseEntity<String> response = mobilePhoneController.returnPhone("123456789012345");

        verify(reservationService, times(1)).returnPhone(any());
        verifyNoMoreInteractions(reservationService);

        // Assert the response
        Assertions.assertNotNull(response);
        Assertions.assertEquals("Imei not found", response.getBody());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testReturnPhoneNotBooked() {
        doThrow(new MobilePhoneNotBookedException("Imei not booked")).when(reservationService).returnPhone(any());

        ResponseEntity<String> response = mobilePhoneController.returnPhone("123456789012345");

        verify(reservationService, times(1)).returnPhone(any());
        verifyNoMoreInteractions(reservationService);

        // Assert the response
        Assertions.assertNotNull(response);
        Assertions.assertEquals("Imei not booked", response.getBody());
        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testReturnPhoneException() {
        doThrow(new RuntimeException("Internal error")).when(reservationService).returnPhone(any());

        ResponseEntity<String> response = mobilePhoneController.returnPhone("123456789012345");

        verify(reservationService, times(1)).returnPhone(any());
        verifyNoMoreInteractions(reservationService);

        // Assert the response
        Assertions.assertNotNull(response);
        Assertions.assertEquals("An error occurred", response.getBody());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}