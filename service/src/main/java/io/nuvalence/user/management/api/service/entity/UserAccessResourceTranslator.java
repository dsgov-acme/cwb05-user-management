package io.nuvalence.user.management.api.service.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.nuvalence.auth.access.cerbos.AccessResourceTranslator;
import io.nuvalence.user.management.api.service.mapper.UserEntityMapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * User Translator Class.
 */
@Component
public class UserAccessResourceTranslator
        implements AccessResourceTranslator, ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    @SuppressFBWarnings(
            value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
            justification =
                    "This is an established pattern for exposing spring state to static contexts."
                        + " The applicationContext is a singleton, so if this write were to occur"
                        + " multiple times, it would be idempotent.")
    public void setApplicationContext(final ApplicationContext applicationContext)
            throws BeansException {
        UserAccessResourceTranslator.applicationContext = applicationContext;
    }

    @Override
    public Object translate(Object resource) {
        if (resource instanceof UserEntity userEntity) {
            final UserEntityMapper mapper = applicationContext.getBean(UserEntityMapper.class);
            return mapper.entityToModel(userEntity);
        }

        return resource;
    }
}
