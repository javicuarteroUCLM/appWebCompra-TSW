package edu.uclm.esi.fakeaccountsbe.http;

import edu.uclm.esi.fakeaccountsbe.dao.UserDao;
import edu.uclm.esi.fakeaccountsbe.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;



@RestController
@RequestMapping("tokens")
@CrossOrigin("*")
public class TokenController {

	@Autowired
	private UserDao userDao;

	@PutMapping("/validar")
	public void validar(@RequestBody String token) {

		User user = this.userDao.findByToken(token);
		if (user == null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Token no válido");
	}

}