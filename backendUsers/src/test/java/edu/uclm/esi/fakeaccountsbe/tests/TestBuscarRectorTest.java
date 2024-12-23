package edu.uclm.esi.fakeaccountsbe.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import java.time.Duration;
import java.util.*;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class TestBuscarRectorTest {
  private WebDriver driver;
  private Map<String, Object> vars;
  JavascriptExecutor js;
  private WebDriverWait wait;

  @BeforeAll // Before All
  public void setUp() {
    System.setProperty("webdriver.chrome.driver",
        "C:/Users/Usuario/Desktop/chromedriver-win64/chromedriver-win64/chromedriver.exe");

    ChromeOptions options = new ChromeOptions();
    options.setBinary(
        "C:/Users/Usuario/Desktop/chrome-win64/chrome-win64/chrome.exe");
    options.addArguments("--remote-allow-origins=*");

    driver = new ChromeDriver(options);
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(3));
    js = (JavascriptExecutor) driver;
    vars = new HashMap<String, Object>();
  }

  @AfterEach // After All
  public void tearDown() {
    driver.quit();
  }

  @Test
  public void testBuscarRector() {
    driver.get("https://directorio.uclm.es/");
    driver.findElement(By.id("CPH_CajaCentro_tb_busqueda")).click();
    driver.findElement(By.id("CPH_CajaCentro_tb_busqueda")).click();
    driver.findElement(By.id("CPH_CajaCentro_tb_busqueda")).sendKeys("Julian Garde");
    driver.findElement(By.id("CPH_CajaCentro_lkbtn_consultar")).click();
    driver.findElement(By.cssSelector(".text-left:nth-child(2)")).click();
    driver.findElement(By.id("CPH_CajaCentro_gv_personas_lkbtn_descripcionPersona_0")).click();
    driver.findElement(By.cssSelector("#CPH_CajaCentro_panel_foto .form-control-static")).click();
    js.executeScript("window.scrollTo(0,116)");
    driver.findElement(By.cssSelector("#CPH_CajaCentro_pa_cargo > .col-sm-9:nth-child(3)")).click();
    assertEquals(driver.findElement(By.id("CPH_CajaCentro_rpt_cargos_lb_cargo_0")).getText(), ("RECTOR/A"));
  }

  @Test
  @Order(1)
  public void testRegistroCorrecto() {
    this.pausa(1000);
    driver.get("https://alarcosj.esi.uclm.es/examplesfortesting");

    WebElement we = driver.findElement(By.xpath("/html/body/div/div/a[1]"));
    we.click();

    we = driver.findElement(By.xpath("/html/body/app-root/div/header/nav/ul/li[2]/a"));
    we.click();

    WebElement cajaNombre = wait
        .until(ExpectedConditions
            .visibilityOfElementLocated(By.xpath("/html/body/app-root/div/main/app-register/form/div[1]/input")));
    WebElement cajaEmail = wait
        .until(ExpectedConditions
            .visibilityOfElementLocated(By.xpath("/html/body/app-root/div/main/app-register/form/div[2]/input")));
    WebElement cajaPwd = wait
        .until(ExpectedConditions
            .visibilityOfElementLocated(By.xpath("/html/body/app-root/div/main/app-register/form/div[3]/input")));
    WebElement cajaPwd2 = wait
        .until(ExpectedConditions
            .visibilityOfElementLocated(By.xpath("/html/body/app-root/div/main/app-register/form/div[4]/input")));
    WebElement btnRegistro = wait
        .until(ExpectedConditions
            .visibilityOfElementLocated(By.xpath("/html/body/app-root/div/main/app-register/form/div[5]/button")));

    cajaNombre.sendKeys("pepe");
    cajaEmail.sendKeys("pepe@email.com");
    cajaPwd.sendKeys("Pepe121211234");
    cajaPwd2.sendKeys("Pepe121211234");

    btnRegistro.click();

    WebElement etiqueta = wait
        .until(ExpectedConditions
            .visibilityOfElementLocated(By.xpath("/html/body/app-root/div/header/nav/ul/li[1]/a")));
    assertEquals("Login", etiqueta.getText());

  }

  @Test
  @Order(2)
  public void testLoginCorrecto() {
    this.pausa(1000);
    driver.get("https://alarcosj.esi.uclm.es/examplesfortesting");

    WebElement we = driver.findElement(By.xpath("/html/body/div/div/a[1]"));
    we.click();

    we = driver.findElement(By.xpath("/html/body/app-root/div/header/nav/ul/li[1]/a"));
    we.click();

    WebElement cajaEmail = wait
        .until(ExpectedConditions
            .visibilityOfElementLocated(By.xpath("/html/body/app-root/div/main/app-login/form/div[1]/input")));
    WebElement cajaPwd = wait
        .until(ExpectedConditions
            .visibilityOfElementLocated(By.xpath("/html/body/app-root/div/main/app-login/form/div[2]/input")));
    WebElement btnLogin = wait
        .until(ExpectedConditions
            .visibilityOfElementLocated(By.xpath("/html/body/app-root/div/main/app-login/form/div[3]/button")));

    cajaEmail.sendKeys("pepe@email.com");
    cajaPwd.sendKeys("Pepe121211234");

    btnLogin.click();
    String currentURL = driver.getCurrentUrl();
    assertEquals("https://alarcosj.esi.uclm.es/examplesfortesting/angular/celebration", currentURL);
  }

  private void pausa(int tiempo) {
    try {
      Thread.sleep(tiempo);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
