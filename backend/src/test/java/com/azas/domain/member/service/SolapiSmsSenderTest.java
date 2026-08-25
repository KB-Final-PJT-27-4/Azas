package com.azas.domain.member.service;

import com.solapi.sdk.message.model.Message;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolapiSmsSenderTest {

    @Test
    void sendsNormalizedPhoneNumbersAndKoreanVerificationMessage() {
        AtomicReference<Message> sentMessage = new AtomicReference<>();
        SolapiSmsSender smsSender = new SolapiSmsSender(
                sentMessage::set,
                "010-9876-5432"
        );

        smsSender.sendVerificationCode("010-1234-5678", "123456");

        Message message = sentMessage.get();
        assertEquals("01098765432", message.getFrom());
        assertEquals("01012345678", message.getTo());
        assertEquals(
                "[아자스] 휴대폰 인증번호는 123456입니다. 3분 이내에 입력해주세요.",
                message.getText()
        );
    }

    @Test
    void wrapsProviderFailure() {
        SolapiSmsSender smsSender = new SolapiSmsSender(
                message -> {
                    throw new IllegalStateException("provider failure");
                },
                "01098765432"
        );

        assertThrows(
                IllegalStateException.class,
                () -> smsSender.sendVerificationCode("01012345678", "123456")
        );
    }
}
