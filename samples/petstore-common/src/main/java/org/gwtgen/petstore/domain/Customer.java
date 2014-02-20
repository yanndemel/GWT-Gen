package org.gwtgen.petstore.domain;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.client.exception.ValidationException;
import com.hiperf.common.ui.client.i18n.INakedConstants;
import com.hiperf.common.ui.shared.annotation.UIAttribute;
import com.hiperf.common.ui.shared.annotation.UIClass;

/**
 * @author Antonio Goncalves
 *         http://www.antoniogoncalves.org
 *         --
 */

@Entity
@UIClass(importable=true)
public class Customer implements INakedObject  {

    // ======================================
    // =             Attributes             =
    // ======================================

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true, nullable = false, length = 10)
    @NotNull
    private String login;
    @Column(nullable = false, length = 10)
    @NotNull
    @Size(min = 1, max = 10)
    private String password;
    @Column(nullable = false)
    @NotNull
    @Size(min = 2, max = 50)
    private String firstname;
    @Column(nullable = false)
    @NotNull
    @Size(min = 2, max = 50)
    private String lastname;
    private String telephone;
    @Pattern(regexp=INakedConstants.REGEX_EMAIL)
    private String email;
    @ManyToOne
    @Valid
    private Address homeAddress;
    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;
    @Transient
    private Integer age;
    
    @ManyToMany(targetEntity=Hobby.class)
    @JoinTable(name="t_cus_hobby",
    joinColumns=
        @JoinColumn(name="CUS_ID", referencedColumnName="ID"),
    inverseJoinColumns=
        @JoinColumn(name="HOB_ID", referencedColumnName="ID")
    )
    private Set<Hobby> hobbies;

    // ======================================
    // =             Constants              =
    // ======================================

    public static final String FIND_BY_LOGIN = "Customer.findByLogin";
    public static final String FIND_BY_LOGIN_PASSWORD = "Customer.findByLoginAndPassword";
    public static final String FIND_ALL = "Customer.findAll";

    // ======================================
    // =            Constructors            =
    // ======================================

    public Customer() {
    }

    public Customer(String firstname, String lastname, String login, String password, String email, Address address) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.login = login;
        this.password = password;
        this.email = email;
        this.homeAddress = address;
    }

    

    // ======================================
    // =              Public Methods        =
    // ======================================

    /**
     * Given a password, this method then checks if it matches the user
     *
     * @param pwd Password
     * @throws ValidationException thrown if the password is empty or different than the one
     *                             store in database
     */
    public void matchPassword(String pwd) throws ValidationException {
        if (pwd == null || "".equals(pwd))
            throw new ValidationException("Invalid password");

        // The password entered by the customer is not the same stored in database
        if (!pwd.equals(password))
            throw new ValidationException("Passwords don't match");
    }

    // ======================================
    // =         Getters & setters          =
    // ======================================

    @UIAttribute(hidden = true)
    public Long getId() {
        return id;
    }
    
    

    public void setId(Long id) {
		this.id = id;
	}

    @UIAttribute
	public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @UIAttribute
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @UIAttribute
    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @UIAttribute
    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @UIAttribute
    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @UIAttribute
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    @UIAttribute
    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

    @UIAttribute
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @UIAttribute
    public Integer getAge() {
        return age;
    }

    // ======================================
    // =   Methods hash, equals, toString   =
    // ======================================

   

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if(firstname != null)
        	sb.append(firstname).append(" ");
        if(lastname != null)
        	sb.append(lastname);
        return sb.toString();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((age == null) ? 0 : age.hashCode());
		result = prime * result
				+ ((dateOfBirth == null) ? 0 : dateOfBirth.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result
				+ ((firstname == null) ? 0 : firstname.hashCode());
		result = prime * result
				+ ((homeAddress == null) ? 0 : homeAddress.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((lastname == null) ? 0 : lastname.hashCode());
		result = prime * result + ((login == null) ? 0 : login.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result
				+ ((telephone == null) ? 0 : telephone.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Customer other = (Customer) obj;
		if (age == null) {
			if (other.age != null)
				return false;
		} else if (!age.equals(other.age))
			return false;
		if (dateOfBirth == null) {
			if (other.dateOfBirth != null)
				return false;
		} else if (!dateOfBirth.equals(other.dateOfBirth))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (firstname == null) {
			if (other.firstname != null)
				return false;
		} else if (!firstname.equals(other.firstname))
			return false;
		if (homeAddress == null) {
			if (other.homeAddress != null)
				return false;
		} else if (!homeAddress.equals(other.homeAddress))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lastname == null) {
			if (other.lastname != null)
				return false;
		} else if (!lastname.equals(other.lastname))
			return false;
		if (login == null) {
			if (other.login != null)
				return false;
		} else if (!login.equals(other.login))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (telephone == null) {
			if (other.telephone != null)
				return false;
		} else if (!telephone.equals(other.telephone))
			return false;
		return true;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@UIAttribute
	public Set<Hobby> getHobbies() {
		return hobbies;
	}

	public void setHobbies(Set<Hobby> hobbies) {
		this.hobbies = hobbies;
	}
    
	
    
}
