package com.karam.pubfinder.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {

    @NotNull(message = "Pub ID is required")
    private Long pubId;

    @NotBlank(message = "Content is required")
    @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
    private String content;

    @NotNull(message = "Rate is required")
    @Min(value = 0, message = "Rate must be at least 0")
    @Max(value = 5, message = "Rate must not exceed 5")
    private Integer rate;
}