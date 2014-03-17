package com.hiperf.gwtjpa.test.table.basic.client;

import java.util.Date;
import java.util.Set;

import org.gwtgen.api.shared.INakedObject;
import org.gwtgen.api.shared.UIAttribute;

public class A implements INakedObject {

    // ======================================
    // =             Attributes             =
    // ======================================
    private Long id;
    private String city;
    private Date lastDate;
    private Integer intField;
    private Double doubleField;
    private Float floatField;
    private B objectField;
    private Set<B> collectionField;

    // ======================================
    // =            Constructors            =
    // ======================================

    public A() {
    }

    // ======================================
    // =         Getters & setters          =
    // ======================================


    @UIAttribute(index=2)
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }




    @UIAttribute(index=1)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@UIAttribute(index=3)
	public Date getLastDate() {
		return lastDate;
	}

	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}

	@UIAttribute
	public Integer getIntField() {
		return intField;
	}

	public void setIntField(Integer intField) {
		this.intField = intField;
	}

	@UIAttribute
	public Double getDoubleField() {
		return doubleField;
	}

	public void setDoubleField(Double doubleField) {
		this.doubleField = doubleField;
	}

	@UIAttribute
	public Float getFloatField() {
		return floatField;
	}

	public void setFloatField(Float floatField) {
		this.floatField = floatField;
	}

	@UIAttribute
	public B getObjectField() {
		return objectField;
	}

	public void setObjectField(B objectField) {
		this.objectField = objectField;
	}

	@UIAttribute
	public Set<B> getCollectionField() {
		return collectionField;
	}

	public void setCollectionField(Set<B> collectionField) {
		this.collectionField = collectionField;
	}
    

	
	
}
