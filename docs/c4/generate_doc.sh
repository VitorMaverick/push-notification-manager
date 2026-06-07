#!/bin/bash
IMG_DIR="./img"
OUTPUT="documentacao_c4.md"

b64_img() {
    local file="$1"
    base64 -w 0 "$file"
}

cat > "$OUTPUT" << HEADER
# Documentação de Arquitetura C4 – Push Notification Manager

## Introdução

Este documento apresenta a arquitetura do sistema **Push Notification Manager** utilizando o modelo C4 (Context, Containers, Components, Code). Os diagramas foram gerados com PlantUML e a biblioteca C4-PlantUML.

O sistema permite o gerenciamento de notificações push, incluindo cadastro de dispositivos e envio de notificações via Firebase Cloud Messaging (FCM).

---

HEADER

# Nível 1 - Contexto
cat >> "$OUTPUT" << NIVEL1
## Nível 1 – Diagrama de Contexto

O diagrama de contexto mostra o sistema Push Notification Manager e suas interações com atores externos e sistemas externos.

\`\`\`plantuml
$(cat c4_nivel1_contexto.puml)
\`\`\`

Figura 1 – Diagrama de Contexto do Sistema (Nível 1)

![Diagrama de Contexto do Sistema](data:image/png;base64,$(b64_img "$IMG_DIR/c4_nivel1_contexto.png"))

---

NIVEL1

# Nível 2 - Contêineres
cat >> "$OUTPUT" << NIVEL2
## Nível 2 – Diagrama de Contêineres

O diagrama de contêineres detalha os componentes internos do sistema: Frontend UI, Backend API, Device Service, Notification Service, RabbitMQ e os bancos de dados PostgreSQL.

\`\`\`plantuml
$(cat c4_nivel2_containers.puml)
\`\`\`

Figura 2 – Diagrama de Contêineres (Nível 2)

![Diagrama de Contêineres](data:image/png;base64,$(b64_img "$IMG_DIR/c4_nivel2_containers.png"))

---

NIVEL2

# Nível 3 - Componentes do Monolith
cat >> "$OUTPUT" << NIVEL3A
## Nível 3 – Componentes do Backend API (monolith-ui)

Este diagrama detalha os componentes internos do Backend API (monolith-ui), incluindo controllers, services, repositories e configurações de segurança.

\`\`\`plantuml
$(cat c4_nivel3_monolith.puml)
\`\`\`

Figura 3 – Diagrama de Componentes do Backend API (Nível 3)

![Diagrama de Componentes do Backend API](data:image/png;base64,$(b64_img "$IMG_DIR/c4_nivel3_monolith.png"))

---

NIVEL3A

# Nível 3 - Componentes do Notification Service
cat >> "$OUTPUT" << NIVEL3B
## Nível 3 – Componentes do Notification Service

Este diagrama detalha os componentes do Notification Service seguindo a Arquitetura Hexagonal (Ports & Adapters), incluindo use cases, ports e adapters.

\`\`\`plantuml
$(cat c4_nivel3_notification.puml)
\`\`\`

Figura 4 – Diagrama de Componentes do Notification Service (Nível 3)

![Diagrama de Componentes do Notification Service](data:image/png;base64,$(b64_img "$IMG_DIR/c4_nivel3_notification.png"))

---

NIVEL3B

# Nível 4 - Diagrama de Classes Auth
cat >> "$OUTPUT" << NIVEL4A
## Nível 4 – Diagrama de Classes: Módulo de Autenticação

Este diagrama de classes detalha o módulo de autenticação do monolith-ui, incluindo o fluxo JWT, UserDetailsService, repositórios e padrões de projeto utilizados.

\`\`\`plantuml
$(cat c4_nivel4_auth.puml)
\`\`\`

Figura 5 – Diagrama de Classes do Módulo de Autenticação (Nível 4)

![Diagrama de Classes - Autenticação](data:image/png;base64,$(b64_img "$IMG_DIR/c4_nivel4_auth.png"))

---

NIVEL4A

# Nível 4 - Diagrama de Classes Async
cat >> "$OUTPUT" << NIVEL4B
## Nível 4 – Diagrama de Classes: Comunicação Assíncrona

Este diagrama de classes detalha a comunicação assíncrona (Event-Driven) entre o Device Service e o Notification Service via RabbitMQ, incluindo eventos, publishers e consumers.

\`\`\`plantuml
$(cat c4_nivel4_async.puml)
\`\`\`

Figura 6 – Diagrama de Classes da Comunicação Assíncrona (Nível 4)

![Diagrama de Classes - Comunicação Assíncrona](data:image/png;base64,$(b64_img "$IMG_DIR/c4_nivel4_async.png"))

---

NIVEL4B

# Instruções finais
cat >> "$OUTPUT" << FINAL
## Instruções para Regeneração dos Diagramas

Para regenerar as imagens a partir dos arquivos \`.puml\`:

\`\`\`bash
cd /home/vitor.maverick/lab/docs/c4
java -Xmx1024m -jar plantuml.jar -tpng -o ./img c4_nivel1_contexto.puml
java -Xmx1024m -jar plantuml.jar -tpng -o ./img c4_nivel2_containers.puml
java -Xmx1024m -jar plantuml.jar -tpng -o ./img c4_nivel3_monolith.puml
java -Xmx1024m -jar plantuml.jar -tpng -o ./img c4_nivel3_notification.puml
java -Xmx1024m -jar plantuml.jar -tpng -o ./img c4_nivel4_auth.puml
java -Xmx1024m -jar plantuml.jar -tpng -o ./img c4_nivel4_async.puml
\`\`\`

**Requisitos:**
- Java 11+ instalado
- Graphviz instalado (\`apt install graphviz\`)
- \`plantuml.jar\` presente no diretório

FINAL

echo "Documento gerado: $(realpath $OUTPUT)"
echo "Tamanho: $(du -h $OUTPUT | cut -f1)"
