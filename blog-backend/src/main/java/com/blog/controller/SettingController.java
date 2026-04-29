package com.blog.controller;

import com.blog.common.ApiResponse;
import com.blog.service.SettingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping("/music")
    public ApiResponse<String> getMusic() {
        String songs = settingService.get("music_songs");
        return ApiResponse.success(songs);
    }

    @PutMapping("/music")
    public ApiResponse<Void> updateMusic(@RequestBody String body) {
        settingService.set("music_songs", body);
        return ApiResponse.success("音乐配置已保存", null);
    }
}
