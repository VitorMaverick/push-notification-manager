# Push Notification Manager – Ambiente de Desenvolvimento

Sistema completo de gerenciamento de notificações push para navegadores web, desenvolvido como Trabalho de Conclusão de Curso no **IFMA (Instituto Federal do Maranhão)**.

O projeto aplica **Arquitetura Hexagonal**, **padrões de projeto GoF** e **arquitetura orientada a eventos** em uma estrutura de microsserviços.

---

## Estrutura de diretórios

```
~/lab/
├── monolith-ui/              # Frontend React + Backend Spring Boot (autenticação JWT, gateway)
├── device-ms/                # Microsserviço de dispositivos (registro de tokens FCM)
├── notification-ms/          # Microsserviço de notificações (envio push via Firebase)
├── docker-compose.yml        # Orquestração: microsserviços, bancos PostgreSQL, RabbitMQ
├── Makefile                  # Comandos unificados (make all, make stop-all, etc.)
├── start-and-watch-errors.sh # Script de inicialização com monitoramento de erros
├── logs/                     # Logs do backend e frontend do monolito
├── error_reports/            # Relatórios de erros capturados pelo script de monitoramento
└── docs/c4/                  # Diagramas C4 (PlantUML + imagens PNG)
```

---

## Arquitetura

O sistema evoluiu de um monolito JHipster para uma **arquitetura híbrida**:

| Serviço | Porta | Responsabilidade | Banco |
|---------|-------|------------------|-------|
| **monolith-ui** (Gateway) | 8080 | Autenticação JWT, gestão de usuários, frontend React | PostgreSQL :5432 |
| **device-service** | 8081 | Registro e gestão de tokens FCM | PostgreSQL :5434 |
| **notification-service** | 8082 | Envio push via Firebase, histórico, retry/DLQ | PostgreSQL :5433 |
| **RabbitMQ** | 5672 / 15672 | Mensageria assíncrona entre microsserviços | — |

### Comunicação

- Frontend ↔ Backend monolito: REST local (porta 8080)
- Backend monolito → microsserviços: REST síncrono (com timeouts: connect 3s / read 5s)
- device-ms → notification-ms: assíncrona via RabbitMQ (eventos `DeviceRegisteredEvent`)
- notification-ms → Firebase: HTTPS (API v1), processado de forma assíncrona (@Async)

### Melhorias implementadas

- **X-Correlation-ID**: propagado em todas as chamadas REST e injetado no MDC para rastreabilidade nos logs
- **Timeouts**: RestTemplate configurado com connect=3s, read=5s para evitar bloqueios
- **Endpoint assíncrono**: `POST /api/notifications` retorna 202 Accepted imediatamente; envio ao Firebase em background via ThreadPool

---

## Pré-requisitos

| Ferramenta | Versão | Notas |
|------------|--------|-------|
| Java | 21 | Use SDKMAN: `sdk use java 21.0.2-open` |
| Node.js | 18+ LTS | Para o frontend React |
| Docker + Docker Compose | recente | Microsserviços, bancos, RabbitMQ |
| Graphviz | qualquer | Para renderizar diagramas PlantUML |

---

## Como iniciar o sistema completo

### Opção 1 – Makefile (recomendado)

```bash
cd ~/lab
make all
```

Sobe tudo em background (Docker Compose + backend + frontend). Para acompanhar logs:

```bash
make logs-backend   # log do backend Spring Boot
make logs-docker    # logs dos contêineres
make status         # estado dos serviços
```

### Opção 2 – Script com monitoramento de erros

```bash
cd ~/lab
./start-and-watch-errors.sh
```

Sobe tudo e monitora silenciosamente. Erros são gravados em `error_reports/`. Para acompanhar:

```bash
tail -f ~/lab/error_reports/error_*.log
```

### Parar o sistema

```bash
make stop-all       # ou Ctrl+C no script de monitoramento
```

---

## Monitoramento de erros

O script `start-and-watch-errors.sh`:

1. Executa `make all` (inicia todo o ecossistema)
2. Captura logs de todos os serviços (Docker + backend + frontend) em background
3. Filtra linhas com `ERROR`, `Exception` ou `FATAL`
4. Grava cada erro com timestamp e origem em `~/lab/error_reports/error_YYYYMMDD_HHMMSS.log`
5. Envia notificação desktop via `notify-send` (se disponível no Linux)
6. Ao pressionar Ctrl+C, derruba todos os serviços

---

## Comandos úteis do Makefile

| Comando | Descrição |
|---------|-----------|
| `make all` | Sobe tudo em background |
| `make dev` | Docker + frontend bg + backend foreground (logs ao vivo) |
| `make stop-all` | Derruba tudo |
| `make up` | Apenas contêineres Docker |
| `make down` | Derruba contêineres |
| `make run-backend` | Backend Spring Boot em background |
| `make run-frontend` | Frontend webpack em background |
| `make logs-backend` | Acompanha log do backend |
| `make logs-docker` | Acompanha logs Docker |
| `make status` | Estado dos serviços |
| `make clean-logs` | Remove logs antigos |

---

## Testando o ciclo completo de notificação push

1. Abra `http://localhost:8080` e faça login (`admin`/`admin`)
2. Clique em **"Obtain FCM Token From Browser"** → permita notificações
3. Preencha título/corpo e clique **"Send Notification"**
4. O navegador exibe a notificação nativa; o Service Worker envia ACK automaticamente
5. Verifique o histórico: `curl -H "Authorization: Bearer <token>" http://localhost:8082/api/notifications`

Status esperados: `PENDING → SENT → DELIVERED`

---

## Diagramas e documentação técnica

Os diagramas C4 (níveis 1 a 4) e diagramas de sequência estão em `docs/c4/`:

```bash
ls docs/c4/img/   # imagens PNG renderizadas
ls docs/c4/*.puml # fontes PlantUML
```

Para regenerar:

```bash
cd docs/c4
java -jar plantuml.jar -tpng -o ./img *.puml
```

---

## Contexto acadêmico

**Título:** _Utilização de Padrões de Projeto Arquiteturais e de Design na Implementação de Sistemas de Notificação Push_

**Instituição:** IFMA — Instituto Federal de Educação, Ciência e Tecnologia do Maranhão

**Curso:** Bacharelado em Sistemas de Informação

**Orientador:** Prof. Dr. Helder Pereira Borges

**Autor:** Vitor Maverick Fonseca dos Santos
