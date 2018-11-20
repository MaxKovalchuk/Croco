package paint;

import java.io.Serializable;
import java.util.function.Consumer;

public class Server extends NetworkConnection {
	private int port;
	
	public Server(int port, Consumer<Serializable> onRecieveCallBack) {
		super(onRecieveCallBack);
		this.port = port;
	}

	@Override
	protected boolean isServer() {
		return true;
	}

	@Override
	protected String getIp() {
		return null;
	}

	@Override
	protected int getPort() {
		return port;
	}
	

}
