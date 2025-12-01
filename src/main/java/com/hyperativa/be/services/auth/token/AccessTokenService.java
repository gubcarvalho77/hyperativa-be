package com.hyperativa.be.services.auth.token;

import com.hyperativa.be.config.security.SecurityProperties;
import org.springframework.stereotype.Service;

@Service("accessTokenService")
public class AccessTokenService extends AbstractTokenService {

    public static final Long ACCESS_TOKEN_EXPIRES_IN = 120L;

    public AccessTokenService(
            SecurityProperties securityProperties
    ) {
        super(securityProperties.issuer(),"access", securityProperties);
    }
}
