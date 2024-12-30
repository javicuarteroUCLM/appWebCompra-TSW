package edu.uclm.esi.fakeaccountsbe.http;

import edu.uclm.esi.fakeaccountsbe.dao.UserDao;
import edu.uclm.esi.fakeaccountsbe.model.User;
import java.util.HashMap;
import java.util.Map;
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
		User user = this.userDao.findByToken(token);
		System.out.println("Token recibido en /validar: " + token);
		System.out.println("User encontrado: " + user);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido");
		}
	}

	@GetMapping("/obtenerEmail")
	public Map<String, Object> obtenerEmail(@RequestParam String token) {
		System.out.println("Token recibido en /obtenerEmail: " + token);
		// Busca el token en la base de datos y retorna el email asociado
		User user = userDao.findByToken(token);
		if (user != null) {
			Map<String, Object> userInfo = new HashMap<>();
			// String email = user.getEmail();
			userInfo.put("email", user.getEmail());
			// System.out.println("Email encontrado: " + email);
			userInfo.put("esPagado", user.isEsPagado());
			return userInfo;
		} else {
			System.out.println("Token no encontrado: " + token);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido");
		}
	}

}
