package com.azas.domain.member.service;

public interface SmsSender {

    void sendVerificationCode(
            String phoneNumber,
            String verificationCode
    );
}