package com.azas.domain.family.controller;

import com.azas.domain.auth.entity.FamilyInviteeType;
import com.azas.domain.family.dto.FamilyInvitationInfoResponse;
import com.azas.domain.family.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

@Controller
@RequiredArgsConstructor
public class FamilyInvitationShareController {

    private static final String SHARE_TITLE =
            "아자스 | 부모와 자녀가 함께 만드는 미래 자산";

    private final FamilyService familyService;

    @Value("${FAMILY_INVITATION_URL_BASE:http://localhost:5173/family-invitations}")
    private String familyInvitationUrlBase;

    @Value("${FAMILY_INVITATION_SHARE_URL_BASE:http://localhost:8080/family-invitations}")
    private String familyInvitationShareUrlBase;

    @GetMapping(
            value = "/family-invitations/{invite_token}/share",
            produces = MediaType.TEXT_HTML_VALUE
    )
    @ResponseBody
    public String getInvitationSharePreview(
            @PathVariable("invite_token") String inviteToken
    ) {
        FamilyInvitationInfoResponse invitation =
                familyService.getFamilyInvitationInfo(inviteToken);

        String invitationUrl = buildInvitationUrl(inviteToken);
        String shareUrl = buildShareUrl(inviteToken);
        String previewImageUrl = buildPreviewImageUrl();
        String description = buildInvitationDescription(
                invitation.getInviterName(),
                invitation.getInviteeType()
        );

        return """
                <!doctype html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8">
                  <meta property="og:type" content="website">
                  <meta property="og:site_name" content="아자스">
                  <meta property="og:title" content="%s">
                  <meta property="og:description" content="%s">
                  <meta property="og:image" content="%s">
                  <meta property="og:url" content="%s">
                  <meta name="twitter:card" content="summary_large_image">
                  <meta name="twitter:title" content="%s">
                  <meta name="twitter:description" content="%s">
                  <meta name="twitter:image" content="%s">
                  <meta http-equiv="refresh" content="0; url=%s">
                  <title>%s</title>
                  <script>window.location.replace(%s);</script>
                </head>
                <body>
                  <a href="%s">초대 페이지로 이동</a>
                </body>
                </html>
                """.formatted(
                escape(SHARE_TITLE),
                escape(description),
                escape(previewImageUrl),
                escape(shareUrl),
                escape(SHARE_TITLE),
                escape(description),
                escape(previewImageUrl),
                escape(invitationUrl),
                escape(SHARE_TITLE),
                toJavaScriptString(invitationUrl),
                escape(invitationUrl)
        );
    }

    private String buildInvitationDescription(
            String inviterName,
            FamilyInviteeType inviteeType
    ) {
        String inviteeName = inviteeType == FamilyInviteeType.PARENT
                ? "보호자"
                : "자녀";

        return inviterName + "님이 당신을 " + inviteeName + "로 초대했어요!";
    }

    private String buildInvitationUrl(String inviteToken) {
        return familyInvitationUrlBase.replaceAll("/+$", "")
                + "/"
                + inviteToken;
    }

    private String buildShareUrl(String inviteToken) {
        return familyInvitationShareUrlBase.replaceAll("/+$", "")
                + "/"
                + inviteToken
                + "/share";
    }

    private String buildPreviewImageUrl() {
        String frontendBaseUrl = familyInvitationUrlBase
                .replaceFirst("/family-invitations/?$", "");

        return frontendBaseUrl.replaceAll("/+$", "")
                + "/pwa-512x512.png";
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }

    private String toJavaScriptString(String value) {
        return "'" + value
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                + "'";
    }
}
