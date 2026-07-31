package com.fm619.paperphoneplus;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(ProxyPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
