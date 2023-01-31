package springboot.csye6225.UserWebApp.user;

import javax.persistence.*;

import java.time.ZonedDateTime;

@Entity(name = "user_details")
@Table(name = "user_details",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "username_unique",
                        columnNames = "username"
                )
        }
)
public class User {

    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    private Long id;

    @Column(
            name = "first_name",
            columnDefinition = "TEXT"
    )
    private String first_name;

    @Column(
            name = "last_name",
            columnDefinition = "TEXT"
    )
    private String last_name;

    @Column(
            name = "username",
            nullable = false,
            columnDefinition = "TEXT",
            updatable = false
    )
    private String username;

    @Column(
            name = "password",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String password;

    @Column(
            name = "created",
            columnDefinition = "TIMESTAMP",
            updatable = false
    )
    private ZonedDateTime account_created;

    @Column(
            name = "account_updated",
            columnDefinition = "TIMESTAMP",
            updatable = false
    )
    private ZonedDateTime account_updated;

    public User() {
    }

    public User(String first_name,
                String last_name,
                String username,
                String password) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.username = username;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ZonedDateTime getAccount_created() {
        return account_created;
    }

    public void setAccount_created(ZonedDateTime account_created) {
        this.account_created = account_created;
    }

    public ZonedDateTime getAccount_updated() {
        return account_updated;
    }

    public void setAccount_updated(ZonedDateTime account_updated) {
        this.account_updated = account_updated;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", account_created=" + account_created +
                ", account_updated=" + account_updated +
                '}';
    }
}
