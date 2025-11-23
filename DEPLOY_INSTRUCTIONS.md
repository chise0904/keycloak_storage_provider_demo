# Keycloak External User Storage Provider - 完整部署指南

## 📋 前置需求

1. **Java 17+** 和 **Maven 3.6+**（用於建置）
2. **Docker** 和 **Docker Compose**（用於執行）
3. **Git**（選用，用於版本控制）

## 🚀 快速部署步驟

### 步驟 1: 建置 Provider

```bash
cd /path/to/keycloak_storage_provider
./build.sh
```

預期輸出：
```
✅ Build successful!
JAR file: target/external-user-storage-provider-1.0.0.jar
```

**如果沒有 Maven**：
```bash
# macOS
brew install maven

# Ubuntu/Debian
sudo apt-get update && sudo apt-get install maven

# RHEL/CentOS
sudo yum install maven
```

### 步驟 2: 部署到 Keycloak

```bash
./deploy.sh
```

這會將 JAR 檔案複製到 `./keycloak-providers/` 目錄。

### 步驟 3: 啟動 Docker 環境

```bash
docker-compose up -d
```

**啟動的服務**：
- ✅ Traefik (反向代理，SSL)
- ✅ keycloak-postgres (Keycloak 資料庫)
- ✅ external-user-db (外部使用者資料庫)
- ✅ keycloak (主服務)
- ✅ demo-app (示範應用)

### 步驟 4: 等待 Keycloak 啟動

```bash
docker-compose logs -f keycloak
```

等待看到：
```
Keycloak 23.0.0 (WildFly Core ...) started in ...ms
```

按 `Ctrl+C` 停止查看日誌。

### 步驟 5: 訪問 Keycloak Admin Console

開啟瀏覽器：
```
URL: https://auth.lovejulian.shop
Username: admin
Password: admin123
```

**⚠️ 如果無法訪問**：
- 確認 DNS 記錄：`auth.lovejulian.shop` 和 `app.lovejulian.shop` 指向你的伺服器 IP
- 或使用 `/etc/hosts` 設定本地測試：
  ```bash
  echo "127.0.0.1 auth.lovejulian.shop app.lovejulian.shop" | sudo tee -a /etc/hosts
  ```

### 步驟 6: 設定 User Federation

1. **登入 Keycloak Admin Console**

2. **選擇或建立 Realm**
   - 預設使用 `master` realm
   - 或建立新的 realm（推薦用於生產環境）

3. **前往 User Federation**
   - 左側選單：`User Federation`

4. **新增 Provider**
   - 點擊：`Add provider`
   - 選擇：`external-user-storage`

5. **設定參數**：
   ```
   Display Name: External User Database
   Enabled: ON

   Database Host: external-user-db
   Database Port: 5432
   Database Name: userdb
   Database User: userapp
   Database Password: userapp_password
   ```

6. **儲存設定**
   - 點擊：`Save`
   - 如果看到綠色成功訊息，表示連線成功！

### 步驟 7: 測試登入

1. **前往測試登入頁面**：
   ```
   https://auth.lovejulian.shop/realms/master/account
   ```

2. **使用測試帳號登入**：
   ```
   Username: testuser1
   Password: password123
   ```

   或使用其他測試帳號：
   ```
   johndoe / password123
   janedoe / password123
   ```

3. **成功登入** ✅
   - 你應該會看到使用者帳號頁面
   - 顯示使用者資訊（從外部資料庫讀取）

## 🔍 驗證部署

### 檢查 Provider 是否載入

```bash
docker exec keycloak ls -l /opt/keycloak/providers/
```

應該看到：
```
-rw-r--r-- 1 keycloak keycloak ... external-user-storage-provider-1.0.0.jar
```

### 檢查 Keycloak 日誌

```bash
docker-compose logs keycloak | grep -i "external"
```

應該看到：
```
... External User Storage Provider initialized for model: External User Database
... Database configuration: external-user-db:5432/userdb
```

### 測試外部資料庫連線

```bash
docker exec external-user-db psql -U userapp -d userdb -c "SELECT username, email FROM users;"
```

應該看到：
```
  username  |         email
------------+------------------------
 testuser1  | testuser1@example.com
 testuser2  | testuser2@example.com
 johndoe    | john.doe@example.com
 janedoe    | jane.doe@example.com
 admin      | admin@example.com
```

## 🧪 測試帳號資訊

