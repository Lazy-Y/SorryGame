package sorryclient;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import customUI.PaintedButton;
import customUI.PaintedPanel;
import library.FontLibrary;
import library.ImageLibrary;

public class PortPanel extends PaintedPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4191891812890918431L;
	private PaintedButton start;
	JTextField textField;
	private JLabel portLabel;

	/**
	 * Create the panel.
	 */
	public PortPanel(Image inImage, ActionListener startAction) {
		super(inImage,true);
		start = new PaintedButton(
				"START",
				ImageLibrary.getImage("images/buttons/grey_button00.png"),
				ImageLibrary.getImage("images/buttons/grey_button01.png"),
				22
				);
		start.addActionListener(startAction);
		
		JPanel portInput = new JPanel(new FlowLayout());
		portLabel = new JLabel("PORT:");
		portLabel.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 24));
		portLabel.setPreferredSize(new Dimension(100,50));
		portInput.add(portLabel);
		textField = new JTextField();
		textField.setPreferredSize(new Dimension(100,50));
		textField.setFont(FontLibrary.getFont("fonts/kenvector_future.ttf", Font.PLAIN, 24));
		portInput.add(textField);
		textField.setText("6789");
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.ipadx = 100;
		gbc.ipady = 50;
		gbc.insets = new Insets(40,40,40,40);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 1;
		add(portInput,gbc);
		gbc.ipadx = 	50;
		gbc.ipady = 25;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 2;
		add(start,gbc);
	}

}
