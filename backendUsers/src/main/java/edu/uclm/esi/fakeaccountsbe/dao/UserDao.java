package edu.uclm.esi.fakeaccountsbe.dao;
import edu.uclm.esi.fakeaccountsbe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;




public interface UserDao extends JpaRepository<User, String> {

    User findByCookie(String fakeUserId);

    User findByToken(String token);
}