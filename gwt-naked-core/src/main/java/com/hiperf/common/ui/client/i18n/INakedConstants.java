package com.hiperf.common.ui.client.i18n;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;

@DefaultLocale("en")
public interface INakedConstants extends Constants {

	 public static final String REGEX_EMAIL = "^[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";

	@DefaultStringValue("Loading data...")
	 String loadingData();

	 @DefaultStringValue("Empty")
	 String emptyCell();

	 @DefaultStringValue("View all changes")
	 String viewAllChanges();

	 @DefaultStringValue("View pending modifications")
	 String viewPendingModifs();

	 @DefaultStringValue("Select items to delete")
	 String selectItemsToDelete();

	 @DefaultStringValue("Add new element")
	 String addNewValue();

	 @DefaultStringValue("Double click to edit row")
	 String doubleClickToEdit();

	 @DefaultStringValue("An error occured while getting data from DB")
	 String exceptionDataDB();

	 @DefaultStringValue("Exception while persisting data")
	 String exceptionPersistDataDB();

	 @DefaultStringValue("An error occured while saving filter")
	 String exceptionSaveFilter();

	 @DefaultStringValue("An error occured while saving screen configuration")
	 String exceptionSaveScreenConfig();

	 @DefaultStringValue("No Data found : Please create a new value")
	 String noDataFound();

	 @DefaultStringValue("Row inserted (go to last page to see it)")
	 String rowInserted();

	 @DefaultStringValue("Please correct the errors before validation.")
	 String errorValidate();

	 @DefaultStringValue("Validation problem")
	 String validationPb();

	 @DefaultStringValue("---- 0 item ----")
	 String noItem();

	 @DefaultStringValue("Cannot create new record : ")
	 String errorNewRecord();

	 @DefaultStringValue("Cannot create new record : the generated id must be String or Long")
	 String errorIdType();

	 @DefaultStringValue("Please edit lines one by one!")
	 String infoEditLines();

	 @DefaultStringValue("Some data are not valid : ")
	 String errorDataNotValid();

	 @DefaultStringValue("Close popup")
	 String closePopup();

	 @DefaultStringValue("Element")
	 String element();

	 @DefaultStringValue("Add element")
	 String addElement();

	 @DefaultStringValue("Remove element(s)")
	 String removeElement();

	 @DefaultStringValue("Edit element")
	 String editElement();

	 @DefaultStringValue("View element")
	 String viewElement();

	 @DefaultStringValue("Available columns :")
	 String availableColumns();

	 @DefaultStringValue("Sending data...")
	 String uploading();
	 
	 @DefaultStringValue("Upload file")
	 String upload();

	 @DefaultStringValue("Open")
	 String download();

	 @DefaultStringValue("Select a file to upload")
	 String selectUploadFile();

	 @DefaultStringValue("You have to select a file first...")
	 String errorSelectFile();

	 @DefaultStringValue("Do you really want to replace existing document ?")
	 String replaceDocument();

	 @DefaultStringValue("Error while retriving file name")
	 String errorFileName();

	 @DefaultStringValue("Refresh")
	 String refresh();

	 @DefaultStringValue("Click to open details")
	 String showDetails();

	 @DefaultStringValue("Discard changes")
	 String discardChanges();

	 @DefaultStringValue("Discard all changes")
	 String discardAllChanges();

	 @DefaultStringValue("Discard changes ?")
	 String questionDiscardChanges();

	 @DefaultStringValue("Discard all changes ?")
	 String questionDiscardAllChanges();

	 @DefaultStringValue("Create new Filter")
	 String newFilter();

	 @DefaultStringValue("Select filter")
	 String selectFilter();

	 @DefaultStringValue("Save filter")
	 String saveFilter();

	 @DefaultStringValue("Save filter as...")
	 String saveFilterAs();

	 @DefaultStringValue("Clear filter")
	 String clearFilter();

	 @DefaultStringValue("Clear sort")
	 String clearSort();

