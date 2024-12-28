package edu.uclm.esi.fakeaccountsbe.model;

import edu.uclm.esi.fakeaccountsbe.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class Deleter {
	
	@Autowired
	private UserService service;

    /*@Scheduled(fixedRate = 600_000) // 600_000 ms = 10 minutos
    public void performTask() {
        this.service.clearOld();
    }*/
}