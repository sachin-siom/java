package com.games.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

import static com.games.util.GameUtil.prevDrawTime;

@Slf4j
@Configuration
@EnableScheduling
public class WinnerPointJobSchedular {

    @Autowired
    private PointPlayService gamePlayService;

    @Scheduled(cron = "0 0/15 * * * ?")
    public void scheduleFixedDelayTask() {
        String drawTime = null;
        try {
            Thread.sleep(1000);
            drawTime = prevDrawTime();
            log.info("Job Started At: {}, drawTime: {}", LocalDateTime.now(), drawTime);
            gamePlayService.decideWinner(drawTime);
        } catch (Exception e) {
            log.error("issue while running cron: {} ", drawTime, e);
        }
    }
}
