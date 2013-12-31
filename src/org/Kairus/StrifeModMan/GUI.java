package org.Kairus.StrifeModMan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
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
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.UIManager;
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
	JMenuItem GUIhelpUser = new JMenuItem("User Help");
	JMenuItem GUIhelpDev = new JMenuItem("Developer Help");
	JMenuItem GUIhelpAbout = new JMenuItem("About");
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
		showMessage("Warning!\nThis modman is still in beta!\nBefore you update your strife, disable all mods,\nor they will be permanently in your game!\nI'm talking with S2 to resolve this bug.", "Warning", 0);
		try {
			/*
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
			*/
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //platform dependent
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); //default
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel"); //odd looking.
		} catch (Exception e) {
			e.printStackTrace();
		}
		// layout
		setLayout(new BorderLayout());
		panel1.setLayout(new BorderLayout());
		panel2.setLayout(new BorderLayout());

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
		table.removeColumn(table.getColumnModel().getColumn(5));
		JScrollPane modPanel = new JScrollPane(table);
		panel1.add(modPanel, BorderLayout.CENTER);

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
