package com.campus.Campus_Connect.features.event.dto.request;


import com.campus.Campus_Connect.features.event.entity.enums.VisibilityType;
import com.campus.Campus_Connect.features.event.entity.enums.RegistrationType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.Instant;




@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEventRequest {

    @NotBlank(message = "Title is required.")
    private String title;

    private String description;

    @NotNull(message = "Latitude is required.")
    @DecimalMin(value = "-90.0", message = "Latitude must be at least -90.")
    @DecimalMax(value = "90", message = "Latitude must be at most 90.")
    private Double latitude;

    @NotNull(message = "Longitude is required.")
    @DecimalMin(value = "-180.0", message = "Longitude must be at least -180.")
    @DecimalMax(value = "180.0", message = "Longitude must be at most 180.")
    private Double longitude;

    @NotNull(message = "Start time is required.")
    private Instant startTime;
    private Instant endTime;

    private Integer clubId;

    private String hostName;

    private String venue;

    @NotBlank(message = "Visibility type is required.")
    private VisibilityType visibilityType;

    private String visibilityValue;

    @NotBlank(message = "Registration type is required.")
    private RegistrationType registrationType;

    private String registrationLink;

    @NotNull(message = "Priority is required.")
    @Min(value = 1, message = "Priority must be between 1 and 6.")
    @Max(value = 6, message = "Priority must be between 1 and 6.")
    private Integer priority;
}