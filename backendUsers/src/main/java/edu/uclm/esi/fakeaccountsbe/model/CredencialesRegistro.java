package edu.uclm.esi.fakeaccountsbe.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CredencialesRegistro {
	private String email;
	private String pwd1;
	private String pwd2;
	
	public void comprobar() {
		if (!pwd1.equals(pwd2))
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Las passwords no coinciden");
		if (pwd1.length()<4)
			throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "La contraseña tiene que tener 4 o más caracteres");
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPwd1() {
		return pwd1;
	}
	public void setPwd1(String pwd) {
		this.pwd1 = pwd;
	}
	public String getPwd2() {
		return pwd2;
	}
	public void setPwd2(String pwd) {
		this.pwd2 = pwd;
	}
	
}
