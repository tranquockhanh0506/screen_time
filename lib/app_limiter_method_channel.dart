import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'app_limiter_platform_interface.dart';

/// An implementation of [AppLimiterPlatform] that uses method channels.
class MethodChannelAppLimiter extends AppLimiterPlatform {
  /// The method channel used to interact with the native platform.
  final methodChannel = const MethodChannel('app_limiter');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
    return version;
  }

  /// iOS-specific implementation for blocking and unblocking apps
  @override
  Future<void> blockAndUnblockIOSApp() async {
    try {
      await methodChannel.invokeMethod('blockApp');
    } on PlatformException catch (e) {
      debugPrint('Failed to block/Unbloc iOS app: ${e.message}');
    }
  }

  /// Requests iOS permissions through the native implementation
  @override
  Future<bool> requestIosPermission() async {
    try {
      final result = await methodChannel.invokeMethod('requestPermission');
      return result;
    } on PlatformException catch (e) {
      debugPrint('Failed to get status: ${e.message}');
      return false;
    }
  }

  /// Checks Android permission status through the native implementation
  @override
  Future<bool> isAndroidPermissionAllowed() async {
    try {
      final result = await methodChannel.invokeMethod('checkPermission');
      if (result == "approved") {
        return true;
      } else {
        return false;
      }
    } on PlatformException catch (e) {
      debugPrint('Failed to get status: ${e.message}');
      return false;
    }
  }

  /// Requests Android permissions through the native implementation
  @override
  Future<void> requestAndroidPermission() async {
    try {
      await methodChannel.invokeMethod('requestAuthorization');
    } on PlatformException catch (e) {
      debugPrint('Failed to request android permission app: ${e.message}');
    }
  }

  /// Android-specific implementation for blocking apps
  @override
  Future<void> blockAndroidApps() async {
    try {
      await methodChannel.invokeMethod('blockApp');
    } on PlatformException catch (e) {
      debugPrint('Failed to block Android app: ${e.message}');
    }
  }

  /// Android-specific implementation for unblocking apps
  @override
  Future<void> unblockAndroidApps() async {
    try {
      await methodChannel.invokeMethod('unblockApp');
    } on PlatformException catch (e) {
      debugPrint('Failed to unblock Android app: ${e.message}');
    }
  }

  @override
  Future<void> unblockAllIosApps() async {
    try {
      await methodChannel.invokeMethod('unblockAppIOS');
    } on PlatformException catch (e) {
      debugPrint('Failed to unblock Android app: ${e.message}');
    }
  }
}
