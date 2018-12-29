package org.seasar.doma.internal.jdbc.command;

import static org.seasar.doma.internal.util.AssertionUtil.assertNotNull;

import java.util.Optional;
import org.seasar.doma.jdbc.domain.DomainType;

/**
 * @author nakamura-to
 * @param <BASIC>
 * @param <DOMAIN>
 */
public class OptionalDomainSingleResultHandler<BASIC, DOMAIN>
    extends ScalarSingleResultHandler<BASIC, Optional<DOMAIN>> {

  public OptionalDomainSingleResultHandler(DomainType<BASIC, DOMAIN> domainType) {
    super(() -> domainType.createOptionalScalar());
    assertNotNull(domainType);
  }
}
