package com.games.service;

import com.games.model.BetDetail;
import com.games.model.TicketDetails;
import com.games.payload.PointDetails;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public class PointPlayAlgo2 {

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

    public Map<String, List<PointDetails>> run(Map<Integer, BetDetail> betMap, Double collectionAmt, Double profitPercentage, Set<Integer> includeNumbers, SortedSet<Integer> excludeNumber,
        List<TicketDetails> ticketDetails) {
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
                prepareAllNumberByRange(low, high, betMap, prepareAllRangeBetMap, excludeNumber);
            }
        }
        //Collections.shuffle(NUMBERS);
        Map<String, List<PointDetails>> finalWinnerMap = new HashMap<>();
        log.info("includeNumber:{}, ",includeNumbers);
        distributeMoney = addIncludeNumbers(includeNumbers, betMap, distributeMoney, finalWinnerMap);
        HashMap<ValueRange, ValueRange> rangeMapForRun = new LinkedHashMap<>(rangesMap);
        final Map<ValueRange, List<TicketDetails>> ticketByRange = sortTicketByRange(ticketDetails, rangeMapForRun);


        log.info("1.after include number added, distributeMoney:{}, finalWinnerMap:{}", distributeMoney, finalWinnerMap);
        distributeMoney = distributePrizeRandomly(betMap, distributeMoney, finalWinnerMap, ticketByRange, rangeMapForRun);
        log.info("2.after prize distribution, distributeMoney:{}, finalWinnerMap:{}", distributeMoney, finalWinnerMap);
        addDummyNumber(distributeMoney, prepareAllRangeBetMap, finalWinnerMap);
        log.info("3.after dummy number, finalWinnerMap:{}", finalWinnerMap);
        finalMissedNumberCheck(finalWinnerMap, NUMBERS, betMap);
        log.info("--------------- winner List num & winner prize ---------------");
        printData(finalWinnerMap);
        return finalWinnerMap;
    }

    private void finalMissedNumberCheck(Map<String, List<PointDetails>> finalWinnerMap, List<String> BUCKET, Map<Integer, BetDetail> betMap) {
        for (String highLow : BUCKET) {
            if (!finalWinnerMap.containsKey(highLow) || Objects.isNull(finalWinnerMap.get(highLow)) || finalWinnerMap.get(highLow).isEmpty()) {
                int lower = Integer.parseInt(highLow.split("-")[0]);
                int higher = Integer.parseInt(highLow.split("-")[1]);
                int num = getRandomNum(lower, higher).intValue();
                double value = 0.0;
                if(betMap.containsKey(num)) {
                    value = betMap.get(num).getWinningAmt();
                }
                finalWinnerMap.put(highLow, Arrays.asList(new PointDetails(num, value)));
            }
        }
    }

    private void addDummyNumber(Double distributeMoney,
        Map<String, List<PointDetails>> prepareAllRangeBetMap,
        Map<String, List<PointDetails>> finalWinnerMap) {
        for (String key : finalWinnerMap.keySet()) {
            if (finalWinnerMap.containsKey(key) && finalWinnerMap.get(key) != null && finalWinnerMap.get(key).isEmpty()) {
                distributeMoney = deductPrizeMoneyToRandomNum(distributeMoney, key,
                    prepareAllRangeBetMap, finalWinnerMap);
            }
        }
    }
    private Double distributePrizeRandomly(Map<Integer, BetDetail> betMap, Double distributeMoney, Map<String, List<PointDetails>> finalWinnerMap,
        Map<ValueRange, List<TicketDetails>> ticketByRange, HashMap<ValueRange,ValueRange> rangeMapForRun) {
        final List<Integer> betNumbers = new ArrayList<>(betMap.keySet());
        Collections.shuffle(betNumbers);
        while (distributeMoney > 0 && (!ticketByRange.isEmpty())) {
            if (betNumbers.isEmpty()){
                break;
            }
            final int indexGenerated = getRandomNum(0, betNumbers.size() - 1).intValue();
            int selectedWinner = selectWinnerNumber(distributeMoney, ticketByRange, rangeMapForRun);
            if(selectedWinner == -1)
                continue;
            int lower = (selectedWinner / 100) * 100;
            int higher = lower + 99;
            if (finalWinnerMap.containsKey(getKey(lower, higher))){
                betNumbers.remove(indexGenerated);
                continue;
            }
            log.info(
                "lower:{} higher:{}, betSize:{}, indexGenerated:{}, distributeMoney:{}, amount:{} number:{}",
                lower, higher, betNumbers.size(), indexGenerated, distributeMoney,
                betMap.get(selectedWinner).getWinningAmt(), selectedWinner);
            final BetDetail betDetail = betMap.get(selectedWinner);
            if (distributeMoney > betDetail.getWinningAmt()){
                distributeMoney -= betDetail.getWinningAmt();
                finalWinnerMap.put(getKey(lower, higher),
                    Arrays.asList(new PointDetails(selectedWinner, betDetail.getWinningAmt())));
            }
            betNumbers.remove(indexGenerated);
        }
        return distributeMoney;
    }

    private int selectWinnerNumber(Double distributeMoney, Map<ValueRange, List<TicketDetails>> ticketByRange, HashMap<ValueRange,ValueRange> rangeMap) {
        log.info("ticket by range: {}",ticketByRange);
        final ValueRange rangeSelected = selectRange(rangeMap, ticketByRange);
        final List<TicketDetails> ticketDetails = ticketByRange.getOrDefault(rangeSelected, null);
        if(Objects.isNull(ticketDetails))
            return -1;
        for (int i = 0; i < ticketDetails.size(); i++) {
            final Map<Integer, Double> betNumbers = ticketDetails.get(i).getBetNumber();
            int winnerNum = selectNumberForTicket(distributeMoney, betNumbers);
            if(winnerNum == -1) {
                if(ticketDetails.size() == 1) {
                    ticketByRange.remove(rangeSelected);
                } else {
                    ticketDetails.remove(i);
                }
            } else {
                betNumbers.remove(winnerNum);
                return winnerNum;
            }
        }
        return -1;
    }

    private int selectNumberForTicket(Double distributeMoney, Map<Integer, Double> betNumbers) {
        for (Integer betNumber : betNumbers.keySet()) {
            if(betNumbers.get(betNumber) < distributeMoney) {
                return betNumber;
            }
        }
        return -1;
    }

    private ValueRange selectRange(HashMap<ValueRange, ValueRange> rangesMap, Map<ValueRange, List<TicketDetails>> ticketByRange) {
        final long number = getRandomNum(0, 100).longValue();
        return rangesMap.keySet().stream()
            .filter(range -> range.isValidValue(number)).findFirst().orElse(ticketByRange.keySet().stream().findFirst().get());
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
                int num = getRandomNum(lower, higher).intValue();
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
            return getRandomNum(lower, higher).intValue();
        }
    }

    private String getKey(int lower, int higher) {
        return String.valueOf(lower) + "-" + String.valueOf(higher);
    }

    private Double getRandomNum(int lower, int higher) {
        return ((Math.random() * (higher - lower)) + lower);
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

    static List<ValueRange> ranges = Arrays.asList(ValueRange.of(0, 100),ValueRange.of(101, 200),ValueRange.of(201, 300),ValueRange.of(301, 400),ValueRange.of(401, 500),
        ValueRange.of(501, 1000),ValueRange.of(1001, 1500),ValueRange.of(1501, 2000),ValueRange.of(2000, 3000),ValueRange.of(3001, Integer.MAX_VALUE));

    static HashMap<ValueRange,ValueRange> rangesMap = new LinkedHashMap(){{
        put(ValueRange.of(0, 5), ranges.get(0));
        put(ValueRange.of(6, 10), ranges.get(1));
        put(ValueRange.of(11, 15), ranges.get(2));
        put(ValueRange.of(16, 20), ranges.get(3));
        put(ValueRange.of(21, 25), ranges.get(4));
        put(ValueRange.of(26, 40), ranges.get(5));
        put(ValueRange.of(41, 50), ranges.get(6));
        put(ValueRange.of(51, 70), ranges.get(7));
        put(ValueRange.of(71, 90), ranges.get(8));
        put(ValueRange.of(91, 100), ranges.get(9));
    }};

    static HashMap<ValueRange,ValueRange> rangesMapReverse = new LinkedHashMap(){{
        put(ranges.get(0), ValueRange.of(0, 5));
        put(ranges.get(1), ValueRange.of(6, 10));
        put(ranges.get(2), ValueRange.of(11, 15));
        put(ranges.get(3), ValueRange.of(16, 20));
        put(ranges.get(4), ValueRange.of(21, 25));
        put(ranges.get(5), ValueRange.of(26, 40));
        put(ranges.get(6), ValueRange.of(41, 50));
        put(ranges.get(7), ValueRange.of(51, 70));
        put(ranges.get(8), ValueRange.of(71, 90));
        put(ranges.get(9), ValueRange.of(91, 100));
    }};

    public Map<ValueRange, List<TicketDetails>> sortTicketByRange(List<TicketDetails> ticketDetails, HashMap<ValueRange, ValueRange> rangeMapForRun) {
        Map<ValueRange, List<TicketDetails>> prepareMap = new HashMap<>();
        for (TicketDetails detail : ticketDetails) {
            ValueRange valueRange = ranges.stream()
                .filter(val -> val.isValidValue((int) detail.getTotalWinningAmount())).findFirst().get();
            if(prepareMap.containsKey(rangesMapReverse.get(valueRange))) {
                prepareMap.get(rangesMapReverse.get(valueRange)).add(detail);
            } else {
                List<TicketDetails> list = new ArrayList<>();
                list.add(detail);
                prepareMap.put(rangesMapReverse.get(valueRange), list);
            }
        }
        rangeMapForRun.keySet().retainAll(prepareMap.keySet());
        return prepareMap;
    }

    ValueRange merge2ValueRange(ValueRange range1, ValueRange range2) {
        return ValueRange.of(range1.getMinimum(), range2.getMaximum());
    }

    public static void main(String[] args) {
        PointPlayAlgo2 algo = new PointPlayAlgo2();
        Map<Integer, BetDetail> betMap = new TreeMap<>();
        List<TicketDetails> input = new ArrayList<>();
        List<String> retailId = Arrays.asList("a","b","c");
        List<Integer> winningMultiplier = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
        double winingAmount = 180.0;
        for (int i = 0; i < 10; i++) {
            final double winningAmt = winningMultiplier.get(algo.getRandomNum(0, 9).intValue()) * winingAmount;
            final int number = algo.getRandomNum(0, 10000).intValue();
            final String retail = retailId.get(algo.getRandomNum(0, 2).intValue());
            Map<Integer, Double> betNumbers = new HashMap<>();
            betNumbers.put(number, winningAmt);
            input.add(TicketDetails.builder().betNumber(betNumbers).retailId(retail).totalWinningAmount(winningAmt).build());
            if(betMap.containsKey(number)) {
                betMap.put(number, BetDetail.builder().winningAmt((betMap.get(number).getWinningAmt() + winningAmt)).build());
            } else {
                betMap.put(number, BetDetail.builder().winningAmt(winningAmt).build());
            }
        }
        HashMap<ValueRange, ValueRange> rangeMapForRun = new LinkedHashMap<>(rangesMap);
        System.out.println(rangeMapForRun);
        final Map<ValueRange, List<TicketDetails>> valueRangeListMap = algo.sortTicketByRange(input,
            rangeMapForRun);
        System.out.println(rangeMapForRun);
        System.out.println(input);
        System.out.println(valueRangeListMap);
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
        algo.run(betMap, collectionAmt, profitPercentage, includeNumber, excludeNumber, input);
    }
}
