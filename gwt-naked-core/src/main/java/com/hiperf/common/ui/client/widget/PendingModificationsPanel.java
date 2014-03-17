package com.hiperf.common.ui.client.widget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gwtgen.api.shared.INakedObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.hiperf.common.ui.client.i18n.NakedConstants;
import com.hiperf.common.ui.client.widget.TablePanel.PendingModifsImage;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;
import com.hiperf.common.ui.shared.util.Id;

public class PendingModificationsPanel extends VerticalPanel {

	private static final String PAR_L = " (";
	private static final String PAR_R = ")";

	interface Images extends ClientBundle {
		ImageResource verySmallDelete();
		ImageResource redBullet();
	}

	protected static final Images images = GWT.create(Images.class);


	private class TabTitle extends Label {
		private int total;
		private String prefix;

		public TabTitle(String prefix) {
			super();
			this.prefix = prefix;
			setWordWrap(false);
			setTitle(NakedConstants.constants.showDetails());
		}
		private void redraw() {
			setText(prefix + PAR_L + total + PAR_R);
		}
		public void decTotal() {
			total--;
			redraw();
		}
		public void setTotal(int i) {
			total = i;
			redraw();
		}
		public void display() {
			setText(prefix);
		}

	}

	public PendingModificationsPanel(final PendingModifsImage pendingModifsImage) {
		super();
		setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		HTML html = new HTML(NakedConstants.constants.pendingModifs());
		html.setStyleName("popup-title");
		add(html);
		Grid g = new Grid(1,2);
		Button b = new Button(NakedConstants.constants.discardAllChanges(), new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {}

					@Override
					public void onSuccess(Boolean b) {
						if(b!=null && b.booleanValue()) {
							discardAllChanges(pendingModifsImage);
						}
					}
				};
				MessageBox.confirm(MessageBox.TYPE.CONFIRM_YN, NakedConstants.constants.confirm(), NakedConstants.constants.questionDiscardAllChanges(), callback);
			}
		});
		b.setStylePrimaryName("naked-button");
		b.addStyleName("popup-button");
		g.setWidget(0, 0, b);
		viewChangesButton = new Button(NakedConstants.constants.viewAllChanges());
		viewChangesButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				tabPanel.setVisible(true);
				viewChangesButton.setEnabled(false);
				redrawTabs();
			}
		});
		viewChangesButton.setStylePrimaryName("naked-button");
		viewChangesButton.addStyleName("popup-button");
		g.setWidget(0, 1, viewChangesButton);
		g.getRowFormatter().addStyleName(0, "small-margin");
		add(g);
		tabPanel = new DecoratedTabPanel();
		add(tabPanel);
		tabPanel.setVisible(false);
		insertedObjects = new ArrayList<INakedObject>();
		updatedObjects = new HashMap<String, Map<Id,Map<String,Serializable>>>();
		removedObjects = new HashMap<String, Set<Id>>();
		manyToManyAdded = new HashMap<String, Map<Id,Map<String,List<Id>>>>();
		manyToManyRemoved = new HashMap<String, Map<Id,Map<String,List<Id>>>>();

	}
	private DecoratedTabPanel tabPanel;
	private Button viewChangesButton;
	private boolean modified;
	private boolean clearAll;
	private List<INakedObject> insertedObjects;
	private boolean insertedInit;
	private Map<String, Map<Id, Map<String, Serializable>>> updatedObjects;
	private boolean updatedInit;
	private Map<String, Set<Id>> removedObjects;
	private boolean deleteInit;
	private Map<String, Map<Id, Map<String, List<Id>>>> manyToManyAdded;
	private boolean manyToManyAddedInit;
	private Map<String, Map<Id, Map<String, List<Id>>>> manyToManyRemoved;
	private boolean manyToManyRemovedInit;


	public void redraw() {
		viewChangesButton.setEnabled(true);
		tabPanel.clear();
		tabPanel.setVisible(false);
	}

	private void redrawTabs() {
		tabPanel.clear();
		tabPanel.setVisible(true);
		modified = false;
		clearAll = false;
		insertedInit = false;
		updatedInit = false;
		deleteInit = false;
		manyToManyAddedInit = false;
		manyToManyRemovedInit = false;
		redrawInsertTab();
		redrawUpdateTab();
		redrawDeleteTab();
		redrawManyToManyAdded();
		redrawManyToManyRemoved();
	}

	private void redrawManyToManyAdded() {
		final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		manyToManyAdded.clear();
		if(!wor.getManyToManyAddedByClassName().isEmpty()) {
			for(Entry<String, Map<Id, Map<String, List<Id>>>> e : wor.getManyToManyAddedByClassName().entrySet()) {
				manyToManyAdded.put(e.getKey(), new HashMap<Id, Map<String,List<Id>>>(e.getValue()));
			}
			final Tree t = new Tree();
			final TabTitle tabTitle = new TabTitle(NakedConstants.constants.manyToManyAdded());
			tabTitle.display();
			tabPanel.add(t, tabTitle);
			tabTitle.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(!manyToManyAddedInit) {
						doAddManyToMany(t, manyToManyAdded);
						manyToManyAddedInit = true;
					}
				}
			});
		}

	}

	private void redrawManyToManyRemoved() {
		final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		manyToManyRemoved.clear();
		if(!wor.getManyToManyRemovedByClassName().isEmpty()) {
			for(Entry<String, Map<Id, Map<String, List<Id>>>> e : wor.getManyToManyRemovedByClassName().entrySet()) {
				manyToManyRemoved.put(e.getKey(), new HashMap<Id, Map<String,List<Id>>>(e.getValue()));
			}
			final Tree t = new Tree();
			final TabTitle tabTitle = new TabTitle(NakedConstants.constants.manyToManyRemoved());
			tabTitle.display();
			tabPanel.add(t, tabTitle);
			tabTitle.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(!manyToManyRemovedInit) {
						doAddManyToMany(t, manyToManyRemoved);
						manyToManyRemovedInit = true;
					}
				}
			});
		}
	}

	public boolean isModified() {
		return modified;
	}


	public boolean isClearAll() {
		return clearAll || !hasToCommit();
	}

	public List<INakedObject> getInsertedObjects() {
		return insertedObjects;
	}

	public Map<String, Map<Id, Map<String, Serializable>>> getUpdatedObjects() {
		return updatedObjects;
	}



	public Map<String, Set<Id>> getRemovedObjects() {
		return removedObjects;
	}

	private void redrawUpdateTab() {
		final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		updatedObjects.clear();
		if(!wor.getUpdatedObjects().isEmpty()) {
			for(String s : wor.getUpdatedObjects().keySet()) {
				updatedObjects.put(s, new HashMap<Id, Map<String,Serializable>>(wor.getUpdatedObjects().get(s)));
			}
			final Tree t = new Tree();
			final TabTitle updatedTabTitle = new TabTitle(NakedConstants.constants.updatedObjects());
			updatedTabTitle.display();
			tabPanel.add(t, updatedTabTitle);
			updatedTabTitle.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(!updatedInit) {
						int i = 0;
						for(final String name : updatedObjects.keySet()) {
							Label label = new Label(WrapperContext.getTableLabel(name, null));

							final TreeItem ti = new TreeItem(label);
							label.setStyleName("bold");
							t.addItem(ti);
							final Map<Id, Map<String, Serializable>> map = updatedObjects.get(name);
							for(final Id id : map.keySet()) {
								Grid g = new Grid(1,2);
								final TreeItem child = new TreeItem(g);
								ti.addItem(child);
								HTML l = new HTML();
								g.setWidget(0, 0, l);
								Image delImg = new Image(images.verySmallDelete());
								delImg.setTitle(NakedConstants.constants.discardChanges());
								g.setWidget(0, 1, delImg);
								delImg.addClickHandler(new ClickHandler() {

									@Override
									public void onClick(ClickEvent event) {
										Map<Id, Map<String, Serializable>> m = updatedObjects.get(name);
										m.remove(id);
										updatedTabTitle.decTotal();
										ti.removeItem(child);
										modified = true;
									}
								});
								StringBuilder sb = new StringBuilder("<b>");
								int k = 1;
								for(String fn : id.getFieldNames()) {
									sb.append(fn.toUpperCase());
									if(k<id.getFieldNames().size())
										sb.append(",");
									else
										sb.append("</b> : ");
									k++;
								}
								k = 1;
								for(Object fn : id.getFieldValues()) {
									sb.append(fn.toString());
									if(k<id.getFieldValues().size())
										sb.append(",");
									k++;
								}
								l.setHTML(sb.toString());
								l.setStyleName("font-normal");
								displayModifs(map, id, child);
								i++;
							}

						}
						updatedTabTitle.setTotal(i);
						updatedInit = true;
						updatedTabTitle.setTitle(null);
					}

				}

				private void displayModifs(
						final Map<Id, Map<String, Serializable>> map, final Id id,
						final TreeItem child) {
					Map<String, Serializable> modifs = map.get(id);
					for(String att : modifs.keySet()) {
						Label l2 = new Label(att+" = "+modifs.get(att));
						l2.setStyleName("font-italic");
						TreeItem modif = new TreeItem(l2);
						child.addItem(modif);
					}
				}
			});
			if(wor.getInsertedObjects().isEmpty()) {
				tabPanel.selectTab(0);
			} else {
				tabPanel.selectTab(1);
			}
		}
	}

	private void redrawDeleteTab() {
		final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		removedObjects.clear();
		if(!wor.getRemovedObjectsIdsByClassName().isEmpty()) {
			for(String s : wor.getRemovedObjectsIdsByClassName().keySet()) {
				Set<Id> c = wor.getRemovedObjectsIdsByClassName().get(s);
				HashSet<Id> set = new HashSet<Id>(c.size());
				for(Id id : c)
					set.add(id);
				removedObjects.put(s, set);
			}
			final Tree t = new Tree();
			final TabTitle removedTabTitle = new TabTitle(NakedConstants.constants.removedObjects());
			removedTabTitle.display();
			tabPanel.add(t, removedTabTitle);
			removedTabTitle.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(!deleteInit) {
						int i = 0;
						for(final String name : removedObjects.keySet()) {
							Label label = new Label(WrapperContext.getTableLabel(name, null));
							final TreeItem ti = new TreeItem(label);
							label.setStyleName("bold");
							t.addItem(ti);
							final Set<Id> set = removedObjects.get(name);
							for(final Id id : set) {
								Grid g = new Grid(1,2);
								final TreeItem child = new TreeItem(g);
								ti.addItem(child);
								HTML l = new HTML();
								g.setWidget(0, 0, l);
								Image delImg = new Image(images.verySmallDelete());
								delImg.setTitle(NakedConstants.constants.discardChanges());
								g.setWidget(0, 1, delImg);
								delImg.addClickHandler(new ClickHandler() {

									@Override
									public void onClick(ClickEvent event) {
										Set<Id> m = removedObjects.get(name);
										m.remove(id);
										removedTabTitle.decTotal();
										ti.removeItem(child);
										modified = true;
									}
								});
								StringBuilder sb = new StringBuilder("<b>");
								int k = 1;
								for(String fn : id.getFieldNames()) {
									sb.append(fn.toUpperCase());
									if(k<id.getFieldNames().size())
										sb.append(",");
									else
										sb.append("</b> : ");
									k++;
								}
								k = 1;
								for(Object fn : id.getFieldValues()) {
									sb.append(fn.toString());
									if(k<id.getFieldValues().size())
										sb.append(",");
									k++;
								}
								l.setHTML(sb.toString());
								l.setStyleName("font-normal");
								i++;
							}

						}
						removedTabTitle.setTotal(i);
						deleteInit = true;
						removedTabTitle.setTitle(null);
					}

				}


			});
			if(wor.getInsertedObjects().isEmpty() && wor.getUpdatedObjects().isEmpty()) {
				tabPanel.selectTab(0);
			} else if(wor.getUpdatedObjects().isEmpty()) {
				tabPanel.selectTab(1);
			} else {
				tabPanel.selectTab(2);
			}
		}

	}

	private void redrawInsertTab() {
		final WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();
		insertedObjects.clear();
		if(!wor.getInsertedObjects().isEmpty()) {
			insertedObjects.addAll(wor.getInsertedObjects());
			final Tree t = new Tree();
			final TabTitle tabTitle = new TabTitle(NakedConstants.constants.newObjects());
			tabTitle.setTotal(insertedObjects.size());
			tabPanel.add(t, tabTitle);
			tabTitle.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(!insertedInit) {
						Map<String,TreeItem> map = new HashMap<String, TreeItem>();
						TreeItem tti;
						for(final INakedObject o : insertedObjects) {
							String name = o.getClass().getName();
							if(!map.containsKey(name)) {
								Label l = new Label(WrapperContext.getTableLabel(name, null));
								l.setStyleName("bold");
								tti = new TreeItem(l);
								t.addItem(tti);
								map.put(name, tti);
							}
							final TreeItem ti = map.get(name);
							final Grid g = new Grid(1,3);
							final TreeItem myTi = ti.addItem(g);
							g.setWidget(0, 0, new Image(images.redBullet()));
							g.setText(0, 1, o.toString());
							g.getColumnFormatter().setStyleName(1, "font-normal");
							Image delImg = new Image(images.verySmallDelete());
							delImg.setTitle(NakedConstants.constants.discardChanges());
							g.setWidget(0, 2, delImg);
							g.getCellFormatter().setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_BOTTOM);
							delImg.addClickHandler(new ClickHandler() {

								@Override
								public void onClick(ClickEvent event) {
									insertedObjects.remove(o);
									ti.removeItem(myTi);
									modified = true;
									tabTitle.decTotal();
								}
							});

						}
						insertedInit = true;
						tabTitle.setTitle(null);
					}

				}
			});
			tabPanel.selectTab(0);
		}
	}

	private boolean hasToCommit() {
		int i = insertedObjects.size();
		if(i>0)
			return true;
		for(Map m : updatedObjects.values()) {
			i += m.size();
		}
		if(i>0)
			return true;
		for(Set s : removedObjects.values()) {
			i += s.size();
		}
		if(i>0)
			return true;
		for(Map m : manyToManyAdded.values()) {
			i += m.size();
		}
		if(i>0)
			return true;
		for(Map m : manyToManyRemoved.values()) {
			i += m.size();
		}
		if(i>0)
			return true;
		return false;
	}

	private void discardAllChanges(PendingModifsImage pendingModifsImage) {
		modified = true;
		clearAll = true;
		tabPanel.clear();
		ViewHelper.hidePopupPanel(this);
		pendingModifsImage.hide();
	}

	private void doAddManyToMany(final Tree t,
			final Map<String, Map<Id, Map<String, List<Id>>>> m) {
		for(Entry<String, Map<Id, Map<String, List<Id>>>> e : m.entrySet()) {
			final String name = e.getKey();
			Label label = new Label(WrapperContext.getTableLabel(name, null));
			final TreeItem ti = new TreeItem(label);
			label.setStyleName("bold");
			t.addItem(ti);
			Map<Id, Map<String, List<Id>>> map = e.getValue();
			for(Entry<Id, Map<String, List<Id>>> ee : map.entrySet()) {
				final Id srcId = ee.getKey();
				Label idLabel = new Label(srcId.toString());
				final TreeItem tiId = new TreeItem(idLabel);
				ti.addItem(tiId);
				for(Entry<String, List<Id>> eee : ee.getValue().entrySet()) {
					final String attr = eee.getKey();
					Label modifLbl = new Label(attr);
					modifLbl.setStyleName("bold");
					final TreeItem item = new TreeItem(modifLbl);
					tiId.addItem(item);
					for(final Id id : eee.getValue()) {
						Grid g = new Grid(1, 2);
						g.setText(0,0,id.toString());
						Image delImg = new Image(images.verySmallDelete());
						delImg.setTitle(NakedConstants.constants.discardChanges());
						g.setWidget(0, 1, delImg);
						final TreeItem addedIt = item.addItem(g);
						delImg.addClickHandler(new ClickHandler() {

							@Override
							public void onClick(ClickEvent event) {
								Map<Id, Map<String, List<Id>>> map2 = m.get(name);
								Map<String, List<Id>> map3 = map2.get(srcId);
								List<Id> list = map3.get(attr);
								list.remove(id);
								item.removeItem(addedIt);
								modified = true;
							}
						});

					}
				}
			}
		}
	}

	public Map<String, Map<Id, Map<String, List<Id>>>> getManyToManyAdded() {
		return manyToManyAdded;
	}

	public Map<String, Map<Id, Map<String, List<Id>>>> getManyToManyRemoved() {
		return manyToManyRemoved;
	}



}
