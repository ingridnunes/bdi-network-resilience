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
package br.ufrgs.inf.bdinetr.domain.logic;

import jade.content.Concept;
import jade.content.ContentElement;
import br.ufrgs.inf.bdinetr.domain.Ip;
import br.ufrgs.inf.bdinetr.domain.Link;

/**
 * @author Ingrid Nunes
 */
public class IpPreposition implements ContentElement, Concept {

	public static class Anomalous extends IpPreposition {
		public Anomalous(Ip ip) {
			super(ip);
		}
	}

	public static class Benign extends IpPreposition {
		public Benign(Ip ip) {
			super(ip);
		}
	}

	public static class OverUsageCause extends IpPreposition {
		private Link link;

		public OverUsageCause(Ip ip, Link link) {
			super(ip);
			this.link = link;
		}

		@Override
		public boolean equals(Object obj) {
			if (!super.equals(obj))
				return false;
			if (obj != null && this.getClass().equals(obj.getClass())) {
				OverUsageCause lp = (OverUsageCause) obj;
				return this.link.equals(lp.link);
			}
			return false;
		}

		public Link getLink() {
			return link;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((link == null) ? 0 : link.hashCode());
			return result;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(this.getClass().getSimpleName());
			sb.append("(").append(ip).append(",").append(link).append(")");
			return sb.toString();
		}

	}

	public static class RateLimited extends IpPreposition {
		public RateLimited(Ip ip) {
			super(ip);
		}
	}

	public static class Restricted extends IpPreposition {
		public Restricted(Ip ip) {
			super(ip);
		}
	}

	protected Ip ip;

	public IpPreposition(Ip ip) {
		this.ip = ip;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && this.getClass().equals(obj.getClass())) {
			IpPreposition lp = (IpPreposition) obj;
			return this.ip.equals(lp.ip);
		}
		return false;
	}

	public Ip getIp() {
		return ip;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.getClass() == null) ? 0 : this.getClass().hashCode());
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		return result;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getClass().getSimpleName());
		sb.append("(").append(ip).append(")");
		return sb.toString();
	}

}
