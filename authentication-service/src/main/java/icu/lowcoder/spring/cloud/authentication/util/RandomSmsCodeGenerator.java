package icu.lowcoder.spring.cloud.authentication.util;

import org.apache.commons.lang.RandomStringUtils;

public class RandomSmsCodeGenerator {
    private int length;

    public RandomSmsCodeGenerator() {
        this.length = 6;
    }
    public RandomSmsCodeGenerator(int length) {
        this.length = length;
    }

    public String generate() {
        return RandomStringUtils.randomNumeric(this.length);
    }
}
