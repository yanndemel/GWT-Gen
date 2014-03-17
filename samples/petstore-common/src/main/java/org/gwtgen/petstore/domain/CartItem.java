package org.gwtgen.petstore.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.gwtgen.api.shared.INakedObject;


/**
 * @author Antonio Goncalves
 *         http://www.antoniogoncalves.org
 *         --
 */

public class CartItem implements INakedObject{

    // ======================================
    // =             Attributes             =
    // ======================================

    @NotNull
    private Item item;
    @NotNull
    @Min(1)
    private Integer quantity;

    // ======================================
    // =            Constructors            =
    // ======================================

    public CartItem(Item item, Integer quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    
    
    // ======================================
    // =              Public Methods        =
    // ======================================

    public CartItem() {
		super();
		// TODO Auto-generated constructor stub
	}



	public Float getSubTotal() {
        return item.getUnitCost() * quantity;
    }

    // ======================================
    // =         Getters & setters          =
    // ======================================

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // ======================================
    // =   Methods hash, equals, toString   =
    // ======================================


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CartItem cartItem = (CartItem) o;

        if (!item.equals(cartItem.item)) return false;
        if (!quantity.equals(cartItem.quantity)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = item.hashCode();
        result = 31 * result + quantity.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if(this.quantity != null)
        	sb.append(quantity).append(" ");
        if(item != null)
        	sb.append(item.toString());
        return sb.toString();
    }
}