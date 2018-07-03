package com.mv.Activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.CalenderEvent;
import com.mv.Model.EventUser;
import com.mv.Model.Template;
import com.mv.Model.User;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Service.LocationService;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityClalenderFliterBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalenderFliterActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private ActivityClalenderFliterBinding binding;
    private int mSelectOrganization = 0, mSelectRole = 0, mSelectState = 1, mSelectDistrict = 1, mSelectTaluka = 0, mSelectCluster = 0, mSelectVillage = 0, mSelectSchoolName = 0;
    private List<String> mListOrganization, mListRoleName, mListDistrict, mListTaluka, mListCluster, mListVillage, mListSchoolName, mStateList;
    ArrayList<EventUser> selectedUser = new ArrayList<>();
    private ArrayAdapter<String> district_adapter, taluka_adapter, cluster_adapter, village_adapter, school_adapter, state_adapter, organization_adapter, role_adapter, catagory_adapter;
    private PreferenceHelper preferenceHelper;
    private RelativeLayout mToolBar;
    private Spinner selectedSpinner;
    String msg = "";
    private int locationState;
    public String selectedState = "", selectedDisrict = "", selectedRolename = "", selectedTaluka = "", selectedCluster = "", selectedVillage = "", selectedSchool = "", selectedOrganization = "", selectedUserId = "", selectedUserName = "", selectedCatagory = "";
    CalenderEvent calenderEvent = new CalenderEvent();
    ArrayList<EventUser> calenderEventUserArrayList;

    ArrayList<Template> processList;
    private String processId = "";

    ArrayList<String> selectedProcessId = new ArrayList<>();
    ArrayList<String> selectedRole = new ArrayList<>();
    Activity context;
    String url = "";
    String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        // overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_clalender_fliter);
        binding.setActivity(this);


     /*   List<Date> dates = getDates("2012-02-01", "2012-03-01");
        for (Date date : dates)
            System.out.println(date);*/


        if (getIntent().getParcelableExtra(Constants.My_Calendar) != null) {
            calenderEvent = getIntent().getParcelableExtra(Constants.My_Calendar);
            id = calenderEvent.getId();
            url = Constants.UpdatetEventcalender_Url;

            selectedState = calenderEvent.getState__c();
            selectedDisrict = calenderEvent.getDistrict__c();
            selectedTaluka = calenderEvent.getTaluka__c();
            selectedCluster = calenderEvent.getCluster__c();
            selectedVillage = calenderEvent.getVillage__c();
            selectedSchool = calenderEvent.getSchool__c();
            if (calenderEvent.getOrganization__c() != null)
                selectedOrganization = calenderEvent.getOrganization__c();
            else
                selectedOrganization = User.getCurrentUser(getApplicationContext()).getMvUser().getOrganisation();
            if (calenderEvent.getStatus() != null && calenderEvent.getStatus().length() > 0) {
                binding.spinnerStatus.setSelection(Arrays.asList(getResources().getStringArray(R.array.array_of_status)).indexOf(calenderEvent.getStatus()));
            }
            selectedRole = new ArrayList<String>(Arrays.asList(getColumnIdex((calenderEvent.getRole__c()).split(","))));
            selectedRolename = calenderEvent.getRole__c();
            binding.spinnerRole.setText(calenderEvent.getRole__c());
            binding.etEventTime.setText(calenderEvent.getEvent_Time__c());
            binding.etEventTitle.setText(calenderEvent.getTitle());
            binding.etEventDate.setText(calenderEvent.getDate());

            binding.etEventEndDate.setText(calenderEvent.getEnd_Date__c());
            binding.etEventEndTime.setText(calenderEvent.getEvent_End_Time__c());

            binding.etEventDiscription.setText(calenderEvent.getDescription());
            if (calenderEvent.getMV_Process__c() != null)
                selectedProcessId = new ArrayList<String>(Arrays.asList(getColumnIdex(("Other," + calenderEvent.getMV_Process__c()).split(","))));
            else
                selectedProcessId = new ArrayList<String>(Arrays.asList(getColumnIdex(("Other,").split(","))));

        } else {
            id = null;
            url = Constants.InsertEventcalender_Url;
            selectedState = User.getCurrentUser(getApplicationContext()).getMvUser().getState();
            selectedDisrict = User.getCurrentUser(getApplicationContext()).getMvUser().getDistrict();
            selectedTaluka = User.getCurrentUser(getApplicationContext()).getMvUser().getTaluka();
            selectedCluster = User.getCurrentUser(getApplicationContext()).getMvUser().getCluster();
            selectedVillage = User.getCurrentUser(getApplicationContext()).getMvUser().getVillage();
            selectedSchool = User.getCurrentUser(getApplicationContext()).getMvUser().getSchool_Name();
            selectedOrganization = User.getCurrentUser(getApplicationContext()).getMvUser().getOrganisation();
            //  selectedRolename = User.getCurrentUser(getApplicationContext()).getMvUser().getRoll();
            selectedRole = new ArrayList<String>(Arrays.asList(getColumnIdex("Select".split(","))));
            selectedProcessId = new ArrayList<String>(Arrays.asList(getColumnIdex(("Other").split(","))));
            binding.spinnerRole.setText("Select");
            binding.spinnerCatogory.setText("Other");
        }

        binding.txtOrg.setVisibility(View.GONE);
        binding.txtRole.setVisibility(View.GONE);
        binding.layoutOrg.setVisibility(View.GONE);
        binding.layoutRole.setVisibility(View.GONE);
        binding.rlMoreLocation.setVisibility(View.GONE);

        initViews();
        processList = new ArrayList<>();
        Template process = new Template();
        process.setName("Other");
        process.setMV_Process__c("Other");
        processList.add(process);
        if (Utills.isConnected(this))
            getCalendeEventsProcess();
        else
            Utills.showToast(getString(R.string.error_no_internet), CalenderFliterActivity.this);
    }

    public static String[] getColumnIdex(String[] value) {

        for (int i = 0; i < value.length; i++) {
            value[i] = value[i].trim();
        }
        return value;

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void initViews() {
        setActionbar("Select Location");
        Utills.setupUI(findViewById(R.id.layout_main), this);
        preferenceHelper = new PreferenceHelper(this);
        binding.rlMoreLocation.setOnClickListener(this);
        binding.spinnerState.setOnItemSelectedListener(this);
        binding.spinnerDistrict.setOnItemSelectedListener(this);
        binding.spinnerTaluka.setOnItemSelectedListener(this);
        binding.spinnerCluster.setOnItemSelectedListener(this);
        binding.spinnerVillage.setOnItemSelectedListener(this);
        binding.spinnerSchoolName.setOnItemSelectedListener(this);
        binding.spinnerCatogory.setOnClickListener(this);
        binding.etEventEndTime.setOnClickListener(this);
        binding.etEventEndTime.setFocusable(false);
        binding.etEventEndTime.setClickable(true);

        binding.etEventTime.setOnClickListener(this);
        binding.etEventTime.setFocusable(false);
        binding.etEventTime.setClickable(true);

        binding.btnSubmit.setOnClickListener(this);
        binding.spinnerOrganization.setOnItemSelectedListener(this);
        binding.tvEventAddUser.setOnClickListener(this);
        binding.etEventDate.setOnClickListener(this);
        binding.etEventDate.setFocusable(false);
        binding.etEventDate.setClickable(true);

        binding.etEventEndDate.setOnClickListener(this);
        binding.etEventEndDate.setFocusable(false);
        binding.etEventEndDate.setClickable(true);

        binding.spinnerRole.setOnClickListener(this);
        //
        mStateList = new ArrayList<String>();
        mStateList.add("Select");
        //mStateList.add(User.getCurrentUser(getApplicationContext()).getState());
        mListDistrict = new ArrayList<String>();
        mListDistrict.add("Select");

        mListTaluka = new ArrayList<String>();
        mListTaluka.add("Select");
        mListCluster = new ArrayList<String>();
        mListCluster.add("Select");
        mListVillage = new ArrayList<String>();
        mListVillage.add("Select");

        mListSchoolName = new ArrayList<String>();
        mListSchoolName.add("Select");

        mListOrganization = new ArrayList<String>();
        mListOrganization.add("Select");

        mListRoleName = new ArrayList<String>();
        mListRoleName.add("Select");
        mStateList.clear();
        mStateList = AppDatabase.getAppDatabase(context).userDao().getState();
        mStateList.removeAll(Collections.singleton(null));
        if (mStateList.size() == 0) {
            if (Utills.isConnected(this))
                getState();
        } else {
            mStateList = AppDatabase.getAppDatabase(context).userDao().getState();
            mStateList.add(0, "Select");
            setSpinnerAdapter(mStateList, state_adapter, binding.spinnerState, selectedState);
            //  mStateList.add(User.getCurrentUser(getApplicationContext()).getState());
        }
        if (Utills.isConnected(this))
            getOrganization();
        mStateList = AppDatabase.getAppDatabase(context).userDao().getState();
        mStateList.add(0, "Select");
        setSpinnerAdapter(mStateList, state_adapter, binding.spinnerState, selectedState);
        //  mStateList.add(User.getCurrentUser(getApplicationContext()).getState());
        ArrayList<String> catagory = new ArrayList<>();
        catagory.add("Select");
        catagory.add("Training Observation");
        catagory.add("Classroom Observation");
        catagory.add("School Visit");
        catagory.add("School and Classroom Observation");
        catagory.add("Training");
        if (User.getCurrentUser(getApplicationContext()).getMvUser().getRole_Juridiction__c().equals(Constants.State)) {

        } else if (User.getCurrentUser(getApplicationContext()).getMvUser().getRole_Juridiction__c().equals(Constants.DISTRICT)) {
            binding.spinnerState.setEnabled(false);

        } else if (User.getCurrentUser(getApplicationContext()).getMvUser().getRole_Juridiction__c().equals(Constants.TALUKA)) {
            binding.spinnerState.setEnabled(false);
            binding.spinnerDistrict.setEnabled(false);

        } else if (User.getCurrentUser(getApplicationContext()).getMvUser().getRole_Juridiction__c().equals(Constants.CLUSTER)) {
            binding.spinnerState.setEnabled(false);
            binding.spinnerDistrict.setEnabled(false);
            binding.spinnerTaluka.setEnabled(false);

        } else if (User.getCurrentUser(getApplicationContext()).getMvUser().getRole_Juridiction__c().equals(Constants.VILlAGE)) {
            binding.spinnerState.setEnabled(false);
            binding.spinnerDistrict.setEnabled(false);
            binding.spinnerTaluka.setEnabled(false);
            binding.spinnerCluster.setEnabled(false);

        } else if (User.getCurrentUser(getApplicationContext()).getMvUser().getRole_Juridiction__c().equals(Constants.SCHOOL)) {
            binding.spinnerState.setEnabled(false);
            binding.spinnerDistrict.setEnabled(false);
            binding.spinnerTaluka.setEnabled(false);
            binding.spinnerCluster.setEnabled(false);
            binding.spinnerVillage.setEnabled(false);
        }
        // setSpinnerAdapter(catagory, catagory_adapter, binding.spinnerCatogory, "");
        setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);
        setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
        setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
        setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
        setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);
        setSpinnerAdapter(mListOrganization, organization_adapter, binding.spinnerOrganization, selectedOrganization);


    }


    private void setActionbar(String Title) {
        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(Title);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
        img_logout.setOnClickListener(this);
    }

    private void getState() {

        Utills.showProgressDialog(this, "Loading States", getString(R.string.progress_please_wait));
        Utills.showProgressDialog(this);
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getState().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mStateList.clear();
                            mStateList.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mStateList.add(jsonArray.getString(i));
                            }
                            setSpinnerAdapter(mStateList, state_adapter, binding.spinnerState, selectedState);

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }

    @Override
    public void onClick(View view) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        switch (view.getId()) {
            case R.id.img_back:

                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.btn_submit:
                String msg = "";
                if (binding.spinnerCatogory.getText().equals("") || binding.spinnerCatogory.getText().equals("Select")) {
                    msg = "Please Select Category";
                } else if (binding.etEventTitle.getText().toString().equals("")) {
                    msg = "Please Enter Title";
                } else if (binding.etEventDate.getText().toString().equals("")) {
                    msg = "Please Select Date";
                } else if (binding.etEventDiscription.getText().toString().equals("")) {
                    msg = "Please Enter Description";
                }
                if (msg.isEmpty()) {
                    submitEventDetail();
                } else {
                    Utills.showToast(msg, context);
                }

                //  sendLocation();

                // sendData();
                break;
            case R.id.spinner_role:

                showrRoleDialog();

                // sendData();
                break;
            case R.id.et_event_time:


                mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        binding.etEventTime.setText(updateTime(selectedHour, selectedMinute));
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

                break;
            case R.id.et_event_date:

                showDateDialog(CalenderFliterActivity.this, 1);

                // sendData();
                break;

            case R.id.et_event_end_time:


                mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        binding.etEventEndTime.setText(updateTime(selectedHour, selectedMinute));
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

                break;
            case R.id.et_event_end_date:

                showDateDialog(CalenderFliterActivity.this, 2);

                // sendData();
                break;

            case R.id.tv_event_add_user:

                getAllFilterUser();

                // sendData();
                break;
            case R.id.rl_more_location:

                if (binding.llLoacationlayout.isShown()) {
                    binding.llLoacationlayout.setVisibility(View.GONE);
                } else
                    binding.llLoacationlayout.setVisibility(View.VISIBLE);

                break;
            case R.id.spinner_catogory:
                showProcessDialog();
                break;
        }
    }

    private String updateTime(int hours, int mins) {

        String timeSet = "";
        if (hours > 12) {
            hours -= 12;
            timeSet = "PM";
        } else if (hours == 0) {
            hours += 12;
            timeSet = "AM";
        } else if (hours == 12)
            timeSet = "PM";
        else
            timeSet = "AM";


        String minutes = "";
        if (mins < 10)
            minutes = "0" + mins;
        else
            minutes = String.valueOf(mins);

        // Append in a StringBuilder
        String aTime = new StringBuilder().append(hours).append(':')
                .append(minutes).append(" ").append(timeSet).toString();

        return aTime;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinner_state:
                mSelectState = i;
                if (mSelectState != 0) {
                    selectedState = mStateList.get(mSelectState);
                }
                mListDistrict.clear();
                mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                mListDistrict.removeAll(Collections.singleton(null));
                if (mListDistrict.size() == 0) {


                    if (Utills.isConnected(this))
                        getDistrict();
                    else {
                        mListDistrict = new ArrayList<>();
                        mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                        mListDistrict.removeAll(Collections.singleton(null));
                        mListDistrict.add(0, "Select");
                        setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);

                    }

                } else {
                    mListDistrict = new ArrayList<>();
                    mListDistrict = AppDatabase.getAppDatabase(context).userDao().getDistrict(selectedState);
                    mListDistrict.removeAll(Collections.singleton(null));
                    mListDistrict.add(0, "Select");
                    setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);

                }
                setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
                setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);


                //    mListDistrict.clear();

                break;

            case R.id.spinner_district:
                mSelectDistrict = i;
                if (mSelectDistrict != 0) {
                    selectedDisrict = mListDistrict.get(mSelectDistrict);
            /*        selectedTaluka="";
                    selectedCluster="";
                    selectedVillage="";
                    selectedSchool="";*/
                    mListTaluka.clear();
                    mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                    mListTaluka.removeAll(Collections.singleton(null));
                    if (mListTaluka.size() == 0) {

                        if (Utills.isConnected(this))
                            getTaluka();
                        else {
                            mListTaluka.clear();
                            mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                            mListTaluka.add(0, "Select");
                            mListTaluka.removeAll(Collections.singleton(null));
                            setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);

                        }

                    } else {
                        mListTaluka.clear();
                        mListTaluka = AppDatabase.getAppDatabase(context).userDao().getTaluka(selectedState, mListDistrict.get(mSelectDistrict));
                        mListTaluka.add(0, "Select");
                        mListTaluka.removeAll(Collections.singleton(null));
                        setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);

                    }
                } else {
                    mListTaluka.clear();
                    mListTaluka.add("Select");
                    setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);

                }
