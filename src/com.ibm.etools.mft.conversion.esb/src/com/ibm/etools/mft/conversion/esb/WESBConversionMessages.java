/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - initial API and implementation
 *******************************************************************************/

package com.ibm.etools.mft.conversion.esb;

import java.lang.reflect.Field;

import org.eclipse.osgi.util.NLS;

public final class WESBConversionMessages extends NLS {
	private static final String BUNDLE_NAME = "com.ibm.etools.mft.conversion.esb.messages";//$NON-NLS-1$

	public static String AbstractMediationPrimitiveConverter_PassThroughName;

	public static String AbstractMediationPrimitiveConverter_ToDoTask;

	public static String advancedPropertyDataHandler;

	public static String advancedPropertyFunctionSelector;

	public static String advancedPropertyJAXWSHandler;

	public static String advancedPropertyPolicySet;

	public static String allMarkers;

	public static String allToDos;

	public static String builtInConverter;

	public static String CalloutConverter_convertedTo;

	public static String CalloutResponseConverter_convertedTo;

	public static String ClassPageDialog_desc;

	public static String ClassPageDialog_title;

	public static String ClassSelectionDialog_create;

	public static String ClassSelectionDialog_LoadSample;

	public static String ClassSelectionDialog_title;
	public static String ConversionEditor_binding;

	public static String ConversionEditor_converter;

	public static String ConversionEditor_ErrorTitle;
	public static String ConversionEditor_globalOptionType;

	public static String ConversionEditor_mapOption;
	public static String ConversionEditor_MapsOption;
	public static String ConversionEditor_MapsOptionDescription;
	public static String ConversionEditor_MapsOptionMessage;

	public static String ConversionEditor_MapsTitle;

	public static String ConversionEditor_mergeResultOption;

	public static String ConversionEditor_option;

	public static String ConversionEditor_primitive;
	public static String ConversionEditor_projectsAlreadyExist;
	public static String ConversionEditor_projectType;
	public static String ConversionEditor_resource;
	public static String ConversionEditor_startConversion;
	public static String ConversionEditor_summaryOfConversionConfiguration;
	public static String ConversionEditor_type;
	public static String ConversionEditor_value;
	public static String ConversionEditor_warningTitle;
	public static String conversionNotes;
	public static String ConversionResultViewer_convertedTo;
	public static String ConversionResultViewer_source;

	public static String convertedTo;

	public static String converterClazz;
	public static String debugMessages;
	public static String defaultConversionResultRenderer_CompleteToDo;
	public static String defaultConversionResultRenderer_MessageColumn;
	public static String defaultConversionResultRenderer_ReopenToDo;
	public static String defaultConverter;
	public static String detailResultInformation;
	public static String errorCannotInstantiate;
	public static String errorCircularProjectReferenceDetected;
	public static String errorComponentIsNotWiredToExport;
	public static String errorComponentReferenceMultiplicity;
	public static String errorConverterNotLoadable;
	public static String errorConverterNotResolvable;

	public static String errorDuringConversion;

	public static String errorEmptyConvertedProjectName;
	public static String errorEntries;
	public static String errorInformation;
	public static String errorInvalidCharacterOnProjectName;
	public static String errorMoreThanOneComponent;
	public static String errorMoreThanOneExport;
	public static String errorMoreThanOneInterfaceInComponentInterfaces;
	public static String errorMoreThanOneInterfaceInExport;

	public static String errorMoreThanOneInterfaceInImport;

	public static String errorMultipleWSDLXSDWithSameNSAndLocalName;

	public static String errorNoExport;

	public static String errorNonMFCComponent;

	public static String errorOnSave;

	public static String errors;

	public static String errorTitle;

	public static String errorUnresolvedBindingForPort;

	public static String errorUnresolvedPortTypeForBinding;

	public static String errorUnresolvedService;

	public static String errorUnsupportedExportBinding;

	public static String errorUnsupportedImportBinding;

	public static String errorWESBConversionsUsed;

	public static String errorWESBProjectNameUsed;

	public static String errorWrongBindingConverterType;
	public static String errorWrongPrimitiveConverterType;
	public static String FlowResourceManager_conversionProgressLayoutMessageFlows;
	public static String genresEditor_ShowAll;
	public static String genresEditor_ShowTodo;
	public static String GlobalOptionsEditor_binding;

	public static String GlobalOptionsEditor_bindingConverterClassDesc;

	public static String GlobalOptionsEditor_bindingConverters;

