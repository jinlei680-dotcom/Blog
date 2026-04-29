package com.blog.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UpdateArticleRequest {

    private String title;
    private String content;
    private List<Long> tagIds;
    private Boolean featured;

    public UpdateArticleRequest(String title, String content, List<Long> tagIds) {
        this.title = title;
        this.content = content;
        this.tagIds = tagIds;
    }

    public UpdateArticleRequest(String title, String content, List<Long> tagIds, Boolean featured) {
        this.title = title;
        this.content = content;
        this.tagIds = tagIds;
        this.featured = featured;
    }
}
