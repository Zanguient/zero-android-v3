package com.ekylibre.android.utils;

import android.content.Context;
import android.content.res.Resources;

import com.ekylibre.android.database.AppDatabase;
import com.ekylibre.android.database.models.EquipmentType;
import com.ekylibre.android.database.models.Storage;
import com.ekylibre.android.type.EquipmentTypeEnum;
import com.ekylibre.android.type.InterventionOutputTypeEnum;
import com.ekylibre.android.type.SpecieEnum;
import com.ekylibre.android.type.StorageTypeEnum;

import org.apache.commons.text.WordUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class Enums {

    private static List<EquipmentTypeEnum> EQUIMPMENT_ENUMS = new ArrayList<>(Arrays.asList(EquipmentTypeEnum.values()));
    public static List<String> EQUIMPMENT_TYPES = new ArrayList<>();
    public static List<String> EQUIMPMENT_NAMES = new ArrayList<>();

    public static List<InterventionOutputTypeEnum> OUTPUT_ENUMS = new ArrayList<>(Arrays.asList(InterventionOutputTypeEnum.values()));
    public static List<String> OUTPUT_TYPES = new ArrayList<>();
    public static List<String> OUTPUT_NAMES = new ArrayList<>();

    private static List<SpecieEnum> SPECIE_ENUMS = new ArrayList<>(Arrays.asList(SpecieEnum.values()));
    public static List<String> SPECIE_TYPES = new ArrayList<>();
    public static List<String> SPECIE_NAMES = new ArrayList<>();

    private static List<StorageTypeEnum> STORAGE_TYPE_ENUMS = new ArrayList<>(Arrays.asList(StorageTypeEnum.values()));
    public static List<String> STORAGE_TYPE_VALUES = new ArrayList<>();
    public static List<String> STORAGE_TYPE_NAMES = new ArrayList<>();

    // Custom lists
    public static List<Storage> STORAGE_LIST = new ArrayList<>();
    public static List<String> STORAGE_LIST_NAMES = new ArrayList<>();
    public static HashMap<String, EquipmentType> INDICATORS_MAP = new HashMap<>();

    public static void generateStorages(AppDatabase database) {

        Enums.STORAGE_LIST.clear();
        Enums.STORAGE_LIST.addAll(database.dao().getStorages());

        STORAGE_LIST_NAMES.clear();
        STORAGE_LIST_NAMES.add("---");
        // STORAGE_LIST_NAMES.add(String.format("%s (%s)",storage.name, translate(context, storage.type)));
        for (Storage storage : STORAGE_LIST)
            STORAGE_LIST_NAMES.add(storage.name);
    }

    public static int getIndex(int id) {
        for (Storage storage : STORAGE_LIST)
            if (storage.id == id)
                return STORAGE_LIST.indexOf(storage);
        return 0;
    }

    public static void buildEnumsTranslation(Context context) {

        if (EQUIMPMENT_NAMES.isEmpty()) {
            Map<String, String> equimpment_map = new HashMap<>();
            Map<String, String> sorted_equimpment_map;
            EQUIMPMENT_ENUMS.remove(EquipmentTypeEnum.$UNKNOWN);
            for (EquipmentTypeEnum item : EQUIMPMENT_ENUMS) {
                equimpment_map.put(item.rawValue(), translate(context, item.rawValue()));
            }
            sorted_equimpment_map = sortMapByValues(equimpment_map);
            for (Map.Entry<String, String> entry : sorted_equimpment_map.entrySet()) {
                EQUIMPMENT_TYPES.add(entry.getKey());
                EQUIMPMENT_NAMES.add(entry.getValue());
            }
        }

        if (OUTPUT_NAMES.isEmpty()) {
            OUTPUT_ENUMS.remove(InterventionOutputTypeEnum.$UNKNOWN);
            for (InterventionOutputTypeEnum item : OUTPUT_ENUMS) {
                OUTPUT_TYPES.add(item.rawValue());
                OUTPUT_NAMES.add(translate(context, item.rawValue()));
            }
        }

        if (SPECIE_NAMES.isEmpty()) {
            Map<String, String> specie_map = new HashMap<>();
            Map<String, String> sorted_specie_map;
            SPECIE_ENUMS.remove(SpecieEnum.$UNKNOWN);
            for (SpecieEnum item : SPECIE_ENUMS) {
                specie_map.put(item.rawValue(), translate(context, item.rawValue()));
            }
            sorted_specie_map = sortMapByValues(specie_map);
            for (Map.Entry<String, String> entry : sorted_specie_map.entrySet()) {
                SPECIE_TYPES.add(entry.getKey());
                SPECIE_NAMES.add(entry.getValue());
            }
        }

        if (STORAGE_TYPE_NAMES.isEmpty()) {
            STORAGE_TYPE_ENUMS.remove(StorageTypeEnum.$UNKNOWN);
            for (StorageTypeEnum item : STORAGE_TYPE_ENUMS) {
                STORAGE_TYPE_VALUES.add(item.rawValue());
                STORAGE_TYPE_NAMES.add(WordUtils.capitalize(translate(context, item.rawValue())));
            }
        }

        if (INDICATORS_MAP.isEmpty()) {
            // Following request authorized in main thread
            AppDatabase database = AppDatabase.getInstance(context);
            List<EquipmentType> equipmentTypes = database.dao().getEquipmentIndicators();
            for (EquipmentType et : equipmentTypes)
                INDICATORS_MAP.put(et.type, et);
        }

//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                equipmentTypes = database.dao().getEquipmentIndicators();
//            }
//        };
//        thread.start();
        // Generates Map with translations for Equipment indicators
    }

    private static String translate(Context ctx, String rawValue) {
        String translation;
        try {
            translation = ctx.getString(ctx.getResources().getIdentifier(rawValue, "string", ctx.getPackageName()));
        } catch (Resources.NotFoundException e) {
            translation = rawValue;
        }
        return translation;
    }

    private static Map sortMapByValues(Map<String, String> aMap) {

        Set<Map.Entry<String,String>> mapEntries = aMap.entrySet();

        // used linked list to sort, because insertion of elements in linked list is faster than an array list.
        List<Map.Entry<String,String>> aList = new LinkedList<>(mapEntries);

        // sorting the List
        Collections.sort(aList, (ele1, ele2) -> {
            Collator localeCollator = Collator.getInstance(Locale.FRANCE);
            return localeCollator.compare(ele1.getValue(), ele2.getValue());
        });

        // Storing the list into Linked HashMap to preserve the order of insertion.
        Map<String,String> aMap2 = new LinkedHashMap<>();
        for(Map.Entry<String,String> entry: aList) {
            aMap2.put(entry.getKey(), entry.getValue());
        }

        return aMap2;
    }
}
