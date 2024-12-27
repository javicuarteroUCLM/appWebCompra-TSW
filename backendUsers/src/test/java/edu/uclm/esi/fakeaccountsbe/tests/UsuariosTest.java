package edu.uclm.esi.fakeaccountsbe.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.openqa.selenium.JavascriptExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import java.time.Duration;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class UsuariosTest {
    @Autowired
    private MockMvc server;

    @Test
    @DisplayName("Contraseña corta o no coincidentes")
    void testRegister409() throws Exception {
        JSONObject jso = new JSONObject().put("email", "ana@ana.com").put("pwd1", "ana1").put("pwd2", "ana1");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/users/registrar1")
                .contentType("application/json")
                .content(jso.toString());
        this.server.perform(request).andExpect(status().isConflict());
        jso.put("pwd1", "ana1234").put("pwd2", "Ana1234");
        request = MockMvcRequestBuilders.post("/users/register").contentType("application/json")
                .content(jso.toString());
        this.server.perform(request).andExpect(status().isConflict());
    }

    @ParameterizedTest
    @CsvSource({
            "Ana, ana1, ana1, ana@ana.com",
            "Ana, ana1234, Ana1234, ana@ana.com",
            "Ana, ana1234, ana1234, ana#ana.com",
    })
    @DisplayName("Contraseñas mal o email inválido")
    void testRegister409(String name, String pwd1, String pwd2, String email) throws Exception {
        JSONObject jso = new JSONObject().put("name", name).put("pwd1", pwd1).put("pwd2", pwd2).put("email", email);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/users/registar1")
                .contentType("application/json")
                .content(jso.toString());
        this.server.perform(request).andExpect(status().isConflict());
    }

}
