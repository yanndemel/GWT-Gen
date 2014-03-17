package org.agoncal.application.petstore.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.gwtgen.api.shared.INakedObject;

/**
 * @author Antonio Goncalves
 *         http://www.antoniogoncalves.org
 *         --
 */

@Entity
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

    public Long getId() {
        return id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    // ======================================
    // =   Methods hash, equals, toString   =
    // ======================================

    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderLine)) return false;

        OrderLine orderLine = (OrderLine) o;

        if (id != null ? !id.equals(orderLine.id) : orderLine.id != null) return false;
        if (item != null ? !item.equals(orderLine.item) : orderLine.item != null) return false;
        if (!quantity.equals(orderLine.quantity)) return false;

        return true;
    }

    public void setId(Long id) {
		this.id = id;
	}

	@Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + quantity.hashCode();
        result = 31 * result + (item != null ? item.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("OrderLine");
        sb.append("{id=").append(id);
        sb.append(", quantity=").append(quantity);
        sb.append(", item=").append(item);
        sb.append('}');
        return sb.toString();
    }

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
    
    
}
