package com.example.journalbeta;

import java.util.HashMap;
import java.util.Map;

public class JournalData {
    //general
    public static Boolean isAuthorized = false;
    public static String[] subjects = {};
    public static int[] ids = {};
    public static String[] marks = {};
    public static String username = "";
    public static String password = "";
    public static String class_id = "";
    public static String student_id = "";
    public static String user_id = "";
    //dairy
    public static Map<Integer, Integer> groups = new HashMap<>();
    public static String[] daySubjects = {};
    public static String[] descriptions = {};
    public static String[] homework = {};
    public static String[] dairyMarks = {};

}
