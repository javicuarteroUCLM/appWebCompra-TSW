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
public class ScrappingGuitarrasDeAmazon {
        private WebDriver driverA;
        private Map<String, Object> vars;
        JavascriptExecutor jsA;
        private WebDriverWait waitA;

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

                driverA.get("https://amazon.es");
                this.pausa(1000);

                java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                int width = (int) screenSize.getWidth();
                int height = (int) screenSize.getHeight();

                driverA.manage().window().setSize(new org.openqa.selenium.Dimension(width / 2, height));
                driverA.manage().window().setPosition(new org.openqa.selenium.Point(0, 0));

                vars = new HashMap<String, Object>();
        }

        @AfterEach // After All
        public void tearDown() {
                driverA.quit();
        }

        @Test
        @Order(0)
        public void testAmazon() {

                WebElement input = driverA.findElement(By.id("twotabsearchtextbox"));
                input.sendKeys("guitarra");
                input.submit();
                this.pausa(2000);
                List<WebElement> guitarras = driverA
                                .findElements(By.xpath("//span[@class='a-size-base-plus a-color-base a-text-normal']"));
                assertTrue(guitarras.size() > 0);
        }

        @Test
        @Order(1)
        public void testGuitarras() {
                WebElement btnCookies = this.waitA
                                .until(ExpectedConditions.elementToBeClickable(By.id("sp-cc-accept")));
                btnCookies.click();
                this.pausa(1000);

                WebElement cajaBusqueda = driverA.findElement(By.id("twotabsearchtextbox"));
                cajaBusqueda.sendKeys("guitarra electrica");
                cajaBusqueda.submit();
                this.pausa(2000);

                List<WebElement> nombres = this.driverA
                                .findElements(By.className("a-size-base-plus a-color-base a-text-normal"));

                nombres.forEach(n -> {
                        System.out.println(n.getText());
                });

                nombres.get(0).click();

        }

        @Test
        @Order(2)
        public void testGuitarras2() {
                WebElement btnCookies = this.waitA
                                .until(ExpectedConditions.elementToBeClickable(By.id("sp-cc-accept")));
                btnCookies.click();
                this.pausa(1000);

                WebElement cajaBusqueda = driverA.findElement(By.id("twotabsearchtextbox"));
                cajaBusqueda.sendKeys("guitarra electrica");
                cajaBusqueda.submit();
                this.pausa(2000);

                List<WebElement> nombres = this.driverA
                                .findElements(By.className("a-size-base-plus a-color-base a-text-normal"));

                nombres.forEach(n -> {
                        System.out.println(n.getText());
                });

                nombres.get(0).click();

        }

        private void pausa(int tiempo) {
                try {
                        Thread.sleep(tiempo);
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }
        }
}
