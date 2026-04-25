package com.blog.service;

import com.blog.dto.CreateMusicRequest;
import com.blog.dto.MusicDTO;

import java.util.List;

public interface MusicService {

    List<MusicDTO> list();

    MusicDTO create(CreateMusicRequest request);
}
