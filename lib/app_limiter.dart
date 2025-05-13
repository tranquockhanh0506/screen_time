import 'app_limiter_platform_interface.dart';

class AppLimiter {
  Future<String?> getPlatformVersion() {
    return AppLimiterPlatform.instance.getPlatformVersion();
  }

  Future<void> blockAndUnblockIOSApp() {
    return AppLimiterPlatform.instance.blockAndUnblockIOSApp();
  }

  Future<bool> requestIosPermission() {
    return AppLimiterPlatform.instance.requestIosPermission();
  }

  Future<bool> isAndroidPermissionAllowed() {
    return AppLimiterPlatform.instance.isAndroidPermissionAllowed();
  }

  Future<void> requestAndroidPermission() {
    return AppLimiterPlatform.instance.requestAndroidPermission();
  }

  Future<void> blocAndroidApp() {
    return AppLimiterPlatform.instance.blockAndroidApps();
  }

  Future<void> unblocAndroidApp() {
    return AppLimiterPlatform.instance.unblockAndroidApps();
  }
}
