package sorryclient;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class SorryTimer extends Thread {
	private JLabel label;
	private int time;
	private JPanel panel;
	private boolean stop;
	public SorryTimer(JLabel label, JPanel panel){
		this.label = label;
		this.panel = panel;
		stop = false;
		if (panel instanceof ColorSelector) time = 30;
		else time = 15;
	}
	public void run(){
		try {
			String timeString = "";
			for (int i = 1; i <= time; i++){
				Thread.sleep(1000);
				if (time - i < 10){
					timeString = "time 0:0"+(time-i);
				}
				else {
					timeString = "time 0:"+(time-i);
				}
				if (panel instanceof ColorSelector){
					label.setText(timeString);
					panel.repaint();
				}
				else if (panel instanceof GamePanel) {
					((GamePanel)panel).setTime(timeString);
				}
			}
			if (panel instanceof GamePanel){
				((GamePanel)panel).mGameManager.client.endTurn();
			}
		} catch (InterruptedException e) {
			System.out.println("SorryTimer IE: " + e.getMessage());
		} finally {
			if (panel instanceof ColorSelector && !stop){
				((ColorSelector)panel).outTime();
			}
		}
	}
	public void kill(){
		this.interrupt();
		stop = true;
	}
	public void reset() {
		this.interrupt();
		((GamePanel)panel).setTime("time 0:15");
	}
}
