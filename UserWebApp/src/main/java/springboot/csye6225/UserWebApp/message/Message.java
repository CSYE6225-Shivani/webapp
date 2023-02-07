package springboot.csye6225.UserWebApp.message;

import org.springframework.stereotype.Service;

@Service
public class Message {
    private String message;

    private String messageToken;

    public Message() {
    }

    public Message(String message, String messageToken) {
        this.message = message;
        this.messageToken = messageToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageToken() {
        return messageToken;
    }

    public void setMessageToken(String messageToken) {
        this.messageToken = messageToken;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", messageToken='" + messageToken + '\'' +
                '}';
    }
}
