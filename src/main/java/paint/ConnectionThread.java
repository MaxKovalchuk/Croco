package paint;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionThread implements Runnable {
	NetworkConnection nc;
	Socket socket;
	ObjectOutputStream out;
	Thread thread;

	public ConnectionThread(NetworkConnection nc) {
		this.nc = nc;
		thread = new Thread(this);
	}

	/*
	 * try to loop server.accept()
	 * https://medium.com/@ssaurel/create-a-simple-http-web-server-in-java-
	 * 3fc12b29d5fd
	 * 
	 */
	@Override
	public void run() {
		try (ServerSocket server = nc.isServer() ? new ServerSocket(nc.getPort()) : null;
				Socket socket = nc.isServer() ? server.accept() : new Socket(nc.getIp(), nc.getPort());
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

			this.socket = socket;
			this.out = out;
			socket.setTcpNoDelay(true);

			while (true) {
				if (nc.isServer())
					server.accept();
				Serializable data = (Serializable) in.readObject();
				nc.onRecieveCallBack.accept(data);
			}

		} catch (Exception e) {
			nc.onRecieveCallBack.accept("Connection closed");
		}
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

}
