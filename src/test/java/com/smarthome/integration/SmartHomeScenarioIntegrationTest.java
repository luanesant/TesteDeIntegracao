package com.smarthome.integration;

import com.smarthome.entity.*;
import com.smarthome.repository.*;
import com.smarthome.service.DeviceService;
import com.smarthome.service.LogService;
import com.smarthome.service.SensorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração - Cenários TST-03 a TST-10.
 * Valida integração real entre Service, Repository e banco H2.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SmartHomeScenarioIntegrationTest {

    @Autowired
    private SensorService sensorService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private LogService logService;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AutomationRuleRepository automationRuleRepository;

    @Autowired
    private LogEntryRepository logEntryRepository;

    private Long sensorId;
    private Long lampId;

    @BeforeEach
    void setUp() {
        cleanData();

        createSensor();
        createDevice(); 
        
        cleanAutomationRule();
    }


    private void cleanData(){
        logEntryRepository.deleteAll();
        automationRuleRepository.deleteAll();
        deviceRepository.deleteAll();
        sensorRepository.deleteAll();
    }

    private void createSensor(){
        Sensor sensor = new Sensor("Motion Sensor - Living Room", true);
        sensor = sensorRepository.save(sensor);
        sensorId = sensor.getId();
    }

    private void createDevice(){
        Device lamp = new Device("Lamp", DeviceStatus.OFF);
        lamp = deviceRepository.save(lamp);
        lampId = lamp.getId();
    }

    private void cleanAutomationRule(){
        AutomationRule rule = new AutomationRule(sensorId, lampId, true);
        automationRuleRepository.save(rule);
    }


    // TST-01: Integração Sensor → Serviço de Automação → Dispositivo
    @Test
    @DisplayName("TST-01: Integração Sensor → Serviço de Automação → Dispositivo")
    void shouldIntegrateSensorAutomationServiceAndDevice() {

        Sensor sensor = sensorRepository.findById(sensorId).get();
        assertNotNull(sensor, "Sensor deve existir no banco");
        assertTrue(sensor.isActive(), "Sensor deve estar ativo");

        Device deviceBefore = deviceRepository.findById(lampId).get();
        assertEquals(DeviceStatus.OFF, deviceBefore.getStatus(),
                "Dispositivo deve estar OFF antes da detecção de movimento");

        assertDoesNotThrow(() -> sensorService.detectMotion(sensorId),
                "O sensor deve processar a detecção de movimento sem lançar exceções");

        Device deviceAfter = deviceRepository.findById(lampId).get();
        assertNotNull(deviceAfter, "Dispositivo deve existir no banco após automação");
        assertEquals(DeviceStatus.ON, deviceAfter.getStatus(),
                "O dispositivo associado deve ter seu status alterado de OFF para ON");

        List<LogEntry> logs = logEntryRepository.findAll();
        assertEquals(1, logs.size(), "Um registro de log deve ser criado no banco de dados");
        assertEquals("Lamp automatically turned on", logs.get(0).getAction(),
                "A mensagem do log deve corresponder à ação executada");
        assertNotNull(logs.get(0).getTimestamp(),
                "O timestamp do log deve estar preenchido");

        assertTrue(logEntryRepository.existsByAction("Lamp automatically turned on"),
                "O log deve ser encontrado por ação no banco");
        assertEquals(DeviceStatus.ON, deviceRepository.findById(lampId).get().getStatus(),
                "A alteração do dispositivo deve estar persistida no banco");
    }


    // TST-02: Integridade dos Dados entre Sensor, Regra de Automação e Banco
    @Test
    @DisplayName("TST-02: Integridade dos Dados entre Sensor, Regra de Automação e Banco de Dados")
    void shouldMaintainDataIntegrityBetweenSensorRuleAndDatabase() {
        Sensor newSensor = new Sensor("Motion Sensor - Kitchen", true);
        Sensor savedSensor = sensorRepository.save(newSensor);
        assertNotNull(savedSensor.getId(), "Sensor deve receber ID ao ser persistido");

        Device newDevice = new Device("Kitchen Lamp", DeviceStatus.OFF);
        Device savedDevice = deviceRepository.save(newDevice);
        assertNotNull(savedDevice.getId(), "Dispositivo deve receber ID ao ser persistido");

        AutomationRule newRule = new AutomationRule(savedSensor.getId(), savedDevice.getId(), true);
        AutomationRule savedRule = automationRuleRepository.save(newRule);
        assertNotNull(savedRule.getId(), "Regra deve receber ID ao ser persistida");

        Sensor retrievedSensor = sensorRepository.findById(savedSensor.getId()).get();
        Device retrievedDevice = deviceRepository.findById(savedDevice.getId()).get();
        AutomationRule retrievedRule = automationRuleRepository.findById(savedRule.getId()).get();


        assertEquals("Motion Sensor - Kitchen", retrievedSensor.getName(),
                "O nome do sensor deve ser mantido após persistência");
        assertTrue(retrievedSensor.isActive(),
                "O status ativo do sensor deve ser mantido após persistência");
        assertEquals(savedSensor.getId(), retrievedSensor.getId(),
                "O ID do sensor deve corresponder ao gerado na gravação");

        assertEquals("Kitchen Lamp", retrievedDevice.getName(),
                "O nome do dispositivo deve ser mantido após persistência");
        assertEquals(DeviceStatus.OFF, retrievedDevice.getStatus(),
                "O status do dispositivo deve ser mantido após persistência");
        assertEquals(savedDevice.getId(), retrievedDevice.getId(),
                "O ID do dispositivo deve corresponder ao gerado na gravação");

        assertEquals(savedSensor.getId(), retrievedRule.getSensorId(),
                "O sensorId da regra deve corresponder ao sensor criado");
        assertEquals(savedDevice.getId(), retrievedRule.getDeviceId(),
                "O deviceId da regra deve corresponder ao dispositivo criado");
        assertTrue(retrievedRule.isEnabled(),
                "A regra deve manter o estado habilitado após persistência");

        assertEquals(savedSensor.getName(), retrievedSensor.getName());
        assertEquals(savedDevice.getName(), retrievedDevice.getName());
        assertEquals(savedDevice.getStatus(), retrievedDevice.getStatus());
        assertEquals(savedRule.getSensorId(), retrievedRule.getSensorId());
        assertEquals(savedRule.getDeviceId(), retrievedRule.getDeviceId());
        assertEquals(savedRule.isEnabled(), retrievedRule.isEnabled());
    }

    // TST-03: Listar dispositivos IoT
    @Test
    @DisplayName("TST-03: Deve listar dispositivos IoT cadastrados no banco corretamente")
    void shouldListAllIoTDevicesFromDatabase() {

        Device lampKitchen = new Device("Lamp Kitchen", DeviceStatus.OFF);
        deviceRepository.save(lampKitchen);

        Device lampGarage = new Device("Lamp Garage", DeviceStatus.ON);
        deviceRepository.save(lampGarage);

        List<Device> devices = deviceRepository.findAll();

        assertFalse(devices.isEmpty());
        assertTrue(devices.size() >= 3);
        boolean foundSetupDevice = devices.stream()
                .anyMatch(d -> d.getId().equals(lampId) && d.getName().equals("Lamp"));
        assertTrue(foundSetupDevice, "O dispositivo criado no setUp deve estar na lista");

        Device foundLamp = devices.stream()
                .filter(d -> d.getName().equals("Lamp Kitchen"))
                .findFirst()
                .orElse(null);
        assertNotNull(foundLamp);
        assertEquals(DeviceStatus.OFF, foundLamp.getStatus());

        Device foundGarage = devices.stream()
                .filter(d -> d.getName().equals("Lamp Garage"))
                .findFirst()
                .orElse(null);
        assertNotNull(foundGarage);
        assertEquals(DeviceStatus.ON, foundGarage.getStatus());
    }


    // TST-04: Buscar dispositivo por ID
    @Test
    @DisplayName("TST-04: Deve buscar dispositivo por ID e retornar dados corretos")
    void shouldFindDeviceByIdWithCorrectData() {
        Device device = deviceService.findById(lampId);

        assertNotNull(device);

        assertEquals(lampId, device.getId());

        assertEquals("Lamp", device.getName());
        assertEquals(DeviceStatus.OFF, device.getStatus());
    }

    // TST-05: Cadastrar novo dispositivo
    @Test
    @DisplayName("TST-05: Deve cadastrar novo dispositivo e persistir no banco")
    void shouldRegisterNewDeviceAndPersistInDatabase() {
        Device newDevice = new Device("Smart Thermostat", DeviceStatus.OFF);

        Device savedDevice = deviceRepository.save(newDevice);

        assertNotNull(savedDevice.getId());

        Optional<Device> foundInDb = deviceRepository.findById(savedDevice.getId());
        assertTrue(foundInDb.isPresent(), "O dispositivo deve existir no banco após persistência");

        Device persisted = foundInDb.get();
        assertEquals("Smart Thermostat", persisted.getName());
        assertEquals(DeviceStatus.OFF, persisted.getStatus());
        assertEquals(savedDevice.getId(), persisted.getId());
    }

    // TST-06: Criar automação inteligente
    @Test
    @DisplayName("TST-06: Deve criar regra de automação associando sensor e dispositivo corretamente")
    void shouldCreateAutomationRuleAssociatingSensorAndDevice() {
        Device newLamp = new Device("Lamp Corridor", DeviceStatus.OFF);
        newLamp = deviceRepository.save(newLamp);

        AutomationRule newRule = new AutomationRule(sensorId, newLamp.getId(), true);
        AutomationRule savedRule = automationRuleRepository.save(newRule);

        assertNotNull(savedRule.getId());
        Optional<AutomationRule> foundRule = automationRuleRepository.findById(savedRule.getId());
        assertTrue(foundRule.isPresent(), "A regra deve existir no banco");

        assertEquals(sensorId, foundRule.get().getSensorId());

        assertEquals(newLamp.getId(), foundRule.get().getDeviceId());

        assertTrue(foundRule.get().isEnabled(), "A regra deve iniciar habilitada");
    }

    // TST-07: Gerar evento de automação
    @Test
    @DisplayName("TST-07: Deve gerar evento de automação quando sensor ativo detecta movimento")
    void shouldGenerateAutomationEventWhenActiveSensorDetectsMotion() {
        sensorService.detectMotion(sensorId);

        Device device = deviceRepository.findById(lampId).get();
        assertEquals(DeviceStatus.ON, device.getStatus(),
                "O dispositivo deve mudar para ON após detecção de movimento");

        List<LogEntry> logs = logEntryRepository.findAll();
        assertEquals(1, logs.size(), "Deve existir exatamente um log criado");

        assertEquals("Lamp automatically turned on", logs.get(0).getAction(),
                "A ação registrada deve descrever o acionamento automático");
    }

    // TST-08: Consultar registros de eventos
    @Test
    @DisplayName("TST-08: Deve consultar registros de eventos (logs) gerados no sistema")
    void shouldQueryEventLogsFromDatabase() {
        sensorService.detectMotion(sensorId);

        logService.logAction("System health check executed");

        List<LogEntry> logs = logEntryRepository.findAll();

        assertNotNull(logs);

        assertFalse(logs.isEmpty(), "Deve existir pelo menos um registro de log");
        assertEquals(2, logs.size(), "Devem existir dois registros de log");

        assertTrue(logEntryRepository.existsByAction("Lamp automatically turned on"),
                "Deve existir log da automação");
        assertTrue(logEntryRepository.existsByAction("System health check executed"),
                "Deve existir log do health check");
    }

    // TST-09: Registrar log do sistema
    @Test
    @DisplayName("TST-09: Deve registrar log do sistema com ação e timestamp persistidos")
    void shouldRegisterSystemLogWithActionAndTimestamp() {
        String actionDescription = "Device firmware updated successfully";

        LogEntry savedLog = logService.logAction(actionDescription);

        assertNotNull(savedLog);
        assertNotNull(savedLog.getId());

        Optional<LogEntry> foundInDb = logEntryRepository.findById(savedLog.getId());
        assertTrue(foundInDb.isPresent(), "O log deve estar persistido no banco");

        assertEquals(actionDescription, foundInDb.get().getAction(),
                "A ação registrada deve corresponder ao valor informado");

        assertNotNull(foundInDb.get().getTimestamp(),
                "O timestamp deve ser persistido junto com o log");
    }

    // TST-10: Fluxo completo de automação
    @Test
    @DisplayName("TST-10: Deve executar fluxo completo de automação - sensor → regra → dispositivo → log")
    void shouldExecuteCompleteAutomationFlow() {
        Sensor newSensor = new Sensor("Motion Sensor - Garage", true);
        newSensor = sensorRepository.save(newSensor);

        Device garageLamp = new Device("Garage Lamp", DeviceStatus.OFF);
        garageLamp = deviceRepository.save(garageLamp);

        AutomationRule garageRule = new AutomationRule(newSensor.getId(), garageLamp.getId(), true);
        garageRule = automationRuleRepository.save(garageRule);

        sensorService.detectMotion(newSensor.getId());

        Sensor persistedSensor = sensorRepository.findById(newSensor.getId()).get();
        assertNotNull(persistedSensor);
        assertTrue(persistedSensor.isActive(), "Sensor deve estar ativo");
        assertEquals("Motion Sensor - Garage", persistedSensor.getName());

        Device persistedDevice = deviceRepository.findById(garageLamp.getId()).get();
        assertNotNull(persistedDevice);
        assertEquals(DeviceStatus.ON, persistedDevice.getStatus(),
                "Dispositivo deve estar ON após automação");

        AutomationRule persistedRule = automationRuleRepository.findById(garageRule.getId()).get();
        assertNotNull(persistedRule);
        assertTrue(persistedRule.isEnabled(), "Regra deve estar habilitada");
        assertEquals(newSensor.getId(), persistedRule.getSensorId());
        assertEquals(garageLamp.getId(), persistedRule.getDeviceId());

        assertTrue(logEntryRepository.existsByAction("Garage Lamp automatically turned on"),
                "Log da automação deve existir no banco");

        List<LogEntry> allLogs = logEntryRepository.findAll();
        LogEntry automationLog = allLogs.stream()
                .filter(log -> log.getAction().equals("Garage Lamp automatically turned on"))
                .findFirst()
                .orElse(null);
        assertNotNull(automationLog, "O log da automação deve estar persistido");
        assertNotNull(automationLog.getTimestamp(), "O timestamp do log deve estar preenchido");
        assertNotNull(automationLog.getId(), "O log deve ter ID gerado pelo banco");
    }

    // TST-11: Exclusão de Dispositivo e Verificação da Remoção no Banco
    @Test
    @DisplayName("TST-11: Exclusão de Dispositivo e Verificação da Remoção no Banco de Dados")
    void shouldDeleteDeviceAndVerifyRemovalFromDatabase() {
        Device deviceToDelete = new Device("Lamp", DeviceStatus.OFF);
        Device savedDevice = deviceRepository.save(deviceToDelete);
        Long deviceId = savedDevice.getId();
        assertNotNull(deviceId, "O dispositivo deve receber um ID ao ser cadastrado");

        Optional<Device> beforeDeletion = deviceRepository.findById(deviceId);
        assertTrue(beforeDeletion.isPresent(),
                "O dispositivo deve existir antes da exclusão");
        assertEquals("Lamp", beforeDeletion.get().getName());
        assertEquals(DeviceStatus.OFF, beforeDeletion.get().getStatus());

        List<AutomationRule> associatedRules = automationRuleRepository.findAll().stream()
                .filter(rule -> rule.getDeviceId().equals(deviceId))
                .toList();
        automationRuleRepository.deleteAll(associatedRules);

        assertDoesNotThrow(() -> deviceRepository.deleteById(deviceId),
                "A operação de remoção deve ser executada com sucesso");

        Optional<Device> afterDeletion = deviceRepository.findById(deviceId);

        assertFalse(afterDeletion.isPresent(),
                "O registro deve ser removido do banco de dados");
        assertTrue(afterDeletion.isEmpty(),
                "O repositório deve retornar resultado vazio para o ID removido");
    }


    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("TST-12: Deve incrementar a contagem de dispositivos após cadastro")
    void shouldIncrementDeviceCountAfterRegistration() {
        long countBefore = deviceRepository.count();

        Device newDevice = new Device("Smart Speaker", DeviceStatus.OFF);
        deviceRepository.save(newDevice);

        long countAfter = deviceRepository.count();
        assertEquals(countBefore + 1, countAfter);
    }

    @Test
    @DisplayName("TST-13: Deve lançar exceção ao buscar dispositivo com ID inexistente")
    void shouldThrowExceptionWhenDeviceNotFoundById() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> deviceService.findById(99999L)
        );

        assertTrue(exception.getMessage().contains("Device not found"));
    }

    @Test
    @DisplayName("TST-14: Deve encontrar regras ativas por sensor após criação")
    void shouldFindActiveRulesBySensorAfterCreation() {
        Device lamp2 = new Device("Lamp Office", DeviceStatus.OFF);
        lamp2 = deviceRepository.save(lamp2);

        AutomationRule rule2 = new AutomationRule(sensorId, lamp2.getId(), true);
        automationRuleRepository.save(rule2);

        List<AutomationRule> activeRules = automationRuleRepository.findBySensorIdAndEnabledTrue(sensorId);

        assertEquals(2, activeRules.size());
        assertTrue(activeRules.stream().allMatch(AutomationRule::isEnabled));
        assertTrue(activeRules.stream().allMatch(r -> r.getSensorId().equals(sensorId)));
    }

    @Test
    @DisplayName("TST-15: Deve gerar múltiplos eventos quando sensor possui várias regras ativas")
    void shouldGenerateMultipleEventsWhenSensorHasMultipleActiveRules() {

        Device lamp2 = new Device("Lamp Bathroom", DeviceStatus.OFF);
        lamp2 = deviceRepository.save(lamp2);
        automationRuleRepository.save(new AutomationRule(sensorId, lamp2.getId(), true));

        sensorService.detectMotion(sensorId);

        assertEquals(DeviceStatus.ON, deviceRepository.findById(lampId).get().getStatus());
        assertEquals(DeviceStatus.ON, deviceRepository.findById(lamp2.getId()).get().getStatus());

        List<LogEntry> logs = logEntryRepository.findAll();
        assertEquals(2, logs.size());
    }

    @Test
    @DisplayName("TST-16: Deve retornar lista vazia quando não existirem logs")
    void shouldReturnEmptyListWhenNoLogsExist() {
        List<LogEntry> logs = logEntryRepository.findAll();

        assertNotNull(logs);
        assertTrue(logs.isEmpty(), "Não deve existir nenhum log antes de eventos serem gerados");
    }
    
    @Test
    @DisplayName("TST-17: Deve verificar existência de log por ação após registro")
    void shouldVerifyLogExistenceByActionAfterRegistration() {
        logService.logAction("Sensor calibration completed");

        assertTrue(logService.existsByAction("Sensor calibration completed"),
                "O log deve ser encontrado pela ação registrada");
        assertFalse(logService.existsByAction("Ação inexistente"),
                "Não deve encontrar log com ação que não foi registrada");
    }

    @Test
    @DisplayName("TST-18: Fluxo completo com múltiplos dispositivos associados ao mesmo sensor")
    void shouldExecuteCompleteFlowWithMultipleDevices() {
        Sensor hallSensor = new Sensor("Motion Sensor - Hall", true);
        hallSensor = sensorRepository.save(hallSensor);

        Device hallLamp = new Device("Hall Lamp", DeviceStatus.OFF);
        hallLamp = deviceRepository.save(hallLamp);

        Device hallFan = new Device("Hall Fan", DeviceStatus.OFF);
        hallFan = deviceRepository.save(hallFan);

        Device hallCamera = new Device("Hall Camera", DeviceStatus.OFF);
        hallCamera = deviceRepository.save(hallCamera);

        automationRuleRepository.save(new AutomationRule(hallSensor.getId(), hallLamp.getId(), true));
        automationRuleRepository.save(new AutomationRule(hallSensor.getId(), hallFan.getId(), true));
        automationRuleRepository.save(new AutomationRule(hallSensor.getId(), hallCamera.getId(), true));

        sensorService.detectMotion(hallSensor.getId());

        assertEquals(DeviceStatus.ON, deviceRepository.findById(hallLamp.getId()).get().getStatus());
        assertEquals(DeviceStatus.ON, deviceRepository.findById(hallFan.getId()).get().getStatus());
        assertEquals(DeviceStatus.ON, deviceRepository.findById(hallCamera.getId()).get().getStatus());

        assertTrue(logEntryRepository.existsByAction("Hall Lamp automatically turned on"));
        assertTrue(logEntryRepository.existsByAction("Hall Fan automatically turned on"));
        assertTrue(logEntryRepository.existsByAction("Hall Camera automatically turned on"));

        List<LogEntry> logs = logEntryRepository.findAll();
        long hallLogs = logs.stream()
                .filter(l -> l.getAction().contains("Hall"))
                .count();
        assertEquals(3, hallLogs, "Devem existir 3 logs para os dispositivos do Hall");
    }
}
