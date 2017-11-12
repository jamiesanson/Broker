package io.github.jamiesanson.broker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Persistent annotation for repository field.
 *
 * Usage:
 * {@code
 *  @Persistent
 *  public Broker<List<Cats>> catsList;
 *
 *  catsList.get( cats -> showCats(cats)) // Here cats are gathered through the local fulfillment
 *                                        // if it exists there, else it fetches from the remote
 *                                        // if either it doesn't exist or the cache is invalidated
 * }
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Persistent {
}
