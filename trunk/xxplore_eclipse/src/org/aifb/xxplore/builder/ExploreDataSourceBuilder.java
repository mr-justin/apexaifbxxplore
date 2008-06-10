package org.aifb.xxplore.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ExploreDataSourceBuilder extends IncrementalProjectBuilder {

	protected final static String BUILDER_ID= "ExploreDataSourceBuilder";
	
	public ExploreDataSourceBuilder() {
		
	}

	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		if (kind == IncrementalProjectBuilder.FULL_BUILD) {
	         fullBuild(monitor);
	      } else {
	         IResourceDelta delta = getDelta(getProject());
	         if (delta == null) {
	            fullBuild(monitor);
	         } else {
	            incrementalBuild(delta, monitor);
	         }
	      }
	      return null;
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) {
		 // the visitor does the work.
	      try {
			delta.accept(new ExploreBuildDeltaVisitor());
		} catch (CoreException e) {

			e.printStackTrace();
		}
	}

	private void fullBuild(IProgressMonitor monitor) {
		 try {
	         getProject().accept(new ExploreBuildVisitor());
	      } catch (CoreException e) { 	    	  
	    	  e.printStackTrace();
	      }	   
	}
	
}
