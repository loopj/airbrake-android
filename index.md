---
layout: project
title: Hoptoad Notifer for Android
tagline: Automatically Notify Hoptoad of Exceptions in your Android App
version: 1.0
github_url: https://github.com/loopj/hoptoad-android
download_url: https://github.com/loopj/hoptoad-android/zipball/hoptoad-android-1.0
---


Overview
--------
The Hoptoad notifier for Android is designed to give you instant notification
of exceptions thrown from your Android applications. The notifier hooks into
`Thread.UncaughtExceptionHandler`, which means any uncaught exceptions will
trigger a notification to be sent to your Hoptoad project.


Features
--------
- Automatic notification of uncaught exceptions
- Exceptions buffered to disk when no internet connection is available, and sent later
- Designed from the ground up to be robust, the notifier should not itself cause crashes or
  freezes
- Minimal cpu and memory footprint
- Optionally send your own non-fatal exceptions to hoptoad


Installation & Setup
--------------------
Download the latest .jar file from github and place it in your Android app's
`lib/` folder.

Import the HoptoadNotifier class in your app's main Activity.

{% highlight java %}
import com.loopj.android.hoptoad.HoptoadNotifier;
{% endhighlight %}

In your activity's `onCreate` function, register to begin capturing exceptions:
{% highlight java %}
HoptoadNotifier.register(this, "your-api-key-goes-here");
{% endhighlight %}


Configuration
-------------
The `HoptoadNotifier.register` call requires a context and Hoptoad API key to
be passed in, and optionally a third argument specifying the environment.
The environment defaults to `production` if not set.

To notify Hoptoad of non-fatal exceptions, or exceptions you have explicitly
caught in your app, you can call `HoptoadNotifier.notify`. This call takes
exactly one argument, a Throwable, and can be called from anywhere in your
code. For example:

{% highlight java %}
try {
    // Something dangerous
} catch(Exception e) {
    // We don't want this to crash our app, but we would like to be notified
    HoptoadNotifier.notify(e);
}
{% endhighlight %}


Reporting Bugs or Feature Requests
----------------------------------
Please report any bugs or feature requests on the github issues page for this
project here:

<https://github.com/loopj/hoptoad-android/issues>


License
-------
The Hoptoad notifier for Android is released under the Android-friendly
Apache License, Version 2.0. Read the full license here:

<http://www.apache.org/licenses/LICENSE-2.0>
