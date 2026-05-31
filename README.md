# Smart Home Automation - Testes de Integração

Projeto Java com Spring Boot, JUnit 5 e banco H2 em memória, focado em **Testes de Integração reais** entre múltiplas camadas do sistema.

## Tema: Smart Home - Acionamento Automático de Luz

Quando um sensor detecta movimento, o sistema automaticamente:
1. Verifica regras de automação ativas
2. Liga as lâmpadas associadas
3. Registra a ação no log

## Tecnologias

- Java 17
- Spring Boot 3.2.5
- JUnit 5
- H2 Database (em memória)
- Spring Data JPA
- Maven

## Estrutura do Projeto

```
src/
├── main/java/com/smarthome/
│   ├── SmartHomeApplication.java
│   ├── config/
│   │   └── GlobalExceptionHandler.java
│   ├── controller/
│   │   └── SmartHomeController.java
│   ├── dto/
│   │   └── MotionDetectedResponse.java
│   ├── entity/
│   │   ├── AutomationRule.java
│   │   ├── Device.java
│   │   ├── DeviceStatus.java
│   │   ├── LogEntry.java
│   │   └── Sensor.java
│   ├── exception/
│   │   ├── SensorInactiveException.java
│   │   └── SensorNotFoundException.java
│   ├── repository/
│   │   ├── AutomationRuleRepository.java
│   │   ├── DeviceRepository.java
│   │   ├── LogEntryRepository.java
│   │   └── SensorRepository.java
│   └── service/
│       ├── AutomationService.java
│       ├── DeviceService.java
│       ├── LogService.java
│       └── SensorService.java
└── test/java/com/smarthome/
    ├── SmartHomeApplicationTests.java
    └── integration/
        ├── SmartHomeControllerIntegrationTest.java
        ├── SmartHomeIntegrationTest.java
        └── SmartHomeScenarioIntegrationTest.java
```

## Módulos Integrados no Projeto

O objetivo dos testes de integração deste projeto é validar a comunicação real entre as diferentes camadas da aplicação Smart Home, garantindo que os componentes funcionem corretamente em conjunto sem utilização de mocks.

### Arquitetura dos Módulos

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Banco H2
```

### 1. Sensor Module

Responsável pelo gerenciamento dos sensores do sistema.

Principais responsabilidades:
- Armazenar informações dos sensores.
- Verificar se um sensor está ativo.
- Detectar eventos de movimento.
- Disparar o processo de automação.

Classes principais:

```text
Sensor
SensorRepository
SensorService
```

---

### 2. Automation Module

Responsável pelas regras de automação do sistema.

Principais responsabilidades:
- Associar sensores a dispositivos.
- Verificar regras habilitadas.
- Determinar quais dispositivos devem ser acionados.

Classes principais:

```text
AutomationRule
AutomationRuleRepository
AutomationService
```

---

### 3. Device Module

Responsável pelo gerenciamento dos dispositivos inteligentes.

Principais responsabilidades:
- Cadastrar dispositivos.
- Consultar dispositivos.
- Alterar status dos dispositivos.
- Executar ações automáticas.

Classes principais:

```text
Device
DeviceRepository
DeviceService
```

---

### 4. Log Module

Responsável pelo histórico de eventos do sistema.

Principais responsabilidades:
- Registrar ações executadas.
- Armazenar logs de automação.
- Permitir consultas dos eventos registrados.

Classes principais:

```text
LogEntry
LogEntryRepository
LogService
```

---

### 5. API Module

Responsável pela exposição dos recursos da aplicação através de endpoints HTTP.

Principais responsabilidades:
- Receber requisições externas.
- Acionar os serviços da aplicação.
- Retornar respostas ao cliente.

Classes principais:

```text
SmartHomeController
MotionDetectedResponse
GlobalExceptionHandler
```

---

## Fluxos de Integração Validados

### Fluxo Principal

Validado pelos testes da classe `SmartHomeIntegrationTest`.

```text
Sensor
   ↓
SensorService
   ↓
AutomationService
   ↓
AutomationRuleRepository
   ↓
DeviceRepository
   ↓
LogService
   ↓
LogEntryRepository
   ↓
