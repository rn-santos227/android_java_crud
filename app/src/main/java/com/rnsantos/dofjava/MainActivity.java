package com.rnsantos.dofjava;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.rnsantos.dofjava.adapters.TasksAdapter;
import com.rnsantos.dofjava.model.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static ArrayList<Task> tasks;
    Button btn_create;
    ListView list_tasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tasks = getTasks();
        TasksAdapter adapter = new TasksAdapter(this, tasks);

        list_tasks = findViewById(R.id.list_tasks);
        list_tasks.setAdapter(adapter);

        //Insert Data to the List

        btn_create = findViewById(R.id.btn_create);
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final View inputLayout = getLayoutInflater().inflate(R.layout.task_input_form, null);

                builder.setView(inputLayout);
                builder.setTitle("Create New Task");
                final EditText txt_title = inputLayout.findViewById(R.id.txt_title);
                final EditText txt_activity = inputLayout.findViewById(R.id.txt_activity);
                final DatePicker txt_date = inputLayout.findViewById(R.id.txt_date);

                builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int id = tasks.size() <= 0 ? 0 : (tasks.get(tasks.size() - 1).getId()) + 1;
                        String date = txt_date.getYear() + "-" + (txt_date.getMonth() + 1) + "-" + txt_date.getMonth() + "T02:00:00.000Z";
                        String title = txt_title.getText().toString();
                        String activity = txt_activity.getText().toString();
                        Task new_task = new Task(id, title, activity, date, 1);
                        adapter.add(new_task);
                        createTask(new_task);
                        txt_activity.setText("");
                        txt_title.setText("");
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void createTask(Task task) {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "http://192.168.1.2:3000/tasks/create";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("title", task.getTitle());
            jsonBody.put("activity", task.getActivity());
            jsonBody.put("date", task.getDate());
            final String requestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Toast.makeText(MainActivity.this, "Task has been added", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            queue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.1.2:3000/tasks"; //Set the Backend API to own IP Address through ipconfig.

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray array = object.getJSONArray("tasks");
                    for(int i=0;i<array.length();i++) {
                        JSONObject response_task = array.getJSONObject(i);
                        Task new_task = new Task(
                                response_task.getInt("id"),
                                response_task.getString("title"),
                                response_task.getString("activity"),
                                response_task.getString("date"),
                                response_task.getInt("status")
                        );
                        tasks.add(new_task);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.toString());
            }
        });

        queue.add(stringRequest);
        return tasks;
    }
}