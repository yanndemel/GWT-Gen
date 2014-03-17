package com.hiperf.common.rebind;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.gwtgen.api.shared.INakedObject;
import org.gwtgen.api.shared.NotEmpty;
import org.gwtgen.api.shared.UIAttribute;
import org.gwtgen.api.shared.UIClass;
import org.gwtgen.api.shared.UILinkedFile;
import org.gwtgen.api.shared.UIManyToMany;
import org.gwtgen.api.shared.UIManyToOne;
import org.gwtgen.api.shared.Unique;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JEnumConstant;
import com.google.gwt.core.ext.typeinfo.JEnumType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.hiperf.common.rebind.AttributeInfo.CollectionType;
import com.hiperf.common.rebind.AttributeInfo.Type;
import com.hiperf.common.rebind.AttributeInfo.ValidatorInfo;
import com.hiperf.common.ui.client.DataType;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.ILazy;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.IWrapperListModel;
import com.hiperf.common.ui.client.WrapperUpdatedHandler;
import com.hiperf.common.ui.client.event.CollectionDataAddedEvent;
import com.hiperf.common.ui.client.event.CollectionDataRemovedEvent;
import com.hiperf.common.ui.client.event.WrapperObjectAddedEvent;
import com.hiperf.common.ui.client.event.WrapperUpdatedEvent;
import com.hiperf.common.ui.client.exception.AttributeNotFoundException;
import com.hiperf.common.ui.client.exception.ParseException;
import com.hiperf.common.ui.client.format.DefaultFormatter;
import com.hiperf.common.ui.client.format.FormatHelper;
import com.hiperf.common.ui.client.format.FormatterFactory;
import com.hiperf.common.ui.client.model.ClassInfo;
import com.hiperf.common.ui.client.model.FieldInfo;
import com.hiperf.common.ui.client.model.LazyListModel;
import com.hiperf.common.ui.client.model.LinkedWrapperListModel;
import com.hiperf.common.ui.client.model.WrapperListModel;
import com.hiperf.common.ui.client.validation.AssertFalseValidator;
import com.hiperf.common.ui.client.validation.AssertTrueValidator;
import com.hiperf.common.ui.client.validation.MaxValidator;
import com.hiperf.common.ui.client.validation.MinValidator;
import com.hiperf.common.ui.client.validation.NotEmptyValidator;
import com.hiperf.common.ui.client.validation.NotNullValidator;
import com.hiperf.common.ui.client.validation.NullValidator;
import com.hiperf.common.ui.client.validation.SizeValidator;
import com.hiperf.common.ui.client.validation.UniqueValidator;
import com.hiperf.common.ui.client.validation.UrlValidator;
import com.hiperf.common.ui.client.validation.ValidatorFactory;
import com.hiperf.common.ui.shared.PersistenceManager;
import com.hiperf.common.ui.shared.RegExpValidator;
import com.hiperf.common.ui.shared.WrappedObjectsRepository;
import com.hiperf.common.ui.shared.WrapperContext;


public class WrapperCreator {

	private static final String DATA_TYPE = "DataType.";
	private static final String WRAPPER = "Wrapper";
	private static final List<String> TYPES = new ArrayList<String>();

	static {
		TYPES.add("int");
		TYPES.add("Integer");
		TYPES.add("byte");
		TYPES.add("Byte");
		TYPES.add("short");
		TYPES.add("Short");
		TYPES.add("long");
		TYPES.add("Long");
		TYPES.add("float");
		TYPES.add("Float");
		TYPES.add("double");
		TYPES.add("Double");
		TYPES.add("boolean");
		TYPES.add("Boolean");
		TYPES.add("char");
		TYPES.add("Character");
		TYPES.add("Date");
		TYPES.add("String");
		TYPES.add(INakedObject.class.getSimpleName());
	}

	protected TreeLogger logger;

	protected GeneratorContext context;

	protected TypeOracle typeOracle;

	protected String typeName;

	public WrapperCreator(TreeLogger logger, GeneratorContext context,
			String typeName) {
		this.logger = logger;
		this.context = context;
		this.typeOracle = context.getTypeOracle();
		this.typeName = typeName;
	}


