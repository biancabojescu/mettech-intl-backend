package mettech_intl.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable= false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name ="user_id", nullable = false)
    private Users user;

    @ManyToOne(optional = false)
    @JoinColumn(name="address_id", nullable = false)
    private Address address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<OrdersQuantities> quantities = new ArrayList<>();

    @Column(name = "status", nullable = false)
    private String status;

    public List<OrdersQuantities> getQuantities() {
        return quantities;
    }

    public void setQuantities(List<OrdersQuantities> quantities) {
        this.quantities = quantities;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
