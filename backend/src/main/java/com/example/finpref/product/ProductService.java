package com.example.finpref.product;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {
  private final ProductDao dao;
  public ProductService(ProductDao dao){ this.dao = dao; }

  // public long create(String n, double p, double f){ return dao.create(n,p,f); }
  // public void update(long no, String n, double p, double f){ dao.update(no,n,p,f); }
  // public void delete(long no){ dao.delete(no); }
  public List<ProductDao.Product> list(){ return dao.list(); }
}
