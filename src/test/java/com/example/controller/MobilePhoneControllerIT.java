package com.example.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.Main;
import com.example.config.ApplicationConfig;
import com.example.config.RabbitMQConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.RabbitMQContainer;


@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    classes = Main.class)
@AutoConfigureMockMvc
@TestPropertySource(
    locations = "classpath:application.yaml")
@Sql(scripts = {"classpath:clear_test_data.sql", "classpath:test_data.sql"},
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ContextConfiguration(initializers = {MobilePhoneControllerIT.Initializer.class})
public class MobilePhoneControllerIT {

    private final static String RABBIT_RETURN_QUEUE = "return-phone-queue";
    private final static String RABBIT_BOOK_QUEUE = "book-phone-queue";


    @Autowired
    private ApplicationConfig applicationConfig;

    static RabbitMQContainer container;


    @Autowired
    RabbitMQConfig rabbitMQConfig;

    @Rule
    public OutputCaptureRule outputCapture = new OutputCaptureRule();


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @BeforeClass
    public static void setup() {
        container = new RabbitMQContainer("rabbitmq:3-management");
        container.start();
    }

    static boolean queuesInitialised = false;

    /**
     * Had to use RabbitAdmin rather than container.withQueue container.WithExchange and container.withBinding as we
     * needed to get exchange names from applicationconfig.
     */
    @Before
    public void init() {
        if (!queuesInitialised) {
            Exchange retrunPhoneExchange = new DirectExchange(applicationConfig.getReturnPhoneExchange());
            Queue returnQueue = new Queue(RABBIT_RETURN_QUEUE);
            Binding returnQueueExchangBinding = new Binding(returnQueue.getName(), DestinationType.QUEUE,
                retrunPhoneExchange.getName(), "", null);

            rabbitAdmin.declareExchange(retrunPhoneExchange);
            rabbitAdmin.declareQueue(returnQueue);
            rabbitAdmin.declareBinding(returnQueueExchangBinding);

            Exchange bookPhoneExchange = new DirectExchange(applicationConfig.getBookPhoneExchange());
            Queue bookQueue = new Queue(RABBIT_BOOK_QUEUE);
            Binding bookQueueExchangBinding = new Binding(bookQueue.getName(), DestinationType.QUEUE,
                bookPhoneExchange.getName(), "", null);

            rabbitAdmin.declareExchange(bookPhoneExchange);
            rabbitAdmin.declareQueue(bookQueue);
            rabbitAdmin.declareBinding(bookQueueExchangBinding);

            queuesInitialised = true;
        }

    }


    static class Initializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of("spring.rabbitmq.port=" + container.getAmqpPort())
                .and
                    ("spring.rabbitmq.password=" + container.getAdminPassword())
                .and("spring.rabbitmq.username=" + container.getAdminUsername())
                .applyTo(configurableApplicationContext.getEnvironment());
        }
    }


    @WithMockUser(username = "user", authorities = {"USER"})
    @Test
    public void testBookPhoneSuccess() throws Exception {
        String imei = "211111111111111";
        mockMvc.perform(post("/api/mobile/" + imei + "/book")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        Assert.assertEquals(imei + " is booked",
            rabbitTemplate.receiveAndConvert(RABBIT_BOOK_QUEUE));
    }

    @WithMockUser(username = "user", authorities = {"USER"})
    @Test
    public void testBookPhoneAlreadyBooked() throws Exception {
        mockMvc.perform(post("/api/mobile/322222222222222/book")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
        String message = (String) rabbitTemplate.receiveAndConvert(RABBIT_BOOK_QUEUE);
        Assert.assertNull(message);

    }

    @WithMockUser(username = "user", authorities = {"USER"})
    @Test
    public void testBookPhoneNotFound() throws Exception {
        mockMvc.perform(post("/api/mobile/aaaa/book")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        String message = (String) rabbitTemplate.receiveAndConvert(RABBIT_BOOK_QUEUE);
        Assert.assertNull(message);
    }

    @Test
    public void testBookPhoneNotAuthorized() throws Exception {
        mockMvc.perform(post("/api/mobile/aaaa/book")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
        String message = (String) rabbitTemplate.receiveAndConvert(RABBIT_BOOK_QUEUE);
        Assert.assertNull(message);
    }


    @WithMockUser(username = "user", authorities = {"USER"})
    @Test
    public void testRetunPhoneSuccess() throws Exception {
        String imei = "522222222222222";
        mockMvc.perform(post("/api/mobile/" + imei + "/return")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        String message = (String) rabbitTemplate.receiveAndConvert(RABBIT_RETURN_QUEUE);
        Assert.assertEquals(imei + " is returned", message);
    }

    @WithMockUser(username = "user", authorities = {"USER"})
    @Test
    public void testBookPhoneAlreadyReturned() throws Exception {
        mockMvc.perform(post("/api/mobile/422222222222222/return")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
        String message = (String) rabbitTemplate.receiveAndConvert(RABBIT_RETURN_QUEUE);
        Assert.assertNull(message);

    }

    @WithMockUser(username = "user", authorities = {"USER"})
    @Test
    public void testReturnPhoneNotFound() throws Exception {

        mockMvc.perform(post("/api/mobile/aaaa/return")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        String message = (String) rabbitTemplate.receiveAndConvert(RABBIT_RETURN_QUEUE);
        Assert.assertNull(message);

    }
}