# Android File Picker Light Library

<img src="https://raw.githubusercontent.com/maxieds/AndroidFilePickerLight/master/Screenshots/LibraryProfileIconPlayStore.png" width="400" />

<hr /><hr />

<img src="https://jitpack.io/v/maxieds/AndroidFilePickerLight.svg" /><img src="https://img.shields.io/badge/NOTE%3A-Project%20is%20a%20work%20in%20progress-orange" /><img src="https://img.shields.io/badge/Android%20API%2029%2B-It%20runs!!-brightgreen" /><img src="https://badges.frapsoft.com/os/gpl/gpl.svg?v=103" /> 


<img src="https://forthebadge.com/images/badges/made-with-java.svg" /><img src="https://forthebadge.com/images/badges/powered-by-coffee.svg" /><img src="https://forthebadge.com/images/badges/built-for-android.svg" />

<img src="https://badges.frapsoft.com/os/v2/open-source-175x29.png?v=103" />

## About the library 

A file and directory chooser widget for Android that focuses on presenting an easy to configure lightweight UI.
This library is intended to be a replacement for other picker libraries that 
works with the new **Android 11** file system and 
[storage management changes](https://developer.android.com/about/versions/11/privacy/storage). 
The source is made openly available as free software according to the 
[project license](https://github.com/maxieds/AndroidFilePickerLight/blob/main/LICENSE). 

The main design considerations were to 
create a file picker library with as minimal a footprint as possible to do basic file selection 
operations, and that the resulting library chooser display must be very easy to extend and 
configure with respect to its look-and-feel themes, color schemes, icons and other UI options that 
users will want to customize to their client application. 
I was unable to find a solid external library for my application use cases that was not 
bloated with respect to media loaders and image processing features, that could be easily 
extended, and that was not limited by only a cumbersome list of built-in themes that the 
user can select. Therefore, I decided to take the best functionality I found in other libraries 
(many written in Kotlin) and write a custom implementation in Java while keeping the 
media file processing minimal. 

### Feature set

Key features in the library include the following:
* Easy to configure theming and UI display settings including icons and color choices
* Simple actions and extendable Java interface to select and filter files/directories
* Allows client code to access many standard file system types on the Android device without 
  complicated procedures and permissions headaches inherited by the new Android 11 policy changes
* Exceptions and errors thrown at runtime extend the standard Java ``RuntimeException`` class for 
  ease of handling. Many of these exceptions are just wrappers around data returned by a newly 
  spawned file picker activity and do not necessarily indicate errors in the file selection process.
  
### Screenshots of the library in action (TODO)

<img src="" width="250" /><img src="" width="250" /><img src="" width="250" />

<img src="" width="250" /><img src="" width="250" /><img src="" width="250" />

## Including the library in an Android application

There are a couple of quickstart items covered in the sections below to handle before this
library can be included in the client Android application:
* Include the library using [Jitpack.io/GitHub](https://jitpack.io/#maxieds/AndroidFilePickerLight) 
  in the application *build.gradle* configuration.
* Update the project *AndroidManifest.xml* file to extend the documents provider, 
  request required permissions, and setup some helpful legacy file handling options for devices 
  targeting Android platforms with SDK < 11.
  
Examples of using the library to pick files and directories from client Java code is also 
included in the detailed documentation in the next section.

### Application build.gradle modifications

We will require the following small modifications to the client **project** *build.gradle* 
configuration:
```bash
android {
     defaultConfig {
        minSdkVersion 29
     }
     compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
dependencies {
    implementation 'com.github.maxieds:AndroidFilePickerLight:-SNAPSHOT'
}
allprojects {
    repositories {
        maven {
            url 'https://maven.fabric.io/public'
        }
    }
}
```

### Project manifest modifications

Near the top of the project manifest file, append the following permissions-related 
statements:
```xml
    <!-- Core storage permissions required: -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:required="true" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:required="true" />
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" android:required="false" />
    <uses-permission android:name="android.permission.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION" android:required="false" />
    <uses-permission android:name="android.permission.INTERNET" android:required="false" />
```
For applications targeting so-called legacy platforms, that is Android devices where the new 
Android 11 storage management options are not explicitly required, it is 
recommended for compatibility sake by the Google developer docs 
that the application set the option
``requestLegacyExternalStorage="true"``. For example, use the following code:
```xml
<application
        android:name=".MyAndroidProjectName"
        android:description="@string/appDesc"
        android:icon="@drawable/appIcon"
        android:label="@string/appLabelDesc"
        android:roundIcon="@drawable/appRoundIcon"
        android:theme="${appTheme}"
        android:launchMode="singleTop"
        android:manageSpaceActivity=".MyAndroidProjectMainActivity"
        android:requestLegacyExternalStorage="true"
        android:preserveLegacyExternalStorage="true"
        android:allowBackup="true"
        >
     <!-- Complete the internals of the application tag (activities, etc.) below -->
</application>
```

## Sample client Java source code

The next examples document basic, advanced, and custom uses of the library in client code. 

### Basic usage: Returning a file path selected by the user

This is a quick method to select a file and/or directory picked by the user:
```java
```

### Detailed list of non-display type options

The next options are available to configure the non-display type (e.g., properties of the 
file chooser that do not depend on how it looks) properties of the library. 
These can be set using the ``AndroidFilePickerLight.Builder`` class as follows:
```java
/* Set the action type return code (for use with onActivityResult): */

/* Select whether or not the user can pick a file, a directory, or either: */

/* Set whether multiple paths can be chosen, and set limits on how many if the 
 * chooser is configured to enable multiple path selection: 
 */

/* Define whether the chooser displays hidden files or not: */

/* Set a custom file and directory sort function: 
 * (Can prioritize directories versus files, or decide that certain types should be 
 *  displayed first in the file list -- sorted lexographically, or reverse lexographically)
 */

/* Set the current working directory, and/or limit the scope of the top level path up to 
 * which the user can navigate: 
 */

/* Enable/disable the default directory navigation buttons at the top of the chooser display: */

/* Default start path locations: */
// External/internal storage; User home directory; SDCard; Downloads/Pictures/DCIM/Screenshots/Camera/Video; 

/* Filter by file types, or exclude certain file types from being displayed: */

/* Set a timeout after which the display returns with an invalid path 
 * (should keep the client application from hanging on neglected chooser instances):
 */
// include whether to throw a certain exception type
// throw an invalid selection exception on error ??? 

/* Set desired file path return types (String path, File, URI, etc.): */

/* 
 * -- See next sections below to configure theme and UI display options like colors, default strings, 
 *    and to setup custom default icons in the chooser display --
 */
```

### Extending file types for filtering and sorting purposes in the picker UI

### Handling runtime exceptions and extending the runtime exception class (for custom error handling)

#### List of default exception sub-types

```java
/* Basic interface methods to implement for subclasses: */
public interface AndroidFilePickerLightException extends RuntimeException  {}

/* Some predefined options for exceptions that can be thrown by the chooser selection activity: */
public class GenericRuntimeErrorException extends AndroidFilePickerLightException { /* ... */ }
public class FileIOException extends AndroidFilePickerLightException { /* ... */ }
public class PermissionsErrorException extends AndroidFilePickerLightException { /* ... */ }
public class AbortedByTimeoutException extends AndroidFilePickerLightException { /* ... */ }
public class AbortedByUserActionException extends AndroidFilePickerLightException { /* ... */ }
public class CommunicateSelectionDataException extends AndroidFilePickerLightException { /* ... */ }
public class CommunicateNoDataException extends AndroidFilePickerLightException { /* ... */ }
public class InvalidInitialPathException extends AndroidFilePickerLightException { /* ... */ }
public class InvalidThemeResourceException extends AndroidFilePickerLightException { /* ... */ }
```

#### Checking whether the returned file path is valid

#### Determine whether an idle timeout occurred to close the picker

#### Query whether items in the list of paths are directories versus files


### Configuring the client theme and UI look-and-feel properties

#### Basic example (quickstart guide to using the file picker library)





#### Full example (detailed usage of the current custom theme/UI display options)

Note that with the exception of a few predefined themes that are included as 
default resources within the library packaging, all UI display related 
resources passed to customize the look-and-feel of the file picker display 
need to be resolved explicitly by the calling client activity. 
This means, in general, if the client passes a resource identifier (integer type) 
as the value for one of these configuration items, there is going to be an error 
where the library does not know where to find the resource. 
Some examples of converting the resources in the application ``/res/*`` directory 
from ID names to a format the library can use are suggested in the next code 
snippets:
```java
/* Assumptions about the activity (not the only, option, just for clarity): */
public class MyMainActivity extends AppCompatActivity { /* ... */ }
MyMainActivity myRunningActivityInst = MyMainActivity.getInstance();

/* Pass a new icon Drawable to replace the default setting: */
Drawable newFolderIcon = myRunningActivityInst.getResources().getDrawable(R.id.custom_folder_icon_24x24, myRunningActivityInst.getTheme());

/* Select a new custom color and get its hex-coded value: */
int newBackgroundColor = myRunningActivityInst.getResources().getColor(R.color.custom_picker_bgcolor, myRunningActivityInst.getTheme());

/* Set a new text value from a string resource: */
String newChoosetTitleText = myRunningActivityInst.getString(R.string.custom_chooser_title_msg);
```
Now that we have the scheme for passing resources to the library to skin/color/custom theme its UI down, 
here is the full listing and type specs for what attributes can actually be changed and reset on-the-fly:
```java
/* Get a handle to the configuration object for the file picker UI: */
AndroidFilePickerLight.UIDisplayConfig customUIDisplayCfg = new AndroidFilePickerLight.UIDisplayConfig.getNewInstance();
// ... Later, after setting its properties, set the active file picker's display configuration with: ... 
AndroidFilePickerLight.Builder filePickerBuilder = new AndroidFilePickerLight.Builder.getDefaultInstance();
filePickerBuilder.setDisplayUIConfig().runUserFilePicker();

/* Configure the Strings and text displays with custom messages or text values: */
customUIDisplayCfg.setPickerTitleText("[String] Select a text file ...");
customUIDisplayCfg.setNavigationBarPrefixText("[String] Navigation Options: ");
customUIDisplayCfg.setDoneActionButtonText("[String] !! Done with selection");
customUIDisplayCfg.setCancelActionButtonText("[String] X Cancel -- Pick nothing");

/* Select a base built-in color theme to start with: */
customUIDisplayCfg.setDefaultTheme(AndroidFilePickerLight.UIDisplayConfig.BaseThemeLight);
customUIDisplayCfg.setDefaultTheme(AndroidFilePickerLight.UIDisplayConfig.BaseThemeDark);
customUIDisplayCfg.setDefaultTheme(AndroidFilePickerLight.UIDisplayConfig.BaseThemeDefault);

/* Define custom colors and (optionally) background gradient types: */
customUIDisplayCfg.setBaseBackgroundColor(int hexColor);
customUIDisplayCfg.setBaseForgroundTextColor(int hexColor);
customUIDisplayCfg.setBaseForgroundAccentColor(int hexColor);
customUIDisplayCfg.useGradientBasedDisplays(boolean enableGradientBGInUIDisplay);

public enum AndroidFilePickerLight.UIDisplayConfig.GradientMethodSpec {
     GRADIENT_METHOD_SWEEP,
     GRADIENT_METHOD_LINEAR,
     GRADIENT_METHOD_RADIAL,
};
public enum AndroidFilePickerLight.UIDisplayConfig.GradientTypeSpec {
     GRADIENT_FILL_TYPE_BL_TR,
     GRADIENT_FILL_TYPE_BOTTOM_TOP,
     GRADIENT_FILL_TYPE_BR_TL,
     GRADIENT_FILL_TYPE_LEFT_RIGHT,
     GRADIENT_FILL_TYPE_RIGHT_LEFT,
     GRADIENT_FILL_TYPE_TL_BR,
     GRADIENT_FILL_TYPE_TOP_BOTTOM,
     GRADIENT_FILL_TYPE_TR_BL,
};
public enum AndroidFilePickerLight.UIDisplayConfig.BorderStyleSpec {
     BORDER_STYLE_SOLID,
     BORDER_STYLE_DASHED,
     BORDER_STYLE_DASHED_LONG,
     BORDER_STYLE_DASHED_SHORT,
     BORDER_STYLE_NONE,
};
public enum AndroidFilePickerLight.UIDisplayConfig.NamedGradientColorThemes {
     NAMED_COLOR_SCHEME_TURQUOISE,
     NAMED_COLOR_SCHEME_YELLOW_TO_BLUE,
     NAMED_COLOR_SCHEME_GREEN_YELLOW_GREEN,
     NAMED_COLOR_SCHEME_METAL_STREAK_BILINEAR,
     NAMED_COLOR_SCHEME_SILVER_BALLS,
     NAMED_COLOR_SCHEME_EVENING_SKYLINE,
     NAMED_COLOR_SCHEME_RAINBOW_STREAK,
};

customUIDisplayCfg.setGradientDisplayProperties(int[] gradientColorsList, GradientMethodSpec gradMethodType, BorderStyleSpec borderStyle, float roundedRectSetting = 0.0f);
customUIDisplayCfg.setGradientDisplayProperties(NamedGradientColorThemes presetColorTheme, GradientMethodSpec gradMethodType, BorderStyleSpec borderStyle, float roundedRectSetting = 0.0f);

/* Set default icons for files/folders and the navigation UI folder button shortcuts for 
 * common standard paths: 
 */
public enum AndroidFilePickerLight.UIDisplayConfig.NamedDisplayIcons {
     DEFAULT_FILE_ICON,
     DEFAULT_HIDDEN_FILE_ICON,
     DEFAULT_FOLDER_ICON,
     NAVBTN_HDSDCARD_ICON,
     NAVBTN_PICTURES_ICON,
     NAVBTN_CAMERA_ICON,
     NAVBTN_SCREENSHOTS_ICON,
     NAVBTN_USER_HOME_ICON,
     NAVBTN_DOWNLOADS_FOLDER_ICON,
     NAVBTN_MEDIA_FOLDER_ICON,
};

customUIDisplayCfg.setDefaultChooserIcon(Drawable replacementIcon, NamedDisplayIcons whichIconToReplace);


```

### Misc other useful utilities and customizations bundled with the main library

### Displaying a visual linear bar style progress bar for slow directory loads

This functionality may be useful at some point for those willing to extend this code with 
custom external file providers, e.g., to read and recurse into directories on Dropbox or GitHub. 
I have a simple visual Toast-like display that can be updated and/or canceled in real time to 
let the user know that the directory is loading and that the client application is just "thinking" 
(as opposed to freezing with a runtime error). The stock progress bar looks something like the following screenshot: 

<img src="https://raw.githubusercontent.com/maxieds/AndroidFilePickerLight/master/Screenshots/SampleApplicationDemo-ProgressBarDisplay.png" width="250" />

To invoke this progress bar display in realtime, consider calling the following code examples:
```java
DisplayUtils.DisplayProgressBar(String waitingOnProcessLabel, int curPos, int totalPos);
DisplayUtils.EnableProgressBarDisplay(true);
// ... Then whenever the long process completes, kill the progress bar update callbacks with: ...
DisplayUtils.EnableProgressBarDisplay(true);
```
In principle, the status bar is useful when the underlying operation takes longer than, say 8-10 seconds to complete. 
This code is modified from a status timer to keep the user informed while scanning for a long duration read of 
NFC tags on Android (see [the MFCToolLibrary](https://github.com/maxieds/MifareClassicToolLibrary) and 
its demo application). The core of the progress bar is 
shown by periodically posting Toast messages with a custom layout ``View``. Please post a new issue message 
if anyone using this library in their own application finds this useful, or amusing too.

#### Overriding the default file and directory sorting

An example of how to do this is already seen by glancing through the sources. 
In particular, we define an extendable base class as follows: 
```java
    public static class FileItemsListSortFunc {
        public static List<FileType> sortFileItemsList(List<FileType> fileItemsList) {
            // default is standard lexicographical ordering (override in base classes for customized sorting):
            Collections.sort(fileItemsList, (fi1, fi2) -> { return fi1.getAbsolutePath().compareTo(fi2.getAbsolutePath()); });
            return fileItemsList;
        }
    }
```
Subclasses that override the method above are free to sort the file 
contents in the order that they are best displayed in the client application. 
Some examples would be to override the default of placing directories at the top of the 
files list, to perform a case-insensitive lexicographical sort (like happens on many Linux), 
or otherwise to prioritize the file rankings where the most importantly valued presentations 
are shown first. 

Note that in the source the custom objects to filter (match, then include/exclude) the full 
directory listings, and then sort those files that remain are applied together. See, for example, 
the next code:
```java
    public static List<FileTypes.FileType> filterAndSortFileItemsList(List<FileTypes.FileType> inputFileItems,
                                                                      FileFilter.FileFilterInterface fileFilter, FileTypes.FileItemsListSortFunc sortCompFunc) {
        List<FileTypes.FileType> allowedFileItemsList = new ArrayList<FileTypes.FileType>();
        for(FileTypes.FileType fileItem : inputFileItems) {
            if(fileFilter == null) {
                allowedFileItemsList = inputFileItems;
                break;
            }
            if(fileFilter.fileMatchesFilter(fileItem)) {
                allowedFileItemsList.add(fileItem);
            }
        }
        if(sortCompFunc != null) {
            return sortCompFunc.sortFileItemsList(allowedFileItemsList);
        }
        else {
            return allowedFileItemsList;
        }
    }
```

#### Extending the inclusion/exclusion mechanism of files by type

The specification (by Java interface and a few utility derived instances) are found in the 
library source file ``FileFilter.java``. The bulk of the interface is reproduced as 
follows:
```java
    public interface FileFilterInterface {

        static final boolean INCLUDE_FILES_IN_FILTER_PATTERN = FilePickerBuilder.INCLUDE_FILES_IN_FILTER_PATTERN;
        static final boolean EXCLUDE_FILES_IN_FILTER_PATTERN = FilePickerBuilder.EXCLUDE_FILES_IN_FILTER_PATTERN;

        void    setIncludeExcludeMatchesOption(boolean includeExcludeParam);
        boolean includeFileInSearchResults(FileTypes.FileType fileItem);
        boolean fileMatchesFilter(FileTypes.FileType fileItem);

    }
```
