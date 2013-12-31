package org.Kairus.StrifeModMan;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

class mod{
	ArrayList<String> fileNames = new ArrayList<String>();
	String fileName;
	String name = "defaultModName";
	String author = "defaultModAuthor";
	String category = "defaultModCategory";
	String version = "0";
	String updateLink = null;
	ImageIcon image = null;
	StringWriter xmlModifications = new StringWriter();
	HashMap<String, String> patches = new HashMap<String, String>();
	HashMap<String, String> patchesToSave = new HashMap<String, String>();
	modMan modman;
	boolean replaceWithoutPatchCheck = false;


	mod(String fileName, modMan mm){
		this.fileName = fileName;
		modman = mm;
	}
	Object[] getData(){
		return new Object[]{false, image!=null?image:modman.gui.defaultIcon, "<html>"+name+"</html>", author, version, category};
	}
}