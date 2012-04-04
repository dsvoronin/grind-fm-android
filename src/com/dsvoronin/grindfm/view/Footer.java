package com.dsvoronin.grindfm.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.dsvoronin.grindfm.R;

/**
 * User: dsvoronin
 * Date: 04.04.12
 * Time: 11:33
 */
public class Footer extends LinearLayout {


    public Footer(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.footer, this, true);
    }
}
