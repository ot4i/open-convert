/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extension.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.WESBConversionMessages;
import com.ibm.etools.mft.conversion.esb.editor.Browser;
import com.ibm.etools.mft.conversion.esb.editor.ConversionResultViewer;
import com.ibm.etools.mft.conversion.esb.extensionpoint.IConversionResultRenderer;

/**
 * @author Zhongming Chen
 * 
 */
public class ConversionSummaryRenderer implements IConversionResultRenderer {

	public static final String ID = "ConversionSummaryRenderer"; //$NON-NLS-1$

	public Browser contentViewer;

	/**
	 * 
	 */
	public ConversionSummaryRenderer() {
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void createControl(ConversionResultViewer editor, Composite parent) {
		contentViewer = new Browser(parent, SWT.BORDER);
		contentViewer.setLayoutData(new GridData(GridData.FILL_BOTH));
		contentViewer.setFont(parent.getFont());
	}

	@Override
	public void setData(Object data) {
		if (data instanceof IConversionResultRenderer.ConversionSummaryData) {
			ConversionSummaryData d = (ConversionSummaryData) data;
			String indexTemplate = ConversionUtils.loadTemplate("internal/conversionSummary/index.html"); //$NON-NLS-1$
			String projectRowTemplate = ConversionUtils.loadTemplate("internal/conversionSummary/project_row.html"); //$NON-NLS-1$
			String resourceRowTemplate = ConversionUtils.loadTemplate("internal/conversionSummary/resource_row.html"); //$NON-NLS-1$
			StringBuffer sb = new StringBuffer();

			HashMap<String, List<String>> resources = new HashMap<String, List<String>>();
			HashMap<String, List<String>> sourceProjectToTargetProject = new HashMap<String, List<String>>();
			for (String k : d.sourceToTargets.keySet()) {
				IResource r = ConversionUtils.getResource(new Path(k));
				String p = r.getProject().getName();
				if (r instanceof IProject) {
					List<String> ps = new ArrayList<String>();
					sourceProjectToTargetProject.put(p, ps);
					for (String k1 : d.sourceToTargets.get(k)) {
						ps.add(ConversionUtils.getResource(new Path(k1)).getName());
					}
				} else {
					List<String> rs = resources.get(p);
					if (rs == null) {
						resources.put(p, rs = new ArrayList<String>());
					}
					rs.add(r.getProjectRelativePath().toString());
					Collections.sort(rs);
				}
			}

			List<String> projects = new ArrayList<String>(resources.keySet());
			Collections.sort(projects);

			for (String p : projects) {
				List<String> ps = sourceProjectToTargetProject.get(p);
				StringBuffer sb1 = new StringBuffer();
				boolean showTargetProjectName = ps.size() > 1;
				for (String r : ps) {
					sb1.append("&nbsp;&nbsp;" + r + "<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				sb.append(NLS.bind(projectRowTemplate, new Object[] { WESBConversionMessages.project, p, sb1.toString() }));
				List<String> rs = resources.get(p);
				for (String r : rs) {
					sb1 = new StringBuffer();
					List<String> sortedResources = new ArrayList<String>(d.sourceToTargets.get("/" + p + "/" + r)); //$NON-NLS-1$ //$NON-NLS-2$
					Collections.sort(sortedResources);
					for (String target : sortedResources) {
						IResource targetResource = ConversionUtils.getResource(new Path(target));
						sb1.append(showTargetProjectName ? targetResource.getFullPath().toString() : targetResource
								.getProjectRelativePath().toString() + "<br/>"); //$NON-NLS-1$
					}
					sb.append(NLS.bind(resourceRowTemplate, new Object[] { r, sb1.toString() }));
				}
			}

			indexTemplate = NLS.bind(indexTemplate, new Object[] { WESBConversionMessages.sourceToTargetDescription,
					WESBConversionMessages.sourceResource, WESBConversionMessages.targetResource, sb.toString() });
			contentViewer.setText(indexTemplate);
		}
	}

	@Override
	public void setShowAll(boolean showAll) {
	}

	@Override
	public void refresh() {
	}

}
