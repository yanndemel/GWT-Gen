package com.hiperf.common.ui.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.hiperf.common.ui.client.IClassInfo;
import com.hiperf.common.ui.client.ICommitHandler;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.event.WrapperUpdatedEvent;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.service.PersistenceService;
import com.hiperf.common.ui.client.service.PersistenceServiceAsync;
import com.hiperf.common.ui.client.widget.AbstractLinkedCell;
import com.hiperf.common.ui.client.widget.ICustomForm;
import com.hiperf.common.ui.shared.util.TableConfig;

/**
 * In order to create the context of your GWT module, this class must by created
 * using the following way :<br>
 * <p>
 * <b>{@code WrapperContext ctx = GWT.create(WrapperContext.class);}</b><br>
 * You can change the images and css here
 * */
public abstract class WrapperContext {
	private static String DYN_KEY = "#@@#";
	protected static final Map<String, IClassInfo> classInfoByName = new HashMap<String, IClassInfo>();
	protected static final Map<String, Map<String, IFieldInfo>> fieldInfoByName = new HashMap<String, Map<String, IFieldInfo>>();
	protected static final Map<String, IWrapper> emptyWrappersMap = new HashMap<String, IWrapper>();
	protected static final Map<String, Map<String, String>> enumsByClassName = new HashMap<String, Map<String, String>>();
	protected static final Map<String, TableConfig> configByName = new HashMap<String, TableConfig>();
	protected static final Map<String, ICustomForm> formByKey = new HashMap<String, ICustomForm>();
	protected static final Map<String, ICommitHandler> commitHandlerByKey = new HashMap<String, ICommitHandler>();
	private static final EventBus eventBus = new SimpleEventBus();
	private static Map<String, String> allViews = new HashMap<String, String>();

	//Array of <ClassName, DefaultClassLabel>
	//private static String[][] defaultTableNames = null;

	public interface GlobalResources extends ClientBundle {

		@NotStrict
		@Source("css/GwtNaked.css")
		CssResource css();
		
		@Source("css/images/button_bg_md.png")
		DataResource button_bg_md();

		@Source("css/images/button_bg_mo.png")
		DataResource button_bg_mo();

		@Source("css/images/button_bg_n.png")
		DataResource button_bg_n();

		@Source("css/images/button-hover.png")
		DataResource buttonOver();

		@Source("css/images/button.png")
		DataResource button();

		@Source("css/images/hborder.png")
		DataResource hborder();

		@Source("css/images/vborder.png")
		DataResource vborder();

		@Source("css/images/corner.png")
		DataResource corner();

		@Source("css/images/vbar.png")
		DataResource vbar();

		@Source("css/images/blue_gradient.gif")
		DataResource blueGradient();

		@Source("css/images/gray_gradient.gif")
		DataResource grayGradient();

		@Source("css/ieUserSelectFix.htc")
		DataResource ieUserSelectFix();

		@Source("com/hiperf/common/ui/client/widget/loadingBig.gif")
		ImageResource loadingBig();
		
	}

	public static final GlobalResources globalResources = GWT.create(GlobalResources.class);

	static {
		globalResources.css().ensureInjected();
	}

	public static String userRole = null;


	/**
	 * @return a map containing, for each class name, a map which keys are the
	 *         attributes names and which values are the corresponding
	 *         IFieldInfo
	 * */
	public static Map<String, Map<String, IFieldInfo>> getFieldInfoByName() {
		return fieldInfoByName;
	}

	public static Map<String, IWrapper> getEmptyWrappersMap() {
		return emptyWrappersMap;
	}

	public static EventBus getEventBus() {
		return eventBus;
	}

	public static Map<String, IClassInfo> getClassInfoByName() {
		return classInfoByName;
	}

	public static Map<String, Map<String, String>> getEnumsByClassName() {
		return enumsByClassName;
	}

	public static void fireWrapperUpdatedEvent(IWrapper w, String attribute,
			Object oldVal, Object newVal) {
		if (!WrappedObjectsRepository.getInstance().isPauseFireEvents()
				&& ((newVal != null && oldVal == null)
						|| (newVal == null && oldVal != null) || newVal != null
						&& oldVal != null && !newVal.equals(oldVal)))
			eventBus.fireEventFromSource(new WrapperUpdatedEvent(w, attribute,
					oldVal, newVal), w.getContent());
	}

