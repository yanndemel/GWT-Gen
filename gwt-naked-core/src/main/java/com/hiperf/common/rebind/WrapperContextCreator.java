package com.hiperf.common.rebind;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtgen.api.shared.INakedObject;
import org.gwtgen.api.shared.MessagesProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.hiperf.common.ui.client.DataType;
import com.hiperf.common.ui.client.IFieldInfo;
import com.hiperf.common.ui.client.INakedLabels;
import com.hiperf.common.ui.client.IRowFormatter;
import com.hiperf.common.ui.client.IUpdateHandler;
import com.hiperf.common.ui.client.IValidator;
import com.hiperf.common.ui.client.IWrappedObjectForm;
import com.hiperf.common.ui.client.IWrapper;
import com.hiperf.common.ui.client.IWrapperValidator;
import com.hiperf.common.ui.client.format.FormatHelper;
import com.hiperf.common.ui.client.format.FormatterFactory;
import com.hiperf.common.ui.client.model.ClassInfo;
import com.hiperf.common.ui.client.model.FieldInfo;
import com.hiperf.common.ui.client.validation.BigDecimalParamsAbstractValidator;
import com.hiperf.common.ui.client.validation.IntParamsAbstractValidator;
import com.hiperf.common.ui.client.validation.ValidatorFactory;
import com.hiperf.common.ui.client.widget.ICustomForm;
import com.hiperf.common.ui.shared.StringParamsAbstractValidator;
import com.hiperf.common.ui.shared.WidgetFactory;
import com.hiperf.common.ui.shared.util.LinkedFileInfo;

public class WrapperContextCreator extends WrapperCreator {

	private static final String SUFFIX = "Impl";

	public WrapperContextCreator(TreeLogger logger, GeneratorContext context,
			String typeName) {
		super(logger, context, typeName);
		// TODO Auto-generated constructor stub
	}

