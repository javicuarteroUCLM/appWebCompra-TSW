package edu.uclm.esi.fakeaccountsbe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

	// @JsonIgnore
	@Transient
	private String ip;

	@Column(length = 36)
	private String cookie;

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
		return token;
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

	public static User findByToken(String token2) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'findByToken'");
	}
}
