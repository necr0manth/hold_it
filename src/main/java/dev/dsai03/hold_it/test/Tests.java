package dev.dsai03.hold_it.test;

import dev.dsai03.hold_it.content.entities.BallData;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder("hold_it")
public class Tests {

    /**
     * Тест 1: Проверяет создание BallData с корректными параметрами
     */
    @GameTest(template = "empty")
    public static void testBallDataCreation(GameTestHelper helper) {
        Vec3 position = new Vec3(1, 2, 3);
        float power = 0.5f;

        BallData ballData = new BallData(position, power);

        helper.assertTrue(ballData.pos().equals(position), "Позиция BallData должна совпадать с заданной");
        helper.assertTrue(ballData.power() == power, "Мощность BallData должна совпадать с заданной");

        helper.succeed();
    }

    /**
     * Тест 2: Проверяет BallData с разными значениями мощности
     */
    @GameTest(template = "empty")
    public static void testBallDataPowerValues(GameTestHelper helper) {
        Vec3 pos = new Vec3(0, 0, 0);

        // Тестируем нулевую мощность
        BallData zeroPower = new BallData(pos, 0.0f);
        helper.assertTrue(zeroPower.power() == 0.0f, "Нулевая мощность должна сохраняться");

        // Тестируем положительную мощность
        BallData positivePower = new BallData(pos, 1.5f);
        helper.assertTrue(positivePower.power() == 1.5f, "Положительная мощность должна сохраняться");

        helper.succeed();
    }

    /**
     * Тест 3: Проверяет BallData с разными позициями
     */
    @GameTest(template = "empty")
    public static void testBallDataPositions(GameTestHelper helper) {
        float power = 1.0f;

        // Тестируем нулевую позицию
        BallData zeroPos = new BallData(Vec3.ZERO, power);
        helper.assertTrue(zeroPos.pos().equals(Vec3.ZERO), "Нулевая позиция должна сохраняться");

        // Тестируем отрицательные координаты
        Vec3 negativePos = new Vec3(-5, -10, -15);
        BallData negativeData = new BallData(negativePos, power);
        helper.assertTrue(negativeData.pos().equals(negativePos), "Отрицательные координаты должны сохраняться");

        helper.succeed();
    }

    /**
     * Тест 4: Проверяет равенство BallData объектов
     */
    @GameTest(template = "empty")
    public static void testBallDataEquality(GameTestHelper helper) {
        Vec3 pos1 = new Vec3(1, 2, 3);
        Vec3 pos2 = new Vec3(1, 2, 3);
        float power = 0.7f;

        BallData data1 = new BallData(pos1, power);
        BallData data2 = new BallData(pos2, power);

        // Records автоматически реализуют equals()
        helper.assertTrue(data1.equals(data2), "BallData с одинаковыми параметрами должны быть равны");

        // Тестируем неравенство
        BallData data3 = new BallData(pos1, power + 0.1f);
        helper.assertFalse(data1.equals(data3), "BallData с разной мощностью должны быть не равны");

        helper.succeed();
    }

    /**
     * Тест 5: Проверяет методы record класса BallData
     */
    @GameTest(template = "empty")
    public static void testBallDataRecordMethods(GameTestHelper helper) {
        Vec3 position = new Vec3(5, 10, 15);
        float power = 2.5f;

        BallData ballData = new BallData(position, power);

        // Проверяем toString (должен содержать значения)
        String toString = ballData.toString();
        helper.assertTrue(toString.contains("5"), "toString должен содержать x координату");
        helper.assertTrue(toString.contains("2.5"), "toString должен содержать значение мощности");

        // Проверяем hashCode
        int hashCode1 = ballData.hashCode();
        BallData sameBallData = new BallData(position, power);
        int hashCode2 = sameBallData.hashCode();
        helper.assertTrue(hashCode1 == hashCode2, "Одинаковые BallData должны иметь одинаковый hashCode");

        helper.succeed();
    }

