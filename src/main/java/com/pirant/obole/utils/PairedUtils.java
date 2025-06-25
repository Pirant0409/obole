package com.pirant.obole.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pirant.obole.models.Device;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PairedUtils {
    private static final String KEY_FOLDER = System.getProperty("user.home") + File.separator + ".obole";
    private static final String JSON_FILE = KEY_FOLDER + File.separator + "paired.json";
    private static final Gson gson = new Gson();

    public static List<Device> getPairedDevices() throws FileNotFoundException {
        if (Files.exists(Paths.get(JSON_FILE))) {
            try (Reader reader = new FileReader(JSON_FILE)) {
                Type listType = new TypeToken<List<Device>>() {}.getType();
                return gson.fromJson(reader,listType);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }
    public static boolean isDeviceKnown(String fp) throws FileNotFoundException {
        List<Device> allDevices = getPairedDevices();
        System.out.println("device :" + allDevices);
        System.out.println("fingerprint :" + fp);
        Device existingDevice = allDevices.stream().filter(d->d.getFingerPrint().equals(fp)).findFirst().orElse(null);
        return existingDevice != null;
    }

    public static void saveDevice(Device pd) throws IOException {
        List<Device> allDevices = getPairedDevices();
        allDevices.add(pd);

        try(Writer writer = new FileWriter(JSON_FILE)) {
            gson.toJson(allDevices,writer);
        }
    }

    public static Device getDeviceFromFP(String fp) throws FileNotFoundException {
        List<Device> allDevices = getPairedDevices();
        return allDevices.stream().filter(d -> d.getFingerPrint().equals(fp)).findFirst().orElse(null);
    }
}