	public static String GlobalOptionsEditor_bindingConverters_desc;
	public static String GlobalOptionsEditor_bindingConvertersDescription;
	public static String GlobalOptionsEditor_BindingUsage_Title;
	public static String GlobalOptionsEditor_conversionResult;

	public static String GlobalOptionsEditor_conversionResult_desc;

	public static String GlobalOptionsEditor_converterClass;
	public static String GlobalOptionsEditor_convertTo;
	public static String GlobalOptionsEditor_defaultConverter;

	public static String GlobalOptionsEditor_mergeResult;
	public static String GlobalOptionsEditor_MP;
	public static String GlobalOptionsEditor_MPUsage_Title;
	public static String GlobalOptionsEditor_primitiveConverterClassDesc;

	public static String GlobalOptionsEditor_primitiveConverters;

	public static String GlobalOptionsEditor_primitiveConverters_desc;
	public static String GlobalOptionsEditor_primitiveConvertersDescription;
	public static String GlobalOptionsEditor_primtive;

	public static String GlobalOptionsEditor_usage;
	public static String GlobalOptionsEditor_UsageHeading;
	public static String GlobalOptionsEditor_UsageMessage;

	public static String iibApplication;

	public static String iibLibrary;
	public static String iibService;
	public static String importWESBPI;
	// Error messages
	public static String INCOMPATIBLE_BROWSER;
	// Informational messages
	public static String infoConversionMessage_ESBLib;
	public static String infoConversionMessage_MediationModuleToApplication;
	public static String infoConversionMessage_MediationModuleToService;
	public static String infoExportConverted;
	public static String infoMfcFlowConverted;
	public static String infoPrimitiveConverted;
	public static String infoRequestFlowConverted;
	public static String infoResponseFlowConverted;
	public static String informationEntries;
	public static String infoUnsupportedPrimitiveConverted;
	public static String infoWsdlHandler_import;
	public static String infoWsdlHandler_inline_schema_externalized;
	public static String InputResponseConverter_convertedTo0;
	public static String launchConversionEditor;
	public static String mapTodoMapContext;

	public static String mapTodoMapContextRelativePath;

	public static String mapTodoMapHeader;
	public static String message;
	public static String messageConversionCompleted;
	public static String NewConversionSessionFileWizardPage_conversionSessionName;
	public static String NewConversionSessionFileWizardPage_desc;
	public static String NewConversionSessionFileWizardPage_errorEmptyProjectName;
	public static String NewConversionSessionFileWizardPage_errorInvalidFileName;
	public static String NewConversionSessionFileWizardPage_errorProjectNoAccessible;
	public static String NewConversionSessionFileWizardPage_errorSessionNameEmpty;
	public static String NewConversionSessionFileWizardPage_newButton;
	public static String NewConversionSessionFileWizardPage_project;

	public static String NewConversionSessionFileWizardPage_windowTitle;

	public static String NewConversionSessionFileWizardPage_windowTitleDesc;
	public static String NewConversionSessionWizard_Confirm;

	public static String NewConversionSessionWizard_FileExists;

	public static String NewConversionSessionWizard_windowTitle;

	public static String NewConverterProjectWizardPage_create;

	public static String NewConverterProjectWizardPage_desc;
	public static String NewConverterProjectWizardPage_javaProject;
	public static String NewConverterProjectWizardPage_title;

	public static String openClassType;
	public static String path;
	public static String previewJavas;

	public static String previewMessage_LIB;

	public static String previewMessage_MODULE;

	public static String previewMessage_unsupportedScaModule;

	public static String previewMessageExport;

	public static String previewMessageImport;
	public static String previewMessageMFCComponent;
	public static String previewMessageSCAModule;

	public static String previewSchemas;

	public static String progress_Previewing;

	public static String progress_PreviewingProject;

	public static String progressBeginConversion;

	public static String progressConfiguringProjectReference;

	public static String progressConverting;

	public static String progressConvertingComponent;

	public static String progressConvertingExport;

	public static String progressConvertingJava;

	public static String progressConvertingMap;

	public static String progressConvertingSCAModule;

	public static String progressConvertingSchema;

	public static String project;

	public static String projectExists;

	public static String projectNameInUse;

	public static String projectSelectionPage_DeselectAll;

	public static String projectSelectionPage_IncludeReferencedProject;

	public static String projectSelectionPage_SelectAll;

	public static String propertyName;

	public static String propertyValue;

	public static String resetToDefault;

	public static String resource;

	public static String ResourceOptionsEditor_websphereResourcesLabel;

