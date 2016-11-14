//----------------------------------------------------------------------------
// Copyright (C) 2011  Ingrid Nunes
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://inf.ufrgs.br/prosoft/bdi4jade/
//
//----------------------------------------------------------------------------
package br.ufrgs.inf.bdinetr;

import jade.BootProfileImpl;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Specifier;
import jade.core.event.NotificationService;
import jade.core.messaging.TopicManagementHelper;
import jade.core.messaging.TopicManagementService;
import jade.tools.rma.rma;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.PlatformController;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observer;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import br.ufrgs.inf.bdinetr.domain.AbstractRouterComponentFactory;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.Observable;
import br.ufrgs.inf.bdinetr.domain.Role;
import br.ufrgs.inf.bdinetr.domain.Router;
import br.ufrgs.inf.bdinetr.domain.dummy.DummyRouterComponentFactory;
import br.ufrgs.inf.bdinetr.domain.omnet.OMNeTRouterComponentFactory;

/**
 * @author Ingrid Nunes
 */
public class BDINetRApp {

	enum SimulationType {
		OMNeT, SINGLE_AGENT, SIMPLE_NETWORK, COMPLEX_NETWORK
	};

	private static final Set<Link> AFFECTED_LINKS;
	private static final Network NETWORK;
	private static final SimulationType SIMULATION_TYPE = SimulationType.SINGLE_AGENT;

