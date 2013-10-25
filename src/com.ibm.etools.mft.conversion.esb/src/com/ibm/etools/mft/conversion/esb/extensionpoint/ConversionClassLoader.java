package com.ibm.etools.mft.conversion.esb.extensionpoint;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.ibm.etools.mft.conversion.esb.ConversionUtils;
import com.ibm.etools.mft.conversion.esb.model.ClassDefinition;

/**
 * @author Zhongming Chen
 *
 */
public class ConversionClassLoader extends URLClassLoader {

	protected HashMap<String, Class> classes = new HashMap<String, Class>();
	private ClassLoader parentLoader;
	private ClassDefinition classDefinition;

	public ConversionClassLoader(URL[] urls) {
		super(urls);
	}

	public Class loadClass(String className) throws ClassNotFoundException {
		return findClass(className);
	}

	public Class findClass(String className) {
		String method = "findClass"; //$NON-NLS-1$
		byte classByte[];
		Class result = null;
		result = (Class) classes.get(className);
		if (result != null) {
			return result;
		}

		try {
			result = super.findClass(className);
			return result;
		} catch (Exception e) {
		}

		try {
			result = parentLoader.loadClass(className);
			return result;
		} catch (Exception e) {
		}

		try {
			String classPath = className.replace('.', File.separatorChar) + ".class"; //$NON-NLS-1$

			classPath = classPath.replace('/', File.separatorChar);
			classByte = loadClassData(classPath);
			result = defineClass(className, classByte, 0, classByte.length, (CodeSource) null);
			classes.put(className, result);
			return result;
		} catch (Throwable e) {
		}

		try {
			result = loadFromProjectBuildPath(className);
			if (result != null) {
				classes.put(className, result);
				return result;
			}
		} catch (Throwable e) {
		}
		return null;
	}

	protected Class loadFromProjectBuildPath(String className) throws JavaModelException {
		if (PrimitiveManager.WORKSPACE.equals(classDefinition.getResourceType())) {
			IResource r = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(classDefinition.getResourcePath()));
			IProject p = r.getProject();
			IJavaProject jp = JavaCore.create(p);
			IType type = jp.findType(className);
			byte[] classByte = type.getClassFile().getBytes();
			Class<?> result = defineClass(className, classByte, 0, classByte.length, (CodeSource) null);
			return result;
		}
		return null;
	}

	private byte[] loadClassData(String classPath) throws Exception {
		if (PrimitiveManager.JAR_SPACE.equals(classDefinition.getResourceType())) {
			FileInputStream fis = new FileInputStream(new File(classDefinition.getResourcePath()));
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

			byte[] content = null;
			ZipEntry e = null;
			while ((e = zis.getNextEntry()) != null) {
				String n = e.getName();
				if (!e.isDirectory() && n.equals(classPath)) {
					content = ConversionUtils.readZipContent(zis, e);
					break;
				}
			}

			zis.close();

			return content;
		} else if (PrimitiveManager.WORKSPACE.equals(classDefinition.getResourceType())) {
			IResource r = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(classDefinition.getResourcePath()));
			IProject p = r.getProject();
			IJavaProject jp = JavaCore.create(p);
			IJavaElement javaElement = jp.findElement(new Path(classPath));
			IFile classFile = ResourcesPlugin.getWorkspace().getRoot().getFile(jp.getOutputLocation().append(new Path(classPath)));
			if (classFile.exists()) {
				InputStream i = classFile.getContents();
				ByteArrayOutputStream o = new ByteArrayOutputStream();
				int c = 0;
				while ((c = i.read()) != -1) {
					o.write(c);
				}
				o.flush();
				return o.toByteArray();
			}
		}
		return null;
	}

	public void setClassDefinition(ClassDefinition clazz, ClassLoader oldLoader) {
		this.classDefinition = clazz;
		this.parentLoader = oldLoader;
	}
}