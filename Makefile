# ─────────────────────────────────────────────────────────────────────────────
# Makefile — Orquestrador do ecossistema PushNotificationManager
# ─────────────────────────────────────────────────────────────────────────────
# Uso:
#   make all          — sobe tudo (docker + backend + frontend em background)
#   make dev          — sobe docker + frontend background + backend foreground (logs ao vivo)
#   make stop-all     — derruba tudo
#   make up           — sobe apenas contêineres (microsserviços + bancos + rabbitmq)
#   make down         — derruba contêineres
#   make run-backend  — inicia backend Spring Boot do monolito (background)
#   make run-backend-fg — inicia backend Spring Boot em foreground (logs ao vivo)
#   make run-frontend — inicia frontend do monolito (background)
#   make logs-backend — acompanha log do backend
#   make logs-frontend— acompanha log do frontend
#   make logs-docker  — acompanha logs dos contêineres
#   make clean-logs   — remove logs antigos
#   make status       — mostra estado dos serviços
# ─────────────────────────────────────────────────────────────────────────────

SHELL := /bin/bash
.PHONY: all dev up down build run-backend run-backend-fg run-frontend run-monolith stop-all \
        logs-backend logs-frontend logs-docker clean-logs status help

# ── Diretórios ───────────────────────────────────────────────────────────────
MONOLITH_DIR := monolith-ui
LOGS_DIR     := logs

# ── Logs ─────────────────────────────────────────────────────────────────────
BACKEND_LOG  := $(LOGS_DIR)/backend.log
FRONTEND_LOG := $(LOGS_DIR)/frontend.log

# ── Portas ───────────────────────────────────────────────────────────────────
BACKEND_PORT  := 8080
FRONTEND_PORT := 9060

# ── Funções ──────────────────────────────────────────────────────────────────
define check_port
	@if lsof -Pi :$(1) -sTCP:LISTEN -t >/dev/null 2>&1; then \
		echo "⚠  Porta $(1) já em uso. Serviço provavelmente já rodando."; \
		exit 1; \
	fi
endef

# ── Alvo padrão ──────────────────────────────────────────────────────────────
all: up run-monolith
	@echo ""
	@echo "══════════════════════════════════════════════════════════"
	@echo "  ✔  Ecossistema completo rodando!"
	@echo ""
	@echo "  Frontend monolito : http://localhost:$(FRONTEND_PORT)"
	@echo "  Backend monolito  : http://localhost:$(BACKEND_PORT)"
	@echo "  device-service    : http://localhost:8081"
	@echo "  notification-svc  : http://localhost:8082"
	@echo "  RabbitMQ UI       : http://localhost:15672"
	@echo ""
	@echo "  Logs em ./$(LOGS_DIR)/"
	@echo "  Para parar tudo: make stop-all"
	@echo "══════════════════════════════════════════════════════════"

# ── Dev (backend foreground com logs ao vivo) ────────────────────────────────
dev: up run-frontend
	@echo ""
	@echo "  Frontend monolito : http://localhost:$(FRONTEND_PORT)"
	@echo "  device-service    : http://localhost:8081"
	@echo "  notification-svc  : http://localhost:8082"
	@echo "  RabbitMQ UI       : http://localhost:15672"
	@echo ""
	@echo "☕ Iniciando backend em foreground (Ctrl+C para parar)..."
	@echo ""
	cd $(MONOLITH_DIR) && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dskip.npm=true -Denforcer.skip=true

# ── Cria diretório de logs ───────────────────────────────────────────────────
$(LOGS_DIR):
	mkdir -p $(LOGS_DIR)

# ── Docker Compose ───────────────────────────────────────────────────────────
build:
	@echo "🔨 Construindo imagens dos microsserviços..."
	docker compose build
	@echo "✔  Imagens prontas."

up:
	@echo "🐳 Subindo contêineres (microsserviços + infra)..."
	docker compose up -d --remove-orphans 2>&1 | tee /dev/stderr | grep -qi "error" && { echo "✘  Falha ao subir contêineres. Corrija os erros acima."; exit 1; } || true
	@EXPECTED=$$(docker compose config --services | wc -l); \
	RUNNING=$$(docker compose ps --status running -q | wc -l); \
	if [ "$$RUNNING" -lt "$$EXPECTED" ]; then \
		echo "✘  Apenas $$RUNNING/$$EXPECTED contêineres rodando:"; \
		docker compose ps; \
		exit 1; \
	fi
	@echo "✔  Todos os contêineres iniciados."
	@echo ""
	@echo "  device-service    : http://localhost:8081"
	@echo "  notification-svc  : http://localhost:8082"
	@echo "  RabbitMQ UI       : http://localhost:15672"
	@echo "  device-db         : localhost:5434"
	@echo "  notification-db   : localhost:5433"
	@echo ""
	@echo "  Logs: make logs-docker"
	@echo "  Monolito (frontend+backend): make run-monolith"

