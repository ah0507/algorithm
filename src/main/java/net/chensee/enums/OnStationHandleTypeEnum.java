package net.chensee.enums;

public enum OnStationHandleTypeEnum {
    //在进出站区间内
    ACCORIDING_INOUTTIME(0),
    //在离站区间内
    ACCORIDING_OUTTIME(1),
    //在进站区间内
    ACCORIDING_INTIME(2),
    //算站距绝对值
    ACCORIDING_ABSOLUTEVALUE(3);

    private int value;

    OnStationHandleTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
