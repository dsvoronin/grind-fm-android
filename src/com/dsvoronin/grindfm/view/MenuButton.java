package com.dsvoronin.grindfm.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dsvoronin.grindfm.R;

public class MenuButton extends RelativeLayout {

    private ImageView image;

    private Pickable pickable;

    private OnPickListener onPickListener;

    private TextView label;

    public MenuButton(final Context context, AttributeSet attrs) {
        super(context, attrs);

        pickable = (Pickable) context;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_button, this, true);

        TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.com_akzia_apps_mdp_view_MenuButton);

//        label = (TextView) findViewById(R.id.menuButtonTitle);
//        label.setTypeface(Typeface.SERIF);
//        int attrLabel = arr.getResourceId(R.styleable.com_akzia_apps_mdp_view_MenuButton_label, -1);
//        if (attrLabel != -1) {
//            label.setText(attrLabel);
//        }

        image = (ImageView) findViewById(R.id.menuButtonIcon);
        int attrImage = arr.getResourceId(R.styleable.com_akzia_apps_mdp_view_MenuButton_image, -1);
        if (attrImage != -1) {
            image.setImageResource(attrImage);
        }

        arr.recycle();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                image.setSelected(true);
//                label.setTextColor(getContext().getResources().getColor(R.color.menu_text_selected));
                pickable.pick(MenuButton.this);
                onPickListener.onPick();
            }
        });
    }

    public void setOnPickListener(OnPickListener onPickListener) {
        this.onPickListener = onPickListener;
    }

    public void setUnpicked() {
        image.setSelected(false);
        label.setTextColor(getResources().getColor(R.color.menu_text));
    }

    public interface Pickable {
        public void pick(MenuButton pickedButton);
    }

    public interface OnPickListener {
        public void onPick();
    }
}
