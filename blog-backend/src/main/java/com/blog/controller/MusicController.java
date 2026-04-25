package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.dto.CreateMusicRequest;
import com.blog.dto.MusicDTO;
import com.blog.service.MusicService;
import org.springframework.web.bind.annotation.*;

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
        List<MusicDTO> musicList = musicService.list();
        return ApiResponse.success(musicList);
    }

    @PostMapping
    public ApiResponse<MusicDTO> create(@RequestBody CreateMusicRequest request) {
        MusicDTO music = musicService.create(request);
        return ApiResponse.success("音乐创建成功", music);
    }
}
