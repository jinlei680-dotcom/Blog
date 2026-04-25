package com.blog.service;

import com.blog.dto.ArticleSummaryDTO;
import com.blog.dto.TagDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TagService {

    TagDTO create(String name);

    List<TagDTO> listAll();

    void delete(Long tagId);

    Page<ArticleSummaryDTO> listArticlesByTag(Long tagId, int page, int size);
}
