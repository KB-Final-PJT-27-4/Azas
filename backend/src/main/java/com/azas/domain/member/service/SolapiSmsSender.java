package com.azas.domain.member.service;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;

import java.util.Objects;

/** Solapi를 통해 휴대폰 인증번호를 발송하는 구현체입니다. */
public class SolapiSmsSender implements SmsSender {

    private static final String MESSAGE_PREFIX =
            "[아자스] 휴대폰 인증번호는 ";
    private static final String MESSAGE_SUFFIX =
            "입니다. 3분 이내에 입력해주세요.";

    private final SolapiMessageClient messageClient;
    private final String senderNumber;

    public SolapiSmsSender(
            String apiKey,
            String apiSecret,
            String senderNumber
    ) {
        this(createMessageClient(apiKey, apiSecret), senderNumber);
    }

    SolapiSmsSender(
            SolapiMessageClient messageClient,
            String senderNumber
    ) {
        this.messageClient = Objects.requireNonNull(messageClient);
        this.senderNumber = requireText(senderNumber, "senderNumber")
                .replaceAll("[^0-9]", "");
    }

    @Override
    public void sendVerificationCode(
            String phoneNumber,
            String verificationCode
    ) {
        Message message = new Message();
        message.setFrom(senderNumber);
        message.setTo(requireText(phoneNumber, "phoneNumber")
                .replaceAll("[^0-9]", ""));
        message.setText(
                MESSAGE_PREFIX
                        + requireText(verificationCode, "verificationCode")
                        + MESSAGE_SUFFIX
        );

        try {
            messageClient.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Solapi SMS 발송 요청에 실패했습니다.",
                    exception
            );
        }
    }

    private static SolapiMessageClient createMessageClient(
            String apiKey,
            String apiSecret
    ) {
        DefaultMessageService messageService =
                SolapiClient.INSTANCE.createInstance(
                        requireText(apiKey, "apiKey"),
                        requireText(apiSecret, "apiSecret")
                );
        return message -> messageService.send(message, null);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " 값은 필수입니다."
            );
        }
        return value;
    }

    @FunctionalInterface
    interface SolapiMessageClient {

        void send(Message message) throws Exception;
    }
}