	public static void addView(String key, TableConfig tc) {
		configByName.put(key, tc);
		if(!allViews.containsKey(key))
			allViews.put(key, DYN_KEY);
	}


	public static void loadScreensConfiguration(final AsyncCallback<Void> initModuleCallback) {
		final PopupPanel waitPopupPanel = new PopupPanel(false, true);
		waitPopupPanel.setStylePrimaryName("naked-popupPanel");
		waitPopupPanel.setGlassEnabled(true);
		VerticalPanel vp = new VerticalPanel();
		vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		waitPopupPanel.add(vp);
		vp.add(new Image(globalResources.loadingBig()));
		vp.add(new Label(NakedConstants.constants.loadingScreens()));
		waitPopupPanel.center();
		waitPopupPanel.show();
		PersistenceServiceAsync srv = PersistenceService.Util.getInstance();
		AsyncCallback<Map<String, TableConfig>> callback = new AsyncCallback<Map<String, TableConfig>>() {

			public void onFailure(Throwable caught) {
				waitPopupPanel.hide();
				initModuleCallback.onFailure(caught);
			}

			public void onSuccess(Map<String, TableConfig> result) {
				if (result != null) {
					configByName.putAll(result);
					for(String key : result.keySet()) {
						if(!allViews.containsKey(key))
							allViews.put(key, DYN_KEY);
					}
				}
				waitPopupPanel.hide();
				initModuleCallback.onSuccess(null);
			}
		};
		srv.getScreenConfigurations(NakedConstants.constants.locale(), callback);
	}

	public static String getTableLabel(String className, String viewName) {
		String s = null;
		TableConfig tableConfig = getTableConfig(className, viewName);
		if (tableConfig != null)
			s = tableConfig.getTableLabel();
		if(s == null) {
			IClassInfo ci = classInfoByName.get(className);
			if (ci != null) {
				s = getDefaultTableTitle(ci);
			}
		}
		return s;
	}

	public static String getFormLabel(String className, String viewName) {
		String s = null;
		TableConfig tableConfig = getTableConfig(className, viewName);
		if (tableConfig != null)
			s = tableConfig.getFormLabel();
		if(s == null) {
			IClassInfo ci = classInfoByName.get(className);
			if (ci != null) {
				s = getDefaultFormTitle(ci);
			}
		}
		return s;
	}

	private static String getDefaultTableTitle(IClassInfo ci) {
		if(ci.getFormTitle() != null)
			return ci.getFormTitle() + "s";
		else if(ci.getTableLabelKey() != null)
			return WidgetFactory.getTablesLabels().get(ci.getTableLabelKey());
		return null;
	}

	private static String getDefaultFormTitle(IClassInfo ci) {
		if(ci.getFormTitle() != null)
			return ci.getFormTitle();
		else if(ci.getFormLabelKey() != null)
			return WidgetFactory.getFormsLabels().get(ci.getFormLabelKey());
		return null;
	}

	public static String getViewFormLabel(String className, String viewName) {
		String s = null;
		TableConfig tableConfig = getTableConfig(className, viewName);
		if (tableConfig != null)
			s = tableConfig.getViewLabel();
		if(s == null) {
			IClassInfo ci = classInfoByName.get(className);
			if (ci != null)
				s = getDefaultFormTitle(ci);
		}
		return s;
	}

	public static String getEditFormLabel(String className, String viewName) {
		String s = null;
		TableConfig tableConfig = getTableConfig(className, viewName);
		if (tableConfig != null)
			s = tableConfig.getEditLabel();
		if(s == null) {
			IClassInfo ci = classInfoByName.get(className);
			if (ci != null) {
				if(ci.getFormLabelKey() != null)
					s = WidgetFactory.getFormsLabels().get(ci.getFormLabelKey());
				else
					s = getDefaultFormTitle(ci);
			}
		}
		return s;
	}

	public static TableConfig getTableConfig(String className, String viewName) {
		TableConfig tableConfig = null;
		if(viewName != null)
			tableConfig = configByName.get(getViewKey(className, viewName));
		if(tableConfig == null)
			tableConfig = configByName.get(className);
		return tableConfig;
	}

	public static String getNewFormLabel(String className, String viewName) {
		String s = null;
		TableConfig tableConfig = getTableConfig(className, viewName);
		if (tableConfig != null)
			s = tableConfig.getCreateLabel();
		if(s == null)
			return NakedConstants.constants.newForm();
		return s;
	}

