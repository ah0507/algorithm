package net.chensee.dao.jdbc.impl.rowMapper;

import net.chensee.common.ConvertUtil;
import net.chensee.entity.po.StationGPSData;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StationGPSDataRowMapper implements RowMapper<StationGPSData> {
    @Nullable
    @Override
    public StationGPSData mapRow(ResultSet resultSet, int i) throws SQLException {
        StationGPSData stationGPSData = new StationGPSData();
        stationGPSData.setStationNo(resultSet.getInt("O_STATIONNO"));
        stationGPSData.setStationName(resultSet.getString("O_STATIONNAME"));
        stationGPSData.setLineNo(resultSet.getString("O_LINENAME"));
        int o_direction = resultSet.getInt("O_DIRECTION");
        String direction = ConvertUtil.convertDirection(o_direction);
        stationGPSData.setDirection(direction);
        stationGPSData.setLat(resultSet.getDouble("O_LATITUDE")/60);
        stationGPSData.setLng(resultSet.getDouble("O_LONGITUDE")/60);
        return stationGPSData;
    }
}
