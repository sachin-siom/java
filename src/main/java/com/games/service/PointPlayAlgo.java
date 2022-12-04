package com.games.service;

import com.games.payload.PointDetails;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PointPlayAlgo {

    private final static int MAXIMUM_NUM = 3;

    private int probablityRandomGeneration() {
        int numbers = (int) (Math.random() * (100 - 1)) + 1;
        int prob = (int) (Math.random() * (5 - 1)) + 1;
        if (numbers > 0 && numbers < prob) {
            return 2;
        } else {
            return 1;
        }
    }

    public Map<String, List<PointDetails>> run(Map<Integer, Double> betMap, Double collectionAmt, Double profitPercentage, Set<Integer> includeNumbers, SortedSet<Integer> excludeNumber) {
        if (profitPercentage < 0 || collectionAmt < 0 || profitPercentage > 100) {
            log.error("Invalid Input");
            return null;
        }
        Double profit = collectionAmt * (profitPercentage / 100.0f);
        profitPercentage = 100.0 - profitPercentage;
        Double distributeMoney = collectionAmt * (profitPercentage / 100.0f);
        log.info("Distributing winning prize of worth: {} and profit will be: {}", distributeMoney, profit);
        Map<String, List<PointDetails>> prepareAllRangeBetMap = new HashMap<>();
        List<String> NUMBERS = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            int lower = i * 1000;
            int higher = lower + 999;
            for (int low = lower; low < higher; low = low + 100) {
                int high = low + 99;
                NUMBERS.add(getKey(low, high));
                prepareNumberByRange(low, high, betMap, prepareAllRangeBetMap, excludeNumber);
            }
        }
        Collections.shuffle(NUMBERS);
        Map<String, List<PointDetails>> finalWinnerMap = new HashMap<>();
        log.info("includeNumber:{}, ",includeNumbers);
        for (String highLow : NUMBERS) {
            distributeMoney = distributePrizeRandomly(includeNumbers, betMap, distributeMoney, finalWinnerMap, highLow, prepareAllRangeBetMap);

        }
        // add missed number but non-winner
        log.info("2.includeNumber:{}, betMap:{}, prepareAllRangeBetMap:{}, finalWinnerMap:{}",includeNumbers, betMap, prepareAllRangeBetMap, finalWinnerMap);
        for (String key : finalWinnerMap.keySet()) {
            if (finalWinnerMap.containsKey(key) && finalWinnerMap.get(key) != null && finalWinnerMap.get(key).isEmpty()) {
                distributeMoney = deductPrizeMoneyToRandomNum(distributeMoney, key, prepareAllRangeBetMap, finalWinnerMap);
            }
        }
        log.info("3. finalWinnerMap:{}", finalWinnerMap);
        // add missed number doesnot care about distribute money variable
        missedNum(finalWinnerMap, NUMBERS, betMap);
        //log.info("4. finalWinnerMap:{}", finalWinnerMap);
        log.info("--------------- winner List num & winner prize ---------------");
        printData(finalWinnerMap);
        return finalWinnerMap;
    }

    private double distributePrizeRandomly(Set<Integer> includeNumber, Map<Integer, Double> betMap, Double moneyNeedsTobeDistributed, Map<String, List<PointDetails>> finalWinnerMap, String key, Map<String, List<PointDetails>> prepareAllRangeBetMap) {
        log.info("moneyNeedsTobeDistributed:{}, key: {} prepareAllRangeBetMap for key:{}",moneyNeedsTobeDistributed, key, prepareAllRangeBetMap.get(key));
        if (Objects.isNull(includeNumber) || includeNumber.isEmpty()) {
            moneyNeedsTobeDistributed = distributePrizeMoney(key, moneyNeedsTobeDistributed, prepareAllRangeBetMap, finalWinnerMap);
            return moneyNeedsTobeDistributed;
        }
        int lower = Integer.parseInt(key.split("-")[0]);
        int higher = Integer.parseInt(key.split("-")[1]);
        Set<Integer> incldNum = includeNumber.stream().filter(num -> (num >= lower && num <= higher)).collect(Collectors.toSet());
        if (Objects.isNull(incldNum) || incldNum.isEmpty()) {
            moneyNeedsTobeDistributed = distributePrizeMoney(key, moneyNeedsTobeDistributed, prepareAllRangeBetMap, finalWinnerMap);
            return moneyNeedsTobeDistributed;
        }
        List<PointDetails> pntDetails = new ArrayList<>();
        for (Integer num : incldNum) {
            double winningPrize = 0.0;
            if (betMap.containsKey(num)) {
                winningPrize = betMap.get(num);
                moneyNeedsTobeDistributed -= winningPrize;
            }
            pntDetails.add(new PointDetails(num, winningPrize));
        }
        finalWinnerMap.put(key, pntDetails);
        return moneyNeedsTobeDistributed;
    }

    private void missedNum(Map<String, List<PointDetails>> finalWinnerMap, List<String> NUMBERS, Map<Integer, Double> betMap) {
        for (String highLow : NUMBERS) {
            if (!finalWinnerMap.containsKey(highLow) || Objects.isNull(finalWinnerMap.get(highLow)) || finalWinnerMap.get(highLow).isEmpty()) {
                int lower = Integer.parseInt(highLow.split("-")[0]);
                int higher = Integer.parseInt(highLow.split("-")[1]);
                int num = getRandomNum(lower, higher);
                finalWinnerMap.put(highLow, Arrays.asList(new PointDetails(num, betMap.getOrDefault(num, 0.0))));
            }
        }
    }

    private void printData(Map<String, List<PointDetails>> finalWinnerMap) {
        Map<String, List<PointDetails>> seq = new TreeMap<>(finalWinnerMap);
        for (String s : seq.keySet()) {
            log.info("{} --> {} --> ", s, finalWinnerMap.get(s), (finalWinnerMap.get(s).stream().map(num -> num.getWinningPrize()).reduce(Double::sum)).get());
        }
    }

    private Double deductPrizeMoneyToRandomNum(Double moneyNeedsTobeDistributed, String key, Map<String, List<PointDetails>> preparedBetMap, Map<String, List<PointDetails>> finalWinnerMap) {
        int numbers = probablityRandomGeneration();
        List<PointDetails> betList = preparedBetMap.get(key);
        Set<Integer> nums = betList.stream().map(bet -> bet.getNum()).collect(Collectors.toSet());
        int lower = Integer.parseInt(key.split("-")[0]);
        int higher = Integer.parseInt(key.split("-")[1]);
        List<Integer> dummyWinner = new ArrayList<>();
        for (int i = lower; i < higher; i++) {
            if (!nums.contains(i)) {
                dummyWinner.add(i);
            }
        }
        Collections.shuffle(dummyWinner);
        List<PointDetails> dummyWinnerBet = new ArrayList<>();
        for (int dummy = 0; dummy < dummyWinner.size() && dummyWinnerBet.size() < numbers; dummy++) {
            dummyWinnerBet.add(new PointDetails(dummyWinner.get(dummy), 0.0));
        }
        finalWinnerMap.put(key, dummyWinnerBet);
        return moneyNeedsTobeDistributed;
    }

    private Double distributePrizeMoney(String key, Double moneyNeedsTobeDistributed, Map<String, List<PointDetails>> prepareAllRangeBetMap, Map<String, List<PointDetails>> finalWinnerMap) {
        int numbers = probablityRandomGeneration();
        List<PointDetails> betList = prepareAllRangeBetMap.get(key);
        List<PointDetails> winnerList = new ArrayList<>(2);
        if (Objects.isNull(betList) || betList.isEmpty()) {
            return moneyNeedsTobeDistributed;
        }
        Iterator<PointDetails> itr = betList.iterator();
        while (itr.hasNext() && winnerList.size() < numbers) {
            PointDetails winnerNum = itr.next();
            if (moneyNeedsTobeDistributed > winnerNum.getWinningPrize()) {
                moneyNeedsTobeDistributed -= winnerNum.getWinningPrize();
                winnerList.add(winnerNum);
                itr.remove();
            }
        }
        log.info("betList: {} winnerList:{}, key:{}",betList, winnerList, key);
        finalWinnerMap.put(key, winnerList);
        return moneyNeedsTobeDistributed;
    }

    private void prepareNumberByRange(int lower, int higher, Map<Integer, Double> betMap, Map<String, List<PointDetails>> prepareAllRangeBetMap,
                                      SortedSet<Integer> excludeNumber) {
        Set<Integer> betNumbersByUserWithinRange = betMap.keySet().stream().filter(num -> (num >= lower && num <= higher)).collect(Collectors.toSet());
        if(Objects.nonNull(excludeNumber) && !excludeNumber.isEmpty()){
            betNumbersByUserWithinRange.removeAll(excludeNumber);
        }
        List<PointDetails> list = new ArrayList<>();
        if (Objects.nonNull(betNumbersByUserWithinRange) && !betNumbersByUserWithinRange.isEmpty()) {
            for (Integer num : betNumbersByUserWithinRange) {
                list.add(new PointDetails(num, betMap.get(num)));
            }
            Collections.shuffle(list);
        } else {
            getRandomDummyWinner(lower, higher, list, excludeNumber);
        }
        prepareAllRangeBetMap.put(getKey(lower, higher), list);
    }

    private void getRandomDummyWinner(int lower, int higher, List<PointDetails> list, SortedSet<Integer> excludeNumber) {
        int numbers = probablityRandomGeneration();
        Set<Integer> rndNum = new HashSet<>();
        int num = getExcludedNumber(lower, higher, excludeNumber);
        rndNum.add(num);
        while (rndNum.size() < numbers) {
            int newNum = getExcludedNumber(lower, higher, excludeNumber);
            if (!rndNum.contains(newNum)) {
                rndNum.add(newNum);
            }
        }
        for (Integer intr : rndNum) {
            list.add(new PointDetails(intr, 0.0));
        }
    }

    private int getExcludedNumber(int lower, int higher, SortedSet<Integer> excludeNumber) {
        if(Objects.nonNull(excludeNumber) && !excludeNumber.isEmpty()){
            return getRandomWithExclusion(lower, higher, excludeNumber);
        } else {
            return getRandomNum(lower, higher);
        }
    }

    private String getKey(int lower, int higher) {
        return String.valueOf(lower) + "-" + String.valueOf(higher);
    }

    private int getRandomNum(int lower, int higher) {
        return (int) ((Math.random() * (higher - lower)) + lower);
    }

    Random rnd = new Random();
    public int getRandomWithExclusion(int start, int end, SortedSet<Integer> exclude) {
        int random = start + rnd.nextInt(end - start + 1 - exclude.size());
        for (int ex : exclude) {
            if (random < ex) {
                break;
            }
            random++;
        }
        return random;
    }

    public static void main(String[] args) {

        System.out.println(LocalDate.now());
        System.exit(0);
        Map<Integer, Double> betMap = new TreeMap<>();
        for (int i = 0; i < 100; i++) {
            betMap.put(i, 2000.0);
        }
        betMap.put(7, 19.0);
        betMap.put(1, 10.0);
        betMap.put(2, 5.0);
        //betMap.put(10, 0.0);
        betMap.put(573, 231.0);

        betMap.put(1219, 42.0);
        betMap.put(1103, 879.00);

        betMap.put(2219, 26.0);
        betMap.put(2131, 1712.0);

        betMap.put(3489, 123.0);
        betMap.put(3911, 111.0);

        betMap.put(4143, 2023.0);
        betMap.put(4401, 1200.0);

        betMap.put(5543, 25.0);
        betMap.put(5410, 100.0);

        betMap.put(6450, 27.0);
        betMap.put(6101, 100.0);

        betMap.put(7592, 70.0);
        betMap.put(7893, 789.0);

        betMap.put(8313, 328.0);
        betMap.put(8432, 1092.0);

        betMap.put(9782, 921.0);
        betMap.put(9742, 72.0);

        Double profitPercentage = 20.0;
        Double collectionAmt = 10000.0;
        Set<Integer> includeNumber = new HashSet<>();
        /*includeNumber.add(10);
        includeNumber.add(11);
        includeNumber.add(12);
        includeNumber.add(13);
        includeNumber.add(14);*/
        //includeNumber.add(2131);
        SortedSet<Integer> excludeNumber = new TreeSet<>();
        //excludeNumber.addAll(IntStream.rangeClosed(0, 96).boxed().collect(Collectors.toSet()));
        System.out.println(excludeNumber);
        PointPlayAlgo algo = new PointPlayAlgo();
        algo.run(betMap, collectionAmt, profitPercentage, includeNumber, excludeNumber);
    }
}
