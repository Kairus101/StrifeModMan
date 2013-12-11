package org.Kairus.StrifeModMan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

public class modMan {
	private static final long serialVersionUID = 1L;
	String version = "1.0";
	public static void main(String[] args) {
		new modMan();
	}
	GUI gui;
	String s2Path = null;
	String appliedMods = "";
	boolean isDeveloper = false;
	ArrayList<mod> mods = new ArrayList<mod>();
	byte[] buffer = new byte[1024];

	public modMan()
	{
		gui = new GUI(this);
		
		//load config
		loadFromConfig();

		//init GUI
		loadModFiles();
		
		setModStatuses();
		
		gui.init();
	}

	void applyMods(){
		HashMap<String, String> toBeZipped = new HashMap<String, String>();
		HashMap<String, Boolean> alreadyZipped = new HashMap<String, Boolean>();

		String output = s2Path+"/game/resources1.s2z";
		String path = s2Path+"/game/resources0.s2z";
		try {
			FileOutputStream fos = new FileOutputStream(output);
			ZipOutputStream zos = new ZipOutputStream(fos);
			ZipFile zipFile = new ZipFile(path);

			appliedMods = "";
			int o = 0;
			for (mod m: mods){
				if ((Boolean)gui.tableData[o++][0] == false)
					continue;
				appliedMods += m.name+"|";
				m.patchesToSave.clear();
				ZipFile sourceZip = new ZipFile(m.fileName);
				for (String s: m.fileNames){
					//target
					ZipEntry entry = zipFile.getEntry(s);
					//source
					if (entry==null){ //new file
						ZipEntry sourceFile = sourceZip.getEntry(s);
						InputStream zis = sourceZip.getInputStream(sourceFile);

						//output
						if (alreadyZipped.get(s) != null){
							gui.showMessage("Warning ("+m.name+"): Duplicate file with no originals:\n");
							continue;
						}
						alreadyZipped.put(s, true);
						ZipEntry ze = new ZipEntry(s);
						zos.putNextEntry(ze);

						int len;
						while ((len = zis.read(buffer)) > 0) {
							zos.write(buffer, 0, len);
						}
						zis.close();
						zos.closeEntry();
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
						String current;

						if (toBeZipped.get(s) != null){ // not the first mod
							current = toBeZipped.get(s);
						}else
							current = fileTools.store(zipFile.getInputStream(entry)); //current

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
								gui.showMessage("Problem in "+m.name+"!\n"+s+"\nFound in resources0, but no patch and no original file.\nIf you are developing this mod, put the official file in your mod pack, under \"original/"+s+"\".");
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
							gui.showMessage("Problem in "+m.name+":"+s+"\nApplying diff: "+patch.get(error));
							continue;
						}
						toBeZipped.remove(s);
						toBeZipped.put(s, (String)result[0]);

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
			}

			//step 4
			//apply any xml modifications.
			o = 0;
			for (mod m: mods){
				if ((Boolean)gui.tableData[o++][0] == false)
					continue;

				//System.out.println("looking for modifications: "+m.name);
				simpleStringParser parser = new simpleStringParser(m.xmlModifications.toString());
				while (true){
					String modification = parser.GetNextString();
					if (modification == null)
						break;

					//Is this a valid command?
					if (modification.toLowerCase().trim().equals("replace") || modification.toLowerCase().trim().equals("add before") || modification.toLowerCase().trim().equals("add after")){
						String file = parser.GetNextString();
						addFileIfNotAdded(toBeZipped, file, zipFile);
						String fileText = toBeZipped.get(file);
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
						toBeZipped.remove(file);
						toBeZipped.put(file, newText);
					}
				}
			}

			//step 5
			//put new file in resources
			Iterator<?> it = toBeZipped.entrySet().iterator();
			while (it.hasNext()) {
				@SuppressWarnings("unchecked")
				Map.Entry<String, String> pairs = (Map.Entry<String, String>)it.next();		        
				ZipEntry ze = new ZipEntry(pairs.getKey());
				zos.putNextEntry(ze);
				PrintWriter writer = new PrintWriter(zos);
				writer.println(pairs.getValue());
				writer.flush();
				zos.closeEntry();
				it.remove(); // avoids a ConcurrentModificationException
			}
			//remember close it
			zos.close();
			saveConfig();
			gui.showMessage("Success.", "Mod merge successful", 3);
		} catch (java.io.FileNotFoundException e){
			e.printStackTrace();
			gui.showMessage("Failure, archive open or non-existant", "Failed to open files warning", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addFileIfNotAdded(HashMap<String, String> toBeZipped, String file, ZipFile zipFile) throws IOException{
		if (toBeZipped.get(file) == null){//we need to add the file to toBeZipped
			ZipEntry entry = zipFile.getEntry(file);
			if (entry == null){
				gui.showMessage("Error: file: "+file+" not found! Mod won't be applied.", "Mod merge unsuccessful", 1);
				throw new IOException();
			}else{
				toBeZipped.put(file, fileTools.store(zipFile.getInputStream(entry)));
			}
		}
	}

	void loadModFiles(){
		final File folder = new File(System.getProperty("user.dir")+"/mods");
		if(!folder.exists())
			folder.mkdirs();

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.getName().toLowerCase().endsWith(".strifemod")) {
				mods.add(fileTools.loadModFile(fileEntry, this));
			}
		}
		gui.tableData = new Object[mods.size()][5];
		for (int i = 0;i<mods.size();i++){
			gui.tableData[i]=mods.get(i).getData();
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
		for (Object[] o: gui.tableData)
			if (appliedMods.contains((String)o[2]))
				o[0] = true;
				
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
}