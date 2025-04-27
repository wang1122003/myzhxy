package com.campus.utils;

import com.campus.entity.CourseSelection;
import com.campus.entity.Score;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实体与Map转换工具类
 * 用于替代VO层及处理实体间转换
 */
public class EntityMapperUtils {

    private static final Logger log = LoggerFactory.getLogger(EntityMapperUtils.class);

    /**
     * 实体对象转Map
     *
     * @param entity 实体对象
     * @return Map对象
     */
    public static Map<String, Object> entityToMap(Object entity) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(entity);
                if (value != null) {
                    map.put(name, value);
                }
            } catch (Exception e) {
                // 忽略访问异常
            }
        }
        return map;
    }

    /**
     * 实体列表转Map列表
     *
     * @param entityList 实体列表
     * @return Map列表
     */
    public static <T> List<Map<String, Object>> entityListToMapList(List<T> entityList) {
        if (entityList == null) {
            return new ArrayList<>();
        }
        return entityList.stream()
                .map(EntityMapperUtils::entityToMap)
                .collect(Collectors.toList());
    }

    /**
     * Map转实体对象
     *
     * @param map         Map对象
     * @param entityClass 实体类
     * @return 实体对象
     */
    public static <T> T mapToEntity(Map<String, Object> map, Class<T> entityClass) {
        if (map == null) {
            return null;
        }

        T entity;
        try {
            entity = entityClass.getDeclaredConstructor().newInstance();
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName();
                if (map.containsKey(name)) {
                    field.setAccessible(true);
                    Object value = map.get(name);
                    if (value != null) {
                        // 处理类型转换
                        if (field.getType() == Long.class && value instanceof Number) {
                            field.set(entity, ((Number) value).longValue());
                        } else if (field.getType() == Integer.class && value instanceof Number) {
                            field.set(entity, ((Number) value).intValue());
                        } else if (field.getType() == Double.class && value instanceof Number) {
                            field.set(entity, ((Number) value).doubleValue());
                        } else if (field.getType() == Float.class && value instanceof Number) {
                            field.set(entity, ((Number) value).floatValue());
                        } else if (field.getType() == Boolean.class && value instanceof String) {
                            field.set(entity, Boolean.valueOf((String) value));
                        } else if (field.getType().isEnum() && value instanceof String) {
                            try {
                                // Use helper method for safe enum conversion
                                Object enumValue = getEnumConstant(field.getType(), (String) value);
                                field.set(entity, enumValue);
                            } catch (IllegalArgumentException e) {
                                log.warn("Invalid enum value '{}' for enum type {}: {}", value, field.getType().getName(), e.getMessage());
                            }
                        } else if (value instanceof Enum) {
                            // Handle Enums: Store their names or ordinal values
                            // return ((Enum<?>) value).name(); // Store name
                            field.set(entity, ((Enum<?>) value).ordinal()); // Store ordinal (integer)
                        } else if (value instanceof Date) {
                            field.set(entity, value);
                        } else {
                            field.set(entity, value);
                        }
                    }
                }
            }
            return entity;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Map列表转实体列表
     *
     * @param mapList     Map列表
     * @param entityClass 实体类
     * @return 实体列表
     */
    public static <T> List<T> mapListToEntityList(List<Map<String, Object>> mapList, Class<T> entityClass) {
        if (mapList == null) {
            return new ArrayList<>();
        }
        return mapList.stream()
                .map(map -> mapToEntity(map, entityClass))
                .collect(Collectors.toList());
    }

    /**
     * 将评论对象转换为JSON字符串并添加到Post对象中
     * TODO: [评论功能] - Code related to Comment entity was removed due to compilation errors (Comment class missing). Needs review/re-implementation.
     */
    /*
    public static Post convertCommentsToPostJson(Post post, List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            post.setCommentsJson("[]");
            post.setComments(new ArrayList<>());
            return post;
        }

        List<Map<String, Object>> commentsList = comments.stream()
                .map(comment -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", comment.getId());
                    map.put("userId", comment.getUserId());
                    map.put("content", comment.getContent());
                    map.put("creationTime", comment.getCreationTime());
                    map.put("likeCount", comment.getLikeCount());
                    map.put("status", comment.getStatus());
                    // 处理父评论ID
                    if (comment.getParentId() != null) {
                        map.put("parentId", comment.getParentId());
                    }
                    return map;
                })
                .collect(Collectors.toList());

        // Requires JsonUtils which is also missing
        // String commentsJson = JsonUtils.toJsonString(commentsList);
        // post.setCommentsJson(commentsJson);
        post.setComments(commentsList);
        return post;
    }
    */

    /**
     * 将Score对象的数据合并到CourseSelection对象中
     */
    public static CourseSelection mergeScoreToCourseSelection(CourseSelection courseSelection, Score score) {
        if (score == null) {
            return courseSelection;
        }

        courseSelection.setScoreValue(score.getTotalScore());
        courseSelection.setGpa(score.getGpa());
        courseSelection.setRegularScore(score.getRegularScore());
        courseSelection.setMidtermScore(score.getMidtermScore());
        courseSelection.setFinalExamScore(score.getFinalScore());

        return courseSelection;
    }

    /**
     * 将Term对象的信息转换为字符串表示
     * TODO: [学期功能] - Code related to Term entity was removed due to compilation errors (Term class missing). Needs review/re-implementation.
     */
    /*
    public static String convertTermToString(Term term) {
        if (term == null) {
            return null;
        }
        return term.getCode(); // 使用学期代码作为字符串表示
    }
    */

    /**
     * 将班级ID转换为班级名称字符串
     * TODO: [班级功能] - Code related to Clazz entity was removed due to compilation errors (Clazz class missing). Needs review/re-implementation.
     */
    /*
    public static String convertClassIdToName(Long classId, List<Clazz> classList) {
        if (classId == null || classList == null || classList.isEmpty()) {
            return null;
        }
        
        return classList.stream()
                .filter(clazz -> clazz.getId().equals(classId))
                .map(Clazz::getName)
                .findFirst()
                .orElse(null);
    }
    */

    /**
     * 将活动参与者和注册记录信息合并到活动对象中
     * TODO: [活动参与者] - This method requires JsonUtils which is missing.
     */
    /*
    public static Activity mergeParticipantsToActivity(Activity activity, List<Map<String, Object>> participants) {
        if (participants == null || participants.isEmpty()) {
            activity.setParticipantsJson("[]");
            activity.setParticipants(new ArrayList<>());
            return activity;
        }

        // String participantsJson = JsonUtils.toJsonString(participants);
        // activity.setParticipantsJson(participantsJson);
        activity.setParticipants(participants);
        return activity;
    }
    */

    /**
     * Converts an enum constant to its string representation (name).
     * If the enum constant is null, returns null.
     *
     * @param enumConstant The enum constant.
     * @param <E>          The enum type.
     * @return The name of the enum constant, or null if the input is null.
     */
    public static <E extends Enum<?>> String enumToString(E enumConstant) {
        return enumConstant != null ? enumConstant.name() : null;
    }

    /**
     * Converts a string value to an enum constant of the specified enum type.
     * If the string value is null or empty, or if no matching enum constant is found,
     * returns null.
     *
     * @param value     The string value to convert.
     * @param enumClass The class object of the enum type.
     * @param <E>       The enum type.
     * @return The corresponding enum constant, or null if not found or input is invalid.
     */
    public static <E extends Enum<E>> E stringToEnum(String value, Class<E> enumClass) {
        if (value == null || value.isEmpty() || enumClass == null || !enumClass.isEnum()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Log or handle the error if the string doesn't match any enum constant
            System.err.println("Error converting string '" + value + "' to enum " + enumClass.getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    public static Map<String, Object> enumToMap(Enum<?> enumInstance) {
        if (enumInstance == null) return Collections.emptyMap();
        Map<String, Object> map = new HashMap<>();
        map.put("name", enumInstance.name());
        // Add other common enum properties if needed (e.g., ordinal, description via interface)
        // Example assuming an interface `DescribableEnum` with `getDescription()`
        // if (enumInstance instanceof DescribableEnum) {
        //     map.put("description", ((DescribableEnum) enumInstance).getDescription());
        // }
        return map;
    }

    public static List<Map<String, Object>> enumListToMapList(Class<? extends Enum<?>> enumClass) {
        if (enumClass == null || !enumClass.isEnum()) return Collections.emptyList();
        return Arrays.stream(enumClass.getEnumConstants())
                .map(EntityMapperUtils::enumToMap)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to safely get an enum constant from a String value,
     * handling wildcard types.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> T getEnumConstant(Class<?> enumType, String value) {
        if (!enumType.isEnum()) {
            throw new IllegalArgumentException("Type is not an enum: " + enumType);
        }
        // Cast the Class object to the appropriate Enum type
        return Enum.valueOf((Class<T>) enumType, value);
    }
}