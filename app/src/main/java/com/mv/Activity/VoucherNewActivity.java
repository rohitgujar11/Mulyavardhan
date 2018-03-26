package com.mv.Activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mv.Model.User;
import com.mv.Model.Voucher;
import com.mv.R;
import com.mv.Retrofit.ApiClient;
import com.mv.Retrofit.AppDatabase;
import com.mv.Retrofit.ServiceRequest;
import com.mv.Utils.Constants;
import com.mv.Utils.LocaleManager;
import com.mv.Utils.PreferenceHelper;
import com.mv.Utils.Utills;
import com.mv.databinding.ActivityVoucherNewBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rohit Gujar on 08-03-2018.
 */

public class VoucherNewActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView img_back, img_list, img_logout;
    private TextView toolbar_title;
    private RelativeLayout mToolBar;
    private ActivityVoucherNewBinding binding;
    private int mProjectSelect = 0;
    private List<String> projectList = new ArrayList<>();
    private Voucher mVoucher;
    private boolean isAdd;

    private PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_voucher_new);
        binding.setActivity(this);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        initViews();
    }

    private void initViews() {
        projectList = Arrays.asList(getResources().getStringArray(R.array.array_of_project));
        setActionbar(getString(R.string.voucher_new));
        binding.txtDate.setOnClickListener(this);
        binding.spinnerProject.setOnItemSelectedListener(this);
        preferenceHelper = new PreferenceHelper(this);
        if (getIntent().getExtras().getString(Constants.ACTION).equalsIgnoreCase(Constants.ACTION_ADD)) {
            isAdd = true;
        } else {
            isAdd = false;
            mVoucher = (Voucher) getIntent().getSerializableExtra(Constants.VOUCHER);
            binding.txtDate.setText(mVoucher.getDate());
            binding.editTextCount.setText(mVoucher.getNoOfPeople());
            binding.editTextDescription.setText(mVoucher.getDecription());
            mProjectSelect = projectList.indexOf(mVoucher.getProject());
            binding.spinnerProject.setSelection(mProjectSelect);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                break;
            case R.id.txtDate:
                showDateDialog();
                break;
        }
    }

    private void showDateDialog() {
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        binding.txtDate.setText(year + "-" + getTwoDigit(monthOfYear + 1) + "-" + getTwoDigit(dayOfMonth));
                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }

    private static String getTwoDigit(int i) {
        if (i < 10)
            return "0" + i;
        return "" + i;
    }

    public void onSubmitClick() {
        if (isValid()) {
            Voucher voucher = new Voucher();
            if (!isAdd) {
                voucher.setUniqueId(mVoucher.getUniqueId());
                voucher.setId(mVoucher.getId());
            }
            voucher.setProject(projectList.get(mProjectSelect));
            voucher.setDate(binding.txtDate.getText().toString().trim());
            voucher.setDecription(binding.editTextDescription.getText().toString().trim());
            voucher.setNoOfPeople(binding.editTextCount.getText().toString().trim());
            voucher.setUser(User.getCurrentUser(this).getMvUser().getId());
            addVoucher(voucher);


        }
    }

    private void addVoucher(final Voucher voucher) {
        if (Utills.isConnected(this)) {
            try {

                Utills.showProgressDialog(this);
                JSONObject jsonObject = new JSONObject();
                JSONArray jsonArray = new JSONArray();

                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                String json = gson.toJson(voucher);
                JSONObject jsonObject1 = new JSONObject(json);
                jsonArray.put(jsonObject1);
                jsonObject.put("listtaskanswerlist", jsonArray);

                ServiceRequest apiService =
                        ApiClient.getClientWitHeader(this).create(ServiceRequest.class);
                JsonParser jsonParser = new JsonParser();
                JsonObject gsonObject = (JsonObject) jsonParser.parse(jsonObject.toString());
                apiService.sendDataToSalesforce(preferenceHelper.getString(PreferenceHelper.InstanceUrl) + "/services/apexrest/InsertVoucher", gsonObject).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utills.hideProgressDialog();
                        try {
                            if (response.body() != null) {
                                String data = response.body().string();
                                if (data != null && data.length() > 0) {
                                    JSONObject object = new JSONObject(data);
                                    JSONArray array = object.getJSONArray("Records");
                                    if (array.length() != 0) {
                                        voucher.setId(array.getJSONObject(0).getString("Id"));
                                    }
                                    AppDatabase.getAppDatabase(VoucherNewActivity.this).userDao().insertVoucher(voucher);
                                    Utills.showToast("Voucher Added successfully", VoucherNewActivity.this);
                                    finish();
                                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                                }
                            }
                        } catch (Exception e) {
                            Utills.hideProgressDialog();
                            Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utills.hideProgressDialog();
                        Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Utills.hideProgressDialog();
                Utills.showToast(getString(R.string.error_something_went_wrong), getApplicationContext());

            }
        } else {
            Utills.showToast(getString(R.string.error_no_internet), getApplicationContext());
        }

    }

    private boolean isValid() {
        String str = "";
        if (mProjectSelect == 0) {
            str = "Please select Project";
        } else if (binding.txtDate.getText().toString().trim().length() == 0) {
            str = "Please select Date";
        } else if (binding.editTextDescription.getText().toString().trim().length() == 0) {
            str = "Please enter Description Of Tour";
        } else if (binding.editTextCount.getText().toString().trim().length() == 0) {
            str = "Please enter No Of People Travelled";
        }
        if (str.length() != 0) {
            Utills.showToast(str, this);
            return false;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    private void setActionbar(String Title) {

        mToolBar = (RelativeLayout) findViewById(R.id.toolbar);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        String str = Title;
        if (Title != null && Title.contains("\n"))
            str = Title.replace("\n", " ");
        toolbar_title.setText(str);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);
        img_list = (ImageView) findViewById(R.id.img_list);
        img_list.setVisibility(View.GONE);
        img_list.setOnClickListener(this);
        img_logout = (ImageView) findViewById(R.id.img_logout);
        img_logout.setVisibility(View.GONE);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spinnerProject:
                mProjectSelect = i;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}