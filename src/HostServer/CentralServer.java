package HostServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import simple.parser.ParseException;

public class CentralServer {
	private static final String filePath = "src/score/scores";
	private static final int NAME_COLUMN = 0;
	private static final int SCORE_COLUMN = 1;

	public static void main(String[] args){
		new CentralServer();
	}
	
	public void webServer(){
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(5555);
			while (true) {
				System.out.println("Waiting for client to connect...");
				Socket s = ss.accept();
				System.out.println("Client " + s.getInetAddress() + ":" + s.getPort() + " connected");
				PrintWriter pw = new PrintWriter(s.getOutputStream());
				Scanner sc = new Scanner(new File(filePath));
			    String msg = ("\r\n<html><h1>Sorry! Top Score List</h1>"
			    		+ "<table border=\"1\"><tr><th>Name</th><th>Score</th></tr>");
				while(sc.hasNext()) {
					msg+="<tr><td>"+sc.next()+"</td><td>"+sc.nextInt()+"</td></tr>";
				}
				msg+="</table></html>";
			    pw.println(msg);
			    pw.flush();
			    pw.close();
				sc.close();
			}
		}
		catch (BindException e){
			System.out.println("Sorry " + e.getMessage());
		}
		catch (IOException e){
			System.out.println("Sorry " + e.getMessage());
		}
		finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException ioe) {
					System.out.println("IOE closing ServerSocket: " + ioe.getMessage());
				}
			}
		}
	}
	
	public CentralServer(){
		ServerSocket ss = null;
		new Thread(new Runnable(){
			@Override
			public void run() {
				webServer();
			}			
		}).start();
		try {
			ss = new ServerSocket(9876);
			while (true) {
				System.out.println("Waiting for client to connect...");
				Socket s = ss.accept();
				new Client(s).start();
			}
		}
		catch (BindException e){
			System.out.println("Sorry " + e.getMessage());
		}
		catch (IOException e){
			System.out.println("Sorry " + e.getMessage());
		}
		finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException ioe) {
					System.out.println("IOE closing ServerSocket: " + ioe.getMessage());
				}
			}
		}
	}
	

	public static synchronized void add(String name, int score) {
		Object[] tableHeaders = new Object[] {"Name", "Score"};
		DefaultTableModel tableModel = new DefaultTableModel(tableHeaders,0) {
			private static final long serialVersionUID = -2100979802046466684L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		try {
			Scanner sc = new Scanner(new File(filePath));
			while(sc.hasNext()) {
				Object[] row = {sc.next(),sc.nextInt()};
				tableModel.addRow(row);
			}
			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("Cannot add product, "+e.getMessage());
		}

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
		save(tableModel);
	}
	
	private static synchronized void save(DefaultTableModel tableModel) {
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
	class Client extends Thread{
		private Socket s;
		public Client(Socket s){
			this.s = s;
		}
		public void run(){
			try {
				System.out.println("Client " + s.getInetAddress() + ":" + s.getPort() + " connected");
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				Scanner sc = new Scanner(new File(filePath));
				Vector<Object[]> table = new Vector<Object[]>();
				while(sc.hasNext()) {
					Object[] row = {sc.next(),sc.nextInt()};
					table.add(row);
				}
				sc.close();
				oos.writeObject(table);
				oos.flush();
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				String code = br.readLine();
				Map<String,Object> map = JSONCode.decode(code);
				if (((String)map.get("type")).equals("add")){
					System.out.println("start adding");
					add((String)map.get("name"),(int)(long)map.get("score"));
					System.out.println("finish adding");
				}
				else {
					System.out.println("finish");
				}
			}
			catch (IOException e){
				System.out.println("Sorry " + e.getMessage());
			} catch (ParseException e) {
				System.out.println("Sorry " + e.getErrorType());
			}
		}
	}
}
