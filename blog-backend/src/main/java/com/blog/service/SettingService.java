package com.blog.service;

public interface SettingService {

    String get(String key);

    void set(String key, String value);
}
