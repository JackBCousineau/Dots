package com.gmail.jackbcousineau;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileSystemView;

import com.gmail.jackbcousineau.Dots.MainMenuFrame.PlayerType;
import com.gmail.jackbcousineau.Match.MatchType;

public class Dots {

	public static JFrame mainMenuFrame;
	public static Match hostMatch, clientMatch;
	public static MatchServer matchServer;
	public static MatchClient matchClient;

	public static Image spinningWheel;

	public static void main(String args[]) throws Exception{
		new Dots();
	}
	
	private static Image getImageResource(String string){
		return new ImageIcon(Dots.class.getResource("/com/gmail/jackbcousineau/resources/" + string)).getImage();
	}

	Dots() throws IOException{
		mainMenuFrame = new MainMenuFrame();
		//Match.dot = new ImageIcon(FileSystemView.getFileSystemView().getHomeDirectory() + "/Desktop/Dots/images/dot.png").getImage();
		Match.dot = getImageResource("images/dot.png");
		Match.leftArrow = getImageResource("images/left_arrow.png");
		Match.rightArrow = getImageResource("images/right_arrow.png");
		Match.buckets = getImageResource("buckets/buckets.png");
		spinningWheel = getImageResource("images/spinning_wheel.gif");
	}

	static class MainMenuFrame extends JFrame{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1787367951887228649L;

		public enum PlayerType{
			PLAYER,
			AI;
		}

		static JPanel pane, slot1, slot2;
		static PlayerPane player1Pane, player2Pane;
		static SpringLayout layout = new SpringLayout();
		static JLabel rowsLabel, columnsLabel, playersLabel;
		static JButton createGameButton, switchButton, joinButton, hostButton;
		static JTextField rowsTextField, columnsTextField;
		static JSpinner rowsSpinner, columnsSpinner;
		static JComboBox<String> playersBox;

