package com.canyonsappclub.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class TabInfo
{
	String tag;
    Class<?> tabClass;
    Bundle args;
    Fragment fragment;
    
    TabInfo(String tag, Class<?> tabClass, Bundle args)
    {
        this.tag = tag;
        this.tabClass = tabClass;
        this.args = args;
    }
}
