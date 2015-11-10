package com.gmail.jackbcousineau;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import javax.net.ssl.SSLContext;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.gmail.jackbcousineau.Dots.MainMenuFrame.PlayerType;
import com.gmail.jackbcousineau.Match.MatchType;

public class MatchServer {

	public SocketServer server;
	private static String test;
	public String player2Name;
	final JSpinner spinner = new JSpinner(new SpinnerNumberModel(9000, 1025, 9999, 1));
	JSpinner rowsSpinner, columnsSpinner;
	public PregamePlayerPane pregamePlayerPane;
	final JLabel serverStatus, connectionStatus, spinningWheel, connectionLabel, requestLabel, rowsLabel, columnsLabel;
	public JCheckBox player1CheckBox = new JCheckBox("first move");
	public JCheckBox player2CheckBox = new JCheckBox("");
	JButton createLobby, confirmButton, declineButton, cancelButton, startButton;
	public JFrame hostFrame = new JFrame("New Lobby");
	JPanel pane, internalPane, pregamePane, pregamePaneSeparator;
	public ServerThread thread;
	public boolean incomingRequest = false;

	public MatchServer() throws Exception{
		//ServerSocket server = new ServerSocket(8888); // 8888 is the port the server will listen on.
		//Socket connectionToTheServer = new Socket("localhost", 8888); // First param: server-address, Second: the port
		//Socket connectionToTheClient = server.accept();

		//Port to monitor
		//final int myPort = 9000;
		//ServerSocket ssock = new ServerSocket(myPort);
		//InetAddress.getByName("localhost")
		hostFrame.setMinimumSize(new Dimension(250, 200));
		hostFrame.setLocationRelativeTo(null);
		hostFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		pane = new JPanel();
		hostFrame.getContentPane().add(pane);
		SpringLayout layout = new SpringLayout();
		internalPane = new JPanel();
		SpringLayout ilayout = new SpringLayout();
		JLabel serverStatusLabel, serverIP;
		final JLabel serverPort;
		JPanel addressSeparator, centerSeparator;
		addressSeparator = new JPanel();
		addressSeparator.setBackground(Color.DARK_GRAY);
		centerSeparator = new JPanel();
		centerSeparator.setBackground(Color.GRAY);
		createLobby = new JButton("Open");
		//centerSeparator.setBorder(BorderFactory.createEtchedBorder(Color.GRAY, Color.GRAY));
		serverStatusLabel = new JLabel("Server is ");
		serverStatus = new JLabel("offline");
		serverStatus.setForeground(Color.BLUE);
		serverIP = new JLabel("IP: " + InetAddress.getLocalHost().getHostAddress());
		internalPane.setBorder(BorderFactory.createLoweredBevelBorder());
		pane.setLayout(layout);
		serverPort = new JLabel("Port: " + (Integer)spinner.getValue());
		connectionStatus = new JLabel("Waiting for connection...");
		connectionStatus.setVisible(false);
		spinningWheel = new JLabel(new ImageIcon(Dots.spinningWheel));
		spinningWheel.setVisible(false);
		connectionLabel = new JLabel("Click \"open\" to start the server");
		requestLabel = new JLabel("Jack would like to join your game");
		requestLabel.setVisible(false);
		confirmButton = new JButton("Allow");
		confirmButton.setVisible(false);
		confirmButton.setFocusable(false);
		declineButton = new JButton("Decline");
		declineButton.setVisible(false);
		declineButton.setFocusable(false);
		pregamePane = new JPanel();
		SpringLayout pregameLayout = new SpringLayout();
		pregamePane.setLayout(pregameLayout);
		pane.add(pregamePane);
		pane.add(internalPane);

		pregamePane.setOpaque(true);
		//pregamePane.setBackground(Color.GRAY);
		pregamePane.setVisible(false);
		cancelButton = new JButton("Cancel");
		cancelButton.setFocusable(false);
		pregamePane.add(cancelButton);
		startButton = new JButton("Start Game");
		startButton.setFocusable(false);
		pregamePane.add(startButton);
		rowsLabel = new JLabel("Rows");
		pregamePane.add(rowsLabel);
		columnsLabel = new JLabel("Columns");
		pregamePane.add(columnsLabel);
		rowsSpinner = new JSpinner(new SpinnerNumberModel(5, 2, 50, 1));
		pregamePane.add(rowsSpinner);
		columnsSpinner = new JSpinner(new SpinnerNumberModel(5, 2, 50, 1));
		pregamePane.add(columnsSpinner);
		pregamePaneSeparator = new JPanel();
		pregamePaneSeparator.setBackground(Color.GRAY);
		pregamePane.add(player1CheckBox);
		player1CheckBox.setFocusable(false);
		player1CheckBox.setSelected(true);
		pregamePane.add(player2CheckBox);
		player2CheckBox.setFocusable(false);
		//pregamePane.add(pregamePaneSeparator);
		pregamePlayerPane = new PregamePlayerPane(1);
		pregamePane.add(pregamePlayerPane);

		internalPane.setLayout(ilayout);
		internalPane.add(serverStatusLabel);
		internalPane.add(serverStatus);
		internalPane.add(serverIP);
		internalPane.add(serverPort);
		internalPane.add(addressSeparator);
		internalPane.add(centerSeparator);
		internalPane.add(connectionStatus);
		internalPane.add(spinningWheel);
		internalPane.add(connectionLabel);
		internalPane.add(requestLabel);
		internalPane.add(confirmButton);
		internalPane.add(declineButton);
		pane.add(spinner);
		pane.add(createLobby);
		spinner.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				serverPort.setText("Port: " + (Integer)spinner.getValue());
			}
		});
		createLobby.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(createLobby.getText().equals("Open")){
					thread = null;
					thread = new ServerThread();
					thread.start();
				}
				else{
					//JOptionPane.showMessageDialog(null, test);
					closeServer();
				}
			}
		});
		confirmButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				//server.client.sendMessage("hi");
				requestLabel.setVisible(false);
				confirmButton.setVisible(false);
				declineButton.setVisible(false);
				server.client.sendMessage("requestAccepted");
				pregamePane.setVisible(true);
			}

		});
		declineButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				server.client.sendMessage("requestDeclined");
				connectionStatus.setVisible(true);
				spinningWheel.setVisible(true);
				requestLabel.setVisible(false);
				confirmButton.setVisible(false);
				declineButton.setVisible(false);
			}
		});
		rowsSpinner.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				server.client.sendMessage("rows:" + rowsSpinner.getValue());
			}
		});
		columnsSpinner.addChangeListener(new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent e) {
				server.client.sendMessage("columns:" + columnsSpinner.getValue());
			}
		});
		cancelButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				server.client.sendMessage("quit");
				closeServer();
				pregamePane.setVisible(false);
			}
		});
		player1CheckBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(player2CheckBox.isSelected()){
					player2CheckBox.setSelected(false);
					server.client.sendMessage("moveSwitch");
				}
				else{
					player1CheckBox.setSelected(true);
				}
			}
		});
		player2CheckBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(player1CheckBox.isSelected()){
					player1CheckBox.setSelected(false);
					server.client.sendMessage("moveSwitch");
				}
				else{
					player2CheckBox.setSelected(true);
				}
			}
		});
		startButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				server.client.sendMessage("starting");
				pregamePane.setVisible(false);
				Dots.hostMatch = new Match(MatchType.SERVER, (Integer)columnsSpinner.getValue(), (Integer)rowsSpinner.getValue(),
						pregamePlayerPane.player1Name.getText(), pregamePlayerPane.player1Pane.color,
						pregamePlayerPane.player2Name.getText(), pregamePlayerPane.player2Pane.color, server.client);
				if(player2CheckBox.isSelected()) Dots.hostMatch.gameFrame.scorePane.setPlayer2Move();
			}
		});
		hostFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				System.out.println("Closed");
				if(server != null){
					server.client.sendMessage("quit");
					System.out.println("Closing server");
					closeServer();
				}
				pregamePane.setVisible(false);
				Dots.MainMenuFrame.playersBox.setEnabled(true);
				Dots.MainMenuFrame.player2Pane.nameField.setEnabled(true);
				Dots.MainMenuFrame.player2Pane.colorChooser.setEnabled(true);
			}
		});

		layout.putConstraint(SpringLayout.EAST, spinner, -5, SpringLayout.HORIZONTAL_CENTER, pane);
		layout.putConstraint(SpringLayout.NORTH, spinner, 10, SpringLayout.NORTH, pane);

		layout.putConstraint(SpringLayout.WEST, createLobby, 5, SpringLayout.HORIZONTAL_CENTER, pane);
		layout.putConstraint(SpringLayout.NORTH, createLobby, 10, SpringLayout.NORTH, pane);

		layout.putConstraint(SpringLayout.NORTH, internalPane, 10, SpringLayout.SOUTH, spinner);
		layout.putConstraint(SpringLayout.EAST, internalPane, -10, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.SOUTH, internalPane, -10, SpringLayout.SOUTH, pane);
		layout.putConstraint(SpringLayout.WEST, internalPane, 10, SpringLayout.WEST, pane);

		ilayout.putConstraint(SpringLayout.NORTH, serverStatusLabel, 10, SpringLayout.NORTH, internalPane);
		ilayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, serverStatusLabel, -17, SpringLayout.HORIZONTAL_CENTER, internalPane);

		ilayout.putConstraint(SpringLayout.NORTH, serverStatus, 10, SpringLayout.NORTH, internalPane);
		ilayout.putConstraint(SpringLayout.WEST, serverStatus, 0, SpringLayout.EAST, serverStatusLabel);

		ilayout.putConstraint(SpringLayout.NORTH, serverIP, 5, SpringLayout.SOUTH, serverStatusLabel);
		ilayout.putConstraint(SpringLayout.EAST, serverIP, -5, SpringLayout.WEST, addressSeparator);

		ilayout.putConstraint(SpringLayout.NORTH, addressSeparator, 5, SpringLayout.SOUTH, serverStatusLabel);
		ilayout.putConstraint(SpringLayout.WEST, addressSeparator, 5, SpringLayout.HORIZONTAL_CENTER, internalPane);
		ilayout.putConstraint(SpringLayout.SOUTH, addressSeparator, 15, SpringLayout.NORTH, addressSeparator);
		ilayout.putConstraint(SpringLayout.EAST, addressSeparator, 1, SpringLayout.WEST, addressSeparator);

		ilayout.putConstraint(SpringLayout.NORTH, serverPort, 5, SpringLayout.SOUTH, serverStatusLabel);
		ilayout.putConstraint(SpringLayout.WEST, serverPort, 5, SpringLayout.EAST, addressSeparator);

		ilayout.putConstraint(SpringLayout.NORTH, centerSeparator, 10, SpringLayout.SOUTH, addressSeparator);
		ilayout.putConstraint(SpringLayout.WEST, centerSeparator, 6, SpringLayout.WEST, internalPane);
		ilayout.putConstraint(SpringLayout.SOUTH, centerSeparator, 1, SpringLayout.NORTH, centerSeparator);
		ilayout.putConstraint(SpringLayout.EAST, centerSeparator, -6, SpringLayout.EAST, internalPane);

		ilayout.putConstraint(SpringLayout.NORTH, connectionStatus, 5, SpringLayout.SOUTH, centerSeparator);
		ilayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, connectionStatus, 5, SpringLayout.HORIZONTAL_CENTER, internalPane);

		ilayout.putConstraint(SpringLayout.NORTH, spinningWheel, 5, SpringLayout.SOUTH, connectionStatus);
		ilayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, spinningWheel, 5, SpringLayout.HORIZONTAL_CENTER, internalPane);

		ilayout.putConstraint(SpringLayout.NORTH, connectionLabel, 17, SpringLayout.SOUTH, centerSeparator);
		ilayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, connectionLabel, 0, SpringLayout.HORIZONTAL_CENTER, internalPane);

		ilayout.putConstraint(SpringLayout.NORTH, requestLabel, 7, SpringLayout.SOUTH, centerSeparator);
		ilayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, requestLabel, 0, SpringLayout.HORIZONTAL_CENTER, internalPane);

		ilayout.putConstraint(SpringLayout.NORTH, confirmButton, 5, SpringLayout.SOUTH, requestLabel);
		ilayout.putConstraint(SpringLayout.WEST, confirmButton, 4, SpringLayout.HORIZONTAL_CENTER, internalPane);

		ilayout.putConstraint(SpringLayout.NORTH,  declineButton, 5, SpringLayout.SOUTH, requestLabel);
		ilayout.putConstraint(SpringLayout.EAST, declineButton, -4, SpringLayout.HORIZONTAL_CENTER, internalPane);

		layout.putConstraint(SpringLayout.NORTH, pregamePane, 0, SpringLayout.NORTH, pane);
		layout.putConstraint(SpringLayout.EAST, pregamePane, 0, SpringLayout.EAST, pane);
		layout.putConstraint(SpringLayout.SOUTH, pregamePane, 0, SpringLayout.SOUTH, pane);
		layout.putConstraint(SpringLayout.WEST, pregamePane, 0, SpringLayout.WEST, pane);

		pregameLayout.putConstraint(SpringLayout.SOUTH, cancelButton, -5, SpringLayout.SOUTH, pregamePane);
		pregameLayout.putConstraint(SpringLayout.WEST, cancelButton, 23, SpringLayout.WEST, pregamePane);

		pregameLayout.putConstraint(SpringLayout.SOUTH, startButton, -5, SpringLayout.SOUTH, pregamePane);
		pregameLayout.putConstraint(SpringLayout.WEST, startButton, 5, SpringLayout.EAST, cancelButton);

		pregameLayout.putConstraint(SpringLayout.NORTH, rowsLabel, -3, SpringLayout.SOUTH, rowsSpinner);
		pregameLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, rowsLabel, -6, SpringLayout.HORIZONTAL_CENTER, rowsSpinner);

		pregameLayout.putConstraint(SpringLayout.NORTH, rowsSpinner, 5, SpringLayout.NORTH, pregamePane);
		pregameLayout.putConstraint(SpringLayout.EAST, rowsSpinner, -10, SpringLayout.HORIZONTAL_CENTER, pregamePane);

		pregameLayout.putConstraint(SpringLayout.NORTH, pregamePaneSeparator, 5, SpringLayout.NORTH, pregamePane);
		pregameLayout.putConstraint(SpringLayout.EAST, pregamePaneSeparator, -12, SpringLayout.HORIZONTAL_CENTER, pregamePane);
		pregameLayout.putConstraint(SpringLayout.SOUTH, pregamePaneSeparator, 17, SpringLayout.NORTH, pregamePaneSeparator);
		pregameLayout.putConstraint(SpringLayout.WEST, pregamePaneSeparator, -1, SpringLayout.EAST, pregamePaneSeparator);

		pregameLayout.putConstraint(SpringLayout.NORTH, columnsLabel, -3, SpringLayout.SOUTH, columnsSpinner);
		pregameLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, columnsLabel, -4, SpringLayout.HORIZONTAL_CENTER, columnsSpinner);

		pregameLayout.putConstraint(SpringLayout.NORTH, columnsSpinner, 5, SpringLayout.NORTH, pregamePane);
		pregameLayout.putConstraint(SpringLayout.WEST, columnsSpinner, 10, SpringLayout.HORIZONTAL_CENTER, pregamePane);

		pregameLayout.putConstraint(SpringLayout.NORTH, player1CheckBox, -10, SpringLayout.SOUTH, pregamePlayerPane);
		pregameLayout.putConstraint(SpringLayout.WEST, player1CheckBox, 70, SpringLayout.WEST, pregamePane);

		pregameLayout.putConstraint(SpringLayout.NORTH, player2CheckBox, -10, SpringLayout.SOUTH, pregamePlayerPane);
		pregameLayout.putConstraint(SpringLayout.WEST, player2CheckBox, -5, SpringLayout.EAST, player1CheckBox);

		pregameLayout.putConstraint(SpringLayout.NORTH, pregamePlayerPane, 0, SpringLayout.SOUTH, rowsLabel);
		pregameLayout.putConstraint(SpringLayout.EAST, pregamePlayerPane, 0, SpringLayout.EAST, pregamePane);
		pregameLayout.putConstraint(SpringLayout.SOUTH, pregamePlayerPane, -15, SpringLayout.NORTH, cancelButton);
		pregameLayout.putConstraint(SpringLayout.WEST, pregamePlayerPane, 0, SpringLayout.WEST, pregamePane);

		createLobby.setFocusable(false);
		hostFrame.setVisible(true);
	}

	public void closeServer(){

		//try {
		//thread.stop();
		//if(serverSocket == null) System.out.println("Serversocket is null");
		//else serverSocket.close();
		//} catch (IOException e) {
		//e.printStackTrace();
		//}
		try {
			server.serverSocket.close();
		} catch (IOException e) {
			System.out.println("Error closing server");
		}
		spinner.setEnabled(true);
		createLobby.setText("Open");
		serverStatus.setText("offline");
		serverStatus.setForeground(Color.BLUE);
		connectionStatus.setVisible(false);
		spinningWheel.setVisible(false);
		connectionLabel.setVisible(true);
		requestLabel.setVisible(false);
		confirmButton.setVisible(false);
		declineButton.setVisible(false);
		thread.stop();
		server = null;
	}

	private class ServerThread extends Thread{

		public void run(){
			int port = (Integer)spinner.getValue();
			try {
				System.out.println("Starting server thread");
				server = new SocketServer(port);
				server.startListening();
				//if(server != null){
				//spinner.setEnabled(false);
				//createLobby.setText("Close");
				//serverStatus.setText("Server is online");
				//}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//System.out.println("FIRST CATCH");
				//server.closeServer(false);
				//JOptionPane.showMessageDialog(hostFrame, "Port " + port + " is already in use");
				//e.printStackTrace();
			}
			//spinner.setEnabled(false);
			//createLobby.setText("Close");
			//serverStatus.setText("Server is online");
		}
	}

	public class SocketServer{

		public ServerSocket serverSocket;
		public Socket sock;
		public OneConnection client;

		public SocketServer(int port) throws Exception{
			//"127.0.0.1"
			try {
				serverSocket = new ServerSocket(port, 0, new InetSocketAddress(InetAddress.getLocalHost(), 9000).getAddress());
			} catch (BindException e) {
				JOptionPane.showMessageDialog(hostFrame, "Port " + port + " is already in use");
				return;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			spinner.setEnabled(false);
			createLobby.setText("Close");
			serverStatus.setText("online");
			serverStatus.setForeground(Color.GREEN);
			connectionStatus.setVisible(true);
			spinningWheel.setVisible(true);
			connectionLabel.setVisible(false);
			System.out.println("port " + port + " opened");
			serverSocket.setReuseAddress(true);

			//while(true){
			//sock = serverSocket.accept();
			//System.out.println("Someone has made socket connection");
			//test = "Hi";

			//client = new OneConnection(sock);
			//String s = client.getRequest();
			//}
			//sock.close();
			//System.out.println(sock.isClosed());
			// TODO Auto-generated catch block
		}

		public void startListening() throws Exception{
			while(true){
				sock = serverSocket.accept();
				incomingRequest = true;
				System.out.println("Someone has made socket connection");
				test = "Hi";

				client = new OneConnection(sock);
				String s = client.getRequest();
			}
		}

		class OneConnection {
			Socket sock;
			BufferedReader in = null;
			DataOutputStream out = null;

			OneConnection(Socket sock) throws Exception {
				this.sock = sock;
				in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				out = new DataOutputStream(sock.getOutputStream());
			}

			String getRequest() throws Exception {
				String s = null;
				while ((s = in.readLine()) != null) {
					System.out.println("got: " + s);
					if(s.startsWith("request:")){
						connectionStatus.setVisible(false);
						spinningWheel.setVisible(false);
						String[] string = s.split(":");
						requestLabel.setText(string[1] + " would like to join");
						requestLabel.setVisible(true);
						confirmButton.setVisible(true);
						declineButton.setVisible(true);
						//player2Name = string[1];
						pregamePlayerPane.player2Name.setText(string[1]);
					}
					else if(s.equals("quit")){
						if(confirmButton.isVisible()){
							requestLabel.setVisible(false);
							confirmButton.setVisible(false);
							declineButton.setVisible(false);
							connectionStatus.setVisible(true);
							spinningWheel.setVisible(true);
						}
						else if(pregamePane.isVisible()){
							pregamePane.setVisible(false);
							closeServer();
						}
					}
					else if(s.startsWith("color:")){
						String[] string = s.split(":");
						Color color = Color.decode(string[1]);
						pregamePlayerPane.player2Pane.setColor(color);
					}
					else if(s.startsWith("nameEdit:")){
						if(s.length() > 9){
							String[] string = s.split(":");
							pregamePlayerPane.player2Name.setText(string[1]);
						}
					}
					else if(s.startsWith("move,")){
						String[] string = s.split(",");
						Dots.hostMatch.gameFrame.scorePane.makeMove(2, new Point(Integer.parseInt(string[1]), Integer.parseInt(string[2])), string[3], true);
					}
					//JOptionPane.showConfirmDialog(hostFrame, string[1] + " would like to join your lobby", "Incoming connection", JOptionPane.YES_NO_OPTION);
					//String[] string = s.split(",");
					//JOptionPane.showMessageDialog(hostFrame, string[0] + " says \"" + string[1] + "\"");
				}
				return s;
			}

			public void sendMessage(String string){
				PrintStream printStream = new PrintStream(out, true);
				printStream.println(string);
			}



		}
	}

}
