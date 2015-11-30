package HostServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import game.GameManager;
import simple.parser.ContainerFactory;
import simple.parser.JSONParser;
import simple.parser.ParseException;
import sorryclient.ChatPanel;
import sorryclient.ClientPanel;
import sorryclient.ColorSelector;


public class Client extends Thread {

	private PrintWriter pw;
	private BufferedReader br;
	private Scanner scan;
	private Socket s;
	private ClientPanel cp;
	private ChatPanel chp;
	private ColorSelector cs;
	private boolean start = false;
	public GameManager gm;
	public synchronized void sendMsg(String msg){
		pw.println(msg);
		pw.flush();
	}
	public void setCS(ColorSelector cs){
		this.cs = cs;
	}
	public Client(String address, int port, ClientPanel cp) {
		try {
			this.cp = cp;
			s = new Socket(address, port);
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(s.getOutputStream());
			this.start();
		} catch (IOException ioe) {
			cp.SelectColor(false,ioe.getMessage());
			System.out.println("IOE in ChatClient constructor: " + ioe.getMessage());
		} catch (IllegalArgumentException iae){
			cp.SelectColor(false, iae.getMessage());
		}
	}
	public void run() {
		try {
			String msg = br.readLine();
			Map json = JSONCode.decode(msg);
			if ((boolean)json.get("status")) cp.SelectColor(true,(String)json.get("errMsg"));
			else {
				cp.SelectColor(false,(String)json.get("errMsg"));
				return;
			}
			receiver(json);
			while (true) {
				try {
					if (s.isClosed()) break;
					System.out.println("Wait to read line");
					msg = br.readLine();
					System.out.println("Read line");
					if (msg == null) break;
					json = JSONCode.decode(msg);
					System.out.println("break 1 " + cp + " " + json);
					if (json.containsKey("type") && ((String)json.get("type")).equals("quitAll")) cp.quit();
					System.out.println("break 2 ");
					if (json != null) receiver(json);
					System.out.println("break 3 ");
				}
				catch (NullPointerException e){
					System.out.println("NullPointerException in Client.run(): " + e.getMessage());
				}	
			}
		} catch (IOException ioe) {
			System.out.println("IOE in Client.run(): " + ioe.getMessage());
		} catch (ParseException e) {
			System.out.println("ParseException in Client.run(): " + e.getMessage());
		}
		finally {
			System.out.println("CP quit");
			cp.quit();
		}
	}
	
	private void receiver(Map<String,Object> info){
		if (HostServer.stages[1].equals((String)info.get("stage"))){
			cs.buttonEnable(info);
		}
		if (HostServer.stages[2].equals((String)info.get("stage"))){
			if (!start) {
				start = true;
				@SuppressWarnings("unchecked")
				Map<String,Long> map = (Map<String,Long>)info.get("allInfo");
				Map<String,Long>playerColor = map;
				System.out.println("Player Color " + playerColor);
				Integer index = (int)(long)info.get("playerIndex");
				cp.startGame(playerColor,index);
			}
			else {
				System.out.println("equal " + info);
				String in = (String)info.get("type");
				if (in.equals("msg")){
					String c = (String)info.get("color");
					String m = (String)info.get("content");
					boolean q = (boolean)info.get("quit");
					chp.receiveMsg(c, m, q);
				}
				else if (in.equals("card")){
					gm.showCard((int)(long)info.get("cardID"),(int)(long)info.get("player"));
				}
				else if (in.equals("endTurn")){
					System.out.println("End Turn");
					gm.endTurn();
					System.out.println("Finish end turn");
				}
				else if (in.equals("click")){
					int playerID = (int)(long)info.get("playing");
					gm.tileClicked(gm.getTile((int)(long)info.get("x"), (int)(long)info.get("y")), gm.players[playerID]);
				}
			}
		}
	}
	public void setChatPanel(ChatPanel chatPanel) {
		chp = chatPanel;
	}
	public void sendCard(int type, int index) {
		Map<String,Object> map = new TreeMap<String,Object>();
		map.put("type", "card");
		map.put("cardID", type);
		map.put("player", index);
		sendMsg(JSONCode.encode(map));
	}
	public void endTurn() {
		Map<String,Object> map = new TreeMap<String,Object>();
		map.put("type", "endTurn");
		sendMsg(JSONCode.encode(map));
	}
	
	public void sendClick(int x, int y, int currentPlaying) {
		Map<String,Object> map = new TreeMap<String,Object>();
		map.put("type", "click");
		map.put("x", x);
		map.put("y", y);
		map.put("playing", currentPlaying);
		sendMsg(JSONCode.encode(map));
	}
	public void close() {
		try {
			if (pw != null) pw.close();
			if (br != null) br.close();
			if (s != null) s.close();
		}
		catch (IOException e){
			System.out.println("Client close IOE: "+e.getMessage());
		}
	}
}