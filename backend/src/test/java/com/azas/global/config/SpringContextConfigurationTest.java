package com.azas.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpringContextConfigurationTest {

    @Test
    void rootContextLoads() {
        try (ClassPathXmlApplicationContext rootContext =
                     new ClassPathXmlApplicationContext("spring/root-context.xml")) {
            assertNotNull(rootContext);
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
}
