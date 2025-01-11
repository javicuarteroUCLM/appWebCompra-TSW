package edu.uclm.esi.fakeaccountsbe.services;

import brevo.ApiClient;
import brevo.ApiException;
import brevo.Configuration;
import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import edu.uclm.esi.fakeaccountsbe.dao.UserDao;
import edu.uclm.esi.fakeaccountsbe.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class EmailService {

    @Autowired
    private UserDao userDao;

    private final Manager manager;

    @Autowired
    public EmailService(Manager manager) throws org.json.JSONException {
        this.manager = manager;
    }

    public void sendCredentialsEmail(String destinatario) throws org.json.JSONException {
        // Obtener las credenciales del usuario desde el DAO
        User user = this.userDao.findByEmail(destinatario);

        if (user == null) {
            System.err.println("Error: No user found with email " + destinatario);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "No existe un usuario con ese correo electrónico");
        }

        // Generar una nueva contraseña temporal
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8);

        // Actualizar la contraseña del usuario en la base de datos
        user.setPwd(tempPassword);
        this.userDao.save(user);

        // Usar la nueva contraseña temporal para el correo
        String credentials = "Usuario: " + user.getEmail() + ", Contraseña temporal: " + tempPassword;

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setApiKey(this.manager.getConfiguration().getJSONObject("brevo").getString("apiKey"));
        String sender = this.manager.getConfiguration().getJSONObject("brevo").getJSONObject("sender")
                .getString("email");
        String name = this.manager.getConfiguration().getJSONObject("brevo").getJSONObject("sender").getString("name");
        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);

        // Configurar el correo
        SendSmtpEmail email = new SendSmtpEmail()
                .to(java.util.List.of(new SendSmtpEmailTo().email(destinatario).name(destinatario))) // Destinatario
                .sender(new SendSmtpEmailSender().email(sender).name(name)) // Remitente
                .subject("Recuperar Credenciales de acceso")
                .htmlContent("<p>Sus credenciales temporales ahora son: " + credentials + "</p>"
                        + "<p>Por favor, cambie su contraseña lo antes posible</p>");

        try {
            apiInstance.sendTransacEmail(email);
            System.out.println("Correo de prueba enviado correctamente a " + destinatario);
        } catch (ApiException e) {
            System.err.println("Error al enviar el correo: " + e.getResponseBody());
        }
    }

    public void sendConfimacionEmail(String destinatario, String token) throws org.json.JSONException {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setApiKey(this.manager.getConfiguration().getJSONObject("brevo").getString("apiKey"));
        String sender = this.manager.getConfiguration().getJSONObject("brevo").getJSONObject("sender")
                .getString("email");
        String name = this.manager.getConfiguration().getJSONObject("brevo").getJSONObject("sender").getString("name");
        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);

        // Configurar el correo
        SendSmtpEmail email = new SendSmtpEmail()
                .to(java.util.List.of(new SendSmtpEmailTo().email(destinatario).name(destinatario))) // Destinatario
                .sender(new SendSmtpEmailSender().email(sender).name(name)) // Remitente
                .subject("Confirma tu cuenta")
                .htmlContent("<p>¡Gracias por registrarse en nuestra aplicacion!</p>"
                        + "<p>Por favor, confirme su cuenta haciendo clic en el siguiente enlace: "
                        + "<a href='http://localhost:3000/confirmarCuenta?token=" + token
                        + "'>Confirmar Cuenta</a></p>");

        try {
            apiInstance.sendTransacEmail(email);
            System.out.println("Correo de prueba enviado correctamente a " + destinatario);
        } catch (ApiException e) {
            System.err.println("Error al enviar el correo: " + e.getResponseBody());
        }
    }

    public void sendEmail(String email, String subject, String message) throws org.json.JSONException {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setApiKey(this.manager.getConfiguration().getJSONObject("brevo").getString("apiKey"));
        String sender = this.manager.getConfiguration().getJSONObject("brevo").getJSONObject("sender")
                .getString("email");
        String name = this.manager.getConfiguration().getJSONObject("brevo").getJSONObject("sender").getString("name");
        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);

        // Configurar el correo
        SendSmtpEmail emailToSend = new SendSmtpEmail()
                .to(java.util.List.of(new SendSmtpEmailTo().email(email).name(email))) // Destinatario
                .sender(new SendSmtpEmailSender().email(sender).name(name)) // Remitente
                .subject(subject).htmlContent(message);

        try {
            apiInstance.sendTransacEmail(emailToSend);
            System.out.println("Correo de prueba enviado correctamente a " + email);
        } catch (ApiException e) {
            System.err.println("Error al enviar el correo: " + e.getResponseBody());
        }
    }

}
