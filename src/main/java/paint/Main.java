/*TO DO:
 * 
 */
package paint;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class Main extends Application {
	Scene mainScene = new Scene(new GridPane());
	Button exit = new Button("Exit");
	Button back = new Button("Back");
	Button exitFromRoom = new Button("Leave");
	Music music;
	Button mute = new Button("Mute");

	GridPane servOrClientPane = new GridPane();
	HBox servOrClientBox = new HBox();
	Button serv = new Button("Create room");
	Button client = new Button("Connect");

	GridPane servPane = new GridPane();
	HBox servCreatingBox = new HBox();
	Label portLabel = new Label("Enter port:");
	TextField portInput = new TextField();
	Button servOK = new Button("OK");

	GridPane clientPane = new GridPane();
	HBox clientCreatingBox = new HBox();
	Label ipLabel = new Label("Enter IP:");
	TextField ipInput = new TextField();
	Button clientOK = new Button("OK");

	GridPane loginPane = new GridPane();
	HBox loginBox = new HBox();
	Label loginLabel = new Label();
	TextField loginInput = new TextField();
	String playerName;
	Button enterName = new Button("OK");

	GridPane paint = new GridPane();
	Canvas canvas = new Canvas(350, 500);
	HBox hbox = new HBox();
	TextField brushSize = new TextField("18");
	ColorPicker colorPicker = new ColorPicker();
	CheckBox eraser = new CheckBox("Eraser");
	Button clearAll = new Button("Clear all");
	VBox drawing = new VBox();
	GraphicsContext g = canvas.getGraphicsContext2D();

	TextArea messages = new TextArea();
	TextField input = new TextField();
	VBox chat = new VBox(10, messages, input);
	boolean isServer = false;
	NetworkConnection connection;
	String[] words = { "Snail", "Wolf", "House", "Pen", "Pepper" };
	String word = "";
	Label guess = new Label();
	Button nextword = new Button("OK");
	Label wordToDraw = new Label();
	VBox wordBox = new VBox();
	VBox guessBox = new VBox();
	Button startGame = new Button("Start Game !");

	GridPane serverVision = new GridPane();
	GridPane clientVision = new GridPane();

	public void gameStart() {
		wordBox.setVisible(false);
		drawing.setVisible(false);
		nextword.setVisible(false);
		startGame.setOnAction(event -> {
			try {
				connection.send("");
			} catch (Exception e) {
				System.out.println("Nah ah");
			}
			if (connection.isEnoughPlayers()) {
				wordBox.setVisible(true);
				drawing.setVisible(true);
				nextword.setVisible(true);
				startGame.setVisible(false);
				randWord();
				hideWord();
				wordToDraw.setText(isServer ? word : hideWord());
			} else {
				messages.appendText("Not enough players\n");
			}
		});
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			exit.setPrefSize(75, 30);
			exit.setOnAction(event -> primaryStage.close());
			back.setPrefSize(75, 30);
			back.setOnAction(event -> mainScene.setRoot(servOrClientPane));
			exitFromRoom.setOnAction(event -> {
				try {
					connection.closeConnection();
				} catch (Exception e) {
					e.printStackTrace();
				}
				mainScene.setRoot(servOrClientPane);
				music.setSong(music.mainMenuTheme);
			});
			mute.setOnAction(event -> {
				if(music.isPlaying()) {
					music.setPlaying(false);
				}else {
					music.setPlaying(true);
				}
			});

			mainScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setFullScreen(true);
			primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
			primaryStage.setResizable(false);
			primaryStage.setScene(mainScene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GridPane serverVisionPart() {
		try {
			music.setSong(music.inGameTheme);
			drawPart();
			chatPart();
			guessPart();
			gameStart();

			serverVision.setAlignment(Pos.CENTER);
			serverVision.add(guessBox, 3, 1);
			serverVision.add(wordBox, 3, 0);
			serverVision.add(drawing, 3, 1);
			serverVision.add(startGame, 3, 1);
			serverVision.add(chat, 4, 1);
			serverVision.add(exitFromRoom, 0, 1);
			serverVision.setMargin(exitFromRoom, new Insets(0, 50, 0, 0));
		} catch (IllegalArgumentException e) {
		}
		return serverVision;
	}

	public GridPane clientVisionPart() {
		try {
			music.setSong(music.inGameTheme);
			drawPart();
			chatPart();
			guessPart();
			drawing.setDisable(true);

			clientVision.setAlignment(Pos.CENTER);
			clientVision.add(guessBox, 1, 1);
			clientVision.add(wordBox, 1, 0);
			clientVision.add(drawing, 1, 1);
			clientVision.add(chat, 2, 1);
			clientVision.add(exitFromRoom, 0, 1);
			clientVision.setMargin(exitFromRoom, new Insets(0, 50, 0, 0));
		} catch (IllegalArgumentException e) {
		}
		return clientVision;
	}

	@Override
	public void init() {
		music = new Music();
		mainScene.setRoot(servOrClientPart());

		g.setFill(Color.WHITE);
		g.fillRect(0, 0, 350, 500);

		clearAll.setOnAction(e -> {
			g.setFill(Color.WHITE);
			g.fillRect(0, 0, 300, 400);
			try {
				connection.sendCanvas(canvas);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});

		canvas.setOnMouseDragged(e -> {
			double size = Double.parseDouble(brushSize.getText());
			double x = e.getX() - size / 2;
			double y = e.getY() - size / 2;

			if (eraser.isSelected()) {
				g.setFill(Color.WHITE);
				g.fillRect(x, y, size, size);
			} else {
				g.setFill(colorPicker.getValue());
				g.fillRect(x, y, size, size);
			}
			try {
				connection.sendCanvas(canvas);
			} catch (Exception e1) {
				messages.appendText("Failed to upload canvas\n");
			}
		});

		canvas.setOnMouseClicked(e -> {
			double size = Double.parseDouble(brushSize.getText());
			double x = e.getX() - size / 2;
			double y = e.getY() - size / 2;

			if (eraser.isSelected()) {
				g.setFill(Color.WHITE);
				g.fillRect(x, y, size, size);
			} else {
				g.setFill(colorPicker.getValue());
				g.fillRect(x, y, size, size);
			}
			try {
				connection.sendCanvas(canvas);
			} catch (Exception e1) {
				messages.appendText("Failed to upload canvas\n");
			}
		});

	}

	@Override
	public void stop() {
		try {
			connection.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void guessed() {
		guess.setText("The word is '" + word + "' !");
		g.setFill(Color.WHITE);
		g.fillRect(0, 0, 300, 400);
		guessBox.setVisible(true);
		wordBox.setVisible(false);
		drawing.setVisible(false);
	}

	public void drawPart() {
		hbox.setAlignment(Pos.CENTER);
		hbox.setSpacing(5);
		hbox.getChildren().add(brushSize);
		hbox.getChildren().add(colorPicker);
		hbox.getChildren().add(eraser);
		hbox.getChildren().add(clearAll);
		drawing.setSpacing(50);
		drawing.getChildren().add(canvas);
		drawing.getChildren().add(hbox);
		drawing.setAlignment(Pos.BOTTOM_CENTER);
	}

	public void chatPart() {
		messages.setEditable(false);
		messages.setPrefHeight(550);
		chat.setPrefSize(300, 600);
		chat.setAlignment(Pos.TOP_LEFT);
		input.setOnAction(event -> {
			String message = playerName + ": ";
			message += input.getText();
			messages.appendText(message + '\n');
			try {
				connection.send(message);
			} catch (Exception e) {
				messages.appendText("Failed to send\n");
			}
			if (input.getText().equalsIgnoreCase(word)) {
				guessed();
				messages.appendText("CROCO: " + playerName + " guessed !\n");
				try {
					connection.send("CROCO: " + playerName + " guessed !\n");
				} catch (Exception e) {
					messages.appendText("Failed to send\n");
				}
			}
			input.clear();
		});
	}

	public void guessPart() {
		startGame.setPrefSize(100, 50);
		nextword.setOnAction(event -> {
			if (isServer)
				randWord();
			canvas.setVisible(true);
			drawing.setVisible(true);
			wordBox.setVisible(true);
			guessBox.setVisible(false);
			wordToDraw.setText(isServer ? word : hideWord());
		});
		guess.setId("guess");
		wordToDraw.setId("wordToDraw");
		wordBox.getChildren().add(wordToDraw);
		wordBox.setAlignment(Pos.CENTER);
		guessBox.getChildren().add(guess);
		guessBox.getChildren().add(nextword);
		guessBox.setAlignment(Pos.CENTER);
	}

	public GridPane loginPart() {
		try {
			VBox loginInBox = new VBox();
			loginInBox.getChildren().add(loginLabel);
			loginInBox.getChildren().add(loginInput);
			loginInBox.getChildren().add(enterName);
			loginInBox.setAlignment(Pos.CENTER);
			loginInBox.setSpacing(10);
			loginBox.getChildren().add(loginInBox);
			loginBox.setAlignment(Pos.CENTER_RIGHT);
			loginLabel.setId("inputLabel");
			loginLabel.setText("Enter your name: ");
			loginInput.setOnAction(event -> {
				if (loginInput.getText().contains(":") || loginInput.getText().contains("@")
						|| loginInput.getText().contains("!") || loginInput.getText().contains(",")
						|| loginInput.getText().contains(".") || loginInput.getText().contains(" ")) {
					loginInput.clear();
					loginLabel.setText("Enter your name: \n(No spaces or special symbols allowed: ! , . @ :)");
				} else if (loginInput.getText().equals("")) {
				} else {
					playerName = loginInput.getText();
					if (isServer) {
						mainScene.setRoot(serverVisionPart());
					} else {
						mainScene.setRoot(clientVisionPart());
						messages.appendText("CROCO: " + playerName + " has joined the game !\n");
						try {
							connection.send("CROCO: " + playerName + " has joined the game !\n");
						} catch (Exception e) {
							messages.appendText("Failed to send");
						}
					}
				}
			});
			enterName.setOnAction(loginInput.getOnAction());
			enterName.setPrefSize(50, 50);

			loginPane.setAlignment(Pos.CENTER);
			loginPane.add(loginBox, 0, 0);
		} catch (IllegalArgumentException e) {
		}
		return loginPane;
	}

	public GridPane servOrClientPart() {
		try {
			VBox sORcIn = new VBox();
			serv.setPrefSize(100, 50);
			client.setPrefSize(100, 50);
			sORcIn.getChildren().add(serv);
			sORcIn.getChildren().add(client);
			sORcIn.getChildren().add(mute);
			sORcIn.getChildren().add(exit);
			sORcIn.setAlignment(Pos.CENTER);
			sORcIn.setSpacing(10);
			servOrClientBox.getChildren().add(sORcIn);
			servOrClientBox.setAlignment(Pos.CENTER);
			serv.setOnAction(event -> {
				mainScene.setRoot(creatingServPart());
			});
			client.setOnAction(event -> {
				mainScene.setRoot(creatingClientPart());
			});

			servOrClientPane.setAlignment(Pos.CENTER);
			servOrClientPane.add(servOrClientBox, 0, 0);
		} catch (IllegalArgumentException e) {
		}
		return servOrClientPane;
	}

	public GridPane creatingServPart() {
		try {
			portLabel.setId("inputLabel");
			servOK.setPrefSize(100, 50);
			VBox servCreatingIn = new VBox();
			servCreatingIn.getChildren().add(portLabel);
			servCreatingIn.getChildren().add(portInput);
			servCreatingIn.getChildren().add(servOK);
			servCreatingIn.getChildren().add(back);
			servCreatingIn.setSpacing(10);
			servCreatingIn.setAlignment(Pos.CENTER);
			servCreatingBox.getChildren().add(servCreatingIn);
			servCreatingBox.setAlignment(Pos.CENTER);
			servOK.setOnAction(event -> {
				int port;
				try {
					if (portInput.getText().length() == 5) {
						port = Integer.parseInt(portInput.getText());
						connection = createServer(port);
						isServer = true;
						mainScene.setRoot(loginPart());
						try {
							connection.startConnection();
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					} else {
						portLabel.setText("Enter correct port value:");
					}
				} catch (NumberFormatException e) {
					portLabel.setText("Enter correct port value:");
				}
			});
			portInput.setOnAction(servOK.getOnAction());

			servPane.setAlignment(Pos.CENTER);
			servPane.add(servCreatingBox, 0, 0);
		} catch (IllegalArgumentException e) {
		}
		return servPane;
	}

	public GridPane creatingClientPart() {
		try {
			VBox clientCreatingIn = new VBox();
			clientOK.setPrefSize(100, 50);
			ipLabel.setId("inputLabel");
			portLabel.setId("inputLabel");
			clientCreatingIn.getChildren().add(ipLabel);
			clientCreatingIn.getChildren().add(ipInput);
			clientCreatingIn.getChildren().add(portLabel);
			clientCreatingIn.getChildren().add(portInput);
			clientCreatingIn.getChildren().add(clientOK);
			clientCreatingIn.getChildren().add(back);
			clientCreatingIn.setSpacing(10);
			clientCreatingIn.setAlignment(Pos.CENTER);
			clientCreatingBox.getChildren().add(clientCreatingIn);
			clientCreatingBox.setAlignment(Pos.CENTER);
			clientOK.setOnAction(event -> {
				int port;
				try {
					if (portInput.getText().length() == 5) {
						port = Integer.parseInt(portInput.getText());
						connection = createClient(ipInput.getText(), port);
						isServer = false;
						mainScene.setRoot(loginPart());
						try {
							connection.startConnection();
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					} else {
						portLabel.setText("Enter correct port value:");
					}
				} catch (NumberFormatException e) {
					portLabel.setText("Enter correct port value:");
				}
			});
			portInput.setOnAction(clientOK.getOnAction());

			clientPane.setAlignment(Pos.CENTER);
			clientPane.add(clientCreatingBox, 0, 0);
		} catch (IllegalArgumentException e) {
		}
		return clientPane;
	}

	public Server createServer(int port) {
		return new Server(port, data -> {
			Platform.runLater(() -> {
				if (data.toString().indexOf(":") == (data.toString().indexOf(" ") - 1)
						|| data.toString().equals("Connection closed")) {
					messages.appendText(data.toString() + '\n');
					if (data.toString().substring(data.toString().indexOf(" ") + 1).equalsIgnoreCase(word)) {
						guessed();
					}
				}
			});
		});
	}

	public Client createClient(String ip, int port) {
		return new Client(ip, port, data -> {
			Platform.runLater(() -> {
				if (data.toString().indexOf(":") == (data.toString().indexOf(" ") - 1)
						|| data.toString().equals("Connection closed")) {
					messages.appendText(data.toString() + '\n');
					if (data.toString().substring(data.toString().indexOf(" ") + 1).equalsIgnoreCase(word)) {
						guessed();
					}
				} else if (!data.toString().equals("")) {
					try {
						word = words[Integer.parseInt(data.toString())];
						wordToDraw.setText(hideWord());
					} catch (NumberFormatException numex) {
						ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) data);
						try {
							BufferedImage bimg = ImageIO.read(bais);
							WritableImage wimg = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
							wimg = SwingFXUtils.toFXImage(bimg, wimg);
							g.drawImage(wimg, 0, 0);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
		});
	}

	public String hideWord() {
		String hide = "";
		for (int i = 0; i < word.length(); i++) {
			hide += " _";
		}
		return hide;
	}

	public void randWord() {
		int r = 0 + (int) (Math.random() * words.length);
		word = words[r];
		try {
			connection.send(r);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
