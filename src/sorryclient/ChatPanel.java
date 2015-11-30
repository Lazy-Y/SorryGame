package sorryclient;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.Color;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import HostServer.Client;
import HostServer.JSONCode;
import customUI.PaintedButton;
import library.FontLibrary;
import library.ImageLibrary;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JTextArea;
import javax.swing.JEditorPane;

public class ChatPanel extends JPanel {
	private JTextField textField;
	private JScrollPane scrollPane;
	private Client client;
	private JTextPane txtpnGameBegin;
	private DefaultStyledDocument document;

	/**
	 * Create the panel.
	 */
	public ChatPanel(Client c) {
		client = c;
		client.setChatPanel(this);
		setPreferredSize(new Dimension(400, 100));
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JButton send = new PaintedButton(
				"SEND",
				ImageLibrary.getImage("images/buttons/grey_button00.png"),
				ImageLibrary.getImage("images/buttons/grey_button01.png"),
				22
				);
		send.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 28));

		send.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				client.sendMsg(createMsg());
			}
		});
		
		panel.add(send, BorderLayout.EAST);
		
		textField = new JTextField();
		textField.setForeground(Color.WHITE);
		textField.setBackground(Color.BLACK);
		panel.add(textField, BorderLayout.CENTER);
		textField.setColumns(10);
		textField.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 28));

		scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

		document = new DefaultStyledDocument();
		txtpnGameBegin = new JTextPane(document);
		txtpnGameBegin.setEditable(false);
		txtpnGameBegin.setForeground(Color.WHITE);
		txtpnGameBegin.setBackground(Color.BLACK);
		txtpnGameBegin.setFont(FontLibrary.getFont("fonts/kenvector_future_thin.ttf", Font.PLAIN, 28));
		scrollPane.setViewportView(txtpnGameBegin);

	}
	
	private String createMsg(){
		Map<String,Object> map = new TreeMap<String,Object>();
		map.put("type", "msg");
		map.put("content", textField.getText());
		textField.setText("");
		return JSONCode.encode(map);
	}
	public void appendText(String str) {
		System.out.println("Append here");
		txtpnGameBegin.setText(txtpnGameBegin.getText() + str);
		System.out.println("End append");
		txtpnGameBegin.repaint();
	}
	
	public void receiveMsg(String color, String msg, boolean quit){
		System.out.println("ChatPanel receive " + msg);
		StyleContext context = new StyleContext();
		Style style = context.addStyle(color, null);
		if (color.equals("yellow")){
			StyleConstants.setForeground(style, Color.yellow);
		}
		else if (color.equals("red")){
			StyleConstants.setForeground(style, Color.red);
		}
		else if (color.equals("blue")){
			StyleConstants.setForeground(style, Color.blue);
		}
		else{
			StyleConstants.setForeground(style, Color.green);
		}
		try {
			if (!quit) document.insertString(document.getLength(), color + ": ", style);
			else {
				document.insertString(document.getLength(), msg, style); 
				return;
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		style = context.addStyle(msg, null);
		StyleConstants.setForeground(style, Color.white);
		try {
			document.insertString(document.getLength(), msg+"\n", style);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		txtpnGameBegin.setCaretPosition(txtpnGameBegin.getDocument().getLength());
	}
//	private void autoscroll() {
//	 
//		JScrollBar vscroll = scrollPane.getVerticalScrollBar();
//	 
//		int distance_to_bottom = vscroll.getMaximum() - ( vscroll.getValue() + vscroll.getVisibleAmount() );
//	
//		if( 0 == distance_to_bottom ) {
//			vscroll.setValue(vscroll.getMaximum());
//		}
//		scrollPane.setVerticalScrollBar(vscroll);
//	}
}