	 @DefaultStringValue("Apply filter and sort")
	 String applyFilter();

	 @DefaultStringValue("Sort by")
	 String sortBy();

	 @DefaultStringValue("Asc.")
	 String asc();

	 @DefaultStringValue("Desc.")
	 String desc();

	 @DefaultStringValue("Get all possible values")
	 String getAllValues();

	 @DefaultStringValue("Write search expression")
	 String writeSearch();

	 @DefaultStringValue("Select field")
	 String selectField();

	 @DefaultStringValue("Loading filters...")
	 String loadingFilters();

	 @DefaultStringValue("Loading screens configuration...")
	 String loadingScreens();

	 @DefaultStringValue("Error while loading filters")
	 String errorLoadingFilters();

	 @DefaultStringValue("Exception while getting filter")
	 String errorGettingFilter();

	 @DefaultStringValue("Please enter the filter name :")
	 String enterFilterName();

	 @DefaultStringValue("Select all")
	 String selectAll();

	 @DefaultStringValue("Select an element")
	 String select();

	 @DefaultStringValue("Save all changes")
	 String saveAll();

	 @DefaultStringValue("Insert new record")
	 String newRecord();

	 @DefaultStringValue("Refresh table")
	 String refreshTable();

	 @DefaultStringValue("Configure table")
	 String configureTable();

	 @DefaultStringValue("Excel Export")
	 String excelExport();

	 @DefaultStringValue("Excel Import / Export")
	 String excelImportExport();

	 @DefaultStringValue("Filter data")
	 String filterData();

	 @DefaultStringValue("You have changed some of your pending modified data.\n Please validate now your changes or keep your original modifications.")
	 String keepModif();

	 @DefaultStringValue("There are some modifications pending. What do you want to do ?")
	 String questionPendingModifs();

	 @DefaultStringValue("Sort Desc")
	 String sortDesc();

	 @DefaultStringValue("Sort Asc")
	 String sortAsc();

	 @DefaultStringValue("Choose an existing element")
	 String selectExistingValue();

	 @DefaultStringValue("Choose an existing element and add it to the list")
	 String selectAddExistingValue();

	 @DefaultStringValue("Delete current element")
	 String deleteCurrentValue();

	 @DefaultStringValue("Mandatory field")
	 String mandatoryField();

	 @DefaultStringValue("     Empty     ")
	 String emptyText();

	 @DefaultStringValue("Edit current element")
	 String editValue();

	 @DefaultStringValue("Attribute not found : ")
	 String errorAttributeNotFound();

	 @DefaultStringValue("Caution !")
	 String alert();

	 @DefaultStringValue("Information")
	 String info();

	 @DefaultStringValue("Confirmation")
	 String confirm();

	 @DefaultStringValue("OK")
	 String ok();

	 @DefaultStringValue("Cancel")
	 String cancel();

	 @DefaultStringValue("Close")
	 String close();
	 
	 @DefaultStringValue("Save")
	 String save();

	 @DefaultStringValue("Pending modifications")
	 String pendingModifs();

	 @DefaultStringValue("Updated elements")
	 String updatedObjects();

	 @DefaultStringValue("Removed elements")
	 String removedObjects();

	 @DefaultStringValue("New elements")
	 String newObjects();

	 @DefaultStringValue("Validate")
	 String validate();

	 @DefaultStringValue("Export")
	 String export();

	 @DefaultStringValue("Index")
	 String index();

	 @DefaultStringValue("Label")
	 String label();

	 @DefaultStringValue("Save configuration")
	 String saveConfig();

	 @DefaultStringValue("Displayed")
	 String displayed();

	 @DefaultStringValue("Number of rows per page : ")
	 String numberOfRows();

	 @DefaultStringValue("New element")
	 String newForm();

	 @DefaultStringValue("Headers information")
	 String headersInfo();

	 @DefaultStringValue("General information")
	 String generalInfo();

