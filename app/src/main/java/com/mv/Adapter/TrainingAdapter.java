

package com.mv.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mv.Activity.ActivityWebView;
import com.mv.ActivityMenu.TrainingFragment;
import com.mv.Model.DownloadContent;
import com.mv.R;
import com.mv.Utils.Constants;
import com.mv.Utils.Utills;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by acer on 9/8/2016.
 */


public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.ViewHolder> {

    private Context mContext;
    private Resources resources;
    private ArrayList<DownloadContent> mDataList;
    private TrainingFragment trainingFragment;

    public TrainingAdapter(Context context, TrainingFragment fragment, ArrayList<DownloadContent> list) {
        mContext = context;
        resources = context.getResources();
        mDataList = list;
        trainingFragment = fragment;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_trainging, parent, false);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDownload,imgshare;
        TextView txtCount, txtName;
        RelativeLayout layoutMain;

        public ViewHolder(View itemLayoutView) {

            super(itemLayoutView);
            layoutMain = (RelativeLayout) itemLayoutView.findViewById(R.id.layoutMain);
            txtCount = (TextView) itemLayoutView.findViewById(R.id.txtCount);
            layoutMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///" + Environment.getExternalStorageDirectory().getPath() + "/MV_e-learning_Mar/Modules/1/story_html5.html"));
                    mContext.startActivity(browserIntent);*/
                    if (isFileAvalible(getAdapterPosition())) {
                        if (mDataList.get(getAdapterPosition()).getFileType().equalsIgnoreCase("zip")) {
                            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/UnZip/" + mDataList.get(getAdapterPosition()).getName();
                            if (new File(filePath + "/story_html5.html").exists()) {
                                Intent intent = new Intent(mContext, ActivityWebView.class);
                                intent.putExtra(Constants.URL, "file:///" + filePath + "/story_html5.html");
                                intent.putExtra(Constants.TITLE, mDataList.get(getAdapterPosition()).getName());
                                mContext.startActivity(intent);
                            } else {
                                deleteRecursive(new File(filePath));
                                notifyDataSetChanged();
                                Utills.showToast("File is corrupted. Please download again.", mContext);
                            }
                        } else if (mDataList.get(getAdapterPosition()).getFileType().equalsIgnoreCase("audio")) {
                            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getName() + ".mp3";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            File file = new File("/sdcard/test.mp3");
                            intent.setDataAndType(Uri.fromFile(new File(filePath)), "audio/*");
                            PackageManager packageManager = mContext.getPackageManager();
                            if (intent.resolveActivity(packageManager) != null) {
                                mContext.startActivity(intent);
                            } else {
                                Utills.showToast("No Application available to open Audio file", mContext);
                            }
                        } else if (mDataList.get(getAdapterPosition()).getFileType().equalsIgnoreCase("video")) {
                            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getName() + ".mp4";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(new File(filePath)), "video/*");
                            PackageManager packageManager = mContext.getPackageManager();
                            if (intent.resolveActivity(packageManager) != null) {
                                mContext.startActivity(intent);
                            } else {
                                Utills.showToast("No Application available to open Video file", mContext);
                            }
                        } else if (mDataList.get(getAdapterPosition()).getFileType().equalsIgnoreCase("pdf")) {
                            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getName() + ".pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            PackageManager packageManager = mContext.getPackageManager();
                            if (intent.resolveActivity(packageManager) != null) {
                                mContext.startActivity(intent);
                            } else {
                                Utills.showToast("No Application available to open PDF file", mContext);
                            }
                        } else if (mDataList.get(getAdapterPosition()).getFileType().equalsIgnoreCase("ppt")) {
                          /*  String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getName() + ".pdf";
                            Intent intent = new Intent();
                            intent.setAction(android.content.Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            PackageManager packageManager = mContext.getPackageManager();
                            if (intent.resolveActivity(packageManager) != null) {
                                mContext.startActivity(intent);
                            } else {
                                Utills.showToast("No Application available to open PDF file", mContext);
                            }*/
                            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getName() + ".ppt";
                            Uri uri = Uri.fromFile(new File(filePath));
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                            PackageManager pm = mContext.getPackageManager();
                            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
                            if (list.size() > 0)
                                mContext.startActivity(intent);
                            else
                                Utills.showToast("No Application available to open PPT file", mContext);
                        }
                    } else {
                        showNoFilePresentPopUp();
                    }


                }
            });
            imgDownload = (ImageView) itemLayoutView.findViewById(R.id.imgDownload);
            imgshare = (ImageView) itemLayoutView.findViewById(R.id.imgshare);
            imgDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    trainingFragment.startDownload(getAdapterPosition());
                }
            });
            imgshare = (ImageView) itemLayoutView.findViewById(R.id.imgshare);
            imgshare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String filePath ="";

                    if (mDataList.get(getAdapterPosition()).getFileType().equalsIgnoreCase("audio")) {
                         filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getName() + ".mp3";

                    } else if (mDataList.get(getAdapterPosition()).getFileType().equalsIgnoreCase("video")) {
                         filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getName() + ".mp4";

                    }  else if (mDataList.get(getAdapterPosition()).getFileType().equalsIgnoreCase("pdf")) {
                     filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getName() + ".pdf";

                } else if (mDataList.get(getAdapterPosition()).getFileType().equalsIgnoreCase("zip")) {
                        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(getAdapterPosition()).getName() + ".zip";
                    }

                        Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType( "application/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Log.e("file path",filePath);

                   intent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(new File(filePath)));
                   mContext.startActivity(Intent.createChooser(intent, "Share Content"));
                }
            });

            txtName = (TextView) itemLayoutView.findViewById(R.id.txtName);
        }
    }

    public void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    private void showNoFilePresentPopUp() {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(mContext.getString(R.string.app_name));

        // Setting Dialog Message
        alertDialog.setMessage(mContext.getString(R.string.fileNotPresent));

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.logomulya);
        // Setting OK Button
        alertDialog.setButton(mContext.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.txtCount.setVisibility(View.GONE);
        holder.txtName.setText(mDataList.get(position).getName());
        Log.i("Value", "Position " + position + " : " + isFileAvalible(position));
        if (isFileAvalible(position)) {
            holder.imgDownload.setVisibility(View.GONE);
            holder.imgshare.setVisibility(View.VISIBLE);

            /* if (mDataList.get(position).getFileType().equalsIgnoreCase("pdf")){
                 holder.imgshare.setVisibility(View.VISIBLE);
            }else {
                 holder.imgshare.setVisibility(View.GONE);
             }
*/
        } else {
            holder.imgDownload.setVisibility(View.VISIBLE);
            holder.imgshare.setVisibility(View.GONE);
        }
    }

    private boolean isFileAvalible(int position) {
        if (mDataList.get(position).getFileType() != null) {
            if (mDataList.get(position).getFileType().equalsIgnoreCase("zip")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/UnZip/" + mDataList.get(position).getName();
                if (new File(filePath).exists())
                    return true;
                return false;
            } else if (mDataList.get(position).getFileType().equalsIgnoreCase("pdf")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(position).getName() + ".pdf";
                if (new File(filePath).exists())
                    return true;
                return false;
            } else if (mDataList.get(position).getFileType().equalsIgnoreCase("video")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(position).getName() + ".mp4";
                if (new File(filePath).exists())
                    return true;
                return false;
            } else if (mDataList.get(position).getFileType().equalsIgnoreCase("audio")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(position).getName() + ".mp3";
                if (new File(filePath).exists())
                    return true;
                return false;
            } else if (mDataList.get(position).getFileType().equalsIgnoreCase("ppt")) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MV/Zip/" + mDataList.get(position).getName() + ".ppt";
                if (new File(filePath).exists())
                    return true;
                return false;
            }
        }
        return false;
    }


}

