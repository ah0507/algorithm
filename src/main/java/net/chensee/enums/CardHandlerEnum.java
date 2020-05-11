package net.chensee.enums;

public enum CardHandlerEnum {

    CITY_CARD_TRADE("城市卡", 0),
    MOT_CARD_TRADE("交通部卡", 1),
    CPU_CARD_MTRADE("CPU月票卡", 2),
    CPU_CARD_TRADE("CPU客票", 3),
    CARD_TRADE("M1客票卡", 4),
    MCARD_TRADE("M1月票卡", 5),
    UPAY_CARD_TRADE("银联卡", 6),
    UPAY_QR_TRADE("银联码", 7),
    WXMINI_TRADE("微信", 8),
    ALIPAY_TRADE("支付宝", 9);


    private String key;

    private int value;

    CardHandlerEnum(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }
}
