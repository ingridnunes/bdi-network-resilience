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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.ufrgs.inf.bdinetr.domain.omnet.event.AnomalousEvent;
import br.ufrgs.inf.bdinetr.domain.omnet.event.OMNeTEvent;
import br.ufrgs.inf.bdinetr.domain.omnet.event.OverUsageEvent;
import br.ufrgs.inf.bdinetr.domain.omnet.event.ThreatEvent;

/**
 * @author Alberto Egon and Ingrid Nunes
 */
public class EventBroker extends Observable {

	private class BrokerThread extends Thread {

		Socket eventSource = null;

		public BrokerThread(Socket s) {
			eventSource = s;
		}

		public void run() {
			try {
				// read the event via the socket
				BufferedReader in = new BufferedReader(new InputStreamReader(
						eventSource.getInputStream()));
				String inputLine = in.readLine();
				log.info("Read event from socket = " + inputLine);

				OMNeTEvent event = null;
				if (inputLine.startsWith(OverUsageEvent.NAME)) {
					event = new OverUsageEvent(inputLine);
				} else if (inputLine.startsWith(AnomalousEvent.NAME)) {
					event = new AnomalousEvent(inputLine);
				} else if (inputLine.startsWith(ThreatEvent.NAME)) {
					event = new ThreatEvent(inputLine);
				}

				// notify observers
				if (event != null) {
					setChanged();
					notifyObservers(event);
				}

				// close stuff
				in.close();
				eventSource.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private static EventBroker INSTANCE;
	private static final Log log = LogFactory.getLog(EventBroker.class);
	private static final int SOCKET_PORT = 4444;

	public synchronized static EventBroker getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new EventBroker();
		}
		return INSTANCE;
	}

	private ServerSocket serverSocket = null;

	public EventBroker() {
		try {
			// creates the server socket
			serverSocket = new ServerSocket(SOCKET_PORT);
			log.info("EventBroker is ready...");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		// creates a thread that will keep the socket open and will spawn new
		// threads for handling each new connection
		(new Thread() {
			public void run() {
				while (true) {
					try {
						Socket eventSource = serverSocket.accept();
						// FIXME events order?
						new BrokerThread(eventSource).start();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		}).start();
	}

}
