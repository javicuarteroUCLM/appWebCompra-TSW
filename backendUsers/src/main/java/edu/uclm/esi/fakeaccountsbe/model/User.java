package edu.uclm.esi.fakeaccountsbe.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class User {
	@Id
	@Column(length = 60)
	private String email;
	private String pwd;

	@JsonIgnore
	@Column(length = 36)
	// @Transient: no se guarda en la base de datos
	private String token;

	@JsonIgnore
	@Transient
	private long creationTime;

	@Column(nullable = false)
	private boolean confirmado;

	// @JsonIgnore
	// @Transient
	private String ip;

	@Column(length = 36)
	private String cookie;

	@Column(nullable = false)
	private boolean esPagado;

	@Column
	private LocalDateTime fechaPago;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = org.apache.commons.codec.digest.DigestUtils.sha512Hex(pwd);
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getCookie() {
		return this.cookie;
	}

	public boolean isEsPagado() {
		return esPagado;
	}

	public void setEsPagado(boolean esPagado) {
		this.esPagado = esPagado;
	}

	public boolean isConfirmado() {
		return confirmado;
	}

	public void setConfirmado(boolean confirmado) {
		this.confirmado = confirmado;
	}

	public LocalDateTime getFechaPago() {
		return fechaPago;
	}

	public void setFechaPago(LocalDateTime fechaPago) {
		this.fechaPago = fechaPago;
	}
}
