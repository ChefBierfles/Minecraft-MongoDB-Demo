package com.chefbierfles.mongodb.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseEntity {
    String collectionName();
    boolean useCache() default false;
    int expiryInSecondsAfterAccess() default 0;
    int maximumSize() default 0;

}