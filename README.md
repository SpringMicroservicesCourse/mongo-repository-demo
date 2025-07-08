# MongoDB Spring Data Repository 示範專案 ☕

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green.svg)](https://www.mongodb.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 專案介紹

這是一個展示 **Spring Data MongoDB Repository** 模式的咖啡店管理系統示範專案。專案演示了如何使用 Spring Boot 與 MongoDB 進行整合，實現完整的 CRUD 操作，並展示自定義類型轉換器的使用。

### 🎯 專案特色

- **Repository 模式**：使用 Spring Data MongoDB Repository 簡化資料存取
- **完整轉換器系統**：支援 Money 類型的雙向轉換（Document ↔ Money, Long ↔ Money）
- **完整 CRUD 操作**：創建、查詢、更新、刪除咖啡記錄
- **數據類型相容性**：解決 MongoDB 儲存格式不一致問題
- **容器化支援**：支援 Docker MongoDB 部署
- **測試友好配置**：支援測試環境隔離和模擬

> 💡 **適合學習對象**  
> - Spring Boot 初學者想了解 MongoDB 整合
> - 後端開發者學習 NoSQL 資料庫操作
> - 架構師參考 Spring Data Repository 設計模式
> - 想了解 MongoDB 類型轉換問題解決方案的開發者

## 技術棧

### 核心框架
| 技術 | 版本 | 用途 |
|-----|------|-----|
| **Spring Boot** | 3.4.5 | 主要應用框架 |
| **Spring Data MongoDB** | 3.4.5 | MongoDB 資料存取層 |
| **MongoDB** | 7.0+ | NoSQL 文件資料庫 |

### 開發工具與輔助函式庫
| 工具 | 版本 | 用途 |
|-----|------|-----|
| **Java** | 21 | 程式語言 |
| **Maven** | 3.6+ | 專案建構工具 |
| **Lombok** | 1.18+ | 減少 Java 樣板代碼 |
| **Joda Money** | 2.0.2 | 貨幣類型處理 |
| **Docker** | Latest | MongoDB 容器化部署 |

## 快速開始

### 前置需求

```bash
# 檢查 Java 版本（需要 21 以上）
java -version

# 檢查 Maven 版本
mvn -version

# 檢查 Docker 版本
docker --version
```

### 📋 安裝步驟

#### 1. 克隆專案

```bash
git clone https://github.com/SpringMicroservicesCourse/mongo-repository-demo.git
cd mongo-repository-demo
```

#### 2. 啟動 MongoDB 容器

```bash
# 啟動 MongoDB Docker 容器
docker run -d --name mongo -p 27017:27017 mongo:latest

# 驗證容器運行狀態
docker ps
```

#### 3. 簡化配置 (推薦)

為了簡化開發和測試，我們已將配置修改為無認證模式：

```bash
# 直接連接到 MongoDB（無需認證）
docker exec -it mongo mongosh

# 驗證連接並查看資料庫
show dbs

# 退出 MongoDB shell
exit
```

#### 4. 編譯和運行專案

```bash
# 編譯專案
mvn clean compile

# 運行測試
mvn test

# 完整建置
mvn clean install

# 運行應用程式
mvn spring-boot:run
```

## 專案結構

```
mongo-repository-demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tw/fengqing/spring/data/mongodemo/
│   │   │       ├── MongoRepositoryDemoApplication.java    # 主應用程式類
│   │   │       ├── config/
│   │   │       │   └── MongoConfig.java                   # MongoDB 配置類
│   │   │       ├── model/
│   │   │       │   └── Coffee.java                        # 咖啡實體模型
│   │   │       ├── repository/
│   │   │       │   └── CoffeeRepository.java              # Repository 接口
│   │   │       └── converter/
│   │   │           ├── MoneyReadConverter.java            # Document→Money 轉換器
│   │   │           ├── MoneyLongReadConverter.java        # Long→Money 轉換器  
│   │   │           └── MoneyWriteConverter.java           # Money→Long 轉換器
│   │   └── resources/
│   │       └── application.properties                     # 應用配置檔
│   └── test/
│       └── java/
│           └── tw/fengqing/spring/data/mongodemo/
│               └── MongoRepositoryDemoApplicationTests.java # 測試類
├── target/                                                 # 建置輸出目錄
├── pom.xml                                                 # Maven 配置檔
└── README.md                                               # 本文件
```

## 核心組件說明

### 🏗️ Coffee 實體類

```java
@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coffee {
    @Id
    private String id;              // MongoDB 自動生成的 ID
    private String name;            // 咖啡名稱
    private Money price;            // 價格（使用 Joda Money）
    private Date createTime;        // 建立時間
    private Date updateTime;        // 更新時間
}
```

### 📦 CoffeeRepository 接口

```java
public interface CoffeeRepository extends MongoRepository<Coffee, String> {
    List<Coffee> findByName(String name);  // 根據名稱查詢咖啡
}
```

**自動提供的方法：**
- `save(T entity)` - 儲存或更新實體
- `findAll()` - 查詢所有記錄
- `findById(ID id)` - 根據 ID 查詢
- `deleteAll()` - 刪除所有記錄
- `insert(Iterable<T> entities)` - 批量插入

### 🔄 Money 轉換器系統

我們實作了完整的 Money 轉換器系統來處理不同的資料格式：

#### MoneyReadConverter (Document → Money)
```java
@ReadingConverter
public class MoneyReadConverter implements Converter<Document, Money> {
    @Override
    public Money convert(Document source) {
        Document money = (Document) source.get("money");
        double amount = money.getDouble("amount");
        String currency = ((Document) money.get("currency")).getString("code");
        return Money.of(CurrencyUnit.of(currency), amount);
    }
}
```

#### MoneyLongReadConverter (Long → Money)
```java
@ReadingConverter
public class MoneyLongReadConverter implements Converter<Long, Money> {
    @Override
    public Money convert(Long source) {
        // 將 Long 值轉換為台幣 Money 對象（假設存儲的是以分為單位）
        return Money.ofMinor(CurrencyUnit.of("TWD"), source);
    }
}
```

#### MoneyWriteConverter (Money → Long)
```java
@WritingConverter
public class MoneyWriteConverter implements Converter<Money, Long> {
    @Override
    public Long convert(Money source) {
        // 將 Money 轉換為以分為單位的 Long 值存儲
        return source.getAmountMinorLong();
    }
}
```

**轉換器功能說明：**
- **MoneyReadConverter**：處理複雜 Document 格式的 Money 數據
- **MoneyLongReadConverter**：處理簡單 Long 格式的 Money 數據
- **MoneyWriteConverter**：將 Money 對象轉換為 Long 存儲
- 支援台幣（TWD）和其他貨幣格式
- 自動處理小數位和貨幣單位轉換

### ⚙️ MongoConfig 配置類

```java
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
```

**設計目的：**
- 註冊多個自定義轉換器
- 解決資料格式不一致問題
- 支援向前和向後相容性
- 提供靈活的資料類型轉換

## 🚀 運行結果示例

成功運行後，您會看到類似以下的輸出：

```log
2025-07-08 11:04:31.486 INFO --- Coffee Coffee(id=686c8abf2e68ee1bf8f04e84, name=espresso, price=TWD 100.00, createTime=Tue Jul 08 11:04:31 CST 2025, updateTime=Tue Jul 08 11:04:31 CST 2025)
```

### 📊 執行流程

1. **CREATE**：建立兩筆咖啡記錄（espresso: TWD 100, latte: TWD 150）
2. **READ**：查詢並顯示所有咖啡記錄（按名稱排序）
3. **UPDATE**：更新 latte 價格從 TWD 150 → TWD 175
4. **DELETE**：刪除所有咖啡記錄

## 配置說明

### application.properties

```properties
# 簡化的 MongoDB 連接配置（無認證）
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=springbucks

# 如果需要認證，請先在 MongoDB 中創建用戶
# spring.data.mongodb.uri=mongodb://springbucks:springbucks@localhost:27017/springbucks

# 日誌等級設定（可選）
logging.level.org.springframework.data.mongodb.core=INFO
```

### 環境變數配置（建議）

```bash
# 設定環境變數
export MONGO_HOST=localhost
export MONGO_PORT=27017
export MONGO_DB=springbucks
```

```properties
# 在 application.properties 中使用環境變數
spring.data.mongodb.host=${MONGO_HOST:localhost}
spring.data.mongodb.port=${MONGO_PORT:27017}
spring.data.mongodb.database=${MONGO_DB:springbucks}
```

## 故障排除

### 常見問題與解決方案

| 問題 | 症狀 | 解決方案 |
|-----|------|----------|
| **MongoDB 連線失敗** | `MongoTimeoutException` | 檢查容器是否運行：`docker ps` |
| **Money 轉換錯誤** | `ConverterNotFoundException: Long to Money` | 已修復：新增 `MoneyLongReadConverter` |
| **測試失敗** | `IllegalStateException` | 已修復：使用 `@MockBean` 隔離測試 |
| **資料格式不一致** | `ClassCastException` | 已修復：多重轉換器支援 |
| **認證失敗** | `AuthenticationFailed` | 使用無認證配置或確認使用者設定 |

### 🛠️ 除錯命令

```bash
# 檢查 MongoDB 容器狀態
docker ps
docker logs mongo

# 連接到 MongoDB 查看資料
docker exec -it mongo mongosh
use springbucks
db.coffee.find().pretty()

# 查看應用程式詳細日誌
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"

# 檢查網路連接
telnet localhost 27017

# 運行測試查看詳細錯誤
mvn test -X
```

### 🔍 資料庫操作

```javascript
// 在 MongoDB shell 中執行

// 查看所有咖啡記錄
db.coffee.find().pretty()

// 查看特定咖啡
db.coffee.find({name: "espresso"})

// 檢查資料格式
db.coffee.findOne()

// 檢查資料庫統計
db.coffee.countDocuments()

// 清空咖啡集合
db.coffee.deleteMany({})
```

## 技術亮點與修復

### 🔧 最近修復

1. **Money 轉換器問題解決**
   - 新增 `MoneyLongReadConverter` 處理 Long 到 Money 轉換
   - 新增 `MoneyWriteConverter` 處理 Money 到 Long 轉換
   - 支援多種資料格式的向前相容

2. **測試配置優化**
   - 使用 `@MockBean` 隔離測試環境
   - 防止測試時執行 `CommandLineRunner`
   - 簡化測試配置

3. **MongoDB 配置簡化**
   - 移除複雜的認證配置
   - 支援開發環境快速啟動
   - 提供認證配置選項

### 📚 核心概念

1. **Spring Data Repository 模式**
   - 自動生成 CRUD 方法
   - 自定義查詢方法命名規則
   - 分頁和排序支援

2. **MongoDB 文件對映**
   - `@Document` 註解的使用
   - `@Id` 主鍵對映
   - 嵌套物件處理

3. **自定義轉換器**
   - `@ReadingConverter` 讀取轉換
   - `@WritingConverter` 寫入轉換
   - 多重轉換器註冊
   - 類型安全的資料轉換

4. **Spring Boot 自動配置**
   - MongoDB 自動配置機制
   - 循環依賴解決策略
   - 配置類的最佳實踐

### 🎯 進階話題

- **查詢方法**：學習 Spring Data 查詢方法命名規則
- **聚合操作**：MongoDB 聚合管道操作
- **事務處理**：MongoDB 多文件事務
- **索引優化**：MongoDB 索引策略
- **監控和效能**：應用程式效能監控
- **類型轉換**：複雜資料類型的序列化/反序列化

## 擴展功能

### 🔧 可能的改進方向

- [ ] 新增更多咖啡屬性（產地、烘焙度等）
- [ ] 實作分頁查詢功能
- [ ] 新增 REST API 接口
- [ ] 整合 Spring Security
- [ ] 新增資料驗證
- [ ] 實作快取機制
- [ ] 新增監控和指標
- [ ] Docker Compose 一鍵部署
- [x] 修復 Money 轉換器問題
- [x] 優化測試配置

## 貢獻指南

歡迎提出問題和改進建議！

1. Fork 本專案
2. 建立功能分支：`git checkout -b feature/new-feature`
3. 提交變更：`git commit -am 'Add some feature'`
4. 推送到分支：`git push origin feature/new-feature`
5. 提交 Pull Request

## 授權說明

本專案採用 [MIT 授權條款](LICENSE)。

## 關於我們

**風清雲談技術團隊**

我們專注於 Java 企業級應用開發，致力於提供高品質的技術解決方案和學習資源。

- **部落格**：[風清雲談](https://blog.fengqing.tw/)
- **電子郵件**：[fengqing.tw@gmail.com](mailto:fengqing.tw@gmail.com)
- **GitHub**：[@SpringMicroservicesCourse](https://github.com/SpringMicroservicesCourse)

---

**📅 最後更新：2025-07-08**  
**👨‍💻 維護者：風清雲談團隊**  
**🔧 版本：v1.1.0 - 修復 Money 轉換器問題**  
**⭐ 如果這個專案對您有幫助，請給我們一個 Star！** 