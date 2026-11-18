package com.wdcftgg.farmersdelightlegacy.common.registry;

import net.minecraftforge.common.config.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum BackportContent {
    NETHERITE("enableNetheriteKnife", "下界合金刀", "netherite_knife"),
    BAMBOO("enableBambooContent", "竹橱柜和竹篮", "bamboo_cabinet", "bamboo_basket"),
    CHERRY("enableCherryCabinet", "樱花木橱柜", "cherry_cabinet"),
    MANGROVE("enableMangroveCabinet", "红树木橱柜", "mangrove_cabinet"),
    NETHER_FUNGI("enableNetherFungusContent", "绯红木橱柜、诡异木橱柜和下界沙拉",
            "crimson_cabinet", "warped_cabinet", "nether_salad"),
    SWEET_BERRIES("enableSweetBerryContent", "甜浆果曲奇、甜浆果芝士派及切片、香烤鲑鱼",
            "sweet_berry_cookie", "sweet_berry_cheesecake", "sweet_berry_cheesecake_slice", "grilled_salmon"),
    GLOW_BERRIES("enableGlowBerryContent", "发光浆果蛋奶沙司和闪亮沙拉",
            "glow_berry_custard", "gleaming_salad", "gleaming_salad_block"),
    HONEY("enableHoneyContent", "蜂蜜曲奇、蜜汁火腿和闪亮沙拉",
            "honey_cookie", "honey_glazed_ham", "honey_glazed_ham_block", "gleaming_salad", "gleaming_salad_block"),
    KELP("enableKelpContent", "海带寿司卷、海带寿司、寿司拼盘和面条汤",
            "kelp_roll", "kelp_roll_slice", "rice_roll_medley_block", "noodle_soup"),
    MIXED_BERRIES("enableMixedBerryMeals", "使用通用浆果矿辞的水果沙拉、填馅南瓜和蜜汁火腿",
            "fruit_salad", "stuffed_pumpkin", "stuffed_pumpkin_block", "honey_glazed_ham", "honey_glazed_ham_block");

    public static final String CATEGORY = "backport_content";
    private static boolean registrationSettingsLocked;

    private final String configKey;
    private final String description;
    private final Set<String> itemPaths;
    private boolean enabled = true;

    BackportContent(String configKey, String description, String... itemPaths) {
        this.configKey = configKey;
        this.description = description;
        this.itemPaths = new HashSet<>(Arrays.asList(itemPaths));
    }

    public static void load(Configuration config) {
        boolean enableAll = config.getBoolean("enableBackportContent", CATEGORY, true,
                "高版本材料相关内容的总开关。关闭后不注册相关物品和方块；重启游戏生效。");
        for (BackportContent content : values()) {
            boolean enabled = config.getBoolean(content.configKey, CATEGORY, true,
                    "启用" + content.description + "。同时受总开关和涉及的其他材料开关控制；重启游戏生效。");
            if (!registrationSettingsLocked) {
                content.enabled = enableAll && enabled;
            }
        }
        config.getCategory(CATEGORY).setRequiresMcRestart(true);
    }

    public static boolean isEnabled(String itemPath) {
        for (BackportContent content : values()) {
            if (!content.enabled && content.itemPaths.contains(itemPath)) {
                return false;
            }
        }
        return true;
    }

    public static void lockRegistrationSettings() {
        registrationSettingsLocked = true;
    }
}
