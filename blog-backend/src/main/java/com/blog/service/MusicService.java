package com.blog.service;

import com.blog.dto.MusicDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MusicService {

    List<MusicDTO> list();

    MusicDTO create(String name, MultipartFile file);

    void delete(Long id);
}
