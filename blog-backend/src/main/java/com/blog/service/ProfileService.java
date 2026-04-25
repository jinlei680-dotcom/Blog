package com.blog.service;

import com.blog.dto.UserProfileDTO;

public interface ProfileService {
    UserProfileDTO getProfile(Long userId);
    void updateBio(Long userId, String bio);
}
