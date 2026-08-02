package com.azas.global.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import springfox.documentation.spring.web.plugins.Docket;

import javax.sql.DataSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SpringContextConfigurationTest {

    @Test
    void rootContextLoads() {
        try (ClassPathXmlApplicationContext rootContext = createRootContext()) {
            assertNotNull(rootContext.getBean(DataSource.class));
            assertNotNull(rootContext.getBean(SqlSessionFactory.class));
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
                    );

            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isOk());
        }
    }

    private ClassPathXmlApplicationContext createRootContext() {
        ClassPathXmlApplicationContext rootContext = new ClassPathXmlApplicationContext();
        rootContext.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("testProperties", Map.of(
                        "DB_URL", "jdbc:mysql://localhost:3306/azas",
                        "DB_USERNAME", "test_user",
                        "DB_PASSWORD", "test_password",
                        "KAKAO_CLIENT_ID", "test-kakao-client-id",
                        "GOOGLE_CLIENT_ID", "test-google-client-id",
                        "GOOGLE_CLIENT_SECRET", "test-google-client-secret",
                        "JWT_SECRET_BASE64",
                        "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
                ))
        );
        rootContext.setConfigLocation("spring/root-context.xml");
        rootContext.refresh();
        return rootContext;
    }
}
