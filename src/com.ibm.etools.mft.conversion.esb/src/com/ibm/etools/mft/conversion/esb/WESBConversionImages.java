package com.ibm.etools.mft.conversion.esb;

import java.util.Hashtable;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;

public class WESBConversionImages {

	public static final String IMAGE_USER_LOG_ENTRY = "icons/full/obj16/log_entry.gif"; //$NON-NLS-1$
	public static final String IMAGE_WARNING = "icons/full/obj16/warning.gif";; //$NON-NLS-1$
	public static String IMAGE_COMPLETE_TODO = "icons/full/obj16/complete_todo.gif"; //$NON-NLS-1$
	public static String IMAGE_ERROR = "icons/full/obj16/error.gif"; //$NON-NLS-1$
	public static String IMAGE_OUTSTANDING_TODO = "icons/full/obj16/outstanding_todo.gif"; //$NON-NLS-1$
	public static final String IMAGE_WESB_LIBRAY = "icons/full/obj16/library_obj.gif"; //$NON-NLS-1$
	public static final String IMAGE_WESB_MODULE = "icons/full/obj16/medproject_obj.gif"; //$NON-NLS-1$
	public static final String WIZARD_NEW_CONVERSION_SESSION = "icons/full/wizban/new_conversion_session_wiz.gif"; //$NON-NLS-1$
	public static final String IMAGE_WESB_COMPONENT = "icons/full/obj16/impcomp_obj.gif"; //$NON-NLS-1$
	public static final String IMAGE_WESB_EXPORT = "icons/full/obj16/export_obj.gif"; //$NON-NLS-1$
	public static final String IMAGE_WESB_IMPORT = "icons/full/obj16/import_obj.gif"; //$NON-NLS-1$
	public static final String IMAGE_SCA_MODULE = "icons/full/obj16/wirediag_obj.gif"; //$NON-NLS-1$
	public static final String IMAGE_WESB_MAPS = "icons/full/obj16/map_obj.gif"; //$NON-NLS-1$
	public static final String IMAGE_WESB_IMPORTER_WIZ = "/icons/full/wizban/ESBImport.gif"; //$NON-NLS-1$
	public static final String IMAGE_WESB_IMPORTER = "icons/full/obj16/map_obj.gif"; //$NON-NLS-1$
	public static final String IMAGE_WESB_JAVA = "icons/full/obj16/java.gif"; //$NON-NLS-1$
	public static final String IMAGE_WESB_SCHEMAS = "icons/full/obj16/schema.gif"; //$NON-NLS-1$

	public static String IMAGE_DEBUG_OVERLAY = "icons/full/ovr16/debug_ovr.gif"; //$NON-NLS-1$
	public static String[] IMAGEKEY_DEBUG_OVERLAY = new String[] { WESBConversionImages.IMAGE_DEBUG_OVERLAY,
			String.valueOf(IDecoration.BOTTOM_RIGHT) };

	/** Hashtable containing the actual images */
	private static Hashtable<ImageDescriptor, Image> images = new Hashtable<ImageDescriptor, Image>();
	private static Hashtable<String, ImageDescriptor> imageDescriptors = new Hashtable<String, ImageDescriptor>();
	private static Hashtable<String[], CompositeImageDescriptor> compositeImageDescriptors = new Hashtable<String[], CompositeImageDescriptor>();

	/**
	 * Constructor
	 */
	public WESBConversionImages() {

		images = new Hashtable<ImageDescriptor, Image>();
		imageDescriptors = new Hashtable<String, ImageDescriptor>();

	}

	/**
	 * Used to get an image out of the cache. If the requested image cannot be
	 * found in the cache, it is loaded from the file and returned - cached for
	 * future use.
	 * <p>
	 * 
	 * @param name
	 *            the key for the icon required
	 * @return imageDescriptor
	 */
	public static final ImageDescriptor getImageDescriptor(String name) {
		ImageDescriptor imgDesc = null;

		// Check to see if we already have the image loaded
		imgDesc = (ImageDescriptor) imageDescriptors.get(name);

		if (imgDesc == null) {

			// look in ImageRegistry
			imgDesc = WESBConversionPlugin.getDefault().getImageRegistry().getDescriptor(name);

			// if not found - load from file
			if (imgDesc == null) {
				try {
					imgDesc = ImageDescriptor.createFromURL(WESBConversionPlugin.getDefault().getBundle().getEntry(name));
				} catch (Exception e) {
					// in the event of an error, create a 'broken' icon instead
					imgDesc = ImageDescriptor.getMissingImageDescriptor();
				}
			}

			// cache the new image
			WESBConversionPlugin.getDefault().getImageRegistry().put(name, imgDesc);
		}

		// return the image description
		return imgDesc;
	}

	public static final ImageDescriptor getCompositeImageDescriptor(String[] key) {
		CompositeImageDescriptor imgDesc = null;
		imgDesc = compositeImageDescriptors.get(key);
		if (imgDesc == null) {
			try {
				imgDesc = new DecorationOverlayIcon(getImage(key[0]), getImageDescriptor(key[1]), Integer.parseInt(key[2]));
				compositeImageDescriptors.put(key, imgDesc);
			} catch (Exception e) {
				return getImageDescriptor(key[0]);
			}
		}
		return imgDesc;
	}

	/**
	 * Used to get an image out of the cache. If the requested image cannot be
	 * found in the cache, it is loaded from the file and returned - cached for
	 * future use.
	 * <p>
	 * 
	 * @param name
	 *            the key for the icon required
	 * @return image
	 */
	public static final Image getImage(String name) {
		// Find the image descriptor
		ImageDescriptor imgDescriptor = getImageDescriptor(name);

		// Check to see if we already have the image loaded
		Image img = (Image) images.get(imgDescriptor);

		if (img == null) {
			// Image not in the table, create it and store for future reference
			img = imgDescriptor.createImage();
			images.put(imgDescriptor, img);
		}

		return img;
	}

	public static final Image getCompositeImage(String[] key) {
		// get overlay descriptor
		ImageDescriptor overlayDescriptor = getCompositeImageDescriptor(key);

		// Check to see if we already have the image loaded
		Image img = (Image) images.get(overlayDescriptor);

		if (img == null) {
			// Image not in the table, create it and store for future reference
			img = overlayDescriptor.createImage();
			images.put(overlayDescriptor, img);
		}

		return img;
	}
}
