package tw.fengqing.spring.data.mongodemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import tw.fengqing.spring.data.mongodemo.repository.CoffeeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "spring.main.web-application-type=none"
})
public class MongoRepositoryDemoApplicationTests {

    @MockBean
    private CoffeeRepository coffeeRepository;

    @Test
    public void contextLoads() {
        // 這個測試僅檢查 Spring 應用上下文是否能正確載入
        // CommandLineRunner 不會執行，因為我們模擬了 CoffeeRepository
    }

} 