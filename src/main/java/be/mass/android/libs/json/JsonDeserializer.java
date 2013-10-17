package be.mass.android.libs.json;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonDeserializer {
    private static String TAG = "JSONHelper";

    /**
     * 反序列化简单对象
     *
     * @param jo
     * @param clazz
     * @return
     */
    public <T> T parseObject(JSONObject jo, Class<T> clazz) {
        if (clazz == null || JsonTypeVerify.isNull(jo)) {
            return null;
        }

        T obj = newInstance(clazz);
        if (obj == null) {
            return null;
        }
        if (JsonTypeVerify.isMap(clazz)) {
            setField(obj, jo);
        } else {
            // 取出bean里的所有方法
            Method[] methods = clazz.getDeclaredMethods();
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                String setMetodName = ReflectionUtils.parseMethodName(f.getName(), "set");
                if (!ReflectionUtils.haveMethod(methods, setMetodName)) {
                    continue;
                }
                try {
                    Method fieldMethod = clazz.getMethod(setMetodName,
                            f.getType());
                    setField(obj, fieldMethod, f, jo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }

    /**
     * 反序列化简单对象
     *
     * @param jsonString
     * @param clazz
     * @return
     */
    public <T> T parseObject(String jsonString, Class<T> clazz) {
        if (clazz == null || jsonString == null || jsonString.length() == 0) {
            return null;
        }

        JSONObject jo = null;
        try {
            jo = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (JsonTypeVerify.isNull(jo)) {
            return null;
        }

        return parseObject(jo, clazz);
    }

    /**
     * 反序列化数组对象
     *
     * @param ja
     * @param clazz
     * @return
     */
    public <T> T[] parseArray(JSONArray ja, Class<T> clazz) {
        if (clazz == null || JsonTypeVerify.isNull(ja)) {
            return null;
        }

        int len = ja.length();

        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(clazz, len);

        for (int i = 0; i < len; ++i) {
            try {
                JSONObject jo = ja.getJSONObject(i);
                T o = parseObject(jo, clazz);
                array[i] = o;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return array;
    }

    /**
     * 反序列化数组对象
     *
     * @param jsonString
     * @param clazz
     * @return
     */
    public <T> T[] parseArray(String jsonString, Class<T> clazz) {
        if (clazz == null || jsonString == null || jsonString.length() == 0) {
            return null;
        }
        JSONArray jo = null;
        try {
            jo = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (JsonTypeVerify.isNull(jo)) {
            return null;
        }

        return parseArray(jo, clazz);
    }

    /**
     * 反序列化泛型集合
     *
     * @param ja
     * @param collectionClazz
     * @param genericType
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> Collection<T> parseCollection(JSONArray ja,
                                             Class<?> collectionClazz, Class<T> genericType) {

        if (collectionClazz == null || genericType == null || JsonTypeVerify.isNull(ja)) {
            return null;
        }

        Collection<T> collection = (Collection<T>) newInstance(collectionClazz);

        for (int i = 0; i < ja.length(); ++i) {
            try {
                JSONObject jo = ja.getJSONObject(i);
                T o = parseObject(jo, genericType);
                collection.add(o);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return collection;
    }

    /**
     * 反序列化泛型集合
     *
     * @param jsonString
     * @param collectionClazz
     * @param genericType
     * @return
     */
    public <T> Collection<T> parseCollection(String jsonString,
                                             Class<?> collectionClazz, Class<T> genericType) {
        if (collectionClazz == null || genericType == null
                || jsonString == null || jsonString.length() == 0) {
            return null;
        }
        JSONArray jo = null;
        try {
            jo = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (JsonTypeVerify.isNull(jo)) {
            return null;
        }

        return parseCollection(jo, collectionClazz, genericType);
    }

    /**
     * 根据类型创建对象
     *
     * @param clazz
     * @return
     */
    private <T> T newInstance(Class<T> clazz) {
        if (clazz == null)
            return null;
        T obj = null;
        try {
            obj = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * 设定Map的值
     *
     * @param obj
     * @param jo
     */
    private void setField(Object obj, JSONObject jo) {
        try {
            @SuppressWarnings("unchecked")
            Iterator<String> keyIter = jo.keys();
            String key;
            Object value;
            @SuppressWarnings("unchecked")
            Map<String, Object> valueMap = (Map<String, Object>) obj;
            while (keyIter.hasNext()) {
                key = (String) keyIter.next();
                value = jo.get(key);
                valueMap.put(key, value);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设定字段的值
     *
     * @param obj
     * @param fieldSetMethod
     * @param f
     * @param jo
     */
    private void setField(Object obj, Method fieldSetMethod, Field f,
                          JSONObject jo) {
        String name = ReflectionUtils.parseMethodName(f.getName(), "");
        Class<?> clazz = f.getType();
        try {
            if (JsonTypeVerify.isArray(clazz)) { // 数组
                Class<?> c = clazz.getComponentType();
                JSONArray ja = jo.optJSONArray(name);
                if (!JsonTypeVerify.isNull(ja)) {
                    Object array = parseArray(ja, c);
                    setFiedlValue(obj, fieldSetMethod, clazz.getSimpleName(),
                            array);
                }
            } else if (JsonTypeVerify.isCollection(clazz)) { // 泛型集合
                // 获取定义的泛型类型
                Class<?> c = null;
                Type gType = f.getGenericType();
                if (gType instanceof ParameterizedType) {
                    ParameterizedType ptype = (ParameterizedType) gType;
                    Type[] targs = ptype.getActualTypeArguments();
                    if (targs != null && targs.length > 0) {
                        Type t = targs[0];
                        c = (Class<?>) t;
                    }
                }

                JSONArray ja = jo.optJSONArray(name);
                if (!JsonTypeVerify.isNull(ja)) {
                    Object o = parseCollection(ja, clazz, c);
                    setFiedlValue(obj, fieldSetMethod, clazz.getSimpleName(), o);
                }
            } else if (JsonTypeVerify.isSingle(clazz)) { // 值类型
                Object o = jo.opt(name);
                if (o != null) {
                    setFiedlValue(obj, fieldSetMethod, clazz.getSimpleName(), o);
                }
            } else if (JsonTypeVerify.isObject(clazz)) { // 对象
                JSONObject j = jo.optJSONObject(name);
                if (!JsonTypeVerify.isNull(j)) {
                    Object o = parseObject(j, clazz);
                    setFiedlValue(obj, fieldSetMethod, clazz.getSimpleName(), o);
                }
            } else if (JsonTypeVerify.isList(clazz)) { // 列表
                // JSONObject j = jo.optJSONObject(name);
                // if (!isNull(j)) {
                // Object o = parseObject(j, clazz);
                // f.set(obj, o);
                // }
            } else {
                throw new Exception("unknow type!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 602. * 设定字段的值
     *
     * @param obj
     * @param f
     * @param jo
     */
    private void setField(Object obj, Field f, JSONObject jo) {
        String name = f.getName();
        Class<?> clazz = f.getType();
        try {
            if (JsonTypeVerify.isArray(clazz)) { // 数组
                Class<?> c = clazz.getComponentType();
                JSONArray ja = jo.optJSONArray(name);
                if (!JsonTypeVerify.isNull(ja)) {
                    Object array = parseArray(ja, c);
                    f.set(obj, array);
                }
            } else if (JsonTypeVerify.isCollection(clazz)) { // 泛型集合
                // 获取定义的泛型类型
                Class<?> c = null;
                Type gType = f.getGenericType();
                if (gType instanceof ParameterizedType) {
                    ParameterizedType ptype = (ParameterizedType) gType;
                    Type[] targs = ptype.getActualTypeArguments();
                    if (targs != null && targs.length > 0) {
                        Type t = targs[0];
                        c = (Class<?>) t;
                    }
                }

                JSONArray ja = jo.optJSONArray(name);
                if (!JsonTypeVerify.isNull(ja)) {
                    Object o = parseCollection(ja, clazz, c);
                    f.set(obj, o);
                }
            } else if (JsonTypeVerify.isSingle(clazz)) { // 值类型
                Object o = jo.opt(name);
                if (o != null) {
                    f.set(obj, o);
                }
            } else if (JsonTypeVerify.isObject(clazz)) { // 对象
                JSONObject j = jo.optJSONObject(name);
                if (!JsonTypeVerify.isNull(j)) {
                    Object o = parseObject(j, clazz);
                    f.set(obj, o);
                }
            } else if (JsonTypeVerify.isList(clazz)) { // 列表
                JSONObject j = jo.optJSONObject(name);
                if (!JsonTypeVerify.isNull(j)) {
                    Object o = parseObject(j, clazz);
                    f.set(obj, o);
                }
            } else {
                throw new Exception("unknow type!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 给对象的字段赋值
     *
     * @param obj
     * @param fieldSetMethod
     * @param fieldType
     * @param value
     */
    public void setFiedlValue(Object obj, Method fieldSetMethod,
                              String fieldType, Object value) {
        try {
            if (null != value && !"".equals(value)) {
                if ("String".equals(fieldType)) {
                    fieldSetMethod.invoke(obj, value.toString());
                } else if ("Date".equals(fieldType)) {
                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                    Date temp;
                    if (value.toString().contains("/Date(")) {
                        temp = new Date(Long.valueOf(value.toString().replace("/Date(", "").replace(")/", "")));
                    } else {
                        temp = sdf.parse(value.toString());
                    }
                    fieldSetMethod.invoke(obj, temp);
                } else if ("Integer".equals(fieldType)
                        || "int".equals(fieldType)) {
                    Integer intval = Integer.parseInt(value.toString());
                    fieldSetMethod.invoke(obj, intval);
                } else if ("Long".equalsIgnoreCase(fieldType)) {
                    Long temp = Long.parseLong(value.toString());
                    fieldSetMethod.invoke(obj, temp);
                } else if ("Double".equalsIgnoreCase(fieldType)) {
                    Double temp = Double.parseDouble(value.toString());
                    fieldSetMethod.invoke(obj, temp);
                } else if ("Boolean".equalsIgnoreCase(fieldType)) {
                    Boolean temp = Boolean.parseBoolean(value.toString());
                    fieldSetMethod.invoke(obj, temp);
                } else {
                    fieldSetMethod.invoke(obj, value);
                    Log.e(TAG, TAG + ">>>>setFiedlValue -> not supper type"
                            + fieldType);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, TAG + ">>>>>>>>>>set value error.", e);
        }
    }
}
