package springboot.csye6225.UserWebApp.user;

import com.timgroup.statsd.StatsDClient;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springboot.csye6225.UserWebApp.message.Message;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;

@RestController
@RequestMapping
public class UserController {

    @Autowired
    private StatsDClient metrics;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //private StatsDClient metrics = new NonBlockingStatsDClient("csye6225_statsd","localhost",8125);

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
        metrics.incrementCounter("Health Check Controller");
        logger.info("Inside health check controller");
        message.setMessage("Everything is OK");
        message.setMessageToken("200 OK");
        return new ResponseEntity<Object>(userServices.getJSONMessageBody(message),HttpStatus.OK);
    }

    @GetMapping(produces = "application/json", path = "v1/user")
    public ResponseEntity<Object> informUser()
    {
        logger.error("User Id must be provided in the path and authorize your credentials");
        return new ResponseEntity<Object>("Please enter userId in the path and auth your creds",HttpStatus.BAD_REQUEST);
    }

    @GetMapping(produces = "application/json", path = "v1/user/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getUserDetails(@PathVariable("userId") Long id, HttpServletRequest httpRequest){
        metrics.incrementCounter("getUserDetails Controller");
        logger.info("Inside getUserDetails controller");
        ResponseEntity<Object> result = userServices.getUserDetails(id,httpRequest);

        if(!result.getStatusCode().equals(HttpStatus.OK))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            logger.info("Exiting getUserDetails controller");
            return new ResponseEntity<Object>(userServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else {
            logger.info("User details fetched successfully");
            logger.info("Exiting getUserDetails controller");
            return new ResponseEntity<Object>(result.getBody(),result.getStatusCode());
        }
    }

    @PostMapping(produces = "application/json", path = "v1/user")
    public ResponseEntity<Object> registerNewUser(@RequestBody User user){
        metrics.incrementCounter("registerNewUser Controller");
        logger.info("Inside registerNewUser controller");
        ResponseEntity<Object> result = userServices.registerNewUser(user);
        if(!result.getStatusCode().equals(HttpStatus.CREATED)){
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            logger.info("Exiting registerNewUser controller");
            return new ResponseEntity<Object>(userServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else {
            logger.info("Exiting registerNewUser controller");
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
        metrics.incrementCounter("updateUserDetails Controller");
        logger.info("Inside updateUserDetails controller");
        ResponseEntity<Object> result = userServices.updateUser(httpRequest,user,id);
        if(!result.getStatusCode().equals(HttpStatus.NO_CONTENT))
        {
            message.setMessage(result.getBody().toString());
            message.setMessageToken(result.getStatusCode().toString());
            logger.info("Exiting updateUserDetails controller");
            return new ResponseEntity<Object>(userServices.getJSONMessageBody(message),result.getStatusCode());
        }
        else
        {
            logger.info("Exiting updateUserDetails controller");
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
