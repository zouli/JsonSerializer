package be.mass.android.libs.json;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONStringer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonSerializer {
    private static String TAG = "JSONHelper";

    /**
     * 将对象转换成Json字符串
     *
     * @param obj
     * @return
     */
    public String serialize(Object obj) {
        JSONStringer js = new JSONStringer();
        serialize(js, obj);
        Log.d(TAG, "JsonSerializer Serialize  :" + js.toString());
        return js.toString();
    }

    /**
     * 序列化为JSON
     *
     * @param js
     * @param o
     */
    private void serialize(JSONStringer js, Object o) {
        if (JsonTypeVerify.isNull(o)) {
            try {
                js.value(null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        Class<?> clazz = o.getClass();
        if (JsonTypeVerify.isObject(clazz)) { // 对象
            serializeObject(js, o);
        } else if (JsonTypeVerify.isArray(clazz)) { // 数组
            serializeArray(js, o);
        } else if (JsonTypeVerify.isCollection(clazz)) { // 集合
            Collection<?> collection = (Collection<?>) o;
            serializeCollect(js, collection);
        } else { // 单个值
            try {
                js.value(o);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 序列化数组
     *
     * @param js
     * @param array
     */
    private void serializeArray(JSONStringer js, Object array) {
        try {
            js.array();
            for (int i = 0; i < Array.getLength(array); ++i) {
                Object o = Array.get(array, i);
                serialize(js, o);
            }
            js.endArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 序列化集合
     *
     * @param js
     * @param collection
     */
    private void serializeCollect(JSONStringer js,
                                  Collection<?> collection) {
        try {
            js.array();
            for (Object o : collection) {
                serialize(js, o);
            }
            js.endArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 序列化对象
     *
     * @param js
     * @param obj
     */
    private void serializeObject(JSONStringer js, Object obj) {
        try {
            js.object();
            Class<? extends Object> objClazz = obj.getClass();
            Method[] methods = objClazz.getDeclaredMethods();
            Field[] fields = objClazz.getDeclaredFields();
            for (Field field : fields) {
                try {
                    String fieldType = field.getType().getSimpleName();
                    String fieldGetName = ReflectionUtils.parseMethodName(field.getName(),
                            "get");
                    if (!ReflectionUtils.haveMethod(methods, fieldGetName)) {
                        continue;
                    }
                    Method fieldGetMet = objClazz.getMethod(fieldGetName,
                            new Class[]{});
                    Object fieldVal = fieldGetMet.invoke(obj, new Object[]{});
                    String result = null;
                    if ("Date".equals(fieldType)) {
                        SimpleDateFormat sdf = new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss", Locale.US);
                        result = sdf.format((Date) fieldVal);

                    } else {
                        if (null != fieldVal) {
                            result = String.valueOf(fieldVal);
                        }
                    }
                    js.key(field.getName());
                    serialize(js, result);
                } catch (Exception e) {
                    continue;
                }
            }
            js.endObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * set属性的值到Bean
     *
     * @param obj
     * @param valMap
     */
    public void setFieldValue(Object obj, Map<String, String> valMap) {
        Class<?> cls = obj.getClass();
        // 取出bean里的所有方法
        Method[] methods = cls.getDeclaredMethods();
        Field[] fields = cls.getDeclaredFields();

        for (Field field : fields) {
            try {
                String setMetodName = ReflectionUtils.parseMethodName(field.getName(), "set");
                if (!ReflectionUtils.haveMethod(methods, setMetodName)) {
                    continue;
                }
                Method fieldMethod = cls.getMethod(setMetodName,
                        field.getType());
                String value = valMap.get(field.getName());
                if (null != value && !"".equals(value)) {
                    String fieldType = field.getType().getSimpleName();
                    if ("String".equals(fieldType)) {
                        fieldMethod.invoke(obj, value);
                    } else if ("Date".equals(fieldType)) {
                        SimpleDateFormat sdf = new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss", Locale.US);
                        Date temp = sdf.parse(value);
                        fieldMethod.invoke(obj, temp);
                    } else if ("Integer".equals(fieldType)
                            || "int".equals(fieldType)) {
                        Integer intval = Integer.parseInt(value);
                        fieldMethod.invoke(obj, intval);
                    } else if ("Long".equalsIgnoreCase(fieldType)) {
                        Long temp = Long.parseLong(value);
                        fieldMethod.invoke(obj, temp);
                    } else if ("Double".equalsIgnoreCase(fieldType)) {
                        Double temp = Double.parseDouble(value);
                        fieldMethod.invoke(obj, temp);
                    } else if ("Boolean".equalsIgnoreCase(fieldType)) {
                        Boolean temp = Boolean.parseBoolean(value);
                        fieldMethod.invoke(obj, temp);
                    } else {
                        System.out.println("setFieldValue not supper type:"
                                + fieldType);
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
    }

    /**
     * 对象转Map
     *
     * @param obj
     * @return
     */
    public Map<String, String> getFieldValueMap(Object obj) {
        Class<?> cls = obj.getClass();
        Map<String, String> valueMap = new HashMap<String, String>();
        // 取出bean里的所有方法
        Method[] methods = cls.getDeclaredMethods();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            try {
                String fieldType = field.getType().getSimpleName();
                String fieldGetName = ReflectionUtils.parseMethodName(field.getName(), "get");
                if (!ReflectionUtils.haveMethod(methods, fieldGetName)) {
                    continue;
                }
                Method fieldGetMet = cls
                        .getMethod(fieldGetName, new Class[]{});
                Object fieldVal = fieldGetMet.invoke(obj, new Object[]{});
                String result = null;
                if ("Date".equals(fieldType)) {
                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    result = sdf.format((Date) fieldVal);

                } else {
                    if (null != fieldVal) {
                        result = String.valueOf(fieldVal);
                    }
                }
                valueMap.put(field.getName(), result);
            } catch (Exception e) {
                continue;
            }
        }
        return valueMap;
    }
}
