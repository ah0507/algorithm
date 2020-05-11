package net.chensee.task;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.chensee.common.Constants;
import net.chensee.common.ConvertUtil;
import net.chensee.common.DateUtil;
import net.chensee.dao.ehcache.CarTrailEhCache;
import net.chensee.dao.ehcache.LineStationEhCache;
import net.chensee.dao.ehcache.RecentStationRageEhCache;
import net.chensee.dao.ehcache.StationDirectionEhcache;
import net.chensee.dao.mongo.MongoDao;
import net.chensee.entity.po.*;
import net.chensee.entity.po.history.HistoryCard;
import net.chensee.entity.vo.CarTrail;
import net.chensee.entity.vo.EachRecentStationRange;
import net.chensee.enums.OffStationHandleTypeEnum;

import java.util.*;

import static net.chensee.common.Constants.configProperties;
import static net.chensee.common.Constants.queryTime;

@Data
@Slf4j
public class CalculateOffStationTask extends AbstractTask {

    private PersonPayRecord personPayRecord;
    private MongoDao mongoDao;
    private CarTrailEhCache carTrailEhCache;
    private RecentStationRageEhCache recentStationRageEhCache;
    private StationDirectionEhcache stationDirectionEhcache;
    private LineStationEhCache lineStationEhCache;

    private int noFindBusNoCardCount = 0;
    //一个用户上一次车刷了多次卡的数量
    private int sameCardCount = 0;
    private int noRecentStationRangeCount = 0;
    private int chanceCardsCount = 0;
    private int getCardsByChanceCount = 0;
    private int endStationCardsCount = 0;
    private int noFindStationDirectionCount = 0;

    public CalculateOffStationTask(PersonPayRecord personPayRecord) {
        this.personPayRecord = personPayRecord;
    }

    @Override
    public void init() {
    }

    @Override
    public void finish() {
    }

    @Override
    public void execute() {
        if (personPayRecord != null) {
            this.calculate();
        }
    }

    private void calculate() {
        List<Card> cardList = personPayRecord.getCardList();
        List<Card> cards = this.handleCardListOrderAsc(cardList);
        List<Card> getOffStationCards = this.handleCurrentUserCardInfo(cards);
        if (cards.size() == 0) {
            return;
        }
        PersonPayRecordInfo ppr = new PersonPayRecordInfo();
        ppr.setUserId(personPayRecord.getUserId());
        ppr.setCardList(cards);
        mongoDao.addPersonPayRecordInfo(ppr);
        UserStatistics userStatistics = new UserStatistics();
        userStatistics.setUserId(personPayRecord.getUserId());
        userStatistics.setAllCardsCount(cards.size());
        userStatistics.setCompleteCardsCount(getOffStationCards.size());
        mongoDao.addUserStatistics(userStatistics);
    }

    private List<Card> handleCardListOrderAsc(List<Card> cardList) {
        //冒泡
        for (int i = 0; i < cardList.size(); i++) {
            for (int j = i; j < cardList.size(); j++) {
                Card cardi = cardList.get(i);
                Card cardj = cardList.get(j);
                if (cardj.getPayTime().getTime() < cardi.getPayTime().getTime()) {
                    cardList.set(i, cardj);
                    cardList.set(j, cardi);
                }
            }
        }
        return cardList;
    }

    private List<Card> handleCurrentUserCardInfo(List<Card> cardList) {
        List<Card> getOffStationCards = new ArrayList<>();
        for (int i = 0; i < cardList.size(); i++) {
            Card card = cardList.get(i);
            // 过滤查询到的消费数据不是当天的数据（少量数据由于操作不当导致，不能进行计算）
            boolean isCorrect = this.filterCard(card);
            LineStation lineStation = lineStationEhCache.get(card.getLineNo() + "," + card.getDirection());
            if (!isCorrect || lineStation == null) {
                continue;
            }
            //TODO
            if (lineStation.getEndStation() - card.getOnStationNo() == 1) {
                endStationCardsCount++;
                //如果本次出行上车是终点站前一站，则下车点为终点站
                card.setOffStationNo(lineStation.getEndStation());
                card.setOffStationName(lineStation.getEndStationName());
                card.setOffStationUniqueKey(card.getLineNo() + "," + lineStation.getEndStation() + "," + card.getDirection());
                card.setOffStationHandleType(OffStationHandleTypeEnum.ACCORIDING_ENDSTATION.getValue());
                //目前暂无终点站的车辆轨迹信息，通过阈值设置重点站时间
                Date offTime = DateUtil.setDateSecond(card.getPayTime(), configProperties.getEndStationTimeValue());
                card.setOffTime(offTime);
                getOffStationCards.add(card);
            } else {
                if (i != cardList.size() - 1) {
                    Card nextCard = cardList.get(i + 1);
                    if (card.getOnStationUniqueKey().equals(nextCard.getOnStationUniqueKey())) {
                        //该用户上一次车刷了好几次卡
                        sameCardCount++;
                    } else {
                        //通过占据表计算下车点和下车时间
                        this.getOffStationInfo(card, nextCard, getOffStationCards, cardList, i);
                    }
                } else {
                    //该用户只消费一次或者消费到最后一次卡算概率
                    this.handleOffStationInfoByChance(card, getOffStationCards, cardList, i);
                }
            }
        }
        return getOffStationCards;
    }

