package sorryclient;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import customUI.PaintedButton;
import customUI.PaintedPanel;
import library.ImageLibrary;

public class MainMenu extends PaintedPanel{
	private static final long serialVersionUID = 3609831945869059312L;
	
	private final JButton start;

	private JButton host;
	
	public MainMenu(/*ActionListener startAction, */ActionListener hostAction,
			ActionListener joinAction, Image inImage){
		super(inImage,true);
		start = new PaintedButton(
				"JOIN",
				ImageLibrary.getImage("images/buttons/grey_button00.png"),
				ImageLibrary.getImage("images/buttons/grey_button01.png"),
				22
				);
		start.addActionListener(joinAction);
		
		Image titleImage = ImageLibrary.getImage("images/sorry.png");
		PaintedPanel titlePanel = new PaintedPanel(titleImage);
		
		JPanel buttonPane = new JPanel(new FlowLayout());
		buttonPane.setMinimumSize(new Dimension(200,100));
		start.setPreferredSize(new Dimension(150, 50));
		host = new PaintedButton(
				"HOST",
				ImageLibrary.getImage("images/buttons/grey_button00.png"),
				ImageLibrary.getImage("images/buttons/grey_button01.png"),
				22
				);
		host.addActionListener(hostAction);
		host.setPreferredSize(new Dimension(150,50));
		buttonPane.add(host);
		buttonPane.add(start);
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.ipadx = titleImage.getWidth(null);
		gbc.ipady = titleImage.getHeight(null);
		gbc.insets = new Insets(40,40,40,40);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 1;
		add(titlePanel,gbc);
		gbc.ipadx = 	300;
		gbc.ipady = 100;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 2;
		add(buttonPane,gbc);
	}
	
}