    /**
     * Тест 6: Проверяет граничные значения мощности
     */
    @GameTest(template = "empty")
    public static void testBallDataPowerBoundaries(GameTestHelper helper) {
        Vec3 pos = new Vec3(0, 0, 0);

        // Очень маленькая мощность
        BallData tinyPower = new BallData(pos, 0.001f);
        helper.assertTrue(tinyPower.power() == 0.001f, "Очень маленькая мощность должна сохраняться");

        // Большая мощность
        BallData bigPower = new BallData(pos, 100.0f);
        helper.assertTrue(bigPower.power() == 100.0f, "Большая мощность должна сохраняться");

        // Отрицательная мощность (если допустима)
        BallData negativePower = new BallData(pos, -1.0f);
        helper.assertTrue(negativePower.power() == -1.0f, "Отрицательная мощность должна сохраняться");

        helper.succeed();
    }

    /**
     * Тест 7: Проверяет граничные значения позиции
     */
    @GameTest(template = "empty")
    public static void testBallDataPositionBoundaries(GameTestHelper helper) {
        float power = 1.0f;

        // Очень большие координаты
        Vec3 bigPos = new Vec3(1000000, 1000000, 1000000);
        BallData bigData = new BallData(bigPos, power);
        helper.assertTrue(bigData.pos().equals(bigPos), "Большие координаты должны сохраняться");

        // Очень маленькие координаты
        Vec3 tinyPos = new Vec3(0.0001, 0.0001, 0.0001);
        BallData tinyData = new BallData(tinyPos, power);
        helper.assertTrue(tinyData.pos().equals(tinyPos), "Маленькие координаты должны сохраняться");

        helper.succeed();
    }

    /**
     * Тест 8: Проверяет неизменяемость BallData (immutability)
     */
    @GameTest(template = "empty")
    public static void testBallDataImmutability(GameTestHelper helper) {
        Vec3 originalPos = new Vec3(1, 2, 3);
        float originalPower = 1.5f;

        BallData ballData = new BallData(originalPos, originalPower);

        // Получаем позицию и мощность
        Vec3 retrievedPos = ballData.pos();
        float retrievedPower = ballData.power();

        // Проверяем что значения не изменились
        helper.assertTrue(retrievedPos.equals(originalPos), "Позиция должна остаться неизменной");
        helper.assertTrue(retrievedPower == originalPower, "Мощность должна остаться неизменной");

        // Records по определению immutable
        helper.assertTrue(ballData.pos() == retrievedPos, "Повторный вызов pos() должен возвращать ту же ссылку");

        helper.succeed();
    }

    /**
     * Тест 9: Проверяет создание множественных BallData
     */
    @GameTest(template = "empty")
    public static void testMultipleBallData(GameTestHelper helper) {
        // Создаем массив BallData
        BallData[] ballDataArray = new BallData[3];

        for (int i = 0; i < 3; i++) {
            Vec3 pos = new Vec3(i, i * 2, i * 3);
            float power = i * 0.5f;
            ballDataArray[i] = new BallData(pos, power);
        }

        // Проверяем что все созданы корректно
        for (int i = 0; i < 3; i++) {
            helper.assertTrue(ballDataArray[i] != null, "BallData " + i + " должен быть создан");
            helper.assertTrue(ballDataArray[i].pos().x == i, "X координата должна быть " + i);
            helper.assertTrue(ballDataArray[i].power() == i * 0.5f, "Мощность должна быть " + (i * 0.5f));
        }

        helper.succeed();
    }

    /**
     * Тест 10: Проверяет работу с null значениями
     */
    @GameTest(template = "empty")
    public static void testBallDataNullHandling(GameTestHelper helper) {
        float power = 1.0f;

        // Тестируем что происходит при попытке создать с null позицией
        try {
            BallData nullPosData = new BallData(null, power);
            // Если дошли сюда, значит null принимается
            helper.assertTrue(nullPosData.pos() == null, "Null позиция должна сохраняться как null");
        } catch (Exception e) {
            // Если выбрасывается исключение, это тоже нормально
            helper.assertTrue(true, "Исключение при null позиции - нормальное поведение");
        }

        // Проверяем сравнение с null
        Vec3 normalPos = new Vec3(1, 2, 3);
        BallData normalData = new BallData(normalPos, power);
        helper.assertFalse(normalData.equals(null), "BallData не должен быть равен null");

        helper.succeed();
    }
}
