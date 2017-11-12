package io.github.jamiesanson.broker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Transient annotation for repository field.
 *
 * Usage:
 * {@code
 *  @Transient
 *  public Broker<List<Cats>> catsList;
 *
 *  catsList.get( cats -> showCats(cats)) // Here cats are gathered through the remote fulfillment
 *                                        // action. Get is an asynchronous action taking a callback
 * }
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Transient {
}