	static {
		PropertyConfigurator.configure(BDINetRApp.class
				.getResource("log4j.properties"));

		NETWORK = new Network();
		AFFECTED_LINKS = new HashSet<>();
		AbstractRouterComponentFactory factory;

		switch (SIMULATION_TYPE) {
		case OMNeT:
			factory = new OMNeTRouterComponentFactory();
			NETWORK.addRouter(new Router(new Ip("Inet.sas1.core0.idsModule"),
					"ids_one", Role.ANOMALY_DETECTION.getId(), factory));
			NETWORK.addRouter(new Router(new Ip("Inet.sas1.core0.linkMonitor"),
					"linkmonitor_one", Role.LINK_MONITOR.getId(), factory));
			NETWORK.addRouter(new Router(new Ip("Inet.sas1.core0.classifier"),
					"classifier_one", Role.CLASSIFIER.getId(), factory));
			NETWORK.addRouter(new Router(
					new Ip("Inet.sas1.core0.flowExporter"), "flowexporter_one",
					Role.FLOW_EXPORTER.getId(), factory));
			NETWORK.addRouter(new Router(new Ip("Inet.sas1.core0.rateLimiter"),
					"ratelimiter_one", Role.RATE_LIMITER.getId(), factory));
			break;
		case SINGLE_AGENT:
			factory = new DummyRouterComponentFactory();
			NETWORK.addRouter(new Router(new Ip("SINGLE_AGENT"), Role.LINK_MONITOR
					.getId() | Role.RATE_LIMITER.getId() | Role.ANOMALY_DETECTION.getId() | Role.CLASSIFIER
					.getId() | Role.FLOW_EXPORTER
					.getId(), factory));
			AFFECTED_LINKS.add(new Link("AFFECTED_LINK"));
			break;
		case SIMPLE_NETWORK:
			factory = new DummyRouterComponentFactory();
			NETWORK.addRouter(new Router(new Ip("LC+RL"), Role.LINK_MONITOR
					.getId() | Role.RATE_LIMITER.getId(), factory));
			NETWORK.addRouter(new Router(new Ip("IPC+RL"),
					Role.ANOMALY_DETECTION.getId() | Role.RATE_LIMITER.getId(),
					factory));
			NETWORK.addRouter(new Router(new Ip("FC+RL"), Role.CLASSIFIER
					.getId() | Role.RATE_LIMITER.getId(), factory));
			NETWORK.addRouter(new Router(new Ip("FE"), Role.FLOW_EXPORTER
					.getId(), factory));

			AFFECTED_LINKS.add(new Link("AFFECTED_LINK"));
			break;
		case COMPLEX_NETWORK:
			factory = new DummyRouterComponentFactory();
			NETWORK.addRouter(new Router(new Ip("RouterLM"), Role.LINK_MONITOR
					.getId(), factory));
			NETWORK.addRouter(new Router(new Ip("RouterRL"), Role.RATE_LIMITER
					.getId(), factory));
			NETWORK.addRouter(new Router(new Ip("RouterAD"),
					Role.ANOMALY_DETECTION.getId(), factory));
			NETWORK.addRouter(new Router(new Ip("RouterCL"), Role.CLASSIFIER
					.getId(), factory));
			NETWORK.addRouter(new Router(new Ip("RouterCL+FE"), Role.CLASSIFIER
					.getId() | Role.FLOW_EXPORTER.getId(), factory));
			NETWORK.addRouter(new Router(new Ip("RouterFE"), Role.FLOW_EXPORTER
					.getId(), factory));

			AFFECTED_LINKS.add(new Link("AFFECTED_LINK"));
			break;
		}

		Set<Router> routers = NETWORK.getRouters();
		for (Router router1 : routers) {
			if (router1.hasRole(Role.RATE_LIMITER)) {
				for (Router router2 : routers) {
					if (router2.hasRole(Role.LINK_MONITOR)) {
						((Observable) router1.getRole(Role.RATE_LIMITER))
								.addObserver(((Observer) router2
										.getRole(Role.LINK_MONITOR)));
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		new BDINetRApp().createAndShowGUI();
	}

	private ProfileImpl bootProfile;
	private final Log log;
	private jade.core.Runtime runtime;

	public BDINetRApp() {
		this.log = LogFactory.getLog(this.getClass());

		List<String> params = new ArrayList<String>();
		params.add("-gui");
		params.add("-detect-main:false");
		params.add("-services:" + TopicManagementHelper.SERVICE_NAME);

		this.bootProfile = new ProfileImpl();
		bootProfile.setParameter(Profile.GUI, "truw");
		bootProfile.setParameter(BootProfileImpl.PLATFORM_ID,
				BDINetRApp.class.getSimpleName());
		bootProfile.setParameter(Profile.DETECT_MAIN, "false");

		jade.util.leap.List result = new jade.util.leap.ArrayList();
		Specifier s = new Specifier();
		s.setName("RMA");
		s.setClassName(rma.class.getName());
		result.add(s);
		bootProfile.setSpecifiers(Profile.AGENTS, result);

		result = new jade.util.leap.ArrayList();
		s = new Specifier();
		s.setClassName(TopicManagementService.class.getName());
		result.add(s);
		s = new Specifier();
		s.setClassName(NotificationService.class.getName());
		result.add(s);
		bootProfile.setSpecifiers(Profile.SERVICES, result);
		log.info("Plataform parameters: " + bootProfile);

		this.runtime = jade.core.Runtime.instance();
		PlatformController controller = runtime
				.createMainContainer(bootProfile);

		for (Router router : NETWORK.getRouters()) {
			try {
				AgentController ac = ((AgentContainer) controller)
						.acceptNewAgent(router.getId(),
								NETWORK.getAgent(router));
				ac.start();
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

	public void createAndShowGUI() {
		JPanel panel = new JPanel(new GridLayout(1, 1));
		JButton button = new JButton("Run!");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				run();
			}
		});
		panel.add(button);
		final JFrame frame = new JFrame();
		frame.setTitle(BDINetRApp.class.getSimpleName());
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.setVisible(true);
			}
		});
	}

	public void run() {
		log.info("Updating link usage");
		for (Link link : AFFECTED_LINKS) {
			for (Router router : NETWORK.getRouters()) {
				if (router.hasRole(Role.LINK_MONITOR)) {
					LinkMonitor lm = (LinkMonitor) router
							.getRole(Role.LINK_MONITOR);
					lm.setOverUsage(link, true);
				}
			}
		}
	}

}
