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
public class JobSchedular {

    @Autowired
    private PointPlayService gamePlayService;

    @Autowired
    private CommissionService commissionService;

    //@Scheduled(cron = "0 0/15 * * * ?")
    //@Scheduled(cron = "0 0/15 * * * ?")
    public void scheduleFixedDelayTaskBetWinner() {
        String drawTime = null;
        try {
            Thread.sleep(1000);
            drawTime = prevDrawTime();
            log.info("Job Started At: {}, drawTime: {}", LocalDateTime.now(), drawTime);
            gamePlayService.decideWinner(drawTime);
        } catch (Exception e) {
            log.error("issue while running cron scheduleFixedDelayTaskBetWinner: {} ", drawTime, e);
        }
    }

    @Scheduled(cron = "0 40 23 * * ?")
    public void scheduleFixedDelayTaskAuditReport() {
        try {
            log.info("Job Started At: {}", LocalDateTime.now());
            commissionService.getTodaysAllRetailersReport();
        } catch (Exception e) {
            log.error("issue while running cron scheduleFixedDelayTaskAuditReport: {} ", e);
        }
    }
}
