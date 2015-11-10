package com.gmail.jackbcousineau;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;

import com.gmail.jackbcousineau.Dots.MainMenuFrame.PlayerType;
import com.gmail.jackbcousineau.MatchClient.ClientThread;
import com.gmail.jackbcousineau.MatchServer.SocketServer.OneConnection;

public class Match {

	public static Image dot;
	public static Image leftArrow;
	public static Image rightArrow;
	public static Image buckets;

	private static File getSoundResource(String string){
		return new File(Dots.class.getResource("/com/gmail/jackbcousineau/resources/sounds/" + string).getFile());
	}

	public static synchronized void playSound(final int number) {
		new Thread(new Runnable() {
			// The wrapper thread is unnecessary, unless it blocks on the
			// Clip finishing; see comments.
			public void run() {
				File path = null;
				int soundToPlay = number;
				System.out.println(soundToPlay);
				if(soundToPlay == 1){
					path = getSoundResource("man_laughing_1.wav");
				}
				else if(soundToPlay == 2){
					path = getSoundResource("man_laughing_2.wav");
				}
				else if(soundToPlay == 3){
					path = getSoundResource("man_laughing_3.wav");
				}
				else if(soundToPlay == 4){
					path = getSoundResource("man_giggling.wav");
				}
				else if(soundToPlay == 5){
					path = getSoundResource("bus_driver_restroom.wav");
				}
				else if(soundToPlay == 6){
					path = getSoundResource("bus_driver_destination.wav");
				}
				else if(soundToPlay == 7){
					path = getSoundResource("bus_driver_next_stop.wav");
				}
				try{
					AudioInputStream stream;
					AudioFormat format;
					DataLine.Info info;
					Clip clip;

					stream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(path)));
					format = stream.getFormat();
					info = new DataLine.Info(Clip.class, format);
					clip = (Clip) AudioSystem.getLine(info);
					clip.open(stream);
					clip.start();
				}
				//catch(UnsupportedAudioFileException | IOException | LineUnavailableException e){
				catch(Exception e){
				}
			}
		}).start();
	}

	public enum MatchType{
		PVP,
		PVC,
		CVC,
		SERVER,
		CLIENT;
	}

	public MatchFrame gameFrame;
	public MatchType matchType;
	public int rows, columns, totalMoves;
	public int currentPlayerMove = 1;
	public String player1Name, player2Name;
	public Color player1Color, player2Color;
	public PlayerType player1Type, player2Type;
	public HashMap<String, String> occupiedPoints = new HashMap<String, String>();
	public OneConnection matchServer;
	public ClientThread matchClient;

	public void talk(Point point, String direction){
		String move = "move," + point.x + "," + point.y + "," + direction;
		//System.out.println("Should be sending: " + move);
		if(matchServer != null){
			//System.out.println("SERVER SENDING: " + move);
			matchServer.sendMessage(move);
		}
		else if(matchClient != null){
			//System.out.println("CLIENT SENDING: " + move);
			matchClient.sendMessage(move);
		}
	}

	private String fromPoint(Point point){
		return point.x + "," + point.y;
	}

	private Point fromString(String string){
		String[] stringSplit = string.split(",");
		return new Point(Integer.parseInt(stringSplit[0]), Integer.parseInt(stringSplit[1]));
	}

	public Match(MatchType matchType, int rows, int columns, String player1Name, Color player1Color, String player2Name, Color player2Color, Object onlineTalker){
		System.out.println("NEW GAME");
		this.matchType = matchType;
		this.rows = rows;
		this.columns = columns;
		this.totalMoves = (((rows-1)*(columns-1))*2)+rows+columns-2;
		this.player1Name = player1Name;
		this.player2Name = player2Name;
		if(matchType == MatchType.PVP||matchType == MatchType.SERVER||matchType == MatchType.CLIENT){
			player1Type = PlayerType.PLAYER;
			player2Type = PlayerType.PLAYER;
		}
		else if(matchType == MatchType.PVC){
			if(player1Name.equals("AI")){
				player1Type = PlayerType.AI;
				player2Type = PlayerType.PLAYER;
			}
			else{
				player1Type = PlayerType.PLAYER;
				player2Type = PlayerType.AI;
			}
		}
		else if(matchType == MatchType.CVC){
			player1Type = PlayerType.AI;
			player2Type = PlayerType.AI;
		}
		this.player1Color = player1Color;
		this.player2Color = player2Color;

		if(onlineTalker instanceof OneConnection) matchServer = (OneConnection)onlineTalker;
		else if(onlineTalker instanceof ClientThread) matchClient = (ClientThread)onlineTalker;

		gameFrame = new MatchFrame(rows, columns);
	}

	public class MatchFrame extends JFrame{

		/**
		 * 
		 */
		public static final long serialVersionUID = 5366993223499445680L;
		public boolean windowOpen = true;
		public JPanel pane = new JPanel();
		public SpringLayout layout = new SpringLayout();
		//public BorderLayout layout = new BorderLayout();
		public BoardPane boardPane = new BoardPane(rows, columns);
		public ScorePane scorePane = new ScorePane();

		public MatchFrame(int rows, int columns){
			pane.setLayout(layout);
			setMinimumSize(new Dimension((rows*50) + 69, (columns*50) + 102));
			getContentPane().add(pane);
			pane.add(boardPane);
			pane.add(scorePane);
			setLocationRelativeTo(null);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			layout.putConstraint(SpringLayout.NORTH, boardPane, 15, SpringLayout.NORTH, pane);
			layout.putConstraint(SpringLayout.WEST, boardPane, 28, SpringLayout.WEST, pane);

			layout.putConstraint(SpringLayout.WEST, scorePane, 0, SpringLayout.WEST, pane);
			layout.putConstraint(SpringLayout.NORTH, scorePane, 0, SpringLayout.NORTH, pane);
			layout.putConstraint(SpringLayout.EAST, scorePane, 0, SpringLayout.EAST, pane);
			layout.putConstraint(SpringLayout.SOUTH, scorePane, 0, SpringLayout.SOUTH, pane);
			//layout.putConstraint(SpringLayout.EAST, boardPane, -31, SpringLayout.EAST, pane);
			//layout.putConstraint(SpringLayout.SOUTH, boardPane, -58, SpringLayout.SOUTH, pane);

			//layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, boardPane, 0, SpringLayout.HORIZONTAL_CENTER, pane);
			//layout.putConstraint(SpringLayout.VERTICAL_CENTER, boardPane, 0, SpringLayout.VERTICAL_CENTER, pane);
			//layout.putConstraint(SpringLayout.NORTH, boardPane, 20, SpringLayout.NORTH, pane);
			//layout.putConstraint(SpringLayout.SOUTH, boardPane, -20, SpringLayout.SOUTH, pane);
			//layout.putConstraint(SpringLayout.EAST, boardPane, -20, SpringLayout.EAST, pane);
			//layout.putConstraint(SpringLayout.WEST, boardPane, 20, SpringLayout.WEST, pane);

			setResizable(false);
			setVisible(true);
			if(player1Type == PlayerType.AI) scorePane.aiMove(1, null, null, true);

			//JOptionPane.showMessageDialog(null, (((rows-1)*(columns-1))*2)+rows+columns-2);

			this.addWindowListener(new WindowListener(){

				@Override
				public void windowOpened(WindowEvent e) {
				}

				@Override
				public void windowClosing(WindowEvent e) {
				}

				@Override
				public void windowClosed(WindowEvent e) {
					windowOpen = false;
				}

				@Override
				public void windowIconified(WindowEvent e) {
				}

				@Override
				public void windowDeiconified(WindowEvent e) {
				}

				@Override
				public void windowActivated(WindowEvent e) {
				}

				@Override
				public void windowDeactivated(WindowEvent e) {
				}
			});
		}

		class ScorePane extends JPanel{

			/**
			 * 
			 */
			public static final long serialVersionUID = 8352430738151113147L;
			public PlayerPane player1Box, player2Box;
			public JLabel turnLabel, turnText, player1ScoreLabel, player2ScoreLabel, player1ScoreNumber, player2ScoreNumber;
			public SpringLayout layout = new SpringLayout();

			public int player1Score, player2Score;
			public int player1Moves, player2Moves;

			public ScorePane(){
				setMinimumSize(new Dimension((rows*50) + 69, (columns*50) + 102));
				//setBackground(Color.BLUE);
				setLayout(layout);


				player1Box = new PlayerPane(1);
				player2Box = new PlayerPane(2);

				turnLabel = new JLabel(new ImageIcon(leftArrow));
				turnText = new JLabel("turn");
				turnText.setFont(new Font("Lucia Grande", Font.PLAIN, 10));

				player1ScoreLabel = new JLabel("score");
				player1ScoreLabel.setFont(new Font("Lucia Grande", Font.PLAIN, 12));
				player1ScoreLabel.setForeground(Color.GRAY);
				player2ScoreLabel = new JLabel("score");
				player2ScoreLabel.setFont(new Font("Lucia Grande", Font.PLAIN, 12));
				player2ScoreLabel.setForeground(Color.GRAY);

				player1Score = 0;
				player2Score = 0;
				player1Moves = 0;
				player2Moves = 0;

				player1ScoreNumber = new JLabel(String.valueOf(player1Score));
				player1ScoreNumber.setFont(new Font("Lucia Grande", Font.PLAIN, 18));
				player2ScoreNumber = new JLabel(String.valueOf(player2Score));
				player2ScoreNumber.setFont(new Font("Lucia Grande", Font.PLAIN, 18));
				//turnText.setText(turnText.getFont().getName());

				player1Box.setBorder(BorderFactory.createEtchedBorder());
				player2Box.setBorder(BorderFactory.createEtchedBorder());

				add(player1Box);
				add(player2Box);
				add(turnLabel);
				add(turnText);
				add(player1ScoreLabel);
				add(player2ScoreLabel);
				add(player1ScoreNumber);
				add(player2ScoreNumber);

				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, turnLabel, 0, SpringLayout.HORIZONTAL_CENTER, this);
				layout.putConstraint(SpringLayout.SOUTH, turnLabel, -26, SpringLayout.SOUTH, this);
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, turnText, 0, SpringLayout.HORIZONTAL_CENTER, this);
				layout.putConstraint(SpringLayout.SOUTH,  turnText,  -11,  SpringLayout.SOUTH, this);
				layout.putConstraint(SpringLayout.WEST, player1ScoreLabel, 8, SpringLayout.WEST, this);
				layout.putConstraint(SpringLayout.SOUTH, player1ScoreLabel, -10, SpringLayout.SOUTH, this);
				layout.putConstraint(SpringLayout.EAST, player2ScoreLabel, -8, SpringLayout.EAST, this);
				layout.putConstraint(SpringLayout.SOUTH, player2ScoreLabel, -10, SpringLayout.SOUTH, this);
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, player1ScoreNumber, 0, SpringLayout.HORIZONTAL_CENTER, player1ScoreLabel);
				layout.putConstraint(SpringLayout.VERTICAL_CENTER, player1ScoreNumber, -17, SpringLayout.VERTICAL_CENTER, player1ScoreLabel);
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, player2ScoreNumber, 0, SpringLayout.HORIZONTAL_CENTER, player2ScoreLabel);
				layout.putConstraint(SpringLayout.VERTICAL_CENTER, player2ScoreNumber, -17, SpringLayout.VERTICAL_CENTER, player2ScoreLabel);

				layout.putConstraint(SpringLayout.WEST, player1Box, 45, SpringLayout.WEST, this);
				layout.putConstraint(SpringLayout.EAST, player1Box, 100, SpringLayout.WEST, player1Box);
				layout.putConstraint(SpringLayout.NORTH, player1Box, 20, SpringLayout.SOUTH, boardPane);
				layout.putConstraint(SpringLayout.SOUTH, player1Box, -8, SpringLayout.SOUTH, this);

				layout.putConstraint(SpringLayout.EAST, player2Box, -45, SpringLayout.EAST, this);
				layout.putConstraint(SpringLayout.WEST, player2Box, -100, SpringLayout.EAST, player2Box);
				layout.putConstraint(SpringLayout.NORTH, player2Box, 20, SpringLayout.SOUTH, boardPane);
				layout.putConstraint(SpringLayout.SOUTH, player2Box, -8, SpringLayout.SOUTH, this);
			}

			public void makeMove(int number, Point point, String direction, boolean passInfo){
				if(!windowOpen) return;
				int aiToMove = 0;
				int pointsScored = ifBoxMade(point, direction, number, false);
				if(number == 1){
					player1Moves++;
					player1Box.movesNumber.setText(String.valueOf(player1Moves));
					boardPane.player1Pane.drawLine(point, direction);
					if(pointsScored == 0){
						//System.out.println("Do not move again");
						turnLabel.setIcon(new ImageIcon(rightArrow));
						currentPlayerMove = 2;
						if(player2Type == PlayerType.AI){
							//JOptionPane.showMessageDialog(null, "AI Move");
							aiToMove = 2;
						}
					}
					else{
						//System.out.println("Move again");
						player1Score = player1Score + pointsScored;
						player1ScoreNumber.setText(String.valueOf(player1Score));
						if(player1Type == PlayerType.AI){
							aiToMove = 1;
						}
					}
					boardPane.player1Pane.updateUI();
				}
				else{
					player2Moves++;
					player2Box.movesNumber.setText(String.valueOf(player2Moves));
					boardPane.player2Pane.drawLine(point, direction);
					if(pointsScored == 0){
						turnLabel.setIcon(new ImageIcon(leftArrow));
						currentPlayerMove = 1;
						if(player1Type == PlayerType.AI){
							aiToMove = 1;
						}
					}
					else{
						player2Score = player2Score + pointsScored;
						player2ScoreNumber.setText(String.valueOf(player2Score));
						if(player2Type == PlayerType.AI){
							aiToMove = 2;
						}
					}
					boardPane.player2Pane.updateUI();
				}
				if(player1Moves + player2Moves == totalMoves){
					if(matchType != MatchType.CLIENT){
						int soundToPlay = (int)(Math.random()*7)+1;
						playSound(soundToPlay);
						if(matchType == MatchType.SERVER) matchClient.sendMessage("sound:" + soundToPlay);
					}
					String winner = null;
					if(player1Score > player2Score) winner = player1Name + " gets buckets";
					else if(player1Score < player2Score) winner = player2Name + " gets buckets";
					else if(player1Score == player2Score) winner = "You're more tied than Thailand";
					///if(player1Score > player2Score) winner = player1Name + " wins!";
					///else if(player1Score < player2Score) winner = player2Name + " wins!";
					///else if(player1Score == player2Score) winner = "You're more tied than Thailand";
					//JOptionPane.showMessageDialog(gameFrame, "Game over!\n" + winner, "9/11 was a consipracy", JOptionPane.INFORMATION_MESSAGE);
					JOptionPane.showMessageDialog(gameFrame, "Game over!\n" + winner, "9/11 was a conspiracy", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(buckets));
					///JOptionPane.showMessageDialog(gameFrame, "Game over!\n" + winner + "\n\nHave some buckets", "Game over!", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(buckets));
					/*JFrame frame = new JFrame();
					frame.setTitle("Game over!");
					frame.setSize(175, 200);
					JPanel framePanel = new JPanel();
					//framePanel.add(new JLabel("Game over!"));
					framePanel.add(new JLabel(winner));
					frame.getContentPane().add(framePanel);
					frame.setLocationRelativeTo(null);
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setResizable(false);
					frame.setAlwaysOnTop(true);
					frame.setVisible(true);
					 */					currentPlayerMove = 0;
				}
				else{
					if(!passInfo){
						point = null;
						direction = null;
						System.out.println("Not passing data");
					}
					else System.out.println("Passing data");
					if(aiToMove == 2) aiMove(2, point, direction, passInfo);
					else if(aiToMove == 1) aiMove(1, point, direction, passInfo);
				}
			}

			public int ifBoxMade(Point point, String direction, int number, boolean test){
				Point[][] points = boardPane.gridPane.points;
				int x = point.x/50;
				int y = point.y/50;
				int pointsScored = 0;
				if(direction.equals("vertical")){
					Point leftTop, leftBottom, middleBottom, rightTop;
					try{
						leftTop = points[x-1][y];
						leftBottom = points[x-1][y+1];
						if(occupiedPoints.containsKey(fromPoint(leftTop))){
							//System.out.println("left top exists");
							if(occupiedPoints.get(fromPoint(leftTop)).equals("both")){
								//System.out.println("left top is both");
								if(occupiedPoints.containsKey(fromPoint(leftBottom))){
									//System.out.println("left bottom exists");
									if(occupiedPoints.get(fromPoint(leftBottom)).equals("horizontal")||occupiedPoints.get(fromPoint(leftBottom)).equals("both")){
										//System.out.println("BOX MADE");
										//System.out.println("left bottom is valid");
										//JOptionPane.showMessageDialog(null, "Square made");
										pointsScored++;
										if(!test) makeBox(number, leftTop);
									}
								}
							}
						}
					}
					catch(ArrayIndexOutOfBoundsException e){
						//System.out.println("ERROR");
					}
					try{
						middleBottom = points[x][y+1];
						rightTop = points[x+1][y];
						if(occupiedPoints.containsKey(fromPoint(point))){
							if(occupiedPoints.get(fromPoint(point)).equals("horizontal")){
								if(occupiedPoints.containsKey(fromPoint(middleBottom))){
									if(occupiedPoints.get(fromPoint(middleBottom)).equals("horizontal")||occupiedPoints.get(fromPoint(middleBottom)).equals("both")){
										if(occupiedPoints.containsKey(fromPoint(rightTop))){
											if(occupiedPoints.get(fromPoint(rightTop)).equals("vertical")||occupiedPoints.get(fromPoint(rightTop)).equals("both")){
												//System.out.println("BOX MADE");
												pointsScored++;
												if(!test) makeBox(number, point);
											}
										}
									}
								}
							}
						}
					}
					catch(ArrayIndexOutOfBoundsException e){
					}
					//if(occupiedPoints.)
				}
				else if(direction.equals("horizontal")){
					Point topLeft, topRight, bottomLeft, middleRight;
					try{
						topLeft = points[x][y-1];
						topRight = points[x+1][y-1];
						if(occupiedPoints.containsKey(fromPoint(topLeft))){
							if(occupiedPoints.get(fromPoint(topLeft)).equals("both")){
								if(occupiedPoints.containsKey(fromPoint(topRight))){
									if(occupiedPoints.get(fromPoint(topRight)).equals("vertical")||occupiedPoints.get(fromPoint(topRight)).equals("both")){
										//System.out.println("BOX MADE");
										//JOptionPane.showMessageDialog(null, "Box made");
										pointsScored++;
										if(!test) makeBox(number, topLeft);
									}
								}
							}
						}
					}
					catch(ArrayIndexOutOfBoundsException e){
					}
					try{
						bottomLeft = points[x][y+1];
						middleRight = points[x+1][y];
						if(occupiedPoints.containsKey(fromPoint(point))){
							if(occupiedPoints.get(fromPoint(point)).equals("vertical")){
								if(occupiedPoints.containsKey(fromPoint(bottomLeft))){
									if(occupiedPoints.get(fromPoint(bottomLeft)).equals("horizontal")||occupiedPoints.get(fromPoint(bottomLeft)).equals("both")){
										if(occupiedPoints.containsKey(fromPoint(middleRight))){
											if(occupiedPoints.get(fromPoint(middleRight)).equals("vertical")||occupiedPoints.get(fromPoint(middleRight)).equals("both")){
												//System.out.println("BOX MADE");
												//JOptionPane.showMessageDialog(null, "Box made");
												pointsScored++;
												if(!test) makeBox(number, point);
											}
										}
									}
								}
							}
						}
					}
					catch(ArrayIndexOutOfBoundsException e){
					}
				}
				return pointsScored;
			}

			public void makeBox(int number, Point point){
				if(number == 1) boardPane.player1Pane.drawBox(point);
				else if(number == 2) boardPane.player2Pane.drawBox(point);
			}

			public void aiMove(int number, Point lastPoint, String lastDirection, boolean passInfo){
				//try {
				//Thread.sleep(1000);
				//} catch (InterruptedException e) {
				//e.printStackTrace();
				//}
				Timer timer = new Timer();
				timer.schedule(new AIMoveTask(number, lastPoint, lastDirection, timer, passInfo), 1000);
			}

			public void setPlayer2Move(){
				turnLabel.setIcon(new ImageIcon(rightArrow));
				currentPlayerMove = 2;
			}

			public class AIMoveTask extends TimerTask{

				public int number;
				public Point lastPoint;
				public String lastDirection;
				public boolean passInfo;
				public Timer timer;

				public AIMoveTask(int number, Point lastPoint, String lastDirection, Timer timer, boolean passInfo){
					this.number = number;
					this.lastPoint = lastPoint;
					this.lastDirection = lastDirection;
					this.passInfo = passInfo;
					this.timer = timer;
				}

				@Override
				public void run(){
					Point pointToMove = null;
					String directionToMove = null;
					if(lastPoint != null&&lastDirection != null){
						Point[][] points = boardPane.gridPane.points;
						try{
							int x = lastPoint.x/50;
							int y = lastPoint.y/50;
							if(lastDirection.equals("vertical")){
								//System.out.println("vertical");
								//System.out.println("detected");
								if(ifBoxMade(lastPoint, "horizontal", number, true) > 0){
									//System.out.println("detected vertical move at " + x + ", " + y);
									pointToMove = lastPoint;
								}
								else if(ifBoxMade(points[x][y+1], "horizontal", number, true) > 0){
									//System.out.println("detected vertical +1 move at " + x + ", " + (y+1));
									pointToMove = points[x][y+1];
								}
								else if(ifBoxMade(points[x-1][y+1], "horizontal", number, true) > 0){
									//System.out.println("detected vertical +1 and horizontal -1 move at " + (x-1) + ", " + (y+1));
									pointToMove = points[x-1][y+1];
								}
								else if(ifBoxMade(points[x-1][y], "horizontal", number, true) > 0){
									//System.out.println("detected horizontal -1 move at " + (x-1) + ", " + y);
									pointToMove = points[x-1][y];
								}
								directionToMove = "horizontal";
							}
							else{
								if(ifBoxMade(lastPoint, "vertical", number, true) > 0){
									//System.out.println("detected horizontal move at " + x + ", " + y);
									pointToMove = lastPoint;
								}
								else if(ifBoxMade(points[x+1][y], "vertical", number, true) > 0){
									//System.out.println("detected horizontal +1 move at " + (x+1) + ", " + y);
									pointToMove = points[x+1][y];
								}
								else if(ifBoxMade(points[x+1][y-1], "vertical", number, true) > 0){
									//System.out.println("detected vertical -1 and horizontal +1 move");
									pointToMove = points[x+1][y-1];
								}
								else if(ifBoxMade(points[x][y-1], "vertical", number, true) > 0){
									//System.out.println("detected vertical -1 move");
									pointToMove = points[x][y-1];
								}
								directionToMove = "vertical";
							}
							//}
						} catch(ArrayIndexOutOfBoundsException e){
						}
					}
					boolean passData = false;
					if(pointToMove == null){
						RandomLine randomLine = new RandomLine();
						pointToMove = randomLine.point;
						directionToMove = randomLine.direction;
						passData = true;
					}
					else System.out.println("making calculated move at " + pointToMove.x/50 + ", " + pointToMove.y/50 + ", " + directionToMove);
					makeMove(number, pointToMove, directionToMove, passData);
					timer.cancel();
				}
			}

			public class RandomLine{
				public Point point;
				public String direction;

				public RandomLine(){
					Point point = null;
					//int randomRow = (int)(Math.random() * rows);
					//int randomColumn = (int)(Math.random() * columns);
					//point = boardPane.gridPane.points[(int)number/rows][1];
					//JOptionPane.showMessageDialog(null, number + "\n" + number + "/" + rows + " = " + number/rows);
					//JOptionPane.showMessageDialog(null, point.y + ", " + point.x);
					String direction = null;
					while(direction == null){
						point = findPoint();
						direction = findDirection(point);
						Point secondPoint = null;
						if(direction == null){
							//JOptionPane.showMessageDialog(null, "Somehow, the point is null");
							continue;
						}
						if(direction.equals("horizontal")) secondPoint = new Point(point.x+50, point.y);
						else if(direction.equals("vertical")) secondPoint = new Point(point.x, point.y+50);
						//JOptionPane.showMessageDialog(null, secondPoint.x/50 + ", " + secondPoint.y/50);
						try{
							@SuppressWarnings("unused")
							Point wut = boardPane.gridPane.points[(secondPoint.x/50)][(secondPoint.y/50)];
						}
						catch(ArrayIndexOutOfBoundsException e){
							direction = null;
						}
						//else{
						//JOptionPane.showMessageDialog(null, "Invalid point");
						//}
						//if(direction.equals("both")){
						//if(occupiedPoints.get(point).equals("horizontal")) secondPoint = new Point(point.x+50, point.y);
						//}
						/*if(secondPoint.y/50 == columns||secondPoint.x/50 == rows){
							JOptionPane.showMessageDialog(null, (secondPoint.x/50)+1 + " out of " + rows + " columns\n" +
									(secondPoint.y/50)+1 + " out of " + columns + " rows\nrestarting");
							//if(new Random().nextBoolean() == true){
							JOptionPane.showMessageDialog(null, "Trying horizontal case");
							if(!occupiedPoints.containsKey(boardPane.gridPane.points[rows-1][columns])){
								point = boardPane.gridPane.points[rows-1][columns];
								direction = "horizontal";
								return;
							}
							else{
								JOptionPane.showMessageDialog(null, "Horizontal case taken");
								//}
								//else{
								JOptionPane.showMessageDialog(null, "Trying vertical case");
								if(!occupiedPoints.containsKey(boardPane.gridPane.points[rows][columns-1])){
									point = boardPane.gridPane.points[rows][columns-1];
									direction = "vertical";
									return;
								}
								else{
									JOptionPane.showMessageDialog(null, "Vertical case taken");
									direction = null;
								}
							}
							//direction = null;
						}
						 */}
					this.point = point;
					this.direction = direction;
					//JOptionPane.showMessageDialog(null, point.y/50 + ", " + point.x/50 + "\n" + direction);
				}

				public String findDirection(Point point){
					if(occupiedPoints.containsKey(fromPoint(point))){
						String direction = occupiedPoints.get(fromPoint(point));
						if(direction.equals("both")){
							return null;
						}
						else if(direction.equals("vertical")) return "horizontal";
						else if(direction.equals("horizontal")) return "vertical";
					}
					if(new Random().nextBoolean() == true){
						return "vertical";
					}
					else return "horizontal";
				}

				public Point findPoint(){
					//Random randomRow = new Random(rows);
					//Random randomColumn = new Random(columns);
					int randomRow = (int)(Math.random() * rows);
					int randomColumn = (int)(Math.random() * columns);
					return boardPane.gridPane.points[randomRow][randomColumn];
				}
			}

			public class PlayerPane extends JPanel{

				/**
				 * 
				 */
				public static final long serialVersionUID = -8351384481981766627L;
				int number;
				public JLabel movesLabel, movesNumber;

				public PlayerPane(int number){
					this.number = number;
					SpringLayout paneLayout = new SpringLayout();
					setLayout(paneLayout);
					JLabel name;
					movesLabel = new JLabel("moves");
					if(number == 1){
						name = new JLabel(player1Name);
						movesNumber = new JLabel(String.valueOf(player1Moves));

						paneLayout.putConstraint(SpringLayout.WEST, name, 18, SpringLayout.WEST, this);
						paneLayout.putConstraint(SpringLayout.EAST, name, -2, SpringLayout.EAST, this);
						paneLayout.putConstraint(SpringLayout.WEST, movesLabel, 19, SpringLayout.WEST, this);
						paneLayout.putConstraint(SpringLayout.SOUTH, movesLabel, -3, SpringLayout.SOUTH, this);
						paneLayout.putConstraint(SpringLayout.WEST, movesNumber, 5, SpringLayout.EAST, movesLabel);
					}
					else{
						name = new JLabel(player2Name, SwingConstants.RIGHT);
						movesNumber = new JLabel(String.valueOf(player2Moves));

						paneLayout.putConstraint(SpringLayout.EAST, name, -18, SpringLayout.EAST, this);
						paneLayout.putConstraint(SpringLayout.WEST, name, 2, SpringLayout.WEST, this);
						paneLayout.putConstraint(SpringLayout.EAST, movesLabel, -20, SpringLayout.EAST, this);
						paneLayout.putConstraint(SpringLayout.SOUTH, movesLabel, -3, SpringLayout.SOUTH, this);
						paneLayout.putConstraint(SpringLayout.EAST, movesNumber, -5, SpringLayout.WEST, movesLabel);
					}
					paneLayout.putConstraint(SpringLayout.VERTICAL_CENTER, movesNumber, 0, SpringLayout.VERTICAL_CENTER, movesLabel);

					add(name);
					add(movesLabel);
					add(movesNumber);
				}

				@Override
				protected void paintComponent(Graphics g){
					super.paintComponent(g);
					Graphics2D g2d = (Graphics2D)g;
					Ellipse2D.Double circle;
					if(number == 1){
						g2d.setColor(player1Color);
						circle = new Ellipse2D.Double(5, 5, 10, 10);
					}
					else{
						g2d.setColor(player2Color);
						circle = new Ellipse2D.Double(83, 5, 10, 10);
					}
					g2d.fill(circle);
				}
			}
		}

		public class BoardPane extends JPanel{

			/**
			 * 
			 */
			public static final long serialVersionUID = 9025760102867943138L;
			public GridPane gridPane = new GridPane();
			public PlayerPane player1Pane, player2Pane;

			public BoardPane(int rows, int columns){
				OverlayLayout overlay = new OverlayLayout(this);
				setLayout(overlay);
				setPreferredSize(new Dimension(rows*50+12, columns*50+12));
				setBorder(BorderFactory.createEtchedBorder());
				player1Pane = new PlayerPane(player1Color);
				player2Pane = new PlayerPane(player2Color);
				add(player1Pane);
				add(player2Pane);
				add(gridPane);
			}

			public class GridPane extends JPanel{

				/**
				 * 
				 */
				public static final long serialVersionUID = 1248077648930120575L;
				Point[][] points = new Point[rows][columns];
				public Graphics2D g2D;
				//DotLine[][] lines = new DotLine[]

				public GridPane(){
					setPreferredSize(new Dimension((rows*50), (columns*50)));
					//add(new LinePane());
					//String output = "";
					int rowsCounted = 0;
					int columnsCounted = 0;
					for (int row = 0; row < rows; row++) {
						for (int col = 0; col < columns; col++) {
							points[row][col] = new Point(rowsCounted*50, columnsCounted*50);
							columnsCounted++;
							//output += " | " + points[row][col].x + ", " + points[row][col].y;
						}
						columnsCounted = 0;
						rowsCounted++;
						//output += "\n";
					}
					//JOptionPane.showMessageDialog(null, output);
					addMouseListener(new MouseListener(){

						@Override
						public void mouseClicked(MouseEvent e) {
							if((currentPlayerMove == 1&&(player1Type == PlayerType.AI||matchType == MatchType.CLIENT))||
									(currentPlayerMove == 2&&(player2Type == PlayerType.AI||matchType == MatchType.SERVER)||currentPlayerMove == 0)){
								return;
							}
							Robot robot;
							try {
								robot = new Robot();
								Color color = robot.getPixelColor(e.getXOnScreen(), e.getYOnScreen());
								if(color.equals(new Color(232, 232, 232))){
									int point1 = e.getX()-25;
									int point2 = e.getY()-25;
									float point1ExactPoint = point1/50;
									float point2ExactPoint = point2/50;

									float point1Test = ((float)point1)/50;
									long point1Long = (long)point1Test;
									float finalPoint1 = point1Test-point1Long;
									float point2Test = ((float)point2)/50;
									long point2Long = (long)point2Test;
									float finalPoint2 = point2Test-point2Long;
									boolean point1valid = true;
									boolean point2valid = true;

									//String validString = "";
									if(finalPoint2 >= 0.06){
										point1valid = false;
									}
									if(finalPoint1 > 0.04){
										point2valid = false;
									}
									Point tempPoint = points[(int)point1ExactPoint][(int)point2ExactPoint];
									String pointString = fromPoint(tempPoint);
									boolean occupied = false;
									if(!point1valid&&!point2valid) return; //validString = "Invalid";//return;//JOptionPane.showMessageDialog(null, "Invalid altogether");
									else if(!point1valid){
										if(occupiedPoints.containsKey(pointString)){
											System.out.println("OCC POINTS CONTAINS");
											if(occupiedPoints.get(pointString).equals("vertical")||occupiedPoints.get(pointString).equals("both")){
												//validString = "Occupied";
												System.out.println("POINT IS OCCUPIED");
												occupied = true;
											}
										}
										if(!occupied){
											scorePane.makeMove(currentPlayerMove, tempPoint, "vertical", true);
											talk(tempPoint, "vertical");
											//validString = "Vertical";//JOptionPane.showMessageDialog(null, "Line is vertical");
										}
									}
									else{
										if(occupiedPoints.containsKey(pointString)){
											System.out.println("OCC POINTS CONTAINS");
											if(occupiedPoints.get(pointString).equals("horizontal")||occupiedPoints.get(pointString).equals("both")){
												//validString = "Occupied";
												System.out.println("POINT IS OCCUPIED");
												occupied = true;
											}
										}
										if(!occupied){
											scorePane.makeMove(currentPlayerMove, tempPoint, "horizontal", true);
											talk(tempPoint, "horizontal");
											//validString = "Horizontal";//JOptionPane.showMessageDialog(null, "Line is horizontal");
										}
									}
									//JOptionPane.showMessageDialog(null, e.getX()-25 + ", " + (e.getY()-25) + "\n" +
									//	point1 + "/50 = " + (point1ExactPoint+1) + "\n" +
									//	point2 + "/50 = " + (point2ExactPoint+1) + "\n" +
									//	finalPoint1 + "\n" +
									//	finalPoint2 + "\n" +
									//	validString);
								}
							} catch (AWTException e1) {
								e1.printStackTrace();
							}
						}

						@Override
						public void mousePressed(MouseEvent e) {							
						}

						@Override
						public void mouseReleased(MouseEvent e) {							
						}

						@Override
						public void mouseEntered(MouseEvent e) {
						}

						@Override
						public void mouseExited(MouseEvent e) {							
						}
					});
				}

				@Override
				protected void paintComponent(Graphics g){
					//System.out.println("PAINTING DOTS");
					super.paintComponent(g);
					g2D = (Graphics2D)g;
					for (int row = 0; row < rows; row++) {
						for (int col = 0; col < columns; col++) {
							Point point = points[row][col];
							g2D.setPaint(new Color(237, 237, 237));
							g2D.setStroke(new BasicStroke(5));
							if(col < columns-1) g2D.drawLine(point.x+25, point.y+25, point.x+25, point.y+75);
							if(row < rows-1) g2D.drawLine(point.x+25, point.y+25, point.x+75, point.y+25);
							placeDot(g, point.x, point.y);
						}
					}
				}

				public void placeDot(Graphics g, int x, int y){
					g.drawImage(dot, (x)+22, (y)+22, this);
				}
			}

			public class PlayerPane extends JPanel{

				/**
				 * 
				 */
				public static final long serialVersionUID = 399723497489724010L;
				Color color;
				public HashMap<Point, String> drawnLines = new HashMap<Point, String>();
				public HashSet<Point> drawnBoxes = new HashSet<Point>();

				public PlayerPane(Color color){
					this.color = color;
					setPreferredSize(new Dimension((rows*50), (columns*50)));
					setOpaque(false);
				}

				public void drawLine(Point point, String direction){
					String pointString = fromPoint(point);
					if(direction.equals("horizontal")){
						if(drawnLines.containsKey(point)) drawnLines.put(point, "both");
						else drawnLines.put(new Point(point.x, point.y), "horizontal");
						if(occupiedPoints.containsKey(pointString)) occupiedPoints.put(point.x + "," + point.y, "both");//occupiedPoints.put(point, "both");
						else{
							if(matchType == MatchType.CLIENT) System.out.println("PLACING " + point.x + ", " + point.y);
							occupiedPoints.put(point.x + "," + point.y, "horizontal"); //occupiedPoints.put(point, "horizontal");
						}
					}
					else if(direction.equals("vertical")){
						if(drawnLines.containsKey(point)) drawnLines.put(point, "both");
						else drawnLines.put(new Point(point.x, point.y), "vertical");
						if(occupiedPoints.containsKey(pointString)) occupiedPoints.put(point.x + "," + point.y, "both");//occupiedPoints.put(point, "both");
						else{
							if(matchType == MatchType.CLIENT) System.out.println("PLACING " + point.x + ", " + point.y);
							occupiedPoints.put(point.x + "," + point.y, "vertical");//occupiedPoints.put(point, "vertical");
						}
					}
					//updateUI();
				}

				public void drawBox(Point point){
					drawnBoxes.add(point);
				}

				@Override
				protected void paintComponent(Graphics g){
					super.paintComponent(g);
					g.setColor(color);
					for(Point point : drawnLines.keySet()){
						if(drawnLines.get(point).equals("horizontal")) g.drawLine(point.x+29, point.y+25, point.x+71, point.y+25);
						else if(drawnLines.get(point).equals("vertical")) g.drawLine(point.x+25, point.y+29, point.x+25, point.y+71);
						else{
							g.drawLine(point.x+29, point.y+25, point.x+71, point.y+25);
							g.drawLine(point.x+25, point.y+29, point.x+25, point.y+71);
						}
						//g.drawLine(50+26, 50+28, 50+26, 50+71);
						//g.drawLine(50+27, 50+28, 50+27, 50+72);
						//g.drawLine(50+28, 50+27, 50+72, 50+27);
						//g.drawLine(50+28, 50+26, 50+71, 50+26);
						//g.fillRect(50+28, 50+28, 45, 45);
						//(Graphics2D)g.
						//g.drawLine(100+23, 50+28, 100+23, 50+72);
						//g.drawLine(100+24, 50+28, 100+24, 50+71);
						//g.drawLine(50+28, 100+24, 50+71, 100+24);
						//g.drawLine(50+28, 100+23, 50+72, 100+23);
					}
					for(Point point : drawnBoxes){
						int x = point.x;
						int y = point.y;
						g.drawLine(x+26, y+28, x+26, y+71); //left
						g.drawLine(x+27, y+28, x+27, y+72); //left

						g.drawLine(x+28, y+27, x+72, y+27); //top
						g.drawLine(x+28, y+26, x+71, y+26); //top

						g.fillRect(x+28, y+28, 45, 45);
						//(Graphics2D)g.
						g.drawLine(x+73, y+28, x+73, y+72); //right
						g.drawLine(x+74, y+28, x+74, y+71); //right

						g.drawLine(x+28, y+74, x+71, y+74); //bottom
						g.drawLine(x+28, y+73, x+72, y+73); //bottom
					}
				}
			}
		}

		/*public class TestPane extends JPanel {

			public static final long serialVersionUID = 3619887266045276377L;
			public int columnCount;
			public int rowCount;
			public List<Rectangle> cells;
			public Point selectedCell;

			public TestPane(int rows, int columns) {
				rowCount = rows;
				columnCount = columns;
				cells = new ArrayList<>(columnCount * rowCount);
				MouseAdapter mouseHandler;
				mouseHandler = new MouseAdapter() {
					@Override
					public void mouseMoved(MouseEvent e) {
						//Point point = e.getPoint();

						int width = getWidth();
						int height = getHeight();

						int cellWidth = width / columnCount;
						int cellHeight = height / rowCount;

						int column = e.getX() / cellWidth;
						int row = e.getY() / cellHeight;

						selectedCell = new Point(column, row);
						repaint();

					}
				};
				addMouseMotionListener(mouseHandler);
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(200, 200);
			}

			@Override
			public void invalidate() {
				cells.clear();
				selectedCell = null;
				super.invalidate();
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g.create();

				int width = getWidth();
				int height = getHeight();

				int cellWidth = width / columnCount;
				int cellHeight = height / rowCount;

				int xOffset = (width - (columnCount * cellWidth)) / 2;
				int yOffset = (height - (rowCount * cellHeight)) / 2;

				if (cells.isEmpty()) {
					for (int row = 0; row < rowCount; row++) {
						for (int col = 0; col < columnCount; col++) {
							Rectangle cell = new Rectangle(
									xOffset + (col * cellWidth),
									yOffset + (row * cellHeight),
									cellWidth,
									cellHeight);
							cells.add(cell);
						}
					}
				}

				if (selectedCell != null) {

					int index = selectedCell.x + (selectedCell.y * columnCount);
					Rectangle cell = cells.get(index);
					g2d.setColor(Color.BLUE);
					g2d.fill(cell);

				}

				g2d.setColor(Color.GRAY);
				for (Rectangle cell : cells) {
					g2d.draw(cell);
				}

				g2d.dispose();
			}
		}
		 */	}

}

class Point{

	public int x;
	public int y;

	public Point(int x, int y){
		this.x = x;
		this.y = y;
	}
}
