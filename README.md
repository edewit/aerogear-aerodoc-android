aerogear-aerodoc-android [![Build Status](https://travis-ci.org/aerogear/aerogear-aerodoc-android.png)](https://travis-ci.org/aerogear/aerogear-aerodoc-android)
================

# Prerequisite

* Having the AndroidSDK installed, ANDROID_HOME set and sdk bin tools added to your path. For more info check [here](https://developer.android.com/sdk/index.html)

* Have a Push App configured in the Unified Push Server along with an Android Variant

* Set up your GCM settings : http://aerogear.org/docs/guides/aerogear-push-android/google-setup/
 
# Clone aerodoc-android

```
git clone https://github.com/aerogear/aerogear-aerodoc-android


```
# Edit your push settings 

Edit :
```

https://github.com/aerogear/aerogear-aerodoc-android/blob/master/src/org/jboss/aerogear/android/unifiedpush/aerodoc/AeroDocApplication.java


```

And be sure to set these properties : 

```

private static final String BASE_BACKEND_URL = "";
private static final String UNIFIED_PUSH_URL = "";
private static final String GCM_SENDER_ID = "";
private static final String VARIANT_ID = "";
private static final String SECRET = "";

```

# Build the app

``` mvn clean install ```

# Deploy to your Device

``` adb -d install target/aerodoc-android.apk ```

  


