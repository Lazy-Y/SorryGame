package HostServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

import simple.parser.ParseException;

class ClientThread extends Thread {
	private BufferedReader br;
	private PrintWriter pw;
	private HostServer hs;
	private int id;
	private Socket s;
	private Map<String,Object> info;
	private boolean start = false;
	public final static String[] colorNames = {"red", "blue", "yellow", "green"};
	public int getID(){
		return id;
	}
	public ClientThread(Socket s, HostServer hs, int id) {
		this.id = id;
		this.hs = hs;
		this.s = s;
		info = new TreeMap<String,Object>();
		try {
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			pw = new PrintWriter(s.getOutputStream());
		} catch (IOException ioe) {
			System.out.println("IOE in ChatThread constructor: " + ioe.getMessage());
		}
	}

	public synchronized void sendMessage(String str) {
//		System.out.println("Server " + str);
		pw.println(str);
		pw.flush();
	}

	public void run() {
		try {
			while (true) {
				String code = br.readLine();
				info = JSONCode.decode(code);
				if (hs.CurrStage() == HostServer.stages[1]){
					if ((boolean)info.get("start")) hs.startGame();
					else {
						int color = (int) (long)info.get("colorSelect");
						hs.playerChooseColor(id, color);
					}
					hs.sendMessageToClients();
				}
				else if (hs.CurrStage() == HostServer.stages[2]){
					System.out.println("MSG " + info);
					if (((String)info.get("type")).equals("msg")){
						Map<String,Object> map = new TreeMap<String,Object>();
						map.put("status", true);
						map.put("stage",hs.CurrStage());
						map.put("type", "msg");
						map.put("color", colorNames[hs.playerColor.get(id)]);
						map.put("content",info.get("content"));
						map.put("quit",false);
						hs.sendMSG(JSONCode.encode(map));
					}
					else if (((String)info.get("type")).equals("card")){
						Map<String,Object> map = new TreeMap<String,Object>();
						map.put("status", true);
						map.put("stage",hs.CurrStage());
						map.put("type", "card");
						map.put("cardID",info.get("cardID"));
						map.put("player",info.get("player"));
						hs.sendMSG(JSONCode.encode(map));
					}
					else if (((String)info.get("type")).equals("endTurn")){
						Map<String,Object> map = new TreeMap<String,Object>();
						map.put("status", true);
						map.put("stage",hs.CurrStage());
						map.put("type", "endTurn");
						map.put("cardID",info.get("cardID"));
						map.put("player",info.get("player"));
						hs.sendMSG(JSONCode.encode(map));
					}
					else if (((String)info.get("type")).equals("click")){
						Map<String,Object> map = new TreeMap<String,Object>();
						map.put("status", true);
						map.put("stage",hs.CurrStage());
						map.put("type", "click");
						map.put("x",info.get("x"));
						map.put("y",info.get("y"));
						System.out.println("Player " + (int)(long)info.get("playing") + " color " + colorNames[hs.playerColor.get((int)(long)info.get("playing"))]);
						map.put("playing",hs.playerColor.get((int)(long)info.get("playing")));
//						map.put("playing",(int)(long)info.get("playing"));
						hs.sendMSG(JSONCode.encode(map));
					}
				}
			}
		} catch (IOException ioe) {
			hs.removeChatThread(this);
			System.out.println(s.getInetAddress() + ":" + s.getPort() + " disconnected.");
		} catch (ParseException e) {
			System.out.println(s.getInetAddress() + ":" + s.getPort() + " disconnected.");
		} catch (NullPointerException e){
			hs.removeChatThread(this);
		}
		finally {
			System.out.println("client quit");
			hs.quit(id);
			if (hs.gm != null && hs.gm.livePlayers!=null && hs.gm.livePlayers[id]!=null && hs.gm.currentPlaying == id){
				hs.gm.simulatePlayer(hs.gm.livePlayers[id]);
			}
		}
	}
	private int getID(Object obj){
		return hs.playerColor.get((int)(long)obj);
	}
}