package com.azas.domain.child.controller;

import com.azas.domain.child.dto.ChildListResponse;
import com.azas.domain.child.dto.ChildResponse;
import com.azas.domain.child.dto.ChildSummaryResponse;
import com.azas.domain.child.entity.BirthStatus;
import com.azas.domain.child.entity.Gender;
import com.azas.domain.child.entity.RelationType;
import com.azas.domain.child.service.ChildService;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ChildControllerTest {

    private static final Long MEMBER_ID = 7L;
    private static final Long CHILD_ID = 3L;

    private static final String AUTHORIZATION =
            "Bearer access-token";

    @Mock
    private ChildService childService;

    @Mock
    private AccessTokenMemberResolver
            accessTokenMemberResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChildController controller =
                new ChildController(
                        childService,
                        accessTokenMemberResolver
                );

        ObjectMapper objectMapper =
                new ObjectMapper()
                        .registerModule(
                                new JavaTimeModule()
                        )
                        .disable(
                                SerializationFeature
                                        .WRITE_DATES_AS_TIMESTAMPS
                        );

        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(
                                objectMapper
                        )
                )
                .build();
    }

    @Test
    void createsExpectedChild() throws Exception {
        ChildResponse response =
                childResponse(
                        CHILD_ID,
                        "깨비",
                        BirthStatus.EXPECTED
                );

        when(accessTokenMemberResolver.resolveMemberId(
                AUTHORIZATION
        )).thenReturn(MEMBER_ID);

        when(childService.createChild(
                org.mockito.ArgumentMatchers.eq(MEMBER_ID),
                any()
        )).thenReturn(response);

        String body =
                "{"
                        + "\"name\":\"깨비\","
                        + "\"birth_status\":\"EXPECTED\","
                        + "\"expected_birth_date\":\"2027-01-30\","
                        + "\"gender\":\"UNKNOWN\","
                        + "\"relation_type\":\"MOTHER\""
                        + "}";

        mockMvc.perform(
                        post("/api/v1/children")
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.child_id")
                                .value(CHILD_ID)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("깨비")
                )
                .andExpect(
                        jsonPath("$.birth_status")
                                .value("EXPECTED")
                )
                .andExpect(
                        jsonPath("$.expected_birth_date")
                                .value("2027-01-30")
                );

        verify(childService).createChild(
                org.mockito.ArgumentMatchers.eq(MEMBER_ID),
                any()
        );
    }

    @Test
    void getsChildren() throws Exception {
        ChildSummaryResponse item =
                childSummary();

        when(accessTokenMemberResolver.resolveMemberId(
                AUTHORIZATION
        )).thenReturn(MEMBER_ID);

        when(childService.getChildren(MEMBER_ID))
                .thenReturn(
                        new ChildListResponse(
                                List.of(item)
                        )
                );

        mockMvc.perform(
                        get("/api/v1/children")
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.items")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.items[0].child_id")
                                .value(CHILD_ID)
                )
                .andExpect(
                        jsonPath("$.items[0].name")
                                .value("깨비")
                );
    }

    @Test
    void getsChildDetail() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                AUTHORIZATION
        )).thenReturn(MEMBER_ID);

        when(childService.getChild(
                MEMBER_ID,
                CHILD_ID
        )).thenReturn(
                childResponse(
                        CHILD_ID,
                        "깨비",
                        BirthStatus.EXPECTED
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/children/{childId}",
                                CHILD_ID
                        )
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.child_id")
                                .value(CHILD_ID)
                )
                .andExpect(
                        jsonPath("$.relation_type")
                                .value("MOTHER")
                );
    }

    @Test
    void updatesChild() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                AUTHORIZATION
        )).thenReturn(MEMBER_ID);

        when(childService.updateChild(
                org.mockito.ArgumentMatchers.eq(MEMBER_ID),
                org.mockito.ArgumentMatchers.eq(CHILD_ID),
                any()
        )).thenReturn(
                childResponse(
                        CHILD_ID,
                        "새로운 태명",
                        BirthStatus.EXPECTED
                )
        );

        String body =
                "{"
                        + "\"name\":\"새로운 태명\","
                        + "\"expected_birth_date\":\"2027-02-05\""
                        + "}";

        mockMvc.perform(
                        patch(
                                "/api/v1/children/{childId}",
                                CHILD_ID
                        )
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                                .contentType(
                                        "application/json"
                                )
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.child_id")
                                .value(CHILD_ID)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("새로운 태명")
                );
    }

    @Test
    void deletesChild() throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                AUTHORIZATION
        )).thenReturn(MEMBER_ID);

        mockMvc.perform(
                        delete(
                                "/api/v1/children/{childId}",
                                CHILD_ID
                        )
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                )
                .andExpect(status().isNoContent());

        verify(childService).deleteChild(
                MEMBER_ID,
                CHILD_ID
        );
    }

    @Test
    void returnsNotFoundWhenChildIsNotAccessible()
            throws Exception {
        when(accessTokenMemberResolver.resolveMemberId(
                AUTHORIZATION
        )).thenReturn(MEMBER_ID);

        when(childService.getChild(
                MEMBER_ID,
                CHILD_ID
        )).thenThrow(
                new BusinessException(
                        ErrorCode.CHILD_NOT_FOUND
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/children/{childId}",
                                CHILD_ID
                        )
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("CHILD_NOT_FOUND")
                );
    }

    private ChildResponse childResponse(
            Long childId,
            String name,
            BirthStatus birthStatus
    ) {
        ChildResponse response =
                new ChildResponse();

        ReflectionTestUtils.setField(
                response,
                "childId",
                childId
        );
        ReflectionTestUtils.setField(
                response,
                "name",
                name
        );
        ReflectionTestUtils.setField(
                response,
                "birthStatus",
                birthStatus
        );
        ReflectionTestUtils.setField(
                response,
                "expectedBirthDate",
                LocalDate.of(2027, 1, 30)
        );
        ReflectionTestUtils.setField(
                response,
                "gender",
                Gender.UNKNOWN
        );
        ReflectionTestUtils.setField(
                response,
                "relationType",
                RelationType.MOTHER
        );

        return response;
    }

    private ChildSummaryResponse childSummary() {
        ChildSummaryResponse response =
                new ChildSummaryResponse();

        ReflectionTestUtils.setField(
                response,
                "childId",
                CHILD_ID
        );
        ReflectionTestUtils.setField(
                response,
                "name",
                "깨비"
        );
        ReflectionTestUtils.setField(
                response,
                "birthStatus",
                BirthStatus.EXPECTED
        );
        ReflectionTestUtils.setField(
                response,
                "expectedBirthDate",
                LocalDate.of(2027, 1, 30)
        );
        ReflectionTestUtils.setField(
                response,
                "relationType",
                RelationType.MOTHER
        );

        return response;
    }
}