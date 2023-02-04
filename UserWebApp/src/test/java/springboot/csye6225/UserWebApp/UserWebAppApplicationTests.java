package springboot.csye6225.UserWebApp;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springboot.csye6225.UserWebApp.user.User;
import springboot.csye6225.UserWebApp.user.UserServices;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testable
class AppApplicationTests {

	UserServices userServices = new UserServices();

	@Test
	public void checkCreateAUserEndpoint()
	{
		String firstName = "Anita";
		String lastName = "Chavan";
		String username = "anita@gmail.com";
		String password = "pwd";

		//Created a dummy user
		User usr = new User();
		usr.setFirst_name(firstName);
		usr.setLast_name(lastName);
		usr.setUsername(username);
		usr.setPassword(password);

		ResponseEntity<Object> result = userServices.performNullCheck(usr);

		assertEquals(result.getStatusCode(),(HttpStatus.OK));
	}

	@Test
	public void demoTest(){
		assert (1 == 1);
	}
}
