package org.processmining.plugins.etm.ui.visualization.paretofront;

import org.processmining.framework.util.ui.widgets.ProMScrollContainer;
import org.processmining.framework.util.ui.widgets.ProMTitledScrollContainerChild;
import org.processmining.plugins.etm.model.narytree.NAryTree;

/**
 * An abstract class for all ParetoFront navigators
 * 
 * @author jbuijs
 * 
 */
public abstract class AbstractParetoFrontNavigator extends ProMTitledScrollContainerChild {

	public AbstractParetoFrontNavigator(String title, ProMScrollContainer parent) {
		super(title, parent);
	}

	public AbstractParetoFrontNavigator(String title, ProMScrollContainer parent, boolean minimized) {
		super(title, parent, minimized);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Triggered when there is a new data update.
	 * 
	 * @param updateVis
	 *            boolean That indicates when the visualization is also allowed
	 *            to be updated. For performance reasons, please only update the
	 *            GUI when TRUE.
	 */
	public abstract void updateData(boolean updateVis);

	/**
	 * Triggered when the selected model is changed by the user
	 * 
	 * @param model
	 *            The newly selected model
	 */
	public abstract void updateSelectedModel(NAryTree model);

}
