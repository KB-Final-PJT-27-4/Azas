package com.azas.domain.family.controller;

import com.azas.domain.auth.entity.FamilyInvitationStatus;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.azas.domain.family.dto.FamilyInvitationChildResponse;
import com.azas.domain.family.dto.FamilyInvitationInfoResponse;
import com.azas.domain.family.service.FamilyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyInvitationShareControllerTest {

    @Mock
    private FamilyService familyService;

    @InjectMocks
    private FamilyInvitationShareController controller;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                controller,
                "familyInvitationUrlBase",
                "https://azas-seven.vercel.app/family-invitations"
        );
        ReflectionTestUtils.setField(
                controller,
                "familyInvitationShareUrlBase",
                "https://api.example.com/family-invitations"
        );
    }

    @Test
    void rendersGuardianInvitationOpenGraphAndRedirect() {
        when(familyService.getFamilyInvitationInfo("invite-token"))
                .thenReturn(invitation("김&lt;하나", FamilyInviteeType.PARENT));

        ResponseEntity<String> response =
                controller.getInvitationSharePreview("invite-token");
        String html = response.getBody();

        assertEquals("text/html;charset=UTF-8", response.getHeaders().getContentType().toString());
        assertTrue(html.contains(
                "아자스 | 부모와 자녀가 함께 만드는 미래 자산"
        ));
        assertTrue(html.contains("김&amp;lt;하나님이 당신을 보호자로 초대했어요!"));
        assertTrue(html.contains("https://azas-seven.vercel.app/pwa-512x512.png"));
        assertTrue(html.contains(
                "https://api.example.com/family-invitations/invite-token/share"
        ));
        assertTrue(html.contains(
                "https://azas-seven.vercel.app/family-invitations/invite-token"
        ));
    }

    @Test
    void rendersChildInvitationOpenGraph() {
        when(familyService.getFamilyInvitationInfo("child-token"))
                .thenReturn(invitation("송준수", FamilyInviteeType.CHILD));

        String html = controller.getInvitationSharePreview("child-token").getBody();

        assertTrue(html.contains("송준수님이 당신을 자녀로 초대했어요!"));
        assertFalse(html.contains("<title>송준수님이 당신을 자녀로 초대했어요!"));
    }

    private FamilyInvitationInfoResponse invitation(
            String inviterName,
            FamilyInviteeType inviteeType
    ) {
        return new FamilyInvitationInfoResponse(
                "깨비",
                List.of(new FamilyInvitationChildResponse(6L, "깨비")),
                1,
                inviterName,
                inviteeType,
                FamilyInvitationStatus.PENDING,
                Instant.parse("2026-08-25T00:00:00Z")
        );
    }
}
