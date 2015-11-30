package sorryclient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import HostServer.Client;
import HostServer.JSONCode;
import customUI.ClearPanel;
import customUI.PaintedButton;
import customUI.PaintedPanel;
import library.FontLibrary;
import library.ImageLibrary;

public class ColorSelector extends PaintedPanel {
	
	private static final long serialVersionUID = 1900724217285760485L;
	
	private Color selection;
	private final int numOptions = 4;
	private final PaintedButton[] optionButtons;
	
	public boolean start = false;
	
	private final PaintedButton confirmButton;
	
	private Client client;
	
	private final static String selectColorString = "Select your color";
	
	public final static String[] colorNames = {"Red", "Blue", "Yellow", "Green"};
	private final static Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
	
	private final static Insets spacing = new Insets(60,80,60,80);
	
	private Map<String,Object> msg;
	int buttonSelection;

	private SorryTimer timer;

	private ClientPanel cp;
	
	public void startTimer(){
		timer.start();
	}

	public Color getPlayerColor() {
		return selection;
	}
	
	public void sendMsg(){
		msg = new TreeMap<String,Object>();
		msg.put("colorSelect", buttonSelection);
		msg.put("start", start);
		System.out.println("Client " + client);
		client.sendMsg(JSONCode.encode(msg));
	}
	
	class ColorAction implements ActionListener{
		int id;
		private SorryTimer timer;
		public ColorAction(int id, SorryTimer timer){
			this.id = id;
			this.timer = timer;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			buttonSelection = id;
			sendMsg();
			timer.kill();
		}		
	}
	
	public void buttonEnable(Map<String,Object> info){
		if (!info.containsKey("button0")) return;
		optionButtons[0].setEnabled((boolean)info.get("button0"));
		optionButtons[1].setEnabled((boolean)info.get("button1"));
		optionButtons[2].setEnabled((boolean)info.get("button2"));
		optionButtons[3].setEnabled((boolean)info.get("button3"));
		confirmButton.setEnabled((boolean)info.get("confirm"));
	}
	
	public void setClient(Client c){
		client = c;
	}
	
	public ColorSelector(ActionListener confirmAction, Image inImage, ClientPanel cp) {
		super(inImage,true);
		this.cp = cp;
		confirmButton = new PaintedButton(
				"Confirm",
				ImageLibrary.getImage("images/buttons/grey_button00.png"),
				ImageLibrary.getImage("images/buttons/grey_button01.png"),
				22
				);
		confirmButton.addActionListener(confirmAction);
		confirmButton.setEnabled(false);
		
		JLabel selectColorLabel = new JLabel(selectColorString);
		selectColorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		selectColorLabel.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 28));
		
		ClearPanel topPanel = new ClearPanel();
		topPanel.setBorder(new EmptyBorder(spacing));
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		topPanel.setAlignmentX(CENTER_ALIGNMENT);
		JLabel timeLabel = new JLabel("time 0:30");
		timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		topPanel.add(timeLabel, Component.CENTER_ALIGNMENT);
		timeLabel.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 28));
		topPanel.add(Box.createVerticalStrut(20), Component.CENTER_ALIGNMENT);

		topPanel.add(selectColorLabel, Component.CENTER_ALIGNMENT);
		
		ClearPanel centerPanel = new ClearPanel();
		centerPanel.setLayout(new GridLayout(2,2,10,10));
		Font buttonFont = new Font("",Font.BOLD,22);
		optionButtons = new PaintedButton[numOptions];
		timer = new SorryTimer(timeLabel,this);
		for(int i = 0; i < numOptions; ++i) {
			optionButtons[i] = new PaintedButton(
					colorNames[i],
					ImageLibrary.getImage("images/buttons/"+colorNames[i].toLowerCase()+"_button00.png"),
					ImageLibrary.getImage("images/buttons/"+colorNames[i].toLowerCase()+"_button01.png"),
					22
					);
			buttonSelection = i;
			optionButtons[i].addActionListener(new ColorAction(i,timer));
			optionButtons[i].setFont(buttonFont);
			centerPanel.add(optionButtons[i]);
		}
		centerPanel.setBorder(new EmptyBorder(new Insets(0, 80, 0, 80)));
		
		ClearPanel bottomPanel = new ClearPanel();
		bottomPanel.setLayout(new GridLayout(1,3));
		bottomPanel.setBorder(new EmptyBorder(spacing));
		bottomPanel.add(Box.createGlue());
		bottomPanel.add(Box.createGlue());
		bottomPanel.add(confirmButton);
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		add(topPanel);
		topPanel.setAlignmentX(CENTER_ALIGNMENT);
		add(centerPanel);
		add(bottomPanel);
		
	}
	
	public void outTime(){
		cp.quit();
		client.close();
	}
}
