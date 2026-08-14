package com.azas.domain.family.controller;

import com.azas.domain.auth.entity.FamilyInvitationStatus;
import com.azas.domain.auth.entity.FamilyInviteeType;
import com.azas.domain.child.entity.RelationType;
import com.azas.domain.family.dto.AllowanceRequestResponse;
import com.azas.domain.family.dto.ChildMemberLinkResponse;
import com.azas.domain.family.dto.FamilyGuardianListResponse;
import com.azas.domain.family.dto.FamilyGuardianResponse;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class FamilyControllerTest {


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

        mockMvc = MockMvcBuilders
                .standaloneSetup(familyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    void getFamilyMembersReturnsGuardianList() throws Exception {
        FamilyGuardianResponse guardian = guardian(
                8L,
                "김엄마",
                RelationType.MOTHER
        );

        given(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .willReturn(7L);
        given(familyService.getFamilyMembers(7L, 10L))
                .willReturn(new FamilyGuardianListResponse(List.of(guardian)));

        mockMvc.perform(
                        get("/api/v1/children/{child_id}/family-members", 10L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].member_id").value(8))
                .andExpect(jsonPath("$.items[0].name").value("김엄마"))
                .andExpect(jsonPath("$.items[0].relation_type").value("MOTHER"));
    }

    @Test
    void getChildMemberLinkReturnsLinkStatus() throws Exception {
        ChildMemberLinkResponse response = childMemberLink(
                10L,
                false,
                null
        );

        given(accessTokenMemberResolver.resolveMemberId("Bearer access-token"))
                .willReturn(7L);
        given(familyService.getChildMemberLink(7L, 10L))
                .willReturn(response);

        mockMvc.perform(
                        get("/api/v1/children/{child_id}/member-link", 10L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.child_id").value(10))
                .andExpect(jsonPath("$.linked").value(false))
                .andExpect(jsonPath("$.child_member_id").doesNotExist());
    }


    private FamilyGuardianResponse guardian(
            Long memberId,
            String name,
            RelationType relationType
    ) {
        FamilyGuardianResponse response = new FamilyGuardianResponse();
        ReflectionTestUtils.setField(response, "memberId", memberId);
        ReflectionTestUtils.setField(response, "name", name);
        ReflectionTestUtils.setField(response, "email", "parent@example.com");
        ReflectionTestUtils.setField(response, "relationType", relationType);
        ReflectionTestUtils.setField(response, "linkedAt",
                LocalDateTime.of(2026, 8, 1, 9, 0));
        return response;
    }

    private ChildMemberLinkResponse childMemberLink(
            Long childId,
            boolean linked,
            Long childMemberId
    ) {
        ChildMemberLinkResponse response = new ChildMemberLinkResponse();
        ReflectionTestUtils.setField(response, "childId", childId);
        ReflectionTestUtils.setField(response, "linked", linked);
        ReflectionTestUtils.setField(response, "childMemberId", childMemberId);
        return response;
    }
}

