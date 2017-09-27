package top.lizhengxian.butterfork;

import android.app.Activity;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhengxianlzx on 17-9-24.
 */

public class ButterFork {
    private static Map<Class,Method> classMethodMap = new HashMap<>();
    public static void bind(Activity target){
        if (target != null){
            Method method = classMethodMap.get(target.getClass());
            try {
                if (method == null) {
                    String bindClassName = target.getClass().getPackage().getName() + ".Bind" + target.getClass().getSimpleName();
                    Class bindClass = Class.forName(bindClassName);
                    method = bindClass.getMethod("bind", target.getClass());
                    classMethodMap.put(target.getClass(), method);
                }
                method.invoke(null, target);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
