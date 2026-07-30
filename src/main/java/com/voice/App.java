package com.voice;

import java.awt.Robot; // Added for mouse wheel control

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

    public static void main(String[] args) {
        LibVosk.setLogLevel(LogLevel.WARNINGS);

        AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);                  // Main audio format and hardware check eg : Microphone....so on 
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try {
            // Initialize Robot for mouse scrolling
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
                                    System.out.println(("Opening Epic Games...."));
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
                                    System.out.println(("Opening Steam..."));              // S t e a m     
                                } catch(Exception ex){
                                    System.out.println("Failed to open Steam");
                                    ex.printStackTrace();
                                }
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
                                                                                                              //if theres nothing to close its gona return the print statement ofc 
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
}
