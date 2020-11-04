/*
        This program (the AndroidFilePickerLight library) is free software written by
        Maxie Dion Schmidt: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        The complete license provided with source distributions of this library is
        available at the following link:
        https://github.com/maxieds/AndroidFilePickerLight
*/

package com.maxieds.androidfilepickerlightlibrary;

import android.app.Application;
import android.content.Context;

public class FileChooserApplication extends Application {

    private static String LOGTAG = FileChooserApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        appContextStaticInst = this; //getApplicationContext();
        super.onCreate();
    }

    private static Context appContextStaticInst = null;
    public static Context getContext() { return appContextStaticInst; }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

}