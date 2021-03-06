package werkzeugkasten.weblauncher;

import static werkzeugkasten.weblauncher.Constants.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import werkzeugkasten.common.debug.LaunchUtil;
import werkzeugkasten.common.debug.TerminateListener;
import werkzeugkasten.common.jdt.JavaElementUtil;
import werkzeugkasten.common.runtime.LogUtil;
import werkzeugkasten.common.ui.ImageLoader;
import werkzeugkasten.common.ui.WorkbenchUtil;
import werkzeugkasten.common.util.FileUtil;
import werkzeugkasten.common.util.StringUtil;
import werkzeugkasten.common.viewers.AbstractLightweightLabelDecorator;
import werkzeugkasten.launcher.ConfigurationFacetRegistry;
import werkzeugkasten.weblauncher.nls.Images;
import werkzeugkasten.weblauncher.preferences.WebPreferences;
import werkzeugkasten.weblauncher.preferences.impl.WebPreferencesImpl;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The shared instance
	private static Activator plugin;

	private static Map<String, IProject> urlToProjectCache;

	private TerminateListener terminateListener = new TerminateListener() {
		@Override
		public void handle(ILaunch l) throws CoreException {
			String id = l.getLaunchConfiguration().getType().getIdentifier();
			if (ID_LAUNCH_CONFIG.equals(id)) {
				IProject p = LaunchUtil.getProject(l);
				Activator.setLaunch(p, null);
				Activator.reject(p);
				AbstractLightweightLabelDecorator.updateDecorators(
						ID_DECORATOR, p);
			}
		}
	};

	private ConfigurationFacetRegistry launchRegistry = new ConfigurationFacetRegistry(
			ID_PLUGIN, EXT_LAUNCHCONFIG_FACET);

	private ConfigurationFacetRegistry libraryRegistry = new ConfigurationFacetRegistry(
			ID_PLUGIN, EXT_LIBRARYCONFIGURATOR);

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		DebugPlugin.getDefault().addDebugEventListener(terminateListener);
		ImageLoader.load(plugin, Images.class);
		urlToProjectCache = new Hashtable<String, IProject>();
		JavaElementUtil.appendEditorSeeker();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		DebugPlugin.getDefault().removeDebugEventListener(terminateListener);
		terminateAll();
		urlToProjectCache.clear();
		urlToProjectCache = null;
		launchRegistry.dispose();
		ImageLoader.unload(plugin, Images.class);
		plugin = null;
		super.stop(context);
	}

	protected void terminateAll() {
		for (IProject p : urlToProjectCache.values()) {
			ILaunch l = getLaunch(p);
			if (l != null && l.canTerminate()) {
				try {
					l.terminate();
				} catch (DebugException e) {
					log(e);
				}
			}
		}
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static WebPreferences getPreferences(IProject project) {
		return new WebPreferencesImpl(project);
	}

	public static void log(String msg) {
		LogUtil.log(getDefault(), msg);
	}

	public static void log(Throwable throwable) {
		LogUtil.log(getDefault(), throwable);
	}

	public static void setLaunch(IProject project, ILaunch launch) {
		try {
			if (project != null) {
				project.setSessionProperty(KEY_SERVER_STATE, launch);
			}
		} catch (CoreException e) {
			log(e);
		}
	}

	public static ILaunch getLaunch(IProject project) {
		ILaunch result = null;
		try {
			if (project != null) {
				result = (ILaunch) project.getSessionProperty(KEY_SERVER_STATE);
			}
		} catch (CoreException e) {
			log(e);
		}
		return result;
	}

	public static void entry(IProject project, URL url) {
		urlToProjectCache.put(url.toExternalForm(), project);
	}

	public static IProject findProject(String url) {
		if (StringUtil.isEmpty(url) == false) {
			return urlToProjectCache.get(url);
		}
		return null;
	}

	public synchronized static void reject(IProject project) {
		if (project == null) {
			return;
		}
		List<String> l = new ArrayList<String>();
		for (String key : urlToProjectCache.keySet()) {
			if (project.equals(urlToProjectCache.get(key))) {
				l.add(key);
			}
		}
		for (String key : l) {
			urlToProjectCache.remove(key);
		}
	}

	public synchronized static void exit(String url) {
		if (StringUtil.isEmpty(url) == false) {
			urlToProjectCache.remove(url);
		}
	}

	public static ConfigurationFacetRegistry getLaunchRegistry() {
		return getDefault().launchRegistry;
	}

	public static ConfigurationFacetRegistry getLibraryRegistry() {
		return getDefault().libraryRegistry;
	}

	public static void tempFileDeletion(final IProject project, final File work) {
		DebugPlugin.getDefault().addDebugEventListener(new TerminateListener() {
			public void handle(ILaunch l) throws CoreException {
				String id = l.getLaunchConfiguration().getType()
						.getIdentifier();
				if (ID_LAUNCH_CONFIG.equals(id)) {
					IProject p = LaunchUtil.getProject(l);
					if (project.equals(p)) {
						FileUtil.delete(work.getAbsolutePath());
						DebugPlugin.getDefault().removeDebugEventListener(this);
					}
				}
			}
		});
	}

	// exportしてないライブラリすら、lookupの対象になっているが、取り合えず無視。
	public static void setSourceLocator(IProject project,
			ILaunchConfigurationWorkingCopy copy) throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		String type = copy.getType().getSourceLocatorId();
		IPersistableSourceLocator locator = manager.newSourceLocator(type);
		if (locator instanceof AbstractSourceLookupDirector) {
			IJavaProject javap = JavaCore.create(project);
			IRuntimeClasspathEntry[] entries = JavaRuntime
					.computeUnresolvedRuntimeClasspath(javap);
			IRuntimeClasspathEntry[] resolved = JavaRuntime
					.resolveSourceLookupPath(entries, copy);
			AbstractSourceLookupDirector asld = (AbstractSourceLookupDirector) locator;
			asld.initializeDefaults(copy);
			asld.setSourceContainers(JavaRuntime.getSourceContainers(resolved));
			copy.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO,
					asld.getMemento());
			copy.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, asld
					.getId());

		}
	}

	public static String getBundleVersion() {
		return getDefault().getBundle().getHeaders().get(
				org.osgi.framework.Constants.BUNDLE_VERSION).toString();
	}

	public static void setVersion(ILaunchConfigurationWorkingCopy copy) {
		copy.setAttribute(org.osgi.framework.Constants.BUNDLE_VERSION,
				getBundleVersion());

	}

	public static boolean isSameVersion(ILaunchConfiguration config)
			throws CoreException {
		String version = config.getAttribute(
				org.osgi.framework.Constants.BUNDLE_VERSION, (String) null);
		String me = getBundleVersion();
		return me.equals(version);
	}

	public static IProject findCurrentProject() {
		IProject result = WorkbenchUtil.getCurrentSelectedProject();
		if (result != null) {
			return result;
		}
		return getProjectByBrowserId();
	}

	public static IProject getProjectByBrowserId() {
		IProject result = null;
		// see. ViewDatabaseManagerAction
		IWorkbenchWindow window = WorkbenchUtil.getWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				// getActiveEditorで取れる参照は、フォーカスがどこにあってもアクティブなエディタの参照が取れてしまう為。
				IWorkbenchPart part = page.getActivePart();
				if (part instanceof IEditorPart) {
					IEditorPart editor = (IEditorPart) part;
					IEditorInput input = editor.getEditorInput();
					if (input instanceof IPersistableElement) {
						IPersistableElement element = (IPersistableElement) input;
						IMemento memento = XMLMemento.createWriteRoot("root");
						// see. WebBrowserEditorInput
						element.saveState(memento);
						String url = memento.getString("url");
						result = Activator.findProject(url);
					}
				}
			}
		}
		return result;
	}

}
