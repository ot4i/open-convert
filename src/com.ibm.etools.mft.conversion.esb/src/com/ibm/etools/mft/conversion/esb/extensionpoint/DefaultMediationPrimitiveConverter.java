/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extensionpoint;

import java.util.ArrayList;

import javax.xml.bind.JAXBElement;

import org.eclipse.core.resources.IFile;
import org.eclipse.osgi.util.NLS;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.model.mfc.AbstractProperty;
import com.ibm.etools.mft.conversion.esb.model.mfc.Property;
import com.ibm.etools.mft.conversion.esb.model.mfc.Row;
import com.ibm.etools.mft.conversion.esb.model.mfc.Table;

/**
 * @author Zhongming Chen
 * 
 */
public class DefaultMediationPrimitiveConverter extends AbstractMediationPrimitiveConverter {

	public static DefaultMediationPrimitiveConverter instance = new DefaultMediationPrimitiveConverter();

	/**
	 * 
	 */
	public DefaultMediationPrimitiveConverter() {
	}

	@Override
	public String getType() {
		return null;
	}

	@Override
	public Nodes convert(ConverterContext converterContext) throws Exception {
		String nodeName = getProposedIIBNodeNameFromSourcePrimitive(converterContext);

		Nodes nodes = super.convert(converterContext);
		IFile flowFile = (IFile) converterContext.flowManager.getFlowResource(converterContext.targetFlow);
		IFile subFlowFile = converterContext.flowManager.getFlowResource(ConversionUtils.getFullyQualifiedFlowName(
				converterContext.targetFlow.getName(), converterContext.targetFlow.getName() + "_" + nodeName)); //$NON-NLS-1$
		String messageTemplate = ConversionUtils.loadTemplate("internal/unsupportedPrimitive.html"); //$NON-NLS-1$
		messageTemplate = messageTemplate.substring(3);
		String rowTemplate = ConversionUtils.loadTemplate("internal/propertyRow.html"); //$NON-NLS-1$
		StringBuffer sb = new StringBuffer();
		for (JAXBElement o : converterContext.sourcePrimitive.getAbstractProperty()) {
			if (o.getValue() instanceof Property) {
				Property p = (Property) o.getValue();
				if (propertyToDisplay(p)) {
					if (!ConversionUtils.hasValue(p.getName())) {
						continue;
					}
					sb.append(NLS.bind(rowTemplate, new Object[] { p.getName(), p.getValue() }));
				}
			} else if (o.getValue() instanceof Table) {
				Table t = (Table) o.getValue();
				if (propertyToDisplay(t)) {
					if (!ConversionUtils.hasValue(t.getName())) {
						continue;
					}
					StringBuffer value = new StringBuffer();
					value.append("<table border=1 style='font-size:14px; font-family:'Arial'' width='100%'>"); //$NON-NLS-1$
					int i = 1;
					for (Row r : t.getRow()) {
						int height = r.getProperty().size() + 1;
						value.append(NLS.bind("<tr><td width='20%' rowspan='" + height + "'>{0}</td>", "Row " + i)); //$NON-NLS-1$ //$NON-NLS-2$
						i++;
						value.append(NLS.bind(
								"<th width='20%'>{0}</th><th width='80%'>{1}</th></tr>", WESBConversionMessages.propertyName, //$NON-NLS-1$
								WESBConversionMessages.propertyValue));
						for (Property p1 : r.getProperty()) {
							value.append(NLS.bind(rowTemplate, new Object[] { p1.getName(), p1.getValue() }));
						}
						value.append("</td></tr>"); //$NON-NLS-1$
					}
					value.append("</table>"); //$NON-NLS-1$
					sb.append(NLS.bind(rowTemplate, new Object[] { t.getName(), value }));
				}
			}
		}

		String headerMessage = NLS.bind(WESBConversionMessages.todoUnsupportedPrimitive, new Object[] { nodeName,
				converterContext.sourcePrimitive.getType(), subFlowFile.getFullPath().toString() });
		String message;
		ArrayList<JAXBElement<? extends AbstractProperty>> al = new ArrayList<JAXBElement<? extends AbstractProperty>>();
		if (al.equals(converterContext.sourcePrimitive.getAbstractProperty())) {
			message = headerMessage;
		} else {
			if ("CustomMediation".equals(converterContext.sourcePrimitive.getType())) {
				headerMessage = headerMessage + WESBConversionMessages.todoUnsupportedPrimitive_tableDescCustomMediation;
			} else {
				headerMessage = headerMessage + WESBConversionMessages.todoUnsupportedPrimitive_tableDesc;
			}
			message = NLS.bind(messageTemplate, new Object[] { headerMessage, WESBConversionMessages.propertyName,
					WESBConversionMessages.propertyValue, sb.toString() });
		}
		createToDoTask(flowFile, message, subFlowFile.getFullPath().toString());
		return nodes;
	}

	@Override
	public String getConvertedTo() {
		return "Subflow placeholder"; //$NON-NLS-1$
	}

	private boolean propertyToDisplay(AbstractProperty ap) {
		if ("sibxOnlyFireAtEnd".equals(ap.getName()) || "sibxMayChangeMessage".equals(ap.getName())
				|| "sibxNoChangeOnFailure".equals(ap.getName()) || "implementationClass".equals(ap.getName())) {
			return false;
		} else {
			return true;
		}
	}
}
