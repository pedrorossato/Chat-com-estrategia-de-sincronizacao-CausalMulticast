package causalMulticast;

import client.Client;

public interface ICausalMulticastAPI {
	void mcsend (String msg, Client client);
}