    private boolean filterCard(Card card) {
        Date nyr00Date = DateUtil.getNYRDateByStr(queryTime);
        Date nyr59Date = DateUtil.setDateDay(nyr00Date,1);
        return card.getPayTime().getTime() > nyr00Date.getTime() && card.getPayTime().getTime() < nyr59Date.getTime() ? true : false;
    }

    private void handleOffStationInfoByChance(Card card, List<Card> getOffStationCards, List<Card> cardList, int currentI) {
        chanceCardsCount++;
        String direction = this.changeDirection(card.getDirection());
        if (direction != null) {
            // TODO
            // 取当天时间之前的消费数据做概率
            Date nowDate = DateUtil.getNYRDateByStr(Constants.queryTime);
            int regularTravelTimeValue = configProperties.getRegularTravelTimeValue();
            Date d = DateUtil.setDateDay(nowDate, -regularTravelTimeValue);
            Date beforeDate = DateUtil.getNYRDate(d);
            List<HistoryCard> historyCards = mongoDao.getHistoryCardsByBusInfo(card.getUserId(), beforeDate, nowDate, card.getLineNo(), direction);
            List<Card> cards = ConvertUtil.convertToCard(historyCards);
            boolean isGetCardOffInfo = this.getCurrentCardOffStationInfo(cards, card, getOffStationCards);
            if (isGetCardOffInfo) {
                this.handleSameCard(getOffStationCards, card, cardList, currentI);
            } else {
                sameCardCount = 0;
            }
        }
    }

    private String changeDirection(String direction) {
        String dir = null;
        switch (direction) {
            case "上行":
                dir = "下行";
                break;
            case "下行":
                dir = "上行";
                break;
            default:
                break;
        }
        return dir;
    }


    private void handleSameCard(List<Card> getOffStationCards, Card card, List<Card> cardList, int currentI) {
        if (sameCardCount > 0) {
            for (int j = currentI - 1; j >= 0; j--) {
                if (sameCardCount == 0) {
                    break;
                }
                Card sameCard = cardList.get(j);
                this.addGetOffStationCards(sameCard, card, getOffStationCards);
                sameCardCount--;
            }
        }
    }

    private boolean getCurrentCardOffStationInfo(List<Card> cards, Card card, List<Card> getOffStationCards) {
        boolean isGetCardOffInfo = false;
        //找到在该站之后的消费的集合
        List<Card> fitCards = this.getFitCards(cards, card);
        Map<Integer, List<Card>> map = this.ListToMapByonStationNoKey(fitCards);
        int num = 0;
        //得到该用户在之后站点上车最多的站点的次数
        List<Card> cardList = new ArrayList<>();
        for (Map.Entry<Integer, List<Card>> entry : map.entrySet()) {
            List<Card> value = entry.getValue();
            if (num == 0) {
                cardList = value;
                num++;
            } else {
                if (cardList.size() < value.size()) {
                    cardList = value;
                }
            }
        }

        //如果超过概率阈值，得到该用户这次使用的下车站点及其时间等信息
        Double regularTravelChanceValue = configProperties.getRegularTravelChanceValue();
        if (regularTravelChanceValue <= (double) cardList.size() / fitCards.size()) {
            if (cardList.size() == 0) {
                return isGetCardOffInfo;
            }
            Card card1 = cardList.get(0);
            List<StationDirection> stationDirections = stationDirectionEhcache.get(card1.getLineNo() + "," + card1.getOnStationName());
            Integer stationNo = null;
            for (StationDirection stationDirection : stationDirections) {
                if (!card1.getDirection().equals(stationDirection.getDirection())) {
                    stationNo = stationDirection.getStationNo();
                    break;
                }
            }
            if (stationNo != null) {
                card.setOffStationNo(stationNo);
                card.setOffStationUniqueKey(card.getLineNo() + "," + stationNo + "," + card.getDirection());
                card.setOffStationName(card1.getOnStationName());
                card.setOffStationHandleType(OffStationHandleTypeEnum.ACCORIDING_CHANCE.getValue());
                //下车时间
                boolean isGetOffTime = this.getOffStationTime(card);
                if (isGetOffTime) {
                    getCardsByChanceCount++;
                    getOffStationCards.add(card);
                    isGetCardOffInfo = true;
                }
            }
        }
        return isGetCardOffInfo;
    }

    private List<Card> getFitCards(List<Card> cards, Card card) {
        List<Card> cardList = new ArrayList<>();
        for (Card currentCard : cards) {
            List<StationDirection> stationDirections = stationDirectionEhcache.get(currentCard.getLineNo() + "," + currentCard.getOnStationName());
            if (stationDirections != null && stationDirections.size() > 0) {
                for (StationDirection stationDirection : stationDirections) {
                    if (!currentCard.getDirection().equals(stationDirection.getDirection()) && stationDirection.getStationNo() > card.getOnStationNo()) {
                        cardList.add(currentCard);
                        break;
                    }
                }
            } else {
                noFindStationDirectionCount++;
            }
        }
        return cardList;
    }

