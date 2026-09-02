package com.azas.domain.family.controller;

import com.azas.domain.auth.entity.FamilyInvitationStatus;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.azas.domain.child.entity.RelationType;
import com.azas.domain.family.dto.FamilyInvitationAcceptResponse;
import com.azas.domain.family.dto.FamilyInvitationChildResponse;
import com.azas.domain.family.dto.FamilyInvitationCreateResponse;
import com.azas.domain.family.dto.FamilyInvitationInfoResponse;
import com.azas.domain.family.service.FamilyService;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import com.azas.global.exception.GlobalExceptionHandler;
import com.azas.global.security.AccessTokenMemberResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FamilyInvitationControllerTest {

    @Mock
    private FamilyService familyService;

    @Mock
    private AccessTokenMemberResolver accessTokenMemberResolver;

    @InjectMocks
    private FamilyController familyController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(familyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    void createsParentInvitation() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .thenReturn(7L);
        when(familyService.createParentFamilyInvitation(eq(7L), any()))
                .thenReturn(createResponse(
                        30L,
                        FamilyInviteeType.PARENT,
                        "parent-token"
                ));

        mockMvc.perform(post("/api/v1/family-invitations")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "expires_in_hours": 24
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.family_invitation_id").value(30))
                .andExpect(jsonPath("$.invitee_type").value("PARENT"))
                .andExpect(jsonPath("$.child_count").value(1))
                .andExpect(jsonPath("$.children[0].child_id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(familyService).createParentFamilyInvitation(eq(7L), any());
    }

    @Test
    void createsChildInvitation() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .thenReturn(7L);
        when(familyService.createChildFamilyInvitation(eq(7L), eq(10L), any()))
                .thenReturn(createResponse(
                        31L,
                        FamilyInviteeType.CHILD,
                        "child-token"
                ));

        mockMvc.perform(post("/api/v1/children/{childId}/family-invitations", 10L)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "expires_in_hours": 24
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.family_invitation_id").value(31))
                .andExpect(jsonPath("$.invitee_type").value("CHILD"));
    }

    @Test
    void returnsBadRequestWhenExpirationHoursIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/children/{childId}/family-invitations", 10L)
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expires_in_hours\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BADREQUEST"));

        verifyNoInteractions(accessTokenMemberResolver, familyService);
    }

    @Test
    void returnsUnauthorizedWhenCreatingInvitationWithoutAccessToken()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(ErrorCode.ACCESS_TOKEN_REQUIRED));

        mockMvc.perform(post("/api/v1/family-invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(familyService);
    }

    @Test
    void returnsInvitationPreviewWithoutAuthentication() throws Exception {
        when(familyService.getFamilyInvitationInfo("raw-token"))
                .thenReturn(new FamilyInvitationInfoResponse(
                        "깨비",
                        List.of(new FamilyInvitationChildResponse(10L, "깨비")),
                        1,
                        "김하나",
                        FamilyInviteeType.CHILD,
                        FamilyInvitationStatus.PENDING,
                        Instant.parse("2026-08-07T01:00:00Z")
                ));

        mockMvc.perform(get("/api/v1/family-invitations/{token}", "raw-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.child_name").value("깨비"))
                .andExpect(jsonPath("$.child_count").value(1))
                .andExpect(jsonPath("$.children[0].child_id").value(10))
                .andExpect(jsonPath("$.invitee_type").value("CHILD"));

        verifyNoInteractions(accessTokenMemberResolver);
    }

    @Test
    void returnsNotFoundForMissingInvitationPreview() throws Exception {
        when(familyService.getFamilyInvitationInfo("missing-token"))
                .thenThrow(new BusinessException(
                        ErrorCode.FAMILY_INVITATION_NOT_FOUND
                ));

        mockMvc.perform(get("/api/v1/family-invitations/{token}", "missing-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code")
                        .value("FAMILY_INVITATION_NOT_FOUND"));
    }

    @Test
    void returnsGoneForExpiredOrAcceptedInvitationPreview() throws Exception {
        when(familyService.getFamilyInvitationInfo("gone-token"))
                .thenThrow(new BusinessException(ErrorCode.FAMILY_INVITATION_GONE));

        mockMvc.perform(get("/api/v1/family-invitations/{token}", "gone-token"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error.code")
                        .value("FAMILY_INVITATION_GONE"));
    }

    @Test
    void returnsGoneWhenAcceptingExpiredOrAcceptedInvitation() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .thenReturn(8L);
        when(familyService.acceptFamilyInvitation(
                eq(8L), eq("gone-token"), any()
        )).thenThrow(new BusinessException(ErrorCode.FAMILY_INVITATION_GONE));

        mockMvc.perform(post("/api/v1/family-invitations/{token}/accept", "gone-token")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"relation_type\":\"FATHER\"}"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error.code")
                        .value("FAMILY_INVITATION_GONE"));
    }

    @Test
    void acceptsParentInvitation() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .thenReturn(8L);
        when(familyService.acceptFamilyInvitation(
                eq(8L), eq("parent-token"), any()
        )).thenReturn(acceptResponse(
                FamilyInviteeType.PARENT,
                RelationType.FATHER
        ));

        mockMvc.perform(post("/api/v1/family-invitations/{token}/accept", "parent-token")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"relation_type\":\"FATHER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.invitee_type").value("PARENT"))
                .andExpect(jsonPath("$.relation_type").value("FATHER"))
                .andExpect(jsonPath("$.child.child_id").value(10))
                .andExpect(jsonPath("$.child_count").value(1))
                .andExpect(jsonPath("$.children[0].child_id").value(10));
    }

    @Test
    void acceptsChildInvitationWithEmptyRequestBody() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId("Bearer child-token"))
                .thenReturn(9L);
        when(familyService.acceptFamilyInvitation(
                eq(9L), eq("child-token"), any()
        )).thenReturn(acceptResponse(FamilyInviteeType.CHILD, null));

        mockMvc.perform(post("/api/v1/family-invitations/{token}/accept", "child-token")
                        .header("Authorization", "Bearer child-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invitee_type").value("CHILD"))
                .andExpect(jsonPath("$.relation_type").value(nullValue()));
    }

    @Test
    void returnsBadRequestWhenParentInvitationHasNoRelationType()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .thenReturn(8L);
        when(familyService.acceptFamilyInvitation(
                eq(8L), eq("parent-token"), any()
        )).thenThrow(new BusinessException(
                ErrorCode.FAMILY_INVITATION_RELATION_TYPE_REQUIRED
        ));

        mockMvc.perform(post("/api/v1/family-invitations/{token}/accept", "parent-token")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code")
                        .value("FAMILY_INVITATION_RELATION_TYPE_REQUIRED"));
    }

    @Test
    void returnsUnauthorizedWhenAcceptingInvitationWithoutAccessToken()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(null))
                .thenThrow(new BusinessException(ErrorCode.ACCESS_TOKEN_REQUIRED));

        mockMvc.perform(post("/api/v1/family-invitations/{token}/accept", "raw-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code")
                        .value("ACCESS_TOKEN_REQUIRED"));

        verifyNoInteractions(familyService);
    }

    private FamilyInvitationCreateResponse createResponse(
            Long invitationId,
            FamilyInviteeType inviteeType,
            String token
    ) {
        return new FamilyInvitationCreateResponse(
                invitationId,
                10L,
                List.of(new FamilyInvitationChildResponse(10L, "깨비")),
                1,
                inviteeType,
                token,
                "http://localhost:5173/family-invitations/" + token,
                FamilyInvitationStatus.PENDING,
                Instant.parse("2026-08-07T01:00:00Z"),
                Instant.parse("2026-08-06T01:00:00Z")
        );
    }

    private FamilyInvitationAcceptResponse acceptResponse(
            FamilyInviteeType inviteeType,
            RelationType relationType
    ) {
        return new FamilyInvitationAcceptResponse(
                30L,
                FamilyInvitationStatus.ACCEPTED,
                inviteeType,
                Instant.parse("2026-08-06T01:05:00Z"),
                new FamilyInvitationChildResponse(10L, "깨비"),
                List.of(new FamilyInvitationChildResponse(10L, "깨비")),
                1,
                relationType
        );
    }
}
