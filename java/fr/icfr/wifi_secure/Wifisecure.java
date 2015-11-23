package fr.icfr.wifi_secure;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.util.List;

//import android.telephony.IccOpenLogicalChannelResponse;

public class Wifisecure extends Application {

    @Override
     public void onCreate() {


        String ssid = "";
        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String simOperator = tel.getSimOperator(); // Not getNetworkOperator wrt Roaming

        if (simOperator != null) {
            int mcc = Integer.parseInt(simOperator.substring(0, 3));
            int mnc = Integer.parseInt(simOperator.substring(3));
            if ( mcc == 208 )
            {
                if ( ( mnc >=  9 ) && ( mnc <= 13 ) ) { ssid = "SFR WiFi Mobile"; }
                if ( ( mnc >= 15 ) && ( mnc <= 16 ) ) { ssid = "FreeWifi_secure"; }
            }

            if ( !ssid.isEmpty() ) {
                WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
                enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.SIM); // EAP SIM / AKA for Mobile Phones

                SmsManager sm = SmsManager.getDefault();
                Bundle b = sm.getCarrierConfigValues();
                String NAI_suffix = b.getString(SmsManager.MMS_CONFIG_NAI_SUFFIX);

                // IMSI : 208(mcc) + 15(mnc) + 0000XXXXXX + @...
                enterpriseConfig.setIdentity("1"+tel.getSubscriberId()+"@"+NAI_suffix); // Use 1 + IMSI (See RFC4186)

                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = ssid;
                //wifiConfig.priority = 0; // Use lower priority than known APs
                wifiConfig.status = WifiConfiguration.Status.ENABLED;
                wifiConfig.allowedKeyManagement.clear();
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                wifiConfig.enterpriseConfig = enterpriseConfig;

                WifiManager wfMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                wfMgr.setWifiEnabled(true);
                wfMgr.disconnect();

                List<WifiConfiguration> list = wfMgr.getConfiguredNetworks();
                for( WifiConfiguration i : list ) {
                    if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                        wfMgr.removeNetwork(i.networkId);
                    }
                }
                int networkId = wfMgr.addNetwork(wifiConfig);
                if (networkId != -1) {
                    wfMgr.reconnect();
                    wfMgr.enableNetwork(networkId, true);
                }
                Toast.makeText(getApplicationContext(),"configuration effectuée",Toast.LENGTH_LONG).show();
            }

        }

        super.onCreate();
        }



}

/*

https://developer.android.com/sdk/api_diff/21/changes.html
https://developer.android.com/sdk/api_diff/21/changes/android.net.wifi.WifiEnterpriseConfig.Eap.html
https://developer.android.com/sdk/api_diff/21/changes/android.telephony.TelephonyManager.html

https://developer.android.com/reference/android/net/wifi/WifiEnterpriseConfig.Eap.html
https://developer.android.com/reference/android/telephony/IccOpenLogicalChannelResponse.html
https://developer.android.com/reference/android/telephony/TelephonyManager.html

android.telephony.TelephonyManager

Added Methods
boolean iccCloseLogicalChannel(int)
byte[] iccExchangeSimIO(int, int, int, int, int, String)
IccOpenLogicalChannelResponse iccOpenLogicalChannel(String)
String iccTransmitApduBasicChannel(int, int, int, int, int, String)
String iccTransmitApduLogicalChannel(int, int, int, int, int, int, String)
String sendEnvelopeWithStatus(String)

 */
