package com.blog.service.impl;

import com.blog.dto.CreateMusicRequest;
import com.blog.dto.MusicDTO;
import com.blog.entity.Music;
import com.blog.repository.MusicRepository;
import com.blog.service.MusicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MusicServiceImpl implements MusicService {

    private final MusicRepository musicRepository;

    public MusicServiceImpl(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MusicDTO> list() {
        return musicRepository.findAll().stream()
                .map(this::toMusicDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MusicDTO create(CreateMusicRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("音乐名称不能为空");
        }
        if (request.getFilePath() == null || request.getFilePath().trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        Music music = Music.builder()
                .name(request.getName().trim())
                .filePath(request.getFilePath().trim())
                .build();

        music = musicRepository.save(music);
        return toMusicDTO(music);
    }

    private MusicDTO toMusicDTO(Music music) {
        return MusicDTO.builder()
                .id(music.getId())
                .name(music.getName())
                .filePath(music.getFilePath())
                .createdAt(music.getCreatedAt())
                .build();
    }
}
