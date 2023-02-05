package springboot.csye6225.UserWebApp.product;

import springboot.csye6225.UserWebApp.user.User;

import javax.persistence.*;

@Entity(name = "productEntity")
@Table(name = "product",
uniqueConstraints = {
        @UniqueConstraint(
                name = "sku_unique",
                columnNames = "sku"
        )
})
public class Product {

    @Id
    @SequenceGenerator(
            name = "product_sequence",
            sequenceName = "product_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "product_sequence"
    )
    @Column(name = "id",
    updatable = false)
    private Long id;

    @Column(
            name = "name",
            columnDefinition = "TEXT"
    )
    private String name;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "sku",
            columnDefinition = "TEXT"
    )
    private String sku;

    @Column(
            name = "manufacturer",
            columnDefinition = "TEXT"
    )
    private String manufacturer;

    @Column(
            name = "quantity",
            columnDefinition = "INTEGER"
    )
    private Long quantity;

    @Column(
            name = "date_added",
            columnDefinition = "TEXT"
    )
    private String date_added;

    @Column(
            name = "date_last_updated",
            columnDefinition = "TEXT"
    )
    private String date_last_updated;

    @Column(
            name = "owner_user_id",
            columnDefinition = "INTEGER"
    )
    private Long owner_user_id;

    public Product() {
    }

    public Product(String name, String description, String sku, String manufacturer, Long quantity) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.manufacturer = manufacturer;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getDate_added() {
        return date_added;
    }

    public void setDate_added(String date_added) {
        this.date_added = date_added;
    }

    public String getDate_last_updated() {
        return date_last_updated;
    }

    public void setDate_last_updated(String date_last_updated) {
        this.date_last_updated = date_last_updated;
    }

    public Long getOwner_user_id() {
        return owner_user_id;
    }

    public void setOwner_user_id(Long owner_user_id) {
        this.owner_user_id = owner_user_id;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", sku='" + sku + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", quantity=" + quantity +
                ", date_added='" + date_added + '\'' +
                ", date_last_updated='" + date_last_updated + '\'' +
                ", owner_user_id=" + owner_user_id +
                '}';
    }
}
