package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.ArticleSummaryDTO;
import com.blog.dto.CreateTagRequest;
import com.blog.dto.TagDTO;
import com.blog.service.TagService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<TagDTO> create(@RequestBody CreateTagRequest request) {
        TagDTO tag = tagService.create(request.getName());
        return ApiResponse.success("标签创建成功", tag);
    }

    @GetMapping
    public ApiResponse<List<TagDTO>> listAll() {
        List<TagDTO> tags = tagService.listAll();
        return ApiResponse.success(tags);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ApiResponse.success("标签删除成功", null);
    }

    @GetMapping("/{id}/articles")
    public ApiResponse<Page<ArticleSummaryDTO>> listArticlesByTag(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ArticleSummaryDTO> articles = tagService.listArticlesByTag(id, page, size);
        return ApiResponse.success(articles);
    }
}
