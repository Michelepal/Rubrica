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
@Table(name = "contact_emails")
public class ContactEmail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "primary_email", nullable = false)
    private boolean primaryEmail;

    public Long getId() { return id; }
    public Contact getContact() { return contact; }
    public void setContact(Contact contact) { this.contact = contact; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isPrimaryEmail() { return primaryEmail; }
    public void setPrimaryEmail(boolean primaryEmail) { this.primaryEmail = primaryEmail; }
}

