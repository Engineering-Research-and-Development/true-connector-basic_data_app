package it.eng.idsa.dataapp.configuration;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.util.CollectionUtils;
import com.github.jsonldjava.shaded.com.google.common.base.Predicate;

@Configuration
@PropertySource("classpath:firewall.properties")
public class DataAppFirewall {

    @Value("${allowedHeaderNames}")
    private List<String> allowedHeaderNames;
    @Value("${allowedHeaderValues}")
    private List<String> allowedHeaderValues;
    @Value("${allowedMethods}")
    private List<String> allowedMethods;
    @Value("${allowBackSlash}")
    private Boolean allowBackSlash;
    @Value("${allowUrlEncodedSlash}")
    private boolean allowUrlEncodedSlash;
    @Value("${allowUrlEncodedDoubleSlash}")
    private boolean allowUrlEncodedDoubleSlash;
    @Value("${allowSemicolon}")
    private boolean allowSemicolon;
    @Value("${allowUrlEncodedPercent}")
    private boolean allowUrlEncodedPercent;
    @Value("${allowUrlEncodedPeriod}")
    private boolean allowUrlEncodedPeriod;

    @Bean
    @ConditionalOnProperty(name = "application.firewall.isEnabled", havingValue = "true", matchIfMissing = false)
    public StrictHttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();

        if (!CollectionUtils.isEmpty(allowedHeaderNames)) {
            Predicate<String> headerNamePredicate = createAllowedHeaderNamesPredicate(allowedHeaderNames);
            firewall.setAllowedHeaderNames(headerNamePredicate);
        } else {
            firewall.setAllowedHeaderNames((header) -> true);
        }
        if (!allowedHeaderValues.isEmpty()) {
            Predicate<String> headerNameValuePredicate = createAllowedHeaderValuesPredicate(allowedHeaderValues);
            firewall.setAllowedHeaderValues(headerNameValuePredicate);
        } else {
            firewall.setAllowedHeaderValues((header) -> true);
        }
        if (!CollectionUtils.isEmpty(allowedMethods)) {
            firewall.setAllowedHttpMethods(allowedMethods);
        } else {
            firewall.setUnsafeAllowAnyHttpMethod(true);
        }
        firewall.setAllowBackSlash(allowBackSlash);
        firewall.setAllowUrlEncodedSlash(allowUrlEncodedSlash);
        firewall.setAllowUrlEncodedDoubleSlash(allowUrlEncodedDoubleSlash);
        firewall.setAllowSemicolon(allowSemicolon);
        firewall.setAllowUrlEncodedPercent(allowUrlEncodedPercent);
        firewall.setAllowUrlEncodedPeriod(allowUrlEncodedPeriod);

        return firewall;
    }

    @Bean
    @ConditionalOnProperty(name = "application.firewall.isEnabled", havingValue = "true", matchIfMissing = false)
    RequestRejectedHandler requestRejectedHandler() {
        return new HttpStatusRequestRejectedHandler();
    }

    private Predicate<String> createAllowedHeaderNamesPredicate(List<String> allowedHeaderNames) {
        return allowedHeaderNames.stream().collect(Collectors.toSet())::contains;
    }

    private Predicate<String> createAllowedHeaderValuesPredicate(List<String> allowedHeaderNamesAndValues) {
        return allowedHeaderNamesAndValues.stream().collect(Collectors.toSet())::contains;
    }
}
