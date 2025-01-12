package edu.uclm.esi.fakeaccountsbe.tests;
import io.netty.handler.timeout.TimeoutException;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.assertTrue;










@TestInstance(Lifecycle.PER_CLASS)
@Transactional
public class SeleniumTests {
    private WebDriver driverPepe;
    private WebDriver driverAna;
    private Map<String, Object> vars;
    JavascriptExecutor jsExecutorPepe;
    JavascriptExecutor jsExecutorAna;

    @BeforeEach
    public void setUp() {
        /* ************************************** ACORDADOS DE CAMBIARLO POR VUESTRA RUTA ************************************************/
        System.setProperty("webdriver.chrome.driver", "/Users/javi/Descargar/chromedriver-mac/chromedriver"); 
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

        String emailPepeString = "PepeListasTyS5@uclm.es";
        String emailAnaString = "AnaListasTyS5@uclm.es";

        //Pepe rellena el formulario de registro
        driverPepe.findElement(By.xpath("/html/body/div/div/div/form/div[1]/input")).sendKeys(emailPepeString);
        driverPepe.findElement(By.xpath("/html/body/div/div/div/form/div[2]/input")).sendKeys("pepe1234");
        driverPepe.findElement(By.xpath("/html/body/div/div/div/form/div[3]/input")).sendKeys("pepe1234");

        /*
        jsExecutorPepe.executeScript("window.open()");
        ArrayList<String> tabs = new ArrayList<String>(driverPepe.getWindowHandles());
        driverPepe.switchTo().window(tabs.get(1));
        driverPepe.get("https://temp-mail.org/es");

        pausa(12000);
    
        //Pepe copia el email temporal
        driverPepe.findElement(By.xpath("/html/body/div[1]/div/div/div[2]/div[1]/form/div[1]/div/button[2]")).click();

        pausa(2000);

        driverPepe.switchTo().window(tabs.get(0));
        String emailCopiado = (String) jsExecutorPepe.executeScript("return navigator.clipboard.readText();");

        try {
            driverPepe.findElement(By.xpath("/html/body/div/div/div/form/div[1]/input")).sendKeys(emailCopiado);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        //Pepe hace click en enviar formulario de registro
        driverPepe.findElement(By.xpath("/html/body/div/div/div/form/button")).click();

        pausa(2000);

        jsExecutorPepe.executeScript("window.open()");
        ArrayList <String> tabs = new ArrayList<String>(driverPepe.getWindowHandles());
        driverPepe.switchTo().window(tabs.get(1));

        //Pepe confirma su cuenta
        driverPepe.get("http://localhost:3000/confirmarCuenta?token=1234");

        pausa(2000);

        //Pepe cierra la pestaña de confirmar cuenta
        driverPepe.close();

        driverPepe.switchTo().window(tabs.get(0));
        pausa(1000);

        //Pepe hace click en el botón que redirige a Iniciar Sesión
        driverPepe.findElement(By.xpath("/html/body/div/div/div/p[2]/button")).click();

        pausa(1000);
        
        //Pepe mete las credenciales
        driverPepe.findElement(By.xpath("/html/body/div/div/div/form/div[1]/input")).sendKeys(emailPepeString);
        driverPepe.findElement(By.xpath("/html/body/div/div/div/form/div[2]/input")).sendKeys("pepe1234");
        pausa(1000);

        //Pepe da click en Iniciar Sesión
        driverPepe.findElement(By.xpath("/html/body/div/div/div/form/button")).click();

        //ORÁCULO comprobar en la base de datos que Pepe ha confirmado su cuenta.
        try {
            boolean confirmado = Oraculo.isUserConfirmed(emailPepeString);
            assertTrue(confirmado, "El usuario no ha confirmado su cuenta");
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Error al verificar si Pepe ha confirmado su cuenta");
        }

        //Pepe crea una lista de la compra
        driverPepe.findElement(By.xpath("/html/body/div/div/div[4]/input")).sendKeys("Cumpleaños");
        pausa(1000);
        driverPepe.findElement(By.xpath("/html/body/div/div/div[4]/button")).click();
        pausa(2000);

        //Pepe selecciona la lista Cumpleaños que acaba de crear
        driverPepe.findElement(By.xpath("//li[span[text()='Cumpleaños']]//button[text()='Seleccionar']")).click();

        pausa(2000);

        WebElement btnAnadirProducto = driverPepe.findElement(By.xpath("/html/body/div/div/div[5]/div/button"));

        //Pepe añade 30 cervezas a la lista
        driverPepe.findElement(By.xpath("/html/body/div/div/div[5]/div/div[1]/input")).sendKeys("Latas de cerveza");
        driverPepe.findElement(By.xpath("/html/body/div/div/div[5]/div/div[2]/input")).clear();
        driverPepe.findElement(By.xpath("/html/body/div/div/div[5]/div/div[2]/input")).sendKeys("30");
        pausa(1000);
        btnAnadirProducto.click();

        //Scroll para ver los productos añadidos
        jsExecutorPepe.executeScript("window.scrollTo(0, document.body.scrollHeight)");

        pausa(2000);

        //Pepe añade 1 tarta
        driverPepe.findElement(By.xpath("/html/body/div/div/div[5]/div/div[1]/input")).sendKeys("Tarta");
        //driverPepe.findElement(By.xpath("/html/body/div/div/div[5]/div/div[2]/input")).sendKeys("1");
        pausa(2000);
        btnAnadirProducto.click();

        jsExecutorPepe.executeScript("window.scrollTo(0, document.body.scrollHeight)");

        pausa(2000);

        //Pepe añade 2 bolsas de patatas fritas
        driverPepe.findElement(By.xpath("/html/body/div/div/div[5]/div/div[1]/input")).sendKeys("Bolsas de patatas fritas");
        driverPepe.findElement(By.xpath("/html/body/div/div/div[5]/div/div[2]/input")).clear();
        driverPepe.findElement(By.xpath("/html/body/div/div/div[5]/div/div[2]/input")).sendKeys("2");
        pausa(1000);
        btnAnadirProducto.click();

        //Scroll para ver los productos añadidos
        jsExecutorPepe.executeScript("window.scrollTo(0, 800)");

        pausa(2000);

        /* ****************** ANA ***************************** */

        //Ana hace click en Registrarse
        driverAna.findElement(By.xpath("/html/body/div/div/div[2]/button[2]")).click();

        pausa(1000);

        //Ana rellena el formulario de registro
        driverAna.findElement(By.xpath("/html/body/div/div/div/form/div[1]/input")).sendKeys(emailAnaString);
        driverAna.findElement(By.xpath("/html/body/div/div/div/form/div[2]/input")).sendKeys("ana1234");
        driverAna.findElement(By.xpath("/html/body/div/div/div/form/div[3]/input")).sendKeys("ana1234");

        pausa(1000);

        //Ana hace click en enviar formulario de registro
        driverAna.findElement(By.xpath("/html/body/div/div/div/form/button")).click();
        pausa(1000);

        jsExecutorAna.executeScript("window.open()");
        tabs = new ArrayList<String>(driverAna.getWindowHandles());
        driverAna.switchTo().window(tabs.get(1));

        //Ana confirma su cuenta
        driverAna.get("http://localhost:3000/confirmarCuenta?token=1234");

        pausa(2000);

        //Pepe cierra la pestaña de confirmar cuenta
        driverAna.close();

        driverAna.switchTo().window(tabs.get(0));

        pausa(1000);

        //Ana hace click en el botón que redirige a Iniciar Sesión
        driverAna.findElement(By.xpath("/html/body/div/div/div/p[2]/button")).click();

        pausa(1000);

        //Ana mete las credenciales
        driverAna.findElement(By.xpath("/html/body/div/div/div/form/div[1]/input")).sendKeys(emailAnaString);
        driverAna.findElement(By.xpath("/html/body/div/div/div/form/div[2]/input")).sendKeys("ana1234");

        pausa(1000);

        //Ana da click en Iniciar Sesión
        driverAna.findElement(By.xpath("/html/body/div/div/div/form/button")).click();


        //Pepe invita a Ana a su lista de la compra
        driverPepe.findElement(By.xpath("//li[span[text()='Cumpleaños']]//button[@aria-label='Compartir lista Cumpleaños']")).click();
        pausa(1000);

        driverPepe.findElement(By.xpath("/html/body/div/div/div[6]/div/div[1]/div/input")).sendKeys(emailAnaString);
        pausa(1000);

        //Click invitar
        driverPepe.findElement(By.xpath("/html/body/div/div/div[6]/div/div[1]/div/button")).click();

        pausa(2000);

        //Pepe copia el enlace de la lista
        driverPepe.findElement(By.xpath("/html/body/div/div/div[6]/div/div[2]/div[1]/button")).click();

        pausa(3000);

        try {
            // Espera la alerta y acéptala
            WebDriverWait wait = new WebDriverWait(driverPepe, Duration.ofSeconds(5));
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept(); 
            System.out.println("Alerta aceptada.");
        } catch (TimeoutException e) {
            System.out.println("No se encontró ninguna alerta.");
        }

        String enlaceCopiado = (String) jsExecutorPepe.executeScript("return navigator.clipboard.readText();");

        pausa(1000);

        //Ana abre una nueva pestaña y copia el enlace de la lista
        jsExecutorAna.executeScript("window.open()");
        tabs = new ArrayList<String>(driverAna.getWindowHandles());
        driverAna.switchTo().window(tabs.get(1));
        pausa(2000);
        System.out.println("Enlace copiado: " + enlaceCopiado);
        driverAna.get(enlaceCopiado);
        pausa(3000);

        
        //Pepe cierra la ventana de compartir
        driverPepe.findElement(By.xpath("/html/body/div/div/div[6]/div/button")).click();
        
        //Pepe selecciona la lista cumpleaños
        driverPepe.findElement(By.xpath("//li[span[text()='Cumpleaños']]//button[text()='Seleccionar']")).click();

        //Ana espera a que le llegue la invitación
        pausa(2000);

        //Ana hace click en aceptar
        driverAna.findElement(By.xpath("/html/body/div/div/div/div/button[1]")).click();

        try {
            // Espera la alerta y acéptala
            WebDriverWait wait = new WebDriverWait(driverAna, Duration.ofSeconds(5));
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept(); 
            System.out.println("Alerta aceptada.");
        } catch (TimeoutException e) {
            System.out.println("No se encontró ninguna alerta.");
        }

        pausa(2000);

        driverAna.findElement(By.xpath("//li[span[text()='Cumpleaños']]//button[text()='Seleccionar']")).click();
        pausa(2000);

        //Scroll para ver los productos añadidos
        jsExecutorAna.executeScript("window.scrollTo(0, document.body.scrollHeight)");

        pausa(2000);

        //Ana selecciona el botón comprar de la tarta
        driverAna.findElement(By.xpath("//tr[td[text()='Tarta']]//button[text()='Comprar']")).click();
        pausa(1000);

        //Ana clica en guardar
        driverAna.findElement(By.xpath("//html/body/div/div/div[6]/div/button[1]")).click();
        
        pausa(1000);



        WebDriverWait wait = new WebDriverWait(driverPepe, Duration.ofSeconds(10));

        //Oráculo comprobar que la tarta aparece como comprada en el driverPepe
        WebElement tartaCantidad = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("/html/body/div/div/div[5]/table/tbody/tr[1]/td[3]")
        ));

        String valorActual = tartaCantidad.getText();
        assertTrue(valorActual.equals("1"), 
        "El valor no es correcto. Se esperaba '1', pero se encontró '" + valorActual + "'.");

        pausa(2000);

    }

    private void pausa (int milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}