package com.tomorrowmust.system.domain.constant;

public class RedisConstant {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final String REGISTER_CODE_KEY = "register:code:";
    public static final Long REGISTER_CODE_TTL = 1L;
    public static final Long LOGIN_CODE_TTL = 1L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 60L;

    public static final String CACHE_RESOURCE_KEY = "cache:resource:";

    public static final String LOCK_ORDER_KEY = "lock:order:";
    public static final String ORDER_STOCK_KEY = "order:stock:";
    public static final String CACHE_RESOURCE_TYPE_KEY = "cache:resource:type:";

    // 缓存 key 前缀
    public static final String CACHE_PAGE_KEY = "page:";
    public static final String EMPTY_FLAG = "EMPTY";

    // 分页数据在 Hash 中的 field 前缀
    public static final String PAGE_ITEM_PREFIX = "item:";
    public static final String PAGE_META = "meta";

    // 布隆过滤器
    public static final String BLOOM_FILTER_RESOURCE="bf:resource";





}
