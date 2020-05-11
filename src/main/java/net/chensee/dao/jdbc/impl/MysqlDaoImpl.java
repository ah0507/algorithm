//package net.chensee.dao.jdbc.impl;
//
//import net.chensee.dao.jdbc.MysqlDao;
//import net.chensee.dao.jdbc.impl.rowMapper.CarTrailRowMapper;
//import net.chensee.dao.jdbc.impl.rowMapper.LineStationRowMapper;
//import net.chensee.dao.jdbc.impl.rowMapper.StationGPSDataRowMapper;
//import net.chensee.entity.po.LineStation;
//import net.chensee.entity.po.StationGPSData;
//import net.chensee.entity.vo.CarTrail;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.lang.Nullable;
//import org.springframework.stereotype.Repository;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.List;
//
//@Repository
//public class MysqlDaoImpl implements MysqlDao{
//
//    @Autowired
//    @Qualifier("primaryJdbcTemplate")
//    private JdbcTemplate mysqlJdbcTemplate;
//
//    @Override
//    public List<CarTrail> getBusInfo(int pageNumber, int pageSize, String queryTime) {
//        String sql = "SELECT b.*,s.O_STATIONNAME as STATION_NAME FROM bus_trail_data b,bus_station_gps_data s where b.LINE_NO = s.O_LINENAME and b.DIRECTION = s.O_DIRECTION and b.STATION_NO = s.O_STATIONNO and CREATE_TIME = ? limit ?,?;";
//        return mysqlJdbcTemplate.query(sql, new Object[]{queryTime, (pageNumber - 1) * pageSize, pageSize}, new CarTrailRowMapper());
//    }
//
//    @Override
//    public List<StationGPSData> getStationGPSData(int pageNumber, int pageSize) {
//        String sql = "select * from bus_station_gps_data limit ?,?";
//        return mysqlJdbcTemplate.query(sql, new Object[]{(pageNumber - 1) * pageSize, pageSize}, new StationGPSDataRowMapper());
//    }
//
//    @Override
//    public List<LineStation> getLineStations() {
//        String sql = "select O_LINENAME,O_DIRECTION,min(O_STATIONNO) as startStation,max(O_STATIONNO) as endStation from bus_station_gps_data GROUP BY O_LINENAME,O_DIRECTION";
//        return mysqlJdbcTemplate.query(sql, new LineStationRowMapper());
//    }
//
//    @Override
//    public String getStartStationName(String lineNo, String direction, Integer startStation) {
//        String sql = "select O_STATIONNAME from bus_station_gps_data where O_LINENAME = ? AND O_DIRECTION = ? AND O_STATIONNO = ?";
//        List<LineStation> query = mysqlJdbcTemplate.query(sql, new Object[]{lineNo, direction, startStation}, new RowMapper<LineStation>() {
//            @Nullable
//            @Override
//            public LineStation mapRow(ResultSet resultSet, int i) throws SQLException {
//                LineStation lineStation1 = new LineStation();
//                lineStation1.setStartStationName(resultSet.getString(1));
//                return lineStation1;
//            }
//        });
//        String startStationName = null;
//        if (query != null && query.size() > 0) {
//            startStationName = query.get(0).getStartStationName();
//        }
//        return startStationName;
//    }
//
//    @Override
//    public String getEndStationName(String lineNo, String direction, Integer startStation) {
//        String sql = "select O_STATIONNAME from bus_station_gps_data where O_LINENAME = ? AND O_DIRECTION = ? AND O_STATIONNO = ?";
//        List<LineStation> query = mysqlJdbcTemplate.query(sql, new Object[]{lineNo, direction, startStation}, new RowMapper<LineStation>() {
//            @Nullable
//            @Override
//            public LineStation mapRow(ResultSet resultSet, int i) throws SQLException {
//                LineStation lineStation1 = new LineStation();
//                lineStation1.setEndStationName(resultSet.getString(1));
//                return lineStation1;
//            }
//        });
//        String endStationName = null;
//        if (query != null && query.size() > 0) {
//            endStationName = query.get(0).getEndStationName();
//        }
//        return endStationName;
//    }
//
//}
