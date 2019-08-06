package com.kit.cordova.AMapLocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.widget.Toast;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Permission;
import java.util.Locale;

/**
 * 高德地图 api http://lbs.amap.com/api/android-location-sdk/guide/android-location/getlocation
 */
public class LocationPlugin extends CordovaPlugin {

    protected final static String[] permissions = {
        // Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    };
    public static final int ACCESS_LOCATION = 1;

    private AMapLocationClient locationClient = null;
    private AMapLocationClient locationClientWatch = null;
    private Context context;
    private CallbackContext callbackContext = null;
    private CallbackContext callbackContextWatch = null;
    private AMapLocationClientOption locationOptionWatch = null;
    // // 猎鹰，因为轨迹ID
    // private AMapTrackClient aMapTrackClient = null;
    // long serviceId = args;
    // long terminalId = args;
    // long trackId = null;
    // boolean weekEndRun = false; //是否在周末进行定位
    // long startTime = 800;   //开始时间，08:00
    // long endTime = 1800;    //结束时间, 18:00

    // Timer timer = null;
    // TimerTask timerTask = null;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        context = this.cordova.getActivity().getApplicationContext();
        locationClient = new AMapLocationClient(context);
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        locationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);// 使用签到定位场景
        locationClient.setLocationOption(locationOption); // 设置定位参数
        locationClient.setLocationListener(mLocationListener("one"));

        locationOptionWatch = new AMapLocationClientOption();
        locationOptionWatch.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        locationOptionWatch.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        locationOptionWatch.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        locationOptionWatch.setInterval(60 * 1000);//可选，设置定位间隔。默认为2秒
        locationOptionWatch.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        locationOptionWatch.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        locationOptionWatch.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        locationOptionWatch.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        locationOptionWatch.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        locationClientWatch = new AMapLocationClient(context);
        locationClientWatch.setLocationOption(locationOptionWatch); // 设置定位参数
        locationClientWatch.enableBackgroundLocation(3080, buildNotification());
        locationClientWatch.setLocationListener(mLocationListener("watch"));

        // aMapTrackClient = new AMapTrackClient(content);
        // timer = new Timer(true);
        // timerTask = new TimerTask() {
        //     public void run() {
        //         //每次需要执行的代码放到这里面。
        //         checkStartTrack();
        //     }
        // };
        // timer.schedule(timerTask, 1, 60*1000);

