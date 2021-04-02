package com.droid.application.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.droid.application.R;

public class LoadingView extends RelativeLayout
{
    
    public static String TAG = LoadingView.class.getSimpleName();
    //private Context context;
    
    public LoadingView(Context context) {
    super(context);
    init(context, null);
  }

  public LoadingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    setBackgroundColor(Color.WHITE);
  }
  
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setKeepScreenOn(true);

        // Instantiate and add TextureView for rendering
        final LayoutInflater li = LayoutInflater.from(getContext());
        View mTextureFrame = li.inflate(R.layout.layout_loading_view, this, false);
        addView(mTextureFrame);
        
        ShimmerLayout shimmerLayout = (ShimmerLayout) mTextureFrame.findViewById(R.id.shimmer_layout);
        shimmerLayout.startShimmerAnimation();
    }
}
