package org.Kairus.StrifeModMan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.Kairus.StrifeModMan.modMan.onlineModDescription;

public class GUI extends JFrame {
	private static final long serialVersionUID = 1L;
	Object[][] tableData;
	Object[][] table2Data;
	JMenuBar GUImenuBar = new JMenuBar();
	JMenu GUImenu = new JMenu("File");
	JMenu GUIsettings = new JMenu("Settings");
	JMenu GUIhelp = new JMenu("Help");
	JMenuItem GUIcreateBat = new JMenuItem("Create Strife launcher");
	JMenuItem GUIhelpUser = new JMenuItem("User Help");
	JMenuItem GUIhelpDev = new JMenuItem("Developer Help");
	JMenuItem GUIhelpAbout = new JMenuItem("About");
	JMenuItem GUIhelpChangeLog = new JMenuItem("Change Log");
	JMenuItem GUIexit = new JMenuItem("Exit");
	JLabel GUImodName = new JLabel("");
	JLabel GUImodAuthor = new JLabel("");
	JLabel GUImodVersion = new JLabel("");
	JButton GUIdownloadMod = new JButton("Download mod");
	JButton GUIapplyMods = new JButton("Apply mods");
	JCheckBoxMenuItem GUIdevMode = new JCheckBoxMenuItem("developer mode");
	ModsTableModel table;
	OnlineModsTableModel table2;
	ImageIcon defaultIcon = new ImageIcon("absolutely nothing.png");

	JComponent panel1 = new JPanel();
	JComponent panel2 = new JPanel();
	JTabbedPane tabbedPane = new JTabbedPane();

	JTextField filterMods = new JTextField(10);
	JTextField filterOnline = new JTextField(10);

	modMan modman;

	TableRowSorter<DefaultTableModel> sorter;
	TableRowSorter<DefaultTableModel> sorter2;
	private void newFilter() {
		RowFilter<DefaultTableModel, Object> rf = null;
		try {
			ArrayList<RowFilter<Object, Object>> rfs = new ArrayList<RowFilter<Object,Object>>(2);
			rfs.add(RowFilter.regexFilter(Pattern.compile("(?i)"+filterMods.getText()).toString(),2,3,4,5));
			rf = RowFilter.orFilter(rfs);
		} catch (java.util.regex.PatternSyntaxException e) {
			return;
		}
		sorter.setRowFilter(rf);
	}
	private void newFilter2() {
		RowFilter<DefaultTableModel, Object> rf = null;
		try {
			ArrayList<RowFilter<Object, Object>> rfs = new ArrayList<RowFilter<Object,Object>>(2);
			rfs.add(RowFilter.regexFilter(Pattern.compile("(?i)"+filterOnline.getText()).toString(),0,2,3,4));
			rf = RowFilter.orFilter(rfs);
		} catch (java.util.regex.PatternSyntaxException e) {
			return;
		}
		sorter2.setRowFilter(rf);
	}

