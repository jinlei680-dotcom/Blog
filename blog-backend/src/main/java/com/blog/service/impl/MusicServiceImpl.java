package com.blog.service.impl;

import com.blog.dto.MusicDTO;
import com.blog.entity.Music;
import com.blog.repository.MusicRepository;
import com.blog.service.MusicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MusicServiceImpl implements MusicService {

    private final MusicRepository musicRepository;
    private static final String UPLOAD_DIR = "./uploads/music";

    public MusicServiceImpl(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MusicDTO> list() {
        return musicRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MusicDTO create(String name, MultipartFile file) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("歌曲名称不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择音频文件");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().matches(".*\\.(mp3|wav|ogg|flac|aac|m4a)$")) {
            throw new IllegalArgumentException("仅支持 mp3/wav/ogg/flac/aac/m4a 格式");
        }
        if (file.getSize() > 20 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过 20MB");
        }

        try {
            Path dir = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String ext = originalName.substring(originalName.lastIndexOf('.'));
            String fileName = UUID.randomUUID().toString() + ext;
            Path target = dir.resolve(fileName);
            file.transferTo(target.toFile());

            Music music = Music.builder()
                    .name(name.trim())
                    .filePath("/uploads/music/" + fileName)
                    .build();
            music = musicRepository.save(music);

            return toDTO(music);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("音乐不存在"));

        try {
            Path file = Paths.get("." + music.getFilePath()).toAbsolutePath().normalize();
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }

        musicRepository.delete(music);
    }

    private MusicDTO toDTO(Music m) {
        return MusicDTO.builder()
                .id(m.getId())
                .name(m.getName())
                .filePath(m.getFilePath())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
