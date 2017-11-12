package io.github.jamiesanson.broker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation labelling an abstract class as a Repository.
 *
 * Usage:
 * {@code
 *  @BrokerRepo
 *  public abstract class CatRepo {
 *
 *      @Persistent
 *      public Broker<List<Cats>> catsList;
 *      ...
 *  }
 * }
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface BrokerRepo {
}

