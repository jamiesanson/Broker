package io.github.jamiesanson.broker.annotation;

import org.joda.time.Duration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.jamiesanson.broker.fulfillment.PersistenceType;

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
    String key();
    PersistenceType type() default PersistenceType.PERSISTENT;
    ExpireDuration cacheExpireDuration() default ExpireDuration.FOUR_HOURS;

    enum ExpireDuration {
        FIVE_MINUTES, ONE_HOUR, TWO_HOURS, FOUR_HOURS, EIGHT_HOURS, ONE_DAY;

        public Duration toDuration() {
            switch (this) {
                case FIVE_MINUTES:
                    return Duration.standardMinutes(5);
                case ONE_HOUR:
                    return Duration.standardHours(1);
                case TWO_HOURS:
                    return Duration.standardHours(2);
                case FOUR_HOURS:
                    return Duration.standardHours(4);
                case EIGHT_HOURS:
                    return Duration.standardHours(8);
                case ONE_DAY:
                    return Duration.standardDays(1);
            }

            return Duration.ZERO;
        }
    }
}
