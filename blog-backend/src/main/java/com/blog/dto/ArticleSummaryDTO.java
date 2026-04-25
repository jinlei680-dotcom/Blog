package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleSummaryDTO {

    private Long id;
    private String title;
    private String authorName;
    private Long authorId;
    private LocalDateTime createdAt;
    private long likeCount;
    private List<TagDTO> tags;
}
