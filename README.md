# MongoDB Spring Data Repository 示範專案 ☕

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green.svg)](https://www.mongodb.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 專案介紹

這是一個展示 **Spring Data MongoDB Repository** 模式的咖啡店管理系統示範專案。專案演示了如何使用 Spring Boot 與 MongoDB 進行整合，實現完整的 CRUD 操作，並展示自定義類型轉換器的使用。

### 🎯 專案特色

- **Repository 模式**：使用 Spring Data MongoDB Repository 簡化資料存取
- **自定義轉換器**：Money 類型的序列化與反序列化
- **完整 CRUD 操作**：創建、查詢、更新、刪除咖啡記錄
- **循環依賴解決方案**：展示 Spring Boot 配置最佳實踐
- **容器化支援**：支援 Docker MongoDB 部署

> 💡 **適合學習對象**  
> - Spring Boot 初學者想了解 MongoDB 整合
> - 後端開發者學習 NoSQL 資料庫操作
> - 架構師參考 Spring Data Repository 設計模式

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

#### 3. 建立 MongoDB 資料庫和使用者

```bash
# 連接到 MongoDB
docker exec -it mongo mongosh

# 建立 springbucks 資料庫和使用者
use springbucks
db.createUser({
  user: "springbucks", 
  pwd: "springbucks", 
  roles: [{role: "readWrite", db: "springbucks"}]
})

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
│   │   │           └── MoneyReadConverter.java            # Money 轉換器
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

### 🔄 MoneyReadConverter 自定義轉換器

```java
@ReadingConverter
public class MoneyReadConverter implements Converter<NumberLong, Money> {
    @Override
    public Money convert(NumberLong source) {
        return Money.ofMinor(CurrencyUnit.of("TWD"), source.longValue());
    }
}
```

**功能說明：**
- 將 MongoDB 中的 `NumberLong` 轉換為 `Money` 對象
- 使用台幣（TWD）作為預設貨幣
- 自動處理小數位轉換

### ⚙️ MongoConfig 配置類

```java
@Configuration
public class MongoConfig {
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(new MoneyReadConverter()));
    }
}
```

**設計目的：**
- 解決循環依賴問題
- 集中管理 MongoDB 自定義配置
- 註冊自定義轉換器

## 🚀 運行結果示例

成功運行後，您會看到類似以下的輸出：

```log
2025-06-27 14:16:53.624 INFO --- Saved Coffee Coffee(id=..., name=espresso, price=TWD 100.00, createTime=...)
2025-06-27 14:16:53.630 INFO --- Saved Coffee Coffee(id=..., name=latte, price=TWD 150.00, createTime=...)
2025-06-27 14:16:54.649 INFO --- Coffee Coffee(id=..., name=latte, price=TWD 175.00, updateTime=...)
```

### 📊 執行流程

1. **CREATE**：建立兩筆咖啡記錄（espresso: TWD 100, latte: TWD 150）
2. **READ**：查詢並顯示所有咖啡記錄（按名稱排序）
3. **UPDATE**：更新 latte 價格從 TWD 150 → TWD 175
4. **DELETE**：刪除所有咖啡記錄

## 配置說明

### application.properties

```properties
# MongoDB 連線配置
spring.data.mongodb.uri=mongodb://springbucks:springbucks@localhost:27017/springbucks

# 日誌等級設定（可選）
logging.level.org.springframework.data.mongodb.core=DEBUG
```

### 環境變數配置（建議）

```bash
# 設定環境變數
export MONGO_HOST=localhost
export MONGO_PORT=27017
export MONGO_DB=springbucks
export MONGO_USER=springbucks
export MONGO_PASS=springbucks
```

```properties
# 在 application.properties 中使用環境變數
spring.data.mongodb.uri=mongodb://${MONGO_USER}:${MONGO_PASS}@${MONGO_HOST}:${MONGO_PORT}/${MONGO_DB}
```

## 故障排除

### 常見問題與解決方案

| 問題 | 症狀 | 解決方案 |
|-----|------|----------|
| **MongoDB 連線失敗** | `MongoTimeoutException` | 檢查容器是否運行：`docker ps` |
| **認證失敗** | `AuthenticationFailed` | 確認使用者已建立且密碼正確 |
| **循環依賴錯誤** | `BeanCurrentlyInCreationException` | 確保 `MongoConfig` 類存在 |
| **Money 轉換錯誤** | `ClassCastException` | 檢查 `MoneyReadConverter` 是否註冊 |

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
```

### 🔍 資料庫操作

```javascript
// 在 MongoDB shell 中執行

// 查看所有咖啡記錄
db.coffee.find().pretty()

// 查看特定咖啡
db.coffee.find({name: "latte"})

// 檢查資料庫統計
db.coffee.countDocuments()

// 清空咖啡集合
db.coffee.deleteMany({})
```

## 學習要點

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

**📅 最後更新：2025-06-27**  
**👨‍💻 維護者：風清雲談團隊**  
**⭐ 如果這個專案對您有幫助，請給我們一個 Star！** 