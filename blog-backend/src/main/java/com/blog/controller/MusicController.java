package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.MusicDTO;
import com.blog.service.MusicService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping
    public ApiResponse<List<MusicDTO>> list() {
        return ApiResponse.success(musicService.list());
    }

    @PostMapping
    public ApiResponse<MusicDTO> create(
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {
        MusicDTO music = musicService.create(name, file);
        return ApiResponse.success("音乐上传成功", music);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        musicService.delete(id);
        return ApiResponse.success("音乐已删除", null);
    }
}