		public MainMenuFrame() throws IOException{
			super("Dots");
			pane = new JPanel();
			getContentPane().add(pane);
			setMinimumSize(new Dimension(340, 225));
			setLocationRelativeTo(null);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			pane.setLayout(layout);

			createGameButton = new JButton("Create Game");
			pane.add(createGameButton);
			createGameButton.setFocusable(false);
			createGameButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent event){
					createGame();
				}
			});

			joinButton = new JButton("Join");
			pane.add(joinButton);
			joinButton.setFocusable(false);
			joinButton.setEnabled(false);
			joinButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e){
					Thread one=new Thread(){
						public void run() {
							try {
								if(matchClient == null) matchClient = new MatchClient();
								else matchClient.clientFrame.setVisible(true);
								//matchClient.sendMessage("HELLO");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}  
					};
					one.start();
				}
			});

			hostButton = new JButton("Host");
			pane.add(hostButton);
			hostButton.setFocusable(false);
			hostButton.setEnabled(false);
			hostButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						//onlineMatch = createGame();
						//if(onlineMatch != null){
						if(matchServer == null) matchServer = new MatchServer();
						else matchServer.hostFrame.setVisible(true);
						playersBox.setEnabled(false);
						player2Pane.nameField.setEnabled(false);
						player2Pane.colorChooser.setEnabled(false);
						//}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			/*columnsUpButton = new JButton("^");
			pane.add(columnsUpButton);
			columnsDownButton = new JButton("v");
			pane.add(columnsDownButton);
			 */

			rowsSpinner = new JSpinner(new SpinnerNumberModel(5, 2, 50, 1));
			pane.add(rowsSpinner);
			columnsSpinner = new JSpinner(new SpinnerNumberModel(5, 2, 50, 1));
			pane.add(columnsSpinner);

			playersBox = new JComboBox<String>();
			playersBox.addItem("1 Player");
			playersBox.addItem("2 Players");
			playersBox.addItem("AI vs. AI");
			pane.add(playersBox);
			playersBox.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent event){
					@SuppressWarnings("unchecked")
					JComboBox<String> box = (JComboBox<String>)event.getSource();
					joinButton.setEnabled(false);
					hostButton.setEnabled(false);
					if(box.getSelectedItem() == "AI vs. AI"){
						player1Pane.nameField.setEditable(false);
						player2Pane.nameField.setEditable(false);
						player1Pane.nameField.setText("AI 1");
						player2Pane.nameField.setText("AI 2");
						player1Pane.nameField.setForeground(Color.DARK_GRAY);
						player2Pane.nameField.setForeground(Color.DARK_GRAY);
					}
					else if(!player1Pane.nameField.isEditable()){
						player1Pane.nameField.setEditable(true);
						player1Pane.nameField.setText("Player 1");
						player1Pane.nameField.setForeground(Color.BLACK);
					}
					if(box.getSelectedItem() == "1 Player"){
						player2Pane.nameField.setEditable(false);
						player2Pane.nameField.setText("AI");
						player2Pane.nameField.setForeground(Color.DARK_GRAY);
					}
					else if(box.getSelectedItem() == "2 Players"){
						player2Pane.nameField.setEditable(true);
						player2Pane.nameField.setText("Player 2");
						player2Pane.nameField.setForeground(Color.BLACK);
						joinButton.setEnabled(true);
						hostButton.setEnabled(true);
					}
				}
			});

			rowsLabel = new JLabel("Rows");
			pane.add(rowsLabel);
			columnsLabel = new JLabel("Columns");
			pane.add(columnsLabel);
			playersLabel = new JLabel("Players");
			pane.add(playersLabel);

			slot1 = new JPanel();
			final SpringLayout slot1Layout = new SpringLayout();
			slot1.setLayout(slot1Layout);
			slot2 = new JPanel();
			final SpringLayout slot2Layout = new SpringLayout();
			slot2.setLayout(slot2Layout);
			player1Pane = new PlayerPane("Player 1", PlayerType.PLAYER, true, true, 0);
			//player1Pane.setBackground(Color.CYAN);
			slot1.setBorder(BorderFactory.createEtchedBorder());
			player2Pane = new PlayerPane("AI", PlayerType.AI, true, true, 0);
			//player2Pane.setBackground(Color.ORANGE);
			slot2.setBorder(BorderFactory.createEtchedBorder());
			pane.add(slot1);
			pane.add(slot2);
			slot1.add(player1Pane);
			place(player1Pane, slot1, slot1Layout);
			place(player2Pane, slot1, slot1Layout);
			slot2.add(player2Pane);
			place(player2Pane, slot2, slot2Layout);
			place(player1Pane, slot2, slot2Layout);

			switchButton = new JButton();
			switchButton.setIcon(new ImageIcon(getImageResource("images/swap.png")));
			//switchButton.setIcon(new ImageIcon(this.getClass().getResource("Dots/resources/images/swap.png")));
			//ImageIcon imageIcon = new ImageIcon(getClass().getResource("/resources/images/swap.png"));
			//File file = new File(getClass().getClassLoader().getResource("swap.png").getFile());
			//InputStream url = ClassLoader.getSystemClassLoader().getResourceAsStream("resources/images/swap.png");
			//Icon icon = new ImageIcon(url);
			//InputStream imageInputStream = getClass().getClassLoader().getResourceAsStream("resources/images/swap.png");
			//ImageInputStream imageInput = ImageIO.createImageInputStream(imageInputStream);
			//BufferedImage bufImage = ImageIO.read(imageInput);
			//InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream("resources/images/swap.png");
			//System.out.println(stream == null);
			//System.out.println(getClass().getClassLoader().getParent().);
			//URL url = this.getClass().getClassLoader().getResource("swap.png");
			//System.out.println(url.toString());
			//InputStream in = Dots.class.getClass().getResourceAsStream("Dots/swap.png");
			//System.out.println(in == null);
			//ImageIcon icon= new ImageIcon(ImageIO.read(stream));
			//ImageIcon imageIcon = new ImageIcon(imageData, "description about image");
			pane.add(switchButton);
			switchButton.setFocusable(false);
			switchButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent event){
					for(Component component : slot1.getComponents()){
						if(component.equals(player1Pane)){
							slot1Layout.removeLayoutComponent(player1Pane);
							slot2Layout.removeLayoutComponent(player2Pane);
							slot1.add(player2Pane);
							slot2.add(player1Pane);
							place(player2Pane, slot1, slot1Layout);
							place(player1Pane, slot2, slot2Layout);
						}
						else if(component.equals(player2Pane)){
							slot1Layout.removeLayoutComponent(player2Pane);
							slot2Layout.removeLayoutComponent(player1Pane);
							slot1.add(player1Pane);
							slot2.add(player2Pane);
							place(player1Pane, slot1, slot1Layout);
							place(player2Pane, slot2, slot2Layout);
						}
						slot1.updateUI();
						slot2.updateUI();
					}
				}
			});

			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, columnsSpinner, 2, SpringLayout.HORIZONTAL_CENTER, pane);
			layout.putConstraint(SpringLayout.WEST, columnsSpinner, -20, SpringLayout.HORIZONTAL_CENTER, pane);
			layout.putConstraint(SpringLayout.NORTH, columnsSpinner, 10, SpringLayout.NORTH, pane);
			layout.putConstraint(SpringLayout.SOUTH, columnsSpinner, 42, SpringLayout.NORTH, pane);
			layout.putConstraint(SpringLayout.WEST, columnsSpinner, -25, SpringLayout.HORIZONTAL_CENTER, pane);

			/*layout.putConstraint(SpringLayout.NORTH, columnsUpButton, 3, SpringLayout.NORTH, columnsTextField);
			layout.putConstraint(SpringLayout.SOUTH, columnsUpButton, 0, SpringLayout.VERTICAL_CENTER, columnsTextField);
			layout.putConstraint(SpringLayout.WEST, columnsUpButton, -4, SpringLayout.EAST, columnsTextField);
			layout.putConstraint(SpringLayout.EAST, columnsUpButton, 10, SpringLayout.EAST, columnsTextField);

			layout.putConstraint(SpringLayout.SOUTH, columnsDownButton, -3, SpringLayout.SOUTH, columnsTextField);
			layout.putConstraint(SpringLayout.NORTH, columnsDownButton, 0, SpringLayout.VERTICAL_CENTER, columnsTextField);
			layout.putConstraint(SpringLayout.WEST, columnsDownButton, -4, SpringLayout.EAST, columnsTextField);
			layout.putConstraint(SpringLayout.EAST, columnsDownButton, 10, SpringLayout.EAST, columnsTextField);
			 */
			layout.putConstraint(SpringLayout.EAST, rowsSpinner, -30, SpringLayout.WEST, columnsSpinner);
			layout.putConstraint(SpringLayout.WEST, rowsSpinner, -85, SpringLayout.WEST, columnsSpinner);
			layout.putConstraint(SpringLayout.NORTH, rowsSpinner, 0, SpringLayout.NORTH, columnsSpinner);
			layout.putConstraint(SpringLayout.SOUTH, rowsSpinner, 0, SpringLayout.SOUTH, columnsSpinner);

			layout.putConstraint(SpringLayout.NORTH, rowsLabel, -3, SpringLayout.SOUTH, rowsSpinner);
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, rowsLabel, -7, SpringLayout.HORIZONTAL_CENTER, rowsSpinner);

			layout.putConstraint(SpringLayout.NORTH, columnsLabel, -3, SpringLayout.SOUTH, columnsSpinner);
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, columnsLabel, -4, SpringLayout.HORIZONTAL_CENTER, columnsSpinner);

			layout.putConstraint(SpringLayout.NORTH, playersLabel, 1, SpringLayout.SOUTH, playersBox);
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, playersLabel, 0, SpringLayout.HORIZONTAL_CENTER, playersBox);

			layout.putConstraint(SpringLayout.WEST, playersBox, 17, SpringLayout.EAST, columnsSpinner);
			layout.putConstraint(SpringLayout.VERTICAL_CENTER, playersBox, 0, SpringLayout.VERTICAL_CENTER, columnsSpinner);
			//layout.putConstraint(SpringLayout.)

			layout.putConstraint(SpringLayout.WEST, slot1, 15, SpringLayout.WEST, pane);
			layout.putConstraint(SpringLayout.EAST, slot1, -21, SpringLayout.HORIZONTAL_CENTER, pane);
			layout.putConstraint(SpringLayout.NORTH, slot1, -70, SpringLayout.SOUTH, slot1);
			layout.putConstraint(SpringLayout.SOUTH, slot1, -20, SpringLayout.NORTH, createGameButton);

			layout.putConstraint(SpringLayout.EAST, slot2, -15, SpringLayout.EAST, pane);
			layout.putConstraint(SpringLayout.WEST, slot2, 21, SpringLayout.HORIZONTAL_CENTER, pane);
			layout.putConstraint(SpringLayout.NORTH, slot2, -70, SpringLayout.SOUTH, slot2);
			layout.putConstraint(SpringLayout.SOUTH, slot2, -20, SpringLayout.NORTH, createGameButton);

			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, switchButton, 0, SpringLayout.HORIZONTAL_CENTER, pane);
			layout.putConstraint(SpringLayout.VERTICAL_CENTER, switchButton, 0, SpringLayout.VERTICAL_CENTER, slot2);



			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, createGameButton, 0, SpringLayout.HORIZONTAL_CENTER, pane);
			layout.putConstraint(SpringLayout.SOUTH, createGameButton, -10, SpringLayout.SOUTH, pane);

			layout.putConstraint(SpringLayout.EAST, joinButton, -18, SpringLayout.WEST, createGameButton);
			layout.putConstraint(SpringLayout.SOUTH, joinButton, -10, SpringLayout.SOUTH, pane);

			layout.putConstraint(SpringLayout.WEST, hostButton, 18, SpringLayout.EAST, createGameButton);
			layout.putConstraint(SpringLayout.SOUTH, hostButton, -10, SpringLayout.SOUTH, pane);

			setResizable(false);
			setVisible(true);
		}

		private Match createGame(){
			Match match = null;
			if(playersBox.getSelectedItem() == "AI vs. AI"){
				match = new Match(MatchType.CVC, (Integer)columnsSpinner.getValue(), (Integer)rowsSpinner.getValue(),
						"AI 1", getSlotPane(1).color, "AI 2", getSlotPane(2).color, null);
			}
			else if(playersBox.getSelectedItem() == "1 Player"){
				if(getSlotPane(1).nameField.getText().equals("AI")&&getSlotPane(2).nameField.getText().equals("AI")) JOptionPane.showMessageDialog(mainMenuFrame, "You cannot name yourself \"AI\"", "Uh-oh...", JOptionPane.PLAIN_MESSAGE);
				else match = new Match(MatchType.PVC, (Integer)columnsSpinner.getValue(), (Integer)rowsSpinner.getValue(),
						getSlotPane(1).nameField.getText(), getSlotPane(1).color, getSlotPane(2).nameField.getText(), getSlotPane(2).color, null);
			}
			else if(playersBox.getSelectedItem() == "2 Players"){
				match = new Match(MatchType.PVP, (Integer)columnsSpinner.getValue(), (Integer)rowsSpinner.getValue(),
						getSlotPane(1).nameField.getText(), getSlotPane(1).color, getSlotPane(2).nameField.getText(), getSlotPane(2).color, null);
			}
			return match;
		}

		private PlayerPane getSlotPane(int number){
			if(number == 1) return (PlayerPane)slot1.getComponent(0);
			else return (PlayerPane)slot2.getComponent(0);
		}

		private void place(JPanel pane, JPanel parentPane, SpringLayout layout){
			layout.putConstraint(SpringLayout.NORTH, pane, 0, SpringLayout.NORTH, parentPane);
			layout.putConstraint(SpringLayout.SOUTH, pane, 0, SpringLayout.SOUTH, parentPane);
			layout.putConstraint(SpringLayout.EAST, pane, 0, SpringLayout.EAST, parentPane);
			layout.putConstraint(SpringLayout.WEST, pane, 0, SpringLayout.WEST, parentPane);
		}
	}

}

