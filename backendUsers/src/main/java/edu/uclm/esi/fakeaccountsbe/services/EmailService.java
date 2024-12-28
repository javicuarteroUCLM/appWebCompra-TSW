package edu.uclm.esi.fakeaccountsbe.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import brevo.ApiClient;
import brevo.ApiException;
import brevo.Configuration;
import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import edu.uclm.esi.fakeaccountsbe.dao.UserDao;
import edu.uclm.esi.fakeaccountsbe.model.User;

@Service
public class EmailService {

    @Autowired
    private UserDao userDao;

    private final Manager manager;

    @Autowired
    public EmailService(Manager manager) throws org.json.JSONException {
        this.manager = manager;

    }

    public void sendCredentialsEmail(String recipientEmail) {
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
}
