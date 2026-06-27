package com.mojang.rubydung;

public class DebugLogger {
  public static boolean ENABLE_AI_LOGS = false;
  
  public static boolean ENABLE_ML_LOGS = false;
  
  public static boolean ENABLE_SYSTEM_LOGS = false;
  
  public static boolean ENABLE_FPS_LOGS = false;
  
  public static boolean ENABLE_CONTROLS_LOGS = false;
  
  public static void ai(String message) {
    if (ENABLE_AI_LOGS)
      System.out.println("[AI] " + message); 
  }
  
  public static void ml(String message) {
    if (ENABLE_ML_LOGS)
      System.out.println("[ML] " + message); 
  }
  
  public static void system(String message) {
    if (ENABLE_SYSTEM_LOGS)
      System.out.println("[SYSTEM] " + message); 
  }
  
  public static void fps(String message) {
    if (ENABLE_FPS_LOGS)
      System.out.println("[FPS] " + message); 
  }
  
  public static void controls(String message) {
    if (ENABLE_CONTROLS_LOGS)
      System.out.println("[CONTROLS] " + message); 
  }
  
  public static void enableAllLogs() {
    ENABLE_AI_LOGS = true;
    ENABLE_ML_LOGS = true;
    ENABLE_SYSTEM_LOGS = true;
    ENABLE_FPS_LOGS = true;
    ENABLE_CONTROLS_LOGS = true;
  }
  
  public static void disableAllLogs() {
    ENABLE_AI_LOGS = false;
    ENABLE_ML_LOGS = false;
    ENABLE_SYSTEM_LOGS = false;
    ENABLE_FPS_LOGS = false;
    ENABLE_CONTROLS_LOGS = false;
  }
}
