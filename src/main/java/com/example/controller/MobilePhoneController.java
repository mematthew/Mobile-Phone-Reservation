package com.example.controller;

import com.example.model.exception.MobilePhoneNotAvailableException;
import com.example.model.exception.MobilePhoneNotBookedException;
import com.example.model.exception.MobilePhoneNotFoundException;
import com.example.service.ReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/mobile")
@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class MobilePhoneController {

    private final ReservationService reservationService;


    /**
     * Books a mobile phone with the specified IMEI.
     *
     * @param imei The IMEI of the mobile phone to be booked.
     * @return Response entity indicating the booking status.
     */
    @PostMapping("/{imei}/book")
    @Secured("hasRole('USER')")
    public ResponseEntity<String> bookPhone(
        @Valid @NotBlank(message = "IMEI is required") @PathVariable String imei
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            log.debug("Booking phone with IMEI: {} for user: {}", imei, userName);
            reservationService.bookPhone(imei, userName);
            return ResponseEntity.ok("Phone booked successfully");
        } catch (MobilePhoneNotFoundException e) {
            log.debug(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (MobilePhoneNotAvailableException e) {
            log.debug(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            log.error("Exception occurred during bookPhone as {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    /**
     * Returns a mobile phone with the specified IMEI.
     *
     * @param imei The IMEI of the mobile phone to be returned.
     * @return Response entity indicating the return status.
     */
    @PostMapping("/{imei}/return")
    @Secured("hasRole('USER')")
    public ResponseEntity<String> returnPhone(
        @Valid @NotBlank(message = "IMEI is required") @PathVariable String imei) {
        try {
            log.debug("Returning phone with IMEI: {}", imei);
            reservationService.returnPhone(imei);
            return ResponseEntity.ok("Phone returned successfully");
        } catch (MobilePhoneNotFoundException e) {
            log.debug(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (MobilePhoneNotBookedException e) {
            log.debug(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            log.debug("Exception occurred during returnPhone as {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }
}



