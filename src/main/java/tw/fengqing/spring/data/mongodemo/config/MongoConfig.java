package tw.fengqing.spring.data.mongodemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import tw.fengqing.spring.data.mongodemo.converter.MoneyReadConverter;
import tw.fengqing.spring.data.mongodemo.converter.MoneyLongReadConverter;
import tw.fengqing.spring.data.mongodemo.converter.MoneyWriteConverter;

import java.util.Arrays;

@Configuration
public class MongoConfig {
    
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
            new MoneyReadConverter(),        // Document -> Money
            new MoneyLongReadConverter(),    // Long -> Money  
            new MoneyWriteConverter()        // Money -> Long
        ));
    }
} 