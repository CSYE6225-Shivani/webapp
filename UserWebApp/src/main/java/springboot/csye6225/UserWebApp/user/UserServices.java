package springboot.csye6225.UserWebApp.user;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class UserServices {

    private UserRepository userRepository;

    @Autowired
    public UserServices(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserServices() {

    }

    public ResponseEntity<Object> registerNewUser(User user) {

        if(performNullCheck(user).getStatusCode() == HttpStatus.OK &&
                emailValidation(user).getStatusCode() == HttpStatus.OK &&
                emailFormatCheck(user).getStatusCode() == HttpStatus.OK &&
                emailValidation(user).getStatusCode() == HttpStatus.OK)
        {
            return createUser(user);
        }
        else
        {
            String body;
            if(performNullCheck(user).getStatusCode() != HttpStatus.OK)
            {
                body = performNullCheck(user).getBody().toString();
            }
            else if(emailValidation(user).getStatusCode() != HttpStatus.OK)
            {
                body = emailValidation(user).getBody().toString();
            }
            else if(emailFormatCheck(user).getStatusCode() != HttpStatus.OK)
            {
                body = emailFormatCheck(user).getBody().toString();
            }
            else {
                body = emailValidation(user).getBody().toString();
            }
            return new ResponseEntity<Object>(
                    body,
                    HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Object> performNullCheck(User user)
    {
        if(user.getFirst_name() == null
                || user.getLast_name() == null
                || user.getUsername() == null ||
                user.getPassword() == null)
        {
            return new ResponseEntity<Object>("Please verify if you have provided your firstName, lastName, userName and password",
                    HttpStatus.BAD_REQUEST);
        }
        else {
            return new ResponseEntity<Object>(HttpStatus.OK);
        }
    }

    public ResponseEntity<Object> emailFormatCheck(User user)
    {
        if(!EmailValidator.getInstance().isValid(user.getUsername()))
        {
            return new ResponseEntity<Object>("Invalid email format.",HttpStatus.BAD_REQUEST);
        }
        else {
            return new ResponseEntity<Object>(HttpStatus.OK);
        }

    }

    public ResponseEntity<Object> emailValidation(User user)
    {
        if(userRepository.findUserByUsername(user.getUsername()).isPresent())
        {
            return new ResponseEntity<Object>("Username is taken. Please enter another username",
                    HttpStatus.BAD_REQUEST);
        }
        else {
            return new ResponseEntity<Object>(HttpStatus.OK);
        }

    }

    public ResponseEntity<Object> createUser(User user)
    {
        LocalDateTime localNow = LocalDateTime.now();
        ZonedDateTime timeInZ = localNow.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z"));

        user.setFirst_name(user.getFirst_name());
        user.setLast_name(user.getLast_name());
        user.setUsername(user.getUsername());
        user.setPassword(BCrypt.hashpw(user.getPassword(),BCrypt.gensalt(10)));
        user.setAccount_created(timeInZ);
        user.setAccount_updated(timeInZ);
        userRepository.save(user);
        return new ResponseEntity<Object>("User created successfully!",HttpStatus.CREATED);
    }

    public ResponseEntity<Object> performBasicAuth(HttpServletRequest request) {
        String loginDetails[];
        String header = request.getHeader("Authorization");
        if(header == null || header.endsWith("0g=="))
        {
            return new ResponseEntity<Object>("Please provide username and password", HttpStatus.BAD_REQUEST);
        }
        User fetchedUser;

        if(header.endsWith("0g=="))
        {
            return new ResponseEntity<Object>("Please provide username and password", HttpStatus.BAD_REQUEST);
        }
        else if(header != null && header.startsWith("Basic"))
        {
            loginDetails = decodeLogin(header);
        }
        else {
            return new ResponseEntity<Object>("Invalid Header",HttpStatus.BAD_REQUEST);
        }

        if(loginDetails.length > 1) {
            //checking if the email is present in the DB
            if (!validateUserExists(loginDetails[0])) {
                return new ResponseEntity<Object>("User does not exist in the DB", HttpStatus.BAD_REQUEST);
            }

            //finding the user in the DB
            else {
                fetchedUser = fetchUser(loginDetails[0]);
                BCryptPasswordEncoder pwd_encoder = new BCryptPasswordEncoder();

                if (!pwd_encoder.matches(loginDetails[1],fetchedUser.getPassword())) {
                    return new ResponseEntity<Object>("Password is invalid. Please enter again", HttpStatus.BAD_REQUEST);
                } else {
                    return new ResponseEntity<Object>(fetchedUser.getUsername(), HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<Object>("Please check if username and pwd is provided for authentication",HttpStatus.UNAUTHORIZED);
    }

    public User fetchUser(String username) {
        User loggedInUser = new User();
        List<User> userList;

        userList = getAllUsersInDB();

        for(User usr:userList)
        {
            if(usr.getUsername().equals(username))
            {
                loggedInUser = usr;
            }
        }
        return loggedInUser;
    }

    private boolean validateUserExists(String usrname) {
        if(usrname == null)
            return false;
        List<User> userList = new ArrayList<>();
        userList = getAllUsersInDB();
        for (User usr:userList)
        {
            if(usr.getUsername().equals(usrname))
            {
                return true;
            }
        }
        return false;
    }

    public HashMap<String,Object> getJSONBody(User usr)
    {
        HashMap<String,Object> map = new HashMap<>();
        map.put("id",usr.getId().toString());
        map.put("first_name",usr.getFirst_name());
        map.put("last_name",usr.getLast_name());
        map.put("username",usr.getUsername());
        map.put("account_created",usr.getAccount_created());
        map.put("account_updated",usr.getAccount_updated());

        return map;
    }
    private List<User> getAllUsersInDB() {
        return userRepository.findAll();
    }

    private String[] decodeLogin(String header) {
        String[] credentials = header.split(" ");
        String getPassword;
        byte[] getPwd;
        getPwd = Base64.decodeBase64(credentials[1]);
        getPassword = new String(getPwd);
        String[] authDetails = getPassword.split(":");
        return authDetails;
    }

    public ResponseEntity<Object> updateUserDetails(HttpServletRequest httpRequest, User user ,String username) {
        ResponseEntity<Object> req_header = performBasicAuth(httpRequest);
        User usr = fetchUser(user.getUsername());

        if(req_header.getStatusCode().equals(HttpStatus.BAD_REQUEST))
        {
            return req_header;
        }

        else if(!validateUserExists(user.getUsername()) || !username.equals(user.getUsername()))
        {
            return new ResponseEntity<Object>(
                    "You cannot update username field!",
                    HttpStatus.FORBIDDEN
            );
        }

        else if((user.getAccount_created() != null && (user.getAccount_created().toString().length() != usr.getAccount_created().toString().length())) ||
                (user.getAccount_updated() != null && (user.getAccount_updated().toString().length() != usr.getAccount_updated().toString().length())) ||
                (user.getId()!= null && (user.getId() != usr.getId()) || ((user.getUsername() != null) && !user.getUsername().equals(usr.getUsername()))))
        {
            return new ResponseEntity<Object>("You can only change firstName, lastName or password",HttpStatus.FORBIDDEN);
        }

        else if(performNullCheck(user).getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            return new ResponseEntity<Object>(
                    "Please check if firstName, lastName, userName or Pwd is not empty.",
                    HttpStatus.BAD_REQUEST);
        }

        else
        {
            String usrname = req_header.getBody().toString();
            if(!user.getUsername().equals(usrname))
            {
                return new ResponseEntity<Object>(
                        "You can only update your credentials.",
                        HttpStatus.FORBIDDEN
                );
            }

            else {
                updateUsr(usr,user);
                return new ResponseEntity<Object>(
                        getJSONBody(fetchUser(usrname)),
                        HttpStatus.OK
                );
            }
        }
    }

    private void updateUsr(User usr, User user) {
        LocalDateTime localNow = LocalDateTime.now();
        ZonedDateTime updatedTimeInZ = localNow.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z"));
        usr.setFirst_name(user.getFirst_name());
        usr.setLast_name(user.getLast_name());
        usr.setAccount_updated(updatedTimeInZ);
        usr.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));

        userRepository.save(usr);
    }
}
