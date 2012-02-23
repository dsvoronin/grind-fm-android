package com.dsvoronin.grindfm.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.dsvoronin.grindfm.R;

public class MenuButton extends LinearLayout {

    public MenuButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_button, this, true);

        TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.com_akzia_apps_mdp_view_MenuButton);

        TextView label = (TextView) findViewById(R.id.menuButtonTitle);
        int attrLabel = arr.getResourceId(R.styleable.com_akzia_apps_mdp_view_MenuButton_label, -1);
        if (attrLabel != -1) {
            label.setText(attrLabel);
        }

        ImageView image = (ImageView) findViewById(R.id.menuButtonIcon);

        int attrImage = arr.getResourceId(R.styleable.com_akzia_apps_mdp_view_MenuButton_image, -1);

        if (attrImage != -1) {
            image.setImageResource(attrImage);
        }

        arr.recycle();
    }
}
