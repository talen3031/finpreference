package com.example.finpref.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {
  private final NamedParameterJdbcTemplate jdbc;
  public UserDao(NamedParameterJdbcTemplate jdbc){ this.jdbc = jdbc; }

  public Optional<Map<String,Object>> findByEmail(String email){
    String sql = "SELECT * FROM sp_get_user_by_email(:email)";
    return jdbc.query(sql, Map.of("email", email), rs -> {
      if(rs.next()){
        Map<String,Object> m = new HashMap<>();
        m.put("user_id", rs.getString("user_id"));
        m.put("user_name", rs.getString("user_name"));
        m.put("email", rs.getString("email"));
        m.put("password_hash", rs.getString("password_hash"));
        m.put("account", rs.getString("account"));
        return Optional.of(m);
      }
      return Optional.empty();
    });
  }
  // 範例：請依實際 schema 調整 table/column
  public String getDefaultAccount(String userId){
    return jdbc.queryForObject(
        "SELECT default_account FROM app_user WHERE id = :u",
        Map.of("u", userId),
        String.class
    );
  }
  public void create(String userId, String userName, String email, String passwordHash, String account){
  String sql = "CALL sp_create_user(:id,:name,:email,:pwd,:acct)";
  jdbc.update(sql, new MapSqlParameterSource()
    .addValue("id", userId).addValue("name", userName)
    .addValue("email", email).addValue("pwd", passwordHash)
    .addValue("acct", account));
}

}
