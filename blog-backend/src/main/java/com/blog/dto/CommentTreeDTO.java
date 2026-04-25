package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentTreeDTO {

    private Long id;
    private String content;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<CommentTreeDTO> children = new ArrayList<>();
}
