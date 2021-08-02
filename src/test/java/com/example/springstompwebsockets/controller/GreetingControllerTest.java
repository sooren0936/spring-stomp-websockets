package com.example.springstompwebsockets.controller;

import com.example.springstompwebsockets.entity.Greeting;
import com.example.springstompwebsockets.entity.HelloMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Suren Kalaychyan
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GreetingControllerTest {

    @Autowired
    private GreetingController greetingController;

    @LocalServerPort
    private Integer port;

    private WebSocketStompClient webSocketStompClient;

    final BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

    private StompFrameHandler prepareTestStompFrameHandler() {
        return new StompFrameHandler() {

            @Override
            public Type getPayloadType(final StompHeaders headers) {
                return Greeting.class;
            }

            @Override
            public void handleFrame(final StompHeaders headers, final Object payload) {
                System.out.println("Received message: " + payload);
                blockingQueue.add(((Greeting) payload).getContent());
            }
        };
    }

    private String prepareTestWsPath() {
        return String.format("ws://localhost:%d/websocket", port);
    }

    @BeforeEach
    public void setup() {
        this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    }

    @Test
    public void verifyGreetingIsReceived() throws Exception {
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        final StompSession session = webSocketStompClient
            .connect(prepareTestWsPath(), new StompSessionHandlerAdapter() {
            })
            .get(1, SECONDS);
        session.subscribe(String.format("/topic/greetings/%d", 1), prepareTestStompFrameHandler());
        session.send(String.format("/app/hello/%d", 1), new HelloMessage("Mike"));

        assertEquals("Hello, Mike!", blockingQueue.poll(1, SECONDS));

        greetingController.updateGreeting();
        System.out.println(blockingQueue.poll(1, SECONDS));
    }
}
