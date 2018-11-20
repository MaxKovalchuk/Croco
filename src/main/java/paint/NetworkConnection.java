package paint;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;

public abstract class NetworkConnection {
	protected Consumer<Serializable> onRecieveCallBack;
	private ConnectionThread conThread = new ConnectionThread(this);
	private boolean enoughPlayers = false;

	public NetworkConnection(Consumer<Serializable> onRecieveCallBack) {
		this.onRecieveCallBack = onRecieveCallBack;
		conThread.getThread().setDaemon(true);
	}

	public void startConnection() throws Exception {
		conThread.getThread().start();
	};

	public void send(Serializable data) throws Exception {
		try {
			conThread.out.writeObject(data);
			enoughPlayers = true;
		} catch (NullPointerException e) {
			enoughPlayers = false;
		}
	};

	public void sendCanvas(Canvas canvas) throws Exception {
		WritableImage img = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
		canvas.snapshot(null, img);
		BufferedImage bimg = new BufferedImage(300, 400, BufferedImage.TYPE_3BYTE_BGR);
		bimg = SwingFXUtils.fromFXImage(img, bimg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bimg, "png", baos);
		conThread.out.writeObject(baos.toByteArray());
	};

	public void closeConnection() throws Exception {
		try{
			conThread.socket.close();
		}catch(NullPointerException e) {
			
		}
	};

	public boolean isEnoughPlayers() {
		return enoughPlayers;
	}

	protected abstract boolean isServer();

	protected abstract String getIp();

	protected abstract int getPort();
}