down:
	@echo "🐳 Derrubando contêineres..."
	docker compose down
	@echo "✔  Contêineres removidos."

# ── Monolito — Backend ───────────────────────────────────────────────────────
run-backend: | $(LOGS_DIR)
	$(call check_port,$(BACKEND_PORT))
	@echo "☕ Iniciando backend do monolito (Spring Boot)..."
	cd $(MONOLITH_DIR) && nohup ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dskip.npm=true -Denforcer.skip=true > ../$(BACKEND_LOG) 2>&1 &
	@echo "✔  Backend iniciado em background. Log: $(BACKEND_LOG)"

# ── Monolito — Backend (foreground) ───────────────────────────────────────────
run-backend-fg:
	$(call check_port,$(BACKEND_PORT))
	@echo "☕ Iniciando backend do monolito (foreground)..."
	cd $(MONOLITH_DIR) && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev -Dskip.npm=true -Denforcer.skip=true

# ── Monolito — Frontend ─────────────────────────────────────────────────────
run-frontend: | $(LOGS_DIR)
	$(call check_port,$(FRONTEND_PORT))
	@mkdir -p $(LOGS_DIR)
	@echo "⚛  Iniciando frontend do monolito (webpack-dev-server)..."
	@cd $(MONOLITH_DIR) && nohup ./npmw start > ../$(FRONTEND_LOG) 2>&1 &
	@echo "   Aguardando porta $(FRONTEND_PORT)..."
	@for i in $$(seq 1 60); do \
		if lsof -Pi :$(FRONTEND_PORT) -sTCP:LISTEN -t >/dev/null 2>&1; then \
			echo "✔  Frontend iniciado em background. Log: $(FRONTEND_LOG)"; \
			exit 0; \
		fi; \
		if ! pgrep -f "npmw start" >/dev/null 2>&1 && ! pgrep -f "webpack" >/dev/null 2>&1; then \
			echo "✘  Frontend morreu antes de ficar pronto."; \
			echo "   Verifique o log: $(FRONTEND_LOG)"; \
			tail -5 $(FRONTEND_LOG) 2>/dev/null; \
			exit 1; \
		fi; \
		sleep 2; \
	done; \
	echo "✘  Frontend não respondeu na porta $(FRONTEND_PORT) após 120s."; \
	echo "   Verifique o log: $(FRONTEND_LOG)"; \
	tail -5 $(FRONTEND_LOG) 2>/dev/null; \
	exit 1

# ── Monolito completo (paralelo com make -j2) ────────────────────────────────
run-monolith:
	@$(MAKE) -j2 run-backend run-frontend
	@echo "✔  Monolito completamente iniciado."

# ── Parar tudo ───────────────────────────────────────────────────────────────
stop-all:
	@echo "🐳 Derrubando contêineres..."
	@docker compose down
	@echo "🛑 Parando processos do monolito..."
	-@lsof -ti :$(BACKEND_PORT) -sTCP:LISTEN | xargs kill 2>/dev/null || true
	-@lsof -ti :$(FRONTEND_PORT) -sTCP:LISTEN | xargs kill 2>/dev/null || true
	@echo "✔  Todos os serviços parados."

# ── Logs ─────────────────────────────────────────────────────────────────────
logs-backend:
	@tail -f $(BACKEND_LOG)

logs-frontend:
	@tail -f $(FRONTEND_LOG)

logs-docker:
	@docker compose logs -f

clean-logs:
	rm -f $(LOGS_DIR)/*.log
	@echo "✔  Logs limpos."

# ── Status ───────────────────────────────────────────────────────────────────
status:
	@echo "── Contêineres Docker ──"
	@docker compose ps
	@echo ""
	@echo "── Processos do Monolito ──"
	@ps aux | grep -E "(spring-boot:run|npmw start|webpack)" | grep -v grep || echo "  Nenhum processo ativo."

# ── Help ─────────────────────────────────────────────────────────────────────
help:
	@echo "Alvos disponíveis:"
	@echo "  all            — Sobe tudo em background (docker + monolito)"
	@echo "  dev            — Sobe docker + frontend bg + backend foreground (logs ao vivo)"
	@echo "  stop-all       — Para tudo"
	@echo "  up             — Sobe contêineres Docker"
	@echo "  down           — Derruba contêineres Docker"
	@echo "  run-backend    — Inicia backend Spring Boot (background)"
	@echo "  run-backend-fg — Inicia backend Spring Boot (foreground)"
	@echo "  run-frontend   — Inicia frontend webpack (background)"
	@echo "  run-monolith   — Inicia backend + frontend (paralelo, background)"
	@echo "  logs-backend   — Acompanha log do backend"
	@echo "  logs-frontend  — Acompanha log do frontend"
	@echo "  logs-docker    — Acompanha logs Docker"
	@echo "  clean-logs     — Remove logs antigos"
	@echo "  status         — Estado dos serviços"
