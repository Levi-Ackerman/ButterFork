package top.lizhengxian.mybutterknife;

import android.app.Activity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhengxianlzx on 17-9-24.
 */

public class MyButterKnife {
    private static Map<Class,Method> classMethodMap = new HashMap<>();
    public static void bindView(Activity target){
        if (target != null){
            Method method = classMethodMap.get(target.getClass());
            try {
                if (method == null) {
                    String bindClassName = target.getPackageName() + ".Bind" + target.getClass().getSimpleName();
                    Class bindClass = Class.forName(bindClassName);
                    method = bindClass.getMethod("bindView", target.getClass());
                    classMethodMap.put(target.getClass(), method);
                }
                method.invoke(null, target);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
