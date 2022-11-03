package com.yung.flutter_audio_manager;

import androidx.annotation.NonNull;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterAudioManagerPlugin */
public class FlutterAudioManagerPlugin implements FlutterPlugin, MethodCallHandler {
  private static MethodChannel channel;
  private static AudioManager audioManager;
  private static Context activeContext;
  private static int audioFocusState = 0;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_audio_manager");
    channel.setMethodCallHandler(new FlutterAudioManagerPlugin());
    AudioChangeReceiver receiver = new AudioChangeReceiver(listener);
    IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    activeContext = flutterPluginBinding.getApplicationContext();
    activeContext.registerReceiver(receiver, filter);
    audioManager = (AudioManager) activeContext.getSystemService(Context.AUDIO_SERVICE);
  }

  public static void registerWith(Registrar registrar) {
    channel = new MethodChannel(registrar.messenger(), "flutter_audio_manager");
    channel.setMethodCallHandler(new FlutterAudioManagerPlugin());
    AudioChangeReceiver receiver = new AudioChangeReceiver(listener);
    IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    activeContext = registrar.activeContext();
    activeContext.registerReceiver(receiver, filter);
    audioManager = (AudioManager) activeContext.getSystemService(Context.AUDIO_SERVICE);
  }

  static AudioEventListener listener = new AudioEventListener() {
    @Override
    public void onChanged() {
      channel.invokeMethod("inputChanged", 1);
    }
  };

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getCurrentOutput")) {
      result.success(getCurrentOutput());
    } else if (call.method.equals("getAvailableInputs")) {
      result.success(getAvailableInputs());
    } else if (call.method.equals("changeToReceiver")) {
      result.success(changeToReceiver());
    } else if (call.method.equals("changeToSpeaker")) {
      result.success(changeToSpeaker());
    } else if (call.method.equals("changeToHeadphones")) {
      result.success(changeToHeadphones());
    } else if (call.method.equals("changeToBluetooth")) {
      result.success(changeToBluetooth());
    } else if (call.method.equals("requestAudioFocus")) {
      result.success(requestAudioFocus());
    } else if (call.method.equals("abandonAudioFocus")) {
      result.success(abandonAudioFocus());
    } else if (call.method.equals("setMixWithOthers")) {
        result.success(setMixWithOthers(call.arguments != null));
    } else if (call.method.equals("getAudioFocusState")) {
        result.success(audioFocusState);
    } else {
      result.notImplemented();
    }
  }

  private Boolean changeToReceiver() {
    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    audioManager.stopBluetoothSco();
    audioManager.setBluetoothScoOn(false);
    audioManager.setSpeakerphoneOn(false);
    listener.onChanged();
    return true;
  }

  private Boolean changeToSpeaker() {
    audioManager.setMode(AudioManager.MODE_NORMAL);
    audioManager.stopBluetoothSco();
    audioManager.setBluetoothScoOn(false);
    audioManager.setSpeakerphoneOn(true);
    listener.onChanged();
    return true;
  }

  private Boolean changeToHeadphones() {
    return changeToReceiver();
  }

  private Boolean changeToBluetooth() {
    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    audioManager.startBluetoothSco();
    audioManager.setBluetoothScoOn(true);
    listener.onChanged();
    return true;
  }

  private List<String> getCurrentOutput() {
    List<String> info = new ArrayList();
    if (audioManager.isSpeakerphoneOn()) {
      info.add("Speaker");
      info.add("2");
    } else if (audioManager.isBluetoothScoOn()) {
      info.add("Bluetooth");
      info.add("4");
    } else if (audioManager.isWiredHeadsetOn()) {
      info.add("Headset");
      info.add("3");
    } else {
      info.add("Receiver");
      info.add("1");
    }
    return info;
    // if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    // MediaRouter mr = (MediaRouter)
    // activeContext.getSystemService(Context.MEDIA_ROUTER_SERVICE);
    // MediaRouter.RouteInfo routeInfo =
    // mr.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO);
    // Log.d("aaa", "getCurrentOutput:
    // "+audioManager.isSpeakerphoneOn()+audioManager.isWiredHeadsetOn()+audioManager.isSpeakerphoneOn());
    // info.add(routeInfo.getName().toString());
    // info.add(_getDeviceType(routeInfo.getDeviceType()));
    // } else {
    // info.add("unknow");
    // info.add("0");
    // }
    // return info;
  }

  private List<List<String>> getAvailableInputs() {
    List<List<String>> list = new ArrayList();
    list.add(Arrays.asList("Receiver", "1"));
    if (audioManager.isWiredHeadsetOn()) {
      list.add(Arrays.asList("Headset", "3"));
    }
    if (audioManager.isBluetoothScoOn()) {
      list.add(Arrays.asList("Bluetooth", "4"));
    }
    return list;
  }

  private String _getDeviceType(int type) {
    Log.d("type", "type: " + type);
    switch (type) {
      case 3:
        return "3";
      case 2:
        return "2";
      case 1:
        return "4";
      default:
        return "0";
    }
  }

  private Boolean requestAudioFocus(){
    audioFocusState = 1;
    //在播放的时候为AudioManager添加获取焦点的监听
    int result = audioManager.requestAudioFocus(afChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN);
    return true;
  }

  //注册OnAudioFocusChangeListener监听
  AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
    public void onAudioFocusChange(int focusChange) {
      if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
      } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
      } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
        abandonAudioFocus();
      }
    }
  };

  private Boolean abandonAudioFocus(){
    audioManager.abandonAudioFocus(afChangeListener);
    audioFocusState = 0;
    return true;
  }

  private Boolean setMixWithOthers(Boolean v){
    if(!v){
      requestAudioFocus();
    }
    else{
      abandonAudioFocus();
    }
    return true;
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    // 切换到正常的模式
    if(channel != null){
      channel.setMethodCallHandler(null);
      channel = null;
    }
  }
}
