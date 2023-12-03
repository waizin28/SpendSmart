package com.cs407.spendsmart;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

public class AddingDialog {

    private Activity activity;
    private AlertDialog alertDialog;

    LottieAnimationView animationView;

    AddingDialog(Activity myactivity){
        activity = myactivity;
    }

    void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.loading_dialog, null);
        builder.setView(view);
        builder.setCancelable(false);
        animationView = view.findViewById(R.id.loadingAni);
        animationView.playAnimation();
        animationView.setRepeatCount(LottieDrawable.INFINITE);
        animationView.setSpeed(1.5f);
        alertDialog = builder.create();
        alertDialog.show();
    }

    void dismissDialog(){
        alertDialog.dismiss();
    }

}
