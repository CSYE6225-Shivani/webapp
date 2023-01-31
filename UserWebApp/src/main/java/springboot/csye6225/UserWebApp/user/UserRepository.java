package springboot.csye6225.UserWebApp.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    @Query("SELECT u FROM userEntity u WHERE u.username = ?1")
    Optional<User> findUserByUsername(String username);

}
