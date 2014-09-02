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
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.PlatformController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import bdi4jade.core.AbstractBDIAgent;
import bdi4jade.examples.BDI4JADEExamplesPanel;
import br.ufrgs.inf.bdinetr.domain.IpAddress;
import br.ufrgs.inf.bdinetr.domain.Link;
import br.ufrgs.inf.bdinetr.domain.LinkMonitor;
import br.ufrgs.inf.bdinetr.domain.Network;
import br.ufrgs.inf.bdinetr.domain.PReSETRole;
import br.ufrgs.inf.bdinetr.domain.PReSETRouter;

/**
 * @author Ingrid Nunes
 */
public class BDINetRApp {

	class LinkUsageUpdater extends TimerTask {
		private static final double OVER_USAGE_PROBABILITY = 0.3;

		@Override
		public void run() {
			Map<Link, Boolean> overUsage = new HashMap<>();
			Random random = new Random(System.currentTimeMillis());
			for (Link link : NETWORK.getLinks()) {
				double d = random.nextDouble();
				overUsage.put(link, d < OVER_USAGE_PROBABILITY);
			}
			log.info("Updating link usage");
			for (PReSETRouter router : NETWORK.getRouters()) {
				if (router.hasRole(PReSETRole.LINK_MONITOR)) {
					LinkMonitor lm = (LinkMonitor) router
							.getRole(PReSETRole.LINK_MONITOR);
					for (Link link : overUsage.keySet()) {
						lm.setOverUsage(link, overUsage.get(link));
					}
				}
			}
			log.info("Restarting agents");
			for (AbstractBDIAgent agent : AGENTS.values()) {
				agent.restart();
			}
		}
	}

	private static final Map<IpAddress, AbstractBDIAgent> AGENTS;

	private static final Network NETWORK;

	static {
		PropertyConfigurator.configure(BDINetRApp.class
				.getResource("log4j.properties"));

		NETWORK = new Network();
		PReSETRouter firewall = new PReSETRouter(new IpAddress("Firewall 1"),
				PReSETRole.RATE_LIMITER.getId());
		NETWORK.addRouter(firewall);
		PReSETRouter linkMonitor = new PReSETRouter(new IpAddress(
				"Rate Limiter 1"), PReSETRole.LINK_MONITOR.getId());
		NETWORK.addRouter(linkMonitor);

		NETWORK.addLink(new Link("F1_RL1"));

		AGENTS = new HashMap<>();
		AGENTS.put(firewall.getIp(), new BDINetRAgent(firewall));
		AGENTS.put(linkMonitor.getIp(), new BDINetRAgent(linkMonitor));

	}

	public static void main(String[] args) {
		new BDINetRApp().run();
	}

	private ProfileImpl bootProfile;
	private final Log log;
	private jade.core.Runtime runtime;

	private Timer timer;

	public BDINetRApp() {
		this.log = LogFactory.getLog(this.getClass());
		this.timer = new Timer();

		List<String> params = new ArrayList<String>();
		params.add("-gui");
		params.add("-detect-main:false");

		log.info("Plataform parameters: " + params);

		this.bootProfile = new BootProfileImpl(params.toArray(new String[0]));

		this.runtime = jade.core.Runtime.instance();
		PlatformController controller = runtime
				.createMainContainer(bootProfile);

		for (IpAddress agentName : AGENTS.keySet()) {
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

	/**
	 * Creates and shows a GUI whose content pane is an
	 * {@link BDI4JADEExamplesPanel}.
	 */
	public void run() {
		int interval = 10 * 1000;
		this.timer.schedule(new LinkUsageUpdater(), interval, interval);
	}

}
