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

	private Ip dstIp;
	private int dstPort;
	private String protocol;
	private Ip srcIp;
	private int srcPort;

	public Flow(Ip srcIp, int srcPort, Ip dstIp, int dstPort, String protocol) {
		this.srcIp = srcIp;
		this.srcPort = srcPort;
		this.dstIp = dstIp;
		this.dstPort = dstPort;
		this.protocol = protocol;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Flow))
			return false;
		Flow other = (Flow) obj;
		if (dstIp == null) {
			if (other.dstIp != null)
				return false;
		} else if (!dstIp.equals(other.dstIp))
			return false;
		if (dstPort != other.dstPort)
			return false;
		if (protocol == null) {
			if (other.protocol != null)
				return false;
		} else if (!protocol.equals(other.protocol))
			return false;
		if (srcIp == null) {
			if (other.srcIp != null)
				return false;
		} else if (!srcIp.equals(other.srcIp))
			return false;
		if (srcPort != other.srcPort)
			return false;
		return true;
	}

	public Ip getDstIp() {
		return dstIp;
	}

	public int getDstPort() {
		return dstPort;
	}

	public String getProtocol() {
		return protocol;
	}

	public Ip getSrcIp() {
		return srcIp;
	}

	public int getSrcPort() {
		return srcPort;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dstIp == null) ? 0 : dstIp.hashCode());
		result = prime * result + dstPort;
		result = prime * result
				+ ((protocol == null) ? 0 : protocol.hashCode());
		result = prime * result + ((srcIp == null) ? 0 : srcIp.hashCode());
		result = prime * result + srcPort;
		return result;
	}

	public void setDstIp(Ip dstIp) {
		this.dstIp = dstIp;
	}

	public void setDstPort(int dstPort) {
		this.dstPort = dstPort;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setSrcIp(Ip srcIp) {
		this.srcIp = srcIp;
	}

	public void setSrcPort(int srcPort) {
		this.srcPort = srcPort;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<src=").append(srcIp).append(":").append(srcPort);
		sb.append(", dst=").append(dstIp).append(":").append(dstPort);
		sb.append(", protocol=").append(protocol).append(">");
		return sb.toString();
	}

}
