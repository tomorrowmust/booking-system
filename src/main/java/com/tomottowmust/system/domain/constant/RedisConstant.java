package com.tomottowmust.system.domain.constant;

public class RedisConstant {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;


    public static final Long CACHE_NULL_TTL = 2L;
    public static final String CACHE_ORDER_TYPE_KEY="cache:order:type";
    public static final Long CACHE_ORDER_TTL = 30L;
    public static final String CACHE_ORDER_KEY = "cache:shop:";

    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;

    public static final String CACHE_RESOURCE_KEY = "cache:resource:";

    public static final String LOCK_ORDER_KEY = "lock:order:";
    public static final String ORDER_STOCK_KEY = "order:stock:";
    public static final String CACHE_RESOURCE_TYPE_KEY = "cache:resource:type:";
    public static final String BOOKING_ORDER_KEY = "booking:order:";

    // 缓存 key 前缀
    public static final String CACHE_PAGE_KEY = "page:";
    public static final String CACHE_RESOURCE_DETAIL_KEY = "resource:detail:";
    public static final String EMPTY_FLAG = "EMPTY";

    // 分页数据在 Hash 中的 field 前缀
    public static final String PAGE_ITEM_PREFIX = "item:";
    public static final String PAGE_META = "meta";





}
