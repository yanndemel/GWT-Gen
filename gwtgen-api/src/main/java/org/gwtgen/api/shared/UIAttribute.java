package org.gwtgen.api.shared;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIAttribute {

	String labelKey() default "";

	boolean display() default true;

	boolean editable() default true;
	
	boolean importable() default true;
	
	boolean forceImport() default false;

	int index() default 0;

	String formatterClass() default "com.hiperf.common.ui.client.format.DefaultFormatter";

	String pattern() default "";

	/**
	 * By default the toString() method of the wrapped INakedObject is called to
	 * display an linked object in a cell of an IWrappedFlexTable (instance of
	 * INakedObject referencing another instance of INakedObject or Collection
	 * of INakedObjects containing only one element)<br>
	 * Defining the displayedAttribute will change the default behavior : the
	 * getAttributeValue will be called on the Wrapper instance instead of the
	 * toString method
	 *
	 * */
	String toStringAttribute() default "";

	/**
	 * Attribute name of the linked object that fires, if updated, a redraw of
	 * the cell. For example in a Collection of INakedObjects containing only
	 * one element, the change of the value of a field of this single element
	 * may have to trigger a redraw on the cell
	 * */
	String redrawOnUpdateLinkedObject() default "";

	/**
	 * Name of the custom class used to display the attribute (in forms &
	 * tables)
	 * */
	String customCellClass() default "";

	/**
	 * Name of the custom sort criteria used to sort the columns (server side)
	 * => usefull for transient attributes/getters that you want to be server
	 * side sortable
	 * */
	String sortCriteria() default "";

	/**
	 * Name of the key in the Resource bundle file containing the content of the
	 * help info popup displayed
	 * */
	String helpTextKey() default "";

	boolean canAddNew() default true;

	boolean canEdit() default true;

	boolean canSelect() default true;

	boolean canRemove() default true;

	/**
	 * Used only for @ManyToOne fields during import : if set, the field id
	 * taken into account in import using the specified attribute on the linked object
	 * */
	String importAttribute() default "";

	boolean importCreateMissing() default false;

	boolean displayInForm() default true;

	boolean preview() default false;

	boolean url() default false;

	boolean hidden() default false;

}
