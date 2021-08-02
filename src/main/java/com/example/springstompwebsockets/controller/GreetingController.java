package com.example.springstompwebsockets.controller;

import com.example.springstompwebsockets.entity.Greeting;
import com.example.springstompwebsockets.entity.HelloMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.HtmlUtils;

/**
 * @author Suren Kalaychyan
 */
@Controller
@RequestMapping("/test")
public class GreetingController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public GreetingController(final SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void updateGreeting() {
        simpMessagingTemplate.convertAndSend(String.format("/topic/greetings/%d", 1), new Greeting("Hello from Scheduled"));
    }

    @MessageMapping("/hello/{id}")
    @SendTo("/topic/greetings/{id}")
    public Greeting greeting(@DestinationVariable("id") final int id, final HelloMessage message) {
        return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }
}
