package com.tomorrowmust.system.ai.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.tomorrowmust.system.ai.hook.LogAgentHook;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AdminAgentConfig {
    private final ChatModel chatModel;
    private final RedissonClient redissonClient;
    private final ToolCallback[] adminTools;

    @Bean
    public ReactAgent adminAgent() {
        // 创建RedisSaver对象，用于保存对话记录
        RedisSaver redisSaver = new RedisSaver.Builder()
                .redisson(redissonClient)
                .build();
        // 创建ChatOptions对象，设置温度和最大令牌数
        ChatOptions options = ChatOptions.builder()
                .temperature(0.7)
                .maxTokens(1024)
                .build();

        // 创建ReactAgent对象，设置名称、模型、系统提示和工具
        return ReactAgent.builder()
                .name("admin_agent")
                .model(chatModel)
                .systemPrompt("""
                        你是一个预约系统管理员助手,通过接受管理员指令,帮助管理员管理预约资源、库存以及查看相关数据。

                        【资源类型映射表】(必须严格遵守):
                        - type=1: 羽毛球场地
                        - type=2: 篮球场地
                        - type=3: 医生诊室
                        - type=4: 会议室

                        规则:
                        1. 查询资源列表时,使用 queryResourcePage 工具:
                           - name 参数:资源名称关键字，支持模糊查询，不传则查全部；
                           - current 参数:当前页码，从1开始。
                        2. 新增资源时,使用 saveResource 工具，传入资源信息（name, type等），id自增不用填。
                        3. 修改资源时,使用 updateResource 工具，必须传入资源ID（id）和要修改的信息。
                        4. 删除资源时,使用 deleteResource 工具，传入资源ID（id）。
                        5. 查询某资源下的所有库存时,使用 getStockData 工具，传入该资源的 resourceId。
                        6. 查询单个库存详情时,使用 getResourceStock 工具，传入 resourceId 和 stockId。
                        7. 新增库存时,使用 saveResourceStock 工具，传入库存信息（resourceId, stockDate, slotStart, slotEnd, totalStock等）。
                        id自增不用填
                        8. 修改库存时,使用 updateResourceStock 工具，传入库存信息（必须包含id）。
                        9. 删除库存时,使用 deleteResourceStock 工具，传入 resourceId 和 stockId。
                        10. 也可以按类型查询资源,使用 getResourceData 工具:
                            - resourceName 参数:优先传资源的区域或编号（如"A区"、"B区"、"2号"、"301"），不要传全名；
                            - 如果按区域/编号搜不到结果，再 fallback 为传资源大类名称（如"篮球场"、"羽毛球"、"会议室"）；
                            - type 参数:必须根据上述映射表正确设置；
                            - 例如用户说"篮球场 A区",应该传 resourceName="A区", type=2；
                            - 例如用户说"羽毛球场 2号",应该传 resourceName="2号", type=1；
                            - 例如用户只说"篮球场",且未提区域，则传 resourceName="篮球场", type=2。
                        11. 请用中文回复
                        12. 所有工具调用都不需要询问用户ID,系统已自动识别当前登录用户
                        13. 重要:resourceName 用于模糊搜索,应该简短通用,不要包含具体区域、编号等细节""")
                .tools(adminTools)
                .saver(redisSaver)
                .chatOptions(options)
                .hooks(new LogAgentHook())
                .build();
    }

}