	public static String getSelectElementLabel(String className, String viewName) {
		String s = null;
		TableConfig tableConfig = getTableConfig(className, viewName);
		if (tableConfig != null)
			s = tableConfig.getSelectLabel();
		if(s == null)
			return NakedConstants.constants.select();
		return s;
	}

	public static String getViewElementLabel(String className, String viewName) {
		String s = null;
		TableConfig tableConfig = getTableConfig(className, viewName);
		if (tableConfig != null)
			s = tableConfig.getViewLabel();
		if(s == null)
			return getViewFormLabel(className, viewName);
		return s;
	}

	public static String getEditElementLabel(String className, String viewName) {
		String s = null;
		TableConfig tableConfig = getTableConfig(className, viewName);
		if (tableConfig != null)
			s = tableConfig.getEditLabel();
		if(s == null)
			return getEditFormLabel(className, viewName);
		return s;
	}

	public static String getHeaderDisplayed(String className, HeaderInfo hi) {
		if(hi.getLabel() == null)
			return fieldInfoByName.get(className).get(hi.getAttribute()).getLabel();
		return hi.getLabel();
	}



	public static void fireWrapperUpdatedEvent(INakedObject no,
			String attribute, Object oldVal, Object newVal) {
		IWrapper w = WrapperContext.getEmptyWrappersMap()
				.get(no.getClass().getName()).newWrapper();
		w.setContent(no);
		fireWrapperUpdatedEvent(w, attribute, oldVal, newVal);
	}


	public static void setDisplayable(String className, String viewName, String att,
			boolean display) {
		fieldInfoByName.get(className).get(att).setDisplayed(display);
		TableConfig tableConfig = getTableConfig(className, viewName);
		if (tableConfig != null) {
			List<HeaderInfo> headers = tableConfig.getHeaders();
			if (headers != null && !headers.isEmpty()) {
				for (HeaderInfo hi : headers) {
					if (hi.getAttribute().equals(att)) {
						hi.setDisplayed(display);
						break;
					}
				}
			}
		}

	}

	public static void addView(String key, String viewLabel) {
		//String l = getTableLabel(className, null);
		allViews.put(key, viewLabel);
	}

	public static void addView(String className, String viewName, String viewLabel) {
		//String l = getTableLabel(className, viewName);
		String key = getViewKey(className, viewName);
		allViews.put(key, viewLabel);
	}

	public static String getViewKey(String className, String viewName) {
		return viewName + "#" + className;
	}



	public static Map<String, String> getAllViews() {
		return allViews;
	}

	public static String getViewLabel(String key) {
		String v = allViews.get(key);
		if(v == null || v.equals(DYN_KEY)) {
			String s = null;
			int i = key.indexOf("#");
			String vK;
			if(i > 0)
				vK = key.substring(0, i);
			else
				vK = key;
			for(String k : allViews.keySet()) {
				if(k.startsWith(vK)) {
					s = allViews.get(k);
					if(s != null && !s.equals(DYN_KEY))
						break;
					else
						s = DYN_KEY;
				}
			}
			v = s;
			if(i > 0 && s != null && !s.equals(DYN_KEY)) {
				IClassInfo ci = classInfoByName.get(key.substring(i+1));
				if (ci != null) {
					v = s + " / " + getDefaultTableTitle(ci);
				}
			}
		}
		return v != null ? v : key;
	}


	public static String getTitle(String viewName,
			final AbstractLinkedCell linkedCell) {
		String lbl = null;
		if(linkedCell != null) {
			TableConfig tc = WrapperContext.getTableConfig(linkedCell.getParentWrapper().getWrappedClassName(), viewName);
			if(tc != null) {
				for(HeaderInfo hi : tc.getHeaders()) {
					if(linkedCell.getAttribute().equals(hi.getAttribute())) {
						lbl =  hi.getLabel();
						break;
					}
				}
			}
			if(lbl == null)
				lbl = WrapperContext.getFieldInfoByName().get(linkedCell.getParentWrapper().getWrappedClassName()).get(linkedCell.getAttribute()).getLabel();
		}
		return lbl;
	}

	public static ICustomForm getNewForm(String className) {
		return formByKey.get(className);
	}

	public static ICommitHandler getCommitMethod(String className) {
		return commitHandlerByKey.get(className);
	}
	
}
