package com.tomorrowmust.system.ai.memory;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Set;

public class RedisChatMemoryRepository implements ChatMemoryRepository,MyChatMemoryRepository{

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 默认redis中key的前缀
    public static final String DEFAULT_PREFIX = "CHAT:";

    private final String prefix;

    public RedisChatMemoryRepository() {
        this.prefix = DEFAULT_PREFIX;
    }

    public RedisChatMemoryRepository(String prefix) {
        this.prefix = prefix;
    }

    private String getKey(String conversationId) {
        return this.prefix + conversationId;
    }

    @NotNull
    @Override
    public List<String> findConversationIds() {
        Set<String> keys = stringRedisTemplate.keys(prefix + "*");
        return keys.stream()
                .map(key -> StrUtil.replace(key, this.prefix, ""))
                .toList();
    }

    @NotNull
    @Override
    public List<Message> findByConversationId(@NotNull String conversationId) {
        // 生成Redis键名用于存储会话消息
        var redisKey = this.getKey(conversationId);
        // 获取Redis列表操作对象
        var listOps = this.stringRedisTemplate.boundListOps(redisKey);

        // 从Redis列表中获取所有的数据
        var messages = listOps.range(0, -1);
        // 将Redis返回的字符串列表转换为Message对象列表
        return CollStreamUtil.toList(messages, MessageUtil::toMessage);
    }

    @Override
    public void saveAll(@NotNull String conversationId, @NotNull List<Message> messages) {
        Assert.notEmpty(messages, "Messages cannot be empty");
        String key = getKey(conversationId);
        BoundListOperations<String, String> listOps = stringRedisTemplate.boundListOps(key);
        stringRedisTemplate.delete(key);
        messages.forEach(message -> listOps.rightPush(MessageUtil.toJson(message)));
    }

    @Override
    public void deleteByConversationId(@NotNull String conversationId) {
        var redisKey = this.getKey(conversationId);
        this.stringRedisTemplate.delete(redisKey);
    }

    @Override
    public void optimization(String conversationId) {
        String key = getKey(conversationId);
        BoundListOperations<String, String> listOps = stringRedisTemplate.boundListOps(key);
        listOps.rightPop(2);
    }
}
