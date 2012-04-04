package com.dsvoronin.grindfm.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.dsvoronin.grindfm.R;

public class MenuButton extends RelativeLayout {

    private ImageView image;

    private Pickable pickable;

    private OnPickListener onPickListener;

    public MenuButton(final Context context, AttributeSet attrs) {
        super(context, attrs);

        pickable = (Pickable) context;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_button, this, true);

        TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.com_akzia_apps_mdp_view_MenuButton);

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
                pickable.pick(MenuButton.this);
                onPickListener.onPick();
            }
        });
    }

    @Override
    public void setSelected(boolean selected) {
        image.setSelected(true);
    }

    public void setOnPickListener(OnPickListener onPickListener) {
        this.onPickListener = onPickListener;
    }

    public void setUnpicked() {
        image.setSelected(false);
    }

    public interface Pickable {
        public void pick(MenuButton pickedButton);
    }

    public interface OnPickListener {
        public void onPick();
    }
}
