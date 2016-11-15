package br.ufrgs.inf.bdinetr.palliative;

import bdi4jade.extension.palliative.NamedResource;
import bdi4jade.extension.palliative.Resource;

public interface Resources {

	public static final Resource TIME = new NamedResource("TIME");
	public static final Resource NETWORK_AVAILABILITY = new NamedResource("NETWORK_AVAILABILITY");
	public static final Resource VULNERABILITY = new NamedResource("VULNERABILITY");

	public static final Resource RESOURCES[] = { TIME, NETWORK_AVAILABILITY, VULNERABILITY };

}
