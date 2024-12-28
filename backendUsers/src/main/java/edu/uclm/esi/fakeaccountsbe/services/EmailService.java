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

    public void sendCredentialsEmail(String recipientEmail) throws org.json.JSONException {
        // Obtener las credenciales del usuario desde el DAO
        User user = this.userDao.findByEmail(recipientEmail);

        if (user == null) {
            System.err.println("Error: No user found with email " + recipientEmail);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "No existe un usuario con ese correo electrónico");
        }

        String credentials = "Usuario: " + user.getEmail() + ", Contraseña: " + user.getPwd();

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setApiKey(this.manager.getConfiguration().getJSONObject("brevo").getString("apiKey"));

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);

        // Configurar el correo
        SendSmtpEmail email = new SendSmtpEmail()
                .to(java.util.List.of(new SendSmtpEmailTo().email(recipientEmail))) // Destinatario
                .sender(new SendSmtpEmailSender().email("gonzidlreyes02s@gmail.com")
                        .name("Gonzalo De Los Reyes Sánchez")) // Remitente
                .subject("Recuperar Credenciales de acceso")
                .htmlContent("<p>Sus credenciales son: " + credentials + "</p>");

        try {
            apiInstance.sendTransacEmail(email);
            System.out.println("Correo enviado correctamente a " + recipientEmail);
        } catch (ApiException e) {
            System.err.println("Error al enviar el correo: " + e.getResponseBody());
        }
    }

    public void sendTestEmail(String destinatario) throws org.json.JSONException {
        // Obtener las credenciales del usuario desde el DAO
        User user = this.userDao.findByEmail(destinatario);

        if (user == null) {
            System.err.println("Error: No user found with email " + destinatario);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "No existe un usuario con ese correo electrónico");
        }

        // Desencriptar la contraseña no es posible ya que se utiliza un hash SHA-512
        // En su lugar, se puede generar una nueva contraseña temporal y enviarla por
        // correo

        // Generar una nueva contraseña temporal
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8);

        // Actualizar la contraseña del usuario en la base de datos
        user.setPwd(tempPassword);
        this.userDao.save(user);

        // Usar la nueva contraseña temporal para el correo
        String credentials = "Usuario: " + user.getEmail() + ", Contraseña temporal: " + tempPassword;

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setApiKey(this.manager.getConfiguration().getJSONObject("brevo").getString("apiKey"));

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);

        // Configurar el correo
        SendSmtpEmail email = new SendSmtpEmail()
                .to(java.util.List.of(new SendSmtpEmailTo().email(destinatario).name("Javier"))) // Destinatario
                .sender(new SendSmtpEmailSender().email("gonzidlreyes02s@gmail.com").name("Gonzalo")) // Remitente
                .subject("Recuperar Credenciales de acceso")
                .htmlContent("<p>Sus credenciales son: " + credentials + "</p>");

        try {
            apiInstance.sendTransacEmail(email);
            System.out.println("Correo de prueba enviado correctamente a gonzidlreyes02s@gmail.com");
        } catch (ApiException e) {
            System.err.println("Error al enviar el correo de prueba: " + e.getResponseBody());
        }
    }
}
