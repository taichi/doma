package org.seasar.doma.internal.apt.dao;

import org.seasar.doma.Dao;
import org.seasar.doma.Select;

@Dao(config = MyConfig.class)
public interface RawTypeParamDao {

  @SuppressWarnings("rawtypes")
  @Select
  int select(Height height);
}
