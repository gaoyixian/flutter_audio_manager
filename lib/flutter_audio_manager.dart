import 'dart:async';
import 'package:flutter/services.dart';

enum AudioPort {
  /// unknow 0
  unknow,

  /// input 1
  receiver,

  /// out speaker 2
  speaker,

  /// headset 3
  headphones,

  /// bluetooth 4
  bluetooth,
}

class AudioInput {
  final String name;
  final int _port;
  AudioPort get port {
    return AudioPort.values[_port];
  }

  const AudioInput(this.name, this._port);

  @override
  String toString() {
    return "name:$name,port:$port";
  }
}

// 声音输出类型
const int _outPutTypeSpeaker = 1;
const int _outPutTypeReceiver = 2;

class FlutterAudioManager {
  static const MethodChannel _channel =
      MethodChannel('flutter_audio_manager');
  static void Function() ?_onInputChanged;

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<AudioInput> getCurrentOutput() async {
    final List<dynamic> data = await _channel.invokeMethod('getCurrentOutput');
    return AudioInput(data[0], int.parse(data[1]));
  }

  static Future<List<AudioInput>> getAvailableInputs() async {
    final List<dynamic> list =
        await _channel.invokeMethod('getAvailableInputs');

    List<AudioInput> arr = [];
    for (var data in list) {
      arr.add(AudioInput(data[0], int.parse(data[1])));
    }
    return arr;
  }

  static int _outPutType = 0;
  
  static Future<bool> changeToSpeaker() async {
    if(_outPutType == _outPutTypeSpeaker){
      return true;
    }
    var b = await _channel.invokeMethod('changeToSpeaker');
    if(b){
      _outPutType = _outPutTypeSpeaker;
    }
    return b;
  }

  static Future<bool> changeToReceiver() async {
     if(_outPutType == _outPutTypeReceiver){
      return true;
    }
    var b = await _channel.invokeMethod('changeToReceiver');
    if(b){
      _outPutType = _outPutTypeReceiver;
    }
    return  b;
  }

  static Future<bool> changeToHeadphones() async {
    return await _channel.invokeMethod('changeToHeadphones');
  }

  static Future<bool> changeToBluetooth() async {
    return await _channel.invokeMethod('changeToBluetooth');
  }

  static Future<dynamic> requestAudioFocus() async {
    return await _channel.invokeMethod('requestAudioFocus');
  }

  static Future<dynamic> abandonAudioFocus() async {
    return await _channel.invokeMethod('abandonAudioFocus');
  }

  static Future<void> setMixWithOthers(bool v)async{
    await _channel.invokeMethod('setMixWithOthers',v? "1":null);
  }

  static Future<int> getAudioFocusState()async{
    return await _channel.invokeMethod('getAudioFocusState');
  }

  static void setListener(void Function() onInputChanged) {
    FlutterAudioManager._onInputChanged = onInputChanged;
    _channel.setMethodCallHandler(_methodHandle);
  }

  static Future<void> _methodHandle(MethodCall call) async {
    if (_onInputChanged == null) return;
    switch (call.method) {
      case "inputChanged":
        return _onInputChanged!();
      default:
        break;
    }
  }
}
