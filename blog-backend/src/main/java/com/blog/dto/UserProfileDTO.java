package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    private Long id;
    private String username;
    private String avatarUrl;
    private String bio;
    private LocalDateTime createdAt;
    private long articleCount;
    private long totalLikes;
}
