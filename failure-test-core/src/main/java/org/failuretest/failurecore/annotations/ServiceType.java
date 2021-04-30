package org.failuretest.failurecore.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotate on classes to be register in system
 * serviceName is the process' service name, this name is used during failure injection, also used for service lookup.
 * If service lookup name is different from serviceName for some system, you can provide alias parameter,
 * then alias name will be used for service lookup, serviceName still used for failure injection.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ServiceType {

    String serviceName();

    String alias() default "";

}
