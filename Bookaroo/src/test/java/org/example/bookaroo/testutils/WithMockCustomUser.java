package org.example.bookaroo.testutils;

import org.springframework.security.test.context.support.WithSecurityContext;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    String username() default "user";

    String name() default "User";

    String password() default "password";

    String role() default "USER";

    String id() default "00000000-0000-0000-0000-000000000001";
}