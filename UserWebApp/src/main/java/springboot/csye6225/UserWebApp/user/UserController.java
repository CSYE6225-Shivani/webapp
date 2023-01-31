package springboot.csye6225.UserWebApp.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping
public class UserController {

    private final UserServices userServices;

    @Autowired
    public UserController(UserServices userServices) {
        this.userServices = userServices;
    }

    @GetMapping(path = "/healthz")
    public ResponseEntity<Object> healthy()
    {
        return new ResponseEntity<Object>("Everything is healthy",HttpStatus.OK);
    }

    @GetMapping(path = "v1/user")
    public ResponseEntity<Object> informUser()
    {
        return new ResponseEntity<Object>("Please enter your username in the path and auth your creds",HttpStatus.BAD_REQUEST);
    }

    @GetMapping(path ="v1/user/{username}")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getUserDetails(@PathVariable("username") String username, HttpServletRequest httpRequest){
        ResponseEntity<Object> request_header = userServices.performBasicAuth(httpRequest);

        if(request_header.getStatusCode() == HttpStatus.BAD_REQUEST)
        {
            return request_header;
        }
        else if(!username.equals(request_header.getBody().toString()))
        {
            return new ResponseEntity<>("Login Credentials and path does not match so you are forbidden",HttpStatus.FORBIDDEN);
        }
        else
        {
            User usr = userServices.fetchUser(request_header.getBody().toString());
            return new ResponseEntity<Object>(userServices.getJSONBody(usr), HttpStatus.OK);
        }
    }

    @PostMapping(produces = "application/json",
            path = "v1/user")
    public ResponseEntity<Object> registerNewUser(@RequestBody User user){
        return userServices.registerNewUser(user);
    }

    @PutMapping(path = "v1/user")
    public ResponseEntity<Object> informUserPut()
    {
        return new ResponseEntity<Object>("Please enter your username in the path and authorize your creds",HttpStatus.BAD_REQUEST);
    }

    @PutMapping(path = "v1/user/{username}")
    public ResponseEntity<Object> updateUserDetails(@PathVariable("username") String username, HttpServletRequest httpRequest, @RequestBody User user)
    {
        return userServices.updateUserDetails(httpRequest,user,username);
    }

    @DeleteMapping(path = "v1/user/{username}")
    public ResponseEntity<Object> deleteUser(@PathVariable("username") String username){
        return new ResponseEntity<Object>("Delete API is not Implemented",HttpStatus.NOT_IMPLEMENTED);
    }

    @PatchMapping("v1/user/{username}")
    public ResponseEntity<Object> patchUser(@PathVariable("username") String username){
        return new ResponseEntity<Object>("Patch API is not Implemented",HttpStatus.NOT_IMPLEMENTED);
    }

}
