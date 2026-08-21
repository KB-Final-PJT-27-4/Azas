package com.azas.domain.notification.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberPushRequestedEvent {

    private final Long memberId;
    private final PushMessage pushMessage;
}
