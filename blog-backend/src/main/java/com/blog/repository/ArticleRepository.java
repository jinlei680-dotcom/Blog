package com.blog.repository;

import com.blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.title LIKE %:keyword% OR a.content LIKE %:keyword% ORDER BY a.createdAt DESC")
    Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByAuthorId(Long authorId);

    Page<Article> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
}
