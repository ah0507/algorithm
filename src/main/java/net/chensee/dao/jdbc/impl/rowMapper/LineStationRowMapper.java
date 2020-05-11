package net.chensee.dao.jdbc.impl.rowMapper;

import net.chensee.common.ConvertUtil;
import net.chensee.entity.po.LineStation;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LineStationRowMapper implements RowMapper<LineStation> {
    @Nullable
    @Override
    public LineStation mapRow(ResultSet resultSet, int i) throws SQLException {
        LineStation lineStation = new LineStation();
        lineStation.setLineNo(resultSet.getString(1));
        Integer direction = resultSet.getInt(2);
        lineStation.setDirection(ConvertUtil.convertDirection(direction));
        lineStation.setStartStation(resultSet.getInt(3));
        lineStation.setEndStation(resultSet.getInt(4));
        return lineStation;
    }
}
