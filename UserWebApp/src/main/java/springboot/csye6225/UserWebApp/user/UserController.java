package springboot.csye6225.UserWebApp.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springboot.csye6225.UserWebApp.message.Message;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping
public class UserController {

    private UserServices userServices;

    @Autowired
    Message message;

    @Autowired
    public UserController(UserServices userServices) {
        this.userServices = userServices;
    }

    @GetMapping(produces = "application/json",path = "/healthz")
    public ResponseEntity<Object> healthy()
    {
        message.setMessage("Everything is OK");
        message.setMessageToken("200 OK");
        return new ResponseEntity<Object>(userServices.getJSONMessageBody(message),HttpStatus.OK);
    }

    @GetMapping(produces = "application/json", path = "v1/user")
    public ResponseEntity<Object> informUser()
    {

        return new ResponseEntity<Object>("Please enter userId in the path and auth your creds",HttpStatus.BAD_REQUEST);
    }

    @GetMapping(produces = "application/json", path = "v1/user/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getUserDetails(@PathVariable("userId") Long id, HttpServletRequest httpRequest){
        ResponseEntity<Object> result = userServices.getUserDetails(id,httpRequest);

        if(!result.getStatusCode().equals(HttpStatus.OK))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            return new ResponseEntity<Object>(userServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else {
            return new ResponseEntity<Object>(result.getBody(),result.getStatusCode());
        }
    }

    @PostMapping(produces = "application/json", path = "v1/user")
    public ResponseEntity<Object> registerNewUser(@RequestBody User user){
        ResponseEntity<Object> result = userServices.registerNewUser(user);
        if(!result.getStatusCode().equals(HttpStatus.CREATED)){
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            return new ResponseEntity<Object>(userServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else {
            return new ResponseEntity<Object>(result.getBody(),result.getStatusCode());
        }
    }

    @PutMapping(produces = "application/json",path = "v1/user")
    public ResponseEntity<Object> informUserPut()
    {
        return new ResponseEntity<Object>("Please enter your username in the path and authorize your creds",HttpStatus.BAD_REQUEST);
    }

    @PutMapping(produces = "application/json",path = "v1/user/{userId}")
    public ResponseEntity<Object> updateUserDetails(@PathVariable("userId") Long id, HttpServletRequest httpRequest, @RequestBody User user)
    {
        ResponseEntity<Object> result = userServices.updateUser(httpRequest,user,id);
        if(!result.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            return new ResponseEntity<Object>(userServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else
        {
            return new ResponseEntity<Object>("Product updated successfully!",result.getStatusCode());
        }
    }

    @DeleteMapping(produces = "application/json",path = "v1/user/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable("userId") Long id){
        message.setMessage("Delete API is not Implemented");
        message.setMessageToken("501 Not Implemented");
        return new ResponseEntity<Object>(userServices.getJSONMessageBody(message),HttpStatus.NOT_IMPLEMENTED);
    }

    @PatchMapping(produces = "application/json",path = "v1/user/{userId}")
    public ResponseEntity<Object> patchUser(@PathVariable("userId") Long id){
        message.setMessage("Patch API is not Implemented");
        message.setMessageToken("501 Not Implemented");
        return new ResponseEntity<Object>(userServices.getJSONMessageBody(message),HttpStatus.NOT_IMPLEMENTED);
    }

}
