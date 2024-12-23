package edu.uclm.esi.fakeaccountsbe.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.uclm.esi.fakeaccountsbe.model.User;

public interface UserDao extends JpaRepository<User, String> {

    User findByCookie(String fakeUserId);
}
