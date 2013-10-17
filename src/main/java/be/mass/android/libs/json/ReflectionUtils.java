package be.mass.android.libs.json;

import java.lang.reflect.Method;

public class ReflectionUtils {
    /**
     * 判断是否存在某属性的 get方法
     *
     * @param methods
     * @param fieldMethod
     * @return boolean
     */
    public static boolean haveMethod(Method[] methods, String fieldMethod) {
        for (Method met : methods) {
            if (fieldMethod.equals(met.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 拼接某属性的 get或者set方法
     *
     * @param fieldName
     * @param methodType
     * @return
     */
    public static String parseMethodName(String fieldName, String methodType) {
        if (null == fieldName || "".equals(fieldName)) {
            return null;
        }
        return methodType + fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);
    }
}
