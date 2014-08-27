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
package br.ufrgs.inf.bdinetr.domain;

/**
 * @author Ingrid Nunes
 */
public class Flow {

	private static long id_counter = 0;

	private IpAddress dstIp;
	private int dstPort;
	private final Long id;
	private String protocol;
	private IpAddress srcIp;
	private int srcPort;

	public Flow(IpAddress srcIp, int srcPort, IpAddress dstIp, int dstPort,
			String protocol) {
		this(++id_counter);
	}

	public Flow(Long id) {
		this.id = id;
	}

	public Flow(Long id, IpAddress srcIp, int srcPort, IpAddress dstIp,
			int dstPort, String protocol) {
		this.id = id;
		this.srcIp = srcIp;
		this.srcPort = srcPort;
		this.dstIp = dstIp;
		this.dstPort = dstPort;
		this.protocol = protocol;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Flow) {
			Flow f = (Flow) obj;
			return this.id.equals(f.id);
		}
		return false;
	}

	public IpAddress getDstIp() {
		return dstIp;
	}

	public int getDstPort() {
		return dstPort;
	}

	public Long getId() {
		return id;
	}

	public String getProtocol() {
		return protocol;
	}

	public IpAddress getSrcIp() {
		return srcIp;
	}

	public int getSrcPort() {
		return srcPort;
	}

	@Override
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}

	public void setDstIp(IpAddress dstIp) {
		this.dstIp = dstIp;
	}

	public void setDstPort(int dstPort) {
		this.dstPort = dstPort;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setSrcIp(IpAddress srcIp) {
		this.srcIp = srcIp;
	}

	public void setSrcPort(int srcPort) {
		this.srcPort = srcPort;
	}

	@Override
	public String toString() {
		return id.toString();
	}

}
