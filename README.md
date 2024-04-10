# TextViewer

An Android 4.4+ application that reads and displays a *.txt file. There are no editing features.

A unique-ish feature of this app is that it doesn't depend on any external libraries. No androidx, no support library, only uses what's already present on Android API, so the APK size is very small (release build weights 66KB at the time of writing). This means the code relies on a lot of deprecated APIs (Google has deprecated everything of Fragments, Preferences, etc) but this is unavoidable without bloating the APK size.

## Features

* Small apk size
* 3 themes: Dark, Black, and Light
* Choose from 5 font size
* Hide the toolbar while scrolling (optional)
* Automatically load the last viewed file on launch (optional)

## License

[MIT License](https://opensource.org/license/mit)
