package net.chensee.common;

import net.chensee.entity.po.Card;
import net.chensee.entity.po.NoMatchOutTimeCard;
import net.chensee.entity.po.StationDirection;
import net.chensee.entity.po.StationDirectionPo;
import net.chensee.entity.po.analyse.line.EachLineStationInfoStatisticsPo;
import net.chensee.entity.po.analyse.line.EachLineStationInfoStatisticsTempPo;
import net.chensee.entity.po.history.HistoryCard;
import net.chensee.entity.po.history.HistoryCardCopy;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConvertUtil {

    public static NoMatchOutTimeCard convertToNoMatchOutTimeCard(Card card) {
        NoMatchOutTimeCard noMatchOutTimeCard = new NoMatchOutTimeCard();
        BeanUtils.copyProperties(card, noMatchOutTimeCard);
        return noMatchOutTimeCard;
    }

    public static Card convertToNoMatchOutTimeCard(NoMatchOutTimeCard noMatchOutTimeCard) {
        Card card = new Card();
        BeanUtils.copyProperties(noMatchOutTimeCard, card);
        return card;
    }

    public static List<Card> convertToCard(List<HistoryCard> historyCards) {
        List<Card> cards = new ArrayList<>();
        for (HistoryCard historyCard : historyCards) {
            Card card = new Card();
            BeanUtils.copyProperties(historyCard, card);
            cards.add(card);
        }
        return cards;
    }

    public static List<HistoryCard> convertToHistoryCard(List<HistoryCard> historyCards, List<Card> cardList) {
        HistoryCard historyCard;
        for (Card card : cardList) {
            historyCard = new HistoryCard();
            historyCard.setId(card.getId());
            historyCard.setUserId(card.getUserId());
            historyCard.setCardId(card.getCardId());
            historyCard.setCardType(card.getCardType());
            historyCard.setPayTime(card.getPayTime());
            historyCard.setLineNo(card.getLineNo());
            historyCard.setBusNo(card.getBusNo());
            historyCard.setDirection(card.getDirection());
            historyCard.setOnStationHandleType(card.getOnStationHandleType());
            historyCard.setOnStationUniqueKey(card.getOnStationUniqueKey());
            historyCard.setOnStationName(card.getOnStationName());
            historyCard.setOnStationNo(card.getOnStationNo());
            historyCard.setOffStationUniqueKey(card.getOffStationUniqueKey());
            historyCard.setOffStationName(card.getOffStationName());
            historyCard.setOffStationNo(card.getOffStationNo());
            historyCard.setOffTime(card.getOffTime());
            historyCard.setOffStationHandleType(card.getOffStationHandleType());
            historyCard.setTransferId(card.getTransferId());
            historyCard.setCreateTime(card.getCreateTime());
            historyCards.add(historyCard);
        }
        return historyCards;
    }

    public static List<HistoryCardCopy> convertToHistoryCardCopy(List<HistoryCardCopy> historyCardCopies, List<Card> cards) {
        HistoryCardCopy historyCardCopy;
        for (Card card : cards) {
            historyCardCopy = new HistoryCardCopy();
            historyCardCopy.setId(card.getId());
            historyCardCopy.setUserId(card.getUserId());
            historyCardCopy.setCardId(card.getCardId());
            historyCardCopy.setCardType(card.getCardType());
            historyCardCopy.setPayTime(card.getPayTime());
            historyCardCopy.setLineNo(card.getLineNo());
            historyCardCopy.setBusNo(card.getBusNo());
            historyCardCopy.setDirection(card.getDirection());
            historyCardCopy.setOnStationHandleType(card.getOnStationHandleType());
            historyCardCopy.setOnStationUniqueKey(card.getOnStationUniqueKey());
            historyCardCopy.setOnStationName(card.getOnStationName());
            historyCardCopy.setOnStationNo(card.getOnStationNo());
            historyCardCopy.setOffStationUniqueKey(card.getOffStationUniqueKey());
            historyCardCopy.setOffStationName(card.getOffStationName());
            historyCardCopy.setOffStationNo(card.getOffStationNo());
            historyCardCopy.setOffTime(card.getOffTime());
            historyCardCopy.setOffStationHandleType(card.getOffStationHandleType());
            historyCardCopy.setTransferId(card.getTransferId());
            historyCardCopy.setCreateTime(card.getCreateTime());
            historyCardCopies.add(historyCardCopy);
        }
        return historyCardCopies;
    }

    public static List<StationDirectionPo> mapToListStationDirectionPo(Map<String, List<StationDirection>> map) {
        List<StationDirectionPo> stationDirectionPos = new ArrayList<>();
        for (Map.Entry<String, List<StationDirection>> entry : map.entrySet()) {
            StationDirectionPo stationDirectionPo = new StationDirectionPo();
            stationDirectionPo.setLineNoAndStationNo(entry.getKey());
            stationDirectionPo.setStationDirections(entry.getValue());
            stationDirectionPos.add(stationDirectionPo);
        }
        return stationDirectionPos;
    }

    public static List<EachLineStationInfoStatisticsTempPo> convertToEachLineStationInfoStatisticsTempPo(List<EachLineStationInfoStatisticsPo> elpList) {
        List<EachLineStationInfoStatisticsTempPo> estpList = new ArrayList<>();
        EachLineStationInfoStatisticsTempPo estp;
        for (EachLineStationInfoStatisticsPo elp : elpList) {
            estp = new EachLineStationInfoStatisticsTempPo();
            BeanUtils.copyProperties(elp, estp);
            estpList.add(estp);
        }
        return estpList;
    }

    public static List<EachLineStationInfoStatisticsPo> convertToEachLineStationInfoStatisticsPos(List<EachLineStationInfoStatisticsTempPo> estps) {
        List<EachLineStationInfoStatisticsPo> espList = new ArrayList<>();
        EachLineStationInfoStatisticsPo esp;
        for (EachLineStationInfoStatisticsTempPo estp : estps) {
            esp = new EachLineStationInfoStatisticsPo();
            BeanUtils.copyProperties(estp, esp);
            espList.add(esp);
        }
        return espList;
    }

    public static String convertDirection(Integer direction) {
        String dir = null;
        switch (direction) {
            case 1:
                dir = "下行";
                break;
            case 0:
                dir = "上行";
                break;
            default:
                break;
        }
        return dir;
    }
}
