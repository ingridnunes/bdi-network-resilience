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
package br.ufrgs.inf.bdinetr.domain.omnet;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import br.ufrgs.inf.bdinetr.domain.ObservableImpl;
import br.ufrgs.inf.bdinetr.domain.Router;
import br.ufrgs.inf.bdinetr.domain.RouterComponent;

/**
 * @author Alberto Egon and Ingrid Nunes
 */
public abstract class OMNeTRouterComponent extends ObservableImpl implements
		RouterComponent {

	private final XmlRpcClient adaptorRPC;
	protected final Log log;
	protected final Router router;

	public OMNeTRouterComponent(Router router) {
		this.router = router;
		this.log = LogFactory.getLog(getClass());

		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

		try {
			config.setServerURL(new URL(router.getIp().getAddress()));
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		}

		this.adaptorRPC = new XmlRpcClient();
		this.adaptorRPC.setConfig(config);
	}

	public Object invoke(String remoteOp, Object[] args) {
		log.info("Invoking operation: " + remoteOp);
		for (int i = 0; i < args.length; i++) {
			log.info("args[" + i + "] = " + args[i]);
		}

		Object result = null;

		try {
			result = this.adaptorRPC.execute(router.getType() + "." + remoteOp,
					args);
		} catch (XmlRpcException xre) {
			xre.printStackTrace();
		}

		log.info("Done");
		return result;
	}

}
