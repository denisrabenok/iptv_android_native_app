package dreamfuture.iptv;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by STAR-Z on 2017-09-25.
 */

public class MyDisabledRecyclerView extends RecyclerView {
    public MyDisabledRecyclerView(Context context) {
        super(context);
    }

    public MyDisabledRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDisabledRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }
}
