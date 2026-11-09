package com.campus.Campus_Connect.features.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePostRequest {

    @NotBlank(message = "Body is required")
    private String body;

    private List<Integer> tags;

    private Integer eventCategoryId;

    private boolean removeImage;
}
