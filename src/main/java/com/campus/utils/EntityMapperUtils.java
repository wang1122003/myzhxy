package com.campus.utils;

import com.campus.entity.CourseSelection;
import com.campus.entity.Post;
import com.campus.entity.Score;
import com.campus.entity.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.util.*;
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
                                // 使用辅助方法进行安全的枚举转换
                                Object enumValue = getEnumConstant(field.getType(), (String) value);
                                field.set(entity, enumValue);
                            } catch (IllegalArgumentException e) {
                                log.warn("枚举类型 {} 的值 '{}' 无效: {}", field.getType().getName(), value, e.getMessage());
                            }
                        } else if (value instanceof Enum) {
                            // 处理枚举：存储它们的名称或序数值
                            // return ((Enum<?>) value).name(); // 存储名称
                            field.set(entity, ((Enum<?>) value).ordinal()); // 存储序数（整数）
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
     * 使用Map<String, Object>替代Comment实体类，规避了Comment实体不存在的问题
     */
    public static Post convertCommentsToPostJson(Post post, List<Map<String, Object>> comments) {
        if (comments == null || comments.isEmpty()) {
            post.setCommentsJson("[]");
            post.setComments(new ArrayList<>());
            return post;
        }

        try {
            // 使用Jackson的ObjectMapper来序列化评论列表
            ObjectMapper objectMapper = new ObjectMapper();
            String commentsJson = objectMapper.writeValueAsString(comments);
            post.setCommentsJson(commentsJson);
            post.setComments(comments);
            return post;
        } catch (Exception e) {
            // 处理序列化异常
            post.setCommentsJson("[]");
            post.setComments(new ArrayList<>());
            return post;
        }
    }

    /**
     * 从JSON字符串解析评论列表
     */
    public static List<Map<String, Object>> parseCommentsFromJson(String commentsJson) {
        if (commentsJson == null || commentsJson.isEmpty() || commentsJson.equals("[]")) {
            return new ArrayList<>();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(commentsJson,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (Exception e) {
            // 处理解析异常
            return new ArrayList<>();
        }
    }

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
     * 将学期信息转换为字符串表示
     * 使用Map代替Term实体类
     */
    public static String convertTermMapToString(Map<String, Object> term) {
        if (term == null) {
            return null;
        }

        // 尝试从Map中获取code字段作为字符串表示
        Object codeObj = term.get("code");
        if (codeObj != null) {
            return codeObj.toString();
        }

        // 如果没有code字段，尝试name字段
        Object nameObj = term.get("name");
        if (nameObj != null) {
            return nameObj.toString();
        }

        // 如果都没有，返回term的字符串表示
        return term.toString();
    }
    
    /**
     * 将班级ID转换为班级名称字符串
     * 使用Map<String, Object>列表替代Clazz实体类列表
     */
    public static String convertClassIdToName(Long classId, List<Map<String, Object>> classList) {
        if (classId == null || classList == null || classList.isEmpty()) {
            return null;
        }
        
        return classList.stream()
                .filter(clazz -> {
                    Object idObj = clazz.get("id");
                    if (idObj instanceof Number) {
                        return ((Number) idObj).longValue() == classId;
                    } else if (idObj instanceof String) {
                        try {
                            return Long.parseLong((String) idObj) == classId;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                    return false;
                })
                .map(clazz -> {
                    Object nameObj = clazz.get("name");
                    return nameObj != null ? nameObj.toString() : null;
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * 将活动参与者和注册记录信息合并到活动对象中
     */
    public static <T> T mergeParticipantsToActivity(T activity, List<Map<String, Object>> participants) {
        if (activity == null) {
            return null;
        }
        
        if (participants == null || participants.isEmpty()) {
            try {
                // 使用反射设置字段
                Field jsonField = activity.getClass().getDeclaredField("participantsJson");
                Field listField = activity.getClass().getDeclaredField("participants");

                jsonField.setAccessible(true);
                listField.setAccessible(true);

                jsonField.set(activity, "[]");
                listField.set(activity, new ArrayList<>());
            } catch (Exception e) {
                log.error("设置活动参与者信息失败", e);
            }
            return activity;
        }

        try {
            // 使用Jackson ObjectMapper序列化参与者列表
            ObjectMapper objectMapper = new ObjectMapper();
            String participantsJson = objectMapper.writeValueAsString(participants);

            // 使用反射设置字段
            Field jsonField = activity.getClass().getDeclaredField("participantsJson");
            Field listField = activity.getClass().getDeclaredField("participants");

            jsonField.setAccessible(true);
            listField.setAccessible(true);

            jsonField.set(activity, participantsJson);
            listField.set(activity, participants);
        } catch (Exception e) {
            log.error("设置活动参与者信息失败", e);
            try {
                // 设置空值
                Field jsonField = activity.getClass().getDeclaredField("participantsJson");
                Field listField = activity.getClass().getDeclaredField("participants");

                jsonField.setAccessible(true);
                listField.setAccessible(true);

                jsonField.set(activity, "[]");
                listField.set(activity, new ArrayList<>());
            } catch (Exception ex) {
                log.error("重置活动参与者信息失败", ex);
            }
        }
        
        return activity;
    }

    /**
     * 从JSON字符串解析活动参与者列表
     */
    public static List<Map<String, Object>> parseParticipantsFromJson(String participantsJson) {
        if (participantsJson == null || participantsJson.isEmpty() || participantsJson.equals("[]")) {
            return new ArrayList<>();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(participantsJson,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (Exception e) {
            log.error("解析活动参与者JSON数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 将枚举常量转换为其字符串表示（名称）。
     * 如果枚举常量为null，则返回null。
     *
     * @param enumConstant 枚举常量。
     * @param <E>          枚举类型。
     * @return 枚举常量的名称，如果输入为null则返回null。
     */
    public static <E extends Enum<?>> String enumToString(E enumConstant) {
        return enumConstant != null ? enumConstant.name() : null;
    }

    /**
     * 将字符串值转换为指定枚举类型的枚举常量。
     * 如果字符串值为null或为空，或者找不到匹配的枚举常量，
     * 则返回null。
     *
     * @param value     要转换的字符串值。
     * @param enumClass 枚举类型的类对象。
     * @param <E>       枚举类型。
     * @return 对应的枚举常量，如果未找到或输入无效则返回null。
     */
    public static <E extends Enum<E>> E stringToEnum(String value, Class<E> enumClass) {
        if (value == null || value.isEmpty() || enumClass == null || !enumClass.isEnum()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 记录或处理错误，如果字符串与任何枚举常量不匹配
            System.err.println("将字符串 '" + value + "' 转换为枚举 " + enumClass.getSimpleName() + " 时出错: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将枚举实例转换为Map
     *
     * @param enumInstance 枚举实例
     * @return 包含枚举信息的Map
     */
    public static Map<String, Object> enumToMap(Enum<?> enumInstance) {
        if (enumInstance == null) return Collections.emptyMap();
        Map<String, Object> map = new HashMap<>();
        map.put("name", enumInstance.name());
        // 如果需要，添加其他常见的枚举属性（例如序数，通过接口的描述）
        // 例如假设有一个接口 `DescribableEnum` 带有 `getDescription()`
        // if (enumInstance instanceof DescribableEnum) {
        //     map.put("description", ((DescribableEnum) enumInstance).getDescription());
        // }
        return map;
    }

    /**
     * 将枚举类的所有常量转换为Map列表
     *
     * @param enumClass 枚举类
     * @return 包含所有枚举常量信息的Map列表
     */
    public static List<Map<String, Object>> enumListToMapList(Class<? extends Enum<?>> enumClass) {
        if (enumClass == null || !enumClass.isEnum()) return Collections.emptyList();
        return Arrays.stream(enumClass.getEnumConstants())
                .map(EntityMapperUtils::enumToMap)
                .collect(Collectors.toList());
    }

    /**
     * 辅助方法，用于从字符串值安全地获取枚举常量，
     * 处理通配符类型。
     */
    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> T getEnumConstant(Class<?> enumType, String value) {
        if (!enumType.isEnum()) {
            throw new IllegalArgumentException("类型不是枚举: " + enumType);
        }
        // 将Class对象转换为适当的Enum类型
        return Enum.valueOf((Class<T>) enumType, value);
    }

    /**
     * 将Term对象的信息转换为字符串表示
     */
    public static String convertTermToString(Term term) {
        if (term == null) {
            return null;
        }

        return term.getCode(); // 使用学期代码作为字符串表示
    }

    /**
     * 将Term实体转换为Map
     */
    public static Map<String, Object> termToMap(Term term) {
        if (term == null) {
            return Collections.emptyMap();
        }

        return entityToMap(term);
    }

    /**
     * 将Map转换为Term实体
     */
    public static Term mapToTerm(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        return mapToEntity(map, Term.class);
    }

    /**
     * 将Term列表转换为Map列表
     */
    public static List<Map<String, Object>> termListToMapList(List<Term> termList) {
        if (termList == null || termList.isEmpty()) {
            return Collections.emptyList();
        }

        return entityListToMapList(termList);
    }

    /**
     * 将Map列表转换为Term列表
     */
    public static List<Term> mapListToTermList(List<Map<String, Object>> mapList) {
        if (mapList == null || mapList.isEmpty()) {
            return Collections.emptyList();
        }

        return mapListToEntityList(mapList, Term.class);
    }

    /**
     * 将JSON格式的参与者字符串转为Map列表
     *
     * @param participantsJson 参与者JSON字符串
     * @return 参与者列表
     */
    public static List<Map<String, Object>> parseParticipantsJson(String participantsJson) {
        if (participantsJson == null || participantsJson.isEmpty() || "[]".equals(participantsJson.trim())) {
            return new ArrayList<>();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(
                    participantsJson,
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );
        } catch (Exception e) {
            log.error("解析参与者JSON数据失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 将参与者列表转换为JSON字符串
     *
     * @param participants 参与者列表
     * @return 参与者JSON字符串
     */
    public static String convertParticipantsToJson(List<Map<String, Object>> participants) {
        if (participants == null || participants.isEmpty()) {
            return "[]";
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(participants);
        } catch (Exception e) {
            log.error("转换参与者列表为JSON失败: {}", e.getMessage(), e);
            return "[]";
        }
    }
}