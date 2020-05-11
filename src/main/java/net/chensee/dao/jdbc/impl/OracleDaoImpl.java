package net.chensee.dao.jdbc.impl;

import net.chensee.dao.jdbc.OracleDao;
import net.chensee.dao.jdbc.impl.rowMapper.CarTrailRowMapper;
import net.chensee.dao.jdbc.impl.rowMapper.CardRowMapper;
import net.chensee.dao.jdbc.impl.rowMapper.LineStationRowMapper;
import net.chensee.dao.jdbc.impl.rowMapper.StationGPSDataRowMapper;
import net.chensee.entity.po.Card;
import net.chensee.entity.po.CompanyToLinePo;
import net.chensee.entity.po.LineStation;
import net.chensee.entity.po.StationGPSData;
import net.chensee.entity.vo.CarTrail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class OracleDaoImpl implements OracleDao{

    @Autowired
//    @Qualifier("secondaryJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    @Override
    public List<Card> getCITYCARDTRADE(int pageNumber, int pageSize, String queryTime) {
        String sql = "SELECT CARD_NO,RIDING_TIME,LINE_NO,BUS_NO,NULL,DEPT_NO FROM (SELECT ROWNUM AS rowno, e.* FROM EX_CITYCARD_TRADE@HANGYE01 e WHERE e.INS_TIME BETWEEN to_date(?,'yyyymmdd hh24:mi:ss') and to_date(?,'yyyymmdd hh24:mi:ss') AND ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        return oracleJdbcTemplate.query(sql, new Object[]{queryTime+" 00:00:00",queryTime+" 23:59:59", pageSize * pageNumber, (pageNumber - 1) * pageSize + 1}, new CardRowMapper());
    }

    @Override
    public List<Card> getMOTCARDTRADE(int pageNumber, int pageSize, String queryTime) {
        String sql = "SELECT CARD_NO,RIDING_TIME,LINE_NO,BUS_NO,NULL,DEPT_NO FROM (SELECT ROWNUM AS rowno, e.* FROM TK_MOTCARD_TRADE@HANGYE01 e WHERE e.INS_TIME BETWEEN to_date(?,'yyyymmdd hh24:mi:ss') and to_date(?,'yyyymmdd hh24:mi:ss') AND ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        return oracleJdbcTemplate.query(sql, new Object[]{queryTime+" 00:00:00",queryTime+" 23:59:59", pageSize * pageNumber, (pageNumber - 1) * pageSize + 1}, new CardRowMapper());
    }

    @Override
    public List<Card> getCPUCardMTRADE(int pageNumber, int pageSize, String queryTime) {
        String sql = "SELECT CARD_NO,RIDING_TIME,LINE_NO,BUS_NO,NULL,DEPT_NO FROM (SELECT ROWNUM AS rowno, e.* FROM TK_CPUCARD_MTRADE@HANGYE01 e WHERE e.INS_TIME BETWEEN to_date(?,'yyyymmdd hh24:mi:ss') and to_date(?,'yyyymmdd hh24:mi:ss') AND\n" +
                "CARD_TYPE IN(3,4,5,10,603,604,605,610)\n" +
                "AND REC_TYPE IN(2) AND ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        return oracleJdbcTemplate.query(sql, new Object[]{queryTime+" 00:00:00",queryTime+" 23:59:59", pageSize * pageNumber, (pageNumber - 1) * pageSize + 1}, new CardRowMapper());
    }

    @Override
    public List<Card> getCPUCardTRADE(int pageNumber, int pageSize, String queryTime) {
        String sql = "SELECT CARD_NO,RIDING_TIME,LINE_NO,BUS_NO,NULL,DEPT_NO FROM (SELECT ROWNUM AS rowno, e.* FROM TK_CPUCARD_TRADE@HANGYE01 e WHERE e.INS_TIME BETWEEN to_date(?,'yyyymmdd hh24:mi:ss') and to_date(?,'yyyymmdd hh24:mi:ss') AND\n" +
                "CARD_TYPE IN(616,603,604,605,610,617,600,618,619,620,621,622,623,0,3,4,5,10,16,17)\n" +
                "AND REC_TYPE IN(0,3,4) AND ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        return oracleJdbcTemplate.query(sql, new Object[]{queryTime+" 00:00:00",queryTime+" 23:59:59", pageSize * pageNumber, (pageNumber - 1) * pageSize + 1}, new CardRowMapper());
    }

    @Override
    public List<Card> getCardTRADE(int pageNumber, int pageSize, String queryTime) {
        String sql = "SELECT CARD_NO,RIDING_TIME,LINE_NO ,BUS_NO,CARD_SERIAL,DEPT_NO FROM (SELECT ROWNUM AS rowno, e.* FROM ZY_CARD_TRADE@HANGYE01 e WHERE e.INS_TIME BETWEEN to_date(?,'yyyymmdd hh24:mi:ss') and to_date(?,'yyyymmdd hh24:mi:ss') AND CARD_TYPE IN(616,603,604,605,610,617,600,618,619,620,621,622,623,0,3,4,5,10,16,17)\n" +
                "AND REC_TYPE IN(0,3,4) AND ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        return oracleJdbcTemplate.query(sql, new Object[]{queryTime+" 00:00:00",queryTime+" 23:59:59", pageSize * pageNumber, (pageNumber - 1) * pageSize + 1}, new CardRowMapper());
    }

    @Override
    public List<Card> getMCardTRADE(int pageNumber, int pageSize, String queryTime) {
        String sql = "SELECT CARD_NO,RIDING_TIME,LINE_NO ,BUS_NO,CARD_SERIAL,DEPT_NO FROM (SELECT ROWNUM AS rowno, e.* FROM ZY_MCARD_TRADE@HANGYE01 e WHERE e.INS_TIME BETWEEN to_date(?,'yyyymmdd hh24:mi:ss') and to_date(?,'yyyymmdd hh24:mi:ss') AND\n" +
                "CARD_TYPE IN(3,4,5,10,603,604,605,610)\n" +
                "AND REC_TYPE IN(2) AND ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        return oracleJdbcTemplate.query(sql, new Object[]{queryTime+" 00:00:00",queryTime+" 23:59:59", pageSize * pageNumber, (pageNumber - 1) * pageSize + 1}, new CardRowMapper());
    }

    @Override
    public List<Card> getUPAYCARDTRADE(int pageNumber, int pageSize, String queryTime) {
        String sql = "SELECT CARD_NO,ALLOT_TIME,LINE_NO ,BUS_NO,NULL,DEPT_NO FROM (SELECT ROWNUM AS rowno, e.* FROM TK_UPAY_CARD_TRADE@HANGYE01 e WHERE e.INS_TIME BETWEEN to_date(?,'yyyymmdd hh24:mi:ss') and to_date(?,'yyyymmdd hh24:mi:ss') AND ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        return oracleJdbcTemplate.query(sql, new Object[]{queryTime+" 00:00:00",queryTime+" 23:59:59", pageSize * pageNumber, (pageNumber - 1) * pageSize + 1}, new CardRowMapper());
    }

    @Override
    public List<Card> getUPAYQRTRADE(int pageNumber, int pageSize, String queryTime) {
        String sql = "SELECT VOUCHER_NO,RIDING_TIME,LINE_NO,BUS_NO,USER_ID,DEPT_NO FROM (SELECT ROWNUM AS rowno, e.* FROM TK_UPAY_QR_TRADE@HANGYE01 e WHERE e.INS_TIME BETWEEN to_date(?,'yyyymmdd hh24:mi:ss') and to_date(?,'yyyymmdd hh24:mi:ss') AND ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        return oracleJdbcTemplate.query(sql, new Object[]{queryTime+" 00:00:00",queryTime+" 23:59:59", pageSize * pageNumber, (pageNumber - 1) * pageSize + 1}, new RowMapper<Card>() {
            @Nullable
            @Override
            public Card mapRow(ResultSet resultSet, int i) throws SQLException {
                Card card = new Card();
                card.setCardId(resultSet.getString(1));
                card.setLineNo(resultSet.getString(3));
                card.setBusNo(resultSet.getString(4));
                String userId = resultSet.getString(5);
                if (userId == null) {
                    card.setUserId(card.getCardId());
                } else {
                    card.setUserId(userId);
                }
                card.setDeptNo(resultSet.getString(6));
                String dateStr = resultSet.getString(2);
                if (dateStr != null && dateStr.length() != 0) {
                    String date = dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8) + " " + dateStr.substring(8, 10) + ":" + dateStr.substring(10, 12) + ":" + dateStr.substring(12, 14);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date payTime = sdf.parse(date);
                        card.setPayTime(payTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return card;
            }
        });
    }

    @Override
    public List<Card> getWXMINITRADE(int pageNumber, int pageSize, String queryTime) {
        String sql = "SELECT TRADE_NO,RIDING_TIME,LINE_NO,BUS_NO,USERUUID,DEPT_NO FROM (SELECT ROWNUM AS rowno, e.* FROM TK_WXMINI_TRADE@HANGYE01 e WHERE e.INS_TIME BETWEEN to_date(?,'yyyymmdd hh24:mi:ss') and to_date(?,'yyyymmdd hh24:mi:ss') AND ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        return oracleJdbcTemplate.query(sql, new Object[]{queryTime+" 00:00:00",queryTime+" 23:59:59", pageSize * pageNumber, (pageNumber - 1) * pageSize + 1}, new CardRowMapper());
    }

    @Override
    public List<Card> getALIPAYTRADE(int pageNumber, int pageSize, String queryTime) {
        String sql = "SELECT CARDID,RIDING_TIME,LINE_NO ,BUS_NO,USER_ID,DEPT_NO FROM (SELECT ROWNUM AS rowno, e.* FROM TK_ALIPAY_TRADE@HANGYE01 e WHERE e.UPDATE_TIME BETWEEN to_date(?,'yyyymmdd hh24:mi:ss') and to_date(?,'yyyymmdd hh24:mi:ss') AND ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        return oracleJdbcTemplate.query(sql, new Object[]{queryTime+" 00:00:00",queryTime+" 23:59:59", pageSize * pageNumber, (pageNumber - 1) * pageSize + 1}, new CardRowMapper());
    }

    @Override
    public int isHaveCITYCARDTRADEPage(String deptNo, String queryTime, Integer pageNumber, Integer pageSize) {
        String sql = "SELECT count(*) as num FROM (SELECT ROWNUM AS rowno, e.* FROM EX_CITYCARD_TRADE@HANGYE01 e WHERE e.INS_TIME BETWEEN to_date(?,'yyyymmdd hh24:mi:ss') and to_date(?,'yyyymmdd hh24:mi:ss') and DEPT_NO = ? AND ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        Map<String, Object> stringObjectMap = oracleJdbcTemplate.queryForMap(sql, new Object[]{queryTime+" 00:00:00",queryTime+" 23:59:59", deptNo, pageSize * pageNumber, (pageNumber - 1) * pageSize + 1});
        int num = Integer.parseInt(stringObjectMap.get("num").toString());
        return num;
    }

    @Override
    public List<CompanyToLinePo> getCompanyToLinePos() {
        String sql = "SELECT \"deptNo\",\"lineNo\" FROM OD_LINE_MATE";
        return oracleJdbcTemplate.query(sql, (rs, rowNum) -> {
            CompanyToLinePo companyToLinePo = new CompanyToLinePo();
            companyToLinePo.setDeptNo(rs.getString("deptNo"));
            companyToLinePo.setLineNo(rs.getString("lineNo"));
            return companyToLinePo;
        });
    }




    @Override
    public List<CarTrail> getBusInfo(int pageNumber, int pageSize, String queryTime) {
//        String sql = "SELECT b.O_LINENAME as LINE_NO,b.O_BUSNAME as BUS_NO,b.O_STATIONNO as STATION_NO,b.O_UP as DIRECTION,b.O_ARRIVEDATETIME as IN_TIME,b.O_LEAVEDATETIME as OUT_TIME,s.O_STATIONNAME as STATION_NAME \n" +
//                "FROM zdcom.T_JK_LEAVESTATION"+queryTime+"@YUND01 b,od_bus_station_gps_data s \n" +
//                "where b.O_LINENAME = s.O_LINENAME and b.O_UP = s.O_DIRECTION and b.O_STATIONNO = s.O_STATIONNO \n" +
//                "limit ?,? ";
        String sql = "SELECT * FROM (SELECT ROWNUM AS rowno,b.O_LINENAME as LINE_NO,b.O_BUSNAME as BUS_NO,b.O_STATIONNO as STATION_NO,b.O_UP as DIRECTION,b.O_ARRIVEDATETIME as IN_TIME,b.O_LEAVEDATETIME as OUT_TIME\n" +
                "                FROM zdcom.T_JK_LEAVESTATION"+queryTime+"@YUND01 b\n" +
                "                where ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        return oracleJdbcTemplate.query(sql, new Object[]{pageSize * pageNumber, (pageNumber - 1) * pageSize + 1}, new CarTrailRowMapper());
    }

    @Override
    public List<StationGPSData> getStationGPSData(int pageNumber, int pageSize) {
        String sql = "SELECT * FROM (SELECT ROWNUM AS rowno, e.* FROM od_bus_station_gps_data e WHERE ROWNUM <= ? ) ex WHERE ex.rowno >= ?";
        return oracleJdbcTemplate.query(sql, new Object[]{pageSize * pageNumber, (pageNumber - 1) * pageSize + 1}, new StationGPSDataRowMapper());
    }

    @Override
    public List<LineStation> getLineStations() {
        String sql = "select O_LINENAME,O_DIRECTION,min(O_STATIONNO) as startStation,max(O_STATIONNO) as endStation from od_bus_station_gps_data GROUP BY O_LINENAME,O_DIRECTION";
        return oracleJdbcTemplate.query(sql, new LineStationRowMapper());
    }

    @Override
    public String getStartStationName(String lineNo, int direction, Integer startStation) {
        String sql = "select O_STATIONNAME from od_bus_station_gps_data where O_LINENAME = ? AND O_DIRECTION = ? AND O_STATIONNO = ?";
        List<LineStation> query = oracleJdbcTemplate.query(sql, new Object[]{lineNo, direction, startStation}, new RowMapper<LineStation>() {
            @Nullable
            @Override
            public LineStation mapRow(ResultSet resultSet, int i) throws SQLException {
                LineStation lineStation1 = new LineStation();
                lineStation1.setStartStationName(resultSet.getString(1));
                return lineStation1;
            }
        });
        String startStationName = null;
        if (query != null && query.size() > 0) {
            startStationName = query.get(0).getStartStationName();
        }
        return startStationName;
    }

    @Override
    public String getEndStationName(String lineNo, int direction, Integer startStation) {
        String sql = "select O_STATIONNAME from od_bus_station_gps_data where O_LINENAME = ? AND O_DIRECTION = ? AND O_STATIONNO = ?";
        List<LineStation> query = oracleJdbcTemplate.query(sql, new Object[]{lineNo, direction, startStation}, new RowMapper<LineStation>() {
            @Nullable
            @Override
            public LineStation mapRow(ResultSet resultSet, int i) throws SQLException {
                LineStation lineStation1 = new LineStation();
                lineStation1.setEndStationName(resultSet.getString(1));
                return lineStation1;
            }
        });
        String endStationName = null;
        if (query != null && query.size() > 0) {
            endStationName = query.get(0).getEndStationName();
        }
        return endStationName;
    }
}
