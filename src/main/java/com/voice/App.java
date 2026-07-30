package com.voice;

import java.awt.Robot; // Added for mouse wheel control
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {

    // Global tracker to store the process executable name of the last launched app
    private static String lastOpenedProcess = null;
    private static String lastOpenedName = "";

    // Simple file model for searching
    static class ProjectFile {
        String name;
        String path;
        boolean isAdminOnly;

        ProjectFile(String name, String path, boolean isAdminOnly) {
            this.name = name;
            this.path = path;
            this.isAdminOnly = isAdminOnly;
        }
    }

    public static void main(String[] args) {
        LibVosk.setLogLevel(LogLevel.WARNINGS);

        AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);                  // Main audio format and hardware check eg : Microphone....so on 
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try {
            // Initialize Robot for mouse scrolling and system keys
            Robot robot = new Robot();                          //  [ robot is hella essential cuz its responsible for user input for example
                                                                // mouse click, scrolling up / down and so on... ]

            Model model = new Model("models/vosk-model-small-en-us-0.15");
            Recognizer recognizer = new Recognizer(model, 16000);             // zeee brainnnn.......human english / vocabulary and stuff you know xd 

            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();                     //microphone starts here due to which we get the text of it hearing us 

            System.out.println("Microphone activated! Speak into your mic...");

            byte[] buffer = new byte[4096];
            int bytesRead;
            ObjectMapper objectMapper = new ObjectMapper();

            // Set current user role (false = regular user, true = admin)
            boolean isUserAdmin = false; 

            // Auto-scan project files dynamically
            List<ProjectFile> fileIndex = scanDirectory(new File("."), new ArrayList<>());

            while (true) {
                bytesRead = microphone.read(buffer, 0, buffer.length);

                if (bytesRead > 0) {
                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        String jsonResult = recognizer.getResult();
                        JsonNode node = objectMapper.readTree(jsonResult);
                        String text = node.get("text").asText();

                        if (!text.isEmpty()) {
                            System.out.println(" || Command Registered : " + text);

                            String command = text.toLowerCase();

                            // Normalizing input (e.g. "d o c s" -> "docs", "v two" -> "v2")
                            String normalizedCommand = normalizeVoiceInput(command);

                            if (command.contains("open edge")) {
                                try {
                                    new ProcessBuilder("cmd", "/c", "start", "msedge").start();   // M i c r o s o f t     E d g e 
                                    lastOpenedProcess = "msedge.exe";
                                    lastOpenedName = "Edge";
                                    System.out.println("Opening Edge...");
                                } catch (Exception ex) {
                                    System.out.println("Failed to open Edge.");
                                    ex.printStackTrace();
                                }
                            } else if (command.contains("open chrome")) {
                                try {
                                    new ProcessBuilder("cmd", "/c", "start", "chrome").start();   // C h r o m e
                                    lastOpenedProcess = "chrome.exe";
                                    lastOpenedName = "Chrome";
                                    System.out.println("Opening Chrome...");
                                } catch (Exception ex) {
                                    System.out.println("Failed to open Chrome.");
                                    ex.printStackTrace();
                                }
                            }
                            else if (command.contains("open games")){
                                try{
                                    Runtime.getRuntime().exec("cmd /c start com.epicgames.launcher://store");   //E P I C     G a m e s 
                                    lastOpenedProcess = "EpicGamesLauncher.exe";
                                    lastOpenedName = "Epic Games";
                                    System.out.println("Opening Epic Games....");
                                } catch(Exception ex){
                                    System.out.println("Failed to open Epic Games");
                                    ex.printStackTrace();
                                }
                            }
                            else if(command.contains("open steam")){
                                try{
                                    Runtime.getRuntime().exec("cmd /c start steam://open/main");
                                    lastOpenedProcess = "steam.exe";
                                    lastOpenedName = "Steam";
                                    System.out.println("Opening Steam...");      // S t e a m     
                                } catch(Exception ex){
                                    System.out.println("Failed to open Steam");
                                    ex.printStackTrace();
                                }
                            }
                            // --- OPEN INSTAGRAM ON EDGE ---
                            else if (command.contains("open instagram") || command.contains("search instagram") || command.contains("instagram")) {
                                try {
                                    new ProcessBuilder("cmd", "/c", "start", "msedge", "https://www.instagram.com").start();   // Opens Instagram in Edge
                                    lastOpenedProcess = "msedge.exe";
                                    lastOpenedName = "Edge (Instagram)";
                                    System.out.println("Opening Instagram on Edge...");
                                } catch (Exception ex) {
                                    System.out.println("Failed to open Instagram on Edge.");
                                    ex.printStackTrace();
                                }
                            }
                            // --- VOLUME CONTROL (FIXED) ---
                            else if (command.contains("volume up") || command.contains("turn up volume") || command.contains("increase volume")) {
                                pressVolumeKey("0xAF", 5); // 0xAF = Volume Up (pressed 5 times)
                                System.out.println("Increasing volume...");
                            }
                            else if (command.contains("volume down") || command.contains("turn down volume") || command.contains("decrease volume")) {
                                pressVolumeKey("0xAE", 5); // 0xAE = Volume Down (pressed 5 times)
                                System.out.println("Decreasing volume...");
                            }
                            else if (command.contains("mute volume") || command.contains("mute audio") || command.contains("unmute")) {
                                pressVolumeKey("0xAD", 1); // 0xAD = Mute Toggle
                                System.out.println("Toggling volume mute...");
                            }
                            else if (command.contains("scroll down")) {
                                robot.mouseWheel(6); // Positive value scrolls down              // main scrolling part starts here 
                                System.out.println("Scrolling down...");
                            }
                            else if (command.contains("scroll up")) {
                                robot.mouseWheel(-6); // Negative value scrolls up
                                System.out.println("Scrolling up...");
                            }
                            else if (command.contains("exit") || command.contains("close process") || command.equals("close")) {
                                if (lastOpenedProcess != null) {
                                    try {
                                        Runtime.getRuntime().exec("taskkill /IM " + lastOpenedProcess + " /F");             //responsible for closing last task first its gona verify 
                                                                                                                            //if ur registered word is close if yes its gona close last 
                                                                                                                            //last opened task 
                                        System.out.println("Exiting last opened app: " + lastOpenedName);
                                        lastOpenedProcess = null;
                                        lastOpenedName = "";
                                    } catch (Exception ex) {
                                        System.out.println("Failed to exit application.");
                                        ex.printStackTrace();
                                    }
                                } else {
                                    System.out.println("No recently opened application recorded to exit.");   //its prtty clear in code but just for reference lol, 
                                                                                                              //if theres nothing to close its gona return the 
                                                                                                              // print statement ofc 
                                }
                            }
                            // File Search (Option A Security + Auto-Scanned Directory Index)
                            else if (normalizedCommand.startsWith("find ") || normalizedCommand.startsWith("search ")) {
                                String searchTerm = normalizedCommand.replace("find ", "").replace("search ", "").trim();
                                System.out.println("Searching files for: " + searchTerm);

                                boolean found = false;
                                for (ProjectFile file : fileIndex) {
                                    // Option A: Skip admin files silently if the user isn't an admin
                                    if (file.isAdminOnly && !isUserAdmin) continue;

                                    if (file.name.toLowerCase().contains(searchTerm)) {
                                        System.out.println("Found match: " + file.name + " at path: " + file.path);
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    System.out.println("No files found matching: " + searchTerm);
                                }
                            }
                        }
                    } else {
                        String jsonPartial = recognizer.getPartialResult();
                        JsonNode node = objectMapper.readTree(jsonPartial);
                        String partialText = node.get("partial").asText();

                        if (!partialText.isEmpty()) {
                            System.out.print("\rHearing : " + partialText);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred with the mic or model file.");
            e.printStackTrace();
        }
    }

    // Helper function to trigger Windows system volume keys cleanly
    private static void pressVolumeKey(String keyCodeHex, int repeatCount) {
        try {
            String psCommand = "for ($i=0; $i -lt " + repeatCount + "; $i++) { "
                             + "(New-Object -ComObject WScript.Shell).SendKeys([char]" + keyCodeHex + ") "
                             + "}";
            new ProcessBuilder("powershell", "-Command", psCommand).start();
        } catch (Exception e) {
            System.out.println("Failed to send volume key event.");
        }
    }

    //  HERE STARTS THE FILE SEARCH THINGY make sure to look here if theres anything wrong with searching 

    // Helper 1: Standard Java regex normalizer (stretches single letters and basic numbers)
    private static String normalizeVoiceInput(String input) {
        if (input == null) return "";

        // 1. Convert simple spoken numbers
        String cleaned = input
            .replaceAll("(?i)\\b(two)\\b", "2")
            .replaceAll("(?i)\\b(three)\\b", "3")
            .replaceAll("(?i)\\b(four)\\b", "4");

        // 2. Stitch single letters ("d o c s" -> "docs") using java.util.regex.Matcher
        Pattern pattern = Pattern.compile("(?:\\b[a-zA-Z0-9]\\b\\s*){2,}");
        Matcher matcher = pattern.matcher(cleaned);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group().replaceAll("\\s+", ""));
        }
        matcher.appendTail(sb);

        return sb.toString().toLowerCase().trim();
    }

    // Helper 2: Scans current directory tree dynamically on launch
    private static List<ProjectFile> scanDirectory(File dir, List<ProjectFile> fileList) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && !file.getName().startsWith(".")) {
                    scanDirectory(file, fileList);
                } else if (file.isFile()) {
                    boolean isAdmin = file.getName().contains("admin") || file.getName().endsWith(".env");
                    fileList.add(new ProjectFile(file.getName(), file.getPath(), isAdmin));
                }
            }
        }
        return fileList;
    }
}