| Username   | Email                    | Password     | First Name | Last Name |
|------------|--------------------------|--------------|------------|-----------|
| testuser1  | testuser1@example.com    | password123  | Test       | User One  |
| testuser2  | testuser2@example.com    | password123  | Test       | User Two  |
| johndoe    | john.doe@example.com     | password123  | John       | Doe       |
| janedoe    | jane.doe@example.com     | password123  | Jane       | Doe       |
| admin      | admin@example.com        | password123  | Admin      | User      |

## 🛠️ 故障排除

### Provider 沒有出現在 User Federation

**檢查 JAR 檔案**：
```bash
docker exec keycloak ls -l /opt/keycloak/providers/
```

**重新部署**：
```bash
./deploy.sh
docker-compose restart keycloak
```

**檢查錯誤日誌**：
```bash
docker-compose logs keycloak | grep -i error
```

### 資料庫連線失敗

**測試網路連線**：
```bash
docker exec keycloak ping -c 3 external-user-db
```

**檢查資料庫是否運行**：
```bash
docker-compose ps external-user-db
```

**驗證資料庫憑證**：
```bash
docker exec external-user-db psql -U userapp -d userdb -c "SELECT 1"
```

### 登入失敗

**檢查使用者是否存在**：
```bash
docker exec external-user-db psql -U userapp -d userdb -c \
  "SELECT username, email, enabled FROM users WHERE username='testuser1';"
```

**檢查密碼哈希**：
```bash
docker exec external-user-db psql -U userapp -d userdb -c \
  "SELECT username, password_hash FROM users WHERE username='testuser1';"
```

應該看到：`{SHA256}75K3eLr+dx6JJFuJ7LwIpEpOFmwGZZkRiB84PURz6U8=`

**查看認證日誌**：
```bash
docker-compose logs keycloak | grep -i "validating credentials"
```

### SSL 憑證問題

**使用 Let's Encrypt 自動憑證**：
確保 `docker-compose.yml` 中的 email 已設定：
```yaml
- "--certificatesresolvers.myresolver.acme.email=your-email@example.com"
```

**本地開發使用 HTTP**：
在 `docker-compose.yml` 中註解掉 HTTPS 相關設定，使用 HTTP：
```
http://auth.lovejulian.shop
```

## 📊 監控和維護

### 查看即時日誌

```bash
# 所有服務
docker-compose logs -f

# 僅 Keycloak
docker-compose logs -f keycloak

# 僅外部資料庫
docker-compose logs -f external-user-db
```

### 重啟服務

```bash
# 重啟 Keycloak
docker-compose restart keycloak

# 重啟所有服務
docker-compose restart

# 停止並移除容器
docker-compose down

# 完全清理（包含資料）
docker-compose down -v
```

### 備份外部資料庫

```bash
docker exec external-user-db pg_dump -U userapp userdb > backup_$(date +%Y%m%d).sql
```

### 還原資料庫

```bash
cat backup_20250123.sql | docker exec -i external-user-db psql -U userapp -d userdb
```

## 🔐 安全性建議

### 生產環境清單

- [ ] 更改預設密碼（Keycloak admin、資料庫）
- [ ] 使用 BCrypt 替代 SHA-256 密碼哈希
- [ ] 啟用資料庫 SSL/TLS 連線
- [ ] 設定防火牆規則
- [ ] 實施 IP 白名單
- [ ] 啟用審計日誌
- [ ] 定期備份資料庫
- [ ] 使用 secrets 管理工具（Vault、AWS Secrets Manager）
- [ ] 設定監控和告警（Prometheus、Grafana）
- [ ] 實施速率限制和 CAPTCHA

## 📝 下一步

1. **整合應用程式**
   - 設定 OAuth2/OIDC 客戶端
   - 實作前端登入流程

2. **擴展功能**
   - 新增角色和權限映射
   - 實作使用者屬性同步
   - 新增群組支援

3. **效能優化**
   - 啟用快取機制
   - 實作連線池
   - 設定資料庫索引

## 🆘 取得協助

- **Keycloak 官方文件**: https://www.keycloak.org/docs/
- **PostgreSQL 文件**: https://www.postgresql.org/docs/
- **Docker Compose 文件**: https://docs.docker.com/compose/

## ✅ 部署確認清單

完成部署後，確認以下項目：

- [ ] Maven 建置成功
- [ ] JAR 檔案已部署到 Keycloak
- [ ] Docker 容器全部運行
- [ ] Keycloak Admin Console 可訪問
- [ ] Provider 出現在 User Federation
- [ ] 資料庫連線測試成功
- [ ] 測試帳號可以登入
- [ ] 日誌沒有錯誤訊息
- [ ] SSL 憑證正常運作（生產環境）

恭喜！你的 Keycloak External User Storage Provider 已成功部署！🎉
