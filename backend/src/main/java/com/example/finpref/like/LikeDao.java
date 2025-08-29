package com.example.finpref.like;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LikeDao {
  private final NamedParameterJdbcTemplate jdbc;
  public LikeDao(NamedParameterJdbcTemplate jdbc){ this.jdbc = jdbc; }

  public long create(String userId, long productNo, String account, int qty){
    Long sn = jdbc.queryForObject("SELECT sp_create_like(:u,:p,:a,:q)",
      new MapSqlParameterSource().addValue("u",userId).addValue("p",productNo).addValue("a",account).addValue("q",qty), Long.class);
    return sn != null ? sn : -1;
  }

    public void update(long sn, String account, int qty){
    jdbc.update("CALL sp_update_like(:sn,:a,:q)",
      new MapSqlParameterSource().addValue("sn",sn).addValue("a",account).addValue("q",qty));
  }

  public void delete(long sn){
    jdbc.update("CALL sp_delete_like(:sn)", Map.of("sn",sn));
  }

  public void audit(String event, String payloadJson){
    jdbc.update("CALL sp_create_audit(:e, CAST(:p AS JSONB))",
      new MapSqlParameterSource().addValue("e",event).addValue("p",payloadJson));
  }


  public List<Map<String,Object>> listByUser(String userId){
    return jdbc.queryForList("SELECT * FROM sp_get_likes_by_user(:u)", Map.of("u", userId));
  }

}
