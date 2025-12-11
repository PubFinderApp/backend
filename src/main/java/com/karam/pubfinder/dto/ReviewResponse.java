package com.karam.pubfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;
    private Long userId;
    private String username;
    private Long pubId;
    private String pubTitle;
    private String content;
    private Integer rate;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Use @JsonProperty to ensure correct serialization
    @JsonProperty("isLikedByCurrentUser")
    private boolean isLikedByCurrentUser;
}