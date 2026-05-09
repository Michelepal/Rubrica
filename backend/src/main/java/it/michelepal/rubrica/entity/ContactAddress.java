package it.michelepal.rubrica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "contact_addresses")
public class ContactAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(length = 160)
    private String street;

    @Column(length = 100)
    private String city;

    @Column(length = 80)
    private String province;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 80)
    private String country;

    @Column(name = "primary_address", nullable = false)
    private boolean primaryAddress;

    public Long getId() { return id; }
    public Contact getContact() { return contact; }
    public void setContact(Contact contact) { this.contact = contact; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public boolean isPrimaryAddress() { return primaryAddress; }
    public void setPrimaryAddress(boolean primaryAddress) { this.primaryAddress = primaryAddress; }
}

