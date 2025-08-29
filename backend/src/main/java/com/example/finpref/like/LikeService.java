package com.example.finpref.like;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class LikeService {
  private final LikeDao dao; private final ObjectMapper om;
  public LikeService(LikeDao dao, ObjectMapper om){ this.dao = dao; this.om = om; }

  @Transactional
  public long create(String userId, long productNo, String account, int qty){
    long sn = dao.create(userId, productNo, account, qty);
    dao.audit("LIKE_CREATE", toJson(Map.of("sn", sn, "userId", userId, "productNo", productNo, "qty", qty)));
    return sn;
  }

  @Transactional
  public void update(long sn, String account, int qty){
    dao.update(sn, account, qty);
    dao.audit("LIKE_UPDATE", toJson(Map.of("sn", sn, "qty", qty)));
  }

  @Transactional
  public void delete(long sn){
    dao.delete(sn);
    dao.audit("LIKE_DELETE", toJson(Map.of("sn", sn)));
  }

  public List<Map<String,Object>> listByUser(String userId){ return dao.listByUser(userId); }

  private String toJson(Object o){
    try{ return om.writeValueAsString(o); }catch(Exception e){ return "{}"; }
  }
}
