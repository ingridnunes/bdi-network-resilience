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
import jade.core.Agent;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import br.ufrgs.inf.bdinetr.agent.RouterAgent;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.Role;
import br.ufrgs.inf.bdinetr.domain.AbstractRouterComponentFactory;
import br.ufrgs.inf.bdinetr.domain.Router;
import br.ufrgs.inf.bdinetr.domain.dummy.DummyRouterComponentFactory;

/**
 * @author Ingrid Nunes
 */
public class BDINetRApp {

	private static final Map<Ip, Agent> AGENTS;
	private static final AbstractRouterComponentFactory FACTORY = new DummyRouterComponentFactory();
	private static final Network NETWORK;

	static {
		PropertyConfigurator.configure(BDINetRApp.class
				.getResource("log4j.properties"));

		Set<Router> routers = new HashSet<>();
		routers.add(new Router(new Ip("RouterLM"), Role.LINK_MONITOR.getId(), FACTORY));
		routers.add(new Router(new Ip("RouterRL"), Role.RATE_LIMITER.getId(), FACTORY));
		routers.add(new Router(new Ip("RouterRL+FE"), Role.RATE_LIMITER.getId() | Role.FLOW_EXPORTER.getId(), FACTORY));
		routers.add(new Router(new Ip("RouterAD"), Role.ANOMALY_DETECTION.getId(), FACTORY));
		routers.add(new Router(new Ip("RouterAD+RL"), Role.ANOMALY_DETECTION.getId() | Role.RATE_LIMITER.getId(), FACTORY));
		routers.add(new Router(new Ip("RouterCL"), Role.CLASSIFIER.getId(), FACTORY));
		routers.add(new Router(new Ip("RouterCL+FE"), Role.CLASSIFIER.getId() | Role.FLOW_EXPORTER.getId(), FACTORY));
		routers.add(new Router(new Ip("RouterFE"), Role.FLOW_EXPORTER.getId(), FACTORY));

		Link affectedLink = new Link("AFFECTED_LINK");

		Set<Link> links = new HashSet<>();
		links.add(affectedLink);
		links.add(new Link("LINK_01"));
		links.add(new Link("LINK_02"));

		Set<Link> affectedLinks = new HashSet<>();
		affectedLinks.add(affectedLink);

		NETWORK = new Network(routers, links, affectedLinks);

		AGENTS = new HashMap<>();
		for (Router router : NETWORK.getRouters()) {
			AGENTS.put(router.getIp(), new RouterAgent(router));
		}
	}

	public static void main(String[] args) {
		new BDINetRApp();

		JPanel panel = new JPanel(new GridLayout(1, 1));
		JButton button = new JButton("Run!");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NETWORK.run();
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

		for (Ip agentName : AGENTS.keySet()) {
			try {
				AgentController ac = ((AgentContainer) controller)
						.acceptNewAgent(agentName.toString(),
								AGENTS.get(agentName));
				ac.start();
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

}
