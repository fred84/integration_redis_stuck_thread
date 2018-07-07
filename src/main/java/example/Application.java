package example;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.redis.inbound.RedisQueueMessageDrivenEndpoint;
import org.springframework.messaging.MessageHandler;

public class Application {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE).run(args);
    }

    @Bean
    public DirectChannel exampleChannel(MessageHandler handler) {
        DirectChannel channel = new DirectChannel();
        channel.subscribe(handler);
        return channel;
    }

    @Bean
    public MessageHandler messageHandler() {
        return message -> {
            System.out.println(message.getPayload());

            if (message.getPayload().toString().length() > 10) {
                throw new IllegalArgumentException("Too large payload");
            }
        };
    }

    @Bean("queue_task_executor")
    public AsyncTaskExecutor queueListenerTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("stuck_thread");
        executor.setConcurrencyLimit(1);
        return executor;
    }

    @Bean
    public RedisQueueMessageDrivenEndpoint redisInboundEndpoint(
            RedisConnectionFactory connectionFactory,
            @Qualifier("queue_task_executor") AsyncTaskExecutor executor,
            @Qualifier("exampleChannel") DirectChannel channel
    ) {
        RedisQueueMessageDrivenEndpoint endpoint = new RedisQueueMessageDrivenEndpoint("queue", connectionFactory);
        endpoint.setTaskExecutor(executor);
        endpoint.setSerializer(new StringRedisSerializer());
        endpoint.setAutoStartup(true);
        endpoint.setOutputChannel(channel);
        return endpoint;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }
}