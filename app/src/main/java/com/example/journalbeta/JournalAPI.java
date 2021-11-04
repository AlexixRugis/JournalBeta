package com.example.journalbeta;


import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JournalAPI {

    public static void Login(final String username, String password, final Context context) {
        try {
            final RequestQueue queue = Volley.newRequestQueue(context);

            final String crypto_password = Crypto.getSHA1(password.getBytes());

            Map<String, String> paramsLogin = new HashMap<>();
            paramsLogin.put("l", username);
            paramsLogin.put("p", crypto_password);

            JournalRequest loginRequest = new JournalRequest("http://95.79.27.250:8085/login", paramsLogin, context,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", response);
                            String[][] student = JournalAPI.parseDataFromArray(response);

                            if (student[0].length > 1) {
                                JournalData.student_id = student[0][6];
                                JournalData.username = username;
                                JournalData.password = crypto_password;
                                JournalData.user_id = student[0][0];
                                getClassData(context);
                            } else {
                                Toast.makeText(context, R.string.status_invalid, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            queue.add(loginRequest);
        }
        catch (Exception ignored) {
        }
    }

    private static void getClassData(final Context context) {

        try {
            final RequestQueue queue = Volley.newRequestQueue(context);

            Map<String, String>  paramsClass = new HashMap<>();
            paramsClass.put("currentDate", new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));
            paramsClass.put("student", JournalData.student_id);
            paramsClass.put("uchYear", new SimpleDateFormat("yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));
            paramsClass.put("uchId", "1");

            JournalAPI.JournalRequest requestClass = new JournalAPI.JournalRequest("http://95.79.27.250:8085/act/GET_STUDENT_CLASS", paramsClass, context,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", response);

                            String[][] cls = JournalAPI.parseDataFromArray(response);
                            JournalData.class_id = cls[0][0];

                            JournalAPI.getStudentGroups(context);

                        }
                    });

            queue.add(requestClass);
        }
        catch (Exception ignored) {}


    }



    private static void getSubjects(final Context context) {
        try {
            RequestQueue queue = Volley.newRequestQueue(context);

            Map<String, String>  params = new HashMap<>();
            params.put("student", JournalData.student_id);
            params.put("uchYear", new SimpleDateFormat("yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));
            params.put("cls", JournalData.class_id);

            JournalRequest request = new JournalRequest("http://95.79.27.250:8085/act/GET_STUDENT_SUBJECTS", params, context,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", response);

                            String[][] subjects = parseDataFromArray(response);
                            int subjCount = subjects.length;
                            JournalData.subjects = new String[subjCount];
                            JournalData.ids = new int[subjCount];
                            JournalData.marks = new String[subjCount];
                            for (int i = 0; i < subjCount; i++) {
                                JournalData.subjects[i] = subjects[i][1];
                                JournalData.ids[i] = Integer.parseInt(subjects[i][0].replace(" ", ""));
                            }
                            if(MarksFragment.instance != null)
                                MarksFragment.instance.updateList();

                            JournalData.isAuthorized = true;

                            getMarks(context);
                            getLessons(context, new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));

                            Intent intent = new Intent(context, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    });

            queue.add(request);
        }
        catch (Exception ignored) {}
    }

    private static void getStudentGroups(final Context context) {
        try {
            RequestQueue queue = Volley.newRequestQueue(context);

            Map<String, String>  params = new HashMap<>();
            params.put("student", JournalData.student_id);
            params.put("cls", JournalData.class_id);

            JournalRequest request = new JournalRequest("http://95.79.27.250:8085/act/GET_STUDENT_GROUPS", params, context,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", response);

                            JournalData.groups.clear();
                            String[][] responseParsed = parseDataFromArray(response);

                            if(responseParsed[0].length > 1) {
                                for (String[] strings : responseParsed) {
                                    Integer subjID = Integer.parseInt(strings[1].replaceAll(" ", ""));
                                    Integer groupID = Integer.parseInt(strings[2].replaceAll(" ", ""));
                                    JournalData.groups.put(subjID, groupID);
                                }
                            }

                            Log.d("subjGr", JournalData.groups.toString());

                            JournalAPI.getSubjects(context);

                            }
                        });
            queue.add(request);
        }
        catch (Exception ignored) {}
    }

    public static void getMarks(final Context context) {
        try {

            if(!JournalData.isAuthorized) return;

            RequestQueue queue = Volley.newRequestQueue(context);

            Map<String, String>  params = new HashMap<>();
            params.put("student", JournalData.student_id);
            params.put("parallelClasses", "");
            params.put("cls", JournalData.class_id);

            JournalRequest request = new JournalRequest("http://95.79.27.250:8085/act/GET_STUDENT_JOURNAL_DATA", params, context,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", response);

                            String[][] marks = parseDataFromArray(response);

                            Arrays.fill(JournalData.marks, "");

                            if(marks[0].length > 1) {
                                for (String[] mark : marks) {
                                    int subjID = Integer.parseInt(mark[10].replaceAll(" ", ""));
                                    for (int j = 0; j < JournalData.ids.length; j++) {
                                        if (subjID == JournalData.ids[j]) {
                                            JournalData.marks[j] = JournalData.marks[j] + mark[2] + " ";
                                            break;
                                        }
                                    }
                                }
                            }
                            if(MarksFragment.instance != null)
                                MarksFragment.instance.updateList();
                        }
                    }) ;
            queue.add(request);
        }
        catch (Exception ignored) {}
    }

    public static void getLessons(Context context, String date) {
        try {

            if(!JournalData.isAuthorized) return;

            RequestQueue queue = Volley.newRequestQueue(context);

            Map<String, String>  params = new HashMap<>();
            params.put("student", JournalData.student_id);
            params.put("pClassesIds", "");
            params.put("cls", JournalData.class_id);
            params.put("begin_dt", date);
            params.put("end_dt", date);

            JournalRequest request = new JournalRequest("http://95.79.27.250:8085/act/GET_STUDENT_DAIRY", params, context,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", response);

                            String[][] dairyLessons = parseStringsFromArray(response);
                            String[][] responseParsed = parseDataFromArray(response);

                            ArrayList<String> descriptions = new ArrayList<>();
                            ArrayList<String> homework = new ArrayList<>();
                            ArrayList<String> daySubjects = new ArrayList<>();
                            ArrayList<String> dayMarks = new ArrayList<>();

                            if(responseParsed[0].length > 1) {

                                for (int lessonId = 0; lessonId < dairyLessons.length; lessonId++) {
                                    int subjID = Integer.parseInt(responseParsed[lessonId][8].replaceAll(" ", ""));

                                    boolean isInGroup = true;
                                    if (JournalData.groups.containsKey(subjID))
                                        isInGroup = !Objects.equals(JournalData.groups.get(subjID), Integer.parseInt(responseParsed[lessonId][responseParsed[lessonId].length - 6].replaceAll(" ", "")));

                                    if (isInGroup) {
                                        String[] lesson = dairyLessons[lessonId];
                                        daySubjects.add(getSubjectByID(subjID));
                                        descriptions.add(lesson[0]);
                                        homework.add(lesson[1]);
                                        dayMarks.add(lesson[2]);
                                    }
                                }
                            }

                            JournalData.descriptions = descriptions.toArray(new String[0]);
                            JournalData.homework = homework.toArray(new String[0]);
                            JournalData.daySubjects = daySubjects.toArray(new String[0]);
                            JournalData.dairyMarks = dayMarks.toArray(new String[0]);

                            if(TimetableFragment.instance != null) {
                                TimetableFragment.instance.updateList();
                            }

                        }
                    }) ;
            queue.add(request);
        }
        catch (Exception ignored) {}
    }

    private static String[][] parseDataFromArray(String input) {
        String[] arr = input.split("],");
        String[][] out = new String[arr.length][];

        for(int i=0; i < arr.length; i++) {
            out[i] = arr[i].replace("\n", "")
                    .replace("[","")
                    .replace("]", "")
                    .replace("\"", "")
                    .split(",");
        }

        return out;

    }

    private static String[][] parseStringsFromArray(String lessons) {
        String[] strings = lessons.split("],");
        String[][] output = new String[strings.length][3];
        Pattern pattern = Pattern.compile("\".*?\",");
        for(int subStringIndex = 0; subStringIndex < strings.length; subStringIndex++) {
            Matcher matcher = pattern.matcher(strings[subStringIndex]);
            int matches = 0;
            while (matcher.find() && matches<3) {
                output[subStringIndex][matches] = matcher.group().substring(1, matcher.group().length()-2).replace("\\n","\n").replace("\\\"", "\"");
                matches++;
            }
        }
        return output;
    }

    private static String getSubjectByID(int id) {
        for(int j = 0; j < JournalData.ids.length; j++) {
            if(id == JournalData.ids[j]) {
                return JournalData.subjects[j];
            }
        }
        return "";
    }

    public static class JournalRequest extends StringRequest {

        Map<String, String> params;

        public JournalRequest(String url, Map<String, String> requestBody, final Context applicationContext, Response.Listener<String> listener) {
            super(Method.POST, url, listener, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Error.Response", error.toString());
                    Toast.makeText(applicationContext, R.string.status_error, Toast.LENGTH_SHORT).show();
                }
            });
            params = requestBody;
        }

        private String getCookies() {
            return "ys-userId=n%3A" +
                    JournalData.user_id +
                    ";ys-user=s%3A" +
                    JournalData.username +
                    ";ys-password=s%3A" +
                    JournalData.password;
        }

        @Override
        public Map<String, String> getHeaders() {
            Map<String, String>  headers = new HashMap<>();
            headers.put("Cookie", getCookies());
            Log.d("Headers", headers.toString());

            return headers;
        }

        @Override
        protected Map<String, String> getParams() {
            return params;
        }
    }

}

