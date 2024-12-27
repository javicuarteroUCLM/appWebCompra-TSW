package edu.uclm.esi.fakeaccountsbe.http;

import edu.uclm.esi.fakeaccountsbe.dao.UserDao;
import edu.uclm.esi.fakeaccountsbe.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
		User user = userDao.findByToken(token);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido");
		}
	}

	@GetMapping("/obtenerEmail")
	public String obtenerEmail(@RequestParam String token) {
		System.out.println("Token recibido en /obtenerEmail: " + token);
		// Busca el token en la base de datos y retorna el email asociado
		User user = userDao.findByToken(token);
		if (user != null) {
			String email = user.getEmail();
			System.out.println("Email encontrado: " + email);
			return email;
		} else {
			System.out.println("Token no encontrado: " + token);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido");
		}
	}

}
