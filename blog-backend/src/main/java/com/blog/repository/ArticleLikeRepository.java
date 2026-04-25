package com.blog.repository;

import com.blog.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {

    Optional<ArticleLike> findByUserIdAndArticleId(Long userId, Long articleId);

    long countByArticleId(Long articleId);

    boolean existsByUserIdAndArticleId(Long userId, Long articleId);

    void deleteByArticleId(Long articleId);

    @Query("SELECT COUNT(al) FROM ArticleLike al JOIN al.article a WHERE a.author.id = :authorId")
    long countByArticleAuthorId(@Param("authorId") Long authorId);
}
