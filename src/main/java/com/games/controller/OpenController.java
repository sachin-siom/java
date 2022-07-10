package com.games.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.payload.DrawOpenResponse;
import com.games.payload.DrawResponse;
import com.games.service.PointPlayService;
import com.sun.xml.bind.marshaller.Messages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.games.util.GameUtil.*;


@Slf4j
@RestController
@RequestMapping("/open")
@CrossOrigin(origins = {"${settings.cors_origin}", "${settings.cors_origin.localhost}"})
public class OpenController {
    @Autowired
    private PointPlayService pointPlayService;

    @Autowired
    private ObjectMapper objectMapper;


    private  Map<String, List<Integer>> getMapStatic() {
        Map<String, List<Integer>> result = new HashMap<>();
        for (int i = 0; i <= 9; i++) {
            int lower = i * 1000;
            int higher = lower + 999;
            for (int low = lower; low < higher; low = low + 100) {
                int high = low + 99;
                result.put(getKey(low, high), null);
            }
        }
        return result;
    }

    private static String getKey(int low, int high) {
        return "_"+low + "_" + high;
    }

    @GetMapping(value = "/winnerListByDate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DrawOpenResponse>> getWinnerListByDate() throws Exception{
        List<DrawResponse> winnerWagerResponse = pointPlayService.getWinnerListByDate(currentDate());
        List<DrawOpenResponse> drawOpenResponses = new ArrayList<>();
        for (DrawResponse response : winnerWagerResponse) {
            Map<String, List<Integer>> winningNumbers = new HashMap<>();
            DrawOpenResponse openResponse= new DrawOpenResponse();
            openResponse.setDrawTime(conver12HrsTime(response.getDrawTime().substring(0, 2)+":"+response.getDrawTime().substring(2, 4)));
            for (Integer num : response.getWinnerNumber()) {
                List<Integer> lowHigh = getLowHigh(num);
                String noOfDigit = checkNoOfDigit(num);
                int low = lowHigh.get(0);
                int high = lowHigh.get(1);
                if(Objects.isNull(winningNumbers.get(getKey(low, high)))) {
                    List<Integer> winner = new ArrayList<>();
                    winner.add(num);
                    winningNumbers.put(getKey(low,high), winner);
                }else{
                    List<Integer> winner = winningNumbers.get(getKey(low, high));
                    winner.add(num);
                    winningNumbers.put(getKey(low,high), winner);
                }
            }
            openResponse.setWinnerNumber(winningNumbers);
            openResponse.setDate(response.getDate());
            drawOpenResponses.add(openResponse);
        }
        return ResponseEntity
                .ok()
                .body(drawOpenResponses);
    }

    @GetMapping(value = "/winnerListByDateNew", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DrawResponse>> getWinnerListByDateNew() {
        List<DrawResponse> winnerWagerResponse = pointPlayService.getWinnerListByDate(currentDate());
        for (DrawResponse response : winnerWagerResponse) {
            response.setDrawTime(conver12HrsTime(response.getDrawTime().substring(0, 2)+":"+response.getDrawTime().substring(2, 4)));
            Collections.sort(response.getWinnerNumber());
        }
        return ResponseEntity
                .ok()
                .body(winnerWagerResponse);
    }

    @GetMapping(value = "/winnerListByDateNew1", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getWinnerListByDateNew1() throws Exception {
        List<Map<String, Object>> response1 = new ArrayList<>();
        List<DrawResponse> winnerWagerResponse = pointPlayService.getWinnerListByDate(currentDate());
        for (DrawResponse res : winnerWagerResponse) {
            Map<String, Object> winningNumbers = new HashMap<>();
            winningNumbers.put("drawTime", conver12HrsTime(res.getDrawTime().substring(0, 2) + ":" + res.getDrawTime().substring(2, 4)));
            for (Integer num : res.getWinnerNumber()) {
                List<Integer> lowHigh = getLowHigh(num);
                int low = lowHigh.get(0);
                int high = lowHigh.get(1);
                if (Objects.isNull(winningNumbers.get(getKey(low, high)))) {
                    List<String> winner = new ArrayList<>();
                    winner.add(appendZero(num));
                    winningNumbers.put(getKey(low, high), winner);
                } else {
                    List<String> winner = (List<String>) winningNumbers.get(getKey(low, high));
                    winner.add(appendZero(num));
                    winningNumbers.put(getKey(low, high), winner);
                }
            }
            winningNumbers.put("date", res.getDate());
            response1.add(winningNumbers);
        }
        return ResponseEntity
                .ok()
                .body(response1);
    }

    private String appendZero(Integer num) {
        return String.valueOf(String.format("%04d", num));
    }


    private String checkNoOfDigit(Integer num) {
        return appendZero(num);
    }
}
