package com.ibm.etools.mft.conversion.esb.editor.parameter;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionImages;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.extension.resource.MapConverterHelper;
import com.ibm.etools.mft.conversion.esb.model.ComponentResource;
import com.ibm.etools.mft.conversion.esb.model.ExportResource;
import com.ibm.etools.mft.conversion.esb.model.ImportResource;
import com.ibm.etools.mft.conversion.esb.model.SCAModule;
import com.ibm.etools.mft.conversion.esb.model.WESBJavas;
import com.ibm.etools.mft.conversion.esb.model.WESBMap;
import com.ibm.etools.mft.conversion.esb.model.WESBMaps;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;
import com.ibm.etools.mft.conversion.esb.model.WESBSchemas;
import com.ibm.etools.mft.navigator.DecoratingTreeLabelProvider;

/**
 * @author Zhongming Chen
 * 
 */
public class WESBResourceLabelProvider extends DecoratingTreeLabelProvider implements IFontProvider {

	private FilteredTree filterTree;
	private PatternFilter filterForBoldElements;

	public WESBResourceLabelProvider(FilteredTree filterTree) {
		this.filterTree = filterTree;
		this.filterForBoldElements = filterTree.getPatternFilter();
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof WESBProject) {
			IProject p = ConversionUtils.getProject(((WESBProject) element).getName());
			if (ConversionUtils.isESBLib(p)) {
				return WESBConversionImages.getImage(WESBConversionImages.IMAGE_WESB_LIBRAY);
			} else if (ConversionUtils.isESBModule(p)) {
				return WESBConversionImages.getImage(WESBConversionImages.IMAGE_WESB_MODULE);
			}
		} else if (element instanceof ExportResource) {
			return WESBConversionImages.getImage(WESBConversionImages.IMAGE_WESB_EXPORT);
		} else if (element instanceof ImportResource) {
			return WESBConversionImages.getImage(WESBConversionImages.IMAGE_WESB_IMPORT);
		} else if (element instanceof SCAModule) {
			return WESBConversionImages.getImage(WESBConversionImages.IMAGE_SCA_MODULE);
		} else if (element instanceof ComponentResource) {
			return WESBConversionImages.getImage(WESBConversionImages.IMAGE_WESB_COMPONENT);
		} else if (element instanceof WESBMaps) {
			return WESBConversionImages.getImage(WESBConversionImages.IMAGE_WESB_MAPS);
		} else if (element instanceof WESBJavas) {
			return WESBConversionImages.getImage(WESBConversionImages.IMAGE_WESB_JAVA);
		} else if (element instanceof WESBSchemas) {
			return WESBConversionImages.getImage(WESBConversionImages.IMAGE_WESB_SCHEMAS);
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof WESBProject) {
			return ((WESBProject) element).getName();
		} else if (element instanceof SCAModule) {
			return WESBConversionMessages.WESBResourceLabelProvider_module;
		} else if (element instanceof WESBMaps) {
			List<WESBMap> mapsToConvert = MapConverterHelper.getMapsToConvert((WESBMaps) element);
			if (((WESBMaps) element).getAllMaps().size() == 0) {
				return WESBConversionMessages.WESBResourceLabelProvider_mapNone;
			} else if (mapsToConvert.size() == ((WESBMaps) element).getAllMaps().size()) {
				return WESBConversionMessages.WESBResourceLabelProvider_mapAllIncluded;
			} else if (mapsToConvert.size() == 0) {
				return WESBConversionMessages.WESBResourceLabelProvider_mapNone;
			} else {
				return WESBConversionMessages.WESBResourceLabelProvider_mapSome;
			}
		} else if (element instanceof ExportResource) {
			return NLS.bind(WESBConversionMessages.WESBResourceLabelProvider_export, ((ExportResource) element).getName());
		} else if (element instanceof WESBJavas) {
			return WESBConversionMessages.WESBResourceLabelProvider_javas;
		} else if (element instanceof WESBSchemas) {
			return WESBConversionMessages.WESBResourceLabelProvider_schemas;
		} else if (element instanceof ImportResource) {
			return NLS.bind(WESBConversionMessages.WESBResourceLabelProvider_import, ((ImportResource) element).getName());
		} else if (element instanceof ComponentResource) {
			return NLS.bind(WESBConversionMessages.WESBResourceLabelProvider_component, ((ComponentResource) element).getName());
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public Font getFont(Object element) {
		return FilteredTree.getBoldFont(element, filterTree, filterForBoldElements);
	}
}