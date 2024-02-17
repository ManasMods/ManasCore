package com.github.manasmods.testmod.config;

import com.github.manasmods.manascore.api.config.Excluded;

public class TestConfig {
    @Excluded
    public boolean testIgnoredBoolean = true;
    public boolean testBoolean = true;
    public int testInt = 1;
    public double testDouble = 1.0;
    public String testString = "test";
    public String[] testStringArray = new String[]{"test1", "test2"};
    public TestEnum testEnum = TestEnum.TEST1;
    public TestEnum testEnum2 = TestEnum.TEST2;
    public TestEnum testEnum3 = TestEnum.TEST3;



    public enum TestEnum {
        TEST1,
        TEST2,
        TEST3
    }
}