/*
                setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
                setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);*/

                break;

            case R.id.spinner_taluka:
                mSelectTaluka = i;
                if (mSelectTaluka != 0) {
                    selectedTaluka = mListTaluka.get(mSelectTaluka);
            /*        selectedCluster="";
                    selectedVillage="";
                    selectedSchool="";*/


                    mListCluster.clear();
                    mListCluster = AppDatabase.getAppDatabase(context).userDao().getCluster(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                    mListCluster.removeAll(Collections.singleton(null));
                    if (mListCluster.size() == 0) {
                        mListCluster.add(0, "Select");
                        if (Utills.isConnected(this))
                            getCluster();
                        else {

                            mListCluster.clear();
                            mListCluster = AppDatabase.getAppDatabase(context).userDao().getCluster(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka));
                            mListCluster.add(0, "Select");
                            mListCluster.removeAll(Collections.singleton(null));
                            //  setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
                        }
                    } else {
                        mListCluster.add(0, "Select");
                        //    setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);

                    }

                } else {
                    mListCluster.clear();
                    mListCluster.add("Select");
                    //  setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);

                }
                //  setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                // setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

                break;

            case R.id.spinner_cluster:
                mSelectCluster = i;
                if (mSelectCluster != 0) {
                    selectedCluster = mListCluster.get(mSelectCluster);
/*                    selectedVillage="";
                    selectedSchool="";*/


                    mListVillage.clear();
                    mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster));
                    mListVillage.removeAll(Collections.singleton(null));
                    if (mListVillage.size() == 0) {
                        if (Utills.isConnected(this))
                            getVillage();
                        else {
                            mListVillage.clear();
                            mListVillage = AppDatabase.getAppDatabase(context).userDao().getVillage(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster));
                            mListVillage.add(0, "Select");
                            mListVillage.removeAll(Collections.singleton(null));
                            //  setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                        }
                    } else {
                        mListVillage.add(0, "Select");
                        //   setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                    }

                } else {
                    mListVillage.clear();
                    mListVillage.add("Select");
                    //  setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);

                }

                //   setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

                break;

            case R.id.spinner_village:
                mSelectVillage = i;
                if (mSelectVillage != 0) {
                    selectedVillage = mListVillage.get(mSelectVillage);
                    // selectedSchool="";

                    mListSchoolName.clear();
                    mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage));
                    mListSchoolName.removeAll(Collections.singleton(null));
                    if (mListSchoolName.size() == 0) {
                        if (Utills.isConnected(this))
                            getSchool();
                        else {
                            mListSchoolName.clear();
                            mListSchoolName = AppDatabase.getAppDatabase(context).userDao().getSchoolName(User.getCurrentUser(context).getMvUser().getState(), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage));
                            mListSchoolName.add(0, "Select");
                            mListSchoolName.removeAll(Collections.singleton(null));
                            //  setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);
                        }
                    } else {
                        mListSchoolName.add(0, "Select");
                        //   setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);

                    }

                } else {
                    mListSchoolName.clear();
                    mListSchoolName.add("Select");
                    //  setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);


                }
                break;
            case R.id.spinner_school_name:
                mSelectSchoolName = i;
                selectedSchool = mListSchoolName.get(mSelectSchoolName);
                break;
            case R.id.spinner_organization:
                mSelectOrganization = i;
                if (mSelectOrganization != 0) {
                    if (Utills.isConnected(this))
                        getRole();
                } else {
                    mListRoleName.add(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll());
                }

                break;
            case R.id.spinner_catogory:
                selectedCatagory = adapterView.getItemAtPosition(i).toString();

                break;

        }
    }

    public void setSpinnerAdapter(List<String> itemList, ArrayAdapter<String> adapter, Spinner spinner, String selectedValue) {
       /* adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (!selectedValue.isEmpty() && itemList.indexOf(selectedValue) >= 0)
            spinner.setSelection(itemList.indexOf(selectedValue));*/
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        String abc;
    }

    private void getDistrict() {

        Utills.showProgressDialog(this, getString(R.string.loding_district), getString(R.string.progress_please_wait));

        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getDistrict(mStateList.get(mSelectState)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    mListDistrict.clear();
                    mListDistrict.add("Select");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        mListDistrict.add(jsonArray.getString(i));
                    }
                    setSpinnerAdapter(mListDistrict, district_adapter, binding.spinnerDistrict, selectedDisrict);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }

    private void getRole() {
        Utills.showProgressDialog(this, getString(R.string.Loading_Roles), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + "/services/data/v36.0/query/?q=select+Id,Juridictions__c,Name+from+MV_Role__c+where+Organisation__c='" + mListOrganization.get(mSelectOrganization) + "'";
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {


                            JSONObject obj = new JSONObject(data);
                            JSONArray jsonArray = obj.getJSONArray("records");
                            mListRoleName.clear();
                            mListRoleName.add("Select");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                mListRoleName.add(jsonObject.getString("Name"));

                            }

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }

    private void getOrganization() {
        Utills.showProgressDialog(this, "Loading Organization", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetOrganizationUrl;
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            mListOrganization.clear();
                            mListOrganization.add("Select");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                mListOrganization.add(jsonArray.getString(i));
                            }
                            setSpinnerAdapter(mListOrganization, organization_adapter, binding.spinnerOrganization, selectedOrganization);

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();

            }
        });
    }


    private void getTaluka() {

        Utills.showProgressDialog(this, getString(R.string.loding_taluka), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getTaluka(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListTaluka.clear();
                    mListTaluka.add("Select");
                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListTaluka.add(jsonArr.getString(i));
                    }
                    setSpinnerAdapter(mListTaluka, taluka_adapter, binding.spinnerTaluka, selectedTaluka);
                    Intent intent = new Intent(getApplicationContext(), LocationService.class);
                    // add infos for the service which file to download and where to store
                    intent.putExtra(Constants.State, mStateList.get(mSelectState));
                    intent.putExtra(Constants.DISTRICT, mListDistrict.get(mSelectDistrict));
                    startService(intent);
                    // taluka_adapter.notifyDataSetChanged();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void getCluster() {
        Utills.showProgressDialog(this, getString(R.string.loding_cluster), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getCluster(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListCluster.clear();
                    mListCluster.add("Select");
                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListCluster.add(jsonArr.getString(i));
                    }
                    setSpinnerAdapter(mListCluster, cluster_adapter, binding.spinnerCluster, selectedCluster);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void getVillage() {
        Utills.showProgressDialog(this, getString(R.string.loding_village), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);
        apiService.getVillage(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListVillage.clear();
                    mListVillage.add("Select");
                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListVillage.add(jsonArr.getString(i));
                    }
                    setSpinnerAdapter(mListVillage, village_adapter, binding.spinnerVillage, selectedVillage);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    private void getSchool() {
        Utills.showProgressDialog(this, getString(R.string.loding_school), getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClient().create(ServiceRequest.class);

        apiService.getSchool(mStateList.get(mSelectState), mListDistrict.get(mSelectDistrict), mListTaluka.get(mSelectTaluka), mListCluster.get(mSelectCluster), mListVillage.get(mSelectVillage)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();
                try {
                    mListSchoolName.clear();
                    mListSchoolName.add("Select");

                    JSONArray jsonArr = new JSONArray(response.body().string());
                    for (int i = 0; i < jsonArr.length(); i++) {
                        mListSchoolName.add(jsonArr.getString(i));
                    }
                    setSpinnerAdapter(mListSchoolName, school_adapter, binding.spinnerSchoolName, selectedSchool);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }


    private void getAllFilterUser() {
        Intent intent = new Intent(CalenderFliterActivity.this, EventUserListActivity.class);
        startActivityForResult(intent, 1);
    }

    private void getCalendeEventsProcess() {
        Utills.showProgressDialog(context, "Loading ", getString(R.string.progress_please_wait));
        ServiceRequest apiService =
                ApiClient.getClientWitHeader(this).create(ServiceRequest.class);

        String url = preferenceHelper.getString(PreferenceHelper.InstanceUrl)
                + Constants.GetCalenderEventsProcess + "?userId=" + User.getCurrentUser(CalenderFliterActivity.this).getMvUser().getId();
        apiService.getSalesForceData(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utills.hideProgressDialog();

                try {
                    if (response.body() != null) {
                        String data = response.body().string();
                        if (data != null && data.length() > 0) {
                            JSONArray jsonArray = new JSONArray(data);
                            StringBuffer sb = new StringBuffer();
                            String prefix = "";
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Template process = new Template();
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                process.setName(jsonObject.getString("Name"));
                                process.setMV_Process__c(jsonObject.getString("Id"));
                                if (selectedProcessId.contains(jsonObject.getString("Id"))) {
                                    sb.append(prefix);

                                    prefix = ",";
                                    sb.append(jsonObject.getString("Name"));

                                    //now original string is changed
                                }
                                processList.add(process);

                            }
                            if (selectedProcessId.contains("Other")) {
                                sb.append(prefix);

                                prefix = ",";
                                sb.append("Other");

                                //now original string is changed
                            }
                            binding.spinnerCatogory.setText(sb.toString());
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utills.hideProgressDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // tvResult.setText(data.getIntExtra("result",-1)+"");
            selectedUser = data.getParcelableArrayListExtra(Constants.PROCESS_ID);
            calenderEventUserArrayList = data.getParcelableArrayListExtra(Constants.ALLUSER);
            selectedRolename = data.getStringExtra("Role");
            StringBuffer sb = new StringBuffer();
            StringBuffer sbName = new StringBuffer();
            String prefix = "";
            for (int i = 0; i < selectedUser.size(); i++) {
                sb.append(prefix);
                sbName.append(prefix);
                prefix = ",";
                sb.append(selectedUser.get(i).getUserID());
                sbName.append(selectedUser.get(i).getUserName());
                //now original string is changed
            }
            selectedUserId = sb.toString();
            selectedUserName = sbName.toString();
            if (calenderEventUserArrayList.size() == selectedUser.size()) {
                binding.tvEventAddUser.setText("All Selected");
            } else {
                binding.tvEventAddUser.setText(selectedUserName);
            }
            Log.e("StringId", selectedUserId);


        }

    }

    private void showrRoleDialog() {
        final List<String> temp = mListRoleName;
        final String[] items = new String[temp.size()];
        final boolean[] mSelection = new boolean[items.length];
        for (int i = 0; i < temp.size(); i++) {
            items[i] = temp.get(i);
            if (selectedRole.contains(temp.get(i)))
                mSelection[i] = true;
            else
                mSelection[i] = false;
        }

       /* if (mListRoleName.indexOf(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll()) > 0)
            mSelection[mListRoleName.indexOf(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll())] = true;
*/
// arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(CalenderFliterActivity.this)
                .setTitle("Select ")
                .setMultiChoiceItems(items, mSelection, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if (mSelection != null && which < mSelection.length) {
                            mSelection[which] = isChecked;


                        } else {
                            throw new IllegalArgumentException(
                                    "Argument 'which' is out of bounds.");
                        }
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        StringBuffer sb = new StringBuffer();
                        String prefix = "";
                        for (int i = 0; i < items.length; i++) {
                            if (mSelection[i]) {
                                sb.append(prefix);
                                prefix = ",";
                                sb.append(temp.get(i));
                                //now original string is changed
                            }
                        }
                        selectedRolename = sb.toString();
                        binding.spinnerRole.setText(selectedRolename);
                        selectedRole = new ArrayList<String>(Arrays.asList(getColumnIdex((selectedRolename).split(","))));

                        Log.e("StringValue", selectedRolename);


                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialog.show();
    }

    private void showProcessDialog() {


        //  final List<Community> temp = AppDatabase.getAppDatabase(getApplicationContext()).userDao().getAllCommunities();
        final String[] items = new String[processList.size()];
        final boolean[] mSelection = new boolean[items.length];

        for (int i = 0; i < processList.size(); i++) {
            items[i] = processList.get(i).getName();
            if (selectedProcessId.contains(processList.get(i).getMV_Process__c()))
                mSelection[i] = true;
            else
                mSelection[i] = false;
        }


      /* if(temp.contains(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll()))
        mSelection[temp.indexOf(User.getCurrentUser(getApplicationContext()).getMvUser().getRoll())] = true;
*/
// arraylist to keep the selected items

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(CalenderFliterActivity.this)
                .setTitle("Select Category")
                .setMultiChoiceItems(items, mSelection, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (mSelection != null && which < mSelection.length) {
                            mSelection[which] = isChecked;


                        } else {
                            throw new IllegalArgumentException(
                                    "Argument 'which' is out of bounds.");
                        }
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        StringBuffer sb = new StringBuffer();
                        StringBuffer roleId = new StringBuffer();
                        String prefix = "";
                        for (int i = 0; i < items.length; i++) {
                            if (mSelection[i]) {
                                sb.append(prefix);
                                roleId.append(prefix);
                                prefix = ",";
                                sb.append(processList.get(i).getName());
                                roleId.append(processList.get(i).getMV_Process__c());
                                //now original string is changed
                            }
                        }
                        processId = roleId.toString();
                        selectedProcessId = new ArrayList<String>(Arrays.asList(getColumnIdex((processId).split(","))));
                        binding.spinnerCatogory.setText(sb.toString());
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();


        dialog.show();
    }

    private void submitEventDetail() {
        if (Utills.isConnected(this)) {
            try {
                Utills.showProgressDialog(context, "Loading ", getString(R.string.progress_please_wait));

                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("Id", id);
                //  jsonObject1.put("Assigned_User_Ids__c", selectedUserId);

                jsonObject1.put("Cluster__c", selectedCluster);
                jsonObject1.put("Date__c", binding.etEventDate.getText().toString());

                jsonObject1.put("Description_New__c", binding.etEventDiscription.getText().toString());
                jsonObject1.put("District__c", selectedDisrict);
                if (selectedUserId.equals(""))
                    jsonObject1.put("Is_Event_for_All_Role__c", true);
                else
                    jsonObject1.put("Is_Event_for_All_Role__c", false);
                if (selectedUserId.length() > 0)
                    selectedUserId = selectedUserId + "," + User.getCurrentUser(CalenderFliterActivity.this).getMvUser().getId();
                else
                    selectedUserId = User.getCurrentUser(CalenderFliterActivity.this).getMvUser().getId();

                jsonObject1.put("Assigned_User_Ids__c", selectedUserId);
                jsonObject1.put("Role__c", selectedRolename);
                jsonObject1.put("Status__c", binding.spinnerStatus.getSelectedItem().toString());
                jsonObject1.put("School__c", selectedSchool);
                if (processId.length() > 0)
                    jsonObject1.put("MV_Process__c", processId);
                else
                    jsonObject1.put("MV_Process__c", "Other");
                jsonObject1.put("State__c", selectedState);
                jsonObject1.put("Taluka__c", selectedTaluka);
                jsonObject1.put("Village__c", selectedVillage);
                jsonObject1.put("MV_User__c", User.getCurrentUser(context).getMvUser().getId());
                jsonObject1.put("category", selectedCatagory);
                jsonObject1.put("Title__c", binding.etEventTitle.getText().toString());
                jsonObject1.put("Event_Time__c", binding.etEventTime.getText().toString());

                jsonObject1.put("End_Date__c", binding.etEventEndDate.getText().toString());
                jsonObject1.put("Event_End_Time__c", binding.etEventEndTime.getText().toString());

                jsonArray.put(jsonObject1);
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + Constants.InsertEventcalender_Url, gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            finish();
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), CalenderFliterActivity.this);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), CalenderFliterActivity.this);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), CalenderFliterActivity.this);

            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), CalenderFliterActivity.this);
        }
    }

    public void showDateDialog(Context context, int type) {


        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        if (type == 1) {
                            binding.etEventDate.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth));
                            binding.etEventEndDate.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth));
                        } else if (type == 2) {
                            binding.etEventEndDate.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth));
                        }
                    }
                }, mYear, mMonth, mDay);
        dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 10000);
        dpd.show();
    }

    public static String getTwoDigit(int i) {
        if (i < 10)
            return "0" + i;
        return "" + i;
    }

    private static List<Date> getDates(String dateString1, String dateString2) {
        ArrayList<Date> dates = new ArrayList<Date>();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");

        Date date1 = null;
        Date date2 = null;

        try {
            date1 = df1.parse(dateString1);
            date2 = df1.parse(dateString2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);


        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        while (!cal1.after(cal2)) {
            dates.add(cal1.getTime());
            cal1.add(Calendar.DATE, 1);
        }
        return dates;
    }
}
