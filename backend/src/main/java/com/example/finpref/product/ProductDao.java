package com.example.finpref.product;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductDao {
  private final NamedParameterJdbcTemplate jdbc;
  public ProductDao(NamedParameterJdbcTemplate jdbc){ this.jdbc = jdbc; }

  // public long create(String name, double price, double fee){
  //   String sql = "SELECT sp_create_product(:n,:p,:f)";
  //   Long id = jdbc.queryForObject(sql, Map.of("n", name, "p", price, "f", fee), Long.class);
  //   return id != null ? id : -1;
  // }

  // public void update(long no, String name, double price, double fee){
  //   jdbc.update("CALL sp_update_product(:no,:n,:p,:f)",
  //     new MapSqlParameterSource().addValue("no", no).addValue("n", name).addValue("p", price).addValue("f", fee));
  // }

  // public void delete(long no){
  //   jdbc.update("CALL sp_delete_product(:no)", Map.of("no", no));
  // }

  public record Product(long no, String productName, double price, double feeRate){}
  public List<Product> list(){
    return jdbc.query("SELECT * FROM sp_list_products()", (rs, i) ->
      new Product(rs.getLong("no"), rs.getString("product_name"), rs.getDouble("price"), rs.getDouble("fee_rate")));
  }
}