Banco H2
```

Resultado esperado:

- Sensor detecta movimento.
- Regras de automação são consultadas.
- Dispositivos associados são acionados.
- Logs são gerados.
- Dados são persistidos no banco H2.

---

### Fluxo Controller → Service → Repository

Validado pelos testes da classe `SmartHomeControllerIntegrationTest`.

```text
HTTP Request
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
Banco H2
```

Resultado esperado:

- Requisições HTTP são processadas corretamente.
- Regras de negócio são executadas.
- Dados são persistidos e recuperados do banco.

---

### Fluxo Completo do Sistema

Validado pelos cenários TST-01 a TST-18.

```text
Sensor
   ↓
Serviço de Automação
   ↓
Regra de Automação
   ↓
Dispositivo
   ↓
Log
   ↓
Banco H2
```

Este fluxo representa a integração completa entre os módulos do sistema Smart Home e constitui o principal objetivo dos testes de integração implementados no projeto.

## Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.8+

### Executar os testes
```bash
mvn clean test
```

### Executar a aplicação
```bash
mvn spring-boot:run
```

## Executando os Testes pelo VS Code

### Pré-requisitos

- Extensão **Extension Pack for Java**
- Java 17 instalado
- Maven configurado
- Projeto aberto no VS Code

### Executar todos os testes

1. Abra o projeto no VS Code.
2. Acesse a aba **Testing** (ícone de béquer 🧪 na barra lateral).
3. Clique em **Run Tests** para executar toda a suíte de testes.

Ou utilize o terminal integrado:

```bash
mvn clean test
```

### Executar uma classe de teste específica

Navegue até:

```text
src/test/java/com/smarthome/integration/
```

Abra uma das classes:

- SmartHomeIntegrationTest (Testes de Integração principal)
- SmartHomeScenarioIntegrationTest (Testes de Integração dos casos de testes)
- SmartHomeControllerIntegrationTest (Testes de Integração de Automação)

Clique no botão **Run Test** exibido acima da classe ou método desejado.

### Executar um cenário específico (TST)

Os cenários podem ser executados individualmente através do botão **Run Test** disponível acima de cada método anotado com `@Test`.

Exemplos:

```java
@Test
@DisplayName("TST-01: Integração Sensor → Serviço de Automação → Dispositivo")
void shouldIntegrateSensorAutomationServiceAndDevice()
```

```java
@Test
@DisplayName("TST-10: Deve executar fluxo completo de automação - sensor → regra → dispositivo → log")
void shouldExecuteCompleteAutomationFlow()
```

### Visualizar Resultados

Após a execução, o VS Code exibirá:

- ✅ Testes aprovados
- ❌ Testes com falha
- Tempo de execução
- Stack trace detalhada em caso de erro

### Cobertura dos Testes

A suíte atual contempla os seguintes cenários:

| ID | Descrição |
|----|-----------|
| TST-01 | Integração Sensor → Automação → Dispositivo |
| TST-02 | Integridade dos Dados |
| TST-03 | Listar Dispositivos |
| TST-04 | Buscar Dispositivo por ID |
| TST-05 | Cadastrar Dispositivo |
| TST-06 | Criar Automação |
| TST-07 | Gerar Evento de Automação |
| TST-08 | Consultar Logs |
| TST-09 | Registrar Log |
| TST-10 | Fluxo Completo de Automação |
| TST-11 | Exclusão de Dispositivo |
| TST-12 | Incrementar Quantidade de Dispositivos |
| TST-13 | Buscar Dispositivo Inexistente |
| TST-14 | Buscar Regras Ativas |
| TST-15 | Múltiplos Eventos de Automação |
| TST-16 | Consulta de Logs Vazia |
| TST-17 | Verificação de Existência de Log |
| TST-18 | Fluxo Completo com Múltiplos Dispositivos |

## Testes de Integração

Os testes validam a comunicação real entre todas as camadas sem uso de mocks:
- **Sensor** → **Automação** → **Dispositivo** → **Log** → **Banco H2**.

Anotações utilizadas:
- `@SpringBootTest` - Carrega contexto completo do Spring
- `@Transactional` - Rollback automático entre testes
- `@ActiveProfiles("test")` - Perfil de teste com H2

---

### Classe: SmartHomeIntegrationTest

Testes do fluxo principal de automação.

| Cenário | Descrição | Resultado Esperado |
|---------|-----------|-------------------|
| Principal | Movimento detectado com regra ativa | Lâmpada ON + Log criado |
| Cenário 1 | Sem regra de automação ativa | Lâmpada OFF + Sem log |
| Cenário 2 | Sensor desativado | Exceção lançada + Sem alteração |
| Cenário 3 | Múltiplas lâmpadas associadas | Todas ON + Log para cada |
| Persistência | Validação completa dos dados | Todos os dados persistidos |

---

### Classe: SmartHomeScenarioIntegrationTest

Cenários de teste TST-01 a TST-11.

| ID | Cenário | Objetivo | Validações |
|----|---------|----------|------------|
| TST-01 | Integração Sensor → Serviço de Automação → Dispositivo | Validar fluxo completo de integração entre sensor, automação e dispositivo | Sensor processa sem exceção; dispositivo OFF→ON; log criado; mensagem correta; persistência confirmada |
| TST-02 | Integridade dos Dados entre Sensor, Regra e Banco | Validar que dados persistidos mantêm integridade após gravação e recuperação | Sensor armazenado corretamente; dispositivo armazenado; regra persistida; campos inalterados |
| TST-03 | Listar dispositivos IoT | Validar que dispositivos cadastrados são retornados corretamente | Existe pelo menos um dispositivo; dispositivo do setup encontrado; dados corretos |
| TST-04 | Buscar dispositivo por ID | Validar busca de dispositivo existente | Dispositivo encontrado; ID corresponde; nome e status corretos |
| TST-05 | Cadastrar novo dispositivo | Validar persistência de novo dispositivo | Recebe ID; salvo no banco; dados persistidos correspondem |
| TST-06 | Criar automação inteligente | Validar criação de regra associando sensor e dispositivo | Regra persistida; sensor correto; dispositivo correto; inicia habilitada |
| TST-07 | Gerar evento de automação | Validar que sensor ativo gera execução da automação | Dispositivo muda para ON; log criado; ação registrada correta |
| TST-08 | Consultar registros de eventos | Validar consulta dos logs gerados | Logs recuperados do banco; pelo menos um registro; descrição correta |
| TST-09 | Registrar log do sistema | Validar persistência de um log | Log salvo; ação corresponde ao valor; timestamp persistido |
| TST-10 | Fluxo completo de automação | Validar fluxo integrado completo | Sensor existe; dispositivo ON; regra existe; log criado; dados persistidos |
| TST-11 | Exclusão de Dispositivo e Verificação da Remoção | Validar remoção de dispositivo e confirmação no banco | Dispositivo existe antes; remoção sem erro; registro removido; consulta retorna vazio |
| TST-12 | Incrementar Quantidade de Dispositivos | Validar incremento da quantidade após cadastro. | Validar quantidade aumenta em uma unidade. |
| TST-13 | Buscar Dispositivo Inexistente | Validar tratamento de erro. | Exceção lançada; mensagem "Device not found". |
| TST-14 | Buscar Regras Ativas por Sensor | Validar recuperação de regras habilitadas. | Todas as regras retornadas estão habilitadas; todas pertencem ao sensor informado. |
| TST-15 | Múltiplos Eventos de Automação | Validar automação com múltiplas regras. | Todos os dispositivos acionados; logs criados para cada dispositivo. |
| TST-16 | Consultar Logs Vazios | Validar comportamento sem eventos. | Lista vazia retornada; nenhuma exceção lançada. |
| TST-17 | Verificar Existência de Log por Ação | Validar pesquisa de logs. | Log encontrado para ação existente; log não encontrado para ação inexistente. |
| TST-18 | Fluxo Completo com Múltiplos Dispositivos | Validar cenário expandido. | Todos os dispositivos acionados; três logs criados; persistência confirmada. |
---

### Classe: SmartHomeControllerIntegrationTest

Testes de integração via requisição HTTP (Controller → Service → Repository → H2).

| Cenário | Endpoint | Resultado Esperado |
|---------|----------|-------------------|
| Acionar lâmpada via HTTP | POST /sensors/{id}/motion | 200 OK + Lâmpada ON + Log criado |
| Sensor inativo via HTTP | POST /sensors/{id}/motion | 400 Bad Request + Sem alteração |
| Sensor inexistente | POST /sensors/9999/motion | 404 Not Found |
| Listar sensores | GET /sensors | 200 OK + Lista de sensores |
| Listar dispositivos | GET /devices | 200 OK + Lista de dispositivos |

---


## API Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/smarthome/sensors/{id}/motion` | Detectar movimento |
| GET | `/api/smarthome/sensors` | Listar sensores |
| GET | `/api/smarthome/devices` | Listar dispositivos |
