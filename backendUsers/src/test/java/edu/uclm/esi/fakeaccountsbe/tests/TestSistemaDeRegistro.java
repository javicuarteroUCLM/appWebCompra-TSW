package edu.uclm.esi.fakeaccountsbe.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import java.time.Duration;
import java.util.*;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class TestSistemaDeRegistro {
        private WebDriver driverA, driverB;
        private Map<String, Object> vars;
        JavascriptExecutor jsA, jsB;
        private WebDriverWait waitA, waitB;

        @BeforeAll // Before All
        public void setUp() {

                System.setProperty("webdriver.chrome.driver",
                                "C:/Users/Usuario/Desktop/TecSIWeb/chromedriver-win64/chromedriver-win64/chromedriver.exe");

                ChromeOptions options = new ChromeOptions();
                options.setBinary(
                                "C:/Users/Usuario/Desktop/TecSIWeb/chrome-win64/chrome-win64/chrome.exe");
                options.addArguments("--remote-allow-origins=*");

                driverA = new ChromeDriver(options);
                this.waitA = new WebDriverWait(driverA, Duration.ofSeconds(3));
                jsA = (JavascriptExecutor) driverA;

                driverB = new ChromeDriver(options);
                this.waitB = new WebDriverWait(driverB, Duration.ofSeconds(3));
                jsB = (JavascriptExecutor) driverB;

                driverA.get("https://alarcosj.esi.uclm.es/examplesfortesting");
                driverB.get("https://alarcosj.esi.uclm.es/examplesfortesting");
                this.pausa(1000);

                java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                int width = (int) screenSize.getWidth();
                int height = (int) screenSize.getHeight();

                driverA.manage().window().setSize(new org.openqa.selenium.Dimension(width / 2, height));
                driverA.manage().window().setPosition(new org.openqa.selenium.Point(0, 0));

                driverB.manage().window().setSize(new org.openqa.selenium.Dimension(width / 2, height));
                driverB.manage().window().setPosition(new org.openqa.selenium.Point(width / 2, 0));

                vars = new HashMap<String, Object>();
        }

        @AfterEach // After All
        public void tearDown() {
                driverA.quit();
        }

        @Test
        @Order(1)
        public void testRegistroCorrecto() {
                this.registrar(this.driverA, this.waitA, "pepe", "pepe@email.com", "Pepe121211234", "Pepe121211234");

                WebElement etiqueta = waitA
                                .until(ExpectedConditions
                                                .visibilityOfElementLocated(
                                                                By.xpath("/html/body/app-root/div/main/app-login/form/div[3]/button")));
                assertEquals("Login", etiqueta.getText());

        }

        @Test
        @Order(3)
        public void testRegistroIncorrecto() {
                this.registrar(this.driverA, this.waitA, "peasd", "pasdsadpe@email.com", "Pepe121asdsad11234",
                                "Pepe121211234");
                this.pausa(1000);
                WebElement etiqueta = driverA
                                .findElement(By.xpath("/html/body/app-root/div/main/app-register/form/div[4]/small"));
                // etiqueta.click();
                assertTrue(etiqueta.getText().contains("Passwords do not match"));
        }

        private void registrar(WebDriver driver, WebDriverWait wait, String nombre, String email, String pwd,
                        String pwd2) {
                this.pausa(1000);
                driver.get("https://alarcosj.esi.uclm.es/examplesfortesting");

                WebElement we = driver.findElement(By.xpath("/html/body/div/div/a[1]"));
                we.click();

                we = driver.findElement(By.xpath("/html/body/app-root/div/header/nav/ul/li[2]/a"));
                we.click();

                WebElement cajaNombre = wait
                                .until(ExpectedConditions
                                                .visibilityOfElementLocated(
                                                                By.xpath("/html/body/app-root/div/main/app-register/form/div[1]/input")));
                WebElement cajaEmail = wait
                                .until(ExpectedConditions
                                                .visibilityOfElementLocated(
                                                                By.xpath("/html/body/app-root/div/main/app-register/form/div[2]/input")));
                WebElement cajaPwd = wait
                                .until(ExpectedConditions
                                                .visibilityOfElementLocated(
                                                                By.xpath("/html/body/app-root/div/main/app-register/form/div[3]/input")));
                WebElement cajaPwd2 = wait
                                .until(ExpectedConditions
                                                .visibilityOfElementLocated(
                                                                By.xpath("/html/body/app-root/div/main/app-register/form/div[4]/input")));
                WebElement btnRegistro = wait
                                .until(ExpectedConditions
                                                .visibilityOfElementLocated(
                                                                By.xpath("/html/body/app-root/div/main/app-register/form/div[5]/button")));

                cajaNombre.sendKeys(nombre);
                cajaEmail.sendKeys(email);
                cajaPwd.sendKeys(pwd);
                cajaPwd2.sendKeys(pwd2);

                btnRegistro.click();
        }

        @Test
        @Order(2)
        public void testLoginCorrecto() {
                this.registrar(driverA, waitA, "pepe", "pepe@email.com", "Pepe121211234", "Pepe121211234");

                this.pausa(1000);

                this.login(this.driverA, this.waitA, "pepe@email.com", "Pepe121211234");

                this.pausa(1000);
                String currentURL = driverA.getCurrentUrl();
                assertEquals("https://alarcosj.esi.uclm.es/examplesfortesting/angular/celebration", currentURL);

                this.jsA.executeScript("window.history.go(-2)");
        }

        private void login(WebDriver driver, WebDriverWait wait, String email, String pwd) {
                this.pausa(1000);
                driver.get("https://alarcosj.esi.uclm.es/examplesfortesting");

                WebElement we = driver.findElement(By.xpath("/html/body/div/div/a[1]"));
                we.click();

                we = driver.findElement(By.xpath("/html/body/app-root/div/header/nav/ul/li[1]/a"));
                we.click();

                WebElement cajaEmail = wait
                                .until(ExpectedConditions
                                                .visibilityOfElementLocated(
                                                                By.xpath("/html/body/app-root/div/main/app-login/form/div[1]/input")));
                WebElement cajaPwd = wait
                                .until(ExpectedConditions
                                                .visibilityOfElementLocated(
                                                                By.xpath("/html/body/app-root/div/main/app-login/form/div[2]/input")));
                WebElement btnLogin = wait
                                .until(ExpectedConditions
                                                .visibilityOfElementLocated(
                                                                By.xpath("/html/body/app-root/div/main/app-login/form/div[3]/button")));

                cajaEmail.sendKeys(email);
                cajaPwd.sendKeys(pwd);

                btnLogin.click();
        }

        @ParameterizedTest
        @CsvSource({ "pepe, manuel@asfd.com, Pepe121211234, Pepe121211234, false",
                        "paqui, paqui@kasmd.com, Paqui211234, Paqui211234, true",
                        "manuel, manu@email.com, Manuel211234, Manuel21123, false",
                        "manuel, manu@email.com, Manuel211234, Manuel211234, true" })
        void RegistroMasivo(WebDriver driver, String nombre, String email, String pwd, String pwd2,
                        boolean veredictoDePaso) {

                this.registrar(driverA, this.waitA, nombre, email, pwd, pwd2);
                if (veredictoDePaso) {
                        driverA.findElement(By.xpath("/html/body/app-root/div/header/nav/ul/li[2]/a")).click();
                } else {
                        WebElement etiqueta = driver
                                        .findElement(By.xpath("/html/body/app-root/div/header/nav/ul/li[2]/a"));
                        etiqueta.click();
                        assertEquals("Passwords do not match", etiqueta.getText());

                        List<WebElement> caja = driver.findElements(By.tagName("input"));
                        caja.forEach(c -> c.clear());
                }

        }

        private void pausa(int tiempo) {
                try {
                        Thread.sleep(tiempo);
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }
        }
}
