package net.chensee.dao.jdbc.impl.rowMapper;

import net.chensee.common.ConvertUtil;
import net.chensee.entity.vo.CarTrail;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class CarTrailRowMapper implements RowMapper<CarTrail> {
    @Nullable
    @Override
    public CarTrail mapRow(ResultSet resultSet, int i) throws SQLException {
        CarTrail carTrail = new CarTrail();
        carTrail.setBusNo(resultSet.getString("BUS_NO"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String in_time = sdf.format(resultSet.getTimestamp("IN_TIME"));
        String out_time = sdf.format(resultSet.getTimestamp("OUT_TIME"));
        try {
            carTrail.setInTime(sdf.parse(in_time));
            carTrail.setOutTime(sdf.parse(out_time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        carTrail.setLineNo(resultSet.getString("LINE_NO"));
        carTrail.setStationNo(resultSet.getInt("STATION_NO"));
//        carTrail.setStationName(resultSet.getString("STATION_NAME"));
        int direction = resultSet.getInt("DIRECTION");
        carTrail.setDirection(ConvertUtil.convertDirection(direction));
        return carTrail;
    }
}
