package com.lody.welike.ui;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.lody.welike.ui.annotation.InitData;
import com.lody.welike.ui.annotation.JoinView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lody
 * @version 1.0
 */
public class DynamicActivityBinder {

    private static Map<String, List<Method>> activityInitDataMethodMap = new ConcurrentHashMap<>();
    private static Map<String, List<Field>> activityFieldMap = new HashMap<>();

    /**
     * 遍历所有包含InitData字段的方法,调用它们
     *
     * @param activity
     */
    public static void invokeInitDataMethod(Activity activity) {
        List<Method> initDataMethod = activityInitDataMethodMap.get(activity.getClass().getName());
        if (initDataMethod == null) {
            initDataMethod = new ArrayList<>(2);
            for (Method method : activity.getClass().getDeclaredMethods()) {
                if (method.getAnnotation(InitData.class) != null) {
                    method.setAccessible(true);
                    initDataMethod.add(method);
                }
            }
            activityInitDataMethodMap.put(activity.getClass().getName(), initDataMethod);
        }

        for (Method method : initDataMethod) {
            try {
                method.invoke(activity);
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * 遍历所有包含JoinView注解的字段,将它们绑定.
     *
     * @param activity
     */
    public static void joinAllView(WelikeActivity activity) {
        List<Field> fields = activityFieldMap.get(activity.getClass().getName());
        if (fields == null) {
            fields = new ArrayList<>(6);
            for (Field field : activity.getClass().getDeclaredFields()) {
                JoinView joinView = field.getAnnotation(JoinView.class);
                if (joinView != null) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
            activityFieldMap.put(activity.getClass().getName(), fields);
        }

        for (Field field : fields) {
            JoinView joinView = field.getAnnotation(JoinView.class);

            int id = joinView.id();
            String name = joinView.name();
            View view = null;
            if (id != 0) {
                try {
                    view = activity.findViewById(id);
                } catch (Throwable ignored) {
                }
            } else if (name.trim().length() != 0) {
                String packageName = activity.getPackageName();
                id = activity.getResources().getIdentifier(name, "id", packageName);
                view = activity.findViewById(id);
            }
            if (view != null) {
                try {
                    field.set(activity, view);
                } catch (Throwable ignored) {
                }
                if (joinView.click() || field.getType().isAssignableFrom(Button.class)) {
                    view.setOnClickListener(activity);
                }
            }
        }
    }
}
