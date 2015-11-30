package sorryclient;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import HostServer.Client;
import HostServer.HostServer;
import game.GameManager;
import library.ImageLibrary;

public class ClientPanel extends JPanel {
	public ClientPanel(SorryClientWindow window) {
		scw = window;
	}
	private static final long serialVersionUID = 6415716059554739910L;
	private Client client;
	private SorryClientWindow scw;
	private MainMenu mainMenu;
	private NumPlayerSelector numPlayerSelect;
	private ColorSelector colorSelect;
	private GamePanel gamePanel;
	private PortPanel portPanel;
	private JoinGame joinPanel;
	private GameManager gameManager;
	private ChatPanel chatPanel;
	private HostServer hs;
	int portNumber;
	String address = "localhost";
	
	{
		mainMenu = new MainMenu(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				ClientPanel.this.removeAll();
				ClientPanel.this.add(portPanel);
				ClientPanel.this.revalidate();
				ClientPanel.this.repaint();
			}
		},
		new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				ClientPanel.this.removeAll();
				ClientPanel.this.add(joinPanel);
				ClientPanel.this.revalidate();
				ClientPanel.this.repaint();
			}
		},
		ImageLibrary.getImage("images/panels/grey_panel.png"));
		
		refreshComponents();
		setLayout(new BorderLayout());
		add(mainMenu);
	}
	
	private void refreshComponents() {
		numPlayerSelect = new NumPlayerSelector(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hs.setNumOfPlayers(numPlayerSelect.getNumberOfPlayers());	
				client = new Client("localhost",portNumber,ClientPanel.this);
			}
		},ImageLibrary.getImage("images/panels/grey_panel.png"));
		portPanel = new PortPanel(ImageLibrary.getImage("images/panels/grey_panel.png"),new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					portNumber = Integer.parseInt(portPanel.textField.getText());
				}
				catch (NumberFormatException nfe){
					JOptionPane.showMessageDialog(scw,
						    "Incorrect port number",
						    "Sorry",
						    JOptionPane.ERROR_MESSAGE);
					return;
				}
				hs = new HostServer(numPlayerSelect.getNumberOfPlayers(),
						portNumber, gameManager);
				new Thread(hs).start();
				if (!hs.getServerStatus()){
					JOptionPane.showMessageDialog(scw,
						    "Cannot access this port",
						    "Sorry",
						    JOptionPane.ERROR_MESSAGE);
					return;
				}
				gameManager.setUp(hs);
				ClientPanel.this.removeAll();
				ClientPanel.this.add(numPlayerSelect);
				ClientPanel.this.revalidate();
				ClientPanel.this.repaint();
			}
		});
		joinPanel = new JoinGame(ImageLibrary.getImage("images/panels/grey_panel.png"),new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				try {
					portNumber = Integer.parseInt(joinPanel.textField.getText());
				}
				catch (NumberFormatException nfe){
					JOptionPane.showMessageDialog(scw,
						    "Incorrect port number",
						    "Sorry",
						    JOptionPane.ERROR_MESSAGE);
					return;
				}
				address = joinPanel.IPtextField.getText();
				System.out.println(client);
				client = new Client(address,portNumber,ClientPanel.this);
				System.out.println(client);
			}
		});
		colorSelect = new ColorSelector(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				colorSelect.start = true;
				colorSelect.sendMsg();
			}
		},ImageLibrary.getImage("images/panels/grey_panel.png"),this);
		System.out.println("client 2 "+ this.client);
		colorSelect.setClient(this.client);
		gameManager = new GameManager();
		gamePanel = new GamePanel(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		}, gameManager, ImageLibrary.getImage("images/sorry.png"));
	}
	
	public void quit(){
		ClientPanel.this.removeAll();
		ClientPanel.this.add(mainMenu);
		ClientPanel.this.revalidate();
		ClientPanel.this.repaint();
		if (hs != null) hs.end();
		refreshComponents();
		System.out.println("already quit");
	}
	
	public void SelectColor(boolean status, String msg){
		if (!status){
			JOptionPane.showMessageDialog(scw,
				    msg,
				    "Sorry",
				    JOptionPane.ERROR_MESSAGE);
			return;
		}
		ClientPanel.this.removeAll();
		ClientPanel.this.add(colorSelect);
		colorSelect.setClient(this.client);
		this.client.setCS(colorSelect);
		colorSelect.startTimer();
		ClientPanel.this.revalidate();
		ClientPanel.this.repaint();
	}

	public void startGame(Map<String,Long>playerColor, Integer playerIndex) {
		chatPanel = new ChatPanel(client);
		JPanel mainGame = new JPanel(new BorderLayout());
		mainGame.add(gamePanel,BorderLayout.CENTER);
		mainGame.add(chatPanel,BorderLayout.SOUTH);
		ClientPanel.this.removeAll();
		ClientPanel.this.add(mainGame);
		ClientPanel.this.revalidate();
		ClientPanel.this.repaint();
		System.out.println("start " + playerColor);
		gameManager.infoSetup(playerColor, playerIndex, client);
	}
}
