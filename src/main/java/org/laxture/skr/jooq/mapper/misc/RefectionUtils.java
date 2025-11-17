package org.laxture.skr.jooq.mapper.misc;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.laxture.skr.jooq.mapper.annotation.MappingInstantiator;
import org.springframework.util.Assert;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class RefectionUtils {

    private RefectionUtils() {}

    //*************************************************************************
    // Type Utils
    //*************************************************************************

    @SuppressWarnings("unchecked")
    public static <T> Class<T> toClass(Type target) {
        if (target instanceof Class) {
            return (Class<T>) target;
        } else if (target instanceof ParameterizedType) {
            return toClass(((ParameterizedType) target).getRawType());
        } else if (target instanceof TypeVariable) {
            return toClass(((TypeVariable) target).getBounds()[0]);
        } else if (target instanceof WildcardType) {
            return toClass(((WildcardType)target).getUpperBounds()[0]);
        } else if (target instanceof GenericArrayType) {
            return (Class<T>) Array.newInstance(toClass(((GenericArrayType) target).getGenericComponentType()), 0).getClass();
        }
        throw new UnsupportedOperationException("Cannot extract class from type " + target + " " + target.getClass());
    }

    public static ClassLoader getClassLoader(Type target, ClassLoader defaultClassLoader) {
        if (target == null) return defaultClassLoader;
        Class<?> clazz = toClass(target);
        if (clazz == null) return defaultClassLoader;
        return clazz.getClassLoader();
    }

    public static <T> Map<TypeVariable<?>, Type> getTypesMap(Type targetType) {
        Class<T> targetClass = RefectionUtils.toClass(targetType);
        Map<TypeVariable<?>, Type> genericTypes = Collections.emptyMap();
        if (targetType instanceof ParameterizedType) {
            TypeVariable<Class<T>>[] typeParameters = targetClass.getTypeParameters();
            Type[] actualTypeArguments = ((ParameterizedType) targetType).getActualTypeArguments();

            genericTypes = new HashMap<TypeVariable<?>, Type>();
            for (int i = 0; i < typeParameters.length; i++) {
                TypeVariable<?> typeParameter = typeParameters[i];
                Type typeArgument = actualTypeArguments[i];
                genericTypes.put(typeParameter, typeArgument);
            }
        }

        return genericTypes;
    }

    public static boolean isPrimitive(Object val) {
        return isPrimitive(val.getClass());
    }

    public static boolean isPrimitive(Type type) {
        return wrappers.containsKey(type)
            || wrappers.containsValue(type);
    }

    public static boolean isString(Object val) {
        return String.class.isAssignableFrom(val.getClass());
    }

    public static Class<?> wrap(Class<?> target) {
        if (target.isPrimitive()) {
            return wrappers.get(target);
        } else {
            return target;
        }
    }

    public static Class unwrap(Class target) {
        return wrappers.entrySet().stream()
            .filter(e -> e.getValue() == target)
            .map(Map.Entry::getKey)
            .findAny()
            .orElse(target);
    }

    public static  boolean areCompatible(Class<?> target, Class<?> source) {
        Class<?> wrapTarget = wrap(target);
        Class<?> wrapSource = wrap(source);
        return wrapTarget.isAssignableFrom(wrapSource);
    }

    public static boolean isNumber(Type target) {
        return Number.class.isAssignableFrom(wrap(RefectionUtils.toClass(target)));
    }

    private final static Map<Class<?>, Class<?>> wrappers = new HashMap<Class<?>, Class<?>>();
    static {
        wrappers.put(boolean.class, Boolean.class);
        wrappers.put(byte.class, Byte.class);
        wrappers.put(short.class, Short.class);
        wrappers.put(char.class, Character.class);
        wrappers.put(int.class, Integer.class);
        wrappers.put(long.class, Long.class);
        wrappers.put(float.class, Float.class);
        wrappers.put(double.class, Double.class);
        wrappers.put(void.class, Void.class);
    }

    public static boolean isArray(Type outType) {
        return RefectionUtils.toClass(outType).isArray();
    }

    public static Type getComponentTypeOfListOrArray(Type outType) {
        Class<?> target = toClass(outType);
        if (target.isArray()) {
            return toClass(outType).getComponentType();
        } else  {
            Type[] parameterTypes = getGenericParameterForClass(outType, Iterable.class);
            if (parameterTypes != null) {
                Type parameterType = parameterTypes[0];
                if (parameterType != null) {
                    return parameterType;
                }
            }
        }
        return Object.class;
    }

    public static MapEntryTypes getKeyValueTypeOfMap(Type outType) {
        Type[] parameterTypes = getGenericParameterForClass(outType, Map.class);
        if (parameterTypes != null) {
            return new MapEntryTypes(parameterTypes[0], parameterTypes[1]);
        }
        return MapEntryTypes.OBJECT_OBJECT;
    }


    private static Type getGenericInterface(Type t, Class<?> i) {
        if (RefectionUtils.areEquals(t, i)) {
            return t;
        }
        Type[] genericInterfaces = RefectionUtils.toClass(t).getGenericInterfaces();
        for(Type it : genericInterfaces) {
            if (isAssignable(i, it)) {
                if (areEquals(it, i)) {
                    return it;
                } else {
                    return getGenericInterface(it, i);
                }
            }
        }
        return null;
    }

    private static Type getGenericSuperType(Type t) {
        return RefectionUtils.toClass(t).getGenericSuperclass();
    }



    public static boolean isAssignable(Type type, Type from) {
        return isAssignable(RefectionUtils.toBoxedClass(type), from);
    }

    public static boolean isAssignable(Class<?> class1, Type from) {
        return class1.isAssignableFrom(toBoxedClass(from));
    }

    public static boolean isJavaLang(Type target) {
        Class<?> clazz = RefectionUtils.toClass(target);
        return clazz.isPrimitive() || (clazz.getPackage() != null && clazz.getPackage().getName().equals("java.lang"));
    }

    public static boolean isInPackage(Type target, Predicate<String> packagePredicate) {
        Class<?> clazz = RefectionUtils.toClass(target);
        Package clazzPackage = clazz.getPackage();
        if (clazzPackage != null) {
            return packagePredicate.test(clazzPackage.getName());
        }
        return false;
    }

    public static boolean isEnum(Type target) {
        Class<?> clazz = RefectionUtils.toClass(target);
        return clazz.isEnum();
    }

    public static Class<?> toBoxedClass(Type type) {
        return RefectionUtils.toBoxedClass(toClass(type));
    }

    public static Class<?> toBoxedClass(Class<?> target) {
        if (target.isPrimitive()) {
            Class<?> clazz = wrappers.get(target);
            if (clazz == null) {
                throw new RuntimeException("Unexpected primitive type " + target);
            }
            return clazz;
        } else {
            return target;
        }
    }

    public static boolean areEquals(Type target, Type clazz) {
        return RefectionUtils.toClass(clazz).equals(RefectionUtils.toClass(target));
    }

    public static Type[] getGenericParameterForClass(Type type, Class<?> interfaceClass) {

        if (isAssignable(interfaceClass, type)) {
            // first look for the interface
            Type genericInterface = getGenericInterface(type, interfaceClass);

            final Type[] types;
            if (genericInterface != null) {
                if (genericInterface instanceof ParameterizedType) {
                    types = ((ParameterizedType) genericInterface).getActualTypeArguments();
                } else {
                    return null;
                }
            } else {
                types = getGenericParameterForClass(getGenericSuperType(type), interfaceClass);
            }
            resolveTypeVariables(type, types);
            return types;
        } else {
            throw new IllegalArgumentException("type " + type + " does not implement/extends " + interfaceClass);
        }
    }


    public static void resolveTypeVariables(Type source, Type[] types) {
        for(int i = 0; i < types.length; i++) {
            Type t = types[i];
            if (t instanceof TypeVariable) {
                types[i] = resolveTypeVariable(source, (TypeVariable) t);
            }
        }
    }

    public static Type resolveTypeVariable(Type type, TypeVariable t) {
        TypeVariable<Class<Object>>[] typeParameters = RefectionUtils.toClass(type).getTypeParameters();

        for(int i = 0; i < typeParameters.length; i++) {
            TypeVariable<Class<Object>> typeVariable = typeParameters[i];
            if (typeVariable.getName().equals(t.getName())) {
                if (type instanceof ParameterizedType) {
                    return ((ParameterizedType) type).getActualTypeArguments()[i];
                } else {
                    return Object.class;
                }
            }
        }
        if (typeParameters.length == 1 && type instanceof ParameterizedType && ((ParameterizedType) type).getActualTypeArguments().length == 1) {
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        }
        return Object.class;
    }

    public static boolean isKotlinClass(Type target) {
        Annotation[] annotations = RefectionUtils.toClass(target).getDeclaredAnnotations();
        if (annotations != null) {
            for(int i = 0; i < annotations.length;i++) {
                Annotation a = annotations[i];
                if (a.annotationType().getName().equals("kotlin.Metadata")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Getter
    public static class MapEntryTypes {
        public static final MapEntryTypes OBJECT_OBJECT = new MapEntryTypes(Object.class, Object.class);
        private final Type keyType;
        private final Type valueType;
        public MapEntryTypes(Type keyType, Type valueType) {
            this.keyType = keyType;
            this.valueType = valueType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MapEntryTypes that = (MapEntryTypes) o;

            if (keyType != null ? !keyType.equals(that.keyType) : that.keyType != null) return false;
            return valueType != null ? valueType.equals(that.valueType) : that.valueType == null;

        }

        @Override
        public int hashCode() {
            int result = keyType != null ? keyType.hashCode() : 0;
            result = 31 * result + (valueType != null ? valueType.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "MapEntryTypes{" +
                "keyType=" + keyType +
                ", valueType=" + valueType +
                '}';
        }
    }

    //*************************************************************************
    // Constructor Utils
    //*************************************************************************

    public static <T> T createInstance(Class<T> clazz) {
        Supplier<T> instructor = null;

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() != 0) continue;
            instructor = () -> {
                constructor.setAccessible(true);
                try {
                    return (T) constructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("Constructor " + constructor + " is not accessible");
                }
            };
            if (constructor.isAnnotationPresent(MappingInstantiator.class)) {
                return instructor.get();
            }
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(MappingInstantiator.class)
                && Modifier.isStatic(method.getModifiers())
                && clazz.isAssignableFrom(method.getReturnType())) {
                method.setAccessible(true);
                try {
                    return (T) method.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("Builder " + method + " is not accessible");
                }
            }
        }

        if (instructor != null) {
            return instructor.get();
        }

        throw new IllegalStateException("No constructor found for " + clazz);
    }

    //*************************************************************************
    // Field Utils
    //*************************************************************************

    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    public static Field findFieldAnnotatedWith(Class<?> clazz, Class<? extends Annotation> annotation) {
        for (Field field : getAllFields(clazz)) {
            if (field.isAnnotationPresent(annotation)) return field;
        }
        return null;
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        for (Field field : getAllFields(clazz)) {
            if (field.getName().equals(fieldName)) return field;
        }
        return null;
    }

    public static RefectionUtils.FieldTuple findMatchModelField(Object modelInstance, String fieldName) {
        return findMatchModelField(modelInstance, fieldName, new ArrayList<Runnable>());
    }

    private static RefectionUtils.FieldTuple findMatchModelField(Object modelInstance, String fieldName,
                                                                 List<Runnable> settleCallback) {
        java.lang.reflect.Field field = RefectionUtils.findField(modelInstance.getClass(), fieldName);
        if (field != null) {
            return new RefectionUtils.FieldTuple(field, modelInstance, settleCallback);
        }

        String[] nameParts = StringUtils.splitByCharacterTypeCamelCase(fieldName);
        int partIndex = 0;
        StringBuilder possibleNestedField = new StringBuilder(nameParts[partIndex]);
        while (!possibleNestedField.toString().equals(fieldName)) {
            field = RefectionUtils.findField(modelInstance.getClass(), possibleNestedField.toString());
            if (field != null && !RefectionUtils.isPrimitive(field.getType())) {
                Object nestedObject = getFieldValue(modelInstance, field);
                if (nestedObject == null) {
                    nestedObject = createInstance(field.getType());
                    Object finalNestedObject = nestedObject;
                    Field finalField = field;
                    settleCallback.add(() -> {
                        setFieldValue(modelInstance, finalField, finalNestedObject);
                    });
                }

                return findMatchModelField(nestedObject,
                    StringUtils.uncapitalize(fieldName.replaceFirst(
                        "^" + possibleNestedField, "")), settleCallback);
            }
            possibleNestedField.append(StringUtils.capitalize(nameParts[++partIndex]));
        }

        return null;
    }

    public static class FieldTuple {
        @Getter
        private final Field field;
        @Getter
        private final Object owner;
        private final List<Runnable> settleCallbacks;

        public FieldTuple(@NonNull Field field, @NonNull Object owner,
                          @NonNull List<Runnable> settleCallbacks) {
            this.field = field;
            this.owner = owner;
            this.settleCallbacks = settleCallbacks;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FieldTuple that = (FieldTuple) o;

            return field == that.field && owner == that.owner;
        }

        public void settle() {
            settleCallbacks.forEach(Runnable::run);
        }
    }

    public static <T> T getFieldValue(@NonNull Object target,
                                      @NonNull Field field) {
        try {
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T getFieldValue(@NonNull Object target,
                                      @NonNull String path) {
        String[] fieldPath = path.split("\\.");
        Object obj = target;
        int i=0;
        while (i<fieldPath.length) {
            if (obj == null) break;
            if ("*".equals(fieldPath[i])) {
                // merge map
                if (obj instanceof Map) {
                    obj = ((Map<?, ?>) obj).values();
                } else if (obj instanceof Collection) {
                    obj = obj;
                } else {
                    // non-support object fields
                    return null;
                }
            } else {
                if (obj instanceof Collection) {
                    List<Object> values = new ArrayList<>();
                    for (Object item : (Collection<?>) obj) {
                        Object value = getFieldValue(item, item.getClass(), fieldPath[i]);
                        values.add(value);
                    }
                    obj = values;
                } else {
                    obj = getFieldValue(obj, obj.getClass(), fieldPath[i]);
                }
            }
            i++;
        }
        return (T) obj;
    }

    public static Class<?> getFieldClass(@NonNull Object target,
                                         @NonNull String fieldName) {
        try {
            return target.getClass().getDeclaredField(fieldName).getType();
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getFieldValue(@NonNull Object target,
                                        @NonNull Class<?> clazz,
                                        @NonNull String fieldName) {
        if (Map.class.isAssignableFrom(clazz)) {
            return ((Map<?, ?>) target).get(fieldName);
        }

        Object val = callMethod(target, "get"+ StringUtils.capitalize(fieldName));
        if (val == null) {
            val = callMethod(target, "is"+ StringUtils.capitalize(fieldName));
        }
        if (val != null) return val;

        try {
            Field field = target instanceof Class
                ? ((Class<?>) target).getDeclaredField(fieldName)
                : clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException nsfe) {
            if (clazz.getSuperclass() != null) {
                return target instanceof Class
                    ? getFieldValue(((Class<?>) target).getSuperclass(), clazz, fieldName)
                    : getFieldValue(target, clazz.getSuperclass(), fieldName);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static void setFieldValue(@NonNull Object target,
                                     @NonNull Field field,
                                     Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Set field "+field.getName()+" failed.", e);
        }
    }

    public static void setFieldValue(@NonNull Object target,
                                     @NonNull String fieldName,
                                     Object value) {
        setFieldValue(target, target.getClass(), fieldName, value);
    }

    private static void setFieldValue(@NonNull Object target,
                                      @NonNull Class clazz,
                                      @NonNull String fieldName,
                                      Object value) {
        try {
            Field field = target instanceof Class
                ? ((Class<?>) target).getDeclaredField(fieldName)
                : clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException nsfe) {
            if (clazz.getSuperclass() != null) {
                setFieldValue(target, clazz.getSuperclass(), fieldName, value);
            } else {
                throw new RuntimeException("Set field "+fieldName+" failed.", nsfe);
            }
        } catch (Exception e) {
            throw new RuntimeException("Set field "+fieldName+" failed.", e);
        }
    }

    /**
     * Copy Object <code>source</code> to Object <code>target</code>. This coping is
     * not recursive.
     * If a source field is {@link Collection}, it's element will be
     * {@link Collection#addAll(Collection)} to corresponding target field, instead of
     * copy the {@link Collection} reference. Hence if target's {@link Collection} is not
     * initialized and remains null, it won't be copied.
     */
    public static <E> void copyFields(@NonNull E source,
                                      @NonNull E target) {
        copyFields(source, target, false, false);
    }

    /**
     * @see #copyFields(Object, Object)
     *
     * @param fields to be excluded for copying
     */
    public static <E> void copyFieldsExcluding(@NonNull E source,
                                               @NonNull E target,
                                               String... fields) {
        copyFields(source, target, false, false, fields);
    }

    /**
     * @see #copyFields(Object, Object)
     *
     * @param fields to be included for copying
     */
    public static <E> void copyFieldsIncluding(@NonNull E source,
                                               @NonNull E target,
                                               String... fields) {
        copyFields(source, target, false, true, fields);
    }

    public static <E> void copyFields(@NonNull E source,
                                      @NonNull E target,
                                      boolean ignoreNullField,
                                      boolean includeOrExclude,
                                      String... fields) {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        if (source == target) return;

        for (Field sourceField : source.getClass().getFields()) {
            try {
                if (Modifier.isStatic(sourceField.getModifiers())
                    || Modifier.isPrivate(sourceField.getModifiers())
                    || Modifier.isProtected(sourceField.getModifiers())
                    || (includeOrExclude && ArrayUtils.indexOf(fields, sourceField.getName()) < 0)
                    || (!includeOrExclude && ArrayUtils.indexOf(fields, sourceField.getName()) >= 0)) continue;

                Field targetField;
                try {
                    targetField = target.getClass().getField(sourceField.getName());
                } catch (NoSuchFieldException e) {
                    continue;
                }

                if (ignoreNullField && targetField.get(source) == null) {
                    continue;
                }

                // deep copy collection
                if (Collection.class.isAssignableFrom(sourceField.getType())) {
                    Collection srcCollection = (Collection) sourceField.get(source);
                    Collection targetCollection;
                    try {
                        targetCollection = (Collection) targetField.get(target);
                    } catch (ClassCastException ex) {
                        continue;
                    }
                    if (srcCollection == null && targetCollection == null) continue;
                    if (srcCollection == null) {
                        targetCollection.clear();
                    } else {
                        if (targetCollection == null) {
                            targetField.set(target, srcCollection);
                        } else {
                            // Overcome immutable collection
                            try {
                                targetCollection.clear();
                                targetCollection.addAll(srcCollection);
                            } catch (UnsupportedOperationException e) {
                                targetField.set(target, srcCollection);
                            }
                        }
                    }

                    // deep copy map
                } else if (Map.class.isAssignableFrom(sourceField.getType())) {
                    Map srcMap = (Map) sourceField.get(source);
                    Map targetMap;
                    try {
                        targetMap = (Map) targetField.get(target);
                    } catch (ClassCastException ex) {
                        continue;
                    }
                    if (srcMap == null && targetMap == null) continue;
                    if (srcMap == null) {
                        targetMap.clear();
                    } else {
                        if (targetMap == null) {
                            targetField.set(target, srcMap);
                        } else {
                            // Overcome immutable collection
                            try {
                                targetMap.clear();
                                targetMap.putAll(srcMap);
                            } catch (UnsupportedOperationException e) {
                                targetField.set(target, srcMap);
                            }
                        }
                    }

                } else {
                    targetField.set(target, sourceField.get(source));
                }
            } catch (Exception e) {
                throw new RuntimeException(
                    "Copy field "+ sourceField.getName()+" failed.", e);
            }
        }
    }

    public static <T extends Serializable> T deepClone(@NonNull T o) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(o);
            out.flush();
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
            return (T) o.getClass().cast(in.readObject());
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy Object "+o.getClass().getName(), e);
        }
    }

    //*************************************************************************
    // Method Utils
    //*************************************************************************

    public static Method getDeclaredMethod(@NonNull Class<?> clazz,
                                           @NonNull String methodName,
                                           Class<?>... parameterTypes) {
        Method method;
        try {
            method = parameterTypes.length > 0
                ? clazz.getDeclaredMethod(methodName, parameterTypes)
                : clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
        if (method != null) method.setAccessible(true);
        return method;
    }

    public static Method getMethod(@NonNull Class<?> clazz,
                                   @NonNull String methodName,
                                   Class<?>... parameterTypes) {
        Method method;
        try {
            method = parameterTypes.length > 0
                ? clazz.getMethod(methodName, parameterTypes)
                : clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
        if (method != null) method.setAccessible(true);
        return method;
    }

    public static <R, O> R callMethod(O object,
                                      @NonNull String methodName,
                                      Object... parameters) {
        Class<O> clazz = (Class<O>) object.getClass();
        return callMethod(clazz, object, methodName, parameters);
    }

    /**
     * This method doesn't always function as expected. Be 100% sure
     * and tested when you use it.
     *
     * As known, this method is not worked for following case:
     * * parameter type is primitive number, e.g. int.class
     * * parameter type is general type, e.g. Object.class
     */
    public static <R> R callMethod(Class<?> clazz,
                                   Object object,
                                   @NonNull String methodName,
                                   Object... parameters) {
        if (object == null) return null;

        Method method;
        // try get method from `getMethod`
        if (parameters == null || parameters.length <= 0) {
            method = getMethod(clazz, methodName);
        } else {
            method = getMethod(clazz, methodName,
                Arrays.stream(parameters).map(Object::getClass).toArray(Class[]::new));
        }

        // try get method from `getDeclaredMethod`
        if (method == null) {
            if (parameters == null || parameters.length <= 0) {
                method = getDeclaredMethod(clazz, methodName);
            } else {
                method = getDeclaredMethod(clazz, methodName,
                    Arrays.stream(parameters).map(Object::getClass).toArray(Class[]::new));
            }
        }

        if (method == null) return null;

        try {
            return (R) method.invoke(object, parameters);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
        return null;
    }

}
