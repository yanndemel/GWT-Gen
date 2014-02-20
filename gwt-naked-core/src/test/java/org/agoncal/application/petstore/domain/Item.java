package org.agoncal.application.petstore.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

import com.hiperf.common.ui.client.INakedObject;
import com.hiperf.common.ui.shared.annotation.NotEmpty;

/**
 * @author Antonio Goncalves
 *         http://www.antoniogoncalves.org
 *         --
 */

@Entity
public class Item implements INakedObject {

    // ======================================
    // =             Attributes             =
    // ======================================

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false, length = 30)
    @NotNull
    @Size(min = 1, max = 30)
    private String name;
    @Column(length = 3000)
    private String description;
    @Column(nullable = false)
    private Float unitCost;
    @NotEmpty
    private String imagePath;
    @ManyToOne
    @JoinColumn(name = "product_fk", nullable = false)
    @XmlTransient
    private Product product;

    // ======================================
    // =             Constants              =
    // ======================================

    public static final String FIND_BY_PRODUCT_ID = "Item.findByProductId";
    public static final String SEARCH = "Item.search";
    public static final String FIND_ALL = "Item.findAll";

    // ======================================
    // =            Constructors            =
    // ======================================

    public Item() {
    }

    public Item(String name, Float unitCost, String imagePath, Product product, String description) {
        this.name = name;
        this.unitCost = unitCost;
        this.imagePath = imagePath;
        this.product = product;
        this.description = description;
    }

    // ======================================
    // =         Getters & setters          =
    // ======================================

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(Float unitCost) {
        this.unitCost = unitCost;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ======================================
    // =   Methods hash, equals, toString   =
    // ======================================

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((imagePath == null) ? 0 : imagePath.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Item other = (Item) obj;
		if (imagePath == null) {
			if (other.imagePath != null)
				return false;
		} else if (!imagePath.equals(other.imagePath))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

    
    
    public void setId(Long id) {
		this.id = id;
	}

	

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Item");
        sb.append("{id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", unitCost=").append(unitCost).append('\'');
        sb.append(", imagePath='").append(imagePath).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}