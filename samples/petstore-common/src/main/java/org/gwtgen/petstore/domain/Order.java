package org.gwtgen.petstore.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.shared.annotation.UIAttribute;
import com.hiperf.common.ui.shared.annotation.UIClass;

/**
 * @author Antonio Goncalves
 *         http://www.antoniogoncalves.org
 *         --
 */

@Entity
@Table(name = "t_order")
@UIClass(importable = true)
public class Order implements INakedObject {

    // ======================================
    // =             Attributes             =
    // ======================================

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "order_date", updatable = false)
    @Temporal(TemporalType.DATE)
    private Date orderDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_fk", nullable = false)
    private Customer customer;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "order")   
    private List<OrderLine> orderLines;
    @ManyToOne
    private Address deliveryAddress;
    @ManyToOne
    private CreditCard creditCard;

    // ======================================
    // =            Constructors            =
    // ======================================

    public Order() {
    }

    public Order(Customer customer, CreditCard creditCard, Address deliveryAddress) {
        this.customer = customer;
        this.creditCard = creditCard;
        this.deliveryAddress = deliveryAddress;
    }

    // ======================================
    // =          Lifecycle Methods         =
    // ======================================

    

    // ======================================
    // =              Public Methods        =
    // ======================================

    public Float getTotal() {
        if (orderLines == null || orderLines.isEmpty())
            return 0f;

        Float total = 0f;

        // Sum up the quantities
        for (OrderLine orderLine : orderLines) {
            total += (orderLine.getSubTotal());
        }

        return total;
    }

    // ======================================
    // =         Getters & setters          =
    // ======================================

    @UIAttribute(hidden=true)
    public Long getId() {
        return id;
    }

    @UIAttribute(index=2)
    public Date getOrderDate() {
        return orderDate;
    }

    @UIAttribute(index=1)
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @UIAttribute(index=3)
    public List<OrderLine> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(List<OrderLine> orderLines) {
        this.orderLines = orderLines;
    }

    @UIAttribute(index=4)
    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    @UIAttribute(index=5)
    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public String getCreditCardNumber() {
        return creditCard.getCreditCardNumber();
    }

    public void setCreditCardNumber(String creditCardNumber) {
        creditCard.setCreditCardNumber(creditCardNumber);
    }

    public CreditCardType getCreditCardType() {
        return creditCard.getCreditCardType();
    }

    public void setCreditCardType(CreditCardType creditCardType) {
        creditCard.setCreditCardType(creditCardType);
    }

    @UIAttribute(index=6, redrawOnUpdateLinkedObject = "creditCard")
    public String getCreditCardExpiryDate() {
        return creditCard != null ? creditCard.getCreditCardExpDate() : null;
    }

    

    // ======================================
    // =   Methods hash, equals, toString   =
    // ======================================

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((creditCard == null) ? 0 : creditCard.hashCode());
		result = prime * result
				+ ((customer == null) ? 0 : customer.hashCode());
		result = prime * result
				+ ((deliveryAddress == null) ? 0 : deliveryAddress.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((orderDate == null) ? 0 : orderDate.hashCode());
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
		Order other = (Order) obj;
		if (creditCard == null) {
			if (other.creditCard != null)
				return false;
		} else if (!creditCard.equals(other.creditCard))
			return false;
		if (customer == null) {
			if (other.customer != null)
				return false;
		} else if (!customer.equals(other.customer))
			return false;
		if (deliveryAddress == null) {
			if (other.deliveryAddress != null)
				return false;
		} else if (!deliveryAddress.equals(other.deliveryAddress))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (orderDate == null) {
			if (other.orderDate != null)
				return false;
		} else if (!orderDate.equals(other.orderDate))
			return false;
		return true;
	}

   

	@Override
	public String toString() {
		return "#" + id;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public void setId(Long id) {
		this.id = id;
	}
    

	
}
