package com.rnsantos.dofjava.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.rnsantos.dofjava.MainActivity;
import com.rnsantos.dofjava.R;
import com.rnsantos.dofjava.model.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class TasksAdapter extends ArrayAdapter<Task> {
    public TasksAdapter(@NonNull Context context, ArrayList<Task> tasks) {
        super(context, 0, tasks);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Task task = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_view_layout, parent, false);
        }

        TextView view_id = convertView.findViewById(R.id.view_id);
        TextView view_title = convertView.findViewById(R.id.view_title);
        TextView view_date = convertView.findViewById(R.id.view_date);
        TextView view_activity = convertView.findViewById(R.id.view_activity);

        Button btn_update = convertView.findViewById(R.id.btn_update);
        Button btn_delete = convertView.findViewById(R.id.btn_delete);

        assert task != null;
        view_id.setText(String.valueOf(task.getId()));
        view_title.setText(task.getTitle());
        view_date.setText(task.getDate());
        view_activity.setText(task.getActivity());

        btn_update.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                LayoutInflater li = (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View updateLayout = li.inflate(R.layout.task_input_form, null);

                builder.setView(updateLayout);
                builder.setTitle("Update Task");
                final EditText txt_title = updateLayout.findViewById(R.id.txt_title);
                final EditText txt_activity = updateLayout.findViewById(R.id.txt_activity);
                final DatePicker txt_date = updateLayout.findViewById(R.id.txt_date);

                txt_title.setText(task.getTitle());
                txt_activity.setText(task.getActivity());


                builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String date = txt_date.getYear() + "-" + (txt_date.getMonth() + 1) + "-" + txt_date.getMonth() + "T02:00:00.000Z";
                            RequestQueue queue = Volley.newRequestQueue(v.getContext());

                            String url = "http://192.168.1.2:3000/tasks/update";
                            JSONObject jsonBody = new JSONObject();
                            jsonBody.put("id", task.getId());
                            jsonBody.put("title", txt_title.getText().toString());
                            jsonBody.put("activity",  txt_activity.getText().toString());
                            jsonBody.put("date",  date);
                            final String requestBody = jsonBody.toString();

                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Toast.makeText(v.getContext(), "Task has been updated", Toast.LENGTH_SHORT).show();
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
                            task.setTitle(txt_title.getText().toString());
                            task.setActivity(txt_activity.getText().toString());
                            task.setDate(date);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        notifyDataSetChanged();
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
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    RequestQueue queue = Volley.newRequestQueue(v.getContext());
                    String url = "http://192.168.1.2:3000/tasks/delete";
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("id", task.getId());
                    final String requestBody = jsonBody.toString();

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(v.getContext(), "Task has been deleted", Toast.LENGTH_SHORT).show();
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

                MainActivity.tasks.remove(position);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }
}
