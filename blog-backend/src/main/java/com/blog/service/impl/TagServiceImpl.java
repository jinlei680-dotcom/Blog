package com.blog.service.impl;

import com.blog.dto.ArticleSummaryDTO;
import com.blog.dto.TagDTO;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.repository.ArticleLikeRepository;
import com.blog.repository.ArticleTagRepository;
import com.blog.repository.TagRepository;
import com.blog.service.TagService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;
    private final ArticleLikeRepository articleLikeRepository;

    public TagServiceImpl(TagRepository tagRepository,
                          ArticleTagRepository articleTagRepository,
                          ArticleLikeRepository articleLikeRepository) {
        this.tagRepository = tagRepository;
        this.articleTagRepository = articleTagRepository;
        this.articleLikeRepository = articleLikeRepository;
    }

    @Override
    public TagDTO create(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        String trimmedName = name.trim();
        if (tagRepository.existsByName(trimmedName)) {
            throw new DuplicateKeyException("标签名称已存在");
        }
        Tag tag = Tag.builder().name(trimmedName).build();
        tag = tagRepository.save(tag);
        return toTagDTO(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDTO> listAll() {
        return tagRepository.findAll().stream()
                .map(tag -> {
                    long articleCount = articleTagRepository.countByTagId(tag.getId());
                    return TagDTO.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .articleCount(articleCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("标签不存在"));
        articleTagRepository.deleteByTagId(tagId);
        tagRepository.delete(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleSummaryDTO> listArticlesByTag(Long tagId, int page, int size) {
        if (!tagRepository.existsById(tagId)) {
            throw new EntityNotFoundException("标签不存在");
        }
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "article.createdAt"));
        Page<ArticleTag> articleTags = articleTagRepository.findByTagId(tagId, pageRequest);
        return articleTags.map(at -> {
            Article article = at.getArticle();
            long likeCount = articleLikeRepository.countByArticleId(article.getId());
            List<TagDTO> tags = articleTagRepository.findByArticleId(article.getId()).stream()
                    .map(articleTag -> toTagDTO(articleTag.getTag()))
                    .collect(Collectors.toList());
            return ArticleSummaryDTO.builder()
                    .id(article.getId())
                    .title(article.getTitle())
                    .authorName(article.getAuthor().getUsername())
                    .authorId(article.getAuthor().getId())
                    .createdAt(article.getCreatedAt())
                    .likeCount(likeCount)
                    .featured(article.getFeatured())
                    .tags(tags)
                    .build();
        });
    }

    private TagDTO toTagDTO(Tag tag) {
        return TagDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}
