package org.Kairus.StrifeModMan;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;

public class downloadsGUI extends JFrame {
	
	ArrayList<downloadMod> downloading = new ArrayList<downloadMod>();
	modMan parent = null;
	downloadsGUI(modMan parent){
		super("mods downloading");
		this.parent = parent;
	}
	JPanel panel;
	public void init(){
		panel = new JPanel();
		add(panel);
		setSize(200, 400);
		setVisible(false);
	}
	
	public void downloadMod(String link, String filename, String name){
		downloadMod task = new downloadMod(link, filename, parent, this);
		task.label = new JLabel(name);
        task.execute();
        panel.add(task.label);
        panel.add(task.bar);
        panel.add(task.separator);
		downloading.add(task);
		refresh();
	}
	

	private class downloadMod extends SwingWorker<Void, Void> {
		JLabel label;
		JProgressBar bar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		String filename = null;
		String link = null;
		modMan parent = null;
		downloadsGUI parent2 = null;
		downloadMod(String link, String filename, modMan parent, downloadsGUI parent2){
			this.link = link;
			this.filename = filename;
			this.parent = parent;
			this.parent2 = parent2;
		}
        @Override
        public Void doInBackground() {
        	try {
        		setProgress(0);
        		
        		java.net.URL url=new java.net.URL(link);
        	    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        	    int filesize = connection.getContentLength();
        	    bar.setMaximum(filesize);
        		
    			int kbChunks = 1 << 14; //8kb
    			java.io.BufferedInputStream in = new java.io.BufferedInputStream(new java.net.URL(link).openStream());
    			java.io.FileOutputStream fos = new java.io.FileOutputStream(filename);
    			java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,kbChunks*1024);
    			byte[] data = new byte[kbChunks*1024];
    			int x=0;
    			int done = 0;
    			while((x=in.read(data,0,kbChunks*1024))>=0)
    			{
    				bout.write(data,0,x);
    				done+=x;
    				bar.setValue(done);
    				bar.invalidate();
    			}
    			bout.flush();
    			bout.close();
    			in.close();
    			parent.mods.add(fileTools.loadModFile(new File(filename), parent));
    			parent.gui.addToTable(parent.mods.get(parent.mods.size()-1).getData());
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            return null;
        }
        @Override
        public void done() {
        	parent2.downloading.remove(this);
        	parent2.panel.remove(bar);
        	parent2.panel.remove(label);
        	parent2.panel.remove(separator);
        	parent2.refresh();
        }
    }

	public void refresh() {
		if (downloading.size()==0)
			setVisible(false);
		else
			setVisible(true);
	}
}
