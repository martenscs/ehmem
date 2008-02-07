package com.googlecode.ehmem.performance.object;

import java.util.Date;
import java.util.Random;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class ObjectFactory {
    public static long indexFirst = 1;
    public static long indexSecond = 1;
    public static long indexThird = 1;

    public static Keyed create(String clazz) {
        if (clazz.equals(FirstTestObject.class.getName())) {
            return createFirst();
        } else if (clazz.equals(SecondTestObject.class.getName())) {
            return createSecond();
        } else {
            return createThird();
        }
    }

    public static long getMaxKey(String clazz) {
        if (clazz.equals(FirstTestObject.class.getName())) {
            return indexFirst;
        } else if (clazz.equals(SecondTestObject.class.getName())) {
            return indexSecond;
        } else {
            return indexThird;
        }
    }

    public static FirstTestObject createFirst() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int len = random.nextInt(10) + 10;

        for (int i = 0; i < len; i++) {
            sb.append(Integer.toHexString(random.nextInt()));
        }

        final String key = String.valueOf(indexFirst++);
        final long l = random.nextLong();

        return new FirstTestObject(key, sb.toString(), l, new Date());
    }

    public static SecondTestObject createSecond() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int len = random.nextInt(10) + 10;

        for (int i = 0; i < len; i++) {
            sb.append(Integer.toHexString(random.nextInt()));
        }

        final String key = String.valueOf(indexSecond++);
        final long l = random.nextLong();

        return new SecondTestObject(key, sb.toString(), l, new Date());
    }

    public static ThirdTestObject createThird() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int len = random.nextInt(10) + 10;

        for (int i = 0; i < len; i++) {
            sb.append(Integer.toHexString(random.nextInt()));
        }

        final String key = String.valueOf(indexThird++);
        final long l = random.nextLong();

        return new ThirdTestObject(key, sb.toString(), l, new Date());
    }
}
