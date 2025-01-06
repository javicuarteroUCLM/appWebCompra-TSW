package edu.uclm.esi.fakeaccountsbe.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.uclm.esi.fakeaccountsbe.dao.UserDao;
import edu.uclm.esi.fakeaccountsbe.services.UserService;












@Component
public class Deleter {
	
	@Autowired
	private UserService service;

    @Autowired
    private UserDao userDao;

    @Scheduled(fixedRate = 60000) // Ejecutar cada minuto
    public void clearExpiredUsers() {
        long currentTime = System.currentTimeMillis();
        List<User> allUsers = this.userDao.findAll();

        for (User user : allUsers) {
            if (!user.isConfirmado() && (currentTime - user.getCreationTime()) > 10 * 60 * 1000) { // A los 10 minutos el usuario no confirmado se elimina
                System.out.println("Eliminando usuario no confirmado: " + user.getEmail());
                this.userDao.delete(user);
            }
        }
    }

    /*@Scheduled(fixedRate = 600_000) // 600_000 ms = 10 minutos
    public void performTask() {
        this.service.clearOld();
    }*/
}