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
    public static final String CACHE_PAGE_KEY = "page:";
    public static final String BOOKING_ORDER_KEY = "booking:order:";

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";
    public static final String FEED_KEY = "feed:";
    public static final String SHOP_GEO_KEY = "shop:geo:";
    public static final String USER_SIGN_KEY = "sign:";
}