	public String createWrapper() {
		try {
			JClassType classType = typeOracle.getType(typeName);
			SourceWriter source = getSourceWriter(classType);
			if (source == null) {
				return classType.getParameterizedQualifiedSourceName()
						+ WRAPPER;
			} else {
				WrapperCreatorData data = getInfos(classType, typeOracle);
				createSource(classType, source, data);
				source.commit(logger);
				return classType.getParameterizedQualifiedSourceName()
						+ WRAPPER;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	private void createSource(JClassType classType, SourceWriter source, WrapperCreatorData data) {
		String simpleName = classType.getSimpleSourceName();
		String qualifiedSourceName = classType.getQualifiedSourceName();
		source.indent();
		source.println("private static final String wrappedClassName = \""+qualifiedSourceName+"\";");
		source.println("private " + simpleName + " content;");
		source.println("private EventBus handlerManager = null;");
		source.println("private Set<String> lazyAttributes;");
		source.println("private Map<String, Long> lazyTimeByAtt;");
		source.println("private boolean lazy = false;");
		source.println();
		String nakedObjectClassName = INakedObject.class.getSimpleName();
		source.println("public void setContent("+nakedObjectClassName+" content) {");
		source.indent();
		source.println("this.content = (" + simpleName + ") content;");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public "+nakedObjectClassName+" getContent() {");
		source.indent();
		source.println("return content;");
		source.outdent();
		source.println("}");
		source.println();
		// create the getAttribute method
		source.println("public String getAttribute(String attr) throws AttributeNotFoundException {");
		getAttributeBegin(source, qualifiedSourceName);
		int i = 0;
		Map<String, AttributeInfo> getters = data.getGetters();
		for (String methodName : getters.keySet()) {
			AttributeInfo info = getters.get(methodName);
			if(!info.isCollection() && !info.isNakedObject()) {
				StringBuilder sb = new StringBuilder("if (attr.equals(\"");
				sb.append(info.getName());
				sb.append("\")) {");
				source.println(sb.toString());
				source.indent();
				source.println("Object o = this.content." + methodName + "();");
				source.println("if(o != null) {");
				source.indent();
				source.println("return info.getFormatter().format(o);");
				source.outdent();
				source.print("} else ");
				source.indent();
				source.print("return null;");
				source.outdent();
				source.print("}");
				source.outdent();
				if(i<(getters.size() - 1)) {
					source.print(" else ");
				}
			}
			i++;
		}
		getAttributeEnd(source);
		source.println();
		// create the getNakedAttribute method
		source.println("public Object getNakedAttribute(String attr) throws AttributeNotFoundException {");
		getAttributeBegin(source, qualifiedSourceName);
		i = 0;
		for (String methodName : getters.keySet()) {
			AttributeInfo info = getters.get(methodName);
			if(!info.isCollection()) {
				StringBuilder sb = new StringBuilder("if (attr.equals(\"");
				sb.append(info.getName());
				sb.append("\")) {");
				source.println(sb.toString());
				source.indent();
				source.println("return this.content." + methodName + "();");
				source.outdent();
				source.println("}");
				if(i<(getters.size() - 1)) {
					source.print(" else ");
				}
			}
			i++;
		}
		getAttributeEnd(source);
		source.println();
		source.println("public void setObjectAttribute(IFieldInfo info, String attr, Object value, boolean fireEvents)  throws AttributeNotFoundException {");
		source.indent();
		for (JMethod methode : data.getMethods()) {
			String name = methode.getName();
			String att = extractAttribute(name.substring(3));
			if (att != null && name.startsWith("set") && methode.getParameters().length == 1
					&& data.getAttributes().contains(att)
					) {
				boolean isCollection = false;
				boolean isNakedObject = false;
				boolean isEnum = false;
				boolean jpaTransient = false;
				String getter = null;
				for (String methodName : getters.keySet()) {
					AttributeInfo info = getters.get(methodName);
					if(info.getName().equals(att)) {
						getter = methodName;
						isCollection = info.isCollection();
						isNakedObject = info.isNakedObject();
						isEnum = info.isEnum();
						jpaTransient = info.isJpaTransient();
						break;
					}
				}
				if(!isCollection && !isNakedObject && !isEnum) {
					String cast = cast(methode.getParameters()[0].getType().getSimpleSourceName());
					source.println("if (attr.equals(\"" + att + "\")) { ");
					source.indent();
					source.println("Object oldVal = this.content."+getter+"();");
					source.println("this.content." + name + "("+cast+"value);");
					if(!jpaTransient) {
						source.println("if(fireEvents) {");
						source.indent();
						source.println("WrapperContext.fireWrapperUpdatedEvent(this, attr, oldVal, this.content."+getter+"());");
						source.outdent();
						source.println("}");
					}
					source.println("return;");
					source.outdent();
					source.println("}");
				}
			}
		}
		source.println("throw new AttributeNotFoundException(attr);");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public void setObjectAttribute(String attr, Object value)  throws AttributeNotFoundException {");
		source.indent();
		source.println("setObjectAttribute(attr, value, true);");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public void setObjectAttribute(String attr, Object value, boolean fireEvents)  throws AttributeNotFoundException {");
		getAttributeBegin(source, qualifiedSourceName);
		source.println("setObjectAttribute(info, attr, value, fireEvents);");
		source.outdent();
		source.println("}");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public void setAttribute(String attr, String value)  throws ParseException, AttributeNotFoundException {");
		source.indent();
		source.println("setAttribute(attr, value, true);");
		source.outdent();
		source.println("}");
		source.println();
		// create the set attribute method
		source.println("public void setAttribute(String attr, String value, boolean fireEvents)  throws ParseException, AttributeNotFoundException {");
		getAttributeBegin(source, qualifiedSourceName);
		source.println("if(info.getFormatter() == null)");
		source.indent();
		source.print("throw new ParseException(\"Unknown input type\");");
		source.outdent();
		source.println("setObjectAttribute(info, attr, info.getFormatter().parse(value), fireEvents);");
		source.outdent();
		source.println("}");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public void setEnumAttribute(String attr, String value) throws AttributeNotFoundException {");
		source.indent();
		source.println("setEnumAttribute(attr, value, true);");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public void setEnumAttribute(String attr, String value, boolean fireEvents) throws AttributeNotFoundException {");
		if(data.isHasEnums()) {
			getAttributeBegin(source, qualifiedSourceName);
			for (JMethod methode : data.getMethods()) {
				String name = methode.getName();
				String att = extractAttribute(name.substring(3));
				if (name.startsWith("set") && methode.getParameters().length == 1
						&& data.getAttributes().contains(att)
						) {
					boolean isEnum = false;
					boolean jpaTransient = false;
					AttributeInfo info = null;
					String getter = null;
					for (String methodName : getters.keySet()) {
						info = getters.get(methodName);
						if(info.getName().equals(att)) {
							getter = methodName;
							isEnum = info.isEnum();
							jpaTransient = info.isJpaTransient();
							break;
						}
					}
					if(isEnum) {
						source.println("if (attr.equals(\"" + att + "\")) { ");
						source.indent();
						source.println("if(value == null) {");
						source.indent();
						source.println("String oldVal = null;");
						//source.println("GWT.log(\""+getter+"\");");
						source.println("if(this.content."+getter+"() != null)");
						source.indent();
						source.println("oldVal = this.content."+getter+"().name();");
						source.outdent();
						source.println("this.content." + name + "(null);");
						if(!jpaTransient) {
							source.println("if(fireEvents) {");
							source.indent();
							source.println("WrapperContext.fireWrapperUpdatedEvent(this, attr, oldVal, null);");
							source.outdent();
							source.println("}");
						}
						source.println("return;");
						source.outdent();
						source.println("} else {");
						source.indent();
						source.println("for("+info.getRealJavaTypeName()+" enumName : "+info.getRealJavaTypeName()+".values()) {");
						source.indent();
						source.println("if (value.equals(enumName.name())) { ");
						source.indent();
						source.println("String oldVal = null;");
						source.println("if(this.content."+getter+"() != null)");
						source.indent();
						source.println("oldVal = this.content."+getter+"().name();");
						source.outdent();
						source.println("this.content." + name + "(enumName);");
						if(!jpaTransient) {
							source.println("if(fireEvents) {");
							source.indent();
							source.println("WrapperContext.fireWrapperUpdatedEvent(this, attr, oldVal, this.content."+getter+"());");
							source.outdent();
							source.println("}");
						}
						source.println("return;");
						source.outdent();
						source.println("}");
						source.outdent();
						source.println("}");
						source.outdent();
						source.println("}");
						source.println("throw new AttributeNotFoundException(attr);");
						source.outdent();
						source.println("}");
					}
				}
			}
			getAttributeEnd(source);
		} else {
			source.println("throw new AttributeNotFoundException(attr);");
			source.outdent();
			source.println("}");
		}
		source.println();
		source.println("public void setNakedObjectAttribute(String attr, INakedObject value)  throws AttributeNotFoundException {");
		source.indent();
		source.println("setNakedObjectAttribute(attr, value, true);");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public void setNakedObjectAttribute(String attr, INakedObject value, boolean fireEvents)  throws AttributeNotFoundException {");
		if(data.isHasLinkedObjects()) {
			getAttributeBegin(source, qualifiedSourceName);
			for (JMethod methode : data.getMethods()) {
				String name = methode.getName();
				String att = extractAttribute(name.substring(3));
				if (name.startsWith("set") && methode.getParameters().length == 1
						&& data.getAttributes().contains(att)
						) {
					boolean isCollection = false;
					boolean isNakedObject = false;
					boolean jpaTransient = false;
					boolean manyToOne = false;
					AttributeInfo info = null;
					for (String methodName : getters.keySet()) {
						info = getters.get(methodName);
						if(info.getName().equals(att)) {
							isCollection = info.isCollection();
							isNakedObject = info.isNakedObject();
							jpaTransient = info.isJpaTransient();
							manyToOne = info.isManyToOne();
							break;
						}
					}
					if(!isCollection && isNakedObject) {
						source.println("if (attr.equals(\"" + att + "\")) { ");
						source.indent();
						if(!jpaTransient) {
							source.println("if(fireEvents) {");
							source.indent();
							source.println("Object oldVal = this.content.g" + name.substring(1) + "();");
							source.println("this.content." + name + "(("+info.getRealJavaTypeName()+")value);");
							source.println("WrapperContext.fireWrapperUpdatedEvent(this, attr, oldVal, value);");
							source.outdent();
							source.println("} else ");
							source.indent();
							source.println("this.content." + name + "(("+info.getRealJavaTypeName()+")value);");
							source.outdent();
						} else if(manyToOne) {
							source.println("if(fireEvents) {");
							source.indent();
							String mappedBy = info.getMappedBy();
							String getter = new StringBuilder("get").append(mappedBy.substring(0, 1).toUpperCase()).append(mappedBy.substring(1)).toString();
							source.println("Object oldVal = this.content." + getter + "();");
							source.println("this.content." + name + "(("+info.getRealJavaTypeName()+")value);");
							source.println("WrapperContext.fireWrapperUpdatedEvent(this, \""+mappedBy+"\", oldVal, this.content." + getter + "());");
							source.outdent();
							source.println("} else ");
							source.indent();
							source.println("this.content." + name + "(("+info.getRealJavaTypeName()+")value);");
							source.outdent();
						} else {
							source.println("this.content." + name + "(("+info.getRealJavaTypeName()+")value);");
						}
						source.println("return;");
						source.outdent();
						source.println("}");
					}
				}
			}
			getAttributeEnd(source);
		} else {
			source.println("throw new AttributeNotFoundException(attr);");
			source.outdent();
			source.println("}");
		}


		source.println();
		source.println("public Object addToCollection(String attr, String value) throws AttributeNotFoundException, ParseException {");

		if(data.isHasCollections()) {
			getAttributeBegin(source, qualifiedSourceName);
			for(String methodName : getters.keySet()) {
				AttributeInfo ai = getters.get(methodName);
				if(ai.isCollection() && !ai.isNakedObject() && ai.isEditable()) {
					String cast = cast1(ai.getJavaType());
					source.println("if (attr.equals(\"" + ai.getName() + "\")) { ");
					source.indent();
					source.println(cast + " o = ("+cast+")info.getFormatter().parse(value);");
					source.println("Collection c = this.content."+methodName+"();");
					source.println("if(c == null) {");
					source.indent();
					String collectionCast;
					if(ai.getCollectionType().equals(CollectionType.SET)) {
						source.println("c = new HashSet<"+cast+">();");
						collectionCast = "(Set<" + cast + ">)";
					} else {
						source.println("c = new ArrayList<"+cast+">();");
						collectionCast = "(List<" + cast + ">)";
					}
					source.println("this.content.s" + methodName.substring(1) + "("+collectionCast+"c);");
					source.outdent();
					source.println("}");
					source.println("this.content."+methodName+"()"+".add(o);");
					if(!ai.isJpaTransient()) {
						source.println("if(!WrappedObjectsRepository.getInstance().isPauseFireEvents())");
						source.indent();
						source.println("WrapperContext.getEventBus().fireEventFromSource(new CollectionDataAddedEvent(this, attr, o), content);");
						source.outdent();
					}
					source.println("return o;");
					source.outdent();
					source.println("}");
				}
			}
			getAttributeEnd(source);
		} else {
			source.indent();
			source.println("throw new AttributeNotFoundException(attr);");
			source.outdent();
			source.println("}");
		}
		source.println();
		source.println("public void addObjectToCollection(String attr, INakedObject o) throws AttributeNotFoundException {");
		source.indent();
		source.println("addObjectToCollection(attr, o, false);");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public void addObjectToCollection(String attr, INakedObject o, boolean redraw) throws AttributeNotFoundException {");
		if(data.isHasObjectsCollections()) {
			getAttributeBegin(source, qualifiedSourceName);
			for(String methodName : getters.keySet()) {
				AttributeInfo ai = getters.get(methodName);
				if(ai.isCollection() && ai.isNakedObject() ) {
					source.println("if (attr.equals(\"" + ai.getName() + "\")) { ");
					source.indent();
					source.println("Collection c = this.content."+methodName+"();");
					source.println("if(c == null) {");
					source.indent();
					if(ai.getCollectionType().equals(CollectionType.SET)) {
						source.println("c = new HashSet<"+ai.getRealJavaTypeName()+">();");
					} else {
						source.println("c = new ArrayList<"+ai.getRealJavaTypeName()+">();");
					}
					source.print("this.content.s" + methodName.substring(1) + "(");
					if(ai.getCollectionType().equals(CollectionType.SET)) {
						source.println("(HashSet)c);");
					} else {
						source.println("(ArrayList)c);");
					}
					source.outdent();
					source.println("}");
					source.println("this.content."+methodName+"()"+".add(("+ai.getRealJavaTypeName()+")o);");
					source.println("WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();");
					source.println("if(!wor.isPauseFireEvents()) {");
					source.indent();
					source.println("WrapperObjectAddedEvent event = new WrapperObjectAddedEvent(this, attr, o, redraw);");
					source.println("wor.processObjectAddedEvent(event);");
					source.println("if(redraw)");
					source.indent();
					source.println("WrapperContext.getEventBus().fireEventFromSource(event, content);");
					source.outdent();
					source.outdent();
					source.println("}");
					//source.println("WrappedObjectsRepository.getInstance().addUpdatedObject(this.content);");
					source.println("return;");
					source.outdent();
					source.println("}");
				}
			}
			getAttributeEnd(source);
		} else {
			source.indent();
			source.println("throw new AttributeNotFoundException(attr);");
			source.outdent();
			source.println("}");
		}
		source.println();
		source.println("public void setCollection(String attr, Collection c) throws AttributeNotFoundException {");
		if(data.isHasObjectsCollections()) {
			getAttributeBegin(source, qualifiedSourceName);
			for(String methodName : getters.keySet()) {
				AttributeInfo ai = getters.get(methodName);
				if(ai.isCollection() && ai.isNakedObject() ) {
					source.println("if (attr.equals(\"" + ai.getName() + "\")) { ");
					source.indent();
					source.print("this.content.s" + methodName.substring(1) + "(");
					if(ai.getCollectionType().equals(CollectionType.SET)) {
						source.println("(Set)c);");
					} else {
						source.println("(List)c);");
					}
					source.outdent();
					source.println("return;");
					source.println("}");
					
					source.outdent();
				}
			}
			getAttributeEnd(source);
		} else {
			source.indent();
			source.println("throw new AttributeNotFoundException(attr);");
			source.outdent();
			source.println("}");
		}
		source.println();
		source.println("public Collection getCollection(String attr) throws AttributeNotFoundException {");
		if(data.isHasCollections() || data.isHasObjectsCollections()) {
			getAttributeBegin(source, qualifiedSourceName);
			for(String methodName : getters.keySet()) {
				AttributeInfo info = getters.get(methodName);
				if(info.isCollection() || info.isOneToMany() || info.isManyToMany()) {
					StringBuilder sb = new StringBuilder("if (attr.equals(\"");
					sb.append(info.getName());
					sb.append("\")) {");
					source.println(sb.toString());
					source.indent();
					//source.println("return FormatHelper.formatCollection(info, this.content." + methodName + "());");
					source.println("return this.content." + methodName + "();");
					source.outdent();
					source.println("}");
				}
			}
			getAttributeEnd(source);
		} else {
			source.indent();
			source.println("return null;");
			source.outdent();
			source.println("}");
		}
		source.println();
		source.println();
		source.println("public IWrapperListModel getWrappedCollection(String attr, boolean persistent) throws AttributeNotFoundException {");
		if(data.isHasObjectsCollections()) {
			getAttributeBegin(source, qualifiedSourceName);
			for(String methodName : getters.keySet()) {
				AttributeInfo info = getters.get(methodName);
				if(info.isCollection() && info.isNakedObject()) {
					StringBuilder sb = new StringBuilder("if (attr.equals(\"");
					sb.append(info.getName());
					sb.append("\")) {");
					source.println(sb.toString());
					source.indent();
					String realJavaTypeName = info.getRealJavaTypeName();
					source.println("Collection coll = this.content." + methodName + "();");
					source.println("if(coll instanceof ILazy || (getLazyTime(attr) != null && getLazyTime(attr) < WrappedObjectsRepository.getInstance().getLastCommitDate())) {");
					source.indent();
					source.println("ILazy lazyColl = (ILazy)coll;");
					String mappedBy = info.getMappedBy();
					String orderBy = info.getOrderBy();
					source.println("return new LazyListModel(\""+realJavaTypeName+"\", this, attr, "+ ((mappedBy != null) ?  "\""+mappedBy+"\", " : "null, ") + ((orderBy != null) ?  "\""+orderBy+"\", " : "null, ")+info.isOrderAsc()+", lazyColl.size(), coll.toString(), lazyColl.isInitialized());");
					source.outdent();
					source.println("} else {");
					source.indent();
					source.println("WrapperListModel wlm;");
					source.println("if(persistent) {");
					source.indent();
					if(info.isOneToMany() || info.isManyToMany())
						source.println("wlm = new LinkedWrapperListModel(\""+realJavaTypeName+"\", this, attr, \""+mappedBy+"\");");
					else
						source.println("wlm = new LinkedWrapperListModel(\""+realJavaTypeName+"\");");
					source.println("if(lazyAttributes != null && lazyAttributes.contains(attr))");
					source.indent();
					source.println("wlm.setLazy(true);");
					source.outdent();
					source.println("}");
					source.outdent();
					source.println("else");
					source.println("wlm = new WrapperListModel(\""+realJavaTypeName+"\");");
					source.outdent();
					source.println("if(coll != null) {");
					source.indent();
					source.println("for(Object o : coll) {");
					source.indent();
					source.println("INakedObject no = (INakedObject)o;");
					source.println("IWrapper w = GWT.create("+realJavaTypeName+".class);");
					source.println("w.setContent((INakedObject)o);");
					//source.println("w.setHandlerManager(handlerManager);");
					if(mappedBy != null && mappedBy.length() > 0 && !mappedBy.startsWith("this.") && (info.isOneToMany() || info.isManyToMany())) {
						if(info.isOneToMany())
							source.println("w.setNakedObjectAttribute(\""+mappedBy+"\", this.content, false);");
						else
							source.println("w.addObjectToCollection(\""+mappedBy+"\", this.content, false);");
					}
					source.println("wlm.simpleAdd(w);");
					source.outdent();
					source.println("}");
					source.println("if(persistent)");
					source.indent();
					source.println("((LinkedWrapperListModel)wlm).setServerCount(coll.size());");
					source.outdent();
					source.outdent();
					source.println("}");
					source.println("return wlm;");
					source.outdent();
					source.println("}");
					source.outdent();
					source.println("}");
				}
			}
			getAttributeEnd(source);
		} else {
			source.indent();
			source.println("return null;");
			source.outdent();
			source.println("}");
		}
		source.println();
		source.println("public String getWrappedCollectionClassName(String attr) throws AttributeNotFoundException {");
		if(data.isHasObjectsCollections()) {
			getAttributeBegin(source, qualifiedSourceName);
			for(String methodName : getters.keySet()) {
				AttributeInfo info = getters.get(methodName);
				if(info.isCollection() && info.isNakedObject()) {
					StringBuilder sb = new StringBuilder("if (attr.equals(\"");
					sb.append(info.getName());
					sb.append("\"))");
					source.println(sb.toString());
					source.indent();
					source.println("return \""+info.getRealJavaTypeName()+"\";");
					source.outdent();
				}
			}
			getAttributeEnd(source);
		} else {
			source.indent();
			source.println("return null;");
			source.outdent();
			source.println("}");
		}
		source.println();
		source.println("public void removeFromCollection(String attr, String value) throws AttributeNotFoundException, ParseException  {");
		if(data.isHasCollections()) {
			getAttributeBegin(source, qualifiedSourceName);
			for(String methodName : getters.keySet()) {
				AttributeInfo ai = getters.get(methodName);
				if(ai.isCollection() && !ai.isNakedObject()) {
					String cast = cast(ai.getJavaType());
					source.println("if (attr.equals(\"" + ai.getName() + "\")) { ");
					source.indent();
					source.println("Collection coll = this.content."+methodName+"();");
					source.println("if(coll != null) {");
					source.indent();
					source.println("Object o = info.getFormatter().parse(value);");
					source.println("this.content."+methodName+"()"+".remove("+cast+"o);");
					if(!ai.isJpaTransient()) {
						source.println("if(!WrappedObjectsRepository.getInstance().isPauseFireEvents())");
						source.indent();
						source.println("WrapperContext.getEventBus().fireEventFromSource(new CollectionDataRemovedEvent(this, attr, o), content);");
						source.outdent();
					}
					source.println("return;");
					source.outdent();
					source.println("} else ");
					source.indent();
					source.println("return;");
					source.outdent();
					source.println("}");
				}
			}
			getAttributeEnd(source);
		} else {
			source.indent();
			source.println("throw new AttributeNotFoundException(attr);");
			source.outdent();
			source.println("}");
		}
		source.println();
		source.println("public void removeObjectFromCollection(String attr, INakedObject o) throws AttributeNotFoundException {");
		if(data.isHasObjectsCollections()) {
			getAttributeBegin(source, qualifiedSourceName);
			for(String methodName : getters.keySet()) {
				AttributeInfo ai = getters.get(methodName);
				if(ai.isCollection() && ai.isNakedObject()) {
					source.println("if (attr.equals(\"" + ai.getName() + "\")) { ");
					source.indent();
					source.println("Collection coll = this.content."+methodName+"();");
					source.println("if(coll != null) {");
					source.indent();
					source.println("coll.remove(o);");
					source.println("WrappedObjectsRepository wor = WrappedObjectsRepository.getInstance();");
					source.println("if(!wor.isPauseFireEvents())");
					source.indent();
					source.println("wor.processObjectRemovedEvent(this, attr, o);");
					source.outdent();
					source.println("return;");
					source.outdent();
					source.println("} else ");
					source.indent();
					source.println("return;");
					source.outdent();
					source.println("}");
					source.outdent();
				}
			}
			getAttributeEnd(source);
		} else {
			source.indent();
			source.println("throw new AttributeNotFoundException(attr);");
			source.outdent();
			source.println("}");
		}
		source.println();
		source.println("public void removeAllObjectsFromCollection(String attr) throws AttributeNotFoundException {");
		if(data.isHasObjectsCollections()) {
			getAttributeBegin(source, qualifiedSourceName);
			for(String methodName : getters.keySet()) {
				AttributeInfo ai = getters.get(methodName);
				if(ai.isCollection() && ai.isNakedObject() && ai.isEditable()) {
					source.println("if (attr.equals(\"" + ai.getName() + "\")) { ");
					source.indent();
					source.println("Collection coll = this.content."+methodName+"();");
					source.println("if(coll != null) {");
					source.indent();
					source.println("if(!(coll instanceof ILazy))");
					source.indent();
					source.println("coll.clear();");
					source.outdent();
					source.println("else");
					source.indent();
					source.print("this.content.s" + methodName.substring(1) + "(");
					if(ai.getCollectionType().equals(CollectionType.SET)) {
						source.println("new HashSet<"+ai.getRealJavaTypeName()+">());");
					} else {
						source.println("new ArrayList<"+ai.getRealJavaTypeName()+">());");
					}
					source.outdent();
					source.println("}");
					source.println("return;");
					source.outdent();
					source.println("}");
					source.outdent();
				}
			}
			getAttributeEnd(source);
		} else {
			source.indent();
			source.println("throw new AttributeNotFoundException(attr);");
			source.outdent();
			source.println("}");
		}
		source.println();
		source.println("public IWrapper getWrappedAttribute(String attr) throws AttributeNotFoundException {");
		source.indent();
		if(data.isHasLinkedObjects()) {
			getAttributeBegin(source, qualifiedSourceName);
			i = 0;
			for (String methodName : getters.keySet()) {
				AttributeInfo info = getters.get(methodName);
				if(!info.isCollection() && info.isNakedObject()) {
					StringBuilder sb = new StringBuilder("if (attr.equals(\"");
					sb.append(info.getName());
					sb.append("\")) {");
					source.println(sb.toString());
					source.indent();
					source.println("INakedObject no = this.content." + methodName + "();");
					source.println("IWrapper w = GWT.create("+info.getRealJavaTypeName()+".class);");
					source.println("w.setContent(no);");
					source.println("if(getLazyTime(attr) != null && getLazyTime(attr) < WrappedObjectsRepository.getInstance().getLastCommitDate())");
					source.indent();
					source.println("w.setLazy(true);");
					source.outdent();
					if(info.getMappedBy() != null && info.getMappedBy().length() > 0 && !info.getMappedBy().startsWith("this.") && (info.isOneToMany() || info.isManyToMany())) {
						source.println("w.setNakedObjectAttribute(\""+info.getMappedBy()+"\", this.content, false);");
					}
					source.println("return w;");
					source.outdent();
					source.println("}");
					if(i<(getters.size() - 1)) {
						source.print(" else ");
					}
				}
				i++;
			}
			getAttributeEnd(source);
		} else {
			source.indent();
			source.println("throw new AttributeNotFoundException(attr);");
			source.outdent();
			source.println("}");
		}
		source.println();
		source.println("public INakedObject newNakedObject() {");
		source.indent();
		source.println("return new "+simpleName+"();");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public IWrapper newWrapper() {");
		source.indent();
		source.println("return (IWrapper)GWT.create("+simpleName+".class);");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public String getWrappedClassName() {");
		source.indent();
		source.println("return wrappedClassName;");
		source.outdent();
		source.println("}");
		source.println();
		source.println("private void addLazyAttribute(String attribute) {");
		source.indent();
		source.println("if(lazyAttributes == null)");
		source.indent();
		source.println("lazyAttributes = new HashSet<String>();");
		source.outdent();
		source.println("lazyAttributes.add(attribute);");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public void removeLazyAttribute(String attribute) {");
		source.indent();
		source.println("if(lazyAttributes != null)");
		source.indent();
		source.println("lazyAttributes.remove(attribute);");
		source.outdent();
		source.println("if(lazyTimeByAtt != null)");
		source.indent();
		source.println("lazyTimeByAtt.remove(attribute);");
		source.outdent();
		source.outdent();
		source.println("}");
		source.println();
		source.println("public void setLazyTime(String attribute, Long time) {");
		source.indent();
		source.println("if(lazyTimeByAtt == null)");
		source.indent();
		source.println("lazyTimeByAtt = new HashMap<String, Long>();");
		source.outdent();
		source.println("lazyTimeByAtt.put(attribute, time);");
		source.outdent();
		source.println("}");
		source.println();
		source.println("private Long getLazyTime(String attr) {");
		source.indent();
		source.println("if(lazyTimeByAtt != null)");
		source.indent();
		source.println("return lazyTimeByAtt.get(attr);");
		source.outdent();
		source.println("return null;");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public void setLazy(boolean lazy) {");
		source.indent();
		source.println("this.lazy = lazy;");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public boolean isLazy() {");
		source.indent();
		source.println("return this.lazy;");
		source.outdent();
		source.println("}");
		source.println();
		source.println("public String toString() {");
		source.indent();
		source.println("return content != null ? content.toString() : super.toString();");
		source.outdent();
		source.println("}");
		source.println();
	}


	protected static WrapperCreatorData getInfos(JClassType classType, TypeOracle typeOracle) throws NotFoundException,
			Exception {
		WrapperCreatorData data = new WrapperCreatorData(classType);

		UIClass classAnn = classType.getAnnotation(UIClass.class);
		if(classAnn != null) {
			ClassInfo ci = new ClassInfo(classAnn.editable(), classAnn.tableLabelKey(), classAnn.formLabelKey());
			ci.setUpdateHandlerClassName(classAnn.onUpdate());
			ci.setRowFormatterClassName(classAnn.rowFormatter());
			ci.setValidatorClassName(classAnn.validator());
			ci.setImportable(classAnn.importable());
			String cf = classAnn.createForm();
			if(!cf.trim().isEmpty())
				ci.setCreateForm(cf);
			cf = classAnn.onCommit();
			if(!cf.trim().isEmpty())
				ci.setCommitHandler(classAnn.onCommit());
			data.setInfo(ci);
		} else {
			data.setInfo(new ClassInfo(true, classType.getSimpleSourceName()));
		}

		String typeName = classType.getSimpleSourceName();
		data.setGetters(new HashMap<String, AttributeInfo>());
		data.setAttributes(new ArrayList<String>());
		data.setIndexes(new ArrayList<Integer>());
		if(classType.isAnnotationPresent(Entity.class)) {
			data.setEntity(true);
		}
		JClassType ct = classType;
		do {
			if(ct.isAnnotationPresent(Entity.class)) {
				data.setEntity(true);
				break;
			} else {
				JClassType[] implInt = ct.getImplementedInterfaces();

				if(implInt!=null && implInt.length > 0) {
					List<JClassType> intList = new ArrayList<JClassType>();
					boolean found = false;

						do {
							intList.clear();
							for(JClassType jct : implInt) {
								if(jct.isAnnotationPresent(Entity.class)) {
									data.setEntity(true);
									found = true;
									break;
								}
								if(jct.getImplementedInterfaces() != null && jct.getImplementedInterfaces().length > 0) {
									for(JClassType jj : jct.getImplementedInterfaces()) {
										intList.add(jj);
									}
								}
							}
							implInt = intList.toArray(new JClassType[0]);
						} while(implInt != null && implInt.length > 0);
						if(found)
							break;
				}

			}
			ct = ct.getSuperclass();
		} while(ct != null && ct.isClass() != null && !ct.getName().equals("Object") && !data.isEntity());

		ct = classType;
		do {
			data.addMethods(ct.getMethods());
			ct = ct.getSuperclass();
		} while(ct != null && ct.isClass() != null && !ct.getName().equals("Object"));

		for (JMethod m : data.getMethods()) {
			String getter = m.getName();
			UIAttribute uiAnn = m.getAnnotation(UIAttribute.class);
			JParameter[] methodParameters = m.getParameters();
			JType returnType = m.getReturnType();
			String myType = null;
			AttributeInfo info = null;
			JEnumType enumType = returnType.isEnum();
			JClassType returnClass = returnType.isClassOrInterface();
			if(enumType != null) {
				info = new AttributeInfo(Type.ENUM);
				data.setHasEnums(true);
				info.setRealJavaTypeName(returnType.getQualifiedSourceName());
				myType = "String";
				for(JEnumConstant c : enumType.getEnumConstants()) {
					info.addEnumConstant(c.getName());
				}
				checkJpaFields(classType, data, m, getter, info);
				checkValidationFields(classType, data, m, getter, info);
			} else if(methodParameters.length == 0 && (getter.startsWith("get") || getter.startsWith("is"))) {
				if(returnClass != null) {
					if(returnClass.isAssignableTo(typeOracle.getType(Collection.class.getName()))) {
						JParameterizedType paramType = returnType.isParameterized();
						if(paramType != null && paramType.getTypeArgs() != null && paramType.getTypeArgs().length == 1) {
							info = new AttributeInfo(Type.COLLECTION);
							if(returnClass.isAssignableTo(typeOracle.getType("java.util.Set")))
								info.setCollectionType(CollectionType.SET);
							else
								info.setCollectionType(CollectionType.LIST);


							JClassType paramClass = paramType.getTypeArgs()[0];
							if(paramClass.isAssignableTo(typeOracle.getType(INakedObject.class.getName()))) {
								myType = INakedObject.class.getSimpleName();
								info.setRealJavaTypeName(paramClass.getQualifiedSourceName());
								data.setHasObjectsCollections(true);
								checkJpaCollection(classType, data, m, getter,
										info);
							} else {
								myType = paramClass.getSimpleSourceName();
								data.setHasCollections(true);
								checkNotEmpty(classType, m, getter, info);
							}

						}
					} else if(returnClass.isAssignableTo(typeOracle.getType(INakedObject.class.getName()))) {
						info = new AttributeInfo(Type.DEFAULT);
						myType = INakedObject.class.getSimpleName();
						String setter = (getter.startsWith("get"))?"s"+getter.substring(1):"set"+getter.substring(2);
						for(JMethod setM : data.getMethods()) {
							if(setM.getName().equals(setter) && setM.getParameters().length == 1) {
								info.setEditable(true);
								break;
							}
						}
						info.setRealJavaTypeName(returnType.getQualifiedSourceName());
						data.setHasLinkedObjects(true);
						checkJpaFields(classType, data, m, getter, info);
						checkValidationFields(classType, data, m, getter, info);
					} else {
						info = new AttributeInfo(Type.DEFAULT);
						myType = returnType.getSimpleSourceName();
						checkJpaFields(classType, data, m, getter, info);
						checkValidationFields(classType, data, m, getter, info);
					}
				} else {
					info = new AttributeInfo(Type.DEFAULT);
					myType = returnType.getSimpleSourceName();
					checkJpaFields(classType, data, m, getter, info);
					checkValidationFields(classType, data, m, getter, info);
				}

			} /*else {
				info = new AttributeInfo(Type.DEFAULT);
				myType = returnType.getSimpleSourceName();
				checkJpaFields(classType, data, m, getter, info);
				checkValidationFields(classType, data, m, getter, info);
			}*/

			if (myType != null && TYPES.contains(myType)
					&& methodParameters.length == 0 && (getter.startsWith("is") || getter.startsWith("get"))) {
				info.setJavaType(myType);
				info.setDataType(getDataType(myType));
				data.getGetters().put(getter, info);
				String attribute;
				if(getter.startsWith("get")) {
					attribute = extractAttribute(getter.substring(3));
				} else {
					attribute = extractAttribute(getter.substring(2));
				}
				if(attribute != null) {
					data.getAttributes().add(attribute);
					info.setName(attribute);
					for (JMethod mm : data.getMethods()) {
						String method = mm.getName();
						if (method.startsWith("set")
								&& mm.getParameters().length == 1
								&& attribute.equals(extractAttribute(method.substring(3)))) {
							info.setEditable(true);
							break;
						}
					}
					boolean found = false;
					if(uiAnn != null) {
						found = true;
						if(uiAnn.hidden()) {
							info.setDisplayed(false);
							info.setDisplayedInForm(false);
							info.setEditable(false);
							info.setHidden(true);
							if(uiAnn.forceImport()) {
								info.setLabelKey(uiAnn.labelKey());
								info.setForceImport(true);
								info.setImportable(true);
							} else {
								info.setImportable(false);
							}
						} else {
							info.setImportable(uiAnn.importable());
							if(uiAnn.preview()) {
								info.setPreview(true);
							}
							if(uiAnn.url())
								info.setUrl(true);
							if(info.isEditable()) {
								info.setEditable(uiAnn.editable());
								info.setCanAddNew(uiAnn.canAddNew());
								info.setCanEdit(uiAnn.canEdit());
								info.setCanSelect(uiAnn.canSelect());
								info.setCanRemove(uiAnn.canRemove());
							}
							info.setDisplayed(uiAnn.display());
							info.setDisplayedInForm(uiAnn.displayInForm());
							info.setLabelKey(uiAnn.labelKey());
							info.setToStringAttribute(uiAnn.toStringAttribute());
							info.setSortCriteria(uiAnn.sortCriteria());
							info.setRedrawOnUpdateLinkedObject(uiAnn.redrawOnUpdateLinkedObject());
							info.setImportAttribute(uiAnn.importAttribute());
							if(uiAnn.customCellClass().length() > 0) {
								String key = typeName + "." + attribute;
								info.setCustomCellKey(key);
								info.setCustomCellClass(uiAnn.customCellClass());
							}
							int idx = uiAnn.index();
							if(data.getIndexes().contains(idx)) {
								throw new Exception("Bad index definition (field already indexed) for "+attribute+" in class "+typeName);
							}
							if(idx > 0)
								data.getIndexes().add(idx);
							info.setIndex(idx);
							if(!uiAnn.formatterClass().equals(DefaultFormatter.class.getName())) {
								String key = typeName + "." + attribute;
								info.setFormatterKey(key);
								info.setFormatterClass(uiAnn.formatterClass());
							}
							if(uiAnn.helpTextKey().length() > 0) {
								info.setHelpTextKey(uiAnn.helpTextKey());
							}							
						}
					}
					UILinkedFile fileAnn = m.getAnnotation(UILinkedFile.class);
					if(fileAnn != null) {
						found = true;
						if(!myType.equals("String"))
							throw new RuntimeException("The file name must be a String for class "+classType.getQualifiedSourceName()+" and property "+m.getName());
						info.setLinkedFile(true);
						info.setLinkedFileDownload(fileAnn.download());
						info.setLinkedFileUpload(fileAnn.upload());
						info.setLinkedFileClassName(fileAnn.fileClassName());
						info.setLinkedFileName(fileAnn.fileFieldName());
						info.setLinkedFileStorageFieldName(fileAnn.fileStorageFieldName());
						info.setLinkedFileLocalKeyField(fileAnn.localKeyField());
					}	
					if(!found) {
						info.setDisplayed(false);
						info.setDisplayedInForm(false);
						info.setEditable(false);
						info.setHidden(true);
					}
				}
				
			}
		}
		Collections.sort(data.getAttributes());

		for(AttributeInfo ai : data.getGetters().values()) {
			int idx = ai.getIndex();
			if(idx > 0) {
				String name = ai.getName();
				int k = data.getAttributes().indexOf(name);
				if((k + 1) != idx) {
					String old = data.getAttributes().set(idx - 1, name);
					data.getAttributes().set(k, old);
				}
			}
		}
		for(AttributeInfo ai : data.getGetters().values()) {
			int k = data.getAttributes().indexOf(ai.getName());
			ai.setIndex(k);
		}
		return data;
	}


	private static void checkJpaCollection(JClassType classType,
			WrapperCreatorData data, JMethod m, String getter,
			AttributeInfo info) {
		if(data.isEntity()) {
			if(m.isAnnotationPresent(OneToMany.class)) {
				info.setOneToMany(true);
				OneToMany ann = m.getAnnotation(OneToMany.class);
				info.setMappedBy(ann.mappedBy());
				info.setJoinClass(getJoinClass(ann, m.getReturnType()));
				processOrderBy(m, info);
			} else if(m.isAnnotationPresent(ManyToMany.class)) {
				info.setManyToMany(true);
				info.setMappedBy(m.getAnnotation(ManyToMany.class).mappedBy());
				processOrderBy(m, info);
			} else if(m.isAnnotationPresent(UIManyToMany.class)) {
				info.setManyToMany(true);
				UIManyToMany ann = m.getAnnotation(UIManyToMany.class);
				info.setReturnClass(ann.returnClass().getName());
				info.setJoinClass(ann.targetJoinClass().getName());
				info.setTargetIdField(ann.targetIdField());
				info.setJoinField(ann.joinField());
				info.setTargetJoinField(ann.targetJoinField());
				info.setNotNull(!ann.nullable());
				if(info.isNotNull())
					info.addValidator(NotNullValidator.class, null);
				processOrderBy(m, info);
			} else {
				String attribute;
				if(getter.startsWith("get")) {
					attribute = extractAttribute(getter.substring(3));
				} else {
					attribute = extractAttribute(getter.substring(2));
				}
				if(attribute != null) {
					for(JField f : classType.getFields()) {
						if(attribute.equals(f.getName())) {
							if(f.isAnnotationPresent(OneToMany.class)) {
								info.setOneToMany(true);
								OneToMany ann = f.getAnnotation(OneToMany.class);
								info.setMappedBy(ann.mappedBy());
								info.setJoinClass(getJoinClass(ann, f.getType()));
								processOrderBy(f, info);
							} else if(f.isAnnotationPresent(ManyToMany.class)) {
								info.setManyToMany(true);
								info.setMappedBy(f.getAnnotation(ManyToMany.class).mappedBy());
								processOrderBy(f, info);
							} else if(f.isAnnotationPresent(UIManyToMany.class)) {
								info.setManyToMany(true);
								UIManyToMany ann = f.getAnnotation(UIManyToMany.class);
								info.setReturnClass(ann.returnClass().getName());
								info.setJoinClass(ann.targetJoinClass().getName());
								info.setTargetIdField(ann.targetIdField());
								info.setJoinField(ann.joinField());
								info.setTargetJoinField(ann.targetJoinField());
								info.setNotNull(!ann.nullable());
								if(info.isNotNull())
									info.addValidator(NotNullValidator.class, null);
								processOrderBy(f, info);
							}
							break;
						}
					}
				}
				
			}
			if(m.isAnnotationPresent(ManyToMany.class)) {
				info.setManyToMany(true);
			} else if(m.isAnnotationPresent(UIManyToMany.class)) {
				info.setManyToMany(true);
				UIManyToMany ann = m.getAnnotation(UIManyToMany.class);
				info.setReturnClass(ann.returnClass().getName());
				info.setJoinClass(ann.targetJoinClass().getName());
				info.setTargetIdField(ann.targetIdField());
				info.setJoinField(ann.joinField());
				info.setTargetJoinField(ann.targetJoinField());
				info.setNotNull(!ann.nullable());
				if(info.isNotNull())
					info.addValidator(NotNullValidator.class, null);
			} else {
				String attribute;
				if(getter.startsWith("get")) {
					attribute = extractAttribute(getter.substring(3));
				} else {
					attribute = extractAttribute(getter.substring(2));
				}
				if(attribute != null) {
					for(JField f : classType.getFields()) {
						if(attribute.equals(f.getName())) {
							if(f.isAnnotationPresent(ManyToMany.class)) {
								info.setManyToMany(true);
							} else if(f.isAnnotationPresent(UIManyToMany.class)) {
								info.setManyToMany(true);
								UIManyToMany ann = f.getAnnotation(UIManyToMany.class);
								info.setReturnClass(ann.returnClass().getName());
								info.setJoinClass(ann.targetJoinClass().getName());
								info.setTargetIdField(ann.targetIdField());
								info.setJoinField(ann.joinField());
								info.setTargetJoinField(ann.targetJoinField());
								info.setNotNull(!ann.nullable());
								if(info.isNotNull())
									info.addValidator(NotNullValidator.class, null);
							}
							break;
						}
					}
				}
				
			}
			checkNotEmpty(classType, m, getter, info);
		}
	}


	private static String getJoinClass(OneToMany ann, JType collType) {
		String tp = collType.getParameterizedQualifiedSourceName();
		return ann.targetEntity().equals(void.class) ? tp.substring(tp.indexOf("<")+1, tp.lastIndexOf(">")) : ann.targetEntity().getName();
	}


	private static void checkNotEmpty(JClassType classType, JMethod m,
			String getter, AttributeInfo info) {
		if(m.isAnnotationPresent(NotEmpty.class)) {
			info.addValidator(NotEmptyValidator.class, null);
			info.setNotEmpty(true);
		} else {
			String attribute;
			if(getter.startsWith("get")) {
				attribute = extractAttribute(getter.substring(3));
			} else {
				attribute = extractAttribute(getter.substring(2));
			}
			for(JField f : classType.getFields()) {
				if(attribute.equals(f.getName()) && f.isAnnotationPresent(NotEmpty.class)) {
					info.addValidator(NotEmptyValidator.class, null);
					info.setNotEmpty(true);
					break;
				}
			}
		}
	}


	private static void processOrderBy(JMethod m, AttributeInfo info) {
		OrderBy orderAnn = m.getAnnotation(OrderBy.class);
		doProcessOrderBy(info, orderAnn);
	}


	public static void doProcessOrderBy(AttributeInfo info, OrderBy orderAnn) {
		if(orderAnn != null) {
			String value = orderAnn.value();
			if(value != null && value.toLowerCase().endsWith(" desc"))
				info.setOrderAsc(false);
			info.setOrderBy(value);
		}
	}
	
	private static void processOrderBy(JField f, AttributeInfo info) {
		OrderBy orderAnn = f.getAnnotation(OrderBy.class);
		doProcessOrderBy(info, orderAnn);
	}


	private static void checkJpaFields(JClassType classType,
			WrapperCreatorData data, JMethod m, String getter,
			AttributeInfo info) {
		if(data.isEntity()) {
			if(m.isAnnotationPresent(Transient.class)) {
				info.setJpaTransient(true);
				if(m.isAnnotationPresent(UIManyToOne.class)) {
					info.setManyToOne(true);
					UIManyToOne ann = m.getAnnotation(UIManyToOne.class);
					info.setMappedBy(ann.mappedBy());
					boolean nullable = ann.nullable();
					info.setNotNull(!nullable);
					if(!nullable)
						info.addValidator(NotNullValidator.class, null);
				}
				return;
			} else {
				String attribute;
				if(getter.startsWith("get")) {
					attribute = extractAttribute(getter.substring(3));
				} else {
					attribute = extractAttribute(getter.substring(2));
				}
				if(attribute != null) {
					if(m.isAnnotationPresent(Column.class)) {
						Column ann = m.getAnnotation(Column.class);
						processJpaColumn(classType, info, attribute, ann);
					}
					boolean found = false;
					for(JField f : classType.getFields()) {
						if(attribute.equals(f.getName())) {
							found = true;
							if(f.isAnnotationPresent(Transient.class)) {
								info.setJpaTransient(true);
								return;
							}
							if(f.isAnnotationPresent(Column.class) && !info.isNotNull()) {
								Column ann = f.getAnnotation(Column.class);
								processJpaColumn(classType, info, attribute, ann);
							}
							break;
						}
					}
					if(!found)
						info.setJpaTransient(true);
					JClassType ct = classType;
					found = false;
					do {
						JClassType[] implInt = ct.getImplementedInterfaces();

						if(implInt!=null && implInt.length > 0) {
							List<JClassType> intList = new ArrayList<JClassType>();

							do {
								intList.clear();
								for(JClassType jct : implInt) {
									for(JMethod jm : jct.getMethods()) {
										if(jm.getName().equals(getter) && jm.isAnnotationPresent(Id.class)) {
											info.setId(true);
											if(m.isAnnotationPresent(GeneratedValue.class) || m.isAnnotationPresent(SequenceGenerator.class) || jm.isAnnotationPresent(GeneratedValue.class) || jm.isAnnotationPresent(SequenceGenerator.class) ) {
												info.setGeneratedId(true);
											} else {
												info.setNotNull(true);
												info.addValidator(NotNullValidator.class, null);
											}
											found = true;
											break;
										}
									}
									if(found)
										break;
									if(jct.getImplementedInterfaces() != null && jct.getImplementedInterfaces().length > 0) {
										for(JClassType jj : jct.getImplementedInterfaces()) {
											intList.add(jj);
										}
									}
								}
								implInt = intList.toArray(new JClassType[0]);
							} while(implInt != null && implInt.length > 0 && !found);
						}
						ct = ct.getSuperclass();
					} while(ct != null && ct.isClass() != null && !ct.getName().equals("Object") && !found);


					if(!info.isId()) {
						if(m.isAnnotationPresent(Id.class)) {
							info.setId(true);
							if(m.isAnnotationPresent(GeneratedValue.class) || m.isAnnotationPresent(SequenceGenerator.class) ) {
								info.setGeneratedId(true);
							} else {
								info.setNotNull(true);
								info.addValidator(NotNullValidator.class, null);
							}
						} else {
							for(JField f : classType.getFields()) {
								if(attribute.equals(f.getName())) {
									if(f.isAnnotationPresent(Id.class)) {
										info.setId(true);
										if(f.isAnnotationPresent(GeneratedValue.class) || m.isAnnotationPresent(SequenceGenerator.class)) {
											info.setGeneratedId(true);
										} else {
											info.setNotNull(true);
											info.addValidator(NotNullValidator.class, null);
										}
									}
									break;
								}
							}
						}
					}	
				}
				

				if(m.isAnnotationPresent(ManyToOne.class)) {
					info.setManyToOne(true);
					if(m.isAnnotationPresent(JoinColumn.class)) {
						JoinColumn jc = m.getAnnotation(JoinColumn.class);
						info.setNotNull(!jc.nullable());
						if(info.isNotNull())
							info.addValidator(NotNullValidator.class, null);
					}
				} else {
					for(JField f : classType.getFields()) {
						if(attribute.equals(f.getName())) {
							if(f.isAnnotationPresent(ManyToOne.class)) {
								info.setManyToOne(true);
								if(f.isAnnotationPresent(JoinColumn.class)) {
									JoinColumn jc = f.getAnnotation(JoinColumn.class);
									info.setNotNull(!jc.nullable());
									if(info.isNotNull())
										info.addValidator(NotNullValidator.class, null);
								}
							}
							break;
						}
					}
				}
				if(m.isAnnotationPresent(UIManyToOne.class)) {
					info.setManyToOne(true);
					info.setJpaTransient(true);
					UIManyToOne ann = m.getAnnotation(UIManyToOne.class);
					info.setMappedBy(ann.mappedBy());
					boolean nullable = ann.nullable();
					info.setNotNull(!nullable);
					if(!nullable)
						info.addValidator(NotNullValidator.class, null);
				}
				if(m.isAnnotationPresent(OneToOne.class)) {
					info.setManyToOne(true);
				} else {
					for(JField f : classType.getFields()) {
						if(attribute.equals(f.getName())) {
							if(f.isAnnotationPresent(OneToOne.class)) {
								info.setOneToOne(true);
							}
							break;
						}
					}
				}
			}

		}
	}


	private static void processJpaColumn(JClassType classType,
			AttributeInfo info, String attribute, Column ann) {
		if(!info.isNotNull()) {
			info.setNotNull(!ann.nullable());
			if(info.isNotNull())
				info.addValidator(NotNullValidator.class, null);
		}
		if(!info.isUnique()) {
			info.setUnique(ann.unique());
			if(info.isUnique()) {
				info.addValidator(UniqueValidator.class, new Object[] {classType.getQualifiedSourceName(), attribute});
			}
		}		
		if(ann.length() > 0) {
			Set<ValidatorInfo> validators = info.getValidators();
			if(validators != null && !validators.isEmpty()) {
				boolean found = false;
				for(ValidatorInfo vi : validators) {
					if(vi.getClazz().equals(SizeValidator.class)) {
						Object[] params = vi.getParams();
						if(params[1] != null) {
							int p = (int) params[1];
							if(ann.length() < p)
								vi.setParams(new Object[] {params[0], ann.length()});
						} else {
							vi.setParams(new Object[] {params[0], ann.length()});
						}
						found = true;						
					}
				}
				if(!found) {
					info.addValidator(SizeValidator.class, new Object[] {0, ann.length()} );
				}
			} else {
				info.addValidator(SizeValidator.class, new Object[] {0, ann.length()} );
			}	
		}
		
			
	}

	private static void checkValidationFields(JClassType classType,
			WrapperCreatorData data, JMethod m, String getter,
			AttributeInfo info) {
		String className = classType.getName();
		String attName;
		if(getter.startsWith("get")) {
			attName = extractAttribute(getter.substring(3));
		} else {
			attName = extractAttribute(getter.substring(2));
		}
		if(attName != null) {
			info.setValidatorKey(className + "." + attName);
			if(m.isAnnotationPresent(NotNull.class)) {
				info.setNotNull(true);
				info.addValidator(NotNullValidator.class, null);
			}
			if(m.isAnnotationPresent(Null.class)) {
				info.addValidator(NullValidator.class, null);
			}
			if(m.isAnnotationPresent(AssertFalse.class)) {
				info.addValidator(AssertFalseValidator.class, null);
			}
			if(m.isAnnotationPresent(AssertTrue.class)) {
				info.addValidator(AssertTrueValidator.class, null);
			}
			if(m.isAnnotationPresent(Size.class)) {
				Size size = m.getAnnotation(Size.class);
				info.addValidator(SizeValidator.class, new Object[] {size.min(), size.max()});
			}
			if(m.isAnnotationPresent(Min.class)) {
				Min min = m.getAnnotation(Min.class);
				info.addValidator(MinValidator.class, new Object[] {BigDecimal.valueOf(min.value())});
			}
			if(m.isAnnotationPresent(Max.class)) {
				Max max = m.getAnnotation(Max.class);
				info.addValidator(MaxValidator.class, new Object[] {BigDecimal.valueOf(max.value())});
			}
			if(m.isAnnotationPresent(DecimalMin.class)) {
				DecimalMin min = m.getAnnotation(DecimalMin.class);
				info.addValidator(MinValidator.class, new Object[] {new BigDecimal(min.value())});
			}
			if(m.isAnnotationPresent(DecimalMax.class)) {
				DecimalMax max = m.getAnnotation(DecimalMax.class);
				info.addValidator(MaxValidator.class, new Object[] {new BigDecimal(max.value())});
			}
			if(m.isAnnotationPresent(Pattern.class)) {
				Pattern p = m.getAnnotation(Pattern.class);
				info.addValidator(RegExpValidator.class, new Object[] {p.regexp()});
			}
			if(m.isAnnotationPresent(Unique.class)) {
				info.addValidator(UniqueValidator.class, new Object[] {classType.getQualifiedSourceName(), attName});
			}
			if(info.isUrl()) {
				info.addValidator(UrlValidator.class, null);
			}

			for(JField f : classType.getFields()) {
				if(attName.equals(f.getName())) {
					if(f.isAnnotationPresent(NotNull.class)) {
						info.setNotNull(true);
						info.addValidator(NotNullValidator.class, null);
					}
					if(f.isAnnotationPresent(Null.class)) {
						info.addValidator(NullValidator.class, null);
					}
					if(f.isAnnotationPresent(AssertFalse.class)) {
						info.addValidator(AssertFalseValidator.class, null);
					}
					if(f.isAnnotationPresent(AssertTrue.class)) {
						info.addValidator(AssertTrueValidator.class, null);
					}
					if(f.isAnnotationPresent(Size.class)) {
						Size size = f.getAnnotation(Size.class);
						info.addValidator(SizeValidator.class, new Object[] {size.min(), size.max()});
					}
					if(f.isAnnotationPresent(Min.class)) {
						Min min = f.getAnnotation(Min.class);
						info.addValidator(MinValidator.class, new Object[] {BigDecimal.valueOf(min.value())});
					}
					if(f.isAnnotationPresent(Max.class)) {
						Max max = f.getAnnotation(Max.class);
						info.addValidator(MaxValidator.class, new Object[] {BigDecimal.valueOf(max.value())});
					}
					if(f.isAnnotationPresent(DecimalMin.class)) {
						DecimalMin min = f.getAnnotation(DecimalMin.class);
						info.addValidator(MinValidator.class, new Object[] {new BigDecimal(min.value())});
					}
					if(f.isAnnotationPresent(DecimalMax.class)) {
						DecimalMax max = f.getAnnotation(DecimalMax.class);
						info.addValidator(MaxValidator.class, new Object[] {new BigDecimal(max.value())});
					}
					if(f.isAnnotationPresent(Pattern.class)) {
						Pattern p = f.getAnnotation(Pattern.class);
						info.addValidator(RegExpValidator.class, new Object[] {p.regexp()});
					}
					if(f.isAnnotationPresent(Unique.class)) {
						info.addValidator(UniqueValidator.class, new Object[] {classType.getQualifiedSourceName(), attName});
					}
					break;
				}
			}			
		}

	}


	private void getAttributeEnd(SourceWriter source) {
		source.println("throw new AttributeNotFoundException(attr);");
		source.outdent();
		source.println("}");
		source.outdent();
		source.println("}");
		source.println();
	}


	private void getAttributeBegin(SourceWriter source, String className) {
		source.indent();
		source.println("IFieldInfo info = WrapperContext.getFieldInfoByName().get(\""+className+"\").get(attr);");
		source.println("if(info == null)");
		source.indent();
		source.println("throw new AttributeNotFoundException(attr);");
		source.outdent();
		source.println("else {");
		source.indent();
	}

	private String cast1(String typeName) {
		if (typeName.equals("String")) {
			return "String";
		} else if(typeName.equals("int") || typeName.equals("Integer")) {
			return "Integer";
		} else if(typeName.equals("byte") || typeName.equals("Byte")){
			return "Byte";
		} else if(typeName.equals("short") || typeName.equals("Short")) {
			return "Short";
		} else if(typeName.equals("long") || typeName.equals("Long")) {
			return "Long";
		} else if(typeName.equals("float") || typeName.equals("Float")) {
			return "Float";
		} else if(typeName.equals("double") || typeName.equals("Double")) {
			return "Double";
		} else if(typeName.equals("boolean") || typeName.equals("Boolean")) {
			return "Boolean";
		} else if(typeName.equals("char") || typeName.equals("Character")) {
			return "Character";
		} else if(typeName.equals("Date")) {
			return "Date";
		}
		return null;
	}

	private String cast(String typeName) {
		String cast = cast1(typeName);
		if(cast != null)
			return "(" + cast + ")";
		return null;
	}


	private static String getDataType(String typeName) {
		if (typeName.equals("String")) {
			return DATA_TYPE + DataType.STRING.name();
		} else if(typeName.equals("int") || typeName.equals("Integer")) {
			return DATA_TYPE + DataType.INT.name();
		} else if(typeName.equals("byte") || typeName.equals("Byte")){
			return DATA_TYPE + DataType.BYTE.name();
		} else if(typeName.equals("short") || typeName.equals("Short")) {
			return DATA_TYPE + DataType.SHORT.name();
		} else if(typeName.equals("long") || typeName.equals("Long")) {
			return DATA_TYPE + DataType.LONG.name();
		} else if(typeName.equals("float") || typeName.equals("Float")) {
			return DATA_TYPE + DataType.FLOAT.name();
		} else if(typeName.equals("double") || typeName.equals("Double")) {
			return DATA_TYPE + DataType.DOUBLE.name();
		} else if(typeName.equals("boolean") || typeName.equals("Boolean")) {
			return DATA_TYPE + DataType.BOOLEAN.name();
		} else if(typeName.equals("char") || typeName.equals("Character")) {
			return DATA_TYPE + DataType.CHAR.name();
		} else if(typeName.equals("Date")) {
			return DATA_TYPE + DataType.DATE.name();
		} else if(typeName.equals(INakedObject.class.getSimpleName())) {
			return DATA_TYPE + DataType.NAKED_OBJECT.name();
		}
		return null;
	}


	private static String extractAttribute(String s) {
		try {
			return new StringBuilder(s.substring(0, 1).toLowerCase()).append(s.substring(1)).toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
	}


	/**
	 * SourceWriter instantiation. Return null if the resource already exist.
	 *
	 * @return sourceWriter
	 */
	protected SourceWriter getSourceWriter(JClassType classType) {
		String packageName = classType.getPackage().getName();
		String simpleName = classType.getSimpleSourceName() + WRAPPER;
		ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(
				packageName, simpleName);
		composer.addImplementedInterface(IWrapper.class.getSimpleName());

		composer.addImport(Date.class.getName());
		composer.addImport(Iterator.class.getName());
		composer.addImport(Map.class.getName());
		composer.addImport(HashMap.class.getName());
		composer.addImport(Collection.class.getName());
		composer.addImport(List.class.getName());
		composer.addImport(Set.class.getName());
		composer.addImport(ArrayList.class.getName());
		composer.addImport(HashSet.class.getName());
		composer.addImport(GWT.class.getName());
		composer.addImport(EventBus.class.getName());
		composer.addImport(WrapperUpdatedEvent.class.getName());
		composer.addImport(WrapperObjectAddedEvent.class.getName());
		composer.addImport(CollectionDataAddedEvent.class.getName());
		composer.addImport(CollectionDataRemovedEvent.class.getName());
		composer.addImport(WrapperUpdatedEvent.class.getName());
		composer.addImport(WrapperUpdatedHandler.class.getName());
		composer.addImport(IWrapper.class.getName());
		composer.addImport(DataType.class.getName());
		composer.addImport(INakedObject.class.getName());
		composer.addImport(IFieldInfo.class.getName());
		composer.addImport(FieldInfo.class.getName());
		composer.addImport(FormatterFactory.class.getName());
		composer.addImport(FormatHelper.class.getName());
		composer.addImport(ValidatorFactory.class.getName());
		composer.addImport(ParseException.class.getName());
		composer.addImport(AttributeNotFoundException.class.getName());
		composer.addImport(ILazy.class.getName());
		composer.addImport(IWrapperListModel.class.getName());
		composer.addImport(LazyListModel.class.getName());
		composer.addImport(LinkedWrapperListModel.class.getName());
		composer.addImport(WrapperListModel.class.getName());
		composer.addImport(WrappedObjectsRepository.class.getName());
		composer.addImport(WrapperContext.class.getName());
		composer.addImport(classType.getQualifiedSourceName());
		composer.addImport(PersistenceManager.class.getName());


		PrintWriter printWriter = context.tryCreate(logger, packageName,
				simpleName);
		if (printWriter == null) {
			return null;
		} else {
			SourceWriter sw = composer.createSourceWriter(context, printWriter);
			return sw;
		}
	}


}
