package com.gmail.jackbcousineau;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

import com.gmail.jackbcousineau.Match.MatchType;

public class MatchClient {

	OutputStream ostream;
	PrintWriter pwrite;
	public ClientThread thread;
	public JFrame clientFrame = new JFrame("Join a lobby");
	public JPanel pane;
	public JTextField nameField, ipField;
	public PregamePlayerPane pregamePlayerPane;
	final JSpinner portSpinner = new JSpinner(new SpinnerNumberModel(9000, 1025, 9999, 1));
	public JButton cancelButton = new JButton("Cancel");
	public JLabel spinningWheel, requestStatus, sizeLabel, sizeNumber, movesFirstLabel, movesFirstName;
	public int rows = 5;
	public int columns = 5;

	public MatchClient() throws IOException{
		clientFrame.setSize(new Dimension(250, 180));
		clientFrame.setLocationRelativeTo(null);
		clientFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		pane = new JPanel();
		SpringLayout layout = new SpringLayout();
		pane.setLayout(layout);

		JLabel nameLabel, ipLabel, portLabel;
		nameLabel = new JLabel("Your name:");
		pane.add(nameLabel);
		ipLabel = new JLabel("IP:");
		pane.add(ipLabel);
		portLabel = new JLabel("Port:");
		pane.add(portLabel);

		JPanel separatorPane = new JPanel();
		separatorPane.setBackground(Color.GRAY);
		pane.add(separatorPane);

		JButton connectButton = new JButton("Connect");
		pane.add(connectButton);
		connectButton.setFocusable(false);


		nameField = new JTextField("");
		pane.add(nameField);
		ipField = new JTextField("");
		ipField.setText(InetAddress.getLocalHost().getHostAddress());
		pane.add(ipField);
		pane.add(portSpinner);

		pane.add(cancelButton);
		cancelButton.setVisible(false);
		cancelButton.setFocusable(false);
		spinningWheel = new JLabel(new ImageIcon(Dots.spinningWheel));
		pane.add(spinningWheel);
		spinningWheel.setVisible(false);
		requestStatus = new JLabel("Waiting for host to accept...");
		pane.add(requestStatus);
		requestStatus.setVisible(false);
		sizeLabel = new JLabel("Size");
		pane.add(sizeLabel);
		sizeLabel.setVisible(false);
		sizeLabel.setForeground(Color.GRAY);
		sizeNumber = new JLabel(rows + "x" + columns);
		pane.add(sizeNumber);
		sizeNumber.setVisible(false);
		sizeNumber.setFont(new Font("Lucia Grande", Font.PLAIN, 16));
		movesFirstLabel = new JLabel("moves first");
		pane.add(movesFirstLabel);
		movesFirstLabel.setVisible(false);
		movesFirstLabel.setForeground(Color.GRAY);
		movesFirstName = new JLabel("Opponent");
		pane.add(movesFirstName);
		movesFirstName.setVisible(false);
		movesFirstName.setFont(new Font("Lucia Grande", Font.PLAIN, 12));

		pregamePlayerPane = new PregamePlayerPane(2);
		//clientFrame.getContentPane().add(pregamePlayerPane);
		clientFrame.getContentPane().add(pane);
		pane.add(pregamePlayerPane);
		//pregamePlayerPane.setOpaque(true);
		pregamePlayerPane.setVisible(false);

		connectButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				//String address = JOptionPane.showInputDialog("Server address");
				//String port = JOptionPane.showInputDialog("Port");
				try{
					thread = new ClientThread();
					thread.start();
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});
		cancelButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Cancelled");
				reset();
			}
		});
		clientFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				System.out.println("Closed");
				reset();
			}
		});

		layout.putConstraint(SpringLayout.NORTH, nameLabel, 15, SpringLayout.NORTH, pane);
		layout.putConstraint(SpringLayout.WEST, nameLabel, 10, SpringLayout.WEST, pane);

		layout.putConstraint(SpringLayout.NORTH, nameField, 9, SpringLayout.NORTH, pane);
		layout.putConstraint(SpringLayout.WEST, nameField, 10, SpringLayout.EAST, nameLabel);
		layout.putConstraint(SpringLayout.EAST, nameField, -10, SpringLayout.EAST, pane);

		layout.putConstraint(SpringLayout.NORTH, separatorPane, 12, SpringLayout.SOUTH, nameLabel);
		layout.putConstraint(SpringLayout.WEST, separatorPane, 6, SpringLayout.WEST, pane);
		layout.putConstraint(SpringLayout.SOUTH, separatorPane, 1, SpringLayout.NORTH, separatorPane);
		layout.putConstraint(SpringLayout.EAST, separatorPane, -6, SpringLayout.EAST, pane);

		layout.putConstraint(SpringLayout.NORTH, ipLabel, 15, SpringLayout.NORTH, separatorPane);
		layout.putConstraint(SpringLayout.EAST, ipLabel, -10, SpringLayout.WEST, ipField);
		//layout.putConstraint(SpringLayout.EAST, ipLabel, -10, SpringLayout.EAST, pane);

		layout.putConstraint(SpringLayout.NORTH, ipField, 9, SpringLayout.NORTH, separatorPane);
		layout.putConstraint(SpringLayout.WEST, ipField, -30, SpringLayout.WEST, nameField);
		layout.putConstraint(SpringLayout.EAST, ipField, -40, SpringLayout.EAST, pane);

		layout.putConstraint(SpringLayout.NORTH, portLabel, 15, SpringLayout.SOUTH, ipLabel);
		layout.putConstraint(SpringLayout.EAST, portLabel, -10, SpringLayout.WEST, portSpinner);

		layout.putConstraint(SpringLayout.NORTH, portSpinner, 9, SpringLayout.SOUTH, ipLabel);
		layout.putConstraint(SpringLayout.WEST, portSpinner, -30, SpringLayout.HORIZONTAL_CENTER, pane);
		//layout.putConstraint(SpringLayout.EAST, portSpinner, -10, SpringLayout.EAST, pane);

		layout.putConstraint(SpringLayout.NORTH, connectButton, 15, SpringLayout.SOUTH, portLabel);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, connectButton, 0, SpringLayout.HORIZONTAL_CENTER, pane);

		layout.putConstraint(SpringLayout.SOUTH, cancelButton, -5, SpringLayout.SOUTH, pane);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, cancelButton, 0, SpringLayout.HORIZONTAL_CENTER, pane);

		layout.putConstraint(SpringLayout.SOUTH, spinningWheel, -10, SpringLayout.NORTH, cancelButton);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, spinningWheel, 0, SpringLayout.HORIZONTAL_CENTER, pane);

		layout.putConstraint(SpringLayout.SOUTH, requestStatus, -10, SpringLayout.NORTH, spinningWheel);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, requestStatus, 0, SpringLayout.HORIZONTAL_CENTER, pane);

		layout.putConstraint(SpringLayout.EAST, sizeLabel, -17, SpringLayout.WEST, cancelButton);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, sizeLabel, 7, SpringLayout.VERTICAL_CENTER, cancelButton);

		layout.putConstraint(SpringLayout.SOUTH, sizeNumber, 2, SpringLayout.NORTH, sizeLabel);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, sizeNumber, 0, SpringLayout.HORIZONTAL_CENTER, sizeLabel);

		layout.putConstraint(SpringLayout.WEST, movesFirstLabel, 3, SpringLayout.EAST, cancelButton);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, movesFirstLabel, 3, SpringLayout.VERTICAL_CENTER, cancelButton);

		layout.putConstraint(SpringLayout.SOUTH, movesFirstName, 2, SpringLayout.NORTH, movesFirstLabel);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, movesFirstName, -1, SpringLayout.HORIZONTAL_CENTER, movesFirstLabel);

		//layout.putConstraint(SpringLayout.WEST, columnsLabel, 0, SpringLayout.EAST, cancelButton);
		//layout.putConstraint(SpringLayout.VERTICAL_CENTER, columnsLabel, -3, SpringLayout.VERTICAL_CENTER, cancelButton);

		layout.putConstraint(SpringLayout.NORTH, pregamePlayerPane, 1, SpringLayout.NORTH, pane);
		layout.putConstraint(SpringLayout.EAST, pregamePlayerPane, 0, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.SOUTH, pregamePlayerPane, 5, SpringLayout.NORTH, cancelButton);
		layout.putConstraint(SpringLayout.WEST, pregamePlayerPane, 0, SpringLayout.WEST, pane);

		clientFrame.setVisible(true);

		//Socket sock = new Socket("127.0.0.1", 3000);
		//Socket connectionToTheServer = new Socket("localhost", 9000); // First param: server-address, Second: the port
		//String address = JOptionPane.showInputDialog("Server address");
		//String port = JOptionPane.showInputDialog("Port");
		//Socket socket = new Socket("127.0.0.1", 9000);
	}

	private void reset(){
		hidePregame();
		if(thread != null){
			thread.sendMessage("quit");
			try {
				thread.socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			thread.stop();
		}
		//if(thread.)
	}

	private void showWaiting(){
		for(Component component : pane.getComponents()){
			component.setVisible(false);
		}
		//pregamePlayerPane.setVisible(true);
		cancelButton.setVisible(true);
		spinningWheel.setVisible(true);
		requestStatus.setVisible(true);
		clientFrame.setSize(new Dimension(250, 140));
	}

	private void showPregame(){
		spinningWheel.setVisible(false);
		requestStatus.setVisible(false);
		sizeLabel.setVisible(true);
		sizeNumber.setVisible(true);
		movesFirstLabel.setVisible(true);
		movesFirstName.setVisible(true);
		//columnsLabel.setVisible(true);
		pregamePlayerPane.setVisible(true);
	}

	private void hidePregame(){
		for(Component component : pane.getComponents()){
			component.setVisible(true);
		}
		cancelButton.setVisible(false);
		spinningWheel.setVisible(false);
		requestStatus.setVisible(false);
		sizeLabel.setVisible(false);
		sizeNumber.setVisible(false);
		movesFirstLabel.setVisible(false);
		movesFirstName.setVisible(false);
		pregamePlayerPane.setVisible(false);
		clientFrame.setSize(new Dimension(250, 180));
	}

	public class ClientThread extends Thread{

		public Socket socket;
		OutputStream out;

		public void run(){
			try {
				String address = ipField.getText();
				String port = (Integer)portSpinner.getValue() + "";
				System.out.println("Starting client thread");
				socket = new Socket(address, Integer.parseInt(port));
				//String string = JOptionPane.showInputDialog("What do you want to tell the server?");
				showWaiting();
				//pane.setVisible(false);
				out = socket.getOutputStream();
				PrintStream ps = new PrintStream(out, true);
				//ps.println(nameField.getText() + "," + string);
				ps.println("request:" + nameField.getText());
				DataInputStream in = new DataInputStream(socket.getInputStream());
				String s = null;
				while ((s = in.readLine()) != null) {
					System.out.println(s);
					if(s.equals("requestDeclined")){
						JOptionPane.showMessageDialog(clientFrame, "Connection declined by the host", "Aw...", JOptionPane.PLAIN_MESSAGE, null);

						reset();
					}
					else if(s.equals("requestAccepted")){
						//pregamePlayerPane.setVisible(true);
						System.out.println("ACCEPTED");
						pregamePlayerPane.player2Name.setText(nameField.getText());
						showPregame();
						//pane.setVisible(false);
						//pregamePlayerPane.setVisible(true);
					}
					else if(s.startsWith("rows:")){
						String[] string = s.split(":");
						rows = Integer.parseInt(string[1]);
						sizeNumber.setText(rows + "x" + columns);
					}
					else if(s.startsWith("columns")){
						String[] string = s.split(":");
						columns = Integer.parseInt(string[1]);
						sizeNumber.setText(rows + "x" + columns);
					}
					else if(s.equals("quit")){
						JOptionPane.showMessageDialog(clientFrame, "The host has quit", ":(", JOptionPane.PLAIN_MESSAGE, null);
						reset();
					}
					else if(s.equals("moveSwitch")){
						if(movesFirstName.getText().equals("Opponent")){
							movesFirstName.setText("You");
							movesFirstLabel.setText("move first");
						}
						else if(movesFirstName.getText().equals("You")){
							movesFirstName.setText("Opponent");
							movesFirstLabel.setText("moves first");
						}
					}
					else if(s.startsWith("color:")){
						String[] string = s.split(":");
						Color color = Color.decode(string[1]);
						pregamePlayerPane.player1Pane.setColor(color);
					}
					else if(s.startsWith("nameEdit:")){
						if(s.length() > 9){
							String[] string = s.split(":");
							pregamePlayerPane.player1Name.setText(string[1]);
						}
					}
					else if(s.equals("starting")){
						Dots.clientMatch = new Match(MatchType.CLIENT, columns, rows,
								pregamePlayerPane.player1Name.getText(), pregamePlayerPane.player1Pane.color,
								pregamePlayerPane.player2Name.getText(), pregamePlayerPane.player2Pane.color, thread);
						if(movesFirstName.getText().equals("You")) Dots.clientMatch.gameFrame.scorePane.setPlayer2Move();
						hidePregame();
					}
					else if(s.startsWith("move,")){
						//System.out.println("CLIENT RECIEVED MOVE");
						String[] string = s.split(",");
						Dots.clientMatch.gameFrame.scorePane.makeMove(1, new Point(Integer.parseInt(string[1]), Integer.parseInt(string[2])), string[3], true);
						//Dots.clientMatch.gameFrame.boardPane.player1Pane.drawLine(new Point(Integer.parseInt(string[1]), Integer.parseInt(string[2])), string[3]);
						//System.out.println(Dots.clientMatch.occupiedPoints.containsKey(new Point(Integer.parseInt(string[1]), Integer.parseInt(string[2]))));
						Timer timer = new Timer();
						timer.schedule(new PointCheckTask(timer, new Point(Integer.parseInt(string[1]), Integer.parseInt(string[2]))), 1000);
						//Dots.clientMatch.gameFrame.scorePane.makeMove(1, new Point(50, 50), "vertical");
					}
				}
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private class PointCheckTask extends TimerTask{

			private Timer timer;
			private Point point;

			public PointCheckTask(Timer timer, Point point){
				this.timer = timer;
				this.point = point;
			}

			@Override
			public void run(){
				System.out.println(point.x + "," + point.y + ": " + Dots.clientMatch.occupiedPoints.containsKey(point.x + "," + point.y));
				System.out.println(Dots.clientMatch.occupiedPoints.size());
				StringBuilder string = new StringBuilder("Points:");
				for(String point : Dots.clientMatch.occupiedPoints.keySet()){
					//string.append(point.x + "," + point.y + Dots.clientMatch.occupiedPoints.get(point) + "\n");
				}
				//System.out.println(string);
				timer.cancel();
			}
		}

		public void sendMessage(String string){
			PrintStream printStream = new PrintStream(out, true);
			printStream.println(string);
		}
	}

}
