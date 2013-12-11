package org.Kairus.StrifeModMan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

public class GUI extends JFrame {

	Object[][] tableData;
	JMenuBar GUImenuBar = new JMenuBar();
	JMenu GUImenu = new JMenu("File");
	JMenu GUIsettings = new JMenu("Settings");
	JMenu GUIhelp = new JMenu("Help");
	JMenuItem GUIhelpUser = new JMenuItem("User Help");
	JMenuItem GUIhelpDev = new JMenuItem("Developer Help");
	JMenuItem GUIhelpAbout = new JMenuItem("About");
	JMenuItem GUIexit = new JMenuItem("Exit");
	JLabel GUImodName = new JLabel("");
	JLabel GUImodAuthor = new JLabel("");
	JLabel GUImodVersion = new JLabel("");
	JButton GUIapplyMods = new JButton("Apply mods");
	JCheckBoxMenuItem GUIdevMode = new JCheckBoxMenuItem("developer mode");
	ModsTableModel table;
	ImageIcon defaultIcon = new ImageIcon("spritesheet.png");
	
	modMan modman;
	
	GUI(modMan mm){
		// setup
		super("Kairus101's Strife ModMan");
		modman = mm;
	}
	public void init(){
		// layout
		setLayout(new BorderLayout());

		// menu
		GUImenu.add(GUIexit);
		GUImenuBar.add(GUImenu);

		GUIdevMode.setSelected(modman.isDeveloper);
		GUIsettings.add(GUIdevMode);
		GUImenuBar.add(GUIsettings);

		GUIhelp.add(GUIhelpUser);
		GUIhelp.add(GUIhelpDev);
		GUIhelp.add(GUIhelpAbout);
		GUImenuBar.add(GUIhelp);
		
		
		add(GUImenuBar, BorderLayout.NORTH);


		String[] columnNames = {"Enabled", "Icon", "Name", "Author", "Version"};
		
		table = new ModsTableModel(tableData, columnNames);
		//table.set
		table.setRowHeight(50);
		table.setPreferredScrollableViewportSize(new Dimension(200, 70));
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(2).setMinWidth(150);
		JScrollPane modPanel = new JScrollPane(table);
		add(modPanel, BorderLayout.CENTER);

		//information panel
		JPanel infoPanel = new JPanel();
		infoPanel.add(new JLabel("Mod name: "));
		infoPanel.add(GUImodName);
		infoPanel.add(new JSeparator());
		infoPanel.add(new JLabel("Mod author: "));
		infoPanel.add(GUImodAuthor);
		infoPanel.add(new JSeparator());
		infoPanel.add(new JLabel("Mod version: "));
		infoPanel.add(GUImodVersion);
		infoPanel.setPreferredSize(new Dimension(150,0));
		add(new JScrollPane(infoPanel), BorderLayout.EAST);

		//bottom bar
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(GUIapplyMods);
		bottomPanel.setPreferredSize(new Dimension(0,50));
		add(bottomPanel, BorderLayout.SOUTH);

		// events
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(WindowEvent winEvt) {
				System.exit(0);
			}});
		GUIexit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}});
		GUIhelpUser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				showMessage(
						"This mod manager is designed to apply mods to Strife.\n"+
						"Mods go in the mods folder and should have the .strifemod format.\n"+
						"Every time strife updates, you should re-run this program and re-apply.\n"+
						"Any questions, refer to the forum post <to come>",
						"User help", JOptionPane.PLAIN_MESSAGE);
			}});
		GUIhelpDev.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				showMessage(
						"Directions to making a mod:\n"+
						"1. Extract files you want to change from resources0 into\n"+
						"<your program Files strife folder>/game/ (keeping file structures)\n"+
						"2. Make changes to those files, these will affect strife directly\n"+
						"  either \"reload interfaces\" in console or a restart will make changes active\n"+
						"3. Put your modified files into a .zip, along with a mod.txt file with\n"+
						"  name:<name Of Mod>\n"+
						"  version:<version Of Mod>\n"+
						"  author:<author of mod>\n\n"+
						"4. Add an icon.png to the zip file, this isn't required, but it is recommended.\n"+
						"5. For all your modified files, place the original ones, from resources0 into\n"+
						"  a folder named \"original\" in the zip file, then rename your .zip to .strifemod.\n"+
						"6. run modMan, and apply the mod - it will ask you if you want to make an official version.\n"
						+ "  Say yes. Put your mod somewhere safe and put mod it made for you into mods.\n"
						+ "7. Check that you can apply your official mod.\n"
						+ "\n"
						+ "There are other (harder) ways of applying the mods, similar to the HoN modMan\n"
						+ "Go to <forum link> to get more information.",
						"Developer help", JOptionPane.PLAIN_MESSAGE);
			}});
		GUIhelpAbout.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				showMessage(
						"Strife ModMan!\n"
						+ "Developed by Kairus101\n"
						+ "Version: "+modman.version+"\n"
						+ "Official forum link: <link incoming>",
						"Developer help", JOptionPane.PLAIN_MESSAGE);
			}});
		GUIapplyMods.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				modman.applyMods();
			}});
		GUIdevMode.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent event) {
		    	  modman.isDeveloper = GUIdevMode.isSelected();
		    	  modman.saveConfig();
		      }});

		pack();
		setSize(600, 600);
		setVisible(true);

	}

	private class ModsTableModel extends JTable {
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int row, int column) {
			return column==0?true:false;
		}
		public void setValueAt(Object value, int row, int col) {  
			tableData[row][col] = value;
			//table.invalidate();
			//fireTableCellUpdated(row, col);
		}  
		public Class<?> getColumnClass(int column) {
			return getValueAt(0, column).getClass();
		}
		ModsTableModel(Object[][] data, Object[] columnNames){
			super(data, columnNames);
			getColumnModel().getColumn(0).setCellRenderer(new CheckBoxRenderer());

			ListSelectionModel rowSM = getSelectionModel();
			rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting()) return;
					ListSelectionModel lsm = (ListSelectionModel)e.getSource();
					if (lsm.isSelectionEmpty()) {
						System.out.println("No rows are selected.");
					} else {
						int selectedRow = lsm.getMinSelectionIndex();
						GUImodName.setText(modman.mods.get(selectedRow).name);
						GUImodAuthor.setText(modman.mods.get(selectedRow).author);
						GUImodVersion.setText(modman.mods.get(selectedRow).version);
					}
				}
			});
		}
	}

	private class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
		private static final long serialVersionUID = 1L;
		CheckBoxRenderer() {
			setHorizontalAlignment(JLabel.CENTER);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			if ((Boolean)table.getValueAt(row, col)) {
				setBackground(Color.GREEN);
			} else {
				setBackground(Color.RED);
			}
			setSelected((value != null && ((Boolean) value).booleanValue()));
			tableData[row][0] = value != null && ((Boolean) value).booleanValue();
			return this;
		}
	}

	public void showMessage(String content){
		JOptionPane.showMessageDialog(this, content);
	}

	public void showMessage(String content, String title, int icon){
		JOptionPane.showMessageDialog(this, content, title, icon);
	}

	public int showYesNo(String title, String content){
		return JOptionPane.showConfirmDialog(this, content, title, JOptionPane.YES_NO_OPTION);
	}
}
