package com.hiperf.common.ui.client.i18n;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale("en")
public interface INakedMessages extends Messages {

	 @DefaultMessage("View current values")
	 @AlternateMessage({"one", "View current value"})
	 String viewCurrentValue( @PluralCount @Optional int count);

	 @DefaultMessage("Edit current values")
	 @AlternateMessage({"one", "Edit current value"})
	 String editCurrentValue( @PluralCount @Optional int count);

	 @DefaultMessage("{0,number} items")
	 @AlternateMessage({"one", "{0,number} item"})
	 String nbItems(@PluralCount int count);

	 @DefaultMessage("Please enter a value between 0 and {0,number}")
	 String errorValueOutOfBounds(int value);

	 @DefaultMessage("Do you really want to replace existing filter {0} ?")
	 String replaceFilter(String name);

	 @DefaultMessage("Delete row(s)")
	 @AlternateMessage({"one", "Delete row"})
	 String deleteRow(@PluralCount @Optional int count);


	 @DefaultMessage("The field {0} cannot be null")
	 String notNullField(String label);

	 @DefaultMessage("Remove filter \"{0}\" ?")
	 String removeFilterQuestion(String name);

	 @DefaultMessage("Value must be less than {0}")
	 String less(Number max);
	 
	 @DefaultMessage("Value must be greater than {0}")
	 String more(Number max);

	 @DefaultMessage("The length of this field has to be between {0} and {1}")
	 String lenBetween(int min, int max);

	 @DefaultMessage("Value {0} is not well formatted")
	 String noMatch(Object value);

}
