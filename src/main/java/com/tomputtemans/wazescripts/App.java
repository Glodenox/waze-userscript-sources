package com.tomputtemans.wazescripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class App extends JFrame {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		App app = new App();
		app.process();
		System.exit(0);
	}
	
	public void process() {
		JFileChooser fileChooser = new JFileChooser("D:\\Projects\\waze-scripts\\src\\main\\resources");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setDialogTitle("Provide the urls CSV file to process");
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
        	return;
        }
		File source = fileChooser.getSelectedFile();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogTitle("In which folder should the script store the files");
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
        	return;
        }
        File targetFolder = fileChooser.getSelectedFile();
		try (FileReader fr = new FileReader(source);
				BufferedReader reader = new BufferedReader(fr)) {
			String currentLine;
			while ((currentLine = reader.readLine()) != null) {
				Thread.sleep(1100);
				StringTokenizer tokenizer = new StringTokenizer(currentLine, ";");
				String url = tokenizer.nextToken().trim();
				String name = tokenizer.nextToken() + ".user.js";
				if ("".equals(url) || url.contains("chrome.google.com/webstore")) {
					continue;
				}
				if (url.contains("greasyfork.org")) { // remove language specifier
					url = url.replaceAll("\\.org/[a-z]{2}/", ".org/");
				}
				if (url.contains("greasyfork.org") && !url.endsWith(".user.js")) { // add link to raw data
					url = url.replaceAll("(/code)?/?$", "/code/" + name);
				}
				if (url.contains("pastebin") && !url.contains("/raw/")) { // add link to raw data
					url = url.replaceAll("pastebin\\.com/", "pastebin.com/raw/");
				}
				System.out.println("Retrieving " + url + ": ");
				URL location = new URL(url);
				File target = new File(targetFolder.getAbsolutePath() + "\\" + name);
				try (InputStream in = location.openStream()) {
					Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
					System.out.println("Data stored to " + target.getName());
				} catch (IOException e) {
					System.err.println("Failed to retrieve data: " + e.getMessage());
				}
			}
			System.out.println("Retrieval completed.");
		} catch (IOException | InterruptedException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
