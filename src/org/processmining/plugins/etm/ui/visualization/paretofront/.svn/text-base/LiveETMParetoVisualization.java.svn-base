package org.processmining.plugins.etm.ui.visualization.paretofront;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.ETMPareto;
import org.processmining.plugins.etm.live.ETMLiveListener;
import org.processmining.plugins.etm.model.ParetoFront;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * This class provides a visualization on a running {@link ETMPareto} instance
 * showing both controls towards the ETMPareto instance as well as the
 * 'standard' ParetoFront visualization which is updated whenever the ETMPareto
 * instance signals an update.
 * 
 * @author jbuijs
 * 
 */
public class LiveETMParetoVisualization implements ETMLiveListener<ParetoFront> {
	
	//TODO make dropdown with update intervals (0 = real time) instead of fixed value

	/**
	 * The main panel, containing the control panel on top and below that the
	 * 'normal' visualization
	 */
	private JPanel mainPanel;

	/**
	 * The default 'live' control panel allowing to stop a running ETM instance
	 * and see basic current stats
	 */
	private JPanel controlPanel;

	/**
	 * The Pareto visualization
	 */
	protected ParetoVisualization paretoVis;

	/**
	 * A simple label notifying the user that we are waiting for the first
	 * result
	 */
	private JLabel waitingLabel;

	private ETMPareto etm;

	private JButton forceRefreshButton;

	private JLabel generationCountLabel;

	private CentralRegistry registry;

	/**
	 * The time when we last updated the GUI
	 */
	private long lastUpdate = 0;

	/**
	 * The timeout between different updates (in milliseconds, 1000 = sec)
	 */
	private long updateInterval = 1 * 1000;

	private JButton cancelButton;

	private PluginContext context;

	@Plugin(
			name = "Visualize and control running ETM Pareto",
				returnLabels = { "ETM Pareto Live Visualization" },
				returnTypes = { JComponent.class },
				parameterLabels = { "ETM Pareto Instance" },
				userAccessible = true)
	@Visualizer
	public JComponent visualize(final PluginContext context, ETMPareto etm) {
		etm.getListenerList().add(this);

		this.etm = etm;
		this.context = context;

		registry = etm.getParams().getCentralRegistry();

		//If the ETM has no result object 'yet', then don't instantiate the visualization but wait for the first update call
		if (etm.getResult() == null) {
			paretoVis = null;
		}

		return getMainPanel();
	}

	public JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = SlickerFactory.instance().createRoundedPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			mainPanel.add(getControlPanel());
			if (paretoVis == null) {
				mainPanel.add(getWaitingLabel());
			} else {
				mainPanel.add(paretoVis);
			}
		}
		return mainPanel;
	}

	private JLabel getWaitingLabel() {
		if (waitingLabel == null) {
			waitingLabel = SlickerFactory
					.instance()
					.createLabel(
							"We are currently awaiting the first result. Please be patient, depending on the settings and the event log, this can take a while.");
			SlickerDecorator.instance().decorate(waitingLabel);
		}
		return waitingLabel;
	}

	public JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = SlickerFactory.instance().createRoundedPanel();

			controlPanel.add(SlickerFactory.instance().createLabel("ETM Live control panel:"));

			controlPanel.add(getForceRefreshButton());
			controlPanel.add(getCancelButton());
			controlPanel.add(getGenerationCountLabel());
		}
		return controlPanel;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = SlickerFactory.instance().createButton("Cancel");

			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					etm.getParams().getTerminationConditionExternal().shouldTerminate = true;
					cancelButton.setEnabled(false);
					cancelButton.setText("Cancelled");
				}
			});
		}

		return cancelButton;
	}

	private JButton getForceRefreshButton() {
		if (forceRefreshButton == null) {
			forceRefreshButton = SlickerFactory.instance().createButton("Force GUI Update");

			forceRefreshButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					paretoVis.updateFront(true);
				}
			});
		}

		return forceRefreshButton;
	}

	private JLabel getGenerationCountLabel() {
		if (generationCountLabel == null) {
			generationCountLabel = SlickerFactory.instance().createLabel("Not started yet.");
		}

		return generationCountLabel;
	}

	public void start() {
		//We don't react (yet) to a start notification
	}

	public void generationFinished(ParetoFront result) {
		//If this is the first generation that is finished then initialize the pareto visualization on this first result
		if (paretoVis == null) {
			List<AbstractParetoFrontNavigator> additionalNavigators = new ArrayList<AbstractParetoFrontNavigator>();
			paretoVis = new ParetoVisualization(result, context, additionalNavigators);
			additionalNavigators.add(new GenerationSeriesPlot(result, this));
			//additionalNavigators.add(new TimeSeriesPlot(result, this));
			additionalNavigators.add(new TimePerGenerationPlot(result, this));
			paretoVis.addNavigators(additionalNavigators);
			mainPanel.remove(getWaitingLabel());
			mainPanel.add(paretoVis);
			mainPanel.revalidate();
		}

		//Check if we should update
		if ((new Date()).getTime() >= lastUpdate + updateInterval) {
			getGenerationCountLabel().setText("Generation " + registry.getCurrentGeneration());
			paretoVis.updateFront(true);
			lastUpdate = (new Date()).getTime();
		} else {
			//Only pass on the data, don't update the GUI yet
			paretoVis.updateFront(false);
		}

	}

	public void finished(org.processmining.plugins.etm.live.ETMLiveListener.RunningState type) {
		etm.getParams().getTerminationConditionExternal().shouldTerminate = true;
	}

}
