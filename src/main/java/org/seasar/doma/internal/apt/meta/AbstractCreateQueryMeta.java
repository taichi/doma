package org.seasar.doma.internal.apt.meta;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public abstract class AbstractCreateQueryMeta extends AbstractQueryMeta {

  protected AbstractCreateQueryMeta(ExecutableElement method, TypeElement dao) {
    super(method, dao);
  }

  @Override
  public <R, P> R accept(QueryMetaVisitor<R, P> visitor, P p) {
    return visitor.visitAbstractCreateQueryMeta(this, p);
  }
}