        super.initialize(cordova, webView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("getlocation".equals(action.toLowerCase(Locale.CHINA)) || "watchlocation".equals(action.toLowerCase(Locale.CHINA))) {
            int timeout = 0;
            if("watchlocation".equals(action.toLowerCase(Locale.CHINA))){
                timeout = args.getInt(0);
                this.callbackContextWatch = callbackContext;
            } else {
                this.callbackContext = callbackContext;
            }
            Log.d("dingweigetlocation",String.valueOf(timeout));
            if (context.getApplicationInfo().targetSdkVersion < 23) {
                this.getLocation(timeout);
            } else {
                boolean access_fine_location = PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
                boolean access_coarse_location = PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                boolean access_backgroup_location = true;// PermissionHelper.hasPermission(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                if (access_fine_location && access_coarse_location && access_backgroup_location) {
                    this.getLocation(timeout);
                } else {
                    PermissionHelper.requestPermissions(this, ACCESS_LOCATION, permissions);
                }
            }
            return true;
        } else if ("startTrack".equals(action.toLowerCase(Locale.CHINA))) {
            // aMapTrackClient.addTrack(new AddTrackRequest(serviceId, terminalId), new OnTrackListener() {
            //     @Override
            //     public void onAddTrackCallback(AddTrackResponse addTrackResponse) {
            //         if (addTrackResponse.isSuccess()) {
            //             trackId = addTrackResponse.getTrid();
            //         } else {
            //             Log.d("网络请求失败，" + addTrackResponse.getErrorMsg());
            //             callbackContext.error(addTrackResponse.getErrorMsg());
            //         }
            //     }
            // }
        }
        return false;
    }
    // private void checkStartTrack() {

    // }
    // private void startTrack() {
    //     if(serviceId && terminalId && trackId) {
    //         try {
    //             TrackParam trackParam = new TrackParam(serviceId, terminalId);
    //             trackParam.setTrackId(trackId);
    //             aMapTrackClient.setInterval(60, 300);
    //             aMapTrackClient.startTrack(trackParam, onTrackLifecycleListener);

    //             JSONObject jo = new JSONObject();
    //             jo.put("success", true);
    //             jo.put("message", "开启实时定位");
    //             callbackContext.success(jo);
    //         } catch (JSONException e) {
    //             Log.d("定位开启失败!",e);
    //             callbackContext.error("定位开启失败!");
    //         }
    //     } else {
    //         Log.d("定位参数不全!",serviceId,terminalId,trackId);
    //         callbackContext.error("定位参数不全!");
    //     }
    // }

    private void getLocation(int timeout) {
        // 设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        if (timeout > 0) {
            locationOptionWatch.setInterval(timeout);//可选，设置定位间隔。默认为2秒
            locationClientWatch.setLocationOption(locationOptionWatch); // 设置定位参数
            if(locationClientWatch.isStarted()) locationClientWatch.stopLocation();
            locationClientWatch.startLocation(); // 启动定位
            callbackContextWatch.sendPluginResult(r);
        } else if (timeout < 0) {
            if(locationClientWatch.isStarted()) locationClientWatch.stopLocation();
            String msg = "关闭后台定位";
            JSONObject rvJo = new JSONObject();
            try{
                rvJo.put("msg", msg);
            } catch (JSONException e) {
                rvJo = null;
                e.printStackTrace();
            }
            callbackContextWatch.success(rvJo);
        } else {
            if(locationClient.isStarted()) locationClient.stopLocation();
            locationClient.startLocation(); // 启动定位
            callbackContext.sendPluginResult(r);
        }
    }

    private static final String NOTIFICATION_CHANNEL_NAME = "BackgroundLocation";
	private NotificationManager notificationManager = null;
	boolean isCreateChannel = false;
    @SuppressLint("NewApi")
    private Notification buildNotification() {
        Log.d("dingwei", "startBuildNotification");
        Notification.Builder builder = null;
        Notification notification = null;
        Log.d("dingwei", String.valueOf(android.os.Build.VERSION.SDK_INT));
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            // Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (null == notificationManager) {
                notificationManager = (NotificationManager) this.cordova.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = this.cordova.getActivity().getPackageName();
            Log.d("dingwei", channelId);
            if (!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);// 是否在桌面icon右上角展示小圆点
                notificationChannel.setLightColor(Color.BLUE); // 小圆点颜色
                notificationChannel.setShowBadge(true); // 是否在久按桌面图标时显示此渠道的通知
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(context, channelId);
        } else {
            builder = new Notification.Builder(context);
        }
        builder.setContentTitle(getAppName(context)).setContentText("正在后台运行").setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;
    }

    private String getAppName(Context context) {
        String appName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            appName = context.getResources().getString(labelRes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Log.d("dingweiAppName", appName);
        return appName;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
            throws JSONException {
        switch (requestCode) {
        case ACCESS_LOCATION:
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.getLocation(60);
            } else {
                Toast.makeText(this.cordova.getActivity(), "请开启应用定位权限", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private AMapLocationListener mLocationListener(String locationType) {
        final String type = locationType;
        return new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                Log.d("dingwei","onLocationChanged" + type);
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        int locationType = amapLocation.getLocationType();//获取当前定位结果来源 定位类型对照表: http://lbs.amap.com/api/android-location-sdk/guide/utilities/location-type/
                        Double latitude = amapLocation.getLatitude();//获取纬度
                        Double longitude = amapLocation.getLongitude();//获取经度
                        float accuracy = amapLocation.getAccuracy();//获取精度信息
                        String address = amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                        String country = amapLocation.getCountry();//国家信息
                        String province = amapLocation.getProvince();//省信息
                        String city = amapLocation.getCity();//城市信息
                        String district = amapLocation.getDistrict();//城区信息
                        String street = amapLocation.getStreet();//街道信息
                        String streetNum = amapLocation.getStreetNum();//街道门牌号信息
                        String cityCode = amapLocation.getCityCode();//城市编码
                        String adCode = amapLocation.getAdCode();//地区编码
                        String aoiName = amapLocation.getAoiName();//获取当前定位点的AOI信息
                        String floor = amapLocation.getFloor();//获取当前室内定位的楼层
                        int gpsAccuracyStatus = amapLocation.getGpsAccuracyStatus();//获取GPS的当前状态
                        long time = amapLocation.getTime(); // 时间
                        JSONObject jo = new JSONObject();
                        try {
                            jo.put("locationType", locationType);
                            jo.put("latitude", latitude);
                            jo.put("longitude", longitude);
                            jo.put("accuracy", accuracy);
                            jo.put("address", address);
                            jo.put("country", country);
                            jo.put("province", province);
                            jo.put("city", city);
                            jo.put("district", district);
                            jo.put("street", street);
                            jo.put("streetNum", streetNum);
                            jo.put("cityCode", cityCode);
                            jo.put("adCode", adCode);
                            jo.put("aoiName", aoiName);
                            jo.put("floor", floor);
                            jo.put("gpsAccuracyStatus", gpsAccuracyStatus);
                            jo.put("time", time);
                        } catch (JSONException e) {
                            jo = null;
                            e.printStackTrace();
                        }
                        Log.d(address, "dingwei");
                        if ("watch".equals(type)) {
                            PluginResult r = new PluginResult(PluginResult.Status.OK, jo);
                            r.setKeepCallback(true);
                            callbackContextWatch.sendPluginResult(r);
                        } else {
                            callbackContext.success(jo);
                        }
                    } else {
                        // 定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Log.d("AmapError", "location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
                        if ("watch".equals(type)) {
                            PluginResult r = new PluginResult(PluginResult.Status.ERROR, amapLocation.getErrorInfo());
                            r.setKeepCallback(true);
                            callbackContext.sendPluginResult(r);
                        } else {
                            callbackContext.error(amapLocation.getErrorInfo());
                        }
                    }
                } else {
                    Log.d("dingwei","Shibai");
                }
            }
        };
    }

    // // 猎鹰监听器
    // private OnTrackLifecycleListener onTrackLifecycleListener = new OnTrackLifecycleListener() {
    //     @Override
    //     public void onStartGatherCallback(int status, String msg) {
    //         if (status == ErrorCode.TrackListen.START_GATHER_SUCEE ||
    //                 status == ErrorCode.TrackListen.START_GATHER_ALREADY_STARTED) {
    //             Toast.makeText(TestDemo.this, "定位采集开启成功！", Toast.LENGTH_SHORT).show();
    //         } else {
    //             Toast.makeText(TestDemo.this, "定位采集启动异常，" + msg, Toast.LENGTH_SHORT).show();
    //         }
    //     }

    //     @Override
    //     public void onStartTrackCallback(int status, String msg) {
    //         if (status == ErrorCode.TrackListen.START_TRACK_SUCEE ||
    //                 status == ErrorCode.TrackListen.START_TRACK_SUCEE_NO_NETWORK ||
    //                 status == ErrorCode.TrackListen.START_TRACK_ALREADY_STARTED) {
    //             // 服务启动成功，继续开启收集上报
    //             aMapTrackClient.startGather(this);
    //         } else {
    //             Toast.makeText(TestDemo.this, "轨迹上报服务服务启动异常，" + msg, Toast.LENGTH_SHORT).show();
    //         }
    //     }
    // };
}