class PlayerPane extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8243850761630360848L;
	public JTextField nameField;
	public JLabel colorLabel = new JLabel("Color:");
	public JButton colorChooser = new JButton();
	public Color color;
	public int player;

	public PlayerPane(String name, PlayerType playerType, boolean useLayout, boolean enableEditing, int player){
		this.player = player;
		SpringLayout layout = new SpringLayout();
		if(useLayout){
			setLayout(layout);

			nameField = new JTextField(name);
			add(nameField);
			if(name == "AI"){
				nameField.setEditable(false);
				nameField.setForeground(Color.DARK_GRAY);
			}
			layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, nameField, 0, SpringLayout.HORIZONTAL_CENTER, this);
			layout.putConstraint(SpringLayout.NORTH, nameField, 5, SpringLayout.NORTH, this);
			layout.putConstraint(SpringLayout.WEST, nameField, 33, SpringLayout.WEST, this);
			layout.putConstraint(SpringLayout.EAST, nameField, -34, SpringLayout.EAST, this);
		}

		add(colorLabel);

		//ImageIcon image = new ImageIcon();

		/*				BufferedImage bi = new BufferedImage(
				image.getIconWidth(),
				image.getIconHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.createGraphics();
		// paint the Icon to the BufferedImage.
		image.paintIcon(null, g, 0,0);
		g.dispose();
		//image.getImage().getGraphics().setColor(Color.BLACK);
		Graphics2D    graphics = bi.createGraphics();

		graphics.setPaint ( Color.BLACK );
		graphics.fillRect ( 0, 0, bi.getWidth(), bi.getHeight() );
		image.paint
		 *///colorChooser = new JButton(image);
		setColor(Color.BLUE);
		if(name == "Player 2"||name == "AI") setColor(Color.RED); //orange

		//add(new JLabel(new ImageIcon(bi)));
		//add(new JLabel(new ImageIcon(FileSystemView.getFileSystemView().getHomeDirectory() + "/Desktop/swap.png")));

		add(colorChooser);
		colorChooser.setFocusable(false);
		if(enableEditing){
			colorChooser.addActionListener(new ColorWheelListener(this));
		}
		//colorChooser.addActionListener(new ActionListener(){
		//
		//@Override
		//public void actionPerformed(ActionEvent e) {
		//// TODO Auto-generated method stub
		//
		//}
		//	
		//}
		//);

		layout.putConstraint(SpringLayout.WEST, colorLabel, 31, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, colorLabel, 5, SpringLayout.SOUTH, nameField);
		layout.putConstraint(SpringLayout.WEST, colorChooser, 1, SpringLayout.EAST, colorLabel);
		layout.putConstraint(SpringLayout.VERTICAL_CENTER, colorChooser, 1, SpringLayout.VERTICAL_CENTER, colorLabel);
	}

	public void setColor(Color color){
		this.color = color;
		BufferedImage bi = new BufferedImage(15, 11, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, 20, 20);
		g.setComposite(AlphaComposite.DstIn);
		g.drawImage(bi, 0, 0, 20, 20, 0, 0, 20, 20, null);
		colorChooser.setIcon(new ImageIcon(bi));
	}

	private class ColorWheelListener implements ActionListener{

		PlayerPane pane;

		public ColorWheelListener(PlayerPane pane){
			this.pane = pane;
		}

		@Override
		public void actionPerformed(ActionEvent event){
			//Color color = JColorChooser.showDialog(pane, "Pick a color", pane.color);
			//pane.setColor(color);
			final JColorChooser colorChooser = new JColorChooser();
			colorChooser.setColor(color);
			JDialog dialog = JColorChooser.createDialog(pane,
					"Pick a color", true, colorChooser
					, new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent event){
					Color newColor = colorChooser.getColor();
					pane.setColor(newColor);
					if(player != 0){
						if(player == 1){
							Dots.matchServer.server.client.sendMessage("color:" + newColor.getRGB());
						}
						else if(player == 2){
							Dots.matchClient.thread.sendMessage("color:" + newColor.getRGB());
						}
					}
				}
			}
			, new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent event){
				}
			}
					);
			dialog.setVisible(true);
		}
	}
}
class PregamePlayerPane extends JPanel{

