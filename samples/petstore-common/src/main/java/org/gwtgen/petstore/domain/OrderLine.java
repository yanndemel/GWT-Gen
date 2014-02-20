package org.gwtgen.petstore.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.shared.annotation.UIAttribute;
import com.hiperf.common.ui.shared.annotation.UIClass;

/**
 * @author Antonio Goncalves
 *         http://www.antoniogoncalves.org
 *         --
 */

@Entity
@UIClass(importable = true)
public class OrderLine implements INakedObject {

    // ======================================
    // =             Attributes             =
    // ======================================

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private Integer quantity;
    @OneToOne
    @JoinColumn(name = "item_fk", nullable = false)
    private Item item;

    @ManyToOne
    private Order order;
    // ======================================
    // =            Constructors            =
    // ======================================

    public OrderLine() {
    }

    public OrderLine(Integer quantity, Item item) {
        this.quantity = quantity;
        this.item = item;
    }

    // ======================================
    // =              Public Methods        =
    // ======================================

    public Float getSubTotal() {
        return item.getUnitCost() * quantity;
    }

    // ======================================
    // =         Getters & setters          =
    // ======================================

    @UIAttribute(hidden = true)
    public Long getId() {
        return id;
    }

    @UIAttribute
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @UIAttribute
    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    // ======================================
    // =   Methods hash, equals, toString   =
    // ======================================

    
    
    
    public void setId(Long id) {
		this.id = id;
	}

	
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		result = prime * result + ((order == null) ? 0 : order.hashCode());
		result = prime * result
				+ ((quantity == null) ? 0 : quantity.hashCode());
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
		OrderLine other = (OrderLine) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (!item.equals(other.item))
			return false;
		if (order == null) {
			if (other.order != null)
				return false;
		} else if (!order.equals(other.order))
			return false;
		if (quantity == null) {
			if (other.quantity != null)
				return false;
		} else if (!quantity.equals(other.quantity))
			return false;
		return true;
	}

	@Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if(quantity != null)
        	sb.append(quantity).append(" ");
        if(item != null)
        	sb.append(item.toString());
        return sb.toString();
    }

	@UIAttribute(display=false)
	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
    
    
}
