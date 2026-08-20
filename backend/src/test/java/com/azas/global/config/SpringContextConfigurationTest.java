package com.azas.global.config;

import com.azas.domain.member.service.FakeSmsSender;
import com.azas.domain.member.service.PhoneNumberProtector;
import com.azas.domain.member.service.PhoneVerificationHasher;
import com.azas.domain.member.service.SmsSender;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import springfox.documentation.spring.web.plugins.Docket;

import javax.sql.DataSource;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SpringContextConfigurationTest {

    @Test
    void rootContextLoads() {
        try (ClassPathXmlApplicationContext rootContext = createRootContext()) {
            assertNotNull(rootContext.getBean(DataSource.class));
            assertNotNull(rootContext.getBean(SqlSessionFactory.class));
            assertNotNull(
                    rootContext.getBean(
                            PhoneNumberProtector.class
                    )
            );
            assertNotNull(
                    rootContext.getBean(
                            PhoneVerificationHasher.class
                    )
            );
            assertNotNull(
                    rootContext.getBean(SmsSender.class)
            );
        }
    }

    @Test
    void demoProfileUsesFakeSmsSender() {
        try (
                ClassPathXmlApplicationContext rootContext =
                        createRootContext("demo")
        ) {
            assertInstanceOf(
                    FakeSmsSender.class,
                    rootContext.getBean(SmsSender.class)
            );
        }
    }

    @Test
    void servletContextLoads() throws Exception {
        try (
                ClassPathXmlApplicationContext rootContext =
                        createRootContext();
                XmlWebApplicationContext servletContext =
                        new XmlWebApplicationContext()
        ) {
            servletContext.setParent(rootContext);
            servletContext.setServletContext(
                    new MockServletContext()
            );
            servletContext.setConfigLocation(
                    "classpath:spring/servlet-context.xml"
            );
            servletContext.refresh();

            assertNotNull(
                    servletContext.getBean(
                            RequestMappingHandlerMapping.class
                    )
            );
            assertNotNull(
                    servletContext.getBean(Docket.class)
            );

            MockMvc mockMvc =
                    MockMvcBuilders
                            .webAppContextSetup(servletContext)
                            .build();

            mockMvc.perform(get("/v2/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.swagger")
                                    .value("2.0")
                    )
                    .andExpect(
                            jsonPath("$.info.title")
                                    .value("Azas Backend API")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.paths['/api/v1/auth/oauth/{provider}']"
                                            + ".post.tags[0]"
                            ).value("인증")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.paths['/api/v1/auth/oauth/{provider}']"
                                            + ".post.summary"
                            ).value("소셜 로그인/회원가입")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.paths['/api/v1/auth/oauth/{provider}']"
                                            + ".post.responses['200'].description"
                            ).value("로그인 또는 회원가입 성공")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.paths['/api/v1/auth/oauth/{provider}']"
                                            + ".post.responses['400'].description"
                            ).value("요청값 오류 또는 지원하지 않는 제공자")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.paths['/api/v1/auth/oauth/{provider}']"
                                            + ".post.responses['401'].description"
                            ).value("만료되었거나 유효하지 않은 인가 코드")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.paths['/api/v1/auth/oauth/{provider}']"
                                            + ".post.responses['502'].description"
                            ).value("소셜 제공자 통신 실패")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.paths['/api/v1/auth/oauth/{provider}']"
                                            + ".post.responses['201']"
                            ).doesNotExist()
                    )
                    .andExpect(
                            jsonPath(
                                    "$.definitions.OAuthLoginRequest"
                                            + ".properties.authorization_code"
                                            + ".description"
                            ).value("소셜 제공자가 발급한 일회용 인가 코드")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.definitions.OAuthLoginResponse"
                                            + ".properties.is_new_member"
                            ).exists()
                    )
                    .andExpect(
                            jsonPath(
                                    "$.definitions.OAuthLoginMemberResponse"
                                            + ".properties.member_type"
                            ).exists()
                    );

            mockMvc.perform(get("/v2/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.paths['/api/v1/auth/token/refresh']"
                                            + ".post.tags[0]"
                            ).value("인증")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.paths['/api/v1/auth/token/refresh']"
                                            + ".post.summary"
                            ).value("Access Token 재발급")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.paths['/api/v1/auth/token/refresh']"
                                            + ".post.responses['200'].description"
                            ).value("토큰 재발급 성공")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.paths['/api/v1/auth/token/refresh']"
                                            + ".post.responses['400'].description"
                            ).value("Refresh Token 누락 또는 빈 값")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.paths['/api/v1/auth/token/refresh']"
                                            + ".post.responses['401'].description"
                            ).value("유효하지 않거나 만료·폐기된 Refresh Token")
                    )
                    .andExpect(
                            jsonPath(
                                    "$.definitions.TokenRefreshRequest"
                                            + ".properties.refresh_token"
                            ).exists()
                    )
                    .andExpect(
                            jsonPath(
                                    "$.definitions.TokenRefreshResponse"
                                            + ".properties.access_token"
                            ).exists()
                    );

            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        }
    }

    @Test
    void corsConfigurationHandlesApiPreflightRequests() throws Exception {
        try (
                ClassPathXmlApplicationContext rootContext =
                        createRootContext();
                XmlWebApplicationContext servletContext =
                        new XmlWebApplicationContext()
        ) {
            servletContext.setParent(rootContext);
            servletContext.setServletContext(
                    new MockServletContext()
            );
            servletContext.setConfigLocation(
                    "classpath:spring/servlet-context.xml"
            );
            servletContext.refresh();

            MockMvc mockMvc =
                    MockMvcBuilders
                            .webAppContextSetup(servletContext)
                            .build();

            assertAllowedPreflight(
                    mockMvc,
                    "https://azas-seven.vercel.app"
            );
            assertAllowedPreflight(
                    mockMvc,
                    "http://localhost:5173"
            );

            mockMvc.perform(
                            options("/api/v1/members/me")
                                    .header(
                                            HttpHeaders.ORIGIN,
                                            "https://untrusted.example.com"
                                    )
                                    .header(
                                            HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                            "GET"
                                    )
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(
                            header().doesNotExist(
                                    HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
                            )
                    );
        }
    }

    private void assertAllowedPreflight(
            MockMvc mockMvc,
            String origin
    ) throws Exception {
        mockMvc.perform(
                        options("/api/v1/members/me")
                                .header(HttpHeaders.ORIGIN, origin)
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                        "GET"
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                        "Authorization,Content-Type"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                                origin
                        )
                )
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                                org.hamcrest.Matchers.containsString("GET")
                        )
                )
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                                org.hamcrest.Matchers.containsString(
                                        "Authorization"
                                )
                        )
                );
    }

    @Test
    // [JMG] CAPSULE-1~3 API 응답의 날짜·시간이 ISO-8601 문자열로 변환되는지 검증한다.
    void servletContextSerializesJavaTimeAsIso8601() throws Exception {
        try (
                ClassPathXmlApplicationContext rootContext =
                        createRootContext();
                XmlWebApplicationContext servletContext =
                        new XmlWebApplicationContext()
        ) {
            servletContext.setParent(rootContext);
            servletContext.setServletContext(
                    new MockServletContext()
            );
            servletContext.setConfigLocation(
                    "classpath:spring/servlet-context.xml"
            );
            servletContext.refresh();

            RequestMappingHandlerAdapter handlerAdapter =
                    servletContext.getBean(
                            RequestMappingHandlerAdapter.class
                    );

            MappingJackson2HttpMessageConverter jacksonConverter =
                    handlerAdapter.getMessageConverters().stream()
                            .filter(
                                    MappingJackson2HttpMessageConverter.class
                                            ::isInstance
                            )
                            .map(
                                    MappingJackson2HttpMessageConverter.class
                                            ::cast
                            )
                            .findFirst()
                            .orElseThrow();

            String responseBody = jacksonConverter.getObjectMapper()
                    .writeValueAsString(
                            Map.of(
                                    "created_at",
                                    LocalDateTime.of(
                                            2026,
                                            8,
                                            4,
                                            13,
                                            24,
                                            10
                                    )
                            )
                    );

            assertEquals(
                    "{\"created_at\":\"2026-08-04T13:24:10\"}",
                    responseBody
            );
        }
    }

    private ClassPathXmlApplicationContext createRootContext(
            String... activeProfiles
    ) {
        ClassPathXmlApplicationContext rootContext = new ClassPathXmlApplicationContext();
        rootContext.getEnvironment()
                .setActiveProfiles(activeProfiles);
        rootContext.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("testProperties", Map.of(
                        "DB_URL", "jdbc:mysql://localhost:3306/azas",
                        "DB_USERNAME", "test_user",
                        "DB_PASSWORD", "test_password",
                        "KAKAO_CLIENT_ID", "test-kakao-client-id",
                        "GOOGLE_CLIENT_ID", "test-google-client-id",
                        "GOOGLE_CLIENT_SECRET", "test-google-client-secret",
                        "JWT_SECRET_BASE64",
                        "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
                        "PHONE_NUMBER_ENCRYPTION_KEY_BASE64",
                        "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                        "PHONE_VERIFICATION_SECRET_BASE64",
                        "YWJjZGVmMDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODk="
                ))
        );
        rootContext.setConfigLocation("spring/root-context.xml");
        rootContext.refresh();
        return rootContext;
    }
}