	public PlayerPane player1Pane, player2Pane;
	public JPanel horizontalSeparator, verticalSeparator;
	public JLabel youLabel, opponentLabel;
	public JTextField player1Name, player2Name;

	public PregamePlayerPane(int player){
		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		player1Name = new JTextField("Player 1");
		player2Name = new JTextField("Player 2");
		if(player == 1){
			player1Pane = new PlayerPane("", PlayerType.PLAYER, false, true, 1);
			player2Pane = new PlayerPane("Player 2", PlayerType.PLAYER, false, false, 2);
			player2Name.setEditable(false);
		}
		else if(player == 2){
			player1Pane = new PlayerPane("", PlayerType.PLAYER, false, false, 1);
			player2Pane = new PlayerPane("Player 2", PlayerType.PLAYER, false, true, 2);
			player1Name.setEditable(false);
		}
		add(player1Pane);
		add(player2Pane);
		//setBackground(Color.BLUE);
		player1Pane.setVisible(true);
		horizontalSeparator = new JPanel();
		horizontalSeparator.setBackground(Color.GRAY);
		add(horizontalSeparator);
		verticalSeparator = new JPanel();
		verticalSeparator.setBackground(Color.GRAY);
		add(verticalSeparator);
		youLabel = new JLabel("You");
		add(youLabel);
		opponentLabel = new JLabel("Opponent");
		add(opponentLabel);
		add(player1Name);
		add(player2Name);
		//layout.putConstraint(SpringLayout.WEST, player1Pane, 0, SpringLayout.WEST, this);
		//layout.putConstraint(SpringLayout.EAST, player1Pane, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, horizontalSeparator, 0, SpringLayout.HORIZONTAL_CENTER, this);
		layout.putConstraint(SpringLayout.NORTH, horizontalSeparator, 2, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, horizontalSeparator, -5, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.EAST, horizontalSeparator, 1, SpringLayout.HORIZONTAL_CENTER, this);

		layout.putConstraint(SpringLayout.WEST, verticalSeparator, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, verticalSeparator, 15, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.SOUTH, verticalSeparator, 16, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, verticalSeparator, -5, SpringLayout.EAST, this);

		layout.putConstraint(SpringLayout.SOUTH, youLabel, 0, SpringLayout.NORTH, verticalSeparator);

		layout.putConstraint(SpringLayout.SOUTH, opponentLabel, -1, SpringLayout.NORTH, verticalSeparator);

		layout.putConstraint(SpringLayout.NORTH, player1Name, 20, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.EAST, player1Name, -29, SpringLayout.HORIZONTAL_CENTER, this);
		layout.putConstraint(SpringLayout.WEST, player1Name, 33, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, player2Name, 20, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, player2Name, 29, SpringLayout.HORIZONTAL_CENTER, this);
		layout.putConstraint(SpringLayout.EAST, player2Name, -33, SpringLayout.EAST, this);

		layout.putConstraint(SpringLayout.NORTH, player1Pane, -2, SpringLayout.SOUTH, player1Name);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, player1Pane, 0, SpringLayout.HORIZONTAL_CENTER, player1Name);

