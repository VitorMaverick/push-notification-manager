# monolith-ui

> ⚠️ Este é apenas o componente monolito (gateway + frontend). O sistema completo inclui microsserviços e infraestrutura; veja a documentação geral em **[../README.md](../README.md)**.

Backend Spring Boot + Frontend React gerado com [JHipster 9](https://www.jhipster.tech/documentation-archive/v9.0.0-beta.0). Responsável por:

- Autenticação JWT e gestão de usuários
- Servir o frontend React (SPA)
- Hospedar o Service Worker para recebimento de push
- Rotear requisições da UI para os microsserviços

---

## Executando em separado

### Pré-requisitos

- Java 21 (`.sdkmanrc` incluso: `sdk env`)
- Node.js 18+
- PostgreSQL do monolito rodando (via Docker Compose raiz ou `src/main/docker/postgresql.yml`)

### Backend

```bash
# Com frontend (build completo)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Sem rebuild do frontend (mais rápido em restarts)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dskip.npm=true -Denforcer.skip=true
```

Porta: `http://localhost:8080`

### Frontend (dev server separado com hot-reload)

```bash
./npmw start
```

Porta: `http://localhost:9060` (proxy para backend em 8080)

---

## Estrutura de pacotes

```
src/main/java/br/edu/acad/ifma/
├── config/                  # Configurações (Security, JWT, CorrelationIdFilter, RestTemplate)
├── model/                   # Entidades JPA (User, Authority)
├── repository/              # Repositórios Spring Data
├── security/                # Filtros de segurança, JWT encoder/decoder
├── service/                 # Serviços de negócio (UserService, MailService)
├── service/dto/             # DTOs (AdminUserDTO)
├── service/mapper/          # MapStruct mappers
└── web/rest/                # Controllers REST (AccountResource, UserResource)
```

---

## Configuração

Variáveis importantes (`application-dev.yml` ou environment):

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `SPRING_DATASOURCE_URL` | URL do PostgreSQL | `jdbc:postgresql://localhost:5432/monolithUi` |
| `JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET` | Chave JWT (compartilhada com microsserviços) | definida em `.env` |
| `SERVER_PORT` | Porta do backend | 8080 |

---

## Build

```bash
# Build de produção (JAR com frontend embutido)
./mvnw -Pprod clean verify

# Imagem Docker
npm run java:docker

# Testes
./mvnw verify              # backend
npm test                   # frontend (Jest)
npm run e2e                # end-to-end (Cypress)
```

---

## Troubleshooting

- **Frontend mostra "An error has occurred"**: limpe cache webpack: `rm -rf target/webpack/`
- **Service Worker não registra**: verifique que `http://localhost:8080/firebase-messaging-sw.js` retorna 200
- **JWT rejeitado pelos microsserviços**: garanta que a mesma `JWT_BASE64_SECRET` está em todos os serviços
