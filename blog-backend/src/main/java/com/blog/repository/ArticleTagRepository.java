package com.blog.repository;

import com.blog.entity.ArticleTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {

    List<ArticleTag> findByArticleId(Long articleId);

    Page<ArticleTag> findByTagId(Long tagId, Pageable pageable);

    void deleteByArticleId(Long articleId);

    void deleteByTagId(Long tagId);

    long countByTagId(Long tagId);
}
