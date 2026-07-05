package com.campus.Campus_Connect.features.event.dto.request;


import com.campus.Campus_Connect.features.event.entity.enums.VisibilityType;
import com.campus.Campus_Connect.features.event.entity.enums.RegistrationType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.time.Instant;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventRequest {

    private String title;

    private String description;

    @DecimalMin(value = "-90.0", message = "Latitude must be at least -90.")
    @DecimalMax(value = "90.0", message = "Latitude must be at most 90.")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be at least -180.")
    @DecimalMax(value = "180.0", message = "Longitude must be at most 180.")
    private Double longitude;

    private Instant startTime;
    private Instant endTime;

    private Integer clubId;

    private String hostName;

    private String venue;

    private VisibilityType visibilityType;

    private String visibilityValue;

    private RegistrationType registrationType;

    private String registrationLink;

    @Min(value = 1, message = "Priority must be between 1 and 6.")
    @Max(value = 6, message = "Priority must be between 1 and 6.")
    private Integer priority;
}