package com.hiperf.gwtjpa.test.table.msgprovider.client;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;

@DefaultLocale("en")
public interface IConstants extends Constants {

	@DefaultStringValue("My English Label")
	String myLabel();
	
	@DefaultStringValue("My Table")
	String myTable();
	
	@DefaultStringValue("My Form")
	String myForm();
	
	
	/*Labels of internal frameworf domain classes (ScreenConfig and headers labels */
	@DefaultStringValue("Screens")
	String screens();

	@DefaultStringValue("Screen Configurations")
	String screenConfigs();
	
	@DefaultStringValue("Screen titles")
	String screenLabels();

	@DefaultStringValue("All screen titles")
	String allScreenLabels();

	@DefaultStringValue("Language")
	String language();


	@DefaultStringValue("Screen header")
	String screenHeader();

	@DefaultStringValue("Screen headers")
	String screenHeaders();

	@DefaultStringValue("Class name")
	String className();

	@DefaultStringValue("Screen name")
	String viewName();

	@DefaultStringValue("User name")
	String userName();

	@DefaultStringValue("Headers")
	String headers();

	@DefaultStringValue("Rows per page")
	String nbRows();

	@DefaultStringValue("Default config.")
	String defaultConfig();

	@DefaultStringValue("Form label")
	String formLabel();

	@DefaultStringValue("Table label")
	String tableLabel();

	@DefaultStringValue("Create label")
	String createLabel();

	@DefaultStringValue("Select label")
	String selectLabel();

	@DefaultStringValue("View label")
	String viewLabel();

	@DefaultStringValue("Edit label")
	String editLabel();

	@DefaultStringValue("Attribute name")
	String attribute();

	@DefaultStringValue("Label")
	String label();

	@DefaultStringValue("Labels")
	String labels();

	@DefaultStringValue("Index")
	String index();

	@DefaultStringValue("Screen Configuration")
	String screenConfig();

	@DefaultStringValue("Displayed")
	String displayed();

}
