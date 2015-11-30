package HostServer;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Dictionary;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import game.GameManager;
import simple.JSONObject;

public class HostServer extends Thread{
	private boolean serverStatus = true;
	public boolean getServerStatus(){return serverStatus;}
	private int numOfPlayers;
	private Vector<ClientThread> ctVector = new Vector<ClientThread>();
	private ServerSocket ss;
	public GameManager gm;
	private String currStage;
	private int currPlayer;
	private Map<String, Object> info;
	private boolean[] buttonEnable = {true,true,true,true,false};
	public Map<Integer,Integer> playerColor = new TreeMap<Integer,Integer>();
	public final static String[] colorNames = {"red", "blue", "yellow", "green"};

	static final String[] stages = {"number","color","game"};
	/*msg {
	 * status,
	 * stage,
	 * id,
	 * errMsg,
	 * color,
	 * numOfPlayer,
	 * 
	 */
	public String CurrStage(){
		return currStage;
	}
	
	private void getStage(int id){
		if (currStage == stages[0]) {
			info.put("status", false);
			info.put("errMsg", "The host haven't select the number of players.");
		}
		else if (currStage == stages[1]){
			if (id >= numOfPlayers){
				info.put("status", false);
				info.put("errMsg", "The room has no more space.");
			}
			else {
				info.put("status", true);
				info.put("errMsg", "");
				setButtonEnable(id);
			}
		}
		else if (currStage == stages[2]){
			info.put("status", false);
			info.put("errMsg", "Game has already started.");
		}
		info.put("stage", currStage);
	}
	
	public void playerChooseColor(int id, int color){
		if (playerColor.containsKey(id)){
			buttonEnable[playerColor.get(id)] = true;
		}
		playerColor.put(id, color);
		buttonEnable[color] = false;
		int k = 0;
		for (int i = 0; i < 4; i++){
			if (!buttonEnable[i]) k++;
		}
		if (k == numOfPlayers) buttonEnable[4] = true;
		else buttonEnable[4] = false;
	}
	
	private void setButtonEnable(int id){
		for (int i = 0; i < 4; i++){
			info.put("button"+i, buttonEnable[i]);
		}
		if (id == 0) info.put("confirm", buttonEnable[4]);
		else info.put("confirm", false);
	}
	
	public synchronized String getInfo(int id){
		getStage(id);
		if (currStage == stages[2]){
			info.put("playerIndex", id);
			info.put("allInfo", playerColor);
		}
		return JSONCode.encode(info);
	}
	
	public void setNumOfPlayers(int n){
		currStage = stages[1];
		numOfPlayers = n;
		gm.setNumOfPlayers(n);
	}
	public HostServer(int numOfPlayers, int port, GameManager gm) {
		currStage = stages[0];
		playerColor = new TreeMap<Integer,Integer>();
		this.numOfPlayers = numOfPlayers;
		this.gm = gm;
		info = new TreeMap<String,Object>();
		try {
			ss = new ServerSocket(port);
			serverStatus = true;
			System.out.println("Starting Chat Server");
		} catch (IOException ioe) {
			serverStatus = false;
			System.out.println("IOE: " + ioe.getMessage());
		} catch (IllegalArgumentException ilae){
			serverStatus = false;
		}
	}
	public void removeChatThread(ClientThread ct) {
		ctVector.remove(ct);
	}
	public void sendMessageToClients() {
		for (int i = 0; i < ctVector.size(); i++){
			ctVector.get(i).sendMessage(getInfo(i));
		}
	}
	public void sendMSG(String msg) {
		System.out.println("Sent");
		for (int i = 0; i < ctVector.size(); i++){
//			System.out.println("Send msg " + msg);
			ctVector.get(i).sendMessage(msg);
		}
	}
	@Override
	public void run() {
		if (!serverStatus) return;
		try {
			while (true) {
				System.out.println("Waiting for client to connect...");
				Socket s = ss.accept();
				System.out.println("Client " + s.getInetAddress() + ":" + s.getPort() + " connected");
				ClientThread ct = new ClientThread(s, this, ctVector.size());
				ct.sendMessage(getInfo(ctVector.size()));
				if ((boolean)info.get("status")){
					ctVector.add(ct);
					ct.start();
				}
			}
		} catch (IOException e) {
			System.out.println("Host Server IOE:" + e.getMessage());
		} finally {
			if (ss != null) {
				try {
					ss.close();
					System.out.println("close");
				} catch (IOException ioe) {
					System.out.println("IOE closing ServerSocket: " + ioe.getMessage());
				}
			}
		}
	}
	public void startGame(){
		currPlayer = 0;
		currStage = HostServer.stages[2];
	}
	public void quit(int id){
		if (currStage == stages[1] && playerColor.containsKey(id)){
			buttonEnable[playerColor.get(id)] = true;
			buttonEnable[4] = false;
			sendMessageToClients();
		}
		else {
			Map<String,Object> map = new TreeMap<String,Object>();
			map.put("status", true);
			map.put("stage",CurrStage());
			map.put("type", "msg");
			map.put("quit", true);
			if (playerColor.containsKey(id)){
				map.put("color", colorNames[playerColor.get(id)]);
				map.put("content","player " + colorNames[playerColor.get(id)] + " has left game.");
			}
			else {
				map.put("color", "");
				map.put("content", "");
			}
			sendMSG(JSONCode.encode(map));
			gm.quitPlayers.add(id);
		}
	}

	public void end() {
		try {
			if (ss!=null){
				TreeMap<String, Object> quitAll = new TreeMap<String, Object>();
				quitAll.put("type", "quitAll");
				sendMSG(JSONCode.encode(quitAll));
				ss.close();
			}
		} catch (IOException e) {
			System.out.println("End IOE:"+e.getMessage());
		}
	}
}