	public static String select;

	public static String SourceProjectEditor_importLink;

	public static String SourceProjectEditor_sourceProject;

	public static String SourceProjectEditor_targetProject;

	public static String sourceResource;

	public static String SourceSelectionStep_MissingMedNodeMessage;

	public static String SourceSelectionStep_MissingMedNodeShortMessage;

	public static String sourceToTargetDescription;

	public static String stepConvert;

	public static String stepConvertDesc;

	public static String stepGlobalOption;

	public static String stepGlobalOptionDesc;

	public static String stepResourceOption;

	public static String stepResourceOptionDesc;

	public static String stepReview;

	public static String stepReviewDesc;

	public static String stepSource;

	public static String stepSourceDesc;

	public static String summaryInformation;

	public static String targetResource;

	public static String todoAdvancedPropertiesOnSOAPBinding;

	// Todo task messages
	public static String todoSchemaProblems;
	
	public static String todoJavaProblems;
	
	public static String todoManualMap;
	
	public static String todoAttachmentsNotSupported;

	public static String todoCantExactlyLocateWSDLMessagePartsBasedOnLocalName;

	public static String todoCantExactlyLocateXSDBasedOnLocalName;

	public static String todoConfigureHTTPTransport;

	public static String todoConfigureJMSTransport;

	public static String todoConfigureUnsupportedBinding;

	public static String todoConvertMessageElementActions;

	public static String todoDisableMessageFilter;

	public static String todoEntries;

	public static String todoFixJavaProjectProblems;

	public static String todoHTTPEndpointInSOAPRequestNode;

	public static String todoJMSEndpointInSOAPRequestNode;

	public static String todoMapIsMissing;

	public static String todoMultiPartWSDLMessage;

	public static String todoMultipleMappingDeclarations;

	public static String todoMultipleMappingRoots;

	public static String todoNonDCWWSDLDetected;

	public static String todoPotentialErrorsOnMap;
	public static String todoUnresolvedElement;
	public static String todoUnresolvedSMO;

	public static String todoUnresolvedVarForElement;

	public static String todoUnresolvedWSDL;

	public static String todoUnresolvedWSDLMessage;

	public static String todoUnsupportedMessageElementPath;

	public static String todoUnsupportedPathInFilterPattern;

	public static String todoUnsupportedPrimitive;

	public static String todoUnsupportedPrimitive_tableDesc;

	public static String todoUnsupportedPrimitive_tableDescCustomMediation;

	public static String todoUnsupportedScaModule;

	public static String todoUnsupportedXPathMappingObject;

	public static String todoXPath10DetectedInMap;

	public static String traceInformation;

	public static String type;

	public static String warningFlashingScreen;

	public static String warningResultOutOfSync;

	public static String WESBMapsOptionPage_map;

	public static String WESBMapsOptionPage_MapsUsageDialogDescription;

	public static String WESBMapsOptionPage_mapUsageDescription;

	public static String WESBMapsOptionPage_MapUsageDialog_Desc;

	public static String WESBMapsOptionPage_MapUsageDialog_Title;

	public static String WESBMapsOptionPage_Message_ReferencedMap;

	public static String WESBMapsOptionPage_Message_UnreferencedMap;

	public static String WESBMapsOptionPage_previewMessageMaps;

	public static String WESBMapsOptionPage_usage;

	public static String WESBMapsOptionPage_WarningTitle;

	public static String WESBMapsOptionPage_WarningUsedMapCannotBeDeSelected;
	public static String WESBResourceLabelProvider_component;
	public static String WESBResourceLabelProvider_export;
	public static String WESBResourceLabelProvider_import;

	public static String WESBResourceLabelProvider_javas;

	public static String WESBResourceLabelProvider_mapAllIncluded;

	public static String WESBResourceLabelProvider_mapNone;

	public static String WESBResourceLabelProvider_mapSome;

	public static String WESBResourceLabelProvider_module;

	public static String WESBResourceLabelProvider_schemas;

	public static String workspaceProblemMarkers;

	static {
		NLS.initializeMessages(BUNDLE_NAME, WESBConversionMessages.class);
	}

	public static String getMessage(String s) {
		Class<WESBConversionMessages> c = WESBConversionMessages.class;
		Field[] fields = c.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getName().equals(s)) {
				try {
					return (String) fields[i].get(null);
				} catch (Exception ex) {
				}
			}
		}
		return null;
	}

	public static String removeColon(String s) {
		return s.replace(':', ' ').replace('*', ' ');
	}

}