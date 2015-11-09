package com.sky.opam.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.loic.common.graphic.AgendaView;

public class AgendaViewPage extends ViewPager
{
    public AgendaViewPage(Context context)
    {
        super(context);
    }

    public AgendaViewPage(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y)
    {
        if(v instanceof AgendaView)
        {
            return ((AgendaView) v).canScrollHorizontal(-dx);
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}
