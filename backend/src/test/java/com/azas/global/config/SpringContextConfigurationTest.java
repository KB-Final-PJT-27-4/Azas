package com.azas.global.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpringContextConfigurationTest {

    @Test
    void rootContextLoads() {
        try (ClassPathXmlApplicationContext rootContext = createRootContext()) {
            assertNotNull(rootContext.getBean(DataSource.class));
            assertNotNull(rootContext.getBean(SqlSessionFactory.class));
        }
    }

    @Test
    void servletContextLoads() {
        try (XmlWebApplicationContext servletContext = new XmlWebApplicationContext()) {
            servletContext.setServletContext(new MockServletContext());
            servletContext.setConfigLocation("classpath:spring/servlet-context.xml");
            servletContext.refresh();

            assertNotNull(servletContext.getBean(RequestMappingHandlerMapping.class));
        }
    }

    private ClassPathXmlApplicationContext createRootContext() {
        ClassPathXmlApplicationContext rootContext = new ClassPathXmlApplicationContext();
        rootContext.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("testDatabaseProperties", Map.of(
                        "DB_URL", "jdbc:mysql://localhost:3306/azas",
                        "DB_USERNAME", "test_user",
                        "DB_PASSWORD", "test_password"
                ))
        );
        rootContext.setConfigLocation("spring/root-context.xml");
        rootContext.refresh();
        return rootContext;
    }
}
