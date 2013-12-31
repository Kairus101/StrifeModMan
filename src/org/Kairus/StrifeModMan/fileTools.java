package org.Kairus.StrifeModMan;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class fileTools {

	static String store(InputStream is) throws IOException {
		BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
		StringWriter sw = new StringWriter();
		for (;;) {
			String line = rdr.readLine();
			if (line == null) break;
			sw.write(line+"\n");
		}
		is.close();
		return sw.toString();
	}

	static mod loadModFile(File file, modMan mm){
		//get the zip file content
		try {
			FileInputStream fis = new FileInputStream(file);
			ZipInputStream zis = new ZipInputStream(fis);
			mod mod = new mod(file.getAbsolutePath(), mm);
			//get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();
			while(ze!=null){
				String fileName = ze.getName();
				if (ze.isDirectory()){
					ze = zis.getNextEntry();
					continue;
				}
				//System.out.println(fileName);
				if (fileName.toLowerCase().equals("icon.png")){
					//mod.image = new ImageIcon(ImageIO.read(zis));
					Image img = ImageIO.read(zis);
					mod.image = new ImageIcon(img.getScaledInstance(55, 55, Image.SCALE_SMOOTH));
				}else if (fileName.toLowerCase().startsWith("patch_")){
					StringWriter sw = new StringWriter();
					BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
					String line;
					while ((line = reader.readLine())!=null)
						sw.write(line+"\n");
					mod.patches.put(fileName.substring(6), sw.toString());
					addIfNotThere(mod.fileNames, fileName.substring(6));
				}else if (fileName.toLowerCase().equals("mod.txt")){
					BufferedReader reader = new BufferedReader(new InputStreamReader(zis));
					String line;
					boolean readingModifications = false;
					while ((line = reader.readLine())!=null)
						if (readingModifications && !line.startsWith("#")){
							mod.xmlModifications.write(line+"\n");
						}else if (line.toLowerCase().startsWith("name: "))
							mod.name = line.substring(6);
						else if (line.toLowerCase().startsWith("author: "))
							mod.author = line.substring(8);
						else if (line.toLowerCase().startsWith("version: "))
							mod.version = line.substring(9);
						else if (line.toLowerCase().startsWith("category: "))
							mod.category = line.substring(10);
						else if (line.toLowerCase().startsWith("update link: "))
							mod.updateLink = line.substring(13);
						else if (line.toLowerCase().startsWith("replacewithoutpatchcheck: "))
							if (line.substring(26).equals("true"))
								mod.replaceWithoutPatchCheck = true;
						else if (line.toLowerCase().equals("start_modifications"))
							readingModifications = true;
				}else if (! fileName.toLowerCase().startsWith("original/")){
					addIfNotThere(mod.fileNames, fileName);
				}
				ze = zis.getNextEntry();
			}
			zis.close();
			fis.close();
			return mod;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	static void remakeZipEntry(String newName, File zipFile, String[] filesToDelete, String[] namesToAdd, String[] contents) throws IOException {
		byte[] buf = new byte[1024];
		ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(newName));
		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			boolean toBeDeleted = entry.isDirectory();
			for (String f : filesToDelete) {
				if (f.equals(name)) {
					toBeDeleted = true;
					break;
				}
			}
			if (!toBeDeleted) {
				//System.out.println(name);
				zout.putNextEntry(new ZipEntry(name));
				int len;
				while ((len = zin.read(buf)) > 0)
					zout.write(buf, 0, len);
			}
			entry = zin.getNextEntry();
		}
		for (int i = 0;i<namesToAdd.length;i++){
			zout.putNextEntry(new ZipEntry(namesToAdd[i]));
			PrintWriter pr = new PrintWriter(zout);
			pr.print(contents[i]);
			pr.flush();
		}
		zin.close();
		zout.close();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static void addIfNotThere(ArrayList a, Object o){
		for (Object a2: a)
			if (o.equals(a2))
				return;
		a.add(o);
	}
}
