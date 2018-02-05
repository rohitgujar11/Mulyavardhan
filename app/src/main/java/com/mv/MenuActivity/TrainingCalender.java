package com.mv.MenuActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.mv.Activity.CalenderFliterActivity;
import com.mv.Activity.ProcessListActivity;
import com.mv.Adapter.IndicatorListAdapter;
import com.mv.Adapter.PichartDescriptiveListAdapter;
import com.mv.Model.CalenderEvent;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.FragmentTrainigCalenderBinding;
import com.mv.decorators.EventDecorator;
import com.mv.decorators.HighlightWeekendsDecorator;
import com.mv.decorators.OneDayDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by nanostuffs on 05-12-2017.
 */

public class TrainingCalender extends AppCompatActivity implements OnDateSelectedListener,View.OnClickListener {
    private PreferenceHelper preferenceHelper;
    List<CalenderEvent> dateList = new ArrayList<>();
    private IndicatorListAdapter mAdapter;
    private FragmentTrainigCalenderBinding binding;
    SimpleDateFormat formatter;
    ArrayList<CalendarDay> dates;
    HashMap<CalendarDay, List<CalenderEvent>> eventMap = new HashMap<>();

    PichartDescriptiveListAdapter adapter;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    Activity context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_trainig_calender);
        binding.setClander(this);
        setActionbar(getString(R.string.training_calendar));
        binding.fabAddBroadcast.setOnClickListener(this);
        preferenceHelper = new PreferenceHelper(context);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        //here data must be an instance of the class MarsDataProvider
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        binding.setClander(this);
  /*      RelativeLayout mToolBar = (RelativeLayout) view.findViewById(R.id.toolbar);
        mToolBar.setVisibility(View.GONE);*/
        binding.calendarView.setOnDateChangedListener(this);
        binding.calendarView.setShowOtherDates(MaterialCalendarView.SHOW_ALL);

        Calendar instance = Calendar.getInstance();
        binding.calendarView.setSelectedDate(instance.getTime());

        Calendar instance1 = Calendar.getInstance();
        instance1.set(instance1.get(Calendar.YEAR), Calendar.JANUARY, 1);



        binding.calendarView.state().edit()
                .setMinimumDate(instance1.getTime())
                .commit();

        binding.calendarView.addDecorators(
                //new MySelectorDecorator(context),
                new HighlightWeekendsDecorator(),
                oneDayDecorator
        );


    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }
    @Override
    public void onResume() {
        super.onResume();
        getAllProcess();
    }


    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        if( eventMap.get(date)!=null) {
            adapter = new PichartDescriptiveListAdapter(context, AppDatabase.getAppDatabase(getApplicationContext()).userDao().getCalenderList( formatter.format(date.getDate())));
            binding.recyclerView.setAdapter(adapter);
        }
        else
        {
            adapter = new PichartDescriptiveListAdapter(context, new ArrayList<CalenderEvent>());
            binding.recyclerView.setAdapter(adapter);
        }


    }
    private void setActionbar(String Title) {
        RelativeLayout mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        ImageView img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        ImageView img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                context.finish();
                context.overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.fab_add_broadcast:
                Intent openClass = new Intent(TrainingCalender.this, CalenderFliterActivity.class);
                 startActivity(openClass);
                break;
        }
    }

    private void getAllProcess() {
        Utills.showProgressDialog(context, "Loading Process", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(context).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/apexrest/getcalenderEventRecords?userId=" + User.getCurrentUser(context).getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();

                try {
                    if(response.isSuccess()) {
                        JSONArray jsonArray = new JSONArray(response.body().string());

                        eventMap = new HashMap<>();
                        dates = new ArrayList<>();
                        dateList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            CalenderEvent calenderEvent = new CalenderEvent();
                            calenderEvent.setId(jsonArray.getJSONObject(i).getString("Id"));
                            calenderEvent.setDate(jsonArray.getJSONObject(i).getString("Date__c"));
                            calenderEvent.setDescription(jsonArray.getJSONObject(i).getString("Description_New__c"));
                            calenderEvent.setTitle(jsonArray.getJSONObject(i).getString("Title__c"));
                          //  calenderEvent.setMV_User1__c(jsonArray.getJSONObject(i).getString("MV_User1__c"));
                            CalendarDay day = CalendarDay.from(formatter.parse(jsonArray.getJSONObject(i).getString("Date__c")));

                            if (eventMap.get(jsonArray.getJSONObject(i).getString("Date__c")) != null)
                                dateList = eventMap.get(jsonArray.getJSONObject(i).getString("Date__c"));
                            dateList.add(calenderEvent);
                            eventMap.put(day, dateList);
                            dates.add(day);

                        }


                        AppDatabase.getAppDatabase(context).userDao().deleteCalender();
                        AppDatabase.getAppDatabase(context).userDao().insertCalendr(dateList);
                        binding.calendarView.addDecorator(new EventDecorator(Color.RED, dates));
                        Calendar instance = Calendar.getInstance();
                        if (eventMap.get(CalendarDay.from(instance)) != null) {
                            adapter = new PichartDescriptiveListAdapter(context,  AppDatabase.getAppDatabase(getApplicationContext()).userDao().getCalenderList( formatter.format(instance.getTime())));
                            binding.recyclerView.setAdapter(adapter);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }
}