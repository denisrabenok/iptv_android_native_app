package dreamfuture.iptv.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * Created by STAR-Z on 2017-09-19.
 */

public class SelfRemovingOnScrollListener extends RecyclerView.OnScrollListener {

    @Override
    public final void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            recyclerView.removeOnScrollListener(this);
        }
    }
}
