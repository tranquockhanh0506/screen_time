import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'app_limiter_method_channel.dart';

abstract class AppLimiterPlatform extends PlatformInterface {
  AppLimiterPlatform() : super(token: _token);

  static final Object _token = Object();

  static AppLimiterPlatform _instance = MethodChannelAppLimiter();

  static AppLimiterPlatform get instance => _instance;

  static set instance(AppLimiterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion();
  Future<void> blockAndUnblockIOSApp();
  Future<bool> requestIosPermission();
  Future<bool> isAndroidPermissionAllowed();
  Future<void> requestAndroidPermission();
  Future<void> blockAndroidApps();
  Future<void> unblockAndroidApps();
}
