package com.azas.domain.auth.service;

import com.azas.domain.auth.entity.OAuthProvider;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuthClientRegistry {

    private final Map<OAuthProvider, OAuthClient> oauthClients;

    public OAuthClientRegistry(List<OAuthClient> oauthClients) {
        this.oauthClients = oauthClients.stream()
                .collect(Collectors.toUnmodifiableMap(
                        OAuthClient::getProvider,
                        Function.identity()
                ));
    }

    public OAuthClient get(OAuthProvider provider) {
        OAuthClient oauthClient = oauthClients.get(provider);

        if (oauthClient == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        return oauthClient;
    }
}