    private Map<Integer, List<Card>> ListToMapByonStationNoKey(List<Card> cards) {
        Map<Integer, List<Card>> map = new HashMap<>();
        for (Card card1 : cards) {
            List<Card> cardList = new ArrayList<>();
            Integer onStationNo = card1.getOnStationNo();
            if (map.containsKey(onStationNo)) {
                cardList = map.get(onStationNo);
            }
            cardList.add(card1);
            map.put(onStationNo, cardList);
        }
        return map;
    }

    private boolean getOffStationTime(Card card) {
        boolean isGetOffTime = false;
        List<CarTrail> carTrails = carTrailEhCache.get(card.getBusNo());
        if (carTrails == null || carTrails.size() == 0) {
            return isGetOffTime;
        }
        long time = 0;
        CarTrail ct = null;
        int num = 0;
        for (int i = 0; i < carTrails.size(); i++) {
            CarTrail carTrail = carTrails.get(i);
            long l = carTrail.getInTime().getTime() - card.getPayTime().getTime();
            if (!card.getOffStationUniqueKey().equals(carTrail.getStationUniqueKey()) || l < 0) {
                continue;
            }
            if (num == 0) {
                time = l;
                ct = carTrail;
                num++;
            } else {
                if (l < time) {
                    time = l;
                    ct = carTrail;
                }
            }
        }
        if (ct != null) {
            //用于处理消费数据上下车时间不一致时记录日志
            boolean isCorrectInTime = this.cardExceptionLog(card, ct);
            if (!isCorrectInTime) {
                return false;
            }
            card.setOffStationName(ct.getStationName());
            card.setOffTime(ct.getInTime());
            isGetOffTime = true;
        }
        return isGetOffTime;
    }

    private boolean cardExceptionLog(Card card, CarTrail ct) {
        boolean isCorrectInTime = true;
        Date date = DateUtil.setDateDay(card.getPayTime(), 1);
        Date nyrDate = DateUtil.getNYRDate(date);
        if (ct.getInTime().getTime() > nyrDate.getTime() || ct.getInTime().getTime() < card.getPayTime().getTime()) {
            LoggingPo loggingPo = new LoggingPo();
            loggingPo.setExceptionContent("上下车时间不一致");
            loggingPo.setCard(card);
            loggingPo.setCarTrail(ct);
            loggingPo.setCreateTime(new Date());
            mongoDao.addLoggingPo(loggingPo);
            isCorrectInTime = false;
        }
        return isCorrectInTime;
    }

    private void getOffStationInfo(Card currentCard, Card nextCard, List<Card> getOffStationCards, List<Card> cardList, int currentI) {
        List<EachRecentStationRange> rangeList = recentStationRageEhCache.get(nextCard.getOnStationUniqueKey());
        if (rangeList != null && rangeList.size() != 0) {
            Double minDistance = configProperties.getDistanceValue();
            EachRecentStationRange ersr = null;
            for (int i = 1; i < rangeList.size(); i++) {
                EachRecentStationRange range = rangeList.get(i);
                Double distance = range.getDistance();
                if (distance < minDistance
                        && range.getLineNo().equals(currentCard.getLineNo())
                        && range.getDirection().equals(currentCard.getDirection())) {
                    minDistance = distance;
                    ersr = range;
                }
            }
            if (ersr != null && ersr.getStationNo() > currentCard.getOnStationNo()) {
                currentCard.setOffStationNo(ersr.getStationNo());
                currentCard.setOffStationUniqueKey(ersr.getStationUniqueKey());
                currentCard.setOffStationName(ersr.getStationName());
                currentCard.setOffStationHandleType(OffStationHandleTypeEnum.ACCORIDING_TRANSFER.getValue());
                currentCard.setTransferId(nextCard.getId());
                boolean isGetOffTime = this.getOffStationTime(currentCard);
                if (isGetOffTime) {
                    getOffStationCards.add(currentCard);
                    //该用户上一次车消费多次卡，下车信息都一致
                    this.handleSameCard(getOffStationCards, currentCard, cardList, currentI);
                } else {
                    this.handleOffStationInfoByChance(currentCard, getOffStationCards, cardList, currentI);
                }
            } else {
                noRecentStationRangeCount++;
                this.handleOffStationInfoByChance(currentCard, getOffStationCards, cardList, currentI);
            }
        } else {
            noRecentStationRangeCount++;
            this.handleOffStationInfoByChance(currentCard, getOffStationCards, cardList, currentI);
        }
    }

    private void addGetOffStationCards(Card sameCard, Card card1, List<Card> getOffStationCards) {
        sameCard.setOffStationNo(card1.getOffStationNo());
        sameCard.setOffStationName(card1.getOffStationName());
        sameCard.setOffStationUniqueKey(card1.getOffStationUniqueKey());
        sameCard.setOffTime(card1.getOffTime());
        getOffStationCards.add(sameCard);
    }

}
