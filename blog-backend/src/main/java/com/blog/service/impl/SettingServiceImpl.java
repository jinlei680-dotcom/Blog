package com.blog.service.impl;

import com.blog.entity.Setting;
import com.blog.repository.SettingRepository;
import com.blog.service.SettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingServiceImpl implements SettingService {

    private final SettingRepository settingRepository;

    public SettingServiceImpl(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public String get(String key) {
        return settingRepository.findBySettingKey(key)
                .map(Setting::getValue)
                .orElse(null);
    }

    @Override
    @Transactional
    public void set(String key, String value) {
        Setting setting = settingRepository.findBySettingKey(key)
                .orElse(Setting.builder().settingKey(key).build());
        setting.setValue(value);
        settingRepository.save(setting);
    }
}
