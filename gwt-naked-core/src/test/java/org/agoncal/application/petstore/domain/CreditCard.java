package org.agoncal.application.petstore.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.hiperf.common.ui.client.INakedObject;

/**
 * @author Antonio Goncalves
 *         http://www.antoniogoncalves.org
 *         --
 */

@Entity
public class CreditCard implements INakedObject {

    // ======================================
    // =             Attributes             =
    // ======================================

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "credit_card_number", length = 30)
    @NotNull
    @Size(min = 1, max = 30)
    private String creditCardNumber;
    @Column(name = "credit_card_type")
    @NotNull
    @Enumerated(EnumType.STRING)
    private CreditCardType creditCardType;
    @Column(name = "credit_card_expiry_date", length = 5)
    @NotNull
    @Size(min = 1, max = 5)
    private String creditCardExpDate;

    // ======================================
    // =            Constructors            =
    // ======================================

    public CreditCard() {
    }

    public CreditCard(String creditCardNumber, CreditCardType creditCardType, String creditCardExpDate) {
        this.creditCardNumber = creditCardNumber;
        this.creditCardType = creditCardType;
        this.creditCardExpDate = creditCardExpDate;
    }

    // ======================================
    // =         Getters & setters          =
    // ======================================

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public CreditCardType getCreditCardType() {
        return creditCardType;
    }

    public void setCreditCardType(CreditCardType creditCardType) {
        this.creditCardType = creditCardType;
    }

    public String getCreditCardExpDate() {
        return creditCardExpDate;
    }

    public void setCreditCardExpDate(String creditCardExpDate) {
        this.creditCardExpDate = creditCardExpDate;
    }

    // ======================================
    // =   Methods hash, equals, toString   =
    // ======================================

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((creditCardExpDate == null) ? 0 : creditCardExpDate
						.hashCode());
		result = prime
				* result
				+ ((creditCardNumber == null) ? 0 : creditCardNumber.hashCode());
		result = prime * result
				+ ((creditCardType == null) ? 0 : creditCardType.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		CreditCard other = (CreditCard) obj;
		if (creditCardExpDate == null) {
			if (other.creditCardExpDate != null)
				return false;
		} else if (!creditCardExpDate.equals(other.creditCardExpDate))
			return false;
		if (creditCardNumber == null) {
			if (other.creditCardNumber != null)
				return false;
		} else if (!creditCardNumber.equals(other.creditCardNumber))
			return false;
		if (creditCardType != other.creditCardType)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

 

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
    
    
}