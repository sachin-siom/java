package com.games.util;

import com.games.model.Sequence;
import com.games.repository.SequenceRepository;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
public class GameUtil {

    public static final String ROLE = "USER";
    public static final String PORTAL_UPDATE = "PORTAL_UPDATE";

    public static String upcomingDrawTime() {
        StringBuilder drawTime = new StringBuilder();
        LocalTime lt = LocalTime.now();
        if (lt.getMinute() >= 00 && lt.getMinute() < 15) {
            drawTime.append(lt.getHour()).append(15);
        } else if (lt.getMinute() >= 15 && lt.getMinute() < 30) {
            drawTime.append(lt.getHour()).append(30);
        } else if (lt.getMinute() >= 30 && lt.getMinute() < 45) {
            drawTime.append(lt.getHour()).append(45);
        } else if (lt.getMinute() >= 45 && lt.getMinute() < 60) {
            drawTime.append(lt.getHour() + 1).append("00");
        }
        log.info("drawTime: {}, hour:{}", drawTime, lt.getHour());
        if(lt.getHour() <= 9){
            return "0" + drawTime.toString();
        }
        return drawTime.toString();
    }

    public static String conver12HrsTime(String time){
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")).format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public static String prevDrawTime() {
        StringBuilder drawTime = new StringBuilder();
        LocalTime lt = LocalTime.now();
        if (lt.getMinute() >= 00 && lt.getMinute() < 15) {
            drawTime.append(lt.getHour()).append("00");
        } else if (lt.getMinute() >= 15 && lt.getMinute() < 30) {
            drawTime.append(lt.getHour()).append(15);
        } else if (lt.getMinute() >= 30 && lt.getMinute() < 45) {
            drawTime.append(lt.getHour()).append(30);
        } else if (lt.getMinute() >= 45 && lt.getMinute() < 60) {
            drawTime.append(lt.getHour()).append(45);
        }
        log.info("prevDrawTime -> drawTime: {}, hour:{}", drawTime, lt.getHour());
        if(lt.getHour() <= 9){
            return "0" + drawTime.toString();
        }
        return drawTime.toString();
    }

    public static double subtract(double a, double b) {
        BigDecimal c = BigDecimal.valueOf(a).subtract(BigDecimal.valueOf(b));
        return c.doubleValue();
    }

    public static double sum(double a, double b) {
        BigDecimal c = BigDecimal.valueOf(a).add(BigDecimal.valueOf(b));
        return c.doubleValue();
    }

    public static boolean validateDrawTime(String drawTime){
        LocalTime lt = LocalTime.now();
        Integer hour = Integer.parseInt(drawTime.substring(0, 2));
        boolean flag = false;
        if(lt.getHour() < hour){
            flag = true;
        } else if (lt.getHour() == hour){
            Integer min = Integer.parseInt(drawTime.substring(2, 4));
            if(lt.getMinute() <= min)
                flag = true;
        }
        return flag;
    }

    public static String getDrawTime(String drawTime){
        if (Objects.nonNull(drawTime)) {
            return drawTime;
        } else {
            return upcomingDrawTime();
        }
    }

    public static boolean isSufficientBalance(double playerWager, double balance) {
        return !(playerWager < balance);
    }

    public static String getTicketId(String drawTime, String retailId, SequenceRepository sequenceRepository) {
        log.info("drawTime: {}, retailId: {}", drawTime, retailId);
        return new StringBuilder().append(drawTime).append(sequenceRepository.save(new Sequence()).getId()).
                append(retailId).toString();
    }

    public static LocalDateTime atStartOfDay(String date) {
        final LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return localDate.atTime(LocalTime.MIN);
    }

    public static LocalDateTime atEndOfDay(String date) {
        LocalDate localDateTime = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return localDateTime.atTime(LocalTime.MAX);
    }

    public static LocalDate getDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String currentDate() {
        return LocalDate.now().toString();
    }


    public static void main(String[] args) {
        String str = "1400";
        System.out.println(conver12HrsTime(str.substring(0, 2)+":"+str.substring(2, 4)));
        LocalTime lt = LocalTime.now();
        System.out.println(upcomingDrawTime());
        StringBuilder drawTime = new StringBuilder();
        drawTime.append(13).append("00");
        System.out.println(drawTime.toString());

        System.out.println(LocalDateTime.now().toString());

        System.out.println(atStartOfDay("2022-03-07"));
        System.out.println(atEndOfDay("2022-03-07"));
    }

}
