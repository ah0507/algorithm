package net.chensee.enums;

public enum OffStationHandleTypeEnum {
    //根据上车点是终点站
    ACCORIDING_ENDSTATION(0),
    //根据换乘出行链
    ACCORIDING_TRANSFER(1),
    //根据规律出行概率
    ACCORIDING_CHANCE(2);

    private int value;

    OffStationHandleTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
