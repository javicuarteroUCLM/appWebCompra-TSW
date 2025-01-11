package edu.uclm.esi.fakeaccountsbe.tests;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;






@TestInstance(Lifecycle.PER_CLASS)
public class SeleniumTests {
    private WebDriver driverPepe;
    private WebDriver driverAna;
    private Map<String, Object> vars;
    JavascriptExecutor jsExecutorPepe;
    JavascriptExecutor jsExecutorAna;

    @BeforeEach
    public void setUp() {
        /* ************************************** ACORDADOS DE CAMBIARLO POR VUESTRA RUTA ************************************************/
        System.setProperty("webdriver.chrome.driver", "/Users/javi/Descargas/chromedriver-mac/chromedriver"); 
        /* ***************************************************************************************************************************** */
        Map <String, Object> prefs = new HashMap<String, Object>();

        //Preconfiguraciones para dar permisos a chrome a acceder al portapapeles y a las notificaciones
        prefs.put("profile.default_content_setting_values.clipboard", 1);
        prefs.put("profile.default_content_setting_values.notifications", 2);


        ChromeOptions options = new ChromeOptions();
        /******** Ruta de Chrome en MacOS, cambiadlo por la vuestra **************/
		options.setBinary("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"); 
        /*****************************************************************************************************************************/

        options.addArguments("--remote-allow-origins=*");

        // Más reconfiguraciones para dar permisos a chrome a acceder al portapapeles y a las notificaciones
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-notifications");
        options.addArguments("--clipboard-access-allowed-origin=*");
        options.addArguments("--unsafely-treat-insecure-origin-as-secure=http://localhost:3000");
        options.setExperimentalOption("prefs", prefs);


        driverPepe = new ChromeDriver(options);
        driverPepe.manage().window().setSize(new Dimension(730, 1664));
        driverPepe.manage().window().setPosition((new org.openqa.selenium.Point(0, 0)));
        jsExecutorPepe = (JavascriptExecutor) driverPepe;

        driverAna = new ChromeDriver(options);
        driverAna.manage().window().setSize(new Dimension(730, 1664));
        driverAna.manage().window().setPosition((new org.openqa.selenium.Point(731, 0)));
        jsExecutorAna = (JavascriptExecutor) driverAna;

        vars = new HashMap<String, Object>();
    }

    @AfterEach
    public void tearDown() {
        driverPepe.quit();
        driverAna.quit();
    }

    @Test
    public void testListaDeCompra() {
        driverPepe.get("http://localhost:3000/");
        driverAna.get("http://localhost:3000/");
        pausa(2000);

        //Pepe click en Registrarse
        driverPepe.findElement(By.xpath("/html/body/div/div/div[2]/button[2]")).click();

        pausa(1000);

        //Pepe rellena el formulario de registro
        //driverPepe.findElement(By.xpath("/html/body/div/div/div/form/div[1]/input")).sendKeys("dexewam981@kvegg.com");
        driverPepe.findElement(By.xpath("/html/body/div/div/div/form/div[2]/input")).sendKeys("pepe1234");
        driverPepe.findElement(By.xpath("/html/body/div/div/div/form/div[3]/input")).sendKeys("pepe1234");

        jsExecutorPepe.executeScript("window.open()");
        ArrayList<String> tabs = new ArrayList<String>(driverPepe.getWindowHandles());
        driverPepe.switchTo().window(tabs.get(1));
        driverPepe.get("https://temp-mail.org/es");

        pausa(12000);
    
        driverPepe.findElement(By.xpath("/html/body/div[1]/div/div/div[2]/div[1]/form/div[1]/div/button[2]")).click();

        pausa(2000);

        driverPepe.switchTo().window(tabs.get(0));
        String emailCopiado = (String) jsExecutorPepe.executeScript("return navigator.clipboard.readText();");

        try {
            driverPepe.findElement(By.xpath("/html/body/div/div/div/form/div[1]/input")).sendKeys(emailCopiado);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Pepe hace click en enviar formulario de registro
        driverPepe.findElement(By.xpath("/html/body/div/div/div/form/button")).click();

        pausa(5000);

        
        driverPepe.switchTo().window(tabs.get(1)); 

        pausa(5000);

        //Scroll un poco para ver el correo
        jsExecutorPepe.executeScript("window.scrollTo(0, 500)");

        //driverPepe.findElement(By.xpath("//span[@class='inboxSenderName inboxSenderEllipsis' and @title='Javier Cuartero Corredor']")).click();
        //driverPepe.findElement(By.xpath("/html/body/main/div[1]/div/div[2]/div[2]/div/div[1]/div/div[4]/ul/li[2]/div[1]/a/span[4]")).click();
        // Espera a que el contenedor con correos esté presente
        WebDriverWait wait = new WebDriverWait(driverPepe, Duration.ofSeconds(10));

        // Localiza el elemento <li> que contiene el asunto "Confirma tu cuenta"
        WebElement emailContainer = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[@class='inbox-dataList']//span[text()='Confirma tu cuenta']/ancestor::li")
        ));
        emailContainer.click();
        
        // Espera a que el enlace con texto "Confirmar Cuenta" esté presente dentro del div inbox-data-content-intro
        wait = new WebDriverWait(driverPepe, Duration.ofSeconds(10));
        WebElement confirmarCuentaLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//div[@class='inbox-data-content-intro']//a[text()='Confirmar Cuenta']")
        ));

        // Haz clic en el enlace de confirmar cuenta
        confirmarCuentaLink.click();

        pausa(5000);

    }

    private void pausa (int milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}