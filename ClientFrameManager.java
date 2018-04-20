package cardGame;

import java.awt.EventQueue;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ClientFrameManager extends JCFrameClient implements ComponentListener {

	public static void main(String[] args) {		
	EventQueue.invokeLater(new Runnable() {
		public void run() {
			try {
                //initAndShowGUI();

				ClientFrameManager frame = new ClientFrameManager();
				//frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	});

	}

	ClientFrameManager() {
		JCFrameClient frame = new JCFrameClient();
		//frame.setVisible(true);
	}
	

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Hidden");
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Moved");

	}

	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Resize! Very exciting");
		//e.

	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Shown");

	}
	

}
