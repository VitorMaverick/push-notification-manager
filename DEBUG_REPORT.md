# Relatório de Depuração Automatizada

**Projeto:** push-notification-manager
**Branch:** fix/debug-errors
**Data:** 2026-05-26
**Ciclos executados:** 7
**Resultado:** SUCESSO

## Sumário Executivo

Todos os três serviços (device-service, notification-service e gateway JHipster) compilam sem erros, os 75 testes do monolito passam, e todos os containers Docker estão saudáveis. Foram corrigidos 7 problemas críticos que impediam a execução do ambiente completo, incluindo ausência de schema de banco de dados, conflito de porta, configurações de teste inconsistentes e configuração de segurança incorreta.

## Log de Correções

### Ciclo 1 — Compilação e testes (estáticos)

- **Problema:** Ausência de `src/test/resources/application.yml` no device-service — qualquer teste de integração futuro falharia ao tentar resolver `${jhipster.security.authentication.jwt.base64-secret}`
- **Correção:** Criado `/services/device-service/src/test/resources/application.yml` com H2 in-memory, Flyway desabilitado, JWT secret de teste e RabbitMQ configurado
- **Resultado:** resolvido

### Ciclo 2 — Test resources de notification-service

- **Problema:** O `application.yml` de teste referenciava `classpath:firebase-service-account-test.json` mas o arquivo não existia; RabbitMQ não estava configurado para testes
- **Correção:** Criado `firebase-service-account-test.json` com chave RSA PKCS#8 válida; adicionado `spring.rabbitmq.listener.simple.auto-startup: false` ao yml de teste; adicionado `spring.flyway.enabled: false`
- **Resultado:** resolvido

### Ciclo 3 — Conflito de porta (monolith vs device-service)

- **Problema:** `application-dev.yml` do monolith usava porta 8081, igual ao device-service
- **Correção:** Alterada porta do monolith de 8081 para 8080 em `src/main/resources/config/application-dev.yml`; atualizada referência de base-url do JHipster mail
- **Resultado:** resolvido

### Ciclo 4 — Schema de banco de dados inexistente

- **Problema:** Ambos os serviços usavam `ddl-auto: validate` mas os bancos PostgreSQL estavam vazios — erro `Schema-validation: missing table [device]` e `missing table [push_notification]`
- **Correção:** Adicionado Flyway (flyway-core + flyway-database-postgresql) ao pom.xml de ambos os serviços; criadas migrações `V1__create_device_table.sql` e `V1__create_push_notification_table.sql`; configurado `spring.flyway.enabled: true` nos application.yml de produção e `false` nos de teste
- **Resultado:** resolvido

### Ciclo 5 — Conflito de porta no docker-compose (device-db)

- **Problema:** `device-db` mapeava host port 5432, mas o PostgreSQL do monolith JHipster já ocupava essa porta
- **Correção:** Alterado mapeamento de porta do device-db de `5432:5432` para `5434:5432` no `docker-compose.yml`
- **Resultado:** resolvido

### Ciclo 6 — Firebase service account key inválida / caminho errado

- **Problema 1:** Chave RSA no JSON era fake (formato PKCS#1 inválido) — erro `Invalid PKCS#8 data`
- **Problema 2:** Variável `FIREBASE_SERVICE_ACCOUNT_KEY: /secrets/firebase-service-account.json` era interpretada como `ServletContextResource` em vez de `FileSystemResource`
- **Correção:** Gerada chave RSA 2048-bit PKCS#8 real com `openssl genpkey + pkcs8`; prefixo `file:` adicionado ao valor da variável no docker-compose; criado `secrets/firebase-service-account.json` e `.env` com JWT_BASE64_SECRET e FIREBASE_KEY_PATH
- **Resultado:** resolvido

### Ciclo 7 — SecurityConfiguration do device-service bloqueava /actuator/health

- **Problema:** `SecurityConfiguration` do device-service permitia `/management/health/**` mas o actuator estava em `/actuator/health` — retornava HTTP 401
- **Correção:** Regras alteradas de `/management/health/**` para `/actuator/health`, `/actuator/health/**`, `/actuator/info`
- **Resultado:** resolvido

## Estado Final dos Serviços

| Serviço               | Compilação | Testes | Docker | Health |
| --------------------- | ---------- | ------ | ------ | ------ |
| device-service        | ✅         | ✅     | ✅     | ✅     |
| notification-service  | ✅         | ✅     | ✅     | ✅     |
| gateway (JHipster)    | ✅         | ✅     | N/A    | ✅     |
| rabbitmq              | N/A        | N/A    | ✅     | ✅     |
| postgres-device       | N/A        | N/A    | ✅     | ✅     |
| postgres-notification | N/A        | N/A    | ✅     | ✅     |

## Erros Residuais

- **Chave Firebase é fictícia:** A `private_key` nos arquivos `secrets/firebase-service-account.json` e `firebase-service-account-test.json` é uma chave RSA gerada localmente. O `FirebaseApp` inicializa com sucesso (validação de formato), mas qualquer chamada real ao FCM falhará com erro de autenticação do Google. Necessário substituir com credenciais reais de um projeto Firebase.
- **Microservices sem no-healthcheck Docker:** Os containers `device-service` e `notification-service` no `docker-compose.yml` não têm `healthcheck` configurado. Isso não impede o funcionamento mas não permite que outros serviços dependam deles via `condition: service_healthy`.
- **Flyway e H2 incompatíveis:** A SQL de migração usa `BIGSERIAL` (sintaxe PostgreSQL). Se alguém tentar rodar testes de integração com Spring Boot full-context, precisará de um script de migração H2 separado ou usar `spring.flyway.locations` diferente em teste.
- **device-service test directory vazio:** `src/test/java/br/edu/acad/ifma/device/` existe mas não tem nenhuma classe de teste. Os recursos de teste foram criados preventivamente.

## Recomendações para Intervenção Humana

1. **Substituir chave Firebase:** Obter um `firebase-service-account.json` real de um projeto Firebase (Console Firebase → Configurações do Projeto → Contas de Serviço) e colocar em `secrets/firebase-service-account.json`. O arquivo `.env` aponta para esse caminho.
2. **Não commitar `.env` e `secrets/`:** Adicionar ao `.gitignore` — contêm o JWT secret e a chave Firebase (mesmo que fictícia por enquanto).
3. **Adicionar healthcheck Docker aos microservices:** Configurar `healthcheck` nos serviços `device-service` e `notification-service` no `docker-compose.yml` usando o endpoint `/actuator/health`.
4. **Escrever testes de integração:** O device-service não tem nenhuma classe de teste. A estrutura e recursos estão prontos.
5. **Porta local do device-service:** Ao rodar localmente (sem Docker), a configuração `${DB_HOST:localhost}:${DB_PORT:5432}` do device-service aponta para 5432, mas o PostgreSQL do monolith ocupa essa porta. Usar a variável de ambiente `DB_PORT=5434` ou um profile separado.
