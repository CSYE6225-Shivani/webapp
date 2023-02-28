package springboot.csye6225.UserWebApp.image;

import javax.persistence.*;

@Entity(name = "imageEntity")
@Table(name = "image",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "s3_bucket_path_unique",
                columnNames = "s3_bucket_path"
        )
})
public class Image {

    @Id
    @SequenceGenerator(
            name = "image_sequence",
            sequenceName = "image_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "image_sequence"
    )
    @Column(
            updatable = false,
            name = "image_id"
    )
    private Long image_id;

    @Column(
            updatable = false,
            nullable = false,
            name = "product_id"
    )
    private Long product_id;

    @Column(
            name = "file_name",
            columnDefinition = "TEXT"
    )
    private String file_name;

    @Column(
            name = "date_created",
            columnDefinition = "TEXT"
    )
    private String date_created;

    @Column(
            name = "s3_bucket_path",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String s3_bucket_path;

    public Long getImage_id() {
        return image_id;
    }

    public void setImage_id(Long image_id) {
        this.image_id = image_id;
    }

    public Long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getS3_bucket_path() {
        return s3_bucket_path;
    }

    public void setS3_bucket_path(String s3_bucket_path) {
        this.s3_bucket_path = s3_bucket_path;
    }

    public Image() {
    }

    public Image(String file_name, String s3_bucket_path) {
        this.file_name = file_name;
        this.s3_bucket_path = s3_bucket_path;
    }

    @Override
    public String toString() {
        return "Image{" +
                "image_id=" + image_id +
                ", product_id=" + product_id +
                ", file_name='" + file_name + '\'' +
                ", date_created='" + date_created + '\'' +
                ", s3_bucket_path='" + s3_bucket_path + '\'' +
                '}';
    }
}
