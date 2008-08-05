package org.aifb.xxplore;

import org.ateam.xxplore.core.ExploreEnvironment;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ide.model.WorkbenchAdapterBuilder;

public class ExploreWorkbenchAdvisor extends WorkbenchAdvisor {

	@SuppressWarnings("restriction")
	public void preStartup(){
		WorkbenchAdapterBuilder.registerAdapters();
	}
	
	public void initialize(IWorkbenchConfigurer configurer) {
	    super.initialize(configurer);
	    configurer.setSaveAndRestore(true);
	}

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ExploreWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return ExploreEnvironment.DEFAULT_START_PERSPECTIVE;
	}

}
