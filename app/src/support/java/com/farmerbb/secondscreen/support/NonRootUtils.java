/* Copyright 2018 Braden Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.farmerbb.secondscreen.support;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.view.Display;

import java.util.Scanner;

public final class NonRootUtils {

    private NonRootUtils() {}

    public static void runCommands(Context context, String[] commands) {
        for(String command : commands) {
            String[] commandArgs = command.split(" ");

            switch(commandArgs[0]) {
                case "settings":
                    switch(commandArgs[2]) {
                        case "global":
                            Settings.Global.putString(context.getContentResolver(), commandArgs[3], commandArgs[4]);
                            break;
                        case "secure":
                            Settings.Secure.putString(context.getContentResolver(), commandArgs[3], commandArgs[4]);
                            break;
                        case "system":
                            Settings.System.putString(context.getContentResolver(), commandArgs[3], commandArgs[4]);
                            break;
                    }
                    break;
                case "wm":
                    try {
                        switch(commandArgs[1]) {
                            case "size":
                                wmSize(commandArgs[2]);
                                break;
                            case "density":
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                                    wmDensity(commandArgs[2]);
                                else
                                    wmDensityOld(commandArgs[2]);
                                break;
                            case "overscan":
                                wmOverscan(commandArgs[2]);
                                break;
                        }
                    } catch (Exception e) { /* Gracefully fail */ }
                    break;
            }
        }
    }

    @SuppressLint("PrivateApi")
    private static Object getWindowManagerService() throws Exception {
        return Class.forName("android.view.WindowManagerGlobal")
                .getMethod("getWindowManagerService")
                .invoke(null);
    }

    @SuppressLint("PrivateApi")
    private static void wmDensity(String commandArg) throws Exception {
        // From android.os.UserHandle
        final int USER_CURRENT_OR_SELF = -3;

        if(commandArg.equals("reset")) {
            Class.forName("android.view.IWindowManager")
                    .getMethod("clearForcedDisplayDensityForUser", int.class, int.class)
                    .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY, USER_CURRENT_OR_SELF);
        } else {
            int density = Integer.parseInt(commandArg);

            Class.forName("android.view.IWindowManager")
                    .getMethod("setForcedDisplayDensityForUser", int.class, int.class, int.class)
                    .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY, density, USER_CURRENT_OR_SELF);
        }
    }

    @SuppressLint("PrivateApi")
    private static void wmDensityOld(String commandArg) throws Exception {
        if(commandArg.equals("reset")) {
            Class.forName("android.view.IWindowManager")
                    .getMethod("clearForcedDisplayDensity", int.class)
                    .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY);
        } else {
            int density = Integer.parseInt(commandArg);

            Class.forName("android.view.IWindowManager")
                    .getMethod("setForcedDisplayDensity", int.class, int.class)
                    .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY, density);
        }
    }

    @SuppressLint("PrivateApi")
    private static void wmSize(String commandArg) throws Exception {
        if(commandArg.equals("reset")) {
            Class.forName("android.view.IWindowManager")
                    .getMethod("clearForcedDisplaySize", int.class)
                    .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY);
        } else {
            Scanner scanner = new Scanner(commandArg);
            scanner.useDelimiter("x");

            int width = scanner.nextInt();
            int height = scanner.nextInt();

            scanner.close();

            Class.forName("android.view.IWindowManager")
                    .getMethod("setForcedDisplaySize", int.class, int.class, int.class)
                    .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY, width, height);
        }
    }

    @SuppressLint("PrivateApi")
    private static void wmOverscan(String commandArg) throws Exception {
        int left, top, right, bottom;

        if(commandArg.equals("reset"))
            left = top = right = bottom = 0;
        else {
            Scanner scanner = new Scanner(commandArg);
            scanner.useDelimiter(",");

            left = scanner.nextInt();
            top = scanner.nextInt();
            right = scanner.nextInt();
            bottom = scanner.nextInt();

            scanner.close();
        }

        Class.forName("android.view.IWindowManager")
                .getMethod("setOverscan", int.class, int.class, int.class, int.class, int.class)
                .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY, left, top, right, bottom);
    }
}