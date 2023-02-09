package springboot.csye6225.UserWebApp.user;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import springboot.csye6225.UserWebApp.message.Message;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServices {

    public UserRepository userRepository;

    @Autowired
    public UserServices(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserServices() {

    }

    public ResponseEntity<Object> registerNewUser(User user) {

        if(!performNullCheck(user).getStatusCode().equals(HttpStatus.OK))
        {
           return new ResponseEntity<Object>(performNullCheck(user).getBody(),performNullCheck(user).getStatusCode());
        }
        else if(user.getId() != null ||
                user.getAccount_created() != null ||
                user.getAccount_updated() != null)
        {
            return new ResponseEntity<Object>("Please do not provide user_id, account_created or account_updated date",HttpStatus.BAD_REQUEST);
        }
        else if(validateUserDetails(user).getStatusCode() != HttpStatus.OK)
        {
            return new ResponseEntity<Object>(validateUserDetails(user).getBody(),validateUserDetails(user).getStatusCode());
        }
        else if(validateUsername(user).getStatusCode() != HttpStatus.OK)
        {
            return new ResponseEntity<Object>("Username is taken. Please enter another username",HttpStatus.BAD_REQUEST);
        }
        else {
            createUser(user);
            return new ResponseEntity<Object>(getJSONBody(user),HttpStatus.CREATED);
        }
    }

    private ResponseEntity<Object> validateUserDetails(User user) {

        //Name
        String regexName = "^[A-Za-z]{5,20}$";
        Pattern pName = Pattern.compile(regexName);
        Matcher first_name = pName.matcher((user.getFirst_name() == null)?"":user.getFirst_name());
        Matcher last_name = pName.matcher((user.getLast_name() == null)?"": user.getLast_name());

        //Email
        String regexEmail = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)" +
                "*@[^-][A-Za-z0-9-]" +
                "+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        Pattern pEmail = Pattern.compile(regexEmail);
        Matcher email = pEmail.matcher((user.getUsername() == null)?"":user.getUsername());

        if(!first_name.matches())
        {
            return new ResponseEntity<Object>("First Name cannot contain digits, special characters or it cannot be blank. Please re-enter correct name.",HttpStatus.BAD_REQUEST);
        }

        else if (!last_name.matches()) {
            return new ResponseEntity<Object>("Last Name cannot contain digits, special characters or it cannot be blank. Please re-enter correct name.",HttpStatus.BAD_REQUEST);
        }

        else if(!email.matches())
        {
            return new ResponseEntity<Object>("Invalid email format.",HttpStatus.BAD_REQUEST);
        }

        else if (user.getPassword().trim().length() == 0)
        {
            return new ResponseEntity<Object>("Password field cannot be empty",HttpStatus.BAD_REQUEST);
        }
        else {
            return new ResponseEntity<Object>("Fields pass validation",HttpStatus.OK);
        }
    }

    public ResponseEntity<Object> validateUsername(User user)
    {
        if(userRepository.findUserByUsername(user.getUsername()).isPresent())
        {
            return new ResponseEntity<Object>("Username is taken. Please enter another username",HttpStatus.FORBIDDEN);
        }
        else
        {
            return new ResponseEntity<Object>("Username is allowed",HttpStatus.OK);
        }
    }


    public ResponseEntity<Object> performNullCheck(User user)
    {
        if(user.getFirst_name() == null || user.getFirst_name().trim().length() == 0
                || user.getLast_name() == null || user.getLast_name().trim().length() == 0
                || user.getUsername() == null || user.getUsername().trim().length() == 0 ||
                user.getPassword() == null || user.getPassword().trim().length() == 0)
//
//            if(user.getFirst_name() == null
//                    || user.getLast_name() == null
//                    || user.getUsername() == null ||
//                    user.getPassword() == null || user.getPassword().trim().length() == 0)
        {
            return new ResponseEntity<Object>("Please verify if you have provided your first_name, last_name, username and password without changing field labels",HttpStatus.BAD_REQUEST);
        }
        else {
            return new ResponseEntity<Object>("All fields are provided",HttpStatus.OK);
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
        user.setAccount_created(timeInZ.toString());
        user.setAccount_updated(timeInZ.toString());
        userRepository.save(user);
        return new ResponseEntity<Object>("User created successfully!",HttpStatus.CREATED);
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

    public ResponseEntity<Object> performBasicAuth(HttpServletRequest request) {
        String loginDetails[];
        String header = request.getHeader("Authorization");
        if(header == null || header.endsWith("0g=="))
        {
            return new ResponseEntity<Object>("Please provide username and password", HttpStatus.UNAUTHORIZED);
        }
        User fetchedUser;

        if(header.endsWith("0g=="))
        {
            return new ResponseEntity<Object>("Please provide username and password", HttpStatus.UNAUTHORIZED);
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
                return new ResponseEntity<Object>("Please check login credentials", HttpStatus.BAD_REQUEST);
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

        map.put("id",usr.getId());
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

    public String[] decodeLogin(String header) {
        String[] credentials = header.split(" ");
        String getPassword;
        byte[] getPwd;
        getPwd = Base64.decodeBase64(credentials[1]);
        getPassword = new String(getPwd);
        String[] authDetails = getPassword.split(":");
        return authDetails;
    }

    public ResponseEntity<Object> updateUser(HttpServletRequest httpRequest, User user, Long id)
    {
        ResponseEntity<Object> request_header = performBasicAuth(httpRequest);
        String userDetails = httpRequest.getHeader("Authorization");
        String[] userCredentials = decodeLogin(userDetails);
        User current = userCredentials.length == 0?null:fetchUser(userCredentials[0]);

        if(request_header.getStatusCode() == HttpStatus.BAD_REQUEST ||
                request_header.getStatusCode() == HttpStatus.UNAUTHORIZED)
        {
            return request_header;
        }

        else if (current!= null && current.getId() != id)
        {
            return new ResponseEntity<Object>("Login Credentials and path does not match",HttpStatus.FORBIDDEN);
        }

        else if(performNullCheck(user).getStatusCode() != HttpStatus.OK)
        {
            return new ResponseEntity<Object>("Please verify if you have provided your first_name, last_name, username and password without changing field labels",HttpStatus.BAD_REQUEST);
        }
        else if(user.getId() != null ||
                user.getAccount_created() != null ||
                user.getAccount_updated() != null)
        {
            return new ResponseEntity<Object>("Please do not provide user_id, account_created or account_updated date",HttpStatus.BAD_REQUEST);
        }
        else if(validateUserDetails(user).getStatusCode() != HttpStatus.OK)
        {
            return new ResponseEntity<Object>(validateUserDetails(user).getBody(),validateUserDetails(user).getStatusCode());
        }
        else if(current != null && !current.getUsername().equals(user.getUsername()))
        {
            return new ResponseEntity<Object>("You cannot update username",HttpStatus.BAD_REQUEST);
        }
        else
        {
            if(current != null) {
                LocalDateTime localNow = LocalDateTime.now();
                ZonedDateTime updatedTimeInZ = localNow.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z"));

                current.setFirst_name(user.getFirst_name());
                current.setLast_name(user.getLast_name());
                current.setAccount_updated(updatedTimeInZ.toString());
                current.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
                userRepository.save(current);

                return new ResponseEntity<Object>("User Updated successfully!", HttpStatus.NO_CONTENT);
            }
            else {
                return new ResponseEntity<Object>("User does not exist",HttpStatus.NOT_FOUND);
            }
        }
    }

    public ResponseEntity<Object> getUserDetails(Long id, HttpServletRequest httpRequest) {
        ResponseEntity<Object> request_header = performBasicAuth(httpRequest);
        String userDetails = httpRequest.getHeader("Authorization");
        String[] userCredentials = decodeLogin(userDetails);
        User current =  userCredentials.length == 0?null:fetchUser(userCredentials[0]);

        if(request_header.getStatusCode() == HttpStatus.BAD_REQUEST ||
                request_header.getStatusCode() == HttpStatus.UNAUTHORIZED)
        {
            return request_header;
        }
        else if (current.getId() != id)
        {
            return new ResponseEntity<Object>("Login Credentials and path does not match so you are forbidden",HttpStatus.FORBIDDEN);
        }
        else
        {
            return new ResponseEntity<Object>(getJSONBody(current), HttpStatus.OK);
        }
    }

    public HashMap<String,String> getJSONMessageBody(Message message) {
        HashMap<String,String> map = new HashMap<>();

        map.put("Message:",message.getMessage());
        map.put("Status Code:",message.getMessageToken());
        return map;
    }
}
