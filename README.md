# 💳 Payment Service — Microsserviço de Pagamentos

Microsserviço responsável pelo **processamento de pagamentos e gerenciamento de falhas**, implementando mecanismos de idempotência, retentativas automáticas e recuperação de mensagens por meio de Dead Letter Queue (DLQ).

O serviço utiliza comunicação assíncrona via RabbitMQ para processar solicitações de pagamento e publicar os respectivos resultados para outros microsserviços.

## 🏛️ Arquitetura e Padrões

### Idempotência de Consumo

Para evitar o processamento duplicado de pagamentos, o serviço implementa um mecanismo de idempotência baseado no identificador do pedido (`orderId`).

Antes de iniciar o processamento, o serviço verifica se o evento já foi registrado na tabela `tb_processed_events`.

- Caso o evento seja novo, o processamento é iniciado.
- Caso o evento já tenha sido processado, a mensagem é ignorada e um warning é registrado no log.

Esse mecanismo evita que mensagens duplicadas provoquem operações financeiras repetidas.

### Resiliência e Dead Letter Queue (DLQ)

O serviço implementa uma estratégia de retentativas automáticas para lidar com falhas durante o processamento das mensagens.

**Estratégia de recuperação:**

1. Ao ocorrer uma falha, o RabbitMQ inicia o mecanismo de retentativa.
2. São realizadas até 3 tentativas adicionais de processamento.
3. Entre as tentativas, é aplicado um intervalo progressivo utilizando *Exponential Backoff*.
4. Caso todas as tentativas sejam esgotadas, a exceção `AmqpRejectAndDontRequeueException` é lançada.
5. A mensagem é encaminhada automaticamente para a Dead Letter Queue `payment.process.dlq`.

Esse mecanismo evita ciclos infinitos de reprocessamento e permite isolar mensagens que apresentam falhas persistentes.

### DLQ Reprocessor (Parking Lot)

O serviço disponibiliza um mecanismo operacional para recuperar mensagens armazenadas na DLQ.

Por meio de um serviço de reprocessamento, é possível:

- Consultar e recuperar mensagens retidas na DLQ.
- Limitar a quantidade de mensagens reprocessadas por operação.
- Reencaminhar mensagens para a fila principal.
- Permitir novas tentativas de processamento após a correção de falhas.

Esse mecanismo facilita a recuperação de mensagens sem necessidade de intervenção direta no RabbitMQ.

### Evento de Resposta de Pagamento

Após concluir o processamento, o serviço publica um evento `PaymentProcessedEvent` na exchange `payments.exchange`.

O evento comunica o resultado do processamento ao `order-service`, permitindo a atualização do status do pedido.

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Finalidade |
|---|---|
| Java 21 | Linguagem de programação |
| Spring Boot 3 | Framework principal |
| Spring Data JPA | Persistência de dados |
| PostgreSQL | Banco de dados relacional |
| Spring AMQP | Integração com RabbitMQ |
| RabbitMQ | Mensageria e comunicação assíncrona |
| JUnit 5 | Testes automatizados |
| Testcontainers | Testes de integração com infraestrutura real |

---

## 🚀 Endpoints Operacionais

### Reprocessar Mensagens da DLQ

`POST /payments/dlq/reprocess?limit=10`

Endpoint responsável por iniciar o reprocessamento de mensagens armazenadas na Dead Letter Queue.

**Parâmetros de Query**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `limit` | Integer | Quantidade máxima de mensagens a serem reprocessadas |

**Exemplo de requisição**

```http
POST /payments/dlq/reprocess?limit=10
```

**Response — `200 OK`**

```json
{
  "message": "Processo de reprocessamento da DLQ concluído",
  "reprocessedMessagesCount": 1
}
```

---

## 🐇 Estrutura do RabbitMQ

### Fila Principal

Responsável pelo recebimento das solicitações de processamento de pagamento.

| Configuração | Valor |
|---|---|
| Queue | `payment.process.queue` |

### Dead Letter Queue (DLQ)

Responsável por armazenar mensagens que falharam após o esgotamento das tentativas de processamento.

| Configuração | Valor |
|---|---|
| Dead Letter Exchange (DLX) | `payment.dlx` |
| Dead Letter Queue | `payment.process.dlq` |

### Exchange de Resposta

Responsável pela publicação dos resultados do processamento de pagamentos.

| Configuração | Valor |
|---|---|
| Exchange | `payments.exchange` |
| Evento publicado | `PaymentProcessedEvent` |

---

## 🔄 Fluxo de Processamento

1. O `payment-service` recebe uma mensagem na fila `payment.process.queue`.
2. O serviço verifica na tabela `tb_processed_events` se o `orderId` já foi processado.
3. Caso seja uma mensagem inédita, o processamento do pagamento é iniciado.
4. Em caso de sucesso, o resultado é registrado e o evento `PaymentProcessedEvent` é publicado na exchange `payments.exchange`.
5. Em caso de falha, são iniciadas até 3 retentativas totais com Exponential Backoff.
6. Se todas as tentativas falharem, a mensagem é encaminhada para a DLQ.
7. Quando necessário, o operador pode utilizar o endpoint de reprocessamento para recuperar mensagens retidas.

---

## 🧪 Estratégia de Testes

O projeto utiliza JUnit 5 e Testcontainers para validar o comportamento do serviço em cenários reais de integração.

Principais cenários:

- Processamento de mensagens inéditas.
- Identificação e descarte de eventos duplicados.
- Retentativas automáticas após falhas.
- Encaminhamento de mensagens para a DLQ após esgotar as tentativas.
- Reprocessamento de mensagens armazenadas na DLQ.
- Publicação correta dos eventos de resposta.

---

## 🎯 Objetivos Arquiteturais

- Garantir idempotência no processamento de pagamentos.
- Evitar operações financeiras duplicadas.
- Implementar tolerância a falhas com retentativas automáticas.
- Isolar mensagens problemáticas utilizando Dead Letter Queue.
- Permitir recuperação operacional de mensagens.
- Garantir comunicação assíncrona e desacoplamento entre microsserviços.
- Aumentar a confiabilidade do processamento distribuído.

---

**Projeto desenvolvido com foco em arquitetura de microsserviços, mensageria assíncrona, idempotência e estratégias de resiliência distribuída.**
