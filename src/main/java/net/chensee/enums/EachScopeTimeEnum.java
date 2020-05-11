package net.chensee.enums;

public enum EachScopeTimeEnum {

    MORNING_PEAK("04:00-08:00(早平峰)"," 08:00:00"),
    ZAOPING_PEAK("08:00-12:00(早高峰)", " 12:00:00"),
    MIDDAY_PEAK("12:00-16:00(午平峰)"," 16:00:00"),
    LATE_PEAK("16:00-19:00(晚高峰)"," 19:00:00"),
    WANPING_PEAK("19:00-23:59:59(晚平峰)"," 23:59:59");

    private String key;
    private String value;

    EachScopeTimeEnum(String key ,String value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
