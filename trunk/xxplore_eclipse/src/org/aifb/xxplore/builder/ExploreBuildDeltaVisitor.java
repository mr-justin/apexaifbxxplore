package org.aifb.xxplore.builder;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

public class ExploreBuildDeltaVisitor implements IResourceDeltaVisitor {

	public boolean visit(IResourceDelta delta) throws CoreException {
		
		return false;
	}

}
