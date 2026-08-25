package com.campus.Campus_Connect.features.registration.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationFieldOptionRequest;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationFieldRequest;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationFieldOptionResponse;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationFieldResponse;
import com.campus.Campus_Connect.features.registration.entity.RegistrationField;
import com.campus.Campus_Connect.features.registration.entity.RegistrationFieldOption;
import com.campus.Campus_Connect.features.registration.repository.RegistrationFieldOptionRepository;
import com.campus.Campus_Connect.features.registration.repository.RegistrationFieldRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationFieldService {

    private final EventRepository eventRepository;
    private final RegistrationFieldRepository fieldRepository;
    private final RegistrationFieldOptionRepository optionRepository;


    // ============================================================
    // CREATE QUESTION
    // ============================================================

    @Transactional
    public ApiResponse<RegistrationFieldResponse> createField(
            Integer eventId,
            RegistrationFieldRequest request) {

        User currentUser = SecurityUtils.getCurrentUser();

        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        if (!event.getCreator().getId().equals(currentUser.getId())) {
            return ApiResponse.failure(
                    "Only the event organizer can manage registration fields."
            );
        }

        RegistrationField field = RegistrationField.builder()
                .event(event)
                .fieldLabel(request.getFieldLabel())
                .fieldType(request.getFieldType())
                .required(request.getRequired())
                .placeholder(request.getPlaceholder())
                .fieldOrder(request.getFieldOrder())
                .build();

        field = fieldRepository.save(field);

        if (request.getOptions() != null) {

            for (RegistrationFieldOptionRequest optionRequest
                    : request.getOptions()) {

                RegistrationFieldOption option =
                        RegistrationFieldOption.builder()
                                .field(field)
                                .optionValue(optionRequest.getOptionValue())
                                .optionOrder(optionRequest.getOptionOrder())
                                .build();

                optionRepository.save(option);
            }
        }

        return ApiResponse.success(
                toResponse(field),
                "Registration field created successfully."
        );
    }


    // ============================================================
    // GET EVENT FIELDS
    // ============================================================

    public ApiResponse<List<RegistrationFieldResponse>> getFields(
            Integer eventId) {

        if (!eventRepository.existsById(eventId)) {
            return ApiResponse.failure("Event not found.");
        }

        List<RegistrationField> fields =
                fieldRepository.findByEventIdOrderByFieldOrderAsc(eventId);

        return ApiResponse.success(
                fields.stream()
                        .map(this::toResponse)
                        .toList(),
                "Registration fields fetched successfully."
        );
    }


    // ============================================================
    // UPDATE QUESTION
    // ============================================================

    @Transactional
    public ApiResponse<RegistrationFieldResponse> updateField(
            Integer eventId,
            Integer fieldId,
            RegistrationFieldRequest request) {

        User currentUser = SecurityUtils.getCurrentUser();

        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        if (!event.getCreator().getId().equals(currentUser.getId())) {
            return ApiResponse.failure(
                    "Only the event organizer can manage registration fields."
            );
        }

        RegistrationField field =
                fieldRepository.findByIdAndEventId(fieldId, eventId)
                        .orElse(null);

        if (field == null) {
            return ApiResponse.failure(
                    "Registration field not found."
            );
        }

        field.setFieldLabel(request.getFieldLabel());
        field.setFieldType(request.getFieldType());
        field.setRequired(request.getRequired());
        field.setPlaceholder(request.getPlaceholder());
        field.setFieldOrder(request.getFieldOrder());

        fieldRepository.save(field);

        // Replace existing options.
        List<RegistrationFieldOption> oldOptions =
                optionRepository.findByFieldIdOrderByOptionOrderAsc(fieldId);

        optionRepository.deleteAll(oldOptions);

        if (request.getOptions() != null) {

            for (RegistrationFieldOptionRequest optionRequest
                    : request.getOptions()) {

                optionRepository.save(
                        RegistrationFieldOption.builder()
                                .field(field)
                                .optionValue(optionRequest.getOptionValue())
                                .optionOrder(optionRequest.getOptionOrder())
                                .build()
                );
            }
        }

        return ApiResponse.success(
                toResponse(field),
                "Registration field updated successfully."
        );
    }


    // ============================================================
    // DELETE QUESTION
    // ============================================================

    @Transactional
    public ApiResponse<Void> deleteField(
            Integer eventId,
            Integer fieldId) {

        User currentUser = SecurityUtils.getCurrentUser();

        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        if (!event.getCreator().getId().equals(currentUser.getId())) {
            return ApiResponse.failure(
                    "Only the event organizer can manage registration fields."
            );
        }

        RegistrationField field =
                fieldRepository.findByIdAndEventId(fieldId, eventId)
                        .orElse(null);

        if (field == null) {
            return ApiResponse.failure(
                    "Registration field not found."
            );
        }

        fieldRepository.delete(field);

        return ApiResponse.success(
                null,
                "Registration field deleted successfully."
        );
    }


    // ============================================================
    // MAPPER
    // ============================================================

    private RegistrationFieldResponse toResponse(
            RegistrationField field) {

        List<RegistrationFieldOptionResponse> options =
                optionRepository
                        .findByFieldIdOrderByOptionOrderAsc(field.getId())
                        .stream()
                        .map(option ->
                                RegistrationFieldOptionResponse.builder()
                                        .id(option.getId())
                                        .optionValue(option.getOptionValue())
                                        .optionOrder(option.getOptionOrder())
                                        .build()
                        )
                        .toList();

        return RegistrationFieldResponse.builder()
                .id(field.getId())
                .fieldLabel(field.getFieldLabel())
                .fieldType(field.getFieldType())
                .required(field.getRequired())
                .placeholder(field.getPlaceholder())
                .fieldOrder(field.getFieldOrder())
                .options(options)
                .build();
    }
}