		layout.putConstraint(SpringLayout.NORTH, player2Pane, -2, SpringLayout.SOUTH, player2Name);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, player2Pane, 0, SpringLayout.HORIZONTAL_CENTER, player2Name);
		//layout.putConstraint(SpringLayout.SOUTH, player1Pane, 35, SpringLayout.SOUTH, player1Name);
		if(player == 1){
			layout.putConstraint(SpringLayout.EAST, youLabel, -50, SpringLayout.WEST, horizontalSeparator);
			layout.putConstraint(SpringLayout.WEST, opponentLabel, 30, SpringLayout.EAST, horizontalSeparator);
			/*player1Name.addKeyListener(new KeyListener(){

				@Override
				public void keyTyped(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent) System.out.println("ENTER PRESSED")
					Dots.matchServer.server.client.sendMessage("nameEdit:" + player1Name.getText());
				}

				@Override
				public void keyPressed(KeyEvent e) {
				}

				@Override
				public void keyReleased(KeyEvent e) {
				}
			});*/
			player1Name.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					Dots.matchServer.server.client.sendMessage("nameEdit:" + player1Name.getText());
					Dots.matchServer.hostFrame.requestFocusInWindow();
				}
			});
		}
		else if(player == 2){
			layout.putConstraint(SpringLayout.WEST, youLabel, 50, SpringLayout.EAST, horizontalSeparator);
			layout.putConstraint(SpringLayout.EAST, opponentLabel, -30, SpringLayout.WEST, horizontalSeparator);
			player2Name.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					Dots.matchClient.thread.sendMessage("nameEdit:" + player2Name.getText());
					Dots.matchClient.clientFrame.requestFocusInWindow();
				}
			});
		}
	}
}