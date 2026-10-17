## App.speak

This function allows apps to use Google's Text-to-Speech service.  It is very simple to use, and can natively support many different dialects.  It accepts two arguments: language abbreviation, and text.  NOTE: You should never use this function as a medium to transmit sensitive information.  It sends the text unencrypted to Google, and then they send back the audio.

```
App.speak('en', 'Hello, world!');
```

> **In `siteroot`:** this function doesn't exist; calling it throws `TypeError: App.speak is not a function`. There is no text-to-speech support in the code, and no `ALLOW_TTS` [permission](Permissions.md).

---

_Source: [App.speak](https://sourceforge.net/p/skylightos/wiki/App.speak/) on the SourceForge wiki · Labels: app object, tts · Last modified 2014-08-26_

_Updated after reviewing the `siteroot` code._
