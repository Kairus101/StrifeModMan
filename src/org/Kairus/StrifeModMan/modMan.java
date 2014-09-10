package org.Kairus.StrifeModMan;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import java.util.LinkedHashSet;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

public class modMan {
	private static final long serialVersionUID = 1L;
	String version = "1.16";

	boolean reloadMods = false;

	public static void main(String[] args) {
		if (args.length>0 && args[0].equals("launchStrife")){
			modMan mm = new modMan();
			mm.loadFromConfig();
			mm.launchStrife();
		}else
			new modMan().init();
	}
	GUI gui;
	downloadsGUI downloadsGui;
	String s2Path = null;
	String repoPath = "http://mods.strifehub.com/";
	String appliedMods = "";
	ArrayList<mod> appliedModsList = new ArrayList<mod>();
	boolean isDeveloper = false;
	ArrayList<mod> mods = new ArrayList<mod>();
	byte[] buffer = new byte[1024];
	String strifeVersion;

	HashMap<String, String> toBeAdded = new HashMap<String, String>();
	HashMap<String, Boolean> alreadyDone = new HashMap<String, Boolean>();

	public modMan(){}
	public void init(){
		try {
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //platform dependent
			/*
		    	NimRODTheme nt = new NimRODTheme();
		    	nt.setPrimary( new Color(205,235,255));
		    	nt.setSecondary( new Color(235,245,255));

		    	NimRODLookAndFeel NimRODLF = new NimRODLookAndFeel();
		    	NimRODLF.setCurrentTheme( nt);
		    	UIManager.setLookAndFeel( NimRODLF);
			 */
			UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		gui = new GUI(this);
		downloadsGui = new downloadsGUI(this);

		//update the main program
		checkForUpdate();

		//load config
		loadFromConfig();

		strifeVersion = fileTools.getStrifeVersionFromFile(s2Path+"/strife.version");
		
		//init GUI
		loadModFiles();

		//load enabled mods
		setModStatuses();

		gui.init();
		downloadsGui.init();

		if ( mods.size()>0 && gui.showYesNo("Update mods?", "Would you like to update your mods?") == 0){ //0 is yes.
			//update mods
			String updated = checkForModUpdates();
			if (updated.length()>0){
				gui.showMessage("Updated:\n"+updated);
			}else
				gui.showMessage("All mods up to date!");
			//if (reloadMods)
			//	loadModFiles();
		}
	}

	String findFileInS2(int number, String name){
		try {
			for (int i = number-1;i>=0;i--){
				ZipFile zipFile;
				zipFile = new ZipFile(s2Path+"/game/resources"+i+".s2z");
				ZipEntry entry =  zipFile.getEntry(name);
				if (entry != null){
					String r = fileTools.store(zipFile.getInputStream(entry));
					zipFile.close();
					return r;
				}
				zipFile.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	int archiveNumber = 1;
	String findArchives(){
		archiveNumber = 1;
		String output = null;
		while (true){
			output = s2Path+"/game/resources"+archiveNumber+".s2z";
			ZipFile zipFile = null;
			try {
				zipFile = new ZipFile(output);
				if ((zipFile.getComment() != null && zipFile.getComment().equals("Long live... ModMan!")) || 
				  (new File(output).length()<10000 && zipFile.size() != 0)
				  ){
					//we've found our guy.
					zipFile.close();
					new File(output).delete(); // REMOVE OUT-DATED S2Z FORMAT!
					//archiveNumber--;
					break;
				}
				zipFile.close();
			} catch (ZipException e){
				new File(output).delete();
				//archiveNumber--;
				return output; //corrupt zip file. This is oue guy.
			} catch (java.io.FileNotFoundException e) {
				// perfect, doesn't even exist.
				//archiveNumber--;
				return output;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (zipFile != null)
						zipFile.close();
				} catch (IOException e) {}
			}
			archiveNumber++;
		}
		return output;

	}

	String output = null;

	boolean applyMod(mod m) throws java.io.IOException	{
		// Delete old mod files
		recursiveDelete(new File(s2Path+"/game/mods/"+m.name+"/"));
		
		appliedMods += m.name+"|";
		m.patchesToSave.clear();
		ZipFile sourceZip = new ZipFile(m.fileName);
		for (String s: m.fileNames){

			String fileInS2 = findFileInS2(archiveNumber, s);
			ZipEntry patchFile = sourceZip.getEntry("patch_"+s);//patch
			
			
			//source
			if ((fileInS2==null || m.replaceWithoutPatchCheck) && patchFile == null){ //new file
				ZipEntry sourceFile = sourceZip.getEntry(s);
				InputStream zis = sourceZip.getInputStream(sourceFile);

				//output
				if (alreadyDone.get(s) != null){
					if (m.requirements.size()==0)
						gui.showMessage("Warning ("+m.name+"): Duplicate file with no originals:\n");
					continue;
				}
				alreadyDone.put(s, true);

				File f = new File(s2Path+"/game/mods/"+m.name.replace(" ", "_")+"/"+s);
				f.getParentFile().mkdirs();
				f.createNewFile();
				FileOutputStream fos = new FileOutputStream(f);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
			}else{
				//we need to perform a diff.
				//step 1, check for a patch file, if so, skip to step 3
				//step 2, check for original files, if so, make patches
				//step 3, apply patch to current file.
				//step 4, apply any xml modifications.
				//step 5, put new file in resources

				//setup
				diff_match_patch differ = new diff_match_patch();
				LinkedList<Patch> patch = null;
				String current = "";

				if (toBeAdded.get(s) != null){ // not the first mod
					current = toBeAdded.get(s);
				}else
					current = fileInS2; //current

				//step 1
				//check for a patch file, if so, skip to step 3
				String potentialPatch = m.patches.get(s);
				if (potentialPatch != null){ //We have a patch! continuing
					patch = (LinkedList<Patch>)differ.patch_fromText(potentialPatch);
				}else{ //no patch, check for original files
					//step 2
					//check for original files, if so, make patches and update mod later
					ZipEntry sourceFile = sourceZip.getEntry("original/"+s);//original
					if (sourceFile == null){
						gui.showMessage("Problem in "+m.name+"!\n"+s+"\nFound in resources, but no patch and no original file.\nIf you are developing this mod, put the official file in your mod pack, under \"original/"+s+"\".");
						continue;
					}

					InputStream zis = sourceZip.getInputStream(sourceFile);//original
					String original = fileTools.store(zis); //original
					sourceFile = sourceZip.getEntry(s);//modified
					zis = sourceZip.getInputStream(sourceFile);//modified
					String modified = fileTools.store(zis); //modified

					LinkedList<diff_match_patch.Diff> diffs1 = differ.diff_main(original, modified);
					patch = differ.patch_make(original, diffs1);

					if (isDeveloper){
						String patchText = differ.patch_toText(patch);
						m.patchesToSave.put(s, patchText);
					}
				}

				//step 3
				//apply patch to current file.
				Object[] result = differ.patch_apply(patch, current);
				boolean good = true;
				int error = 0;
				for (error = 0; error < ((boolean[])result[1]).length;error++)
					if (!((boolean[])result[1])[error]){
						good=false;
						break;
					}
				if (!good){
					gui.showMessage("Problem in "+m.name+":"+s+". So I won't apply it.\n"+(isDeveloper?"Applying diff: "+patch.get(error):""));
					continue;
				}
				toBeAdded.remove(s);
				toBeAdded.put(s, (String)result[0]);
				

				File f = new File(s2Path+"/game/mods/"+m.name.replace(" ", "_")+"/"+s);
				f.getParentFile().mkdirs();
				PrintWriter fos = new PrintWriter(f);
				fos.print((String)result[0]);
				fos.close();

			}
		}
		sourceZip.close();
		if (m.patchesToSave.size() > 0){
			int n = gui.showYesNo("Compress", "Save new official strifemod?");
			if (n==0){
				String[] filesToDelete = new String[2*m.patchesToSave.size()+1];
				filesToDelete[2*m.patchesToSave.size()] = "original/";
				String[] filesToAdd = new String[m.patchesToSave.size()];
				String[] content = new String[m.patchesToSave.size()];
				int i = 0;
				Iterator<?> it = m.patchesToSave.entrySet().iterator();
				while (it.hasNext()) {
					@SuppressWarnings("unchecked")
					Map.Entry<String, String> pairs = (Map.Entry<String, String>)it.next();		        
					filesToDelete[2*i] = pairs.getKey();
					filesToDelete[2*i+1] = "original/"+pairs.getKey();
					filesToAdd[i] = "patch_"+pairs.getKey();
					content[i] = pairs.getValue();
					it.remove(); // avoids a ConcurrentModificationException
					i++;
				}
				fileTools.remakeZipEntry(m.name+"_official.strifemod", new File(m.fileName), filesToDelete, filesToAdd, content);
				gui.showMessage("Created official mod at: "+new File(m.name+"_official.strifemod").getAbsolutePath());
			}
		}
		appliedModsList.add(m);
		return true;
	}

	LinkedHashSet<mod> arrangeRequirements(mod Mod)
	{
		LinkedHashSet<mod> returnArray = new LinkedHashSet<mod>();
		for(String requirement : Mod.requirements)
		{
			boolean requirementFound = false;
			for(mod m : mods)
			{
				if(!m.equals(Mod) && m.name.toLowerCase().equals(requirement.toLowerCase()))
				{
					requirementFound = true;
					returnArray.addAll(arrangeRequirements(m));
				}
			}
			if(!requirementFound)
			{
				//internet
				for(onlineModDescription m : onlineModList)
				{
					if(m.name.toLowerCase().equals(requirement.toLowerCase()))
					{
						requirementFound = true;
						downloadsGUI.downloadMod modDownload = downloadMod((repoPath + m.link).replace(" ", "%20"), System.getProperty("user.dir") + "/mods/" + m.name + ".strifemod", m.name);
						try
						{
							//Wait for the download to complete
							modDownload.get();
						}
						catch(Exception e)
						{
							System.out.println(e);
						}
						File modFile = new File(System.getProperty("user.dir") + "/mods/" + m.name + ".strifemod");
						if(modFile.exists())
						{
							mod newMod = fileTools.loadModFile(modFile, this);
							returnArray.add(newMod);
							this.gui.tableData[this.gui.tableData.length - 1][0] = true;
						}
						break;
					}
				}
			}
			if(!requirementFound)
			{
				//Panic?
				//return empty list to not apply this mod nor any of its requirements?
				this.gui.showMessage("Could not find mod " + requirement + " requirement for " + Mod.name);
			}
		}
		returnArray.add(Mod);

		return returnArray;
	}
	
	void recursiveDelete(File file) {
        if (!file.exists())
            return;
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                recursiveDelete(f);
            }
        }
        file.delete();
    }

	void applyMods(){

		populateOnlineModsTable();

		toBeAdded.clear();
		alreadyDone.clear();

		//toBeAdded.put("modmanPlaceholder", "");

		//lets find out output.
		output = findArchives();

		boolean success = true;

		new File(s2Path+"/game/mods/").mkdirs();
		
		try {
			appliedMods = "";
			this.appliedModsList.clear();
			
			int o = 0;
			LinkedHashSet<mod> modsToApply = new LinkedHashSet<mod>();
			ArrayList<mod> currentMods = new ArrayList<mod>(this.mods);
			
			// Add framework mods 
			for (mod m: currentMods){
				if (!m.framework && (Boolean)gui.tableData[o++][0] == true)	{
					modsToApply.addAll(arrangeRequirements(m));
				}
			}

			// Add normal mods
			for (mod m: modsToApply)
			{
				if(!applyMod(m))
				{
					success = false;
					break;
				}
			}

			//step 4
			//apply any xml modifications.
			for (mod m: modsToApply){

				//System.out.println("looking for modifications: "+m.name);
				simpleStringParser parser = new simpleStringParser(m.xmlModifications.toString());
				while (true){
					String modification = parser.GetNextString();
					if (modification == null)
						break;

					//Is this a valid command?
					if (modification.toLowerCase().trim().equals("replace") || modification.toLowerCase().trim().equals("add before") || modification.toLowerCase().trim().equals("add after")){
						String file = parser.GetNextString();
						addFileIfNotAdded(toBeAdded, file);
						String fileText = toBeAdded.get(file);
						String text1 = parser.GetNextString();
						String text2 = parser.GetNextString();
						String newText = null;

						if (modification.toLowerCase().trim().equals("replace")){//replacement
							newText = fileText.replace(text1, text2);
							if (fileText == newText)
								gui.showMessage("Warning ("+m.name+"): \n\n"+text1+"\n\nnot found in "+file+"\n continuing anyway.", "Warning: couldn't find text.", 3);

						}else if (modification.toLowerCase().trim().equals("add before")){
							int insertPosition = fileText.indexOf(text1);
							if (insertPosition == -1)
								gui.showMessage("Warning ("+m.name+"): "+newText+" not found in "+file+"\n continuing anyway.", "Warning: couldn't find text.", 3);
							else
								newText = fileText.substring(0, insertPosition) + text2 + fileText.substring(insertPosition);

						}else if (modification.toLowerCase().trim().equals("add after")){
							int insertPosition = fileText.indexOf(text1)+text1.length();
							if (insertPosition == text1.length()-1)
								gui.showMessage("Warning ("+m.name+"): "+newText+" not found in "+file+"\n continuing anyway.", "Warning: couldn't find text.", 3);
							else
								newText = fileText.substring(0, insertPosition) + text2 + fileText.substring(insertPosition);
						}
						toBeAdded.remove(file);
						toBeAdded.put(file, newText);

						File f = new File(s2Path+"/game/mods/"+m.name.replace(" ", "_")+"/"+file);
						PrintWriter fos = new PrintWriter(f);
						fos.print(newText);
						fos.close();
					}
				}
			}
			// Create our lua script, if mods have been selected
			//String modmanLoader = fileTools.store(new FileInputStream(new File("modman.lua")));
			// It's much easier to hard-code the lua into the modman code.
			String modmanLoader = "--Long live... ModMan!\nlibThread.threadFunc(function()\n	wait(500)\n	if GetCvarString('host_version') ~= '{version}' then\n		GenericDialog(\n			Translate('Outdated Mods'), Translate('^rYour mods are out of date!\\n^*Do you want to shut down Strife, so you can open modman and re-apply mods?\\nOtherwise you may have game-breaking bugs!'), '', Translate('general_ok'), Translate('I\\\'ll deal'), \n			function()\n				Cmd('Quit')\n			end,\n			function()\n				--Cmd('Quit')\n			end,\n			nil,\n			nil,\n			true\n		)\n	end\nend)";
			modmanLoader = modmanLoader.replace("{version}", strifeVersion);
			PrintWriter pout = new PrintWriter(new File(s2Path+"/game/mods/modman.lua"));
			pout.print(modmanLoader);
			pout.close();
			
			saveConfig();

			if (success && gui.showYesNo("Success.", "Mod merge successful.\n\nLaunch Strife") == 0){ //0 is yes.
				launchStrife();
			}

		} catch (java.io.FileNotFoundException e){
			e.printStackTrace();
			gui.showMessage("Failure, archive open or non-existant\nAre you running Strife? Close it.\nHave you got a mod/resource file open? Close it.", "Failed to open files warning", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	String getLaunchParams(){
		String ret = "game";
		simpleStringParser parser = new simpleStringParser(appliedMods);
		while (true){
			String mod = parser.GetNextString();
			if (mod == null)
				break;
			ret += ";game/mods/"+mod.replace(" ", "_");
		}
		return ret;
	}

	void launchStrife(){
		if (output == null) output = findArchives();
		final ProcessBuilder builder = new ProcessBuilder(s2Path+"/bin/strife", 
				"-mod", getLaunchParams(),
				"-execute", "set host_autoexec \"\"\"script \\\\\\\"dofile 'game/mods/modman.lua'\\\\\\\"\"\"\"\""
		);
		try {
			Process p = builder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void addFileIfNotAdded(HashMap<String, String> toBeAdded, String file) throws IOException{
		if (toBeAdded.get(file) == null){//we need to add the file to toBeAdded
			String s2File = findFileInS2(archiveNumber, file);
			if (s2File == null){
				gui.showMessage("Error: file: "+file+" not found! Mod won't be applied.", "Mod merge unsuccessful", 1);
				System.out.println("Error: file: "+file+" not found! Mod won't be applied.");
				throw new IOException();
			}else{
				toBeAdded.put(file, s2File);
			}
		}
	}
	public int numMods = 0;
	void loadModFiles(){
		mods.clear();
		final File folder = new File(System.getProperty("user.dir")+"/mods");
		if(!folder.exists())
			folder.mkdirs();

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.getName().toLowerCase().endsWith(".strifemod")) {
				mods.add(fileTools.loadModFile(fileEntry, this));
			}
		}
		
		int i = 0;
		int numFrameworks = 0;
		while (i<mods.size()-numFrameworks){
			if (mods.get(i).framework){
				numFrameworks++;
				mods.add(mods.remove(i));//move to end
			}else
				i++;
		}
		if (!isDeveloper)
			numMods = mods.size()-numFrameworks;
		else
			numMods = mods.size();
		
		gui.tableData = new Object[numMods][5];
		for (i = 0;i<numMods;i++){
			gui.tableData[i]=mods.get(i).getData();
		}
		
		if (gui.table != null){
			String[] columnNames = {"Enabled", "Icon", "Name", "Author", "Version"};
			gui.table.setModel(new DefaultTableModel(gui.tableData, columnNames));
			gui.table.revalidate();
		}
	}

	public void loadFromConfig(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader("config.txt"));
			//S2 path
			s2Path = reader.readLine();
			if (s2Path == null)
				s2Path = "";
			//Developer mode
			if (reader.readLine().equals("1"))
				isDeveloper = true;
			else
				isDeveloper = false;
			//applied mods
			appliedMods = reader.readLine();
			if (appliedMods == null)
				appliedMods = "";

			reader.close();
		} catch (FileNotFoundException e1) {
			gui.showMessage("Welcome to Strife ModMan!\nPlease select your strife folder.",
					"Welcome!", JOptionPane.PLAIN_MESSAGE);

			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
			int returnVal = chooser.showOpenDialog(null);

			if(returnVal == JFileChooser.APPROVE_OPTION) {
				s2Path = chooser.getSelectedFile().getAbsolutePath();
				saveConfig();
			}else
				System.exit(0);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void setModStatuses(){
		for (Object[] o: gui.tableData){
			String tmp = (String)o[2];
			if (appliedMods.contains(tmp.substring(6, tmp.length()-7))){
				o[0] = true;
			}
		}
	}

	public void saveConfig(){
		try {
			PrintWriter pr = new PrintWriter(new File("config.txt"));
			pr.println(s2Path);
			if (isDeveloper)
				pr.println(1);
			else
				pr.println(0);
			pr.println(appliedMods);
			pr.close();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
	}

	private void checkForUpdate(){
		try {
			BufferedReader webIn;
			webIn = new BufferedReader(new InputStreamReader(new URL("http://pastebin.com/raw.php?i=aXuwRyFM").openStream()));
			String version = webIn.readLine();
			if (version.startsWith("<")) throw new Exception();
			String link = webIn.readLine();
			if (!version.equals(this.version)){
				gui.showMessage("Update found, automatically updating!");

				int kbChunks = 1 << 10; //1kb

				java.io.BufferedInputStream in = new java.io.BufferedInputStream(new java.net.URL(link).openStream());
				java.io.FileOutputStream fos = new java.io.FileOutputStream("ModManager.jar");
				java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,kbChunks*1024);
				byte[] data = new byte[kbChunks*1024];
				int x=0;
				while((x=in.read(data,0,kbChunks*1024))>=0)
				{
					bout.write(data,0,x);
				}
				bout.flush();
				bout.close();
				in.close();

				gui.showMessage("Restarting!");

				try {
					final ArrayList<String> command = new ArrayList<String>();
					command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
					command.add("-jar");
					command.add(new File("ModManager.jar").getAbsolutePath());
					System.out.println(command);
					final ProcessBuilder builder = new ProcessBuilder(command);
					builder.start();
				} catch (Exception e) {
					throw new IOException("Error while trying to restart the application", e);
				}
				System.exit(0);
			}
		} catch (Exception e) {
			gui.showMessage("Failed to update modman.\nThis could be because:\n\nYou aren't connected to the internet\nYou are using a proxy\nstrifehub.com is down.","Failure updateing modman",0);
		}
	}

	ArrayList<onlineModDescription> onlineModList = new ArrayList<onlineModDescription>();
	private onlineModDescription getOnlineModDescription(String name){
		for (onlineModDescription i:onlineModList)
			if (i.name.equals(name))
				return i;
		return null;
	}

	public void populateOnlineModsTable(){
		if (onlineModList.size()==0){
			//populate the online mods table.
			try {
				BufferedReader webIn;
				webIn = new BufferedReader(new InputStreamReader(new URL(repoPath+"rawModList.php").openStream()));
				String input;
				while ((input=webIn.readLine())!=null){
					if (input.startsWith("<")) throw new Exception();
					onlineModList.add(new onlineModDescription(input));
				}
			} catch (Exception e) {
				gui.showMessage("Failed to get mods.\nThis could be because:\n\nYou aren't connected to the internet\nYou are using a proxy\nstrifehub.com is down.","Failure getting online mod list",0);
				//e.printStackTrace();
				onlineModList.add(new onlineModDescription("example mod|Kairus101|1.0|1|gameplay|use ^q to make your text rainbow|RainbowAdder.strifemod|false"));
			}
		}
	}

	private boolean purgedOnlineList = false;
	public int numOnlineMods = 0;
	public void purgeOnlineModsTable(){
		if (purgedOnlineList) return;
		for (int i = 0;i<mods.size();i++){
			for (int o = 0;o<onlineModList.size();o++){
				if (mods.get(i).name.equals(onlineModList.get(o).name)){
					onlineModList.remove(o);
					o--;
				}
			}	
		}
		
		int i = 0;
		int numFrameworks = 0;
		while (i<onlineModList.size()-numFrameworks){
			if (onlineModList.get(i).framework.toLowerCase().equals("true")){
				numFrameworks++;
				onlineModList.add(onlineModList.remove(i));//move to end
			}else
				i++;
		}

		if (!isDeveloper)
			numOnlineMods = onlineModList.size()-numFrameworks;
		else
			numOnlineMods = onlineModList.size();
		
		purgedOnlineList = true;
	}

	private String checkForModUpdates(){

		populateOnlineModsTable();
		purgedOnlineList = false;

		String updated="";
		for (int i = 0; i < mods.size(); i++){
			mod m = mods.get(i);

			try {
				String latestVersion = null;
				String latestLink = null;

				//check mod repo
				onlineModDescription onlineModDesc = getOnlineModDescription(m.name);
				if (onlineModDesc!=null){
					latestVersion = onlineModDesc.version;
					latestLink = repoPath+onlineModDesc.link.replace(" ", "%20");
				}
				//check mod update link
				if (m.updateLink != null){
					BufferedReader webIn;
					webIn = new BufferedReader(new InputStreamReader(new URL(m.updateLink).openStream()));
					String version = webIn.readLine();
					String link = webIn.readLine();
					//if we are out of date, and the link version is higher than the repo version
					if (!version.equals(m.version) && (latestVersion==null || version.compareTo(latestVersion) > 0)){
						latestVersion = version;
						latestLink = link;
					}
				}
				//we have an update, grab it.
				if (latestVersion!=null && m.version.compareTo(latestVersion) < 0){
					updated += m.name + " "+m.version+" -> "+latestVersion+"\n";
					downloadMod(latestLink, m.fileName, m.name);
					if (!m.framework){
						gui.removeFromTable1(mods.indexOf(m));
					}
					mods.remove(i);
					i--;
				}
			} catch (MalformedURLException e) {
				gui.showMessage("Failed to update "+m.name+".");
				e.printStackTrace();
				System.exit(0);
			} catch (IOException e) {
				gui.showMessage("Failed to update "+m.name+".");
				e.printStackTrace();
				System.exit(0);
			}
		}
		return updated;
	}

	downloadsGUI.downloadMod downloadMod(String link, String filename, String name){
		return downloadsGui.downloadMod(link, filename, name);
	}


	class simpleStringParser{
		String text;
		simpleStringParser(String text){
			this.text = text;
		}
		public String GetNextString(){
			if (text.length() == 0) return null;
			int nextSeperator = text.indexOf("|");
			if (nextSeperator == -1) return null;
			String currentString = text.substring(0, nextSeperator);
			text = text.substring(nextSeperator+1);
			if (currentString.startsWith("\n")) currentString = currentString.substring(1);
			if (currentString.endsWith("\n")) currentString = currentString.substring(0,currentString.length()-1);
			return currentString;
		}
	}

	class onlineModDescription{
		String name;
		String author;
		String version;
		String rating;
		String category;
		String description;
		String link;
		String framework;
		onlineModDescription(String text){
			simpleStringParser ssp = new simpleStringParser(text);
			name=ssp.GetNextString();
			author=ssp.GetNextString();
			version=ssp.GetNextString();
			rating=ssp.GetNextString();
			category=ssp.GetNextString();
			description=ssp.GetNextString();
			link=ssp.GetNextString();
			framework=ssp.GetNextString();
		}
	}
}