package org.seasar.doma.internal.apt.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface ClassAnnotation2 {

  int aaa();

  boolean bbb();
}