	 @DefaultStringValue("Table title")
	 String tableTitle();

	 @DefaultStringValue("Form title")
	 String formTitle();

	 @DefaultStringValue("New element title")
	 String newElementTitle();

	 @DefaultStringValue("Select element title")
	 String selectElementTitle();

	 @DefaultStringValue("View element title")
	 String viewElementTitle();

	 @DefaultStringValue("Edit element title")
	 String editElementTitle();

	 @DefaultStringValue("Some changes have been done. Do you want to save the screen configuration ?")
     String questionSaveConfigChanges();

	 @DefaultStringValue("The class name of the screen configuration must be chosen before...")
	 String errorScreenClassName();

	 @DefaultStringValue("All the columns are defined... Please choose one column and edit it \n(click on the magnifier and then double click on the row to edit)")
	 String errorScreenHeaderBounds();

	 @DefaultStringValue("Associated objects")
	 String manyToManyAdded();

	 @DefaultStringValue("Unassociated objects")
	 String manyToManyRemoved();

	 @DefaultStringValue(" for attributes :")
	 String attributes();

	 @DefaultStringValue("Help")
	 String helpPopup();

	 @DefaultStringValue("Cannot add a new header")
	 String errorCannotAddHeader();

	 @DefaultStringValue("Cannot delete a header")
	 String errorCannotDeleteHeader();

	 @DefaultStringValue("Edit label")
	 String editName();

	 @DefaultStringValue("No")
	 String no();

	 @DefaultStringValue("Yes")
	 String yes();

	 @DefaultStringValue("Filter saved")
	 String filterSaved();

	 @DefaultStringValue("No filter saved")
	 String noFilter();

	 @DefaultStringValue("Cannot change screen name")
	 String cannotChangeScreenName();

	 @DefaultStringValue("Screen configuration saved")
	 String screenConfigSaved();

	 @DefaultStringValue("English")
	 String english();

	 @DefaultStringValue("French")
	 String french();

	 @DefaultStringValue("en")
	 String locale();

	 @DefaultStringValue("What do you want to do :")
	 String whatUwant();

	 @DefaultStringValue("Export data to an Excel file")
	 String exportExcel();

	 @DefaultStringValue("Import new data from an Excel")
	 String importExcel();

	 @DefaultStringValue("Download Excel template file")
	 String downloadImportTemplate();

	 @DefaultStringValue("Upload Excel file (respecting the format specified in the template)")
	 String uploadExcelFile();

	 @DefaultStringValue("Upload successfull")
	 String uploadDone();

	 @DefaultStringValue("An error occurred during the upload")
	 String errorUpload();

	 @DefaultStringValue("Remove line")
	 String removeLine();

	 @DefaultStringValue("Remove filter")
	 String removeFilter();

	 @DefaultStringValue("Case sensitive filters")
	 String caseSensitive();

	 @DefaultStringValue("Save changes and continue")
	 String saveContinue();

	 @DefaultStringValue("Discard changes and continue")
	 String discardContinue();

	 @DefaultStringValue("Stay on the current screen")
	 String stayCurrentPage();

	 @DefaultStringValue("Validation pending")
	 String validationWaiting();

	 @DefaultStringValue("Edit value")
	 String edit();
	 
	 @DefaultStringValue("Screen configuration")
	 String screenConfiguration();

	 @DefaultStringValue("Click to sort the current page")
	 String sortByPage();

	 @DefaultStringValue("Click to sort all pages")
	 String sortGlobal();
	 
	 @DefaultStringValue("Your session has expired. Please log in again.")
	 String sessionTimeout();

	 @DefaultStringValue("Value must not be null")
	 String notNull();

	 @DefaultStringValue("Collection must not be empty")
	 String notNullColl();

	 @DefaultStringValue("Please wait while the Excel file generation")
	 String waitForFile();

	 @DefaultStringValue("Display in new tab")
	 String displayInTab();

}
