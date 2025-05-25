package com.example.devicemanager.service;

import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheEvictSchedulerService {

    private final CacheManager cacheManager;

    // Every day at 00:00
    @Scheduled(cron = "0 0 0 * * ?")
    public void evictAllCachesAtMidnight() {
        log.info("Evicting all caches at {}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        cacheManager.getCacheNames().forEach(cacheName -> {
            log.info("Clearing cache: {}", cacheName);
            cacheManager.getCache(cacheName).clear();
        });
    }
}
