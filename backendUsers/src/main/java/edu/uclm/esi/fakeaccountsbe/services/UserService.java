package edu.uclm.esi.fakeaccountsbe.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.fakeaccountsbe.dao.UserDao;
import edu.uclm.esi.fakeaccountsbe.model.User;



@Service
public class UserService {
	@Autowired
	private UserDao userDao;

	@Autowired
	private EmailService emailService;

	private Map<String, User> users = new ConcurrentHashMap<>();
	private Map<String, List<User>> usersByIp = new ConcurrentHashMap<>();

	public void registrar(String ip, User user) {
		this.getAllUsers();

		if (this.users.get(user.getEmail()) != null) {
			System.out.println("Ya existe un usuario con ese correo electrónico");
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ya existe un usuario con ese correo electrónico");
		}
		List<User> users = this.usersByIp.get(ip);
		if (users == null)
			users = new ArrayList<>();

		System.out.println("users = " + users);
		/*
		 * if (users.size() > 10) {
		 * System.out.println("No puedes crear más de 10 usuarios");
		 * throw new ResponseStatusException(HttpStatus.FORBIDDEN,
		 * "No puedes crear más de 10 usuarios");
		 * }
		 */

		user.setIp(ip);
		user.setConfirmado(false);
		user.setEsPagado(false);
		user.setFechaPago(null);
		// Crear token de confirmación
		String token = java.util.UUID.randomUUID().toString();
		user.setToken(token);
		users.add(user);

		this.usersByIp.put(ip, users);
		this.users.put(user.getEmail(), user);
		user.setCreationTime(System.currentTimeMillis());
		this.userDao.save(user);

		// Enviar correo de confirmación con token
		try {
			this.emailService.sendConfimacionEmail(user.getEmail(), token);
		} catch (org.json.JSONException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al enviar el correo de confirmación", e);
		}
	}


	public void login(User tryingUser) {
		this.find(tryingUser.getEmail(), tryingUser.getPwd());
	}

	public void clearAll() {
		this.limpiarListas();
		this.userDao.deleteAll();
	}

	public void limpiarListas() {
		this.usersByIp.clear();
		this.users.clear();
	}

	public Collection<User> getAllUsers() {
		this.limpiarListas();
		List<User> allUsers = this.userDao.findAll();
		for (User user : allUsers) {
			this.users.put(user.getEmail(), user);
			String userIp = user.getIp();
			if (userIp != null) {
				List<User> usersByIpList = this.usersByIp.get(userIp);
				if (usersByIpList == null) {
					usersByIpList = new ArrayList<>();
					this.usersByIp.put(userIp, usersByIpList);
				}
				usersByIpList.add(user);
			} else {
				// Manejo del caso cuando user.getIp() es null
				// Por ejemplo, puedes registrar un mensaje de advertencia
				System.out.println("Advertencia: El usuario " + user.getEmail() + " tiene una IP nula.");
			}
		}
		return allUsers;
	}

	public User find(String email, String pwd) {
		this.getAllUsers();
		User user = this.users.get(email);
		if (user == null || !user.getPwd().equals(pwd))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Credenciales incorrectas");
		return user;
	}

	public void delete(String email) {
		// Elimino del backend el usuario por el email
		this.userDao.deleteById(email);
		// Recargo los usuarios desde la base de datos
		this.getAllUsers();
	}

	public void actualizarPwd(String email, String pwd1, String pwd2) {
		// Verificar que el usuario existe
		User user = userDao.findById(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

		// Verificar que las contraseñas coincidan y cumplan los requisitos
		if (!pwd1.equals(pwd2)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
		}
		if (pwd1.length() < 4) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 4 caracteres");
		}

		// Actualizar la contraseña y guardar
		user.setPwd(pwd1); // El hash se aplica automáticamente en el método `setPwd` del modelo `User`
		userDao.save(user);
	}

	/*
	 * public synchronized void clearOld() {
	 * long time = System.currentTimeMillis();
	 * for (User user : this.users.values())
	 * if (time > 600_000 + user.getCreationTime())
	 * this.delete(user.getEmail());
	 * }
	 */
}
