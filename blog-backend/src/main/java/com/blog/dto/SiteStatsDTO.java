package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteStatsDTO {

    private long articleCount;
    private long totalWords;
    private long totalLikes;
    private long userCount;
    private long commentCount;
}