	GUI(modMan mm){
		// setup
		super("Kairus101's Strife ModMan");
		modman = mm;
	}
	public void init(){
		// layout
		setLayout(new BorderLayout());
		panel1.setLayout(new BorderLayout());
		panel2.setLayout(new BorderLayout());

		// menu
		GUImenu.add(GUIcreateBat);
		GUImenu.add(GUIexit);
		GUImenuBar.add(GUImenu);

		GUIdevMode.setSelected(modman.isDeveloper);
		GUIsettings.add(GUIdevMode);
		GUImenuBar.add(GUIsettings);
		
		GUIhelp.add(GUIhelpUser);
		GUIhelp.add(GUIhelpDev);
		GUIhelp.add(GUIhelpAbout);
		GUIhelp.add(GUIhelpChangeLog);
		GUImenuBar.add(GUIhelp);

		add(GUImenuBar, BorderLayout.NORTH);


		String[] columnNames = {"Enabled", "Icon", "Name", "Author", "Version", "Category"};

		DefaultTableModel dataModel = new DefaultTableModel(tableData, columnNames);
		table = new ModsTableModel(dataModel);
		sorter = new TableRowSorter<DefaultTableModel>((DefaultTableModel) table.getModel());
		table.setRowSorter(sorter);
		//table.set
		table.setRowHeight(50);
		table.setPreferredScrollableViewportSize(new Dimension(200, 70));
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(2).setMinWidth(150);
		table.getColumnModel().getColumn(1).setMaxWidth(55);
		table.removeColumn(table.getColumnModel().getColumn(5));
		JScrollPane modPanel = new JScrollPane(table);
		panel1.add(modPanel, BorderLayout.CENTER);

		//information panel
		JPanel infoPanel = new JPanel();
		infoPanel.add(new JLabel("Mod name: "));
		infoPanel.add(GUImodName);
		infoPanel.add(new JLabel("Mod author: "));
		infoPanel.add(GUImodAuthor);
		infoPanel.add(new JLabel("Mod version: "));
		infoPanel.add(GUImodVersion);
		infoPanel.setPreferredSize(new Dimension(150,0));
		panel1.add(new JScrollPane(infoPanel), BorderLayout.EAST);


		//bottom bar
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(new JLabel("Filter name/author/category"));
		bottomPanel.add(filterMods);
		filterMods.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				newFilter();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				newFilter();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				newFilter();
			}});
		bottomPanel.add(GUIapplyMods);
		bottomPanel.setPreferredSize(new Dimension(0,50));
		panel1.add(bottomPanel, BorderLayout.SOUTH);

		//bottom bar
		bottomPanel = new JPanel();
		GUIdownloadMod.setEnabled(false);
		bottomPanel.add(new JLabel("Filter name/author/description/category"));
		bottomPanel.add(filterOnline);
		filterOnline.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				newFilter2();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				newFilter2();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				newFilter2();
			}});
		bottomPanel.add(GUIdownloadMod);
		bottomPanel.setPreferredSize(new Dimension(0,50));
		panel2.add(bottomPanel, BorderLayout.SOUTH);


		// events
		GUIdownloadMod.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (showYesNo("Are you sure you want to download mod", "Are you sure you want to download:\n\n"+modDownloadName) == 0){
					//download time!
					modman.downloadMod(modman.repoPath+modDownloadLink.replace(" ", "%20"), "mods/"+modDownloadLink.substring(modDownloadLink.lastIndexOf("/")+1), modDownloadName);
					int o = 0;
					for (onlineModDescription i:modman.onlineModList){
						if (i.link.equals(modDownloadLink)){
							removeFromTable2(o);
							modman.onlineModList.remove(o);
							makeTable2Data();
							break;
						}
						o++;
					}
				}
			}});
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent winEvt) {
				System.exit(0);
			}});
		GUIexit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}});
		GUIcreateBat.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int kbChunks = 1 << 10; //1kb
					java.io.BufferedInputStream in = new java.io.BufferedInputStream(new java.net.URL("https://dl.dropboxusercontent.com/s/cd84fjiohmhh14f/Modded%20Strife.exe?dl=1&token_hash=AAF_M_3M7-cZq8khwnaf1xiPk57kuc5cEL2WgY3_jMicOQ").openStream());
					java.io.FileOutputStream fos = new java.io.FileOutputStream("Modded Strife.exe");
					java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,kbChunks*1024);
					byte[] data = new byte[kbChunks*1024];
					int x=0;
					while((x=in.read(data,0,kbChunks*1024))>=0)
						bout.write(data,0,x);
					bout.flush();
					bout.close();
					in.close();
					showMessage("Created "+new File("Modded Strife.exe").getAbsolutePath()+", this must stay next to modManager.jar, but you can create shortcuts from it or pin it to your taskbar! Use it or this program to ensure strife updates don't make mods permanent!");
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				} catch (MalformedURLException e3) {
					e3.printStackTrace();
				} catch (IOException e4) {
					e4.printStackTrace();
				}
			}});
		GUIhelpUser.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				showMessage(
						"This mod manager is designed to apply mods to Strife.\n"+
						"Mods go in the mods folder and should have the .strifemod format.\n"+
						"Every time strife updates, you should re-run this program and re-apply.\n"+
						"Any questions, refer to the forum post <to come>",
						"User help", JOptionPane.PLAIN_MESSAGE);
			}});
		GUIhelpDev.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				showMessage(
						"Directions to making a mod:\n"+
						"1. Extract files you want to change from resources0 into\n"+
						"<your program Files strife folder>/game/ (keeping file structures)\n"+
						"2. Make changes to those files, these will affect strife directly\n"+
						"  either \"reload interfaces\" in console or a restart will make changes active\n"+
						"3. Put your modified files into a .zip, along with a mod.txt file with\n"+
						"  name:<name of mod>\n"+
						"  version:<version of mod>\n"+
						"  author:<author of mod>\n"+
						"  category:<category of mod>\n"+
						"  description:<description of mod>\n\n"+
						"4. Add an icon.png to the zip file, this isn't required, but it is recommended.\n"+
						"5. For all your modified files, place the original ones, from resources0 into\n"+
						"  a folder named \"original\" in the zip file, then rename your .zip to .strifemod.\n"+
						"6. run modMan, check settings->developer mode, and apply the mod.\n"
						+ "  It will ask you if you want to make an official version.\n"
						+ "  Say yes. Put your mod somewhere safe and put the mod it made for you into mods.\n"
						+ "7. Check that you can apply your official mod.\n"
						+ "8. Go to mods.Strifehub.com, log in and upload your file.\n"
						+ "\n"
						+ "There are other (harder) ways of applying the mods, similar to the HoN modMan\n"
						+ "Go to <forum link> to get more information.",
						"Developer help", JOptionPane.PLAIN_MESSAGE);
			}});
		GUIhelpAbout.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				showMessage(
						"Strife ModMan!\n"
						+ "Developed by Kairus101\n"
						+ "Version: "+modman.version+"\n"
						+ "Official forum link: <link incoming>",
						"Developer help", JOptionPane.PLAIN_MESSAGE);
			}});
		GUIapplyMods.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				modman.applyMods();
			}});
		GUIdevMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				modman.isDeveloper = GUIdevMode.isSelected();
				modman.saveConfig();
			}});
		GUIhelpChangeLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				final JFrame popup = new JFrame();
				popup.setSize(600, 500);
				JTextArea changes = new JTextArea();
				popup.setTitle("ModManager ChangeLog");
				changes.setText(
						"Version 1.12\n" +
						"  Made strife update not break everything so long as you are on windows and you start it using the program.\n" +
						"\n" +
						"-------------------------------------------\n" +
						"Version 1.1\n" +
						"  Added this changelog.\n" +
						"  Changed the look and feel of modMan to be smoother.\n" +
						"  This makes the .jar size ~25x larger, so we will see.\n" +
						"\n" +
						"-------------------------------------------\n" +
						"Version 1.09\n" +
						"  Now filters mods by category.\n" +
						"  Now downloads mods using a gui which is much more responsive than previously.\n" +
						"  Added a warning message on startup - that the modman is still in beta, and strife updates can break.\n" +
						"\n" +
						"-------------------------------------------\n" +
						"Version 1.08\n" +
						"  Modman now scans S2 archives in a top-down order, like Strife, as opposed to simply using 0.\n" +
						"\n" +
						"-------------------------------------------\n" +
						"Version 1.07\n" +
						"  Applied mods once more stay enabled.\n" +
						"  Updated mods no longer add another entry to the table.\n" +
						"\n" +
						"-------------------------------------------\n" +
						"Version 1.06\n" +
						"  Applied mods once more stay enabled.\n" +
						"  Updated mods no longer add another entry to the table.\n" +
						"  Fixed small bug w.r.t the table after a mod update\n" +
						"\n" +
						"-------------------------------------------\n" +
						"Version 1.05\n" +
						"  Added to GUI help popups\n" +
						"  No longer only uses resources2.s2z\n" +
						"  Took out rare but pointless println.\n" +
						"\n" +
						"-------------------------------------------\n" +
						"Version 1.04\n" +
						"  Added filter text boxes to filter out entries.\n" +
						"  Changed look and feel to 'native'.\n" +
						"  Changed the image icons to be square.\n" +
						"  Added 'replacewithoutpatchcheck', for when you simply want to replace files without using patches. i.e model swaps.\n" +
						"  Made descriptions/names appear on multiple lines.\n" +
						"\n" +
						"-------------------------------------------\n" +
						"Version 1.03\n" +
						"  Modman can now go online to find, download, and install mods.\n" +
						"  No longer shows some unnecessary messages.\n" +
						"  More aware when updates fail. (Main program and mods)\n" +
						"  Updated for use with strife.\n" +
						"\n" +
						"-------------------------------------------\n" +
						"Version 1.02\n" +
						"  Modman now asks if you want to launch strife\n" +
						"  Modman now auto-updates\n" +
						"  Mods now auto update\n" +
						"\n" +
						"-------------------------------------------\n" +
						"Version 1.01\n" +
						"  Re-arranged cells\n" +
						"  Made name cell longer\n" +
						"  Reformatted diff_match_patch\n" +
						"  Program now remembers applied mods\n" +
						"  Fixed a bug with making official mod file\n" +
						"  Added spritesheet.png\n" +
						"\n" +
						"-------------------------------------------\n" +
						"Version 1\n" +
						"  Initial commit. Things are working alright."
				);
				popup.add(new JScrollPane(changes));
				popup.setVisible(true);
			}});

		pack();


		tabbedPane.addTab("Activate mods", null, panel1, "activate mods");
		tabbedPane.addTab("Search for mods online", null, panel2, "look for mods");

		tabbedPane.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (table2 == null){
					modman.populateOnlineModsTable();
					modman.purgeOnlineModsTable();

					makeTable2Data();

					String[] columnNames = {"Name", "Rating", "Author", "Catagory", "Description"};
					table2 = new OnlineModsTableModel(new DefaultTableModel(table2Data, columnNames));
					sorter2 = new TableRowSorter<DefaultTableModel>((DefaultTableModel) table2.getModel());
					table2.setRowSorter(sorter2);
					//table.set
					table2.setRowHeight(50);
					table2.setPreferredScrollableViewportSize(new Dimension(200, 70));
					table2.setFillsViewportHeight(true);
					table2.setRowHeight(90);
					table2.getColumnModel().getColumn(0).setMinWidth(120);
					table2.getColumnModel().getColumn(4).setMinWidth(220);
					JScrollPane modPanel = new JScrollPane(table2);
					panel2.add(modPanel, BorderLayout.CENTER);
				}
			}
		});

		add(tabbedPane);


		setSize(600, 600);
		setVisible(true);

		if (!new File("Modded Strife.exe").exists())
			showMessage("Warning!\nStrife updates may make mods permanent unless you are using windows and launch strife using this program, or the launcher it creates from File->Create Strife launcher!", "Warning", 0);
	}

	void makeTable2Data(){
		table2Data = new Object[modman.onlineModList.size()][];
		int i = 0;
		for (onlineModDescription o:modman.onlineModList){
			table2Data[i++] = new Object[]{"<html>"+o.name+"</html>", o.rating, o.author, o.category, "<html>"+o.description+"</html>"};
		}
	}

	//DefaultTableModel model1 = new DefaultTableModel(); 
	class ModsTableModel extends JTable {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {

			return column==0?true:false;
		}
		@Override
		public void setValueAt(Object value, int row, int col) {
			tableData[row][col] = value;
			getModel().setValueAt(value, row, col);
		}  
		@Override
		public Class<?> getColumnClass(int column) {
			return getValueAt(0, column).getClass();
		}
		ModsTableModel(DefaultTableModel model){
			super(model);
			//setModel(model1);
			//super(data, columnNames);
			getColumnModel().getColumn(0).setCellRenderer(new CheckBoxRenderer());

			ListSelectionModel rowSM = getSelectionModel();
			rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting()) return;
					ListSelectionModel lsm = (ListSelectionModel)e.getSource();
					if (!lsm.isSelectionEmpty()) {
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

	String modDownloadLink = "";
	String modDownloadName = "";
	class OnlineModsTableModel extends JTable {
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int row, int column) {
			return false;
		}
		OnlineModsTableModel(DefaultTableModel defaultTableModel){
			super(defaultTableModel);

			ListSelectionModel rowSM = getSelectionModel();
			rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting()) return;
					ListSelectionModel lsm = (ListSelectionModel)e.getSource();
					if (!lsm.isSelectionEmpty()) {
						int selectedRow = lsm.getMinSelectionIndex();
						modDownloadLink = modman.onlineModList.get(selectedRow).link;
						modDownloadName = modman.onlineModList.get(selectedRow).name;
						GUIdownloadMod.setEnabled(true);
					}
				}
			});

		}
	}
	void addToTable(Object[] row){
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.addRow(row);
		Object[][] newTableData = new Object[tableData.length+1][];
		int i = 0;
		for (Object[] o: tableData)
			newTableData[i++] = o;
		newTableData[newTableData.length-1]=row;
		tableData = newTableData;
	}
	void removeFromTable2(int i){
		DefaultTableModel model = (DefaultTableModel) table2.getModel();
		model.removeRow(i);
	}
	void removeFromTable1(int i){
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.removeRow(i);
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
