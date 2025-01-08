package edu.uclm.esi.fakeaccountsbe.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import edu.uclm.esi.fakeaccountsbe.services.UserService;

@Component
public class Deleter {

    @Autowired
    private UserService userService;

    @Scheduled(fixedRate = 60000) // Ejecutar cada minuto
    public void clearExpiredUsers() {
        this.userService.clearExpiredUsers();
    }
}