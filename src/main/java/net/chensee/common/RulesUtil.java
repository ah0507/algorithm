package net.chensee.common;

public class RulesUtil {

    public static String getBusNoRuleOfZeros(String busId){
        String bus_no = null;
        if (busId!=null && busId.length()!=0) {
            switch (busId.length()){
                case 1:
                    bus_no = "00000" + busId;
                    break;
                case 2:
                    bus_no = "0000"+busId;
                    break;
                case 3:
                    bus_no = "000"+busId;
                    break;
                case 4:
                    bus_no = "00"+busId;
                    break;
                case 5:
                    bus_no = "0"+busId;
                    break;
                default:
                    bus_no = busId;
                    break;
            }
        }
        return bus_no;
    }

}
