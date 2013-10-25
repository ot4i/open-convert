package com.ibm.etools.mft.conversion.esb.editor.parameter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import com.ibm.etools.mft.conversion.esb.model.SCAModule;
import com.ibm.etools.mft.conversion.esb.model.WESBProject;

/**
 * @author Zhongming Chen
 * 
 */
public class WESBResourceContentProvider extends ArrayContentProvider implements ITreeContentProvider {

	public WESBResourceContentProvider() {
	}

	@Override
	public Object[] getElements(Object o) {
		if (o instanceof WESBProject) {
			List<Object> kids = new ArrayList<Object>();
			addKid(kids, ((WESBProject) o).getModule());
			addKid(kids, ((WESBProject) o).getSchemas());
			addKid(kids, ((WESBProject) o).getMaps());
			addKid(kids, ((WESBProject) o).getJavas());
			return kids.toArray();
		} else if (o instanceof SCAModule) {
			return ((SCAModule) o).getChildren().toArray();
		}
		return super.getElements(o);
	}

	protected void addKid(List<Object> kids, Object kid) {
		if (kid != null) {
			kids.add(kid);
		}
	}

	public Object[] getChildren(Object o) {
		return getElements(o);
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

}
