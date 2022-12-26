package com.games.service;

import com.games.model.BetDetail;
import com.games.payload.PointDetails;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
public class PointPlayAlgo1 {

    private final static int MAXIMUM_NUM = 3;

    public Map<String, List<PointDetails>> runNew(Map<Integer, BetDetail> betMap, Double collectionAmt, Double profitPercentage, Set<Integer> includeNumbers, SortedSet<Integer> excludeNumber) {
        if (profitPercentage < 0 || collectionAmt < 0 || profitPercentage > 100) {
            log.error("Invalid Input");
            return null;
        }
        Double profit = collectionAmt * (profitPercentage / 100.0f);
        profitPercentage = 100.0 - profitPercentage;
        Double distributeMoney = collectionAmt * (profitPercentage / 100.0f);
        log.info("includeNumber:{}, betMap:{}",includeNumbers, betMap);
        log.info("Distributing winning prize of worth: {} and profit will be: {}", distributeMoney, profit);
        Map<String, List<PointDetails>> prepareAllRangeBetMap = new HashMap<>();
        List<String> BUCKETS = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            int lower = i * 1000;
            int higher = lower + 999;
            for (int low = lower; low < higher; low = low + 100) { //0 to 99, 100 to 199, 200 to 299
                int high = low + 99;
                BUCKETS.add(getKey(low, high));
                prepareAllNumberByRange(low, high, betMap, prepareAllRangeBetMap, excludeNumber);
            }
        }
        log.info("prepareAllRangeBetMap:{}", prepareAllRangeBetMap);
        Collections.shuffle(BUCKETS);
        Map<String, List<PointDetails>> finalWinnerMap = new HashMap<>();
        distributeMoney = addIncludeNumbers(includeNumbers, betMap, distributeMoney, finalWinnerMap);
        log.info("1.after include number added, distributeMoney:{}, finalWinnerMap:{}", distributeMoney, finalWinnerMap);
        distributeMoney = distributePrizeRandomly(betMap, distributeMoney, finalWinnerMap);
        log.info("2.after prize distribution, distributeMoney:{}, finalWinnerMap:{}", distributeMoney, finalWinnerMap);
        addDummyNumber(distributeMoney, prepareAllRangeBetMap, finalWinnerMap, BUCKETS);
        log.info("3.after dummy number, finalWinnerMap:{}", finalWinnerMap);
        //finalMissedNumberCheck(finalWinnerMap, BUCKETS, betMap);
        log.info("=========================================== winner numbers List & prize amount (verify below: dupplicate, no not generated, etc..)============================================");
        printData(finalWinnerMap);
        return finalWinnerMap;
    }

    private void addDummyNumber(Double distributeMoney,
        Map<String, List<PointDetails>> prepareAllRangeBetMap,
        Map<String, List<PointDetails>> finalWinnerMap, List<String> BUCKETS) {
        for (String highLow : BUCKETS) {
            if (!finalWinnerMap.containsKey(highLow) || Objects.isNull(finalWinnerMap.get(highLow)) || finalWinnerMap.get(highLow).isEmpty()) {
                distributeMoney = deductPrizeMoneyToRandomNum(distributeMoney, highLow,
                    prepareAllRangeBetMap, finalWinnerMap);
            }
        }
        for (String key : finalWinnerMap.keySet()) {
            if (finalWinnerMap.containsKey(key) && finalWinnerMap.get(key) != null && finalWinnerMap.get(key).isEmpty()) {
                log.info("adding dummy number2: {}", key);
                distributeMoney = deductPrizeMoneyToRandomNum(distributeMoney, key,
                    prepareAllRangeBetMap, finalWinnerMap);
            }
        }
    }

    private Double distributePrizeRandomly(Map<Integer, BetDetail> betMap, Double distributeMoney, Map<String, List<PointDetails>> finalWinnerMap) {
        final List<Integer> betNumbers = new ArrayList<>(betMap.keySet());
        final Set<Integer> duplicateRangeNumbers = new HashSet<>();
        Collections.shuffle(betNumbers);
        while(distributeMoney > 0) {
            if(betNumbers.isEmpty())
                break;
            final int indexGenerated = getRandomNum(0, betNumbers.size()-1);
            int selectedWinner = betNumbers.get(indexGenerated);
            int last2Digit = getLast2Digit(selectedWinner);
            if(duplicateRangeNumbers.contains(last2Digit)) {
                betNumbers.remove(indexGenerated);
                continue;
            }
            duplicateRangeNumbers.add(last2Digit);
            int lower = (selectedWinner / 100) * 100;
            int higher = lower + 99;
            if(finalWinnerMap.containsKey(getKey(lower, higher))) {
                betNumbers.remove(indexGenerated);
                continue;
            }
            log.info("lower:{} higher:{}, betSize:{}, indexGenerated:{}, distributeMoney:{}, amount:{} number:{}",lower, higher, betNumbers.size(), indexGenerated, distributeMoney, betMap.get(selectedWinner).getWinningAmt(), selectedWinner);
            final BetDetail betDetail = betMap.get(selectedWinner);
            if(distributeMoney > betDetail.getWinningAmt()) {
                distributeMoney -= betDetail.getWinningAmt();
                finalWinnerMap.put(getKey(lower, higher), Arrays.asList(new PointDetails(selectedWinner, betDetail.getWinningAmt())));
            }
            betNumbers.remove(indexGenerated);
        }
        return distributeMoney;
    }

    private int getLast2Digit(int selectedWinner) {
        return selectedWinner % 100;
    }

    private double addIncludeNumbers(Set<Integer> includeNumber, Map<Integer, BetDetail> betMap, Double moneyNeedsTobeDistributed, Map<String, List<PointDetails>> finalWinnerMap) {
        if (Objects.isNull(includeNumber) || includeNumber.isEmpty()) {
            return moneyNeedsTobeDistributed;
        }

        for (Integer num : includeNumber) {
            double winningPrize = 0.0;
            if (betMap.containsKey(num)) {
                winningPrize = betMap.get(num).getWinningAmt();
                moneyNeedsTobeDistributed -= winningPrize;
            }
            int lower = (num / 100) * 100;
            int higher = lower + 99;
            if(finalWinnerMap.containsKey(getKey(lower, higher)))
                finalWinnerMap.get(getKey(lower, higher)).add(new PointDetails(num, winningPrize));
            else{
                List<PointDetails> pointDetails = new ArrayList<>(3);
                pointDetails.add(new PointDetails(num, winningPrize));
                finalWinnerMap.put(getKey(lower, higher), pointDetails);
            }
        }
        return moneyNeedsTobeDistributed;
    }


    private void finalMissedNumberCheck(Map<String, List<PointDetails>> finalWinnerMap, List<String> BUCKET, Map<Integer, BetDetail> betMap) {
        for (String highLow : BUCKET) {
            if (!finalWinnerMap.containsKey(highLow) || Objects.isNull(finalWinnerMap.get(highLow)) || finalWinnerMap.get(highLow).isEmpty()) {
                int lower = Integer.parseInt(highLow.split("-")[0]);
                int higher = Integer.parseInt(highLow.split("-")[1]);
                int num = getRandomNum(lower, higher);
                double value = 0.0;
                if(betMap.containsKey(num)) {
                    value = betMap.get(num).getWinningAmt();
                }
                log.info("adding missed number: {}",num);
                finalWinnerMap.put(highLow, Arrays.asList(new PointDetails(num, value)));
            }
        }
    }

    private void printData(Map<String, List<PointDetails>> finalWinnerMap) {
        Map<String, List<PointDetails>> seq = new TreeMap<>(finalWinnerMap);
        for (String s : seq.keySet()) {
            log.info("{} => {}", s, finalWinnerMap.get(s), (finalWinnerMap.get(s).stream().map(num -> num.getWinningPrize()).reduce(Double::sum)).get());
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
        log.info("adding dumy winner range:{} number:{}", key, dummyWinnerBet);
        finalWinnerMap.put(key, dummyWinnerBet);
        return moneyNeedsTobeDistributed;
    }

    private void prepareAllNumberByRange(int lower, int higher, Map<Integer, BetDetail> betMap, Map<String, List<PointDetails>> prepareAllRangeBetMap,
        SortedSet<Integer> excludeNumber) {
        Set<Integer> betNumbersByUserWithinRange = betMap.keySet().stream().filter(num -> (num >= lower && num <= higher)).collect(Collectors.toSet());
        if(Objects.nonNull(excludeNumber) && !excludeNumber.isEmpty()){
            betNumbersByUserWithinRange.removeAll(excludeNumber);
        }
        List<PointDetails> list = new ArrayList<>();
        if (Objects.nonNull(betNumbersByUserWithinRange) && !betNumbersByUserWithinRange.isEmpty()) {
            for (Integer num : betNumbersByUserWithinRange) {
                list.add(new PointDetails(num, betMap.get(num).getWinningAmt()));
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

    private int probablityRandomGeneration() {
        int numbers = (int) (Math.random() * (100 - 1)) + 1;
        int prob = (int) (Math.random() * (5 - 1)) + 1;
        if (numbers > 0 && numbers < prob) {
            return 2;
        } else {
            return 1;
        }
    }

    public static void main(String[] args) {
        PointPlayAlgo1 algo = new PointPlayAlgo1();
        Map<Integer, BetDetail> betMap = new TreeMap<>();
        for (int i = 0; i < 100; i++) {
            List<Integer> winningMultiplier = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
            final double winningAmt = winningMultiplier.get(algo.getRandomNum(0, 9)) * 180.0;
            betMap.put(Integer.parseInt(String.valueOf(i)+"71"), BetDetail.builder().winningAmt(winningAmt).build());
        }
        betMap.put(7, BetDetail.builder().winningAmt(19.0).retailId(new HashSet<>(Arrays.asList("1"))).ticketId(new HashSet<>(Arrays.asList("abc"))).build());
        betMap.put(1, BetDetail.builder().winningAmt(10.0).retailId(new HashSet<>(Arrays.asList("1"))).ticketId(new HashSet<>(Arrays.asList("abc"))).build());
        betMap.put(2, BetDetail.builder().winningAmt(5.0).build());
        betMap.put(573, BetDetail.builder().winningAmt(231.0).build());

        betMap.put(1219, BetDetail.builder().winningAmt(42.0).build());
        betMap.put(1103, BetDetail.builder().winningAmt(879.00).build());

        betMap.put(2219, BetDetail.builder().winningAmt(26.0).build());
        betMap.put(2131, BetDetail.builder().winningAmt(1712.0).build());

        betMap.put(3489, BetDetail.builder().winningAmt(123.0).build());
        betMap.put(3911, BetDetail.builder().winningAmt(111.0).build());

        betMap.put(4143, BetDetail.builder().winningAmt(2023.0).build());
        betMap.put(4401, BetDetail.builder().winningAmt(1200.0).build());

        betMap.put(5543, BetDetail.builder().winningAmt(25.0).build());
        betMap.put(5410, BetDetail.builder().winningAmt(100.0).build());

        betMap.put(6450, BetDetail.builder().winningAmt(27.0).build());
        betMap.put(6101, BetDetail.builder().winningAmt(100.0).build());

        betMap.put(7592, BetDetail.builder().winningAmt(70.0).build());
        betMap.put(7893, BetDetail.builder().winningAmt(789.0).build());

        betMap.put(8313, BetDetail.builder().winningAmt(328.0).build());
        betMap.put(8432, BetDetail.builder().winningAmt(1092.0).build());

        betMap.put(9782, BetDetail.builder().winningAmt(921.0).build());
        betMap.put(9742, BetDetail.builder().winningAmt(72.0).build());

        Double profitPercentage = 20.0;
        Double collectionAmt = 10000.0;
        Set<Integer> includeNumber = new HashSet<>();
        includeNumber.add(10);
        /*includeNumber.add(11);
        includeNumber.add(12);
        includeNumber.add(13);
        includeNumber.add(14);*/
        SortedSet<Integer> excludeNumber = new TreeSet<>();
        //excludeNumber.addAll(IntStream.rangeClosed(0, 96).boxed().collect(Collectors.toSet()));

        algo.runNew(betMap, collectionAmt, profitPercentage, includeNumber, excludeNumber);
    }
}
