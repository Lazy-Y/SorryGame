package score;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import HostServer.JSONCode;

public class Score {
	
	private static final String filePath = "src/score/scores";
	
	private static JTable scoreTable;
	private static DefaultTableModel tableModel;
	private static final int NAME_COLUMN = 0;
	private static final int SCORE_COLUMN = 1;
	
	private JDialog scoreDisplay;
	
	public Score() {
//		Object[] tableHeaders = new Object[] {"Name", "Score"};
//		tableModel = new DefaultTableModel(tableHeaders,0) {
//			private static final long serialVersionUID = -2100979802046466684L;
//			@Override
//			public boolean isCellEditable(int row, int column) {
//				return false;
//			}
//		};
//		try {
//			Scanner sc = new Scanner(new File(filePath));
//			while(sc.hasNext()) {
//				Object[] row = {sc.next(),sc.nextInt()};
//				tableModel.addRow(row);
//			}
//			sc.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		Socket s = null;
		try {
			s = new Socket("localhost", 9876);
			
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

			Object[] tableHeaders = new Object[] {"Name", "Score"};
			DefaultTableModel tableModel = new DefaultTableModel(tableHeaders,0) {
				private static final long serialVersionUID = -2100979802046466684L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
			Vector<Object[]> table = (Vector<Object[]>) ois.readObject();
			tableModel = new DefaultTableModel(tableHeaders,0);
			
			scoreTable = new JTable(tableModel);

			for (Object[] obj : table){
				tableModel.addRow(obj);
			}
			
			scoreDisplay = new JDialog();
			scoreDisplay.setTitle("Scores");
			scoreDisplay.setModal(true);
			scoreDisplay.setSize(200, 300);
			JScrollPane jsp = new JScrollPane(scoreTable);
			jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scoreDisplay.add(jsp);
			PrintWriter pw = new PrintWriter(s.getOutputStream());
			Map<String, Object>map = new TreeMap<String,Object>();
			map.put("type", "ok");
			pw.println(JSONCode.encode(map));
			pw.flush();
		} 
		catch (IOException | ClassNotFoundException e) {
			System.out.println("Score: "+e.getMessage());
			JDialog dialog = new JDialog();
			dialog.setTitle("Sorry");
			dialog.setSize(200, 300);
			dialog.add(new JLabel(e.getMessage()));
		} 
		finally {
			scoreTable = null;
			tableModel = null;
		}
	}
	
	public void add(String name, int score) {
		boolean placed = false;
		for(int i = 0; i < tableModel.getRowCount(); i++) {
			if ((int)tableModel.getValueAt(i, SCORE_COLUMN) < score) {
				Object[] row = {name,score};
				tableModel.insertRow(i, row);
				placed = true;
				break;
			}
		}
		if(!placed) {
			Object[] row = {name,score};
			tableModel.addRow(row);
		}
		save();
	}
	
	private static void save() {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filePath, "UTF-8");
			for(int i = 0; i < tableModel.getRowCount(); i++) {
				writer.print(tableModel.getValueAt(i, NAME_COLUMN));
				writer.print(" ");
				writer.println(tableModel.getValueAt(i, SCORE_COLUMN));
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.out.println("File not found: " + filePath);
			System.out.println("Could not save scores!");
		} finally {
			if(writer != null) writer.close();
		}
	}

	public void display() {
		System.out.println("Display " + scoreDisplay);
		if (scoreDisplay == null) {
			JOptionPane.showMessageDialog(null, "Cannot find server","Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		scoreDisplay.setLocationRelativeTo(null);
		scoreDisplay.setVisible(true);
		scoreDisplay = null;
	}
}
