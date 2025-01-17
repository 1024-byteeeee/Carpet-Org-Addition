package org.carpet_org_addition.translate;

import carpet.CarpetSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.carpet_org_addition.exception.UnableToTranslateException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Translate {
    private static final HashMap<String, Map<String, String>> TRANSLATE = new HashMap<>();

    // 获取翻译
    public static Map<String, String> getTranslate() {
        // 每种语言只从文件读取一次
        if (TRANSLATE.containsKey(CarpetSettings.language)) {
            return TRANSLATE.get(CarpetSettings.language);
        }
        String translateJson;
        ClassLoader classLoader = Translate.class.getClassLoader();
        try {
            // 从文件读取翻译
            String path = "assets/carpet-org-addition/lang/%s.json".formatted(CarpetSettings.language);
            InputStream resourceAsStream = classLoader.getResourceAsStream(path);
            // 如果指定语言不存在，返回英文语言
            if (resourceAsStream == null) {
                if (TRANSLATE.containsKey("en_us")) {
                    Map<String, String> enUs = TRANSLATE.get("en_us");
                    TRANSLATE.put(CarpetSettings.language, enUs);
                    return enUs;
                }
                resourceAsStream = classLoader.getResourceAsStream("assets/carpet-org-addition/lang/en_us.json");
            }
            translateJson = IOUtils.toString(Objects.requireNonNull(resourceAsStream), StandardCharsets.UTF_8);
            resourceAsStream.close();
        } catch (NullPointerException | IOException e) {
            // 不应试图捕获这个异常，游戏只会在开始时获取翻译文件，当执行出现错误时，只需要直接结束游戏运行
            throw new UnableToTranslateException("未能成功读取翻译文件", e);
        }
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        Map<String, String> translate = gson.fromJson(translateJson, new TypeToken<Map<String, String>>() {
        }.getType());
        TRANSLATE.put(CarpetSettings.language, translate);
        return translate;
    }
}
