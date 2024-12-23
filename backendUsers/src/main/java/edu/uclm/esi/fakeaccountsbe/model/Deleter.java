package edu.uclm.esi.fakeaccountsbe.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.uclm.esi.fakeaccountsbe.services.UserService;

@Component
public class Deleter {
	
	@Autowired
	private UserService service;

    @Scheduled(fixedRate = 600_000) // 600_000 ms = 10 minutos
    public void performTask() {
        this.service.clearOld();
    }
}