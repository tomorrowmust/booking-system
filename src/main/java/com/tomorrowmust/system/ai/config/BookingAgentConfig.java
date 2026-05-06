package com.tomorrowmust.system.ai.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.tomorrowmust.system.ai.hook.LogAgentHook;
import com.tomorrowmust.system.ai.memory.RedisChatMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BookingAgentConfig {

    @Value("${booking.ai.memory.max:100}")
    private Integer maxMessages;

    private final ChatModel chatModel;
    private final RedissonClient redissonClient;
    private final ToolCallback[] userTools;
    private final VectorStore vectorStore;

    @Bean
    public ReactAgent bookingAgent() {
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
                .name("booking_agent")
                .model(chatModel)
                .systemPrompt("""
                        你是一个预约助手,通过接受用户指令,帮助用户查看资源库存和创建预约订单。
                        
                        【资源类型映射表】(必须严格遵守):
                        - type=1: 羽毛球场地
                        - type=2: 篮球场地
                        - type=3: 医生诊室
                        - type=4: 会议室
                        
                        规则:
                        1. 查询资源时,使用 getResourceData 工具:
                           - resourceName 参数:优先传资源的区域或编号（如"A区"、"B区"、"2号"、"301"），不要传全名；
                           - 如果按区域/编号搜不到结果，再 fallback 为传资源大类名称（如"篮球场"、"羽毛球"、"会议室"）；
                           - type 参数:必须根据上述映射表正确设置；
                           - 例如用户说"篮球场 A区",应该传 resourceName="A区", type=2；
                           - 例如用户说"羽毛球场 2号",应该传 resourceName="2号", type=1；
                           - 例如用户只说"篮球场",且未提区域，则传 resourceName="篮球场", type=2。
                        2. 查询库存时，使用 getStockData 工具：
                            查询之前先要获取资源id时，资源id可以使用 getStockData 工具获取：
                            - resourceID 参数:通过 getResourceData 工具获取资源ID；
                        3. 创建预约时,先查库存是否充足,再调用 createOrder 工具(只需传 stockId,用户ID会自动获取)
                        4. 取消预约时,使用 cancelBooking 工具(只需传预约ID,用户ID会自动获取)
                        5. 查询我的预约时,使用 queryMyBooking 工具(不需要传参数,会自动查询当前用户)
                        6. 请用中文回复
                        7. 所有工具调用都不需要询问用户ID,系统已自动识别当前登录用户
                        8. 重要:resourceName 用于模糊搜索,应该简短通用,不要包含具体区域、编号等细节""")
                .tools(userTools)
                .saver(redisSaver)
                .chatOptions(options)
                .hooks(new LogAgentHook())
                .build();
    }
    /**
     * 配置 ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
                                 Advisor loggerAdvisor,
                                 Advisor messageChatMemoryAdvisor) {  // 日志记录器
        return chatClientBuilder
                .defaultAdvisors(loggerAdvisor,messageChatMemoryAdvisor) //添加 Advisor 功能增强
                .defaultToolCallbacks(userTools)
                .build();
    }

    /**
     * 日志记录器
     */
    @Bean
    public Advisor loggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }

    /**
     * 配置 ChatMemoryRepository
     * @return
     */
    @Bean
    public ChatMemoryRepository redisChatMemoryRepository() {
        return new RedisChatMemoryRepository();
    }

    /**
     * 配置 ChatMemory
     * @param chatMemoryRepository
     * @return
     */
    @Bean
    public ChatMemory redisChatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(maxMessages)
                .build();
    }
    /**
     * 配置 MessageChatMemoryAdvisor
     * @param chatMemory
     * @return
     */
    @Bean
    public Advisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

}
