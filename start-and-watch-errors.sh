#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# start-and-watch-errors.sh
# Inicia o ecossistema Push Notification Manager e monitora erros silenciosamente.
# Erros são registrados em ~/lab/error_reports/ e notificados via notify-send.
# ─────────────────────────────────────────────────────────────────────────────
set -uo pipefail

LAB_DIR="$HOME/lab"
REPORT_DIR="$LAB_DIR/error_reports"
LOGS_DIR="$LAB_DIR/logs"
REPORT_FILE="$REPORT_DIR/error_$(date +%Y%m%d_%H%M%S).log"
PIDS=()

mkdir -p "$REPORT_DIR" "$LOGS_DIR"
cd "$LAB_DIR"

# ── Cleanup ao sair ─────────────────────────────────────────────────────────
cleanup() {
    echo -e "\n🛑 Parando monitoramento..."
    for pid in "${PIDS[@]}"; do
        kill "$pid" 2>/dev/null
    done
    wait 2>/dev/null

    echo "🐳 Derrubando serviços..."
    make stop-all 2>/dev/null
    echo "✔  Encerrado. Relatório de erros: $REPORT_FILE"
    exit 0
}
trap cleanup SIGINT SIGTERM

# ── Iniciar serviços ─────────────────────────────────────────────────────────
echo "🚀 Iniciando serviços..."
make all
echo ""

# Aguardar logs existirem
for i in $(seq 1 30); do
    [[ -f "$LOGS_DIR/backend.log" ]] && break
    sleep 2
done

# ── Função de processamento de erros ─────────────────────────────────────────
process_errors() {
    local source="$1"
    while IFS= read -r line; do
        if echo "$line" | grep -qEi 'ERROR|Exception|FATAL'; then
            local ts
            ts=$(date '+%Y-%m-%d %H:%M:%S')
            echo "[$ts] [$source] $line" >> "$REPORT_FILE"
            if command -v notify-send &>/dev/null; then
                notify-send -u critical "Erro: $source" "$(echo "$line" | head -c 200)" 2>/dev/null || true
            fi
        fi
    done
}

# ── Iniciar monitoramento silencioso ─────────────────────────────────────────
# Docker logs (microsserviços, bancos, rabbitmq)
docker compose logs -f --no-color 2>/dev/null | process_errors "docker" &
PIDS+=($!)

# Backend do monolito
if [[ -f "$LOGS_DIR/backend.log" ]]; then
    tail -f "$LOGS_DIR/backend.log" 2>/dev/null | process_errors "backend" &
    PIDS+=($!)
fi

# Frontend do monolito
if [[ -f "$LOGS_DIR/frontend.log" ]]; then
    tail -f "$LOGS_DIR/frontend.log" 2>/dev/null | process_errors "frontend" &
    PIDS+=($!)
fi

echo "══════════════════════════════════════════════════════════"
echo "  👁  Monitoramento de erros ativo"
echo "  📄 Relatório: $REPORT_FILE"
echo "  🛑 Pressione Ctrl+C para parar e derrubar os serviços"
echo "══════════════════════════════════════════════════════════"

# Aguardar indefinidamente (até Ctrl+C)
wait
