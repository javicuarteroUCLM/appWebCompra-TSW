package edu.uclm.esi.fakeaccountsbe.http;

import edu.uclm.esi.fakeaccountsbe.services.EmailService;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("email")
@CrossOrigin("*")
public class EmailController {
    @Autowired
    private EmailService emailService;

    @PostMapping("/recoverEmail")
    public void recoverCredentialsByEmail(@RequestBody String email) {
        this.emailService.sendCredentialsEmail(email);
    }

    @PostMapping("/sendEmail")
    public void sendEmail(@RequestBody String emailRequestString) {
        System.out.println("Email request: " + emailRequestString);
        JSONObject emailRequest = new JSONObject(emailRequestString);
        String email = emailRequest.getString("email");
        String subject = emailRequest.getString("subject");
        String message = emailRequest.getString("message");

        System.out.println("Email: " + email + " Subject: " + subject + " Message: " + message);
        this.emailService.sendEmail(email, subject, message);
    }
}