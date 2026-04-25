package com.blog.service.impl;

import com.blog.dto.UserProfileDTO;
import com.blog.entity.User;
import com.blog.repository.ArticleLikeRepository;
import com.blog.repository.ArticleRepository;
import com.blog.repository.UserRepository;
import com.blog.service.ProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final ArticleLikeRepository articleLikeRepository;

    public ProfileServiceImpl(UserRepository userRepository,
                              ArticleRepository articleRepository,
                              ArticleLikeRepository articleLikeRepository) {
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.articleLikeRepository = articleLikeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        long articleCount = articleRepository.countByAuthorId(userId);
        long totalLikes = articleLikeRepository.countByArticleAuthorId(userId);

        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .articleCount(articleCount)
                .totalLikes(totalLikes)
                .build();
    }

    @Override
    @Transactional
    public void updateBio(Long userId, String bio) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        user.setBio(bio != null ? bio.strip() : null);
        userRepository.save(user);
    }
}
