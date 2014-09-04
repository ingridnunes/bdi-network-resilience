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
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.PlatformController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import br.ufrgs.inf.bdinetr.agent.RouterAgent;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.PReSETRouter;

/**
 * @author Ingrid Nunes
 */
public class BDINetRApp {

	private static final Map<Ip, Agent> AGENTS;

	static {
		PropertyConfigurator.configure(BDINetRApp.class
				.getResource("log4j.properties"));

		AGENTS = new HashMap<>();
		for (PReSETRouter router : Network.NETWORK.getRouters()) {
			AGENTS.put(router.getIp(), new RouterAgent(router));
		}
	}

	public static void main(String[] args) {
		new BDINetRApp();
		Network.NETWORK.run();
	}

	private ProfileImpl bootProfile;
	private final Log log;
	private jade.core.Runtime runtime;

	public BDINetRApp() {
		this.log = LogFactory.getLog(this.getClass());

		List<String> params = new ArrayList<String>();
		params.add("-gui");
		params.add("-detect-main:false");

		log.info("Plataform parameters: " + params);

		this.bootProfile = new BootProfileImpl(params.toArray(new String[0]));

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
