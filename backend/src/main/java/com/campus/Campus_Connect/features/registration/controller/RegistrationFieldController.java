package com.campus.Campus_Connect.features.registration.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationFieldRequest;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationFieldResponse;
import com.campus.Campus_Connect.features.registration.service.RegistrationFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class RegistrationFieldController {

    private final RegistrationFieldService registrationFieldService;


    @PostMapping("/{eventId}/registration-fields")
    public ApiResponse<RegistrationFieldResponse> createField(
            @PathVariable Integer eventId,
            @RequestBody RegistrationFieldRequest request) {

        return registrationFieldService.createField(
                eventId,
                request
        );
    }


    @GetMapping("/{eventId}/registration-fields")
    public ApiResponse<List<RegistrationFieldResponse>> getFields(
            @PathVariable Integer eventId) {

        return registrationFieldService.getFields(eventId);
    }


    @GetMapping("/{eventId}/registration-form")
    public ApiResponse<List<RegistrationFieldResponse>> getForm(
            @PathVariable Integer eventId) {

        return registrationFieldService.getFields(eventId);
    }


    @PutMapping(
            "/{eventId}/registration-fields/{fieldId}"
    )
    public ApiResponse<RegistrationFieldResponse> updateField(
            @PathVariable Integer eventId,
            @PathVariable Integer fieldId,
            @RequestBody RegistrationFieldRequest request) {

        return registrationFieldService.updateField(
                eventId,
                fieldId,
                request
        );
    }


    @DeleteMapping(
            "/{eventId}/registration-fields/{fieldId}"
    )
    public ApiResponse<Void> deleteField(
            @PathVariable Integer eventId,
            @PathVariable Integer fieldId) {

        return registrationFieldService.deleteField(
                eventId,
                fieldId
        );
    }
}