	public String createWrapper() {
		try {
			JClassType classType = typeOracle.getType(typeName);
			SourceWriter source = getSourceWriter(classType);
			// Si le wrapper existe déjà, getSourceWriter renvoie null
			// Il n'est donc pas nécessaire de créer cette classe
			if (source == null) {
				return classType.getParameterizedQualifiedSourceName()
						+ SUFFIX;
			} else {

				JClassType[] msgProviders = typeOracle.getType(INakedLabels.class.getName()).getSubtypes();
				if(msgProviders != null) {
					if(msgProviders.length == 1) {
						MessagesProvider msgProvider = msgProviders[0].getAnnotation(MessagesProvider.class);
						if(msgProvider != null) {
							MessagesReference.setMessageProviderStaticReference(msgProvider.name());
						}
					} else if(msgProviders.length > 1) {
						throw new RuntimeException("Please define only one implmentation of INakedLabels");
					}
				}

				JClassType[] subtypes = typeOracle.getType(INakedObject.class.getName()).getSubtypes();
				List<WrapperCreatorData> dataList = new ArrayList<WrapperCreatorData>();
				for(JClassType ct : subtypes) {
					if(!ct.isAbstract())
						dataList.add(getInfos(ct, typeOracle));
				}

				createSource(source, dataList);
				source.commit(logger);
				return classType.getParameterizedQualifiedSourceName()
						+ SUFFIX;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void createSource(SourceWriter source, List<WrapperCreatorData> dataList) {
		StringBuilder sb;
		String msgProvider = MessagesReference.getMessageProviderStaticReference();
		source.println("static {");
		source.indent();
		source.println("init();");
		source.outdent();
		source.println("}");
		source.println();
		source.println("private static void init() {");
		source.println();
		source.println();
		source.println("Map<String, IFieldInfo> fieldInfoMap;");
		source.println("Map<String, String> enumMap;");
		source.println("List<IValidator> validatorList;");
		source.println("IFieldInfo fi;");
		source.println("ClassInfo ci;");
		for(WrapperCreatorData data : dataList) {
			JClassType classType = data.getClassType();
			String srcName = classType.getQualifiedSourceName();
			source.println("");
			source.println("emptyWrappersMap.put(\""+srcName+"\", (IWrapper)GWT.create("+srcName+".class));");
			ClassInfo classInfo = data.getInfo();
			if(classInfo != null) {
				String line = "ci = new ClassInfo("+classInfo.isEditable()+", "+classInfo.isImportable()+
						", "+((classInfo.getTableLabelKey() != null)?("\""+classInfo.getTableLabelKey() + "\""):"null")+
						", "+((classInfo.getFormLabelKey() != null)?("\""+classInfo.getFormLabelKey() + "\""):"null")+
						((classInfo.getUpdateHandlerClassName() != null) ? ", new "+classInfo.getUpdateHandlerClassName()+"()" : ", null")+
						((classInfo.getRowFormatterClassName() != null) ? ", new "+classInfo.getRowFormatterClassName()+"(), " : ", null, ")+data.isEntity()+");";
				source.println(line);
				if(classInfo.getFormTitle() != null) {
					source.println("ci.setFormTitle(\""+classInfo.getFormTitle()+"\");");
				}
				source.println("classInfoByName.put(\""+srcName+"\", ci);");
				if(classInfo.getCreateForm() != null)
					source.println("formByKey.put(\""+srcName+"\", new "+classInfo.getCreateForm()+"());");
				if(classInfo.getCommitHandler() != null)
					source.println("commitHandlerByKey.put(\""+srcName+"\", new "+classInfo.getCommitHandler()+"());");
				if(classInfo.getValidatorClassName() != null) {
					source.println("ValidatorFactory.getWrapperValidators().put(\""+srcName+"\", new "+classInfo.getValidatorClassName()+"());");
				}
				if(classInfo.getTableLabelKey() != null && !classInfo.getTableLabelKey().isEmpty()) {
					sb = new StringBuilder("WidgetFactory.getTablesLabels().put(\"");
					sb.append(classInfo.getTableLabelKey()).append("\", ");
					if(msgProvider == null) {
						sb.append("null);");
					} else {
						sb.append(msgProvider).append(".").append(classInfo.getTableLabelKey().substring(classInfo.getTableLabelKey().lastIndexOf(".") + 1)).append("());");
					}
					source.println(sb.toString());
				}
				if(classInfo.getFormLabelKey() != null && !classInfo.getFormLabelKey().isEmpty()) {
					sb = new StringBuilder("WidgetFactory.getFormsLabels().put(\"");
					sb.append(classInfo.getFormLabelKey()).append("\", ");
					if(msgProvider == null) {
						sb.append("null);");
					} else {
						sb.append(msgProvider).append(".").append(classInfo.getFormLabelKey().substring(classInfo.getFormLabelKey().lastIndexOf(".") + 1)).append("());");
					}
					source.println(sb.toString());
				}
			}
			source.println("fieldInfoMap = new HashMap<String, IFieldInfo>();");
			for (JMethod method : data.getMethods()) {
				String name = method.getName();
				AttributeInfo info = data.getGetters().get(name);
				if(info != null && method.getParameters().length == 0) {
					if(info.getFormatterKey() != null) {
						sb = new StringBuilder("FormatterFactory.getCustomFormatters().put(\"");
						sb.append(info.getFormatterKey()).append("\", new ").append(info.getFormatterClass()).append("());");
						source.println(sb.toString());
					}
					if(info.getCustomCellKey() != null) {
						sb = new StringBuilder("WidgetFactory.getCustomCells().put(\"");
						sb.append(info.getCustomCellKey()).append("\", new ").append(info.getCustomCellClass()).append("());");
						source.println(sb.toString());
					}
					if(info.getLabelKey() != null && info.getLabelKey().length() > 0) {
						sb = new StringBuilder("WidgetFactory.getAttributesLabels().put(\"");
						sb.append(info.getLabelKey()).append("\", ");
						if(msgProvider == null) {
							sb.append("null);");
						} else {
							sb.append(msgProvider).append(".").append(info.getLabelKey().substring(info.getLabelKey().lastIndexOf(".") + 1)).append("());");
						}
						source.println(sb.toString());
					}
					if(info.getHelpTextKey() != null) {
						sb = new StringBuilder("WidgetFactory.getHelpMessages().put(\"");
						sb.append(info.getHelpTextKey()).append("\", ");
						if(msgProvider == null) {
							sb.append("null);");
						} else {
							sb.append(msgProvider).append(".").append(info.getHelpTextKey().substring(info.getHelpTextKey().lastIndexOf(".") + 1)).append("());");
						}
						source.println(sb.toString());
					}
					if(info.getValidators() != null && !info.getValidators().isEmpty()) {
						sb = new StringBuilder("validatorList = new ArrayList<IValidator>();");
						source.println(sb.toString());
						sb = new StringBuilder("ValidatorFactory.getValidators().put(\"");
						sb.append(info.getValidatorKey()).append("\", validatorList);");
						source.println(sb.toString());

						for(AttributeInfo.ValidatorInfo vi : info.getValidators()) {
							sb = new StringBuilder("validatorList.add(new ").append(vi.getClazz().getName()).append("(");
							Object[] params = vi.getParams();
							if(params != null && params.length > 0) {
								int i=0;
								if(BigDecimalParamsAbstractValidator.class.isAssignableFrom(vi.getClazz())) {
									for(Object o : params) {
										sb.append("new BigDecimal(\"").append(o.toString()).append("\")");
										if(i < params.length - 1) {
											sb.append(", ");
										}
										i++;
									}
								}
								else if(IntParamsAbstractValidator.class.isAssignableFrom(vi.getClazz())) {
									for(Object o : params) {
										sb.append(o);
										if(i < params.length - 1) {
											sb.append(", ");
										}
										i++;
									}
								}
								else if(StringParamsAbstractValidator.class.isAssignableFrom(vi.getClazz())) {
									for(Object o : params) {
										sb.append("\"").append(((String)o).replaceAll("\\\\", "\\\\\\\\")).append("\"");
										if(i < params.length - 1) {
											sb.append(", ");
										}
										i++;
									}
								}
							}
							sb.append("));");
							source.println(sb.toString());
						}
					}
					sb = new StringBuilder("fi = new FieldInfo(");
					sb.append("\"");
					sb.append(info.getName());
					sb.append("\",");
					sb.append(info.isCollection());
					sb.append(",");
					sb.append("\"");
					sb.append(info.getLabelKey());
					sb.append("\",");
					String toStringAttribute = info.getToStringAttribute();
					if(toStringAttribute != null && toStringAttribute.length() > 0) {
						sb.append("\"");
						sb.append(toStringAttribute);
						sb.append("\",");
					} else {
						sb.append("null,");
					}
					String sortCriteria = info.getSortCriteria();
					if(sortCriteria != null && sortCriteria.length() > 0) {
						sb.append("\"");
						sb.append(sortCriteria);
						sb.append("\",");
					} else {
						sb.append("null,");
					}
					String toRedrawAttribute = info.getRedrawOnUpdateLinkedObject();
					if(toRedrawAttribute != null && toRedrawAttribute.length() > 0) {
						sb.append("\"");
						sb.append(toRedrawAttribute);
						sb.append("\",");
					} else {
						sb.append("null,");
					}
					sb.append(info.getDataType());
					if(info.getFormatterKey() != null) {
						sb.append(", \"").append(info.getFormatterKey()).append("\"");
					} else {
						sb.append(", null");
					}
					if(info.getCustomCellKey() != null) {
						sb.append(", \"").append(info.getCustomCellKey()).append("\"");
					} else {
						sb.append(", null");
					}
					if(info.getHelpTextKey() != null) {
						sb.append(", \"").append(info.getHelpTextKey()).append("\"");
					} else {
						sb.append(", null");
					}
					if(info.getValidators() != null && !info.getValidators().isEmpty()) {
						sb.append(", \"").append(info.getValidatorKey()).append("\"");
					} else {
						sb.append(", null");
					}
					sb.append(", ").append(info.isEditable()+", "+info.isId()+", "+info.isGeneratedId()
							+", "+info.isManyToOne()+", "+info.isOneToOne()+", "+info.isOneToMany()
							+", \""+info.getMappedBy()+"\", "+info.isManyToMany()+", "+info.isJpaTransient()
							+", "+info.isEnum()+", "+info.isDisplayed()+", "+info.isDisplayedInForm()
							+", "+info.getIndex()+", \""+info.getRealJavaTypeName()+"\", "+info.isNotNull()+", "+info.isNotEmpty());
					if(info.isLinkedFile()) {
						sb.append(", new LinkedFileInfo(")
						.append(info.isLinkedFileDownload()).append(", ")
						.append(info.isLinkedFileUpload()).append(", \"")
						.append(info.getLinkedFileClassName()).append("\", \"")
						.append(info.getLinkedFileStorageFieldName()).append("\", \"")
						.append(info.getLinkedFileName()).append("\", \"")
						.append(info.getLinkedFileLocalKeyField()).append("\")");
					} else {
						sb.append(", null");
					}
					sb.append(", ").append(info.canAddNew())
					.append(", ").append(info.canEdit())
					.append(", ").append(info.canSelect())
					.append(", ").append(info.canRemove());
					String ia = info.getImportAttribute();
					if(ia != null && ia.length() > 0) {
						sb.append(", \"");
						sb.append(ia);
						sb.append("\"");
					} else {
						sb.append(", null");
					}
					sb.append(");");
					source.println(sb.toString());
					if(info.getJoinClass() != null)
						source.println("fi.setJoinClass(\""+info.getJoinClass()+"\");");
					if(info.isManyToMany() && info.getReturnClass() != null) {
						source.println("fi.setReturnClass(\""+info.getReturnClass()+"\");");
						source.println("fi.setTargetIdField(\""+info.getTargetIdField()+"\");");
						source.println("fi.setJoinField(\""+info.getJoinField()+"\");");
						source.println("fi.setTargetJoinField(\""+info.getTargetJoinField()+"\");");
					}
					if(info.isPreview()) {
						source.println("fi.setPreview(true);");
					}
					if(info.isHidden()) {
						source.println("fi.setHidden(true);");
					}
					if(info.isForceImport()) {
						source.println("fi.setForceImport(true);");
					}
					if(info.isImportable()) {
						source.println("fi.setImportable(true);");
					}
					sb = new StringBuilder("fieldInfoMap.put(\"");
					sb.append(info.getName());
					sb.append("\", fi);");
					source.println(sb.toString());
					if(info.isEnum()) {
						source.println("enumMap = new HashMap<String,String>();");
						for(String s : info.getEnumConstants()) {
							source.println("enumMap.put(\""+s+"\", "+info.getRealJavaTypeName()+"."+s+".toString());");
						}
						source.println("enumsByClassName.put(\""+info.getRealJavaTypeName()+"\", enumMap);");
					}
				}
			}
			source.println("fieldInfoByName.put(\""+srcName+"\", fieldInfoMap);");
		}
		source.outdent();
		source.println("}");
		source.println();
	}

	public SourceWriter getSourceWriter(JClassType classType) {
		String packageName = classType.getPackage().getName();
		String simpleName = classType.getSimpleSourceName() + SUFFIX;
		ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(
				packageName, simpleName);

		composer.addImport(List.class.getName());
		composer.addImport(ArrayList.class.getName());
		composer.addImport(Map.class.getName());
		composer.addImport(HashMap.class.getName());

		composer.addImport(GWT.class.getName());

		composer.addImport(IWrapper.class.getName());
		composer.addImport(IFieldInfo.class.getName());
		composer.addImport(IValidator.class.getName());
		composer.addImport(IUpdateHandler.class.getName());
		composer.addImport(IRowFormatter.class.getName());
		composer.addImport(FieldInfo.class.getName());
		composer.addImport(LinkedFileInfo.class.getName());
		composer.addImport(ClassInfo.class.getName());
		composer.addImport(DataType.class.getName());
		composer.addImport(FormatterFactory.class.getName());
		composer.addImport(FormatHelper.class.getName());
		composer.addImport(IWrapperValidator.class.getName());
		composer.addImport(ValidatorFactory.class.getName());
		composer.addImport(WidgetFactory.class.getName());

		composer.addImport(classType.getQualifiedSourceName());
		
		composer.addImport(IWrappedObjectForm.class.getName());
		composer.addImport(ICustomForm.class.getName());
		composer.addImport(BigDecimal.class.getName());
		composer.setSuperclass(classType.getName());

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
