package net.chensee.dao.jdbc.impl.rowMapper;

import net.chensee.entity.po.Card;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class CardRowMapper implements RowMapper<Card> {
    @Nullable
    @Override
    public Card mapRow(ResultSet resultSet, int i) throws SQLException {
        Card card = new Card();
        card.setCardId(resultSet.getString(1));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = sdf.format(resultSet.getTimestamp(2));
        try {
            card.setPayTime(sdf.parse(timeStamp));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        card.setLineNo(resultSet.getString(3));
        card.setBusNo(resultSet.getString(4));
        String userId = resultSet.getString(5);
        if (userId == null) {
            card.setUserId(card.getCardId());
        }else{
            card.setUserId(userId);
        }
        card.setDeptNo(resultSet.getString(6));
        return card;
    }
